package com.honeycomb.colorphone.recentapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.acb.utils.ConcurrentUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sundxing on 2018/1/31.
 */

public class AppUsageOp {
    private final Object mLock = new Object();
    private final SQLiteDatabase mDb;
    private List<AppUsage> mAppUsages = new ArrayList<>();
    private volatile boolean dataSync = false;

    public AppUsageOp() {
        mDb = new RecentAppDBHelper(HSApplication.getContext()).getWritableDatabase();
    }

    public void sync() {
        if (dataSync) {
            return;
        }
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                List<AppUsage> appUsages = getAppUsagesFromDb();
                synchronized (mLock) {
                    mAppUsages.clear();
                    mAppUsages.addAll(appUsages);
                }
                dataSync = true;
            }
        });

    }

    private List<AppUsage> getAppUsagesFromDb() {
        Cursor c = null;
        List<AppUsage> appUsages = new ArrayList<>();
        try {
            c = mDb.rawQuery("SELECT * FROM " + AppUsage.AppUsageEntry.TABLE_NAME, null);
            if (!c.moveToLast()) {
                return appUsages;
            }

            if (BuildConfig.DEBUG) {
                int count = c.getCount();
                HSLog.d("Read App Usage count : " + count);
            }

            do {
                String pkg = c.getString(c.getColumnIndex(AppUsage.AppUsageEntry.COLUMN_NAME_PACKAGE_NAME));
                long launchTime = c.getLong(c.getColumnIndex(AppUsage.AppUsageEntry.COLUMN_NAME_LAUNCHTIME_LAST));

                updateAppUsageCache(appUsages, pkg, launchTime);

            } while (c.moveToPrevious());
        } catch (SQLiteException e) {
            // ignore
            if (BuildConfig.DEBUG) {
                throw e;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return appUsages;
    }

    private void updateAppUsageCache(List<AppUsage> appUsages, String packageName, long launchTime) {
        AppUsage target = null;
        for (AppUsage appUsage : appUsages) {
            if (TextUtils.equals(appUsage.getPackageName(), packageName)) {
                target = appUsage;
            }
        }

        // Update cache.
        synchronized (mLock) {
            if (target == null) {
                target = new AppUsage(packageName, launchTime);
                appUsages.add(target);
            } else {
                // Update
                target.setRecentLaunchTime(launchTime);
            }
        }
    }

    public List<AppUsage> getAppUsageListRecently() {
        List<AppUsage> resultList = new ArrayList<>();

        synchronized (mLock) {
            Collections.sort(mAppUsages, new Comparator<AppUsage>() {
                @Override
                public int compare(AppUsage o1, AppUsage o2) {
                    return (int) (o2.getLastTimeUsed() - o1.getLastTimeUsed());
                }
            });
        }

        resultList.addAll(mAppUsages);

        return resultList;
    }

    public List<AppUsage> getAppUsageListFrequently(final int includeDays) {
        List<AppUsage> resultList = new ArrayList<>();

        synchronized (mLock) {
            Collections.sort(mAppUsages, new Comparator<AppUsage>() {
                @Override
                public int compare(AppUsage o1, AppUsage o2) {
                    return (o2.getLaunchCountByDays(includeDays) - o1.getLaunchCountByDays(includeDays));
                }
            });
        }

        resultList.addAll(mAppUsages);

        return resultList;
    }

    public void recordAppOnForeground(final String packageName, final long timeStamp) {
        // Update cache.
        updateAppUsageCache(mAppUsages, packageName, timeStamp);

        // Update db.
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                insert(packageName, timeStamp);
            }
        });
    }

    private void insert(String packageName, long timeStamp) {
        HSLog.d("Recent Apps", "App display: " + packageName);
        final ContentValues contentValues = new ContentValues();
        contentValues.put(AppUsage.AppUsageEntry.COLUMN_NAME_PACKAGE_NAME, packageName);
        contentValues.put(AppUsage.AppUsageEntry.COLUMN_NAME_LAUNCHTIME_LAST, timeStamp);
        mDb.insert(AppUsage.AppUsageEntry.TABLE_NAME, null, contentValues);
    }

    private void delete(AppUsage appUsage) {
        mDb.delete(AppUsage.AppUsageEntry.TABLE_NAME,
                AppUsage.AppUsageEntry.COLUMN_NAME_PACKAGE_NAME + " = ?", new String[]{appUsage.getPackageName()});
        HSLog.d("Recent Apps", "App usage remove from db : " + appUsage.getPackageName());
    }

    public void onAppUninstall(String packageName) {
        HSLog.d("Recent Apps", "App uninstalled: " + packageName);

        AppUsage targetDelete = null;
        synchronized (mLock) {
            Iterator<AppUsage> iterator = mAppUsages.iterator();
            while (iterator.hasNext()) {
                AppUsage appUsage = iterator.next();
                if (TextUtils.equals(appUsage.getPackageName(), packageName)) {
                    targetDelete = appUsage;
                    iterator.remove();
                    break;
                }
            }
        }

        if (targetDelete != null) {
            final AppUsage appToDelete = targetDelete;
            ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    delete(appToDelete);
                }
            });
        }
    }
}

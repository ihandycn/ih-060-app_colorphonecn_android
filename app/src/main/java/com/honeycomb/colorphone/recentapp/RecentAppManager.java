package com.honeycomb.colorphone.recentapp;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.monitor.topapp.HSTopAppManager;
import com.ihs.device.monitor.topapp.HSUsageAccessMgr;
import com.ihs.device.monitor.topapp.TopAppManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sundxing on 2018/1/31.
 */

public class RecentAppManager {
    private static final String TOP_MANAGER_TAG = "ColorPhone_RecentApp";

    private static RecentAppManager INSTANCE = new RecentAppManager();

    public static RecentAppManager getInstance() {
        return INSTANCE;
    }

    private AppUsageOp mAppUsageOp;
    private String mCurrentAppPkgName;
    private HSTopAppManager.TopAppListener mTopAppListener = new HSTopAppManager.TopAppListener() {
        @Override
        public void onChanged(String packageName) {
            HSLog.i("RecentLog", "onChanged() packageName = " + packageName);
            if (!TextUtils.isEmpty(packageName)) {
                if (!TextUtils.isEmpty(mCurrentAppPkgName)) {
                    HSLog.i("RecentLog", "onChanged() mCurrentAppPkgName = " + mCurrentAppPkgName);
                    RecentAppManager.this.mAppUsageOp.recordAppOnForeground(mCurrentAppPkgName, System.currentTimeMillis());
                }
                RecentAppManager.this.mCurrentAppPkgName = packageName;
            }
        }

        @Override
        public void onStateChanged(int state) {
            // Nothing
        }
    };

    private HSUsageAccessMgr.PermissionListener usageAccessListener = new HSUsageAccessMgr.PermissionListener() {
        public void onPermissionChanged(boolean isGranted) {
            HSLog.i("RecentLog", "RecentAppManager.onPermissionChanged() isGranted = " + isGranted);
            RecentAppManager.this.updateMonitorStyle();
        }
    };

    private void updateMonitorStyle() {
        HSLog.i("RecentLog", "RecentAppManager.updateMonitorStyle()");
        if (HSUsageAccessMgr.getInstance().isPermissionGranted()) {
            HSTopAppManager.getInstance().unregister(this.mTopAppListener);
            HSTopAppManager.getInstance().stop(TOP_MANAGER_TAG);
            HSLog.i("RecentLog", "RecentAppManager.updateMonitorStyle() have UsageAccess");
        } else {
            HSTopAppManager.getInstance().register(this.mTopAppListener);
            HSTopAppManager.getInstance().start(5000, TOP_MANAGER_TAG);

        }
    }

    public void start() {
        updateMonitorStyle();
        if (mAppUsageOp == null) {
            mAppUsageOp = new AppUsageOp();
        }
        mAppUsageOp.sync();
        HSUsageAccessMgr.getInstance().checkPermission(this.usageAccessListener);
    }

    public void stop() {
        TopAppManager.getInstance().stopMonitor(TOP_MANAGER_TAG);
        HSTopAppManager.getInstance().unregister(this.mTopAppListener);
        HSUsageAccessMgr.getInstance().uncheckPermission(this.usageAccessListener);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public List<String> getAppUsageListRecently(int count) {
        List<String> recentAppList = new ArrayList<>(count);

        if (HSUsageAccessMgr.getInstance().isPermissionGranted()) {
            List<UsageStats> usageStatsList = UsageStatsUtil.getUsageStatsList(HSApplication.getContext(), 7);
            Collections.sort(usageStatsList, new Comparator<UsageStats>() {
                @Override
                public int compare(UsageStats o1, UsageStats o2) {
                    return (int) (o2.getLastTimeUsed() - o1.getLastTimeUsed());
                }
            });

            int realSize = Math.min(count, usageStatsList.size());
            for (int i = 0; i < realSize; i++) {
                recentAppList.add(usageStatsList.get(i).getPackageName());
            }
        } else {
            List<AppUsage> appUsages = mAppUsageOp.getAppUsageListRecently(count);
            int realSize = Math.min(count, appUsages.size());
            for (int i = 0; i < realSize; i++) {
                recentAppList.add(appUsages.get(i).getPackageName());
            }
        }

        return recentAppList;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private List<UsageStateWrapper> wrapUsageStats(List<UsageStats> usageStatses) {
        List<UsageStateWrapper> usageStateWrappers = new ArrayList<>(usageStatses.size());
        for (UsageStats stats : usageStatses) {
            usageStateWrappers.add(new UsageStateWrapper(stats));
        }
        return usageStateWrappers;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public List<String> getAppUsageListFrequently(int count) {
        List<String> mostlyUsedAppList = new ArrayList<>(count);
        if (HSUsageAccessMgr.getInstance().isPermissionGranted()) {
            List<UsageStats> usageStatsList = UsageStatsUtil.getUsageStatsList(HSApplication.getContext(), 7);
            List<UsageStateWrapper> usageStateWrappers = wrapUsageStats(usageStatsList);
            Collections.sort(usageStateWrappers, new Comparator<IUsageStat>() {
                @Override
                public int compare(IUsageStat o1, IUsageStat o2) {
                    return (int) (o2.getLastTimeUsed() - o1.getLastTimeUsed());
                }
            });
            int realSize = Math.min(count, usageStateWrappers.size());
            for (int i = 0; i < realSize; i++) {
                mostlyUsedAppList.add(usageStateWrappers.get(i).getPackageName());
            }
        } else {
            List<AppUsage> appUsages = mAppUsageOp.getAppUsageListFrequently(count, 7);
            int realSize = Math.min(count, appUsages.size());
            for (int i = 0; i < realSize; i++) {
                mostlyUsedAppList.add(appUsages.get(i).getPackageName());
            }
        }
        return mostlyUsedAppList;
    }
}

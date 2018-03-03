package com.honeycomb.colorphone.boost;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.acb.utils.ConcurrentUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.PackageList;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *  All apps here is has default launch activity.
 */
public class SystemAppsManager {

    private ArrayList<AppInfo> allAppInfos;
    private ArrayList<String> allAppPackageNames;

    private static SystemAppsManager instance = new SystemAppsManager();
    PackageManager pkgMgr;

    private SystemAppsManager() {
        allAppPackageNames = new ArrayList<>();
        allAppInfos = new ArrayList<>();

        pkgMgr = HSApplication.getContext().getPackageManager();
    }

    public static SystemAppsManager getInstance() {
        return instance;
    }

    public void init() {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                initAllInstalledAppInfo();
            }
        });
    }

    private List<AppInfo> initAllInstalledAppInfo() {
        List<PackageInfo> installedPackages = pkgMgr.getInstalledPackages(0);
        HSLog.w("notification", "init Allapps == " + installedPackages.size());

        synchronized (this) {
            allAppInfos.clear();
            allAppPackageNames.clear();
        }

        for (PackageInfo info : installedPackages) {
            addAppInfo(info);
            // For analysis
            PackageList.checkAndLogPackage(info.packageName);
        }
        HSLog.w("notification", "init Allappd == " + allAppPackageNames.size());
        return allAppInfos;
    }

    private void addAppInfo(PackageInfo info) {
        AppInfo app;
        if (info.applicationInfo != null && !allAppPackageNames.contains(info.packageName)) {
            ActivityInfo launchActivityInfo = getLaunchActivityInfo(info.packageName, pkgMgr);
            if (null != launchActivityInfo) {
                app = new AppInfo(info, false);
                app.setLaunchActivityName(launchActivityInfo.name);
                synchronized (this) {
                    allAppInfos.add(app);
                    allAppPackageNames.add(info.packageName);
                }
            }
        }
    }

    public void addPackage(String pkgName) {
        try {
            PackageInfo packageInfo = pkgMgr.getPackageInfo(pkgName, 0);
            if (packageInfo.applicationInfo != null) {
                addAppInfo(packageInfo);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void removePackage(String pkgName) {
        Iterator<AppInfo> iterator = allAppInfos.iterator();
        while (iterator.hasNext()) {
            AppInfo info = iterator.next();
            if (TextUtils.equals(info.getPackageName(), pkgName)) {
                iterator.remove();
            }
        }

        allAppPackageNames.remove(pkgName);
    }

    public List<String> getAllAppPackageNames() {
        return new ArrayList<>(allAppPackageNames);
    }

    public List<AppInfo> getAllAppInfos() {
        return new ArrayList<>(allAppInfos);
    }


    private static ActivityInfo getLaunchActivityInfo(String packageName, PackageManager pm) {
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = null;
        try {
            ris = pm.queryIntentActivities(intentToResolve, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }

        return ris.get(0).activityInfo;
    }

    public synchronized AppInfo getAppInfoByPkgName(String pkgName) {
        for (AppInfo appInfo : allAppInfos) {
            if (TextUtils.equals(pkgName, appInfo.getPackageName())) {
                return appInfo;
            }
        }
        return null;
    }
}

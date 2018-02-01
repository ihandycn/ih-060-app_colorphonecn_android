package com.honeycomb.colorphone.boost;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
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
import java.util.List;

import static com.flurry.sdk.nr.o;

public class SystemAppsManager {

    private List<AppInfo> allAppInfos;
    private List<String> allAppPackageNames;
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
        List<ApplicationInfo> installedApplications = pkgMgr.getInstalledApplications(0);
        HSLog.w("notification", "init Allapps == " + installedApplications.size());

        allAppInfos.clear();
        allAppPackageNames.clear();

        AppInfo app;
        for (ApplicationInfo info : installedApplications) {
            if (!allAppPackageNames.contains(info.packageName)) {
                ActivityInfo launchActivityInfo = getLaunchActivityInfo(info.packageName, pkgMgr);
                if (null != launchActivityInfo) {
                    app = new AppInfo(info, false);
                    app.setLaunchActivityName(launchActivityInfo.name);
                    allAppInfos.add(app);
                    allAppPackageNames.add(info.packageName);
                }
            }
            // For analysis
            PackageList.checkAndLogPackage(info.packageName);
        }
        HSLog.w("notification", "init Allappd == " + allAppPackageNames.size());
        return allAppInfos;
    }

    public List<String> getAllAppPackageNames() {
        return allAppPackageNames;
    }

    public List<AppInfo> getAllAppInfos() {
        return allAppInfos;
    }

    public List<PackageInfo> getAllPackageInfos() {
        try {
            return pkgMgr.getInstalledPackages(0);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw e;
            }
            return Collections.emptyList();
        }
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

    public AppInfo getAppInfoByPkgName(String pkgName) {
        for (AppInfo appInfo : allAppInfos) {
            if (TextUtils.equals(pkgName, appInfo.getPackageName())) {
                return appInfo;
            }
        }
        return null;
    }
}

package com.honeycomb.colorphone.recentapp;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.colorphone.lock.ScreenStatusReceiver;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.receiver.UserPresentReceiver;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.monitor.topapp.HSTopAppManager;
import com.ihs.device.monitor.topapp.HSUsageAccessMgr;
import com.ihs.device.monitor.topapp.TopAppManager;

import net.appcloudbox.ads.expressad.AcbExpressAdManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by sundxing on 2018/1/31.
 */

public class RecentAppManager {
    private static final String TOP_MANAGER_TAG = "ColorPhone_RecentApp";
    private static final String TAG = "RecentAppManager";

    private static RecentAppManager INSTANCE = new RecentAppManager();
    private boolean isStarted;

    public static RecentAppManager getInstance() {
        return INSTANCE;
    }

    private AppUsageOp mAppUsageOp;
    private String mCurrentAppPkgName;
    private HSTopAppManager.TopAppListener mTopAppListener = new HSTopAppManager.TopAppListener() {
        @Override
        public void onChanged(String packageName) {
            HSLog.i(TAG, "onChanged() packageName = " + packageName);
            if (!TextUtils.isEmpty(packageName)) {
                if (!TextUtils.isEmpty(mCurrentAppPkgName)) {
                    HSLog.i(TAG, "onChanged() mCurrentAppPkgName = " + mCurrentAppPkgName);
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
            HSLog.i(TAG, "RecentAppManager.onPermissionChanged() isGranted = " + isGranted);
            RecentAppManager.this.updateMonitorStyle();
        }
    };

    private INotificationObserver screenOnObserver = new INotificationObserver() {
        boolean triggered = false;

        @Override
        public void onReceive(String s, HSBundle hsBundle) {

            boolean hasKeyGuard = Utils.isKeyguardLocked(HSApplication.getContext(), false);
            switch (s) {
                case UserPresentReceiver.USER_PRESENT:
                    if (!triggered) {
                        SmartAssistantUtils.tryShowSmartAssistant();
                        triggered = true;
                    }
                    break;

                case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                    if (!hasKeyGuard && !triggered) {
                        SmartAssistantUtils.tryShowSmartAssistant();
                        triggered = true;
                    }
                    break;
                case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                    triggered = false;
                    break;
            }
        }
    };

    private BroadcastReceiver mPackageUninstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
                removePackage(intent.getData().getSchemeSpecificPart());
            }
        }
    };

    private void removePackage(String packageName) {
        HSLog.i(TAG, "RecentAppManager remove : " + packageName);

        SystemAppsManager.getInstance().removePackage(packageName);
        mAppUsageOp.onAppUninstall(packageName);
    }

    private void updateMonitorStyle() {
        HSLog.i(TAG, "RecentAppManager.updateMonitorStyle()");
        if (HSUsageAccessMgr.getInstance().isPermissionGranted()) {
            HSTopAppManager.getInstance().unregister(this.mTopAppListener);
            HSTopAppManager.getInstance().stop(TOP_MANAGER_TAG);
            HSLog.i(TAG, "RecentAppManager.updateMonitorStyle() have UsageAccess");
        } else {
            HSTopAppManager.getInstance().register(this.mTopAppListener);
            HSTopAppManager.getInstance().start(5000, TOP_MANAGER_TAG);

        }
    }

    public void start() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        HSLog.d(TAG, "start");

        if (mAppUsageOp == null) {
            mAppUsageOp = new AppUsageOp();
        }
        mAppUsageOp.sync();

        updateMonitorStyle();
        HSUsageAccessMgr.getInstance().checkPermission(this.usageAccessListener);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, screenOnObserver);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, screenOnObserver);
        HSGlobalNotificationCenter.addObserver(UserPresentReceiver.USER_PRESENT, screenOnObserver);

        AcbExpressAdManager.getInstance().activePlacementInProcess(AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);

        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");

        HSApplication.getContext().registerReceiver(mPackageUninstallReceiver, filter);

    }

    public void stop() {
        if (!isStarted) {
            return;
        }
        isStarted = false;
        HSLog.d(TAG, "stop");
        TopAppManager.getInstance().stopMonitor(TOP_MANAGER_TAG);
        HSTopAppManager.getInstance().unregister(this.mTopAppListener);
        HSUsageAccessMgr.getInstance().uncheckPermission(this.usageAccessListener);
        HSGlobalNotificationCenter.removeObserver(screenOnObserver);

        AcbExpressAdManager.getInstance().deactivePlacementInProcess(AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);
        HSApplication.getContext().unregisterReceiver(mPackageUninstallReceiver);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public List<String> getAppUsageListRecently(int count) {
        List<String> recentAppList = new ArrayList<>();

        if (HSUsageAccessMgr.getInstance().isPermissionGranted()) {
            List<UsageStats> usageStatsList = UsageStatsUtil.getUsageStatsList(HSApplication.getContext(), 7);
            Collections.sort(usageStatsList, new Comparator<UsageStats>() {
                @Override
                public int compare(UsageStats o1, UsageStats o2) {
                    return (o2.getLastTimeUsed() - o1.getLastTimeUsed() > 0 ? 1 : -1);
                }
            });

            for (int i = 0; i < usageStatsList.size(); i++) {
                String pkgName = usageStatsList.get(i).getPackageName();
                if (isLaunchableApp(pkgName) && recentAppList.size() < count && !recentAppList.contains(pkgName)) {
                    recentAppList.add(pkgName);
                    if (recentAppList.size() >= count) {
                        break;
                    }
                }
            }
        } else {
            List<AppUsage> appUsages = mAppUsageOp.getAppUsageListRecently();
            for (int i = 0; i < appUsages.size(); i++) {
                String pkgName = appUsages.get(i).getPackageName();
                if (isLaunchableApp(pkgName) && recentAppList.size() < count && !recentAppList.contains(pkgName)) {
                    recentAppList.add(pkgName);
                    if (recentAppList.size() >= count) {
                        break;
                    }
                }
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
        List<String> mostlyUsedAppList = new ArrayList<>();
        if (HSUsageAccessMgr.getInstance().isPermissionGranted()) {
            List<UsageStats> usageStatsList = UsageStatsUtil.getUsageStatsList(HSApplication.getContext(), 7);
            List<UsageStateWrapper> usageStateWrappers = wrapUsageStats(usageStatsList);
            Collections.sort(usageStateWrappers, new Comparator<IUsageStat>() {
                @Override
                public int compare(IUsageStat o1, IUsageStat o2) {
                    return (o2.getLaunchCountByDays(7) - o1.getLaunchCountByDays(7));
                }
            });
            int realSize = usageStateWrappers.size();
            for (int i = 0; i < realSize; i++) {
                String pkgName = usageStateWrappers.get(i).getPackageName();
                if (isLaunchableApp(pkgName) && mostlyUsedAppList.size() < count) {
                    mostlyUsedAppList.add(pkgName);
                }
            }
        } else {
            List<AppUsage> appUsages = mAppUsageOp.getAppUsageListFrequently(7);
            for (int i = 0; i < appUsages.size(); i++) {
                String pkgName = appUsages.get(i).getPackageName();
                if (isLaunchableApp(pkgName) && mostlyUsedAppList.size() < count) {
                    mostlyUsedAppList.add(pkgName);
                }
            }
        }
        return mostlyUsedAppList;
    }

    private boolean isLaunchableApp(String pkgName) {
        // TODO system other apps list.
        return SystemAppsManager.getInstance().getAppInfoByPkgName(pkgName) != null
                && !"com.android.systemui".equals(pkgName);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            if (TextUtils.equals(key, SmartAssistantUtils.PREF_KEY_SMART_ASSISTANT_USER_ENABLED)) {
                HSLog.d(TAG, "setting preference change");
                updateStatus();
            }
        }
    };

    public void init() {
        updateStatus();
        HSLog.d(TAG, "init");

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                HSLog.d(TAG, "config change");
                updateStatus();
            }
        });

        SharedPreferences sharedPreferences = HSApplication.getContext().getSharedPreferences(
                SmartAssistantUtils.PREF_FILE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);

    }

    private void updateStatus() {
        if (SmartAssistantUtils.isEnabled()) {
            RecentAppManager.getInstance().start();
        } else {
            RecentAppManager.getInstance().stop();
        }
    }

}

package com.honeycomb.colorphone.recentapp;

import android.annotation.TargetApi;
import android.app.usage.UsageStats;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.colorphone.lock.ScreenStatusReceiver;
import com.honeycomb.colorphone.AdPlacements;
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

import net.appcloudbox.ads.nativeads.AcbNativeAdManager;

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
        @Override
        public void onReceive(String s, HSBundle hsBundle) {

            boolean hasKeyGuard = Utils.isKeyguardLocked(HSApplication.getContext(), false);
            switch (s) {
                case UserPresentReceiver.USER_PRESENT:
                    if (hasKeyGuard) {
                        SmartAssistantUtils.tryShowSmartAssistant();
                    }
                    break;

                case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                    if (!hasKeyGuard) {
                        SmartAssistantUtils.tryShowSmartAssistant();
                    }

                    break;
            }

        }
    };

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
        HSGlobalNotificationCenter.addObserver(UserPresentReceiver.USER_PRESENT, screenOnObserver);

        AcbNativeAdManager.sharedInstance().activePlacementInProcess(AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);

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

        AcbNativeAdManager.sharedInstance().deactivePlacementInProcess(AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);

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

        HSPreferenceHelper.registerObserver(new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                HSLog.d(TAG, "setting preference change");
                updateStatus();
            }
        }, SmartAssistantUtils.PREF_FILE_NAME, SmartAssistantUtils.PREF_KEY_SMART_ASSISTANT_USER_ENABLED);

    }

    private void updateStatus() {
        if (SmartAssistantUtils.isEnabled()) {
            RecentAppManager.getInstance().start();
        } else {
            RecentAppManager.getInstance().stop();
        }
    }

}

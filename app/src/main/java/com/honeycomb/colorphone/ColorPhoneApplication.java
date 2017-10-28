package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.acb.autopilot.AutopilotConfig;
import com.acb.autopilot.AutopilotEvent;
import com.acb.call.CPSettings;
import com.acb.call.constant.CPConst;
import com.acb.call.customize.AcbCallManager;
import com.acb.call.themes.Type;
import com.acb.expressads.AcbExpressAdManager;
import com.acb.nativeads.AcbNativeAdManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.ConcurrentUtils;
import com.crashlytics.android.Crashlytics;
import com.honeycomb.colorphone.module.Module;
import com.honeycomb.colorphone.util.HSPermanentUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.charging.HSChargingManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;

public class ColorPhoneApplication extends HSApplication {

    private static ConfigLog mConfigLog;

    private List<Module> mModules = new ArrayList<>();

    private INotificationObserver mObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                HSLog.d("Session Start.");
            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("Session End.");
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                checkModuleAdPlacement();
            } else if (CPConst.NOTIFY_CHANGE_SCREEN_FLASH.equals(notificationName)) {
                HSPermanentUtils.checkAliveForProcess();
            } else {
                checkModuleAdPlacement();
            }
        }
    };

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        systemFix();
        Fabric.with(this, new Crashlytics());
        mConfigLog = new ConfigLogDefault();
        FileDownloader.setup(this);

        String packageName = getPackageName();
        String processName = getProcessName();

        if (TextUtils.equals(processName, packageName)) {
            AcbCallManager.init("", new CallConfigFactory());
            AcbCallManager.getInstance().setParser(new AcbCallManager.TypeParser() {
                @Override
                public Type parse(Map<String, ?> map) {
                    Theme type = new Theme();
                    Type.fillData(type, map);
                    type.setDownload(HSMapUtils.getInteger(map, Theme.CONFIG_DOWNLOAD_NUM));
                    return type;
                }
            });
            AcbCallManager.getInstance().setImageLoader(new ThemeImageLoader());

            HSPermanentUtils.keepAlive();
            if (BuildConfig.DEBUG) {
                AutopilotConfig.setDebugConfig(true, true, true);
            }

            addGlobalObservers();

            initModules();
            checkModuleAdPlacement();

            initLockerCharging();
            Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);

            preloadThemeResources();
        }
    }

    private void preloadThemeResources() {
        ConcurrentUtils.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                Type.values();
                ConcurrentUtils.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        doPreload();
                    }
                });
            }
        });
    }

    private void doPreload() {
        List<Type> themes = Type.values();
        for (Type theme : themes) {
            if (!TextUtils.isEmpty(theme.getPreviewImage())) {
                HSLog.d("preload", theme.getPreviewImage());
                Glide.with(this).downloadOnly().load(theme.getPreviewImage()).preload();
            }
        }
    }

    private void initLockerCharging() {
        LockScreenStarter.init();

        HSLog.d("Start", "initLockScreen");
        LockerCustomConfig.get().setLauncherIcon(R.mipmap.ic_launcher);
        LockerCustomConfig.get().setSPFileName("colorPhone_locker");
        LockerCustomConfig.get().setLockerAdName(AdPlacements.AD_LOCKER);
        LockerCustomConfig.get().setChargingExpressAdName(AdPlacements.AD_CHAEGING_SCREEN);
        LockerCustomConfig.get().setEventDelegate(new LockerEvent());
        FloatWindowCompat.initLockScreen(this);
        HSChargingManager.getInstance().start();
    }

    private void addGlobalObservers() {
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, mObserver);
        HSGlobalNotificationCenter.addObserver(CPConst.NOTIFY_CHANGE_CALL_ASSISTANT, mObserver);
        HSGlobalNotificationCenter.addObserver(CPConst.NOTIFY_CHANGE_SCREEN_FLASH, mObserver);
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);

        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    ScreenStatusReceiver.onScreenOff(context);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    ScreenStatusReceiver.onScreenOn(context);
                }
            }
        }, screenFilter);
    }

    private void initModules() {
        Module locker = new Module();
        locker.setAdName(AdPlacements.AD_LOCKER);
        locker.setAdType(Module.AD_EXPRESS);
        locker.setNotifyKey(LockerSettings.NOTIFY_LOCKER_STATE);
        locker.setChecker(new Module.Checker() {
            @Override
            public boolean isEnable() {
                return LockerSettings.isLockerEnabled();
            }
        });

        Module charging = new Module();
        charging.setAdName(AdPlacements.AD_CHAEGING_SCREEN);
        charging.setAdType(Module.AD_EXPRESS);
        charging.setNotifyKey(ChargingScreenSettings.NOTIFY_CHARGING_SCREEN_STATE);
        charging.setChecker(new Module.Checker() {
            @Override
            public boolean isEnable() {
                return ChargingScreenSettings.isChargingScreenEnabled();
            }
        });
        mModules.add(locker);
        mModules.add(charging);
    }

    private void systemFix() {
        // Fix crash: NoClassDefFoundError: android.os.AsyncTask
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            try {
                Class.forName("android.os.AsyncTask");
            }
            catch(Throwable ignore) {
                // ignored
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HSGlobalNotificationCenter.removeObserver(mObserver);
    }


    @Override
    protected String getConfigFileName() {
        if (BuildConfig.DEBUG) {
            return "config-d.ya";
        } else {
            return "config-r.ya";
        }
    }

    public static void checkCallAssistantAdPlacement() {
        final String adName = AcbCallManager.getInstance().getAcbCallFactory().getCallIdleConfig().getAdPlaceName();
        boolean enable = CPSettings.isCallAssistantModuleEnabled();
        checkNativeAd(adName, enable);

    }

    private void checkModuleAdPlacement() {
        checkCallAssistantAdPlacement();

        for (Module module : mModules) {
            if (module.getAdType() == Module.AD_EXPRESS) {
                checkExpressAd(module.getAdName(), module.getChecker().isEnable());
            } else if (module.getAdType() == Module.AD_NATIVE) {
                checkNativeAd(module.getAdName(), module.getChecker().isEnable());
            }
        }

        HSPermanentUtils.checkAliveForProcess();
    }

    private static void checkExpressAd(String adName, boolean enable) {
        HSLog.d("AD_CHECK_express", "Name = " + adName + ", enable = " + enable );
        if (enable) {
            AcbExpressAdManager.getInstance().activePlacementInProcess(adName);
        } else {
            AcbExpressAdManager.getInstance().deactivePlacementInProcess(adName);
        }
    }

    private static void checkNativeAd(String adName, boolean enable) {
        HSLog.d("AD_CHECK_native", "Name = " + adName + ", enable = " + enable );
        if (enable) {
            AcbNativeAdManager.sharedInstance().activePlacementInProcess(adName);
        } else {
            AcbNativeAdManager.sharedInstance().deactivePlacementInProcess(adName);
        }
    }

    public static ConfigLog getConfigLog() {
        return mConfigLog;
    }

    public static class LockerEvent extends LockerCustomConfig.Event {
        @Override
        public void onEventLockerAdShow() {
            super.onEventChargingAdClick();
            AutopilotEvent.onAdShow();
        }

        @Override
        public void onEventLockerShow() {
            super.onEventChargingAdClick();
            AutopilotEvent.logTopicEvent("topic-1505294061097", "color_screensaver_show");
        }

        @Override
        public void onEventLockerAdClick() {
            super.onEventChargingAdClick();
            AutopilotEvent.onAdClick();
        }

        @Override
        public void onEventChargingAdShow() {
            super.onEventChargingAdClick();
            AutopilotEvent.onAdShow();
        }

        @Override
        public void onEventChargingAdClick() {
            super.onEventChargingAdClick();
            AutopilotEvent.onAdClick();
        }

        @Override
        public void onEventChargingViewShow() {
            super.onEventChargingAdClick();
        }
    }
}

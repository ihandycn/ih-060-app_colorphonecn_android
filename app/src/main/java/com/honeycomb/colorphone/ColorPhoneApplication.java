package com.honeycomb.colorphone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.appsflyer.AppsFlyerLib;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.call.assistant.customize.CallAssistantConsts;
import com.call.assistant.customize.CallAssistantManager;
import com.call.assistant.customize.CallAssistantSettings;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.factoryimpl.CpCallAssistantFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpMessageCenterFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpScreenFlashFactoryImpl;
import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.module.LockerEvent;
import com.honeycomb.colorphone.module.LockerLogger;
import com.honeycomb.colorphone.module.Module;
import com.honeycomb.colorphone.notification.NotificationAlarmReceiver;
import com.honeycomb.colorphone.notification.NotificationCondition;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.recentapp.RecentAppManager;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.ApplyInfoAutoPilotUtils;
import com.honeycomb.colorphone.util.CallFinishUtils;
import com.honeycomb.colorphone.util.HSPermanentUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.UserSettings;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.Upgrader;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.chargingreport.ChargingReportCallback;
import com.ihs.chargingreport.ChargingReportConfiguration;
import com.ihs.chargingreport.ChargingReportManager;
import com.ihs.chargingreport.DismissType;
import com.ihs.commons.analytics.publisher.HSPublisherMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.libcharging.HSChargingManager;
import com.liulishuo.filedownloader.FileDownloader;
import com.messagecenter.customize.MessageCenterManager;
import com.messagecenter.customize.MessageCenterSettings;
import com.superapps.debug.SharedPreferencesOptimizer;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.expressad.AcbExpressAdManager;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;
import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.common.notificationcenter.AcbNotificationConstant;
import net.appcloudbox.common.utils.AcbApplicationHelper;
import net.appcloudbox.h5game.AcbH5GameManager;
import net.appcloudbox.internal.service.DeviceInfo;
import net.appcloudbox.service.AcbService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import colorphone.acb.com.libscreencard.CardCustomConfig;
import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;
import static net.appcloudbox.AcbAds.GDPR_NOT_GRANTED;
import static net.appcloudbox.AcbAds.GDPR_USER;

public class ColorPhoneApplication extends HSApplication {
    private static ConfigLog mConfigLog;

    private List<Module> mModules = new ArrayList<>();

    private static Stack<Integer> activityStack = new Stack<>();

    private boolean mAppsFlyerResultReceived;
    private BroadcastReceiver mAgencyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mAppsFlyerResultReceived
                    && HSNotificationConstant.HS_APPSFLYER_RESULT.equals(intent.getAction())) {
                mAppsFlyerResultReceived = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recordInstallType();
                        HSGlobalNotificationCenter.sendNotification(HSNotificationConstant.HS_CONFIG_CHANGED);
                    }
                }, 1000);
            }
        }
    };

    private INotificationObserver mObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            HSLog.d("Receive INotification: " + notificationName);

            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                checkModuleAdPlacement();
                HSLog.d("Session Start.");
            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("Session End.");
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                checkModuleAdPlacement();
                // Call-Themes update timely.
                Theme.updateThemes();
                initNotificationToolbar();
                // remove download New Type when config changed to reduce
//                downloadNewType();
            } else if (ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH.equals(notificationName)) {
                HSPermanentUtils.checkAliveForProcess();
            } else {
                checkModuleAdPlacement();
            }
        }
    };

    private BroadcastReceiver mAutopilotFetchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCallFinishFullScreenAdPlacement();
        }
    };

    /**
     * Size of theme preview image.
     */
    public static int mWidth;
    public static int mHeight;

    private boolean isCallAssistantActivated;

    private static boolean isFabricInitted;
    public static boolean isAppForeground() {
        return !activityStack.isEmpty();
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        systemFix();

        onAllProcessCreated();

        String packageName = getPackageName();
        String processName = getProcessName();
        if (TextUtils.equals(processName, packageName)) {
            onMainProcessCreate();
        }
    }

    private void initNotificationToolbar() {
        if (HSVersionControlUtils.isFirstLaunchSinceInstallation() || HSVersionControlUtils.isFirstLaunchSinceUpgrade()) {
            UserSettings.checkNotificationToolbarToggleClicked();
        }

        if (!UserSettings.isNotificationToolbarToggleClicked()) {
            UserSettings.setNotificationToolbarEnabled(ModuleUtils.isNotificationToolBarEnabled());
        }

        NotificationManager.getInstance().showNotificationToolbarIfEnabled();
    }


    public static boolean isFabricInitted() {
        return isFabricInitted;
    }

    private void onAllProcessCreated() {
        if (GdprUtils.isNeedToAccessDataUsage()) {
            initFabric();
        }
        mConfigLog = new ConfigLogDefault();
        FileDownloader.setup(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mHeight = Utils.getPhoneHeight(this);
        mWidth = Utils.getPhoneWidth(this);
        AcbApplicationHelper.init(this);
        AcbService.initialize(this);
        SharedPreferencesOptimizer.install(BuildConfig.DEBUG);

        HSGdprConsent.addListener(new HSGdprConsent.GDPRConsentListener() {
            @Override
            public void onGDPRStateChanged(HSGdprConsent.ConsentState oldState, HSGdprConsent.ConsentState newState) {
                if (GdprUtils.isNeedToAccessDataUsage()) {
                    initFabric();
                }
                if (!isMainProcess()) {
                    if (oldState == HSGdprConsent.ConsentState.ACCEPTED && newState != oldState) {
                        System.exit(0);
                    }
                }
            }
        });
    }

    @DebugLog
    private void onMainProcessCreate() {

        if (HSGdprConsent.isGdprUser()) {
            if (HSGdprConsent.getConsentState() != HSGdprConsent.ConsentState.ACCEPTED) {
                AcbAds.getInstance().setGdprInfo(GDPR_USER, GDPR_NOT_GRANTED);
            }
        }

        registerReceiver(mAgencyBroadcastReceiver, new IntentFilter(HSNotificationConstant.HS_APPSFLYER_RESULT));
        AcbAds.getInstance().initializeFromGoldenEye(this);
        //
        AcbAds.getInstance().setLogEventListener(new AcbAds.logEventListener() {
            @Override
            public void logFirebaseEvent(String s, Bundle bundle) {

                if (GdprUtils.isNeedToAccessDataUsage()) {
                    // TODO Firebase event.
                }

            }
        });

        if (!GdprUtils.isGdprNewUser() && HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.TO_BE_CONFIRMED) {
            GdprUtils.setDataUsageUserEnabled(true);
        }
        String customId = GdprUtils.isDataUsageUserEnabled() ? HSApplication.getInstallationUUID()
                : null;
        AppsFlyerLib.getInstance().setCustomerUserId(customId);

        AutopilotConfig.initialize(this, "Autopilot_Config.json", customId);

        CallAssistantManager.init(new CpCallAssistantFactoryImpl());
        MessageCenterManager.init(new CpMessageCenterFactoryImpl());
        ScreenFlashManager.init(new CpScreenFlashFactoryImpl());

        ScreenFlashManager.getInstance().setParser(new ScreenFlashManager.TypeParser() {
            @Override
            public Type parse(Map<String, ?> map) {
                Theme type = new Theme();
                Type.fillData(type, map);
                type.setNotificationLargeIconUrl(HSMapUtils.optString(map, "", "LocalPush", "LocalPushIcon"));
                type.setNotificationBigPictureUrl(HSMapUtils.optString(map, "", "LocalPush", "LocalPushPreviewImage"));
                type.setNotificationEnabled(HSMapUtils.optBoolean(map, false, "LocalPush", "Enable"));
                type.setDownload(HSMapUtils.getInteger(map, Theme.CONFIG_DOWNLOAD_NUM));
                type.setRingtoneUrl(HSMapUtils.optString(map, "", Theme.CONFIG_RINGTONE));
                type.setLocked(HSMapUtils.optBoolean(map,false, "Status", "Lock"));
                type.setCanDownload(!HSMapUtils.optBoolean(map,true, "Status", "StaticPreview"));
                type.setSpecialTopic(HSMapUtils.optBoolean(map, false, "SpecialTopic"));
                return type;
            }
        });
        ScreenFlashManager.getInstance().setImageLoader(new ThemeImageLoader());
        ScreenFlashManager.getInstance().logTest = true;
        ContactManager.init();


        AcbRewardAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_REWARD_VIDEO);
        SystemAppsManager.getInstance().init();
        NotificationCondition.init();

        AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_RESULT_PAGE);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_RESULT_PAGE_INTERSTITIAL);

        HSPermanentUtils.keepAlive();

        Upgrader.upgrade();
        addGlobalObservers();
        initModules();
        checkModuleAdPlacement();

        initChargingReport();
        initLockerCharging();
        initNotificationToolbar();

        initRecentApps();
        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
        String popularThemeBgUrl = HSConfig.optString("", "Application", "Special", "SpecialBg");
        GlideApp.with(this).downloadOnly().load(popularThemeBgUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LockJobService.startJobScheduler();
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                activityStack.push(1);
                HSPreferenceHelper.getDefault().putLong(NotificationConstants.PREFS_APP_OPENED_TIME, System.currentTimeMillis());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (!activityStack.isEmpty()) {
                    activityStack.pop();
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        initNotificationAlarm();

        Theme.updateThemes();


        SmsFlashListener.getInstance().start();

        if (AutopilotConfig.hasConfigFetchFinished()) {
        } else {
            IntentFilter configFinishedFilter = new IntentFilter();
            configFinishedFilter.addAction(AutopilotConfig.ACTION_CONFIG_FETCH_FINISHED);
            registerReceiver(mAutopilotFetchReceiver, configFinishedFilter, AcbNotificationConstant.getSecurityPermission(this), null);
        }
        logUserLevelDistribution();

        ScreenFlashManager.getInstance().setLogEventListener(new ScreenFlashManager.LogEventListener() {
            @Override
            public void onListPageItemClicked() {

            }

            @Override
            public void onListPageScrollUp() {

            }

            @Override
            public void onListItemDownloadIconClicked() {

            }

            @Override
            public void onDetailContactIconClicked() {

            }

            @Override
            public void onContactSetIconClicked() {

            }

            @Override
            public void onSelectedFromList() {

            }

            @Override
            public void onSelectedFromDetail() {

            }

            @Override
            public void onInCallFlashShown(String themeId) {

            }

            @Override
            public void onScreenFlashSetSucceed(String idName) {
                ApplyInfoAutoPilotUtils.logCallFlashSet();
                LauncherAnalytics.logEvent("ColorPhone_Set_Success", "type", idName);
            }
        });

    }

    private void initRecentApps() {
        RecentAppManager.getInstance().init();
    }
    
    private void updateCallFinishFullScreenAdPlacement() {
        if (CallFinishUtils.isCallFinishFullScreenAdEnabled() && !isCallAssistantActivated) {
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_CALL_ASSISTANT_FULL_SCREEN);
            isCallAssistantActivated = true;
        }
    }

    private void initChargingReport() {
        long firstInstallTime = HSSessionMgr.getFirstSessionStartTime();
        AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_CHARGING_REPORT);
        ChargingReportConfiguration configuration = new ChargingReportConfiguration.Builder()
                .adPlacement(AdPlacements.AD_CHARGING_REPORT)
                .appName(getResources().getString(R.string.smart_charging))
                .appIconResourceId(R.mipmap.ic_launcher)
                .timeAppInstall(firstInstallTime > 0 ? firstInstallTime : System.currentTimeMillis())
                .lockerConflic(new ChargingReportConfiguration.LockScreenConflict() {
                    @Override
                    public boolean hasChargingScreen() {
                        return SmartChargingSettings.isChargingScreenEnabled();
                    }
                })
                .sceneSwitch(new ChargingReportConfiguration.ISceneSwitch() {

                    // TODO 库添加总开关。删除此代码
                    @Override
                    public boolean sceneUnlockPlugEnabled() {
                        return SmartChargingSettings.isChargingReportEnabled()
                                && HSConfig.optBoolean(false, "Application", "ChargingReport", "ChargeReportScene", "Plug_Unlocked");
                    }

                    @Override
                    public boolean sceneLockPlugEnabled() {
                        return SmartChargingSettings.isChargingReportEnabled()
                                && HSConfig.optBoolean(false, "Application", "ChargingReport", "ChargeReportScene", "Plug_Locked");
                    }

                    // 解锁出现的充电报告
                    @Override
                    public boolean sceneChargingEnabled() {
                        return SmartChargingSettings.isChargingReportEnabled()
                                && HSConfig.optBoolean(false, "Application", "ChargingReport", "ChargeReportScene", "Charging");
                    }

                    @Override
                    public boolean sceneUnlockUnplugEnabled() {
                        return SmartChargingSettings.isChargingReportEnabled()
                                && HSConfig.optBoolean(false, "Application", "ChargingReport", "ChargeReportScene", "Unplug_Unlocked");
                    }

                    @Override
                    public boolean sceneLockUnplugEnabled() {
                        return SmartChargingSettings.isChargingReportEnabled() &&
                                HSConfig.optBoolean(false, "Application", "ChargingReport", "ChargeReportScene", "Unplug_Locked");
                    }
                })
                .build();
        ChargingReportManager.getInstance().init(configuration);
        ChargingReportManager.getInstance().setChargingReportCallback(new ChargingReportCallback() {
            @Override
            public void logEvent(String s, boolean logToFlurry, String... strings) {
                LauncherAnalytics.logEvent(s, strings);

            }

            @Override
            public void logAdEvent(String s, boolean b) {
                LauncherAnalytics.logEvent("AcbAdNative_Viewed_In_App", s, String.valueOf(b));
            }

            @Override
            public void onChargingReportShown() {

            }

            @Override
            public void onChargingReportDismiss(DismissType dismissType) {

            }
        });
    }

    private void initLockerCharging() {
        LockScreenStarter.init();

        HSLog.d("Start", "initLockScreen");
        LockerCustomConfig.get().setLauncherIcon(R.mipmap.ic_launcher);
        LockerCustomConfig.get().setCustomScreenIcon(R.drawable.ic_charging_screen_logo);
        LockerCustomConfig.get().setSPFileName("colorPhone_locker");
        LockerCustomConfig.get().setLockerAdName(AdPlacements.AD_LOCKER);
        LockerCustomConfig.get().setChargingExpressAdName(AdPlacements.AD_CHARGING_SCREEN);
        LockerCustomConfig.get().setEventDelegate(new LockerEvent());
        LockerCustomConfig.get().setRemoteLogger(new LockerLogger());
        FloatWindowCompat.initLockScreen(this);
        HSChargingManager.getInstance().start();

        CardCustomConfig.get().setRemoteLogger(new CardCustomConfig.RemoteLogger() {
            @Override
            public void logEvent(String eventID) {
                LauncherAnalytics.logEvent(eventID);
            }

            @Override
            public void logEvent(String eventID, String... vars) {
                LauncherAnalytics.logEvent(eventID, vars);
            }
        });
        AcbH5GameManager.initialize(this);
        AcbH5GameManager.setCustomerUserID(DeviceInfo.getUUID());
        AcbH5GameManager.setGDPRConsentGranted(true);

    }

    private void addGlobalObservers() {
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, mObserver);
        HSGlobalNotificationCenter.addObserver(CallAssistantConsts.NOTIFY_CHANGE_CALL_ASSISTANT, mObserver);
        HSGlobalNotificationCenter.addObserver(ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH, mObserver);
        HSGlobalNotificationCenter.addObserver(LockerSettings.NOTIFY_LOCKER_STATE, mObserver);
        HSGlobalNotificationCenter.addObserver(ChargingScreenSettings.NOTIFY_CHARGING_SCREEN_STATE, mObserver);
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);

        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    ScreenStatusReceiver.onScreenOff(context);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    ScreenStatusReceiver.onScreenOn(context);
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    ScreenStatusReceiver.onUserPresent(context);

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
        charging.setAdName(AdPlacements.AD_CHARGING_SCREEN);
        charging.setAdType(Module.AD_EXPRESS);
        charging.setNotifyKey(ChargingScreenSettings.NOTIFY_CHARGING_SCREEN_STATE);
        charging.setChecker(new Module.Checker() {
            @Override
            public boolean isEnable() {
                return SmartChargingSettings.isChargingScreenEnabled();
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
        final String adName = CallAssistantManager.getInstance().getCallAssistantFactory().getCallIdleConfig().getAdPlaceName();
        boolean enable = CallAssistantSettings.isCallAssistantModuleEnabled();
        checkExpressAd(adName, enable);
        final String smsName = MessageCenterManager.getInstance().getMessageCenterFactory().getSMSConfig().getAdPlacement();
        checkExpressAd(smsName, MessageCenterSettings.isSMSAssistantModuleEnabled());

    }

    public static void checkChargingReportAdPlacement() {
        checkExpressAd(AdPlacements.AD_CHARGING_REPORT, SmartChargingSettings.isChargingReportEnabled());
    }

    private void checkModuleAdPlacement() {
        checkCallAssistantAdPlacement();
        checkChargingReportAdPlacement();
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
            AcbNativeAdManager.getInstance().activePlacementInProcess(adName);
        } else {
            AcbNativeAdManager.getInstance().deactivePlacementInProcess(adName);
        }
    }

    public static ConfigLog getConfigLog() {
        return mConfigLog;
    }

    public static void initNotificationAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), NotificationAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR, 6);
        time.set(Calendar.AM_PM, Calendar.PM);
        time.set(Calendar.MINUTE, 30);

        long setTime = time.getTimeInMillis();
        long timeInMillis = setTime > System.currentTimeMillis() ? setTime : setTime + DateUtils.DAY_IN_MILLIS;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, DateUtils.DAY_IN_MILLIS, pendingIntent);
    }


    private void initFabric() {
        // Set up Crashlytics, disabled for debug builds
        if (!isFabricInitted) {
            Fabric.with(this, new Crashlytics(), new Answers());
            isFabricInitted = true;
        }
    }

    private void recordInstallType() {
        HSPublisherMgr.PublisherData data = HSPublisherMgr.getPublisherData(this);
        if (data.isDefault()) {
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        String installType = data.getInstallMode().name();
        parameters.put("ad_set", data.getAdset());
        parameters.put("ad_set_id", data.getAdsetId());
        parameters.put("ad_id", data.getAdId());
        String debugInfo = "" + installType + "|" + data.getMediaSource() + "|" + data.getAdset();
        parameters.put("install_type", installType);
        parameters.put("publisher_debug_info", debugInfo);
        LauncherAnalytics.logEvent("Agency_Info", "install_type", installType, "campaign_id", "" + data.getCampaignID(), "user_level", "" + HSConfig.optString("not_configured", "UserLevel"));

        final String PREF_KEY_AGENCY_INFO_LOGGED = "PREF_KEY_AGENCY_INFO_LOGGED";

        if (HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode) {
            if (!HSPreferenceHelper.getDefault().contains(PREF_KEY_AGENCY_INFO_LOGGED)) {
                HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_AGENCY_INFO_LOGGED, true);
                LauncherAnalytics.logEvent("New_User_Agency_Info", "install_type", installType, "user_level", "" + HSConfig.optString("not_configured", "UserLevel"), "version_code", "" + HSApplication.getCurrentLaunchInfo().appVersionCode);
            }
        }
    }

    private void logUserLevelDistribution() {
        String PREF_KEY_New_User_User_Level_LOGGED = "lv_logged";
        Log.e("SUNDXING", "WifiStateChange2：" + HSConfig.optString("not_configured", "UserLevel"));
        Log.e("SUNDXING", "USER_ID =" + HSApplication.getCustomerUserIDInWorkThread());
        Logger.e("SUNDXING", "All_Map =" + HSConfig.getMap("Application"));
        HSPreferenceHelper hsPreferenceHelper = HSPreferenceHelper.create(HSApplication.getContext(), "framework_config");
        String newUrl = hsPreferenceHelper.getString("hs.commons.config.remote.file.url", "");
        Log.e("SUNDXING", "Remote url =" + newUrl);
        HSConfig.fetchRemote();

        if (Utils.isNewUser()) {
            if (!HSPreferenceHelper.getDefault().contains(PREF_KEY_New_User_User_Level_LOGGED)) {
                int delayTimes [] = {20, 40, 65, 95, 125, 365, 1850, 7300, 11000};
                for (int delay : delayTimes) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("WifiStateChange2", HSConfig.optString("not_configured", "UserLevel"));
                            LauncherAnalytics.logEvent("New_User_Agency_Info_" + delay, LauncherAnalytics.FLAG_LOG_FABRIC,
                                    "user_level", "" + HSConfig.optString("not_configured", "UserLevel"),
                                    "version_code", "" + HSApplication.getCurrentLaunchInfo().appVersionCode);
                        }
                    }, delay * 1000);
                }
                HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_New_User_User_Level_LOGGED, true);
            }
        } else {
            final String PREF_KEY_ATTRIBUTION_OLD_USER_TEST = "old_user_attribution";
            if (HSApplication.getFirstLaunchInfo().appVersionCode <= 64
                    && !HSPreferenceHelper.getDefault().contains(PREF_KEY_ATTRIBUTION_OLD_USER_TEST)) {
                HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_ATTRIBUTION_OLD_USER_TEST, true);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("WifiStateChange2", HSConfig.optString("not_configured", "UserLevel"));
                        LauncherAnalytics.logEvent("Old_User_Agency_Info", LauncherAnalytics.FLAG_LOG_FABRIC,
                                "user_level", "" + HSConfig.optString("not_configured", "UserLevel"),
                                "version_code", "" + HSApplication.getCurrentLaunchInfo().appVersionCode,
                                "first_version_code", "" + HSApplication.getFirstLaunchInfo().appVersionCode);
                    }
                }, 20 * 1000);
            }
        }
    }

}

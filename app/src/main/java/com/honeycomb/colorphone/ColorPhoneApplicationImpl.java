package com.honeycomb.colorphone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashFactory;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.utils.FileUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.call.assistant.customize.CallAssistantConsts;
import com.call.assistant.customize.CallAssistantManager;
import com.call.assistant.customize.CallAssistantSettings;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenActivity;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerActivity;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.boost.BoostActivity;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.boost.FloatWindowDialog;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.cashcenter.CashCenterGuideDialog;
import com.honeycomb.colorphone.cmgame.CmGameUtil;
import com.honeycomb.colorphone.cmgame.GameInit;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.factoryimpl.CpCallAssistantFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpMessageCenterFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpScreenFlashFactoryImpl;
import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.module.ChargingImproverCallbackImpl;
import com.honeycomb.colorphone.module.LockerEvent;
import com.honeycomb.colorphone.module.LockerLogger;
import com.honeycomb.colorphone.module.Module;
import com.honeycomb.colorphone.notification.NotificationAlarmReceiver;
import com.honeycomb.colorphone.notification.NotificationCondition;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.ADAutoPilotUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.CallFinishUtils;
import com.honeycomb.colorphone.util.ChannelInfoUtil;
import com.honeycomb.colorphone.util.ColorPhonePermanentUtils;
import com.honeycomb.colorphone.util.DailyLogger;
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
import com.ihs.chargingimprover.ChargingImproverManager;
import com.ihs.chargingreport.ChargingReportCallback;
import com.ihs.chargingreport.ChargingReportConfiguration;
import com.ihs.chargingreport.ChargingReportManager;
import com.ihs.chargingreport.DismissType;
import com.ihs.chargingreport.utils.ChargingReportUtils;
import com.ihs.commons.analytics.publisher.HSPublisherMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.permanent.HSPermanentUtils;
import com.ihs.libcharging.HSChargingManager;
import com.ihs.permission.HSPermissionRequestMgr;
import com.liulishuo.filedownloader.FileDownloader;
import com.messagecenter.customize.MessageCenterManager;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.debug.SharedPreferencesOptimizer;
import com.superapps.push.PushMgr;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.expressad.AcbExpressAdManager;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;
import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.common.notificationcenter.AcbNotificationConstant;
import net.appcloudbox.feast.call.HSFeast;
import net.appcloudbox.service.AcbService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;
import static net.appcloudbox.AcbAds.GDPR_NOT_GRANTED;
import static net.appcloudbox.AcbAds.GDPR_USER;

public class ColorPhoneApplicationImpl {
    private static final long TIME_NEED_LOW = 10 * 1000; // 10s
    private static ConfigLog mConfigLog;

    private List<Module> mModules = new ArrayList<>();
    private DailyLogger mDailyLogger;

    private static Stack<Integer> activityStack = new Stack<>();
    private List<AppInit> mAppInitList = new ArrayList<>();

    private HSApplication mBaseApplication;

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
                Beta.checkUpgrade(true, true);

                HSLog.d("Session Start.");
            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("Session End.");
                onSessionEnd();
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                checkModuleAdPlacement();
                // Call-Themes update timely.
                Theme.updateThemesTotally();
                initNotificationToolbar();
                ConfigChangeManager.getInstance().onChange(ConfigChangeManager.REMOTE_CONFIG);

                CrashGuard.updateIgnoredCrashes();
                NotificationCondition.getsInstance().onConfigChange();
                // remove download New Type when config changed to reduce
//                downloadNewType();
            } else if (ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH.equals(notificationName)) {
                ColorPhonePermanentUtils.checkAliveForProcess();
            } else if (SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE.equals(notificationName)) {
                BoostActivity.start(HSApplication.getContext(), ResultConstants.RESULT_TYPE_BOOST_LOCKER);
            } else {
                checkModuleAdPlacement();
            }
        }
    };

    public void onSessionEnd() {
        Preferences.get(Constants.DESKTOP_PREFS).doOnce(() -> {
            if (mDailyLogger != null) {
                mDailyLogger.logOnceFirstSessionEndStatus();
            }
            Analytics.logEvent("Display_Resolution", "Size",
                    Dimensions.getPhoneWidth(HSApplication.getContext()) + "*"
                            + Dimensions.getPhoneHeight(HSApplication.getContext()));
        }, "Permission_Check_Above23_FirstSessionEnd");

        if (mDailyLogger != null) {
            mDailyLogger.checkAndLog();
        }
    }

    private BroadcastReceiver mAutopilotFetchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCallFinishFullScreenAdPlacement();
            AdManager.getInstance().setEnable(ConfigSettings.showAdOnDetailView() || ConfigSettings.showAdOnApplyTheme());
            ConfigChangeManager.getInstance().onChange(ConfigChangeManager.AUTOPILOT);
            ADAutoPilotUtils.update();
            ADAutoPilotUtils.logAutopilotEventToFaric();
        }
    };

    /**
     * Size of theme preview image.
     */
    public static int mWidth;
    public static int mHeight;

    private boolean isCallAssistantActivated;

    private static boolean isFabricInitted;
    public static long launchTime;

    public static boolean isAppForeground() {
        return !activityStack.isEmpty();
    }

    public ColorPhoneApplicationImpl(HSApplication application) {
        mBaseApplication = application;
    }

    public void onCreate() {
        systemFix();
        mAppInitList.add(new GdprInit());
        mAppInitList.add(new ScreenFlashInit());
        mAppInitList.add(new GameInit());

        onAllProcessCreated();

        String packageName = mBaseApplication.getPackageName();
        String processName = HSApplication.getProcessName();
        if (TextUtils.equals(processName, packageName)) {
            onMainProcessCreate();

        }

        if (processName.endsWith(":work")) {
            onWorkProcessCreate();
        }
        launchTime = System.currentTimeMillis();

        HSFeast.getInstance().init(mBaseApplication, null);
    }

    private void onWorkProcessCreate() {
        HSPermanentUtils.setJobSchedulePeriodic(2 * DateUtils.HOUR_IN_MILLIS);
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


    public static boolean isFabricInited() {
        return isFabricInitted;
    }

    private void onAllProcessCreated() {
        initFabric();

        CrashReport.initCrashReport(mBaseApplication.getApplicationContext(), mBaseApplication.getString(R.string.bugly_app_id), BuildConfig.DEBUG);

        String channel = ChannelInfoUtil.getChannelInfo(mBaseApplication);

        String appKey = HSConfig.getString("libCommons", "Umeng", "AppKey");
        String pushKey = HSConfig.getString("libCommons", "Umeng", "PushKey");
        UMConfigure.init(mBaseApplication, appKey, channel, UMConfigure.DEVICE_TYPE_PHONE, pushKey);
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        UMConfigure.setProcessEvent(true);
        PushAgent pushAgent = PushAgent.getInstance(mBaseApplication);
        pushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                PushMgr.sendTokenToServer(deviceToken);
                HSLog.d("Umeng.test", "注册成功：deviceToken：-------->  " + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                HSLog.d("Umeng.test", "注册失败：-------->  " + "s:" + s + ",s1:" + s1);
            }
        });
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            @Override
            public void dealWithCustomMessage(Context context, UMessage uMessage) {
                HSLog.d("Umeng.test", "Receive umeng push");
                Analytics.logEvent("ColorPhone_Push_Receive",
                        "Brand", Build.BRAND.toLowerCase(),
                        "DeviceVersion", Utils.getDeviceInfo());
                long receivePush = System.currentTimeMillis() - launchTime;
                if (receivePush <= 25 * 1000) {
                    Analytics.logEvent("Wake_Up_By_Umeng_Push");
                }
                checkChargingOrLocker();
            }
        };
        pushAgent.setMessageHandler(messageHandler);

        AcbService.initialize(mBaseApplication);
        // Init Crash optimizer
        CrashGuard.install();

        // Init ANR optimizer
        SharedPreferencesOptimizer.install(BuildConfig.DEBUG);

        mHeight = Utils.getPhoneHeight(mBaseApplication);
        mWidth = Utils.getPhoneWidth(mBaseApplication);
        mConfigLog = new ConfigLogDefault();
        mDailyLogger = new DailyLogger();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        FileDownloader.setup(mBaseApplication);

        initAutopilot();

        HSPermanentUtils.initKeepAlive(
                true,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                null, null);

        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                HSPermanentUtils.startKeepAlive();
            }
        }, TIME_NEED_LOW);

        HSPermissionRequestMgr.InitOptions options = new HSPermissionRequestMgr.InitOptions();
        options.setCustomConfig("action_custom.ja", null, null, "rules_config_custom.ja");
        HSPermissionRequestMgr.getInstance().init(mBaseApplication, options);

    }

    private void checkChargingOrLocker() {
        if (ChargingReportUtils.isScreenOn()) {
            return;
        }
        if (DeviceManager.getInstance().isCharging() && SmartChargingSettings.isChargingScreenEnabled()) {
            //
            if (!ChargingScreenActivity.exist) {
                ChargingScreenUtils.startChargingScreenActivity(false, true);
            }
        } else if (LockerSettings.isLockerEnabled()) {
            if (!LockerActivity.exit) {
                ChargingScreenUtils.startLockerActivity(true);
            }
        }
    }

    private void initAutopilot() {
        AutopilotConfig.initialize(mBaseApplication, "Autopilot_Config.json");
        if (!AutopilotConfig.hasConfigFetchFinished()) {
            IntentFilter configFinishedFilter = new IntentFilter();
            configFinishedFilter.addAction(AutopilotConfig.ACTION_CONFIG_FETCH_FINISHED);
            mBaseApplication.registerReceiver(mAutopilotFetchReceiver, configFinishedFilter, AcbNotificationConstant.getSecurityPermission(mBaseApplication), null);
        }
    }

    @DebugLog
    private void onMainProcessCreate() {
        CrashFix.fix();
        copyMediaFromAssertToFile();
        DauChecker.get().start();

        for (AppInit appInit : mAppInitList) {
            if (appInit.onlyInMainProcess()) {
                appInit.onInit(mBaseApplication);
            }
        }

        // Only restore tasks here.
        TasksManager.getImpl().init();
        Theme.updateThemes();
        ThemeList.updateThemes(true);

        mBaseApplication.registerReceiver(mAgencyBroadcastReceiver, new IntentFilter(HSNotificationConstant.HS_APPSFLYER_RESULT));
        AcbAds.getInstance().initializeFromGoldenEye(mBaseApplication, new AcbAds.GoldenEyeInitListener() {
            @Override
            public void onInitialized() {

            }
        });
        AcbAds.getInstance().setLogEventListener(new AcbAds.logEventListener() {
            @Override
            public void logFirebaseEvent(String s, Bundle bundle) {

                if (GdprUtils.isNeedToAccessDataUsage()) {
                    // TODO Firebase event.
                }

            }
        });
        if (HSGdprConsent.isGdprUser()) {
            if (HSGdprConsent.getConsentState() != HSGdprConsent.ConsentState.ACCEPTED) {
                AcbAds.getInstance().setGdprInfo(GDPR_USER, GDPR_NOT_GRANTED);
            }
        }

        CallAssistantManager.init(new CpCallAssistantFactoryImpl());
        MessageCenterManager.init(new CpMessageCenterFactoryImpl());

        ContactManager.init();

        AcbRewardAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_REWARD_VIDEO);
        SystemAppsManager.getInstance().init();
        NotificationCondition.init();

        AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_RESULT_PAGE);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_RESULT_PAGE_INTERSTITIAL);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.CASHCENTER);
        ColorPhonePermanentUtils.keepAlive();

        Upgrader.upgrade();
        addGlobalObservers();
        initModules();
        checkModuleAdPlacement();

        initChargingReport();
        initLockerCharging();
        initNotificationToolbar();

        Glide.get(mBaseApplication).setMemoryCategory(MemoryCategory.HIGH);
        String popularThemeBgUrl = HSConfig.optString("", "Application", "Special", "SpecialBg");
        GlideApp.with(mBaseApplication).downloadOnly().load(popularThemeBgUrl);

        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    LockJobService.startJobScheduler();
                }
            }
        }, TIME_NEED_LOW);

        lifeCallback();
        initNotificationAlarm();

        SmsFlashListener.getInstance().start();

        logUserLevelDistribution();

        watchLifeTimeAutopilot();
    }

    private void watchLifeTimeAutopilot() {
        IntentFilter configFinishedFilter = new IntentFilter();
        configFinishedFilter.addAction(AutopilotConfig.ACTION_USER_INIT_COMPLETE);
        mBaseApplication.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Theme.updateThemes();
            }
        }, configFinishedFilter, AcbNotificationConstant.getSecurityPermission(mBaseApplication), null);
    }

    private void copyMediaFromAssertToFile() {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                doCopyTheme(10000, "randomtheme.mp4");
                doCopyTheme(65, "dog.mp4");
            }
        });
    }

    private void doCopyTheme(int id, String fileName) {
        final File file = new File(FileUtils.getMediaDirectory(), "Mp4_" + (id - 2));
        try {
            if (!(file.isFile() && file.exists())) {
                Utils.copyAssetFileTo(mBaseApplication,
                        fileName, file);
                HSLog.d("CopyFile", fileName + " copy ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            boolean result = file.delete();
            HSLog.d("CopyFile", fileName + " deleted " + result);
        }
    }

    private void lifeCallback() {
        mBaseApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            private Class<? extends Activity> exitActivityClazz;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity instanceof ColorPhoneActivity) {
                    ActivitySwitchUtil.onMainViewCreate();
                }

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                activityStack.push(1);
                HSPreferenceHelper.getDefault().putLong(NotificationConstants.PREFS_APP_OPENED_TIME, System.currentTimeMillis());
                ActivitySwitchUtil.onActivityChange(exitActivityClazz, activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (!activityStack.isEmpty()) {
                    activityStack.pop();
                }

                exitActivityClazz = activity.getClass();
            }

            @Override
            public void onActivityStopped(Activity activity) {
                ActivitySwitchUtil.onActivityExit(activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }


    private void updateCallFinishFullScreenAdPlacement() {
        if (CallFinishUtils.isCallFinishFullScreenAdEnabled() && !isCallAssistantActivated) {
            HSLog.d("Ad Active ： " + AdPlacements.AD_CALL_ASSISTANT_FULL_SCREEN);
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_CALL_ASSISTANT_FULL_SCREEN);
            isCallAssistantActivated = true;
        }
    }

    private void initChargingReport() {
        long firstInstallTime = HSSessionMgr.getFirstSessionStartTime();
        AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_CHARGING_REPORT);
        ChargingReportConfiguration configuration = new ChargingReportConfiguration.Builder()
                .adPlacement(AdPlacements.AD_CHARGING_REPORT)
                .appName(mBaseApplication.getResources().getString(R.string.smart_charging))
                .appIconResourceId(R.drawable.ic_launcher)
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
                Analytics.logEvent(s, strings);

            }

            @Override
            public void logAdEvent(String s, boolean b) {
                Analytics.logEvent("AcbAdNative_Viewed_In_App", s, String.valueOf(b));
            }

            @Override
            public void onChargingReportShown() {

            }

            @Override
            public void onChargingReportDismiss(DismissType dismissType) {

            }
        });

        ChargingImproverManager.getInstance().init(new ChargingImproverCallbackImpl());
    }

    private void initLockerCharging() {
        if (!SmartChargingSettings.isChargingScreenConfigEnabled()
                && !LockerSettings.isLockerConfigEnabled()) {
            return;
        }
        LockScreenStarter.init();

        HSLog.d("Start", "initLockScreen");
        LockerCustomConfig.get().setLauncherIcon(R.drawable.ic_launcher);
        LockerCustomConfig.get().setCustomScreenIcon(R.drawable.ic_charging_screen_logo);
        LockerCustomConfig.get().setSPFileName("colorPhone_locker");
        LockerCustomConfig.get().setLockerAdName(AdPlacements.AD_LOCKER);
        LockerCustomConfig.get().setChargingExpressAdName(AdPlacements.AD_CHARGING_SCREEN);
        LockerCustomConfig.get().setEventDelegate(new LockerEvent());
        LockerCustomConfig.get().setRemoteLogger(new LockerLogger());
        LockerCustomConfig.get().setGameCallback(new LockerCustomConfig.GameCallback() {
            @Override
            public void startGameCenter(Context context) {
                CmGameUtil.startCmGameActivity(context, "Locker");
            }

            @Override
            public boolean isGameEnable() {
                return CmGameUtil.canUseCmGame()
                        && HSConfig.optBoolean(false, "Application", "GameCenter", "LockScreenEnable");
            }
        });

        FloatWindowCompat.initLockScreen(mBaseApplication);
        HSChargingManager.getInstance().start();

    }

    private void addGlobalObservers() {
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, mObserver);
        HSGlobalNotificationCenter.addObserver(CallAssistantConsts.NOTIFY_CHANGE_CALL_ASSISTANT, mObserver);
        HSGlobalNotificationCenter.addObserver(ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH, mObserver);
        HSGlobalNotificationCenter.addObserver(LockerSettings.NOTIFY_LOCKER_STATE, mObserver);
        HSGlobalNotificationCenter.addObserver(ChargingScreenSettings.NOTIFY_CHARGING_SCREEN_STATE, mObserver);
        HSGlobalNotificationCenter.addObserver(SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE, mObserver);

        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);

        BroadcastCenter.register(mBaseApplication, new BroadcastListener() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    ScreenStatusReceiver.onScreenOff(context);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    ScreenStatusReceiver.onScreenOn(context);
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    ScreenStatusReceiver.onUserPresent(context);

                    if (RequestPermissionsActivity.isShowOnLockScreenDialogEnable()) {
                        ScreenFlashFactory factory = ScreenFlashManager.getInstance().getAcbCallFactory();
                        if (factory instanceof CpScreenFlashFactoryImpl) {
                            if (((CpScreenFlashFactoryImpl) factory).isScreenFlashNotShown()) {
                                ArrayList<String> perms = new ArrayList<>(1);
                                perms.add(RequestPermissionsActivity.PERMISSION_SHOW_ON_LOCK_SCREEN_OUTSIDE);
                                RequestPermissionsActivity.start(context, "", perms);
                            }
                        }
                    }


                    if (CashCenterGuideDialog.isPeriod()) {
                        CashCenterGuideDialog.showCashCenterGuideDialog(context);

                        Threads.postOnMainThreadDelayed(() -> {
                            FloatWindowDialog dialog = FloatWindowManager.getInstance().getDialog(CashCenterGuideDialog.class);
                            FloatWindowManager.getInstance().removeDialog(dialog);
                        }, 3 * DateUtils.MINUTE_IN_MILLIS);

                    } else {
                        HSLog.i("CCTest", "not time");
                    }

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

        Module sms = new Module();
        sms.setAdName(AdPlacements.AD_MSG);
        sms.setAdType(Module.AD_EXPRESS);
        sms.setChecker(new Module.Checker() {
            @Override
            public boolean isEnable() {
                return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT);
            }
        });

        mModules.add(locker);
        mModules.add(charging);
        mModules.add(sms);

    }

    private void systemFix() {
        // Fix crash: NoClassDefFoundError: android.os.AsyncTask
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            try {
                Class.forName("android.os.AsyncTask");
            } catch (Throwable ignore) {
                // ignored
            }
        }
    }


    public void onTerminate() {
        HSGlobalNotificationCenter.removeObserver(mObserver);
    }

    public static void checkCallAssistantAdPlacement() {
        final String adName = CallAssistantManager.getInstance().getCallAssistantFactory().getCallIdleConfig().getAdPlaceName();
        boolean enable = CallAssistantSettings.isCallAssistantModuleEnabled();
        checkExpressAd(adName, enable);

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

        ColorPhonePermanentUtils.checkAliveForProcess();

        updateCallFinishFullScreenAdPlacement();
        AdManager.getInstance().setEnable(ConfigSettings.showAdOnDetailView() || ConfigSettings.showAdOnApplyTheme());
    }

    private static void checkExpressAd(String adName, boolean enable) {
        HSLog.d("AD_CHECK_express", "Name = " + adName + ", enable = " + enable);
        if (enable) {
            AcbExpressAdManager.getInstance().activePlacementInProcess(adName);
        } else {
            AcbExpressAdManager.getInstance().deactivePlacementInProcess(adName);
        }
    }

    private static void checkNativeAd(String adName, boolean enable) {
        HSLog.d("AD_CHECK_native", "Name = " + adName + ", enable = " + enable);
        if (enable) {
            AcbNativeAdManager.getInstance().activePlacementInProcess(adName);
        } else {
            AcbNativeAdManager.getInstance().deactivePlacementInProcess(adName);
        }
    }

    public static ConfigLog getConfigLog() {
        return mConfigLog;
    }

    // TODO delay
    public static void initNotificationAlarm() {
        AlarmManager alarmMgr = (AlarmManager) HSApplication.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(HSApplication.getContext(), NotificationAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(HSApplication.getContext(), 0, intent, 0);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR, 6);
        time.set(Calendar.AM_PM, Calendar.PM);
        time.set(Calendar.MINUTE, 30);

        long setTime = time.getTimeInMillis();
        long timeInMillis = setTime > System.currentTimeMillis() ? setTime : setTime + DateUtils.DAY_IN_MILLIS;
        if (alarmMgr != null) {
            try {
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, DateUtils.DAY_IN_MILLIS, pendingIntent);
            } catch (NullPointerException ingore) {
            }
        }
    }


    protected void initFabric() {
        // Set up Crashlytics, disabled for debug builds
        if (!isFabricInitted) {
            Fabric.with(mBaseApplication, new Crashlytics(), new Answers());
            isFabricInitted = true;
        }
    }

    private void recordInstallType() {
        HSPublisherMgr.PublisherData data = HSPublisherMgr.getPublisherData(mBaseApplication);
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
        Analytics.logEvent("Agency_Info", "install_type", installType, "campaign_id", "" + data.getCampaignID(), "user_level", "" + HSConfig.optString("not_configured", "UserLevel"));

        final String PREF_KEY_AGENCY_INFO_LOGGED = "PREF_KEY_AGENCY_INFO_LOGGED";

        if (HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode) {
            if (!HSPreferenceHelper.getDefault().contains(PREF_KEY_AGENCY_INFO_LOGGED)) {
                HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_AGENCY_INFO_LOGGED, true);
                Analytics.logEvent("New_User_Agency_Info", "install_type", installType, "user_level", "" + HSConfig.optString("not_configured", "UserLevel"), "version_code", "" + HSApplication.getCurrentLaunchInfo().appVersionCode);
            }
        }
    }

    private void logUserLevelDistribution() {

    }

}

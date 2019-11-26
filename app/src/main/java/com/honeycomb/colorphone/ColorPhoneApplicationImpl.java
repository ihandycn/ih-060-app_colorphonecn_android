package com.honeycomb.colorphone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.os.BuildCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashFactory;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.utils.FileUtils;
import com.acb.colorphone.permissions.StableToast;
import com.honeycomb.colorphone.util.StartProcessTestAutopilotUtils;
import com.honeycomb.colorphone.wallpaper.EventsDelegate;
import com.honeycomb.colorphone.wallpaper.LauncherAnalytics;
import com.honeycomb.colorphone.wallpaper.Manager;
import com.call.assistant.customize.CallAssistantConsts;
import com.call.assistant.customize.CallAssistantManager;
import com.call.assistant.customize.CallAssistantSettings;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenActivity;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerActivity;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.RingtoneImageLoader;
import com.colorphone.ringtones.RingtoneSetter;
import com.colorphone.ringtones.WebLauncher;
import com.colorphone.ringtones.module.Ringtone;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ContactsRingtoneSelectActivity;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.boost.BoostActivity;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.boost.FloatWindowDialog;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.cashcenter.CashCenterGuideDialog;
import com.honeycomb.colorphone.cmgame.NotificationBarInit;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.dialer.notification.NotificationChannelManager;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.factoryimpl.CpCallAssistantFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpMessageCenterFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpScreenFlashFactoryImpl;
import com.honeycomb.colorphone.feedback.FeedbackManager;
import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.lifeassistant.LifeAssistantConfig;
import com.honeycomb.colorphone.lifeassistant.LifeAssistantOccasion;
import com.honeycomb.colorphone.module.ChargingImproverCallbackImpl;
import com.honeycomb.colorphone.module.LockerEvent;
import com.honeycomb.colorphone.module.LockerLogger;
import com.honeycomb.colorphone.module.Module;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.news.WebViewActivity;
import com.honeycomb.colorphone.notification.CleanGuideCondition;
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
import com.honeycomb.colorphone.util.DeviceUtils;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.Upgrader;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.HSSessionMgr;
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
import com.ihs.device.monitor.usage.monitor.AppMobileMonitorMgr;
import com.ihs.device.monitor.usage.monitor.AppUsageMonitorMgr;
import com.ihs.device.permanent.HSPermanentUtils;
import com.ihs.libcharging.HSChargingManager;
import com.ihs.permission.HSPermissionRequestMgr;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.connection.FileDownloadUrlConnection;
import com.messagecenter.customize.MessageCenterManager;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.debug.SharedPreferencesOptimizer;
import com.superapps.occasion.OccasionManager;
import com.superapps.push.PushMgr;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;
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

import static android.content.IntentFilter.SYSTEM_HIGH_PRIORITY;
import static net.appcloudbox.AcbAds.GDPR_NOT_GRANTED;
import static net.appcloudbox.AcbAds.GDPR_USER;

public class ColorPhoneApplicationImpl {
    private static final long TIME_NEED_LOW = 10 * 1000; // 10s
    private static ConfigLog mConfigLog;

    boolean needRestartApp;

    private List<Module> mModules = new ArrayList<>();
    private DailyLogger mDailyLogger;

    private static Stack<Integer> activityStack = new Stack<>();
    private List<AppInit> mAppInitList = new ArrayList<>();

    private HSApplication mBaseApplication;
    private HomeKeyWatcher homeKeyWatcher;
    private static final String PREF_KEY_FIRST_LAUNCH = "PREF_KEY_FIRST_LAUNCH";

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
                Analytics.logEvent("ColorPhone_Session_Start");
                checkModuleAdPlacement();

                Beta.checkUpgrade(false, true);
                /*
                 *  Because we disabled {@link com.ihs.device.monitor.usage.UsageBroadcastReceiver}, handle it ourself.
                 */
                AppUsageMonitorMgr.getInstance().handleOnSessionStart();
                AppMobileMonitorMgr.getInstance().handleOnSessionStart();

                HSLog.d("Session Start.");
            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("Session End.");
                onSessionEnd();
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                checkModuleAdPlacement();
                // Call-Themes update timely.
                ThemeList.getInstance().updateThemesTotally();
                NotificationManager.getInstance().showNotificationToolbarIfEnabled();
                ConfigChangeManager.getInstance().onChange(ConfigChangeManager.REMOTE_CONFIG);

                CrashGuard.updateIgnoredCrashes();
                NotificationCondition.getsInstance().onConfigChange();
                // remove download New Type when config changed to reduce
//                downloadNewType();
            } else if (ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH.equals(notificationName)) {
                ColorPhonePermanentUtils.checkAliveForProcess();
            } else if (SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE.equals(notificationName)) {
                BoostActivity.start(HSApplication.getContext(), ResultConstants.RESULT_TYPE_BOOST_LOCKER);
            } else if (Constants.NOTIFY_KEY_APP_FULLY_DISPLAY.equals(notificationName)) {
                for (AppInit appInit : mAppInitList) {
                    if (appInit.afterAppFullyDisplay()) {
                        appInit.onInit(mBaseApplication);
                    }
                }
            } else if (TextUtils.equals(FloatWindowController.NOTIFY_KEY_LOCKER_DISMISS, notificationName)) {
                if (CashCenterGuideDialog.isPeriod(HSApplication.getContext())) {
                    CashCenterGuideDialog.showCashCenterGuideDialog(HSApplication.getContext());

                    Threads.postOnMainThreadDelayed(() -> {
                        FloatWindowDialog dialog = FloatWindowManager.getInstance().getDialog(CashCenterGuideDialog.class);
                        FloatWindowManager.getInstance().removeDialog(dialog);
                    }, 3 * DateUtils.MINUTE_IN_MILLIS);

                } else {
                    HSLog.i("CCTest", "not time");
                }

                NewsManager.getInstance().preloadForLifeAssistant(null);
                LifeAssistantConfig.recordLifeAssistantCheck();
                OccasionManager.getInstance().handleOccasion(new LifeAssistantOccasion());
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

            if (DefaultPhoneUtils.isDefaultPhone() && BuildCompat.isAtLeastO()) {
                NotificationChannelManager.initChannels(mBaseApplication);
            }
        }, "Permission_Check_Above23_FirstSessionEnd");

        if (needRestartApp) {
            needRestartApp = false;
            DeviceUtils.triggerRebirth(mBaseApplication);
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
            if (Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PREF_KEY_FIRST_LAUNCH, true)) {
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(PREF_KEY_FIRST_LAUNCH, false);
                boolean skip = StartProcessTestAutopilotUtils.shouldShowSkipOnFixAlert();
                boolean guide = StartProcessTestAutopilotUtils.shouldGuideThemeSet();
                String type;
                if (skip) {
                    if (guide) {
                        type = "Guide";
                    } else {
                        type = "Skip";
                    }
                } else {
                    type = "Now";
                }
                LauncherAnalytics.logEvent("Start_Process_Test_R1", true, "Type", type);
            }
        }
    };

    /**
     * Size of theme preview image.
     */
    public static int mWidth;
    public static int mHeight;

    private boolean isCallAssistantActivated;

    public static long launchTime;

    public static boolean isAppForeground() {
        return !activityStack.isEmpty();
    }

    public ColorPhoneApplicationImpl(HSApplication application) {
        mBaseApplication = application;
    }

    @DebugLog
    public void onCreate() {
        systemFix();
        mAppInitList.add(new GdprInit());
        mAppInitList.add(new ScreenFlashInit());
        mAppInitList.add(new NotificationBarInit());

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
        Manager.getInstance().setDelegate(new EventsDelegate() {
            @Override
            public void logEvent(String eventID) {
                Analytics.logEvent(eventID);
            }

            @Override
            public void logEvent(String eventID, String... vars) {
                Analytics.logEvent(eventID, vars);
            }
        });
    }

    private void onWorkProcessCreate() {
        HSPermanentUtils.setJobSchedulePeriodic(2 * DateUtils.HOUR_IN_MILLIS);
    }

    private void onAllProcessCreated() {
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

        mHeight = Dimensions.getPhoneHeight(mBaseApplication);
        mWidth = Dimensions.getPhoneWidth(mBaseApplication);
        mConfigLog = new ConfigLogDefault();
        mDailyLogger = new DailyLogger();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        FileDownloader.setupOnApplicationOnCreate(mBaseApplication)
                .connectionCreator(new FileDownloadUrlConnection.Creator(
                        new FileDownloadUrlConnection.Configuration()
                                .connectTimeout(8000)
                                .readTimeout(4000)))
                .commit();

        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                initAutopilot();
            }
        });

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

        HSPermissionRequestMgr.getInstance().init(mBaseApplication);

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
            if (!LockerActivity.exist) {
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

        for (AppInit appInit : mAppInitList) {
            if (appInit.onlyInMainProcess() && !appInit.afterAppFullyDisplay()) {
                appInit.onInit(mBaseApplication);
            }
        }

        ThemeList.getInstance().initThemes();

        copyMediaFromAssertToFile();

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

        initKuyinRingtone();

        AcbRewardAdManager.getInstance().activePlacementInProcess(Placements.AD_REWARD_VIDEO);
        SystemAppsManager.getInstance().init();
        NotificationCondition.init();
        CleanGuideCondition.getInstance();

        AcbNativeAdManager.getInstance().activePlacementInProcess(Placements.BOOST_DONE);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.BOOST_WIRE);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.CASHCENTER);
        ColorPhonePermanentUtils.keepAlive();

        Upgrader.upgrade();
        addGlobalObservers();
        initModules();
        checkModuleAdPlacement();

        initChargingReport();
        initLockerCharging();

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

        DauChecker.get().start();
        if (mDailyLogger != null) {
            mDailyLogger.checkAndLog();
        }

        Threads.postOnMainThreadDelayed(FeedbackManager::sendFeedbackToServerIfNeeded, 10 * DateUtils.SECOND_IN_MILLIS);

        if (DefaultPhoneUtils.isDefaultPhone() && BuildCompat.isAtLeastO()) {
            NotificationChannelManager.initChannels(mBaseApplication);
        }

        mBaseApplication.registerComponentCallbacks(new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(Configuration newConfig) {

            }

            @Override
            public void onLowMemory() {
                Analytics.logEvent("Device_Check_LowMemory",
                        "Brand", AutoLogger.getBrand(), "Os", AutoLogger.getOSVersion());
            }
        });

        homeKeyWatcher = new HomeKeyWatcher(mBaseApplication);
        homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            long lastRecord;
            boolean cpuChangeToHigh = false;
            boolean batteryChangeToLow = false;

            @Override
            public void onHomePressed() {
                Analytics.logEvent("Home_Back_Tracked");

                int batteryLevel = DeviceManager.getInstance().getBatteryLevel();
                if (batteryLevel < 20) {
                    if (batteryChangeToLow) {
                        batteryChangeToLow = false;
                        Analytics.logEvent("Battery_Power_LowTo20");
                    }
                } else {
                    batteryChangeToLow = true;
                }

                float cpuTemp = DeviceManager.getInstance().getCpuTemperatureCelsius();
                if (cpuTemp >= 45) {
                    boolean timeInterVal = System.currentTimeMillis() - lastRecord > 5 * DateUtils.MINUTE_IN_MILLIS;
                    if (cpuChangeToHigh && timeInterVal) {
                        cpuChangeToHigh = false;
                        Analytics.logEvent("CPU_Temp_HighTo55");
                        lastRecord = System.currentTimeMillis();
                    }
                } else {
                    cpuChangeToHigh = true;
                }

                Analytics.logEvent("Storage_Occupied", "Memory", String.valueOf(DeviceManager.getInstance().getRamUsage()));

                boolean cancel = StableToast.cancelToast();
                if (cancel) {
                    long curTimeMills = System.currentTimeMillis();
                    long intervalMills = StableToast.timeMills - curTimeMills;
                    long secondsInTen = intervalMills / 10000 + 1;

                    if (!TextUtils.isEmpty(StableToast.logEvent)) {
                        Analytics.logEvent(StableToast.logEvent, "Duration", String.valueOf(secondsInTen * 10));
                        StableToast.logEvent = null;
                    }
                }
            }

            @Override
            public void onRecentsPressed() {

            }
        });
        homeKeyWatcher.startWatch();
    }

    private void initKuyinRingtone() {
        RingtoneConfig.getInstance().setRingtoneImageLoader(new RingtoneImageLoader() {
            @Override
            public void loadImage(Context context, String imageUrl, ImageView imageView, int defaultResId) {
                GlideApp.with(context)
                        .load(imageUrl)
                        .placeholder(defaultResId)
                        .into(imageView);
            }
        });

        RingtoneConfig.getInstance().setRingtoneSetter(new RingtoneSetter() {
            @Override
            public boolean onSetRingtone(Ringtone ringtone) {
                return true;
            }

            @Override
            public boolean onSetAsDefault(Ringtone ringtone) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(mBaseApplication)) {
                        // Check permission
                        Toast.makeText(mBaseApplication, "设置铃声失败，请授予权限", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + mBaseApplication.getPackageName()));
                        Navigations.startActivitySafely(mBaseApplication, intent);
                        return false;
                    }
                }

                RingtoneHelper.setDefaultRingtoneInBackground(ringtone.getFilePath(), ringtone.getTitle());
                Toasts.showToast("设置成功");

                RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_SetForAll_Success",
                        "Name", ringtone.getTitle(),
                        "Type:", ringtone.getColumnSource());
                RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_Set_Success",
                        "Name", ringtone.getTitle(),
                        "Type:", ringtone.getColumnSource());
                return true;
            }

            @Override
            public boolean onSetForSomeOne(Ringtone ringtone) {
                // TODO 检查权限
                ContactsRingtoneSelectActivity.startSelectRingtone(HSApplication.getContext(), ringtone);
                return true;
            }
        });
        RingtoneConfig.getInstance().setRemoteLogger(new RingtoneConfig.RemoteLogger() {
            @Override
            public void logEvent(String eventID) {
                Analytics.logEvent(eventID);
            }

            @Override
            public void logEvent(String eventID, String... vars) {
                Analytics.logEvent(eventID, vars);
            }
        });

        RingtoneConfig.getInstance().setWebLauncher(new WebLauncher() {
            @Override
            public boolean handleUrl(String url) {
                Navigations.startActivitySafely(mBaseApplication, WebViewActivity.newIntent(url, false, WebViewActivity.FROM_LIST));
                return true;
            }
        });

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

                if (activity.getPackageName().equals(HSApplication.getContext().getPackageName())) {
                    boolean cancel = StableToast.cancelToast();
                    if (cancel) {
                        long curTimeMills = System.currentTimeMillis();
                        long intervalMills = StableToast.timeMills - curTimeMills;
                        long secondsInTen = intervalMills / 10000 + 1;

                        if (!TextUtils.isEmpty(StableToast.logEvent)) {
                            Analytics.logEvent(StableToast.logEvent, "Duration", String.valueOf(secondsInTen * 10));
                            StableToast.logEvent = null;
                        }
                    }
                }
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
            HSLog.d("Ad Active ： " + Placements.AD_CALL_ASSISTANT_FULL_SCREEN);
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.AD_CALL_ASSISTANT_FULL_SCREEN);
            isCallAssistantActivated = true;
        }
    }

    private void initChargingReport() {
        long firstInstallTime = HSSessionMgr.getFirstSessionStartTime();
        AcbNativeAdManager.getInstance().activePlacementInProcess(Placements.AD_CHARGING_REPORT);
        ChargingReportConfiguration configuration = new ChargingReportConfiguration.Builder()
                .adPlacement(Placements.AD_CHARGING_REPORT)
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
        LockerCustomConfig.get().setLockerAdName(Placements.AD_LOCKER);
        LockerCustomConfig.get().setChargingExpressAdName(Placements.AD_CHARGING_SCREEN);
        LockerCustomConfig.get().setEventDelegate(new LockerEvent());
        LockerCustomConfig.get().setRemoteLogger(new LockerLogger());
        LockerCustomConfig.get().setGameCallback(new LockerCustomConfig.GameCallback() {
            @Override
            public void startGameCenter(Context context) {
            }

            @Override
            public boolean isGameEnable() {
                return false;
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
        HSGlobalNotificationCenter.addObserver(Constants.NOTIFY_KEY_APP_FULLY_DISPLAY, mObserver);
        HSGlobalNotificationCenter.addObserver(FloatWindowController.NOTIFY_KEY_LOCKER_DISMISS, mObserver);

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

                    if (CashCenterGuideDialog.isPeriod(context)) {
                        CashCenterGuideDialog.showCashCenterGuideDialog(context);

                        Threads.postOnMainThreadDelayed(() -> {
                            FloatWindowDialog dialog = FloatWindowManager.getInstance().getDialog(CashCenterGuideDialog.class);
                            FloatWindowManager.getInstance().removeDialog(dialog);
                        }, 3 * DateUtils.MINUTE_IN_MILLIS);

                    } else {
                        HSLog.i("CCTest", "not time");
                    }

                    NewsManager.getInstance().preloadForLifeAssistant(null);
                    if (!MessageCenterManager.getInstance().getConfig().waitForLocker()) {
                        LifeAssistantConfig.recordLifeAssistantCheck();
                        OccasionManager.getInstance().handleOccasion(new LifeAssistantOccasion());
                    }
                }
            }
        }, screenFilter);
    }

    private void initModules() {
        Module locker = new Module();
        locker.setAdName(Placements.AD_LOCKER);
        locker.setAdType(Module.AD_EXPRESS);
        locker.setNotifyKey(LockerSettings.NOTIFY_LOCKER_STATE);
        locker.setChecker(new Module.Checker() {
            @Override
            public boolean isEnable() {
                return LockerSettings.isLockerEnabled();
            }
        });

        Module charging = new Module();
        charging.setAdName(Placements.AD_CHARGING_SCREEN);
        charging.setAdType(Module.AD_EXPRESS);
        charging.setNotifyKey(ChargingScreenSettings.NOTIFY_CHARGING_SCREEN_STATE);
        charging.setChecker(new Module.Checker() {
            @Override
            public boolean isEnable() {
                return SmartChargingSettings.isChargingScreenEnabled();
            }
        });

        Module sms = new Module();
        sms.setAdName(Placements.AD_MSG);
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
        homeKeyWatcher.stopWatch();
    }

    public static void checkCallAssistantAdPlacement() {
        final String adName = CallAssistantManager.getInstance().getCallAssistantFactory().getCallIdleConfig().getAdPlaceName();
        boolean enable = CallAssistantSettings.isCallAssistantModuleEnabled();
        checkExpressAd(adName, enable);

    }

    public static void checkChargingReportAdPlacement() {
        checkExpressAd(Placements.AD_CHARGING_REPORT, SmartChargingSettings.isChargingReportEnabled());
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

package com.honeycomb.colorphone;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
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
import com.acb.call.utils.FileUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.call.assistant.customize.CallAssistantConsts;
import com.call.assistant.customize.CallAssistantManager;
import com.call.assistant.customize.CallAssistantSettings;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.dialer.ConfigProvider;
import com.honeycomb.colorphone.dialer.notification.NotificationChannelManager;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.factoryimpl.CpCallAssistantFactoryImpl;
import com.honeycomb.colorphone.factoryimpl.CpMessageCenterFactoryImpl;
import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.module.Module;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.news.NewsPushActivity;
import com.honeycomb.colorphone.news.NewsTest;
import com.honeycomb.colorphone.notification.NotificationAlarmReceiver;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.receiver.ScreenStatusReceiver;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.trigger.DailyTrigger;
import com.honeycomb.colorphone.triviatip.TriviaTip;
import com.honeycomb.colorphone.util.ADAutoPilotUtils;
import com.honeycomb.colorphone.util.ColorPhonePermanentUtils;
import com.honeycomb.colorphone.util.DailyLogger;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.Upgrader;
import com.honeycomb.colorphone.weather.WeatherPushManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.analytics.publisher.HSPublisherMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.permanent.HSPermanentUtils;
import com.liulishuo.filedownloader.FileDownloader;
import com.messagecenter.customize.MessageCenterManager;
import com.messagecenter.customize.MessageCenterSettings;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.debug.SharedPreferencesOptimizer;
import com.superapps.phonestate.PhoneStateManager;
import com.superapps.util.Commons;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.expressad.AcbExpressAdManager;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;
import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.common.notificationcenter.AcbNotificationConstant;
import net.appcloudbox.common.utils.AcbApplicationHelper;
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

public class ColorPhoneApplication extends HSApplication {
    private static final long TIME_NEED_LOW = 12 * 1000; // 10s

    private static final long TIME_NEED_FIRSTINIT_LOW = 10 * 1000;

    private static ConfigLog mConfigLog;

    private List<Module> mModules = new ArrayList<>();
    private DailyLogger mDailyLogger;

    private static Stack<Integer> activityStack = new Stack<>();
    private List<AppInit> mAppInitList = new ArrayList<>();

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
                onSessionEnd();
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                checkModuleAdPlacement();
                // Call-Themes update timely.
                Theme.updateThemesTotally();
                ConfigChangeManager.getInstance().onChange(ConfigChangeManager.REMOTE_CONFIG);

                CrashGuard.updateIgnoredCrashes();
                PushManager.getInstance().onConfigChanged();
                // remove download New Type when config changed to reduce
//                downloadNewType();
            } else if (ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH.equals(notificationName)) {
                ColorPhonePermanentUtils.checkAliveForProcess();
            } else {
                checkModuleAdPlacement();
            }
        }
    };

    public void onSessionEnd() {
        Preferences.get(Constants.DESKTOP_PREFS).doOnce(() -> {
            if (ColorPhoneApplication.getContext().getApplicationContext() instanceof ColorPhoneApplication) {
                if (mDailyLogger != null) {
                    mDailyLogger.logOnceFirstSessionEndStatus();
                }
            }
        }, "Permission_Check_Above23_FirstSessionEnd");

        // Just ensure phone state not listen.
        PhoneStateManager.getInstance().listenPhoneState();

        if (mDailyLogger != null) {
            mDailyLogger.checkAndLog();
        }
    }

    private BroadcastReceiver mAutopilotFetchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCallFinishFullScreenAdPlacement();
            updateTriviaTipAdPlacement();
            AdManager.getInstance().setEnable(ConfigSettings.showAdOnDetailView() || ConfigSettings.showAdOnApplyTheme());
            ConfigChangeManager.getInstance().onChange(ConfigChangeManager.AUTOPILOT);
            ADAutoPilotUtils.update();
            ADAutoPilotUtils.logAutopilotEventToFaric();

            TriviaTip.cacheImagesFirstTime();

        }
    };

    /**
     * Size of theme preview image.
     */
    public static int mWidth;
    public static int mHeight;

    private static boolean isFabricInitted;
    public static long launchTime;

    public static boolean isAppForeground() {
        return !activityStack.isEmpty();
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();

        systemFix();

        AcbApplicationHelper.init(this);

        launchTime = System.currentTimeMillis();
        mAppInitList.add(new GdprInit());
        mAppInitList.add(new ScreenFlashInit());
        onAllProcessCreated();

        String packageName = getPackageName();
        String processName = getProcessName();
        if (TextUtils.equals(processName, packageName)) {
            onMainProcessCreate();

        }

        if (processName.endsWith(":work")) {
            onWorkProcessCreate();
        }
    }

    private void onWorkProcessCreate() {
        HSPermanentUtils.setJobSchedulePeriodic(2 * DateUtils.HOUR_IN_MILLIS);
    }

    public static boolean isFabricInitted() {
        return isFabricInitted;
    }

    private void onAllProcessCreated() {
        if (GdprUtils.isNeedToAccessDataUsage()) {
            initFabric();
        }
        AcbService.initialize(this);
        // Init Crash optimizer
        CrashGuard.install();

        // Init ANR optimizer
        SharedPreferencesOptimizer.install(BuildConfig.DEBUG);

        mHeight = Dimensions.getPhoneHeight(this);
        mWidth = Dimensions.getPhoneWidth(this);
        mConfigLog = new ConfigLogDefault();
        mDailyLogger = new DailyLogger();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        FileDownloader.setup(this);

        initAutopilot();

        HSPermanentUtils.initKeepAlive(
                true,
                false,
                false,
                true,
                true,
                false,
                false,
                false,
                null, null);

        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                HSPermanentUtils.startKeepAlive();
            }
        }, TIME_NEED_LOW);
    }

    private void initAutopilot() {
        AutopilotConfig.initialize(this, "Autopilot_Config.json");
        if (!AutopilotConfig.hasConfigFetchFinished()) {
            IntentFilter configFinishedFilter = new IntentFilter();
            configFinishedFilter.addAction(AutopilotConfig.ACTION_CONFIG_FETCH_FINISHED);
            registerReceiver(mAutopilotFetchReceiver, configFinishedFilter, AcbNotificationConstant.getSecurityPermission(this), null);
        }
    }

    @DebugLog
    private void onMainProcessCreate() {
        CrashFix.fix();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannelManager.initChannels(this);
        }
        copyMediaFromAssertToFile();
        DauChecker.get().start();

        for (AppInit appInit : mAppInitList) {
            if (appInit.onlyInMainProcess()) {
                appInit.onInit(this);
            }
        }

        PushManager.getInstance().init();

        TriviaTip.getInstance().init();

        // Only restore tasks here.
        TasksManager.getImpl().init();
        Theme.updateThemes();
        ThemeList.updateThemes(true);

        registerReceiver(mAgencyBroadcastReceiver, new IntentFilter(HSNotificationConstant.HS_APPSFLYER_RESULT));

        CallAssistantManager.init(new CpCallAssistantFactoryImpl());
        MessageCenterManager.init(new CpMessageCenterFactoryImpl());

        ContactManager.init();

        Upgrader.upgrade();
        addGlobalObservers();

        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
        String popularThemeBgUrl = HSConfig.optString("", "Application", "Special", "SpecialBg");
        GlideApp.with(this).downloadOnly().load(popularThemeBgUrl);

        if (getCurrentLaunchInfo() != null && getCurrentLaunchInfo().launchId > 1) {
            delayInitOnFirstLaunch();
        } else {
            Threads.postOnMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    delayInitOnFirstLaunch();
                }
            }, TIME_NEED_FIRSTINIT_LOW);
        }

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

        checkDailyTask();

        watchLifeTimeAutopilot();

        WeatherPushManager.getInstance().updateWeatherIfNeeded();

        ConfigProvider.set(new ConfigProvider() {
            @Override
            public Typeface getCustomBoldTypeface() {
                return Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_BOLD);
            }

            @Override
            public Typeface getCustomMediumTypeface() {
                return Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD);
            }

            @Override
            public Typeface getCustomTypeface() {
                return Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR);
            }
        });
    }

    private void delayInitOnFirstLaunch() {
        ColorPhonePermanentUtils.keepAlive();
        initGoldenEye();
        checkModuleAdPlacement();

        AcbRewardAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_REWARD_VIDEO);
        AcbNativeAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_RESULT_PAGE);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(AdPlacements.AD_RESULT_PAGE_INTERSTITIAL);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.CASHCENTER);

    }

    private void initGoldenEye() {
        AcbAds.getInstance().initializeFromGoldenEye(this);
        AcbAds.getInstance().setLogEventListener(new AcbAds.logEventListener() {
            @Override
            public void logFirebaseEvent(String s, Bundle bundle) {

            }
        });
        if (HSGdprConsent.isGdprUser()) {
            if (HSGdprConsent.getConsentState() != HSGdprConsent.ConsentState.ACCEPTED) {
                AcbAds.getInstance().setGdprInfo(GDPR_USER, GDPR_NOT_GRANTED);
            }
        }
    }

    private void watchLifeTimeAutopilot() {
        IntentFilter configFinishedFilter = new IntentFilter();
        configFinishedFilter.addAction(AutopilotConfig.ACTION_USER_INIT_COMPLETE);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Theme.updateThemes();
                WeatherPushManager.getInstance().onAutoPilotDataInit();

            }
        }, configFinishedFilter, AcbNotificationConstant.getSecurityPermission(this), null);
    }


    private void checkDailyTask() {
        DailyTrigger dailyTrigger = new DailyTrigger();
        boolean active = dailyTrigger.onChance();
        if (active) {
            // Do something once a day.
            dailyTrigger.onConsumeChance();
        }
    }

    private void copyMediaFromAssertToFile() {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                doCopyTheme(10000, "randomtheme.mp4");
                doCopyTheme(8, "deeplove.mp4");
            }
        });
    }

    private void doCopyTheme(int id, String fileName) {
        String targetFileName = "Mp4_" + (id - 2);
        doCopyTheme(targetFileName, fileName);
    }

    private void doCopyTheme(String targetFileName, String srcfileName) {
        final File file = new File(FileUtils.getMediaDirectory(), targetFileName);
        try {
            if (!(file.isFile() && file.exists())) {
                Utils.copyAssetFileTo(getApplicationContext(),
                        srcfileName, file);
                HSLog.d("CopyFile", srcfileName + " copy ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            boolean result = file.delete();
            HSLog.d("CopyFile", srcfileName + " deleted " + result);
        }
    }

    private void lifeCallback() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
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


    @Deprecated
    private void updateCallFinishFullScreenAdPlacement() {

    }

    private void updateTriviaTipAdPlacement() {
        if (TriviaTip.isModuleEnable()) {
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.TRIVIA_TIP_INTERSTITIAL_AD_PLACEMENT_NAME);
            AcbNativeAdManager.getInstance().activePlacementInProcess(Placements.TRIVIA_TIP_NATIVE_AD_PLACEMENT_NAME);
        }
    }

    private void addGlobalObservers() {
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, mObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, mObserver);
        HSGlobalNotificationCenter.addObserver(CallAssistantConsts.NOTIFY_CHANGE_CALL_ASSISTANT, mObserver);
        HSGlobalNotificationCenter.addObserver(ScreenFlashConst.NOTIFY_CHANGE_SCREEN_FLASH, mObserver);

        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        screenFilter.setPriority(SYSTEM_HIGH_PRIORITY);

        BroadcastCenter.register(this, new BroadcastListener() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    ScreenStatusReceiver.onScreenOff(context);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    ScreenStatusReceiver.onScreenOn(context);
                    if (!Commons.isKeyguardLocked(getApplicationContext(), true)) {
                        tryShowWeather(false);
                    }
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    tryShowWeather(true);
                    ScreenStatusReceiver.onUserPresent(context);

                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if (6 <= hour && hour <= 11) {
                        LauncherAnalytics.logEvent("videonews_lockscreen_unlock_morning", "time", String.valueOf(hour));
                    } else if (18 <= hour && hour <= 23) {
                        LauncherAnalytics.logEvent("videonews_lockscreen_unlock_night", "time", String.valueOf(hour - 12));
                    }

                    HSLog.i(NewsManager.TAG, "UserPresentReceiver");
                    if (NewsTest.canShowNewsAlert()) {
                        Threads.postOnMainThreadDelayed(() -> {
                            NewsManager.getInstance().fetchPushNews(null);
                            NewsManager.getInstance().preloadAD();
                        }, 100);

                        Threads.postOnMainThreadDelayed(() -> {
                            if (NewsTest.shouldShowWithAD() && NewsManager.getInstance().getPushBean() != null) {
                                NewsPushActivity.start(ColorPhoneApplication.getContext());
                            } else {
                                HSLog.i(NewsManager.TAG, "canShowNewsAlert no News");
                            }
                        }, 2000);
                    }
                }
            }
        }, screenFilter);
    }

    private void tryShowWeather(boolean hasLockGuard) {
        WeatherPushManager.getInstance().push(this, hasLockGuard);
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

        updateCallFinishFullScreenAdPlacement();
        updateTriviaTipAdPlacement();
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
            Fabric.with(this, new Crashlytics(), new Answers());
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
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
                LauncherAnalytics.logEventAndFirebase("New_User_Agency_Info", "install_type", installType, "user_level", "" + HSConfig.optString("not_configured", "UserLevel"), "version_code", "" + HSApplication.getCurrentLaunchInfo().appVersionCode);
            }
        }
    }

    private void logUserLevelDistribution() {
        String PREF_KEY_New_User_User_Level_LOGGED = "lv_logged";
        HSConfig.fetchRemote();
        if (Utils.isNewUser()) {
            if (!HSPreferenceHelper.getDefault().contains(PREF_KEY_New_User_User_Level_LOGGED)) {
                int delayTimes[] = {20, 40, 65, 95, 125, 365, 1850, 7300, 11000};
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

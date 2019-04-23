package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.bumptech.glide.Glide;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.AppflyerLogger;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigChangeManager;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.menu.SettingsPage;
import com.honeycomb.colorphone.news.NewsPage;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.RewardVideoView;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.libcharging.ChargingPreferenceUtil;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, INotificationObserver {

    public static final String NOTIFICATION_ON_REWARDED = "notification_on_rewarded";

    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_SCROLL_TO_BOTTOM = "prefs_main_scroll_to_bottom";

    private static final int WELCOME_REQUEST_CODE = 2;
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 3;

    private static final int MAIN_TAB_THEMES = 0;
    private static final int MAIN_TAB_NEWS = 1;
    private static final int MAIN_TAB_SETTINGS = 2;
    private static final String MAIN_TAB_KEY = "tabKey";

    private static final int EVENT_CLICK_TOOLBAR = 43020;

    private RecyclerView mRecyclerView;
    private ThemeSelectorAdapter mAdapter;
    private final ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();
    private final ThemeList mThemeList = new ThemeList();
    private RewardVideoView mRewardVideoView;
    private Toolbar toolbar;
    private View settingLayout;
    private NewsPage newsLayout;
    private TextView themesTab;
    private TextView newsTab;
    private TextView settingTab;
    private int currentIndex = MAIN_TAB_THEMES;

    private final static int RECYCLER_VIEW_SPAN_COUNT = 2;
    private boolean initCheckState;
    private boolean isPaused;

    private Handler mHandler = new Handler();

    private boolean mIsHandsDown = false;
    private boolean mIsFirstScrollThisTimeHandsDown = true;
    public static final int SCROLL_STATE_DRAGGING = 1;

    private Runnable UpdateRunnable = new Runnable() {

        @Override
        public void run() {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                runOnUiThread(this);
                return;
            }

            if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
                HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "TaskManager service bind, notifyDataSetChanged");
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

        }
    };

    private Runnable mainViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (logOpenEvent) {
                logOpenEvent = false;
                ColorPhoneApplication.getConfigLog().getEvent().onMainViewOpen();
            }
        }
    };

    private ConfigChangeManager.Callback configChangeCallback =  new ConfigChangeManager.Callback() {
        @Override
        public void onChange(int type) {

        }
    };

    private boolean logOpenEvent;
    private boolean pendingShowRateAlert = false;
    private boolean showAllFeatureGuide = false;
    private boolean isCreate = false;
    private SettingsPage mSettingsPage = new SettingsPage();

    public static void startNews(Context context) {
        Intent intent = new Intent(context, ColorPhoneActivity.class);
        intent.putExtra(MAIN_TAB_KEY, MAIN_TAB_NEWS);
        Navigations.startActivitySafely(context, intent);
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContactManager.getInstance().update();
        if (NotificationUtils.isShowNotificationGuideAlertInFirstSession(this)) {
            Intent intent = new Intent(this, NotificationAccessGuideAlertActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, true);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_APP_IS_FIRST_SESSION, true);
            startActivity(intent);
            HSAlertMgr.delayRateAlert();
            HSPreferenceHelper.getDefault().putBoolean(NotificationUtils.PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, true);
        }
        setTheme(R.style.AppLightStatusBarTheme);

        setContentView(R.layout.activity_main);
        initMainFrame();
        AdManager.getInstance().preload();
        AppflyerLogger.logAppOpen();
        isCreate = true;

        Intent intent  = getIntent();
        if (intent != null) {
            int tabIndex = intent.getIntExtra(MAIN_TAB_KEY, MAIN_TAB_THEMES);
            if (tabIndex != currentIndex) {
                hideOldTab();
                currentIndex = tabIndex;
                switchPage();
                highlightNewTab();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            if (mAdapter.isTipHeaderVisible() &&
                    !PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
                HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "setHeaderTipVisible, " +
                        "notifyDataSetChanged");
                mAdapter.setHeaderTipVisible(false);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void showRewardVideoView(String themeName) {
        requestRewardAd(themeName);
    }

    @DebugLog
    @NonNull
    private void initDrawer() {
        toolbar = findViewById(R.id.toolbar);

        toolbar.setOnClickListener(v -> {
            if (currentIndex == MAIN_TAB_NEWS) {
                if (mHandler.hasMessages(EVENT_CLICK_TOOLBAR)) {
                    newsLayout.onScrollToTop();
                } else {
                    mHandler.sendEmptyMessageDelayed(EVENT_CLICK_TOOLBAR, 500);
                }
            }
        });

        logOpenEvent = true;
        Utils.configActivityStatusBar(this, toolbar, 0);
    }

    private void initTabs() {
        themesTab = findViewById(R.id.main_tab_themes);
        newsTab = findViewById(R.id.main_tab_news);
        settingTab = findViewById(R.id.main_tab_setting);

        themesTab.setOnClickListener(v -> {
            if (currentIndex != MAIN_TAB_THEMES) {
                hideOldTab();
                mRecyclerView.setVisibility(View.VISIBLE);
                currentIndex = MAIN_TAB_THEMES;
                highlightNewTab();
                LauncherAnalytics.logEvent("tab_change");
            }
        });

        newsTab.setOnClickListener(v -> {
            if (currentIndex != MAIN_TAB_NEWS) {
                hideOldTab();
                newsLayout.setVisibility(View.VISIBLE);
                currentIndex = MAIN_TAB_NEWS;
                highlightNewTab();

                LauncherAnalytics.logEvent("mainview_newstab_click", "type", Utils.isNewUser() ? "new" : "upgrade");
                LauncherAnalytics.logEvent("mainview_newstab_show", "type", Utils.isNewUser() ? "new" : "upgrade");
                LauncherAnalytics.logEvent("tab_change");
            }
        });

        settingTab.setOnClickListener(v -> {
            if (currentIndex != MAIN_TAB_SETTINGS) {
                hideOldTab();
                settingLayout.setVisibility(View.VISIBLE);
                currentIndex = MAIN_TAB_SETTINGS;
                highlightNewTab();
                LauncherAnalytics.logEvent("mainview_settingstab_click", "type", Utils.isNewUser() ? "new" : "upgrade");
                LauncherAnalytics.logEvent("tab_change");
            }
        });

        highlightNewTab();
    }

    private void hideOldTab() {
        TextView tv = null;
        Resources res = getResources();
        int resId = 0;
        switch (currentIndex) {
            case MAIN_TAB_THEMES:
                resId = R.drawable.main_tab_themes;
                tv = themesTab;
                mRecyclerView.setVisibility(View.GONE);
                break;
            case MAIN_TAB_NEWS:
                resId = R.drawable.main_tab_news;
                tv = newsTab;
                newsLayout.setVisibility(View.GONE);
                break;
            case MAIN_TAB_SETTINGS:
                resId = R.drawable.main_tab_settings;
                tv = settingTab;
                settingLayout.setVisibility(View.GONE);
                break;
            default:
                HSLog.i("hideOldTab no this tab");
                break;
        }
        if (resId != 0 && tv != null) {
            tv.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(resId), null, null);
        }
    }

    private void highlightNewTab() {
        Resources res = getResources();
        TextView tv = null;
        int resId = 0;
        int stringResId = 0;
        switch (currentIndex) {
            case MAIN_TAB_THEMES:
                resId = R.drawable.main_tab_themes_light;
                stringResId = R.string.toolbar_title_themes;
                tv = themesTab;
                break;
            case MAIN_TAB_NEWS:
                resId = R.drawable.main_tab_news_light;
                stringResId = R.string.toolbar_title_news;
                tv = newsTab;
                break;
            case MAIN_TAB_SETTINGS:
                resId = R.drawable.main_tab_settings_light;
                stringResId = R.string.toolbar_title_settings;
                tv = settingTab;
                break;
            default:
                HSLog.i("highlightNewTab no this tab");
                break;
        }
        if (resId != 0 && tv != null) {
            tv.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(resId), null, null);
        }
        if (stringResId != 0) {
            toolbar.setTitle(stringResId);
        }
    }

    private void switchPage() {
        switch (currentIndex) {
            case MAIN_TAB_NEWS:
                newsLayout.setVisibility(View.VISIBLE);
                break;
            case MAIN_TAB_THEMES:
                mRecyclerView.setVisibility(View.VISIBLE);
                break;
            case MAIN_TAB_SETTINGS:
                settingLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initSettingPage() {
        settingLayout = findViewById(R.id.setting_layout);
        settingLayout.setOnTouchListener((v, event) -> true);
        mSettingsPage.initPage(settingLayout);
    }

    private void initNewsPage() {
        newsLayout = findViewById(R.id.news_layout);
    }

    @DebugLog
    private void initMainFrame() {
        initDrawer();
        initTabs();
        initData();
        initRecyclerView();
        initNewsPage();
        initSettingPage();
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME, this);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED, this);
        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));

        ConfigChangeManager.getInstance().registerCallbacks(
                ConfigChangeManager.AUTOPILOT | ConfigChangeManager.REMOTE_CONFIG, configChangeCallback);

    }

    @Override
    protected void onStart() {
        super.onStart();
        int maxId = -1;
        for (Type type : Type.values()) {
            if (maxId < type.getId()) {
                maxId = type.getId();
            }
        }
        HSPreferenceHelper.getDefault().putInt(NotificationConstants.PREFS_NOTIFICATION_OLD_MAX_ID, maxId);

        // log event
        long sessionPast = System.currentTimeMillis() - SessionMgr.getInstance().getCurrentSessionStartTime();
        boolean isNearSession = Math.abs(sessionPast) < 2000;
        if (isNearSession) {
            if (mAdapter != null && mAdapter.isTipHeaderVisible()) {
                LauncherAnalytics.logEvent("Colorphone_List_Page_Notification_Alert_Show");
            }
        }
        AcbRewardAdManager.preload(1, AdPlacements.AD_REWARD_VIDEO);
        if (!showAllFeatureGuide && isCreate) {
            dispatchPermissionRequest();
        }

        if (!showAllFeatureGuide) {
            isCreate = false;
        }
        showAllFeatureGuide = false;
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            int tabIndex = intent.getIntExtra(MAIN_TAB_KEY, MAIN_TAB_THEMES);
            if (tabIndex != currentIndex) {
                hideOldTab();
                currentIndex = tabIndex;
                switchPage();
                highlightNewTab();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // clear previous observers.
        PermissionHelper.stopObservingPermission();

        HSLog.d("ColorPhoneActivity", "onResume " + mAdapter.getLastSelectedLayoutPos() + "");
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
        if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).startAnimation();
        }

        mAdapter.updateApplyInformationAutoPilotValue();
        mHandler.postDelayed(mainViewRunnable, 1000);
        isPaused = false;
        mAdapter.markForeground(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPaused = true;
        HSLog.d("ColorPhoneActivity", "onPause" + mAdapter.getLastSelectedLayoutPos() + "");
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
        if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).stopAnimation();
        }
        mAdapter.markForeground(false);
        mRecyclerView.getRecycledViewPool().clear();
        mHandler.removeCallbacks(mainViewRunnable);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSettingsPage != null) {
            mSettingsPage.onSaveToggleState();
        }
        saveThemeLikes();
        // TODO: has better solution for OOM?
        Glide.get(this).clearMemory();
    }

    private void dispatchPermissionRequest() {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        if (!isEnabled) {
            return;
        }

        Runnable runnable;

        if (Build.VERSION.SDK_INT < 16) {
            // Not support lottie.
            runnable = () -> requiresPermission();
        } else {
            runnable = () -> PermissionChecker.getInstance().check(this, "AppOpen");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && RuntimePermissions.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
                        == RuntimePermissions.PERMISSION_GRANTED_BUT_NEEDS_REQUEST) {
            RuntimePermissions.requestPermissions(this,
                    new String[]{Manifest.permission.ANSWER_PHONE_CALLS}, FIRST_LAUNCH_PERMISSION_REQUEST);
        }

        Preferences.get(Constants.DESKTOP_PREFS).doLimitedTimes(
                runnable,
                "permission_launch", HSConfig.optInteger(2,"GrantAccess", "MaxCount"));
    }

    /**
     * Only request first launch. (if Enabled and not has permission)
     */
    private void requiresPermission() {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        HSLog.i("Permissions ScreenFlash state change : " + isEnabled);
        if (!isEnabled) {
            return;
        }

        String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};
        boolean phonePerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == RuntimePermissions.PERMISSION_GRANTED;
        boolean contactPerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == RuntimePermissions.PERMISSION_GRANTED;
        if (!phonePerm) {
            LauncherAnalytics.logEvent("Flashlight_Permission_Phone_View_Showed");
        }
        if (!contactPerm) {
            LauncherAnalytics.logEvent("Flashlight_Permission_Contact_View_Showed");
        }
        if (!phonePerm || !contactPerm){
            // Do not have permissions, request them now
            RuntimePermissions.requestPermissions(this, perms, FIRST_LAUNCH_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();

        for(int i = 0; i < permissions.length; ++i) {
            String perm = permissions[i];
            if (grantResults[i] == 0) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        onPermissionsGranted(requestCode, granted);
        onPermissionsDenied(requestCode, denied);
    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
            if (list.contains(Manifest.permission.READ_PHONE_STATE)) {
                LauncherAnalytics.logEvent("Flashlight_Permission_Phone_Allow_Success");
            }
            if (list.contains(Manifest.permission.READ_CONTACTS)) {
                LauncherAnalytics.logEvent("Flashlight_Permission_Contact_Allow_Success");
            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case WELCOME_REQUEST_CODE:
                break;
        }
    }

    private void saveThemeLikes() {
        StringBuilder sb = new StringBuilder(4);
        for (Theme theme : mRecyclerViewData) {
            if (theme.isLike()) {
                sb.append(theme.getId());
                sb.append(",");
            }
        }
        HSPreferenceHelper.getDefault().putString(PREFS_THEME_LIKE, sb.toString());
    }

    private String[] getThemeLikes() {
        String likes = HSPreferenceHelper.getDefault().getString(PREFS_THEME_LIKE, "");
        return likes.split(",");
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        TasksManager.getImpl().onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
        PermissionHelper.stopObservingPermission();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }

        if (mRewardVideoView != null) {
            mRewardVideoView.onCancel();
        }
        ConfigChangeManager.getInstance().removeCallback(configChangeCallback);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mRewardVideoView != null && mRewardVideoView.isLoading()) {
            mRewardVideoView.onHideAdLoading();
            mRewardVideoView.onCancel();
        } else {
            super.onBackPressed();
            // Stop all download tasks;
            TasksManager.getImpl().stopAllTasks();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return true;
    }

    private void initData() {
        mThemeList.fillData(mRecyclerViewData);
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ThemeSelectorAdapter(this, mRecyclerViewData);
        mRecyclerView.setLayoutManager(mAdapter.getLayoutManager());
        mAdapter.setHotThemeHolderVisible(HSConfig.optBoolean(false, "Application", "Special", "SpecialEntrance"));
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.RecycledViewPool pool = mRecyclerView.getRecycledViewPool();

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = mAdapter.getLayoutManager();
                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int pastVisibleItems = manager.findFirstVisibleItemPosition();

                Preferences prefsFile = Preferences.getDefault();
                if (pastVisibleItems + visibleItemCount >= totalItemCount && !prefsFile.getBoolean(PREFS_SCROLL_TO_BOTTOM, false)) {
                    //End of list
                    LauncherAnalytics.logEvent("ColorPhone_List_Bottom_Show");
                    prefsFile.putBoolean(PREFS_SCROLL_TO_BOTTOM, true);
                }

                if (mIsFirstScrollThisTimeHandsDown && mIsHandsDown && dy > 0) {
                    mIsFirstScrollThisTimeHandsDown = false;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_DRAGGING) {
                    mIsHandsDown = true;
                } else {
                    mIsHandsDown = false;
                    mIsFirstScrollThisTimeHandsDown = true;
                }
            }
        });
        // TODO: set proper view count.
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_LED, 1);
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_TECH, 1);
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO, 2);
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_GIF, 2);

        // Header
        updatePermissionHeader();
    }

    private void updatePermissionHeader() {
//        boolean notificationNotGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//                && !PermissionUtils.isNotificationAccessGranted(ColorPhoneActivity.this);
//        boolean overlayNotGranted = !FloatWindowManager.getInstance().checkPermission(HSApplication.getContext());
//        if (!DefaultPhoneUtils.isDefaultPhone()
//                && (notificationNotGranted || overlayNotGranted)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
            mAdapter.setHeaderTipVisible(true);
        } else {
            mAdapter.setHeaderTipVisible(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    private void requestRewardAd(final String themeName) {
        if (mRewardVideoView == null) {
            mRewardVideoView = new RewardVideoView((ViewGroup) findViewById(R.id.drawer_layout), new RewardVideoView.OnRewarded() {
                @Override
                public void onRewarded() {
                    HSBundle bundle = new HSBundle();
                    if (mAdapter.getUnLockThemeId() != -1) {
                        bundle.putInt(ThemePreviewActivity.NOTIFY_THEME_KEY, mAdapter.getUnLockThemeId());
                    }
                    HSGlobalNotificationCenter.sendNotification(NOTIFICATION_ON_REWARDED, bundle);
                    LauncherAnalytics.logEvent("Colorphone_Theme_Unlock_Success", "from", "list", "themeName", themeName);
                }

                @Override
                public void onAdClose() {

                }

                @Override
                public void onAdCloseAndRewarded() {

                }

                @Override
                public void onAdShow() {

                    //todo theme name needs to be recorded
                    LauncherAnalytics.logEvent("Colorphone_Rewardvideo_show", "from", "list", "themeName", themeName);
                }

                @Override
                public void onAdFailed() {

                }
            }, false);
        }
        mRewardVideoView.onRequestRewardVideo();
    }


    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s)) {
            mSettingsPage.onThemeSelected();
        } else if (NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME.equals(s)) {
            HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "NOTIFICATION_REFRESH_MAIN_FRAME notifyDataSetChanged");
            initData();
            mAdapter.notifyDataSetChanged();
        } else if (HSNotificationConstant.HS_SESSION_START.equals(s)) {
            ChargingPreferenceUtil.setChargingModulePreferenceEnabled(SmartChargingSettings.isChargingScreenEnabled());
            ChargingPreferenceUtil.setChargingReportSettingEnabled(SmartChargingSettings.isChargingReportEnabled());
            ColorPhoneApplication.checkChargingReportAdPlacement();
            if (mAdapter != null) {
                mAdapter.resetShownCount();
            }
        } else if (PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED.equals(s)
                || PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED.equals(s)) {
            boolean visible = mAdapter.isTipHeaderVisible();
            updatePermissionHeader();
            if (visible != mAdapter.isTipHeaderVisible()) {
                HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "PERMISSION_GRANTED notifyDataSetChanged");
                mAdapter.notifyDataSetChanged();
            }
        }
    }


}

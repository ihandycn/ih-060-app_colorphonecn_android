package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

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
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.RewardVideoView;
import com.honeycomb.colorphone.weather.WeatherContentUtils;
import com.honeycomb.colorphone.weather.WeatherPushManager;
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
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libweather.WeatherClockManager;
import colorphone.acb.com.libweather.view.WeatherView;
import hugo.weaving.DebugLog;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, INotificationObserver {

    public static final String NOTIFICATION_ON_REWARDED = "notification_on_rewarded";

    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_SCROLL_TO_BOTTOM = "prefs_main_scroll_to_bottom";

    private static final int WELCOME_REQUEST_CODE = 2;
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 3;

    public static void startWeatherPage(Context context) {
        Intent intent = new Intent(context, ColorPhoneActivity.class);
        intent.putExtra("switch_weather", true);
        Navigations.startActivitySafely(context, intent);
    }
    boolean mWeatherMode;
    private RecyclerView mRecyclerView;
    private ThemeSelectorAdapter mAdapter;
    private final ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();
    private final ThemeList mThemeList = new ThemeList();
    private RewardVideoView mRewardVideoView;

    private Handler mHandler = new Handler();

    private boolean mIsHandsDown = false;
    private boolean mIsFirstScrollThisTimeHandsDown = true;
    public static final int SCROLL_STATE_DRAGGING = 1;

    private Toolbar toolbar;

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
    private boolean isPaused = false;
    private SettingsPage mSettingsPage = new SettingsPage();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

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
        mWeatherMode = getIntent().getBooleanExtra("switch_weather", false);

        setContentView(R.layout.activity_main);
        initMainFrame();
        AdManager.getInstance().preload();
        AppflyerLogger.logAppOpen();
        WeatherPushManager.getInstance().updateWeatherIfNeeded();
        isCreate = true;
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

            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    boolean heightChanged = WeatherContentUtils.saveWeatherTransHeight(ColorPhoneActivity.this, toolbar.getHeight());
                    if (heightChanged) {
                        weatherView.setWeatherHeight(WeatherContentUtils.getWeatherWindowHeight());
                    }
                }
            });
        }
    }

    public void showRewardVideoView(String themeName) {
        requestRewardAd(themeName);
    }

    @DebugLog
    @NonNull
    private DrawerLayout initDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        logOpenEvent = true;
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Utils.applyFontForToolbarTitle(this, toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                LauncherAnalytics.logEvent("Colorphone_Settings_Boost_Icon_Shown");
                LauncherAnalytics.logEvent("Colorphone_Sidebar_Shown");
            }
        };
        DrawerArrowDrawable arrowDrawable = mDrawerToggle.getDrawerArrowDrawable();
        arrowDrawable.getPaint().setStrokeCap(Paint.Cap.ROUND);
        arrowDrawable.getPaint().setStrokeJoin(Paint.Join.ROUND);
        arrowDrawable.setBarThickness(arrowDrawable.getBarThickness() * 1.5f);
        arrowDrawable.setBarLength(arrowDrawable.getBarLength() * 0.86f);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        View leftDrawer = findViewById(R.id.left_drawer);
        leftDrawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mSettingsPage.initPage(leftDrawer);

        return mDrawerLayout;
    }

    @DebugLog
    private void initMainFrame() {
        initDrawer();
        initData();
        initRecyclerView();
        initWeather();
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME, this);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(WeatherClockManager.NOTIFICATION_WEATHER_CONDITION_CHANGED, this);

        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));

        ConfigChangeManager.getInstance().registerCallbacks(
                ConfigChangeManager.AUTOPILOT | ConfigChangeManager.REMOTE_CONFIG, configChangeCallback);

    }

    private ViewGroup main_container;
    private WeatherView weatherView;
    private ImageButton weatherIcon;
    private void initWeather() {
        main_container = findViewById(R.id.main_container_framelayout);
        int weatherHeight = WeatherContentUtils.getWeatherWindowHeight();
        if (weatherView == null) {
            HSLog.d("Weather", "content height = " + weatherHeight);
            weatherView = new WeatherView(ColorPhoneActivity.this);
            FrameLayout.LayoutParams weatherParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            main_container.addView(weatherView, 0, weatherParams);
            weatherIcon = findViewById(R.id.toolbar_weather_icon);
            weatherView.setOuterMainLayout(mDrawerLayout);
            weatherView.setOuterIconView(weatherIcon);
            weatherView.setWeatherHeight(weatherHeight);
            View drawerDelegateToggleView = findViewById(R.id.toolbar_delegate_toggle_button);
            drawerDelegateToggleView.setVisibility(View.GONE);
            weatherView.setOnWeatherVisibleListener(new WeatherView.OnWeatherVisibleListener() {
                @Override
                public void onVisibleChange(boolean visible) {
                    drawerDelegateToggleView.setVisibility(visible ? View.VISIBLE : View.GONE);
                }
            });
            drawerDelegateToggleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    weatherView.toggle();
                }
            });

        }
        weatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weatherView.toggle();
            }
        });

        updateWeatherIcon();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            final int statusBarInset = Dimensions.getStatusBarInset(this);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) weatherIcon.getLayoutParams();
            params.topMargin += statusBarInset;
            weatherIcon.requestLayout();

            ViewGroup.MarginLayoutParams drawParams = (ViewGroup.MarginLayoutParams) mDrawerLayout.getLayoutParams();
            drawParams.topMargin += statusBarInset;
            mDrawerLayout.requestLayout();

        }

        if (mWeatherMode) {
            weatherView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    weatherView.getViewTreeObserver().addOnGlobalLayoutListener(this);
                    weatherView.toggleImmediately();
                    HSLog.d("Weather.Page", "Show");
                }
            });
        }
    }

    private void updateWeatherIcon() {
        if (WeatherClockManager.getInstance().isWeatherUnknown()) {
            weatherIcon.setVisibility(View.GONE);
        } else {
            weatherIcon.setVisibility(View.VISIBLE);
            int weatherIconId = WeatherClockManager.getInstance().getWeatherConditionIconResourceID();
            weatherIcon.setImageResource(weatherIconId);
        }
    }

    private void setFullScreen(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void quitFullScreen(){
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attrs);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public  int getStatusBarHeight(){
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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

        List<String> granted = new ArrayList();
        List<String> denied = new ArrayList();

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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mRewardVideoView != null && mRewardVideoView.isLoading()) {
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
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
        } else if (WeatherClockManager.NOTIFICATION_WEATHER_CONDITION_CHANGED.equals(s)) {
            updateWeatherIcon();
        }
    }


}

package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
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
import com.honeycomb.colorphone.boost.BoostStarterActivity;
import com.honeycomb.colorphone.cmgame.CmGameUtil;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.dialer.guide.GuideSetDefaultActivity;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.menu.SettingsPage;
import com.honeycomb.colorphone.news.NewsFrame;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.RewardVideoView;
import com.honeycomb.colorphone.view.ViewPagerFixed;
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
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String NOTIFICATION_ON_REWARDED = "notification_on_rewarded";

    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_SCROLL_TO_BOTTOM = "prefs_main_scroll_to_bottom";

    private static final int WELCOME_REQUEST_CODE = 2;
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 3;

    private RecyclerView mRecyclerView;
    private ThemeSelectorAdapter mAdapter;
    private final ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();
    private final ThemeList mThemeList = new ThemeList();
    private RewardVideoView mRewardVideoView;

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
                BoostStarterActivity.createShortCut(ColorPhoneActivity.this);

                GuideSetDefaultActivity.start(ColorPhoneActivity.this, true);
            }
        }
    };

    private ConfigChangeManager.Callback configChangeCallback = new ConfigChangeManager.Callback() {
        @Override
        public void onChange(int type) {

        }
    };

    private boolean logOpenEvent;
    private boolean pendingShowRateAlert = false;
    private boolean showAllFeatureGuide = false;
    private boolean isCreate = false;
    private SettingsPage mSettingsPage = new SettingsPage();
    private NewsFrame newsLayout;

    private static final int TAB_SIZE = 3;
    private static final int MAIN_POSITION = 0;
    private static final int NEWS_POSITION = 1;
    private static final int SETTING_POSITION = 2;

    private ViewPagerFixed mViewPager;
    private MainTabAdapter mTabAdapter;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TabTransController tabTransController;
    private View gameIcon;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContactManager.getInstance().update();
        AcbAds.getInstance().setActivity(this);
        if (NotificationUtils.isShowNotificationGuideAlertInFirstSession(this)) {
            Intent intent = new Intent(this, NotificationAccessGuideAlertActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, true);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_APP_IS_FIRST_SESSION, true);
            startActivity(intent);
            HSAlertMgr.delayRateAlert();
            HSPreferenceHelper.getDefault().putBoolean(NotificationUtils.PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, true);
        }

        setContentView(R.layout.activity_main);
        initMainFrame();
        AdManager.getInstance().preload(this);
        AppflyerLogger.logAppOpen();
        isCreate = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mSettingsPage.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (mAdapter != null && mAdapter.isTipHeaderVisible() &&
                    !PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
                HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "setHeaderTipVisible, " +
                        "notifyDataSetChanged");
                mAdapter.setHeaderTipVisible(false);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void showRewardVideoView(String themeName) {

    }

    @DebugLog
    private void initMainFrame() {

        toolbar = findViewById(R.id.toolbar);
        gameIcon = findViewById(R.id.iv_game);
        boolean gameMainEntranceEnabled = CmGameUtil.canUseCmGame()
                && HSConfig.optBoolean(false, "Application", "GameCenter", "MainViewEnable");
        if (gameMainEntranceEnabled) {
            Analytics.logEvent("MainView_GameCenter_Shown");
        }
        gameIcon.setVisibility(gameMainEntranceEnabled ? View.VISIBLE : View.GONE);
        gameIcon.setOnClickListener(this);

        logOpenEvent = true;
        Utils.configActivityStatusBar(this, toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        mViewPager = findViewById(R.id.viewpager);

        int tabPos = Preferences.get(Constants.PREF_FILE_DEFAULT).getInt(Constants.KEY_TAB_POSITION, 0);
        mTabAdapter = new MainTabAdapter();
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCanScroll(false);
        initTab();
        mViewPager.setCurrentItem(tabPos, false);

        initData();
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME, this);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED, this);
        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));

        ConfigChangeManager.getInstance().registerCallbacks(
                ConfigChangeManager.AUTOPILOT | ConfigChangeManager.REMOTE_CONFIG, configChangeCallback);

    }

    private String[] titles = new String[] {"首页", "资讯", "设置"};
    private int[] drawableIds = new int[] {
            R.drawable.seletor_tab_main,
            R.drawable.seletor_tab_news,
            R.drawable.seletor_tab_settings
    };

    private void initTab() {
        tabLayout = findViewById(R.id.tab_layout);
        tabTransController = new TabTransController(tabLayout);
        for (int i = 0; i < titles.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            View view = getLayoutInflater().inflate(R.layout.tab_item_layout, null, false);
            TextView textView = view.findViewById(R.id.tab_layout_title);
            textView.setText(titles[i]);
            Drawable icon = ResourcesCompat.getDrawable(getResources(),drawableIds[i], null);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);

            tab.setCustomView(view);
            tabLayout.addTab(tab);
        }

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            int lastPosition = -1;
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                Preferences.get(Constants.PREF_FILE_DEFAULT).putInt(Constants.KEY_TAB_POSITION, pos);

                if (mViewPager != null) {
                    mViewPager.setCurrentItem(pos, false);
                }
                updateTitle(pos);
                if (pos == NEWS_POSITION) {
                    toolbar.setBackgroundColor(Color.WHITE);
                    toolbar.setTitleTextColor(Color.BLACK);
                    ActivityUtils.setCustomColorStatusBar(ColorPhoneActivity.this, Color.WHITE);
                    NewsManager.logNewsListShow("othertab");

                    if (needUpdateNews()) {
                        if (newsLayout != null) {
                            newsLayout.refreshNews("");
                        }
                    }
                    if (newsLayout != null) {
                        newsLayout.onSelected(true);
                    }

                    View view = tab.getCustomView();
                    if (view != null) {
                        view.findViewById(R.id.tab_layout_hint).setVisibility(View.GONE);
                    }
                    updateTabStyle(true);

                } else {
                    if (lastPosition == NEWS_POSITION || lastPosition == -1) {
                        toolbar.setBackgroundColor(Color.BLACK);
                        toolbar.setTitleTextColor(Color.WHITE);
                        ActivityUtils.setCustomColorStatusBar(ColorPhoneActivity.this, Color.BLACK);
                        updateTabStyle(false);
                    }
                    if (newsLayout != null) {
                        newsLayout.onSelected(false);
                    }
                }

                gameIcon.setVisibility(pos == MAIN_POSITION ? View.VISIBLE : View.INVISIBLE);

                switch (pos) {
                    case MAIN_POSITION:
                        Analytics.logEvent("Tab_Themes_Show");
                        break;
                    case NEWS_POSITION:
                        Analytics.logEvent("Tab_News_Show");
                        break;
                    case SETTING_POSITION:
                        Analytics.logEvent("Tab_Settings_Show");
                        break;
                    default:
                        break;
                }

                lastPosition = pos;

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == NEWS_POSITION) {
                    Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(Constants.KEY_TAB_LEAVE_NEWS, System.currentTimeMillis());
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == NEWS_POSITION) {
                    if (newsLayout != null) {
                        newsLayout.refreshNews("Tab");
                    }
                }
            }
        });

    }

    private void updateTabStyle(boolean reverseColor) {
        int colorRes = reverseColor ? R.color.colorPrimaryReverse : R.color.colorPrimary;
        int tabBgRes = reverseColor ? R.drawable.tab_background_reverse : R.drawable.tab_background;
        tabLayout.setBackgroundColor(getResources().getColor(colorRes));
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View customView = tabLayout.getTabAt(i).getCustomView();
            if (customView != null) {
                if (i == NEWS_POSITION) {
                    // Change TextColor
                    TextView textView = (TextView) customView.findViewById(R.id.tab_layout_title);
                    if (reverseColor) {
                        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_90_transparent, null));
                    } else {
                        textView.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.seletor_color_tab_txt, null));
                    }
                }
                View parent = (View) customView.getParent();
                parent.setBackgroundResource(tabBgRes);
            }
        }
    }

    private void updateTitle(int pos) {
        if (pos == 0) {
            toolbar.setTitle(getTitle());
        } else {
            toolbar.setTitle(titles[pos]);
        }
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
                Analytics.logEvent("List_Page_Notification_Alert_Show");
            }
        }
        AcbRewardAdManager.preload(1, AdPlacements.AD_REWARD_VIDEO);
        if (!showAllFeatureGuide) {
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
        isPaused = false;
        mHandler.postDelayed(mainViewRunnable, 1000);

        if (tabLayout != null) {
            updateTitle(tabLayout.getSelectedTabPosition());
        }

        if (tabTransController != null) {
            tabTransController.showNow();
        }

        if (mAdapter != null) {
            HSLog.d("ColorPhoneActivity", "onResume " + mAdapter.getLastSelectedLayoutPos() + "");
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
            if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
                ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).startAnimation();
            }
            mAdapter.updateApplyInformationAutoPilotValue();
            mAdapter.markForeground(true);
        }

        if (needUpdateNews()) {
            showNewsHint();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPaused = true;
        mHandler.removeCallbacks(mainViewRunnable);

        if (mAdapter != null) {
            HSLog.d("ColorPhoneActivity", "onPause" + mAdapter.getLastSelectedLayoutPos() + "");
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
            if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
                ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).stopAnimation();
            }
            mAdapter.markForeground(false);
            mRecyclerView.getRecycledViewPool().clear();
        }
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
                "permission_launch", HSConfig.optInteger(2, "Application", "GrantAccess", "MaxCount"));
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
            Analytics.logEvent("Permission_Phone_View_Showed");
        }
        if (!contactPerm) {
            Analytics.logEvent("Permission_Contact_View_Showed");
        }
        if (!phonePerm || !contactPerm) {
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

        for (int i = 0; i < permissions.length; ++i) {
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
                Analytics.logEvent("Permission_Phone_Allow_Success");
            }
            if (list.contains(Manifest.permission.READ_CONTACTS)) {
                Analytics.logEvent("Permission_Contact_Allow_Success");
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

        if (tabTransController != null) {
            tabTransController.release();
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

    private void initData() {
        mThemeList.fillData(mRecyclerViewData);
    }

    private void initRecyclerView(RecyclerView frame) {
        mRecyclerView = frame;
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
                    Analytics.logEvent("ColorPhone_List_Bottom_Show");
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
            case R.id.iv_game:
                Analytics.logEvent("MainView_GameCenter_Clicked");
                CmGameUtil.startCmGameActivity(this, "MainIcon");
                break;
            default:
                break;
        }
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

    public boolean isNewsTab() {
        return tabLayout.getSelectedTabPosition() == NEWS_POSITION;
    }

    private boolean needUpdateNews() {
        return System.currentTimeMillis()
                - Preferences.get(Constants.PREF_FILE_DEFAULT).getLong(Constants.KEY_TAB_LEAVE_NEWS, 0)
                > 30 * DateUtils.SECOND_IN_MILLIS;
    }

    private void showNewsHint() {
        if (tabLayout.getSelectedTabPosition() != NEWS_POSITION) {
            View view = tabLayout.getTabAt(NEWS_POSITION).getCustomView();
            if (view != null) {
                view.findViewById(R.id.tab_layout_hint).setVisibility(View.VISIBLE);
            }
        }
    }

    private List<View> mTabContentLayoutList = new ArrayList<>();
    private class MainTabAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return TAB_SIZE;
        }

        @DebugLog
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            HSLog.d("MainTabAdapter", "TabAdapter");
            View frame = null;
            switch (position) {
                case MAIN_POSITION:
                    frame = getLayoutInflater().inflate(R.layout.main_frame_content, container, false);
                    initRecyclerView((RecyclerView) frame);
                    break;

                case SETTING_POSITION:
                    frame = getLayoutInflater().inflate(R.layout.layout_settings, container, false);
                    mSettingsPage.initPage(frame);
                    break;

                case NEWS_POSITION:
                    frame = getLayoutInflater().inflate(R.layout.news_frame, container, false);
                    newsLayout = (NewsFrame) frame;

                    break;
                default:
                    throw new IllegalStateException("Pager index out of bounds");
            }
            container.addView(frame);
            frame.setTag(position);
            mTabContentLayoutList.add(frame);

            return frame;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mTabContentLayoutList.remove(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    }

    private static class TabTransController implements INotificationObserver {
        private static final int TRANS_TRIGGER_Y = Dimensions.pxFromDp(24);
        private View mTab;
        int distance = 0;
        int totalDraggingDy = 0;
        boolean upScrolled = false;
        TabTransController(View tabView) {
            mTab = tabView;
            HSGlobalNotificationCenter.addObserver(Constants.NOTIFY_KEY_LIST_SCROLLED,this);
            HSGlobalNotificationCenter.addObserver(Constants.NOTIFY_KEY_LIST_SCROLLED_TOP,this);
        }

        private void onInnerListScrollChange(int state, int dy) {
            if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
                // Direction changed.
                if (totalDraggingDy * dy < 0) {
                    totalDraggingDy = 0;
                }

                totalDraggingDy += dy;
                if (Math.abs(totalDraggingDy) >= TRANS_TRIGGER_Y) {
                    boolean upScroll = totalDraggingDy > 0;
                    boolean changed = upScrolled != upScroll;
                    if (changed) {
                        upScrolled = upScroll;
                        updateTabVisibility(upScroll);
                    }
                }
            } else {
                totalDraggingDy = 0;
            }
        }

        private void updateTabVisibility(boolean upScroll) {
            if (upScroll) {
                hide();
            } else {
                show();
            }
        }

        private void hide() {
            mTab.animate().translationY(mTab.getHeight()).setDuration(200).start();
        }

        private void showNow() {
            mTab.setTranslationY(0);
        }

        private void show() {
            if (mTab.getTranslationY() == 0) {
                return;
            }
            mTab.animate().translationY(0).setDuration(200).start();
        }

        private void release() {
            HSGlobalNotificationCenter.removeObserver(this);
        }

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (Constants.NOTIFY_KEY_LIST_SCROLLED.equals(s)) {
                if (hsBundle != null) {
                    int state = hsBundle.getInt("state", RecyclerView.SCROLL_STATE_IDLE);
                    int dy = hsBundle.getInt("dy", 0);
                    onInnerListScrollChange(state, dy);
                }
            } else if (Constants.NOTIFY_KEY_LIST_SCROLLED_TOP.equals(s)) {
                HSLog.d("TabTransController", "Scrolled to Top!");
                show();
            }
        }
    }


}

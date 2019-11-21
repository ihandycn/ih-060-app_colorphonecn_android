package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.VideoManager;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.acb.cashcenter.HSCashCenterManager;
import com.acb.cashcenter.OnIconClickListener;
import com.acb.cashcenter.lottery.LotteryWheelLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.ringtones.view.RingtonePageView;
import com.honeycomb.colorphone.AppflyerLogger;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigChangeManager;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.boost.BoostStarterActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.debug.DebugActions;
import com.honeycomb.colorphone.dialer.guide.GuideSetDefaultActivity;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.bean.LoginUserBean;
import com.honeycomb.colorphone.menu.SettingsPage;
import com.honeycomb.colorphone.menu.TabItem;
import com.honeycomb.colorphone.news.NewsFrame;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.theme.ThemeApplyManager;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.theme.ThemeUpdateListener;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.uploadview.ClassicHeader;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.MediaSharedElementCallback;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.HomePageRefreshFooter;
import com.honeycomb.colorphone.view.MainTabLayout;
import com.honeycomb.colorphone.view.TabFrameLayout;
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
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libweather.debug.DebugConfig;
import hugo.weaving.DebugLog;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String NOTIFICATION_ON_REWARDED = "notification_on_rewarded";

    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_SCROLL_TO_BOTTOM = "prefs_main_scroll_to_bottom";
    private static final String PREFS_CASH_CENTER_SHOW = "prefs_cash_center_show";
    private static final String PREFS_CASH_CENTER_GUIDE_SHOW = "prefs_cash_center_guide_show";
    private static final String PREFS_RINGTONE_SHOW = "prefs_ringtone_frame_show";
    private static final String PREFS_SET_DEFAULT_THEME = "prefs_set_default_theme";

    private static final int WELCOME_REQUEST_CODE = 2;
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 3;

    private RelativeLayout mMainPage;
    private SmartRefreshLayout mSmartRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mMainNetWorkErrView;
    private ThemeSelectorAdapter mAdapter;
    private final ArrayList<Theme> mRecyclerViewData = new ArrayList<>();
//    private RewardVideoView mRewardVideoView;

    private boolean isPaused;
    private boolean isWindowFocus;

    private Handler mHandler = new Handler();

    private boolean mIsHandsDown = false;
    private boolean mIsFirstScrollThisTimeHandsDown = true;
    public static final int SCROLL_STATE_DRAGGING = 1;
    private boolean isDoubleClickToolbar = false;
    private boolean isFirstRequestData = true;
    private boolean isAlreadyDownloadFirstTheme = false;

    private TasksManagerModel model;
    private TasksManagerModel ringtoneModel;

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
            if (mMainViewShowFlag) {
                mMainViewShowFlag = false;
                ColorPhoneApplication.getConfigLog().getEvent().onMainViewOpen();
                BoostStarterActivity.createShortCut(ColorPhoneActivity.this);

                GuideSetDefaultActivity.start(ColorPhoneActivity.this, true);

                HSGlobalNotificationCenter.sendNotificationOnMainThread(Constants.NOTIFY_KEY_APP_FULLY_DISPLAY);

                if (HSConfig.optBoolean(true, "Application", "Ringtone", "Enable")) {
                    guideLottie = findViewById(R.id.lottie_guide);
                    guideLottie.setOnClickListener(view -> mTabFrameLayout.setCurrentItem(getTabPos(TabItem.TAB_RINGTONE)));
                    guideLottie.playAnimation();

                    if (mTabLayout.getSelectedTabPosition() == getTabPos(TabItem.TAB_MAIN)) {
                        guideLottie.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    /**
     * For activity transition
     */
    private MediaSharedElementCallback sharedElementCallback;

    private Runnable cashCenterGuideRunnable = new Runnable() {
        @Override
        public void run() {
            if (showTabCashCenter && !Preferences.getDefault().getBoolean(PREFS_CASH_CENTER_SHOW, false)) {
                tabCashCenterGuide.setVisibility(View.VISIBLE);
                tabCashCenterGuide.useHardwareAcceleration();
                tabCashCenterGuide.playAnimation();

                Preferences.getDefault().putBoolean(PREFS_CASH_CENTER_GUIDE_SHOW, true);
                Analytics.logEvent("Tab_CashCenter_Guide_Show");

                tabTransController.setInterceptView(tabCashCenterGuide);
                tabTransController.showNow();
            }
        }
    };

    private ConfigChangeManager.Callback configChangeCallback = new ConfigChangeManager.Callback() {
        @Override
        public void onChange(int type) {

        }
    };

    private boolean mMainViewShowFlag;
    private boolean showAllFeatureGuide = false;
    private boolean isCreate = false;
    private SettingsPage mSettingsPage = new SettingsPage();
    private NewsFrame newsLayout;
    private RingtonePageView mRingtoneFrame;
    private LotteryWheelLayout lotteryWheelLayout;

//    private static final int TAB_SIZE = 4;
//    private static final int MAIN_POSITION = 0;
//    // Disable news
//    @Deprecated
//    private static final int NEWS_POSITION = -100;
//    private static final int RINGTONE_POSITION = 1;
//
//    public static final int CASH_POSITION = 2;
//    private static final int SETTING_POSITION = 3;

    private TabFrameLayout mTabFrameLayout;
    private Toolbar toolbar;
    private MainTabLayout mTabLayout;
    private LottieAnimationView tabCashCenterGuide;
    private boolean showTabCashCenter = false;
    private TabTransController tabTransController;
    private LottieAnimationView guideLottie;

    private DoubleBackHandler mDoubleBackHandler = new DoubleBackHandler();

    public static void startColorPhone(Context context, String initTabId) {
        Intent intent = new Intent(context, ColorPhoneActivity.class);
        intent.putExtra(Constants.INTENT_KEY_TAB_POSITION, initTabId);
        Navigations.startActivitySafely(context, intent);
    }

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

        Utils.setupTransparentStatusBarsForLmp(this);

        initMainFrame();
        AdManager.getInstance().preload(this);
        AppflyerLogger.logAppOpen();
        isCreate = true;
        // Transition
        sharedElementCallback = new MediaSharedElementCallback();
        sharedElementCallback.setClearAfterConsume(true);
        ActivityCompat.setExitSharedElementCallback(this, sharedElementCallback);
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
        isWindowFocus = true;

        if (lotteryWheelLayout != null) {
            HSLog.i("CashCenter", "lotteryWheelLayout != null");
            lotteryWheelLayout.onWindowFocusChanged(true);
            if (lotteryWheelLayout.getLotterySpinView() != null) {
                lotteryWheelLayout.getLotterySpinView().onWindowFocusChanged(true);
            }
        } else {
            HSLog.i("CashCenter", "not onWindowFocusChanged");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (DebugConfig.OVERRIDE_VOLUME_KEYS) {
                        onDebugAction(true);
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (DebugConfig.OVERRIDE_VOLUME_KEYS) {
                        onDebugAction(false);
                        return true;
                    }
                    break;
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void onDebugAction(boolean volumeDown) {
        // Notice: DO NOT modify this method. Modify inside DebugActions#onDebugAction().
        if (volumeDown) {
            DebugActions.onVolumeDown(this);
        } else {
            DebugActions.onVolumeUp(this);
        }
    }

    public void showRewardVideoView(String themeName) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int tabPos = -1;

        if (intent != null) {
            String tabId = intent.getStringExtra(Constants.INTENT_KEY_TAB_POSITION);
            tabPos = getTabPos(tabId);
        }

        if (tabPos == -1) {
            tabPos = Preferences.get(Constants.PREF_FILE_DEFAULT).getInt(Constants.KEY_TAB_POSITION, 0);
        }

        if (mTabFrameLayout != null) {
            mTabFrameLayout.setCurrentItem(tabPos);
        }
    }

    @DebugLog
    private void initMainFrame() {

        toolbar = findViewById(R.id.toolbar);
        toolbar.setOnClickListener(v -> {
            if (!isDoubleClickToolbar) {
                isDoubleClickToolbar = true;
                mHandler.postDelayed(() -> isDoubleClickToolbar = false, 500);
            } else {
                if (mRecyclerView != null) {
                    mRecyclerView.scrollToPosition(0);
                    tabTransController.upScrolled = false;
                }
            }
        });

        mMainViewShowFlag = true;
        Utils.configActivityStatusBar(this, toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        initTab();
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, this);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_UPLOAD_SELECT, this);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_PUBLISH_SELECT, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_PREVIEW_POSITION, this);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(HttpManager.NOTIFY_REFRESH_USER_INFO, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_UPDATE_THEME_IN_MAIN_FRAME, this);

        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));

        ConfigChangeManager.getInstance().registerCallbacks(
                ConfigChangeManager.AUTOPILOT | ConfigChangeManager.REMOTE_CONFIG, configChangeCallback);

    }
//
//    private String[] titles = new String[] {"首页", "资讯", "赚现金", "设置"};
//    private int[] drawableIds = new int[] {
//            R.drawable.seletor_tab_main,
//            R.drawable.seletor_tab_news,
//            R.drawable.seletor_tab_cash_center,
//            R.drawable.seletor_tab_settings
//    };

    private List<TabItem> mTabItems = new ArrayList<>();

    private void initTab() {
        mTabItems.add(new TabItem(TabItem.TAB_MAIN,
                R.drawable.seletor_tab_main, "首页", true));

//        TabItem tabItemNews = new TabItem(TabItem.TAB_NEWS,
//                R.drawable.seletor_tab_news, "资讯", true);
//        // Coo
//        tabItemNews.setColorReversed(true);
//        mTabItems.add(tabItemNews);

        if (HSConfig.optBoolean(true, "Application", "Ringtone", "Enable")) {
            mTabItems.add(new TabItem(TabItem.TAB_RINGTONE,
                    R.drawable.seletor_tab_ringtone, "铃声", false));
        }

        mTabItems.add(new TabItem(TabItem.TAB_SETTINGS,
                R.drawable.seletor_tab_settings, "我的", true));

        mTabFrameLayout = findViewById(R.id.tab_frame_container);
        mTabFrameLayout.setTabItems(mTabItems);
        showTabCashCenter = HSConfig.optBoolean(true, "Application", "CashCenter", "Enable");

        if (showTabCashCenter) {
            int index = Math.max(1, mTabItems.size() - 1);
            mTabItems.add(index, new TabItem(TabItem.TAB_CASH,
                    R.drawable.seletor_tab_cash_center, "赚现金", false));
            CashCenterUtil.init(this);
            Analytics.logEvent("Tab_CashCenter_Icon_Show");
        }

        final int colorPrimary = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setTabBackgroundResId(R.drawable.tab_background);
        tabTransController = new TabTransController(mTabLayout);
        for (int i = 0; i < mTabItems.size(); i++) {
            TabItem tabItem = mTabItems.get(i);
            View view = getLayoutInflater().inflate(R.layout.tab_item_layout, mTabLayout, false);
            TextView textView = view.findViewById(R.id.tab_layout_title);
            textView.setText(tabItem.getTabName());
            Drawable icon = ResourcesCompat.getDrawable(getResources(), tabItem.getTabDrawable(), null);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            mTabLayout.addTab(view);
        }
        tabCashCenterGuide = findViewById(R.id.tab_cash_center_guide_four);
        tabCashCenterGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTabFrameLayout.setCurrentItem(getTabPos(TabItem.TAB_CASH));
            }
        });

        boolean needRingtoneRemind = !Preferences.getDefault().getBoolean(PREFS_RINGTONE_SHOW, false);
        if (needRingtoneRemind) {
            View ringtoneTab = mTabLayout.getTabAt(getTabPos(TabItem.TAB_RINGTONE));
            if (ringtoneTab != null) {
                View hintView = ringtoneTab.findViewById(R.id.tab_layout_hint);
                hintView.getLayoutParams().height = Dimensions.pxFromDp(6);
                hintView.getLayoutParams().width = Dimensions.pxFromDp(6);
                hintView.setVisibility(View.VISIBLE);
                hintView.setTranslationX(-Dimensions.pxFromDp(5));
                hintView.setTranslationY(Dimensions.pxFromDp(5));
                hintView.requestLayout();
            }
        }

        mTabFrameLayout.setFrameChangeListener(new TabFrameLayout.FrameChangeListener() {
            @Override
            public void onFrameChanged(int position) {
                if (mTabLayout != null && mTabLayout.getSelectedTabPosition() != position
                        && position < mTabLayout.getTabCount()) {
                    mTabLayout.setCurrentTab(position);
                }
            }
        });

        mTabFrameLayout.setFrameProvider(new TabFrameLayout.FrameProvider() {
            @Override
            public View getFrame(ViewGroup viewGroup, int pos) {
                return createFrameItem(viewGroup, pos);
            }
        });

        mTabLayout.addOnTabSelectedListener(new MainTabLayout.OnTabSelectedListener() {
            TabItem lastItem = null;

            @Override
            public void onTabSelected(int pos) {
                final TabItem tabItem = mTabItems.get(pos);
                final View tabView = mTabLayout.getTabAt(pos);

                Preferences.get(Constants.PREF_FILE_DEFAULT).putInt(Constants.KEY_TAB_POSITION, pos);
                if (mTabFrameLayout != null) {
                    mTabFrameLayout.setCurrentItem(pos);
                }
                updateTitle(pos);
                tabTransController.showNow();

                HSCashCenterManager.getInstance().setAutoFirstRewardFlag(false);

                // Hide red point for Ringtone Tab
                if (TabItem.TAB_RINGTONE.equals(tabItem.getId())) {
                    Preferences.getDefault().putBoolean(PREFS_RINGTONE_SHOW, true);
                    if (tabView != null) {
                        tabView.findViewById(R.id.tab_layout_hint).setVisibility(View.GONE);
                    }
                }

                if (tabItem.getId().equals(TabItem.TAB_NEWS)) {
                    toolbar.setVisibility(View.VISIBLE);
                    toolbar.setBackgroundColor(Color.WHITE);
                    toolbar.setTitleTextColor(colorPrimary);
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

                    if (tabView != null) {
                        tabView.findViewById(R.id.tab_layout_hint).setVisibility(View.GONE);
                    }
                    updateTabStyle(true);

                } else {
                    if (tabItem.isEnableToolBarTitle()) {
                        toolbar.setVisibility(View.VISIBLE);
                    } else {
                        toolbar.setVisibility(View.GONE);
                    }

                    // Cash tab
                    if (tabItem.getId().equals(TabItem.TAB_CASH)) {
                        Preferences.getDefault().putBoolean(PREFS_CASH_CENTER_SHOW, true);
                        tabView.findViewById(R.id.tab_layout_hint).setVisibility(View.GONE);
                        tabCashCenterGuide.setVisibility(View.GONE);

                        tabTransController.setInterceptView(null);

                        boolean show = HSCashCenterManager.getInstance().startFirstReward();
                        if (!show) {
                            HSCashCenterManager.getInstance().setAutoFirstRewardFlag(true);
                        }

                        if (newsLayout != null) {
                            newsLayout.onSelected(false);
                        }
                        ActivityUtils.setCustomColorStatusBar(ColorPhoneActivity.this, 0xffb62121);
                    } else {
                        ActivityUtils.setCustomColorStatusBar(ColorPhoneActivity.this, colorPrimary);
                    }


                    if (lastItem == null || lastItem.isColorReversed()) {
                        toolbar.setBackgroundColor(colorPrimary);
                        toolbar.setTitleTextColor(Color.WHITE);
                        updateTabStyle(false);
                    }
                    if (newsLayout != null) {
                        newsLayout.onSelected(false);
                    }
                }

                switch (tabItem.getId()) {
                    case TabItem.TAB_MAIN:
                        if (guideLottie != null) {
                            guideLottie.setVisibility(View.VISIBLE);
                        }

                        Analytics.logEvent("Tab_Themes_Show");
                        break;
                    case TabItem.TAB_NEWS:
                        Analytics.logEvent("Tab_News_Show");
                        break;
                    case TabItem.TAB_RINGTONE:
                        // TODO
//                        Analytics.logEvent("Tab_News_Show");
                        break;
                    case TabItem.TAB_SETTINGS:
                        if (guideLottie != null) {
                            guideLottie.setVisibility(View.GONE);
                        }

                        Analytics.logEvent("Tab_Settings_Show");
                        break;
                    case TabItem.TAB_CASH:
                        Analytics.logEvent("CashCenter_Wheel_Shown", "type", "Click");
                        if (lotteryWheelLayout != null) {
                            lotteryWheelLayout.refreshData();
                        }
                        break;
                    default:
                        break;
                }

                lastItem = tabItem;

            }

            @Override
            public void onTabUnselected(int pos) {
                if (TabItem.TAB_NEWS.equals(mTabItems.get(pos).getId())) {
                    Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(Constants.KEY_TAB_LEAVE_NEWS, System.currentTimeMillis());
                }

            }

            @Override
            public void onTabReselected(int pos) {

                String tabId = mTabItems.get(pos).getId();
                if (TabItem.TAB_NEWS.equals(tabId)) {
                    if (newsLayout != null) {
                        newsLayout.refreshNews("Tab");
                    }
                } else if (TabItem.TAB_MAIN.equals(tabId)) {
                    if (mRecyclerView != null) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        });

        setTabInitPosition();
    }

    private int getTabPos(String tabId) {
        for (int i = 0; i < mTabItems.size(); i++) {
            TabItem item = mTabItems.get(i);
            if (TextUtils.equals(item.getId(), tabId)) {
                return i;
            }
        }
        return 0;
    }

    private void setTabInitPosition() {
        int tabPos = getIntent().getIntExtra(Constants.INTENT_KEY_TAB_POSITION, -1);
        if (tabPos == -1) {
            tabPos = Preferences.get(Constants.PREF_FILE_DEFAULT).getInt(Constants.KEY_TAB_POSITION, 0);
        }

        mTabFrameLayout.setCurrentItem(tabPos);
    }

    private void updateTabStyle(boolean reverseColor) {
        int colorRes = reverseColor ? R.color.colorPrimaryReverse : R.color.colorPrimary;
        int tabBgRes = reverseColor ? R.drawable.tab_background_reverse : R.drawable.tab_background;
        mTabLayout.setBackgroundColor(ResourcesCompat.getColor(getResources(), colorRes, null));
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            View customView = mTabLayout.getTabAt(i);
            TabItem tabItem = mTabItems.get(i);
            if (customView != null) {
                if (TabItem.TAB_NEWS.equals(tabItem.getId())) {
                    // Change TextColor
                    TextView textView = (TextView) customView.findViewById(R.id.tab_layout_title);
                    if (reverseColor) {
                        textView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black_90_transparent, null));
                    } else {
                        textView.setTextColor(ResourcesCompat.getColorStateList(getResources(), R.color.seletor_color_tab_txt, null));
                    }
                }
                customView.setBackgroundResource(tabBgRes);
            }
        }
    }

    private void updateTitle(int pos) {
        if (pos == 0) {
            toolbar.setTitle(getTitle());
        } else {
            toolbar.setTitle(mTabItems.get(pos).getTabName());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        tabTransController.upScrolled = false;

        // log event
        long sessionPast = System.currentTimeMillis() - SessionMgr.getInstance().getCurrentSessionStartTime();
        boolean isNearSession = Math.abs(sessionPast) < 2000;
        if (isNearSession) {
            if (mAdapter != null && mAdapter.isTipHeaderVisible()) {
                Analytics.logEvent("List_Page_Permission_Alert_Show");
            }
        }
        AcbRewardAdManager.getInstance().preload(1, Placements.AD_REWARD_VIDEO);
        if (!showAllFeatureGuide) {
//            dispatchPermissionRequest();
        }
        if (!showAllFeatureGuide) {
            isCreate = false;
        }
        showAllFeatureGuide = false;

        if (mRingtoneFrame != null) {
            mRingtoneFrame.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // clear previous observers.
        PermissionHelper.stopObservingPermission();
        VideoManager.get().mute(true);
        isPaused = false;
        mHandler.postDelayed(mainViewRunnable, 1000);

        if (mTabLayout != null) {
            updateTitle(mTabLayout.getSelectedTabPosition());
        }

        if (tabTransController != null) {
            tabTransController.show();
        }

        if (mAdapter != null) {
            HSLog.d("ColorPhoneActivity", "onResume " + mAdapter.getLastSelectedLayoutPos() + "");
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
            if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
                ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).startAnimation();
            }

        }

        if (showTabCashCenter
                && !Preferences.getDefault().getBoolean(PREFS_CASH_CENTER_GUIDE_SHOW, false)
                && !Preferences.getDefault().getBoolean(PREFS_CASH_CENTER_SHOW, false)) {
            mHandler.postDelayed(cashCenterGuideRunnable, 10 * DateUtils.SECOND_IN_MILLIS);
        }

        if (lotteryWheelLayout != null) {
            lotteryWheelLayout.onResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        isPaused = true;
        mHandler.removeCallbacks(mainViewRunnable);
        mHandler.removeCallbacks(cashCenterGuideRunnable);

        if (tabTransController != null) {
            tabTransController.hide();
        }
        if (mAdapter != null) {
            HSLog.d("ColorPhoneActivity", "onPause" + mAdapter.getLastSelectedLayoutPos() + "");
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
            if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
                ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).stopAnimation();
            }

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

    private void requestThemeData(boolean isRefresh) {

        ThemeList.getInstance().requestThemeForMainFrame(isRefresh, new ThemeUpdateListener() {
            @Override
            public void onFailure(String errorMsg) {
                if (isFirstRequestData) {
                    Analytics.logEvent("CallFlash_Request_First_Failed", "type", errorMsg);
                    if (mMainNetWorkErrView != null) {
                        mMainNetWorkErrView.setVisibility(View.VISIBLE);
                    }
                    isFirstRequestData = false;
                }
                Analytics.logEvent("CallFlash_Request_Failed", "type", errorMsg);

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.theme_page_not_network_toast, findViewById(R.id.toast_layout));
                Toast toast = new Toast(getBaseContext());
                toast.setGravity(Gravity.CENTER | Gravity.TOP, 0, Dimensions.pxFromDp(80));
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
                if (isRefresh) {
                    mSmartRefreshLayout.finishRefresh();
                } else {
                    mSmartRefreshLayout.finishLoadMore(true);
                }
            }

            @Override
            public void onSuccess(boolean isHasData) {
                if (mMainNetWorkErrView != null) {
                    mMainNetWorkErrView.setVisibility(View.GONE);
                }
                if (isFirstRequestData) {
                    Analytics.logEvent("CallFlash_Request_First_Success");
                    isFirstRequestData = false;
                }
                Analytics.logEvent("CallFlash_Request_Success");

                if (isRefresh) {
                    mSmartRefreshLayout.finishRefresh();
                    mSmartRefreshLayout.resetNoMoreData();
                } else {
                    mSmartRefreshLayout.finishLoadMore(true);
                }
                if (isHasData) {
                    refreshData();
                } else {
                    if (!isRefresh) {
                        mSmartRefreshLayout.finishLoadMoreWithNoMoreData();
                    }
                }

                //download first theme
                if (Preferences.getDefault().getBoolean(PREFS_SET_DEFAULT_THEME, true) && Theme.getFirstTheme() != null) {
                    model = TasksManager.getImpl().requestMediaTask(Theme.getFirstTheme());
                    ringtoneModel = TasksManager.getImpl().requestRingtoneTask(Theme.getFirstTheme());

                    if (model != null) {
                        TasksManager.doDownload(model, null);
                        FileDownloadMultiListener.getDefault().addStateListener(model.getId(), mDownloadStateListener);
                    }

                    if (ringtoneModel != null) {
                        TasksManager.doDownload(ringtoneModel, null);
                        FileDownloadMultiListener.getDefault().addStateListener(ringtoneModel.getId(), mRingtoneDownloadStateListener);
                    }
                    Preferences.getDefault().putBoolean(PREFS_SET_DEFAULT_THEME, false);
                }
            }
        });
    }

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            if (ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == -1 && Theme.getFirstTheme() != null) {
                Theme theme = Theme.getFirstTheme();
                ThemeApplyManager.getInstance().addAppliedTheme(theme.toPrefString());
                ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, theme.getId());
                if (mRecyclerViewData.get(0) != null && mRecyclerViewData.get(0).getId() == Theme.getFirstTheme().getId()) {
                    mRecyclerViewData.get(0).setSelected(true);
                }
                mAdapter.notifyDataSetChanged();
                isAlreadyDownloadFirstTheme = true;
            }
            FileDownloadMultiListener.getDefault().removeStateListener(model.getId());
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {

        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {

        }
    };

    DownloadStateListener mRingtoneDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            if (isAlreadyDownloadFirstTheme) {
                if (ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == -1 && Theme.getFirstTheme() != null) {
                    FileDownloadMultiListener.getDefault().removeStateListener(ringtoneModel.getId());
                    if (!Settings.System.canWrite(getBaseContext())) {
                        Toast.makeText(getBaseContext(), "设置铃声失败，请授予权限", Toast.LENGTH_LONG).show();
                        return;
                    }
                    RingtoneHelper.setDefaultRingtoneInBackground(Theme.getFirstTheme());
                }
            } else {
                if (Theme.getFirstTheme() != null && ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == Theme.getFirstTheme().getId()) {
                    FileDownloadMultiListener.getDefault().removeStateListener(ringtoneModel.getId());
                    if (!Settings.System.canWrite(getBaseContext())) {
                        Toast.makeText(getBaseContext(), "设置铃声失败，请授予权限", Toast.LENGTH_LONG).show();
                        return;
                    }
                    RingtoneHelper.setDefaultRingtoneInBackground(Theme.getFirstTheme());
                }
            }
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {
        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {
        }
    };

    private void refreshData() {
        setData();
        mAdapter.notifyDataSetChanged();

        int maxId = -1;
        for (Type type : Type.values()) {
            if (maxId < type.getId()) {
                maxId = type.getId();
            }
        }
        HSPreferenceHelper.getDefault().putInt(NotificationConstants.PREFS_NOTIFICATION_OLD_MAX_ID, maxId);
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

        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();

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

//        if (mRewardVideoView != null) {
//            mRewardVideoView.onCancel();
//        }

        if (tabTransController != null) {
            tabTransController.release();
        }
        ConfigChangeManager.getInstance().removeCallback(configChangeCallback);

        if (showTabCashCenter) {
            CashCenterUtil.cleanAds(this);
            HSCashCenterManager.getInstance().releaseWheelAds();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if ((mRingtoneFrame != null && mRingtoneFrame.onBackPressed())) {
            // Block
            return;
        }
        boolean blockBackPress = mTabLayout.getSelectedTabPosition() == getTabPos(TabItem.TAB_CASH)
                && (lotteryWheelLayout != null && lotteryWheelLayout.isSpining());

        if (!blockBackPress) {
            if (mDoubleBackHandler.interceptBackPressed()) {
                mDoubleBackHandler.toast();
            } else {
                super.onBackPressed();
                // Stop all download tasks;
                TasksManager.getImpl().stopAllTasks();
            }
        }
    }

    private void setData() {
        ThemeList.getInstance().updateThemesTotally();
        ThemeList.getInstance().fillData(mRecyclerViewData);
    }

    private void initNetworkErrorView(View rootView) {
        mMainNetWorkErrView = rootView.findViewById(R.id.frame_no_network);
        mMainNetWorkErrView.findViewById(R.id.no_network_action).setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#ff696681"),
                Dimensions.pxFromDp(22), true));
        mMainNetWorkErrView.findViewById(R.id.no_network_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSmartRefreshLayout.autoRefresh();
            }
        });
    }

    private void initRefreshView(SmartRefreshLayout smartRefreshLayout) {
        mSmartRefreshLayout = smartRefreshLayout;
        mSmartRefreshLayout.setEnableAutoLoadMore(true);
        mSmartRefreshLayout.setRefreshHeader(new ClassicHeader(this));
        mSmartRefreshLayout.setRefreshFooter(new HomePageRefreshFooter(this));
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                if (isFirstRequestData) {
                    Analytics.logEvent("CallFlash_Request_First");
                }
                Analytics.logEvent("CallFlash_Request");
                requestThemeData(true);
            }
        });
        mSmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                Analytics.logEvent("CallFlash_Request");
                requestThemeData(false);
            }
        });

        //触发自动刷新
        mSmartRefreshLayout.autoRefresh();
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
                !AutoRequestManager.getInstance().isGrantAllPermission()) {
//                PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
            mAdapter.setHeaderTipVisible(true);
        } else {
            mAdapter.setHeaderTipVisible(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s) || ThemePreviewActivity.NOTIFY_THEME_UPLOAD_SELECT.equals(s) ||
                ThemePreviewActivity.NOTIFY_THEME_PUBLISH_SELECT.equals(s)) {
            mSettingsPage.onThemeSelected();
        } else if (HSNotificationConstant.HS_SESSION_START.equals(s)) {
            ChargingPreferenceUtil.setChargingModulePreferenceEnabled(SmartChargingSettings.isChargingScreenEnabled());
            ChargingPreferenceUtil.setChargingReportSettingEnabled(SmartChargingSettings.isChargingReportEnabled());
            if (mAdapter != null) {
                mAdapter.resetShownCount();
            }
        } else if (PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED.equals(s)
                || PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED.equals(s)) {
            if (mAdapter != null) {
                boolean visible = mAdapter.isTipHeaderVisible();
                updatePermissionHeader();
                if (visible != mAdapter.isTipHeaderVisible()) {
                    HSLog.d(ThemeSelectorAdapter.class.getSimpleName(), "PERMISSION_GRANTED notifyDataSetChanged");
                    mAdapter.notifyDataSetChanged();
                }
            }
        } else if (NotificationConstants.NOTIFICATION_PREVIEW_POSITION.equals(s)) {
            if (mAdapter != null) {
                int pos = hsBundle.getInt("position");
                HSLog.d("preview pos = " + pos);
                mRecyclerView.scrollToPosition(mAdapter.themePositionToAdapterPosition(pos));
            }
        } else if (HttpManager.NOTIFY_REFRESH_USER_INFO.equals(s)) {
            if (hsBundle != null && hsBundle.getObject(HttpManager.KEY_USER_INFO) instanceof LoginUserBean.UserInfoBean) {
                mSettingsPage.showUserInfo((LoginUserBean.UserInfoBean) hsBundle.getObject(HttpManager.KEY_USER_INFO));
            } else {
                mSettingsPage.refreshUserInfo();
            }
        } else if (NotificationConstants.NOTIFICATION_UPDATE_THEME_IN_MAIN_FRAME.equals(s)) {
            refreshData();
        }
    }

    public boolean isNewsTab() {
        return mTabLayout.getSelectedTabPosition() == getTabPos(TabItem.TAB_NEWS);
    }

    @Deprecated
    private boolean needUpdateNews() {
        return false;
    }

    private View createFrameItem(ViewGroup container, int position) {
        HSLog.d("MainTabAdapter", "getItem");
        View frame = null;
        final TabItem tabItem = mTabItems.get(position);
        switch (tabItem.getId()) {
            case TabItem.TAB_MAIN:
                if (mMainPage == null) {
                    frame = getLayoutInflater().inflate(R.layout.main_frame_content, null, false);
                    mMainPage = (RelativeLayout) frame;
                    initNetworkErrorView(frame);
                    initRefreshView(frame.findViewById(R.id.refresh_layout));
                    initRecyclerView(frame.findViewById(R.id.recycler_view));
                } else {
                    frame = mMainPage;
                }
                break;

            case TabItem.TAB_SETTINGS:
                if (!mSettingsPage.isInit()) {
                    frame = getLayoutInflater().inflate(R.layout.layout_settings, null, false);
                    mSettingsPage.initPage(frame, this);
                } else {
                    frame = mSettingsPage.getRootView();
                }
                break;

            case TabItem.TAB_NEWS:
                if (newsLayout == null) {
                    newsLayout = (NewsFrame) getLayoutInflater().inflate(R.layout.news_frame, null, false);
                }
                frame = newsLayout;
                break;
            case TabItem.TAB_RINGTONE:
                if (mRingtoneFrame == null) {
                    mRingtoneFrame = new RingtonePageView(this);
                }
                frame = mRingtoneFrame;
                break;
            case TabItem.TAB_CASH:
                if (showTabCashCenter) {
                    if (lotteryWheelLayout == null) {
                        lotteryWheelLayout = (LotteryWheelLayout) getLayoutInflater().inflate(R.layout.cashcenter_layout, container, false);
                        lotteryWheelLayout.setBackToCashCenterPage(false);
                        HSLog.i("CashCenterCp", "bottom: nav == " + Dimensions.getNavigationBarHeight(getBaseContext()) + " tabH == " + mTabLayout.getHeight());

                        lotteryWheelLayout.setLeftCornerIconResource(R.drawable.cash_center_icon);
                        lotteryWheelLayout.setTvLeftCornerTextRes(R.string.cash_center);
                        lotteryWheelLayout.setIconClickListener(new OnIconClickListener() {
                            @Override
                            public void onRightCornerIcClick() {
                            }

                            @Override
                            public void onLeftCornerIcClick() {
                                if (!lotteryWheelLayout.isSpining()) {
                                    Navigations.startActivitySafely(ColorPhoneActivity.this, CashCenterActivity.class);
                                    Analytics.logEvent("CashCenter_Clicked");
                                }
                            }
                        });

                        TextView title = lotteryWheelLayout.findViewById(com.acb.cashcenter.R.id.cash_center_left_corner_text);
                        title.setVisibility(View.VISIBLE);
                        title.setText(R.string.cash_center);
                        title.setTextColor(0xffffffff);
                        title.setTextSize(14);

                        frame = lotteryWheelLayout;
                    } else {
                        frame = lotteryWheelLayout;
                    }

                    lotteryWheelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            lotteryWheelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            if (isWindowFocus) {
                                HSLog.i("CashCenter", "isWindowFocus true");
                                lotteryWheelLayout.onWindowFocusChanged(true);
                                if (lotteryWheelLayout.getLotterySpinView() != null) {
                                    lotteryWheelLayout.getLotterySpinView().onWindowFocusChanged(true);
                                }
                            } else {
                                HSLog.i("CashCenter", "not isWindowFocus");
                            }
                        }
                    });
                } else {
                    if (!mSettingsPage.isInit()) {
                        frame = getLayoutInflater().inflate(R.layout.layout_settings, null, false);
                        mSettingsPage.initPage(frame, this);
                    } else {
                        frame = mSettingsPage.getRootView();
                    }
                }
                break;
            default:
                throw new IllegalStateException("Pager index out of bounds");
        }

        frame.setTag(position);
        return frame;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            final int exitPos = data.getIntExtra("index", -1);
            if (mRecyclerView != null) {
                ActivityCompat.postponeEnterTransition(this);
                mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        overrideSharedElement(exitPos);
                        ActivityCompat.startPostponedEnterTransition(ColorPhoneActivity.this);
                        return true;
                    }
                });
            }
        }
    }

    private void overrideSharedElement(int exitPos) {
        final int adapterPosition = mAdapter.themePositionToAdapterPosition(exitPos);
        if (mRecyclerView != null) {
            RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(adapterPosition);
            if (viewHolder != null && viewHolder.itemView != null) {
                ImageView imageView = viewHolder.itemView.findViewById(R.id.card_preview_img);
                sharedElementCallback.setSharedElementViews(imageView);
            }
        }
    }

    public void setTabLayoutClickable(boolean enable) {
        mTabLayout.setEnabled(enable);
    }

    private static class TabTransController implements INotificationObserver {
        private static final int TRANS_TRIGGER_Y = Dimensions.pxFromDp(24);
        private View mTab;
        private View mInterceptView;
        int distance = 0;
        int totalDraggingDy = 0;
        boolean upScrolled = false;

        TabTransController(View tabView) {
            mTab = tabView;
            HSGlobalNotificationCenter.addObserver(Constants.NOTIFY_KEY_LIST_SCROLLED, this);
            HSGlobalNotificationCenter.addObserver(Constants.NOTIFY_KEY_LIST_SCROLLED_TOP, this);
        }

        public void setInterceptView(View view) {
            mInterceptView = view;
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
            if (mInterceptView != null) {
                if (mInterceptView.getVisibility() != View.VISIBLE) {
                    mTab.animate().translationY(mTab.getHeight()).setStartDelay(200).setDuration(200).start();
                }
            } else {
                mTab.animate().translationY(mTab.getHeight()).setStartDelay(200).setDuration(200).start();
            }
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
                    if (upScrolled || state == RecyclerView.SCROLL_STATE_IDLE) {
                        countDownTimer.start();
                    }
                }
            } else if (Constants.NOTIFY_KEY_LIST_SCROLLED_TOP.equals(s)) {
                HSLog.d("TabTransController", "Scrolled to Top!");
                show();
            }
        }

        CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (BuildConfig.DEBUG) {
                    HSLog.e("Second Remaining: " + millisUntilFinished / 1000);
                }
            }

            @Override
            public void onFinish() {
                if (BuildConfig.DEBUG) {
                    HSLog.e("Second Remaining None, Done");
                }
                show();
                upScrolled = false;
            }
        };
    }


}

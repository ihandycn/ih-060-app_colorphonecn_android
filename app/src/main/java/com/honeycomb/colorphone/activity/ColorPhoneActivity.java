package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.VideoManager;
import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.ringtones.view.RingtonePageView;
import com.honeycomb.colorphone.AppflyerLogger;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigChangeManager;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.autopermission.AutoLogger;
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
import com.honeycomb.colorphone.http.bean.AllCategoryBean;
import com.honeycomb.colorphone.http.bean.LoginUserBean;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.menu.SettingsPage;
import com.honeycomb.colorphone.menu.TabItem;
import com.honeycomb.colorphone.news.NewsFrame;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.receiver.NetworkStateChangedReceiver;
import com.honeycomb.colorphone.theme.ThemeApplyManager;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.theme.ThemeUpdateListener;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.uploadview.ClassicHeader;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.MediaSharedElementCallback;
import com.honeycomb.colorphone.util.NetUtils;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.DotsPictureResManager;
import com.honeycomb.colorphone.view.DotsPictureView;
import com.honeycomb.colorphone.view.HomePageRefreshFooter;
import com.honeycomb.colorphone.view.MainTabLayout;
import com.honeycomb.colorphone.view.TabFrameLayout;
import com.honeycomb.colorphone.wechatincall.WeChatInCallAutopilot;
import com.honeycomb.colorphone.wechatincall.WeChatInCallUtils;
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
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import net.appcloudbox.AcbAds;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import colorphone.acb.com.libweather.debug.DebugConfig;
import hugo.weaving.DebugLog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements View.OnClickListener, INotificationObserver {

    public static final String NOTIFICATION_ON_REWARDED = "notification_on_rewarded";

    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_SCROLL_TO_BOTTOM = "prefs_main_scroll_to_bottom";
    private static final String PREFS_CASH_CENTER_SHOW = "prefs_cash_center_show";
    private static final String PREFS_CASH_CENTER_GUIDE_SHOW = "prefs_cash_center_guide_show";
    private static final String PREFS_RINGTONE_SHOW = "prefs_ringtone_frame_show";

    private static final String PREFS_CALLFLASH_REQUEST_FIRST = "prefs_callflash_request_first";
    private static final String PREFS_CALLFLASH_RESPONSE_FIRST = "prefs_callflash_response_first";

    private static final int WELCOME_REQUEST_CODE = 2;
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 3;

    private RelativeLayout mMainPage;
    private SmartRefreshLayout mSmartRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mMainNetWorkErrView;
    private View mMainPageCover;
    private ThemeSelectorAdapter mAdapter;
    private List<MainPagerHolder> mainPagerRecyclePool;
    private Map<Integer, MainPagerHolder> mainPagerCachedPool;
    private List<AllCategoryBean.CategoryItem> categoryList;
    private ArrayList<Theme> mRecyclerViewData = new ArrayList<>();
    private boolean firstShowPager = true;
    public int mainPagerPosition = 0;
    private boolean isPaused;
    private boolean isWindowFocus;

    private Handler mHandler = new Handler();

    private boolean mIsHandsDown = false;
    private boolean mIsFirstScrollThisTimeHandsDown = true;
    public static final int SCROLL_STATE_DRAGGING = 1;
    private boolean isDoubleClickToolbar = false;
    private boolean isFirstRequestData = true;
    private boolean hasLoggedRequestCategory = true;
    private boolean isNeedSetFirstTheme = false;

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
                    guideLottie.setVisibility(View.VISIBLE);
                    guideLottie.setOnClickListener(view -> mTabFrameLayout.setCurrentItem(getTabPos(TabItem.TAB_RINGTONE)));
                    guideLottie.playAnimation();

                    if (mTabLayout.getSelectedTabPosition() == getTabPos(TabItem.TAB_MAIN)) {
                        guideLottie.setVisibility(VISIBLE);
                    }
                }
            }
        }
    };

    /**
     * For activity transition
     */
    private MediaSharedElementCallback sharedElementCallback;

    private ConfigChangeManager.Callback configChangeCallback = type -> {

    };

    private boolean mMainViewShowFlag;
    private SettingsPage mSettingsPage = new SettingsPage();
    private NewsFrame newsLayout;
    private RingtonePageView mRingtoneFrame;

    private TabFrameLayout mTabFrameLayout;
    private Toolbar toolbar;
    private MainTabLayout mTabLayout;
    private TabTransController tabTransController;
    private LottieAnimationView guideLottie;

    private DoubleBackHandler mDoubleBackHandler = new DoubleBackHandler();
    private ThemePagerAdapter mainPagerAdapter;
    private TabLayout mMainPageTab;
    private ViewPager mViewPager;
    private GridView mGridView;
    private TextView mCategoriesTitle;
    private ImageView mArrowLeftPart;
    private ImageView mArrowRightPart;
    private AnimatorSet mAnimatorSet;
    private DotsPictureView mDotsPictureView;
    private boolean mainPagerScrolled;
    private View arrowContainer;
    private boolean tabScrolling = false;

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
        AppflyerLogger.logAppOpen();
        // Transition
        sharedElementCallback = new MediaSharedElementCallback();
        sharedElementCallback.setClearAfterConsume(true);
        ActivityCompat.setExitSharedElementCallback(this, sharedElementCallback);

        dispatchPermissionRequest();

        String networkName = NetUtils.getNetWorkStateName();
        Preferences.getDefault().putString(NetworkStateChangedReceiver.PREF_KEY_NETWORK_STATE_NAME, networkName);
        HSLog.d(NetworkStateChangedReceiver.TAG, "refresh network state = " + networkName);
    }

    public List<AllCategoryBean.CategoryItem> getCategoryList() {
        return categoryList;
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

    public boolean isRefreshing() {
        return mSmartRefreshLayout != null && mSmartRefreshLayout.getState() == RefreshState.Refreshing;
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
                R.drawable.seletor_tab_main, "首页", false));

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

        boolean needRingtoneRemind = !Preferences.getDefault().getBoolean(PREFS_RINGTONE_SHOW, false);
        if (needRingtoneRemind) {
            View ringtoneTab = mTabLayout.getTabAt(getTabPos(TabItem.TAB_RINGTONE));
            if (ringtoneTab != null) {
                View hintView = ringtoneTab.findViewById(R.id.tab_layout_hint);
                hintView.getLayoutParams().height = Dimensions.pxFromDp(6);
                hintView.getLayoutParams().width = Dimensions.pxFromDp(6);
                hintView.setVisibility(VISIBLE);
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

        mTabFrameLayout.setFrameProvider((viewGroup, pos) -> createFrameItem(viewGroup, pos));

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
                // Hide red point for Ringtone Tab
                if (TabItem.TAB_RINGTONE.equals(tabItem.getId())) {
                    Preferences.getDefault().putBoolean(PREFS_RINGTONE_SHOW, true);
                    if (tabView != null) {
                        tabView.findViewById(R.id.tab_layout_hint).setVisibility(GONE);
                    }
                }

                if (tabItem.getId().equals(TabItem.TAB_NEWS)) {
                    toolbar.setVisibility(VISIBLE);
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
                        tabView.findViewById(R.id.tab_layout_hint).setVisibility(GONE);
                    }
                    updateTabStyle(true);

                } else {
                    if (tabItem.isEnableToolBarTitle()) {
                        toolbar.setVisibility(VISIBLE);
                    } else {
                        toolbar.setVisibility(GONE);
                    }

                    ActivityUtils.setCustomColorStatusBar(ColorPhoneActivity.this, colorPrimary);

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
                        Analytics.logEvent("ThemeCategory_Page_Show", "Category", categoryList.get(mainPagerPosition).getName());
                        if (guideLottie != null) {
                            guideLottie.setVisibility(VISIBLE);
                            guideLottie.setProgress(1f);
                        }
                        startCurrentVideo();
                        Analytics.logEvent("Tab_Themes_Show");
                        break;
                    case TabItem.TAB_NEWS:
                        endCurrentVideo();
                        break;
                    case TabItem.TAB_RINGTONE:
                        endCurrentVideo();
                        Analytics.logEvent("Tab_RingTone_Show");
                        break;
                    case TabItem.TAB_SETTINGS:
                        if (guideLottie != null) {
                            guideLottie.setVisibility(GONE);
                        }
                        endCurrentVideo();
                        Analytics.logEvent("Tab_Settings_Show");
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

        if (mRingtoneFrame != null) {
            mRingtoneFrame.onStart();
        }
    }

    private void startCurrentVideo() {
        if (mRecyclerView != null && mRecyclerView.getAdapter() instanceof ThemeSelectorAdapter) {
            ThemeSelectorAdapter themeSelectorAdapter = (ThemeSelectorAdapter) mRecyclerView.getAdapter();
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(themeSelectorAdapter.getLastSelectedLayoutPos());
            if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
                ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).startAnimation();
            }
        }
    }

    private void endCurrentVideo() {
        if (mRecyclerView != null && mRecyclerView.getAdapter() instanceof ThemeSelectorAdapter) {
            ThemeSelectorAdapter themeSelectorAdapter = (ThemeSelectorAdapter) mRecyclerView.getAdapter();
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(themeSelectorAdapter.getLastSelectedLayoutPos());
            if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
                ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).stopAnimation();
            }
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

            //start home page list video (make current theme playing)
            if (mTabLayout.getSelectedTabPosition() == getTabPos(TabItem.TAB_MAIN)) {
                startCurrentVideo();
            }
        }

        if (tabTransController != null) {
            tabTransController.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPaused = true;
        mHandler.removeCallbacks(mainViewRunnable);

        if (tabTransController != null) {
            tabTransController.hide();
        }

        if (mRecyclerView != null) {
            mRecyclerView.getRecycledViewPool().clear();
        }

        endCurrentVideo();
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
        if (!hasLoggedRequestCategory) {
            Analytics.logEvent("CallFlash_Request");
        }
        hasLoggedRequestCategory = false;
        ThemeList.getInstance().requestCategoryThemes(categoryList.get(mainPagerPosition).getId(), isRefresh, new ThemeUpdateListener() {
            @Override
            public void onFailure(String errorMsg) {
                if (isFirstRequestData) {
                    Analytics.logEvent("CallFlash_Request_First_Failed", "type", errorMsg);
                    if (mMainNetWorkErrView != null) {
                        mMainNetWorkErrView.setVisibility(VISIBLE);
                    }
                    isFirstRequestData = false;
                }
                Analytics.logEvent("CallFlash_Request_Failed", "type", errorMsg);
                Preferences.getDefault().doOnce(() -> Analytics.logEvent("CallFlash_Request_FirstSession_Failed"), PREFS_CALLFLASH_RESPONSE_FIRST);

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
                hideLoadingMainPage(true);
            }

            @Override
            public void onSuccess(boolean isHasData) {
                if (mMainNetWorkErrView != null) {
                    mMainNetWorkErrView.setVisibility(GONE);
                }
                if (isFirstRequestData) {
                    Analytics.logEvent("CallFlash_Request_First_Success");
                    isFirstRequestData = false;
                }
                Analytics.logEvent("CallFlash_Request_Success");
                Preferences.getDefault().doOnce(() -> Analytics.logEvent("CallFlash_Request_FirstSession_Success"), PREFS_CALLFLASH_RESPONSE_FIRST);

                if (isRefresh) {
                    mSmartRefreshLayout.finishRefresh();
                    mSmartRefreshLayout.resetNoMoreData();
                } else {
                    mSmartRefreshLayout.finishLoadMore(true);
                }
                if (isHasData) {
                    refreshData(isRefresh);
                } else {
                    if (!isRefresh) {
                        mSmartRefreshLayout.finishLoadMoreWithNoMoreData();
                    }
                }

                //download current theme
                if (ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1) == -1 && Theme.getFirstTheme() != null) {
                    isNeedSetFirstTheme = true;
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
                }
                hideLoadingMainPage(true);
            }
        });
    }

    DownloadStateListener mDownloadStateListener = new DownloadStateListener() {
        @Override
        public void updateDownloaded(boolean progressFlag) {
            if (isNeedSetFirstTheme && Theme.getFirstTheme() != null) {
                Theme theme = Theme.getFirstTheme();
                ThemeApplyManager.getInstance().addAppliedTheme(theme.toPrefString());
                ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, theme.getId());
                boolean isShouldSetWeChatTheme = WeChatInCallUtils.isWeChatThemeEnable() && WeChatInCallAutopilot.isEnable() && WeChatInCallAutopilot.isSetDefault();
                if (isShouldSetWeChatTheme) {
                    WeChatInCallUtils.applyWeChatInCallTheme(theme, true);
                }

                if (mRecyclerViewData.size() > 0 && mRecyclerViewData.get(0) != null && mRecyclerViewData.get(0).getId() == Theme.getFirstTheme().getId()) {
                    mRecyclerViewData.get(0).setSelected(true);
                    if (isShouldSetWeChatTheme) {
                        mRecyclerViewData.get(0).setWeChatSelected(true);
                    }
                    mAdapter.notifyDataSetChanged();
                }
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
            if (isNeedSetFirstTheme && Theme.getFirstTheme() != null) {
                if (RuntimePermissions.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != RuntimePermissions.PERMISSION_GRANTED ||
                        !Settings.System.canWrite(getBaseContext())) {

                    Toast.makeText(getBaseContext(), "设置铃声失败，请授予权限", Toast.LENGTH_LONG).show();
                    return;
                }
                RingtoneHelper.setDefaultRingtoneInBackground(Theme.getFirstTheme());
            }
            FileDownloadMultiListener.getDefault().removeStateListener(ringtoneModel.getId());
        }

        @Override
        public void updateNotDownloaded(int status, long sofar, long total) {
        }

        @Override
        public void updateDownloading(int status, long sofar, long total) {
        }
    };

    private void refreshData(boolean isRefresh) {
        setData(isRefresh);
    }


    private void dispatchPermissionRequest() {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        if (!isEnabled) {
            return;
        }
        List<String> reqPermission = AutoRequestManager.getNOTGrantRuntimePermissions(AutoRequestManager.getAllRuntimePermission());
        if (reqPermission.size() > 0) {
            requiresPermission(reqPermission);
            Analytics.logEvent("Permission_MainView_Request", "Permission", AutoLogger.getRuntimePermissionString(reqPermission));
        }
    }

    /**
     * Only request first launch. (if Enabled and not has permission)
     *
     * @param reqPermission
     */
    private void requiresPermission(List<String> reqPermission) {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        HSLog.i("Permissions ScreenFlash state change : " + isEnabled);
        if (!isEnabled) {
            return;
        }

        // Do not have permissions, request them now
        RuntimePermissions.requestPermissions(this, reqPermission.toArray(new String[0]), FIRST_LAUNCH_PERMISSION_REQUEST);
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
            Analytics.logEvent("Permission_MainView_Granted", "Permission", AutoLogger.getRuntimePermissionString(list));
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

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        TasksManager.getImpl().onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
        PermissionHelper.stopObservingPermission();
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }

        if (tabTransController != null) {
            tabTransController.release();
        }
        ConfigChangeManager.getInstance().removeCallback(configChangeCallback);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if ((mRingtoneFrame != null && mRingtoneFrame.onBackPressed())) {
            return;
        }

        if (mDoubleBackHandler.interceptBackPressed()) {
            mDoubleBackHandler.toast();
        } else {
            Preferences.getDefault().doOnce(() ->
                    Analytics.logEvent(
                            "ColorPhone_MainView_Exit_First"
                            ,"Permission"
                            , AutoLogger.getGrantRuntimePermissions())
                    ,"ColorPhone_MainView_Exit_First");
            super.onBackPressed();
            TasksManager.getImpl().stopAllTasks();
        }
    }

    private void setData(boolean isRefresh) {
        mRecyclerViewData = ThemeList.getInstance().getCategoryThemes(categoryList.get(mainPagerPosition).getId());
        ThemeSelectorAdapter adapter = (ThemeSelectorAdapter) mRecyclerView.getAdapter();
        if (adapter == null || isRefresh) {
            mAdapter = new ThemeSelectorAdapter(this, mRecyclerViewData, mainPagerPosition);
            mRecyclerView.setLayoutManager(mAdapter.getLayoutManager());
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter = adapter;
            mAdapter.setData(mRecyclerViewData, mainPagerPosition);
            mAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initNetworkErrorView(View rootView) {
        mMainNetWorkErrView = rootView.findViewById(R.id.frame_no_network);
        mMainNetWorkErrView.findViewById(R.id.no_network_action).setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#ff696681"),
                Dimensions.pxFromDp(22), true));
        mMainNetWorkErrView.findViewById(R.id.no_network_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categoryList.size() == 1) {
                    requestCategories(mainPagerCachedPool.get(0));
                } else {
                    mSmartRefreshLayout.autoRefresh();
                }
            }
        });
        mMainNetWorkErrView.setOnTouchListener((v, event) -> true);
    }

    private void initRefreshView(SmartRefreshLayout smartRefreshLayout, boolean autoRefresh) {
        mSmartRefreshLayout = smartRefreshLayout;
        mSmartRefreshLayout.setEnableAutoLoadMore(true);
        mSmartRefreshLayout.setRefreshHeader(new ClassicHeader(this));
        mSmartRefreshLayout.setRefreshFooter(new HomePageRefreshFooter(this));
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                requestThemeData(true);
                mAdapter.setData(mRecyclerViewData, mainPagerPosition);
                mAdapter.notifyDataSetChanged();
            }
        });
        mSmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                requestThemeData(false);
                mAdapter.setData(mRecyclerViewData, mainPagerPosition);
                mAdapter.notifyDataSetChanged();
            }
        });

        //触发自动刷新
        if (autoRefresh) {
            mSmartRefreshLayout.autoRefresh();
        } else {
            requestThemeData(true);
        }
    }

    private void initRecyclerView() {
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ThemeSelectorAdapter(this, mRecyclerViewData, mainPagerPosition);
        mRecyclerView.setLayoutManager(mAdapter.getLayoutManager());
        mAdapter.setHotThemeHolderVisible(HSConfig.optBoolean(false, "Application", "Special", "SpecialEntrance"));
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
            refreshData(true);
        }
    }

    public boolean isNewsTab() {
        return mTabLayout.getSelectedTabPosition() == getTabPos(TabItem.TAB_NEWS);
    }

    @Deprecated
    private boolean needUpdateNews() {
        return false;
    }

    @SuppressLint("UseSparseArrays")
    private View createFrameItem(ViewGroup container, int position) {
        HSLog.d("MainTabAdapter", "getItem");
        View frame = null;
        final TabItem tabItem = mTabItems.get(position);
        switch (tabItem.getId()) {
            case TabItem.TAB_MAIN:
                if (mMainPage == null) {
                    frame = getLayoutInflater().inflate(R.layout.main_frame_content, null, false);
                    mMainPage = (RelativeLayout) frame;
                    mainPagerRecyclePool = new ArrayList<>();
                    mainPagerCachedPool = new HashMap<>();
                    categoryList = new ArrayList<>();
                    AllCategoryBean.CategoryItem categoryItem = new AllCategoryBean.CategoryItem();
                    categoryItem.setId("-1");
                    categoryItem.setName("发现");
                    categoryList.add(categoryItem);

                    mMainPageTab = frame.findViewById(R.id.main_page_tabs);
                    mViewPager = frame.findViewById(R.id.main_tab_pager);
                    mGridView = frame.findViewById(R.id.categories_grid_view);
                    mCategoriesTitle = frame.findViewById(R.id.categories_title);
                    mArrowLeftPart = frame.findViewById(R.id.tab_top_arrow_left);
                    mArrowRightPart = frame.findViewById(R.id.tab_top_arrow_right);

                    mViewPager.setOffscreenPageLimit(2);
                    ViewStub stub = frame.findViewById(R.id.stub_loading_animation);
                    stub.inflate();
                    mDotsPictureView = frame.findViewById(R.id.dots_progress_view);
                    mDotsPictureView.setVisibility(VISIBLE);

                    initGridViewListener(frame);
                    mGridView.setAdapter(new MainPageGridAdapter());

                    arrowContainer = frame.findViewById(R.id.arrow_container);
                    setArrowOnClickAnimation(frame, mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart);
                    mMainPageCover = frame.findViewById(R.id.main_page_cover);
                    mMainPageCover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            arrowContainer.performClick();
                        }
                    });

                    mMainPageTab.setSelectedTabIndicatorHeight(0);
                    frame.findViewById(R.id.tab_layout_container)
                            .setElevation(Dimensions.pxFromDp(1));

                    mMainPageTab.setupWithViewPager(mViewPager);
                    mMainPageTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            try {
                                Field fieldView = tab.getClass().getDeclaredField("mView");
                                fieldView.setAccessible(true);
                                View view = (View) fieldView.get(tab);
                                Field fieldTxt = view.getClass().getDeclaredField("mTextView");
                                fieldTxt.setAccessible(true);
                                TextView tabSelect = (TextView) fieldTxt.get(view);

                                tabSelect.setTypeface(FontUtils.getTypeface(FontUtils.Font.ROBOTO_MEDIUM));
                                tabSelect.setText(tab.getText());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {
                            try {
                                Field fieldView = tab.getClass().getDeclaredField("mView");
                                fieldView.setAccessible(true);
                                View view = (View) fieldView.get(tab);
                                Field fieldTxt = view.getClass().getDeclaredField("mTextView");
                                fieldTxt.setAccessible(true);
                                TextView tabSelect = (TextView) fieldTxt.get(view);

                                tabSelect.setTypeface(FontUtils.getTypeface(FontUtils.Font.ROBOTO_REGULAR));
                                tabSelect.setText(tab.getText());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                    mMainPageTab.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            if (!tabScrolling) {
                                Analytics.logEvent("ThemeCategory_Tabbar_Slide");
                                tabScrolling = true;
                                Threads.postOnMainThreadDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tabScrolling = false;
                                    }
                                }, 500);
                            }
                        }
                    });
                    mainPagerAdapter = new ThemePagerAdapter();

                    mViewPager.setAdapter(mainPagerAdapter);
                    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            if (!mainPagerScrolled) {
                                mainPagerScrolled = true;
                            }
                        }

                        @Override
                        public void onPageSelected(int position) {
                            mainPagerPosition = position;
                            MainPagerHolder holder = mainPagerCachedPool.get(position);
                            ((MainPageGridAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                            if (holder != null) {
                                mRecyclerView = holder.recyclerView;
                                mSmartRefreshLayout = holder.refreshLayout;
                                Threads.postOnMainThreadDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!holder.loaded()) {
                                            holder.load(false);
                                            mDotsPictureView.setVisibility(VISIBLE);
                                            mDotsPictureView.setBitmapPool(DotsPictureResManager.get().getBitmapPool());
                                            mDotsPictureView.setDotResultBitmap(DotsPictureResManager.get().getDotsBitmap());
                                            mDotsPictureView.startAnimation();
                                        }
                                    }
                                }, 200);
                            }
                            Analytics.logEvent("ThemeCategory_Page_Show", "Category", categoryList.get(mainPagerPosition).getName());
                            Analytics.logEvent("ThemeCategory_Page_Switch", "CategorySwitchMode", categoryList.get(position).getName(), "SwitchMode", mainPagerScrolled ? "slide" : "click");
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                            if (state == ViewPager.SCROLL_STATE_IDLE) {
                                mainPagerScrolled = false;
                            }
                        }
                    });
                    initNetworkErrorView(frame);
                } else {
                    frame = mMainPage;
                }
                Analytics.logEvent("ThemeCategory_Page_Show", "Category", categoryList.get(mainPagerPosition).getName());
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
            default:
                throw new IllegalStateException("Pager index out of bounds");
        }

        frame.setTag(position);
        return frame;
    }

    private void requestCategories(MainPagerHolder holder) {
        if (isFirstRequestData) {
            Analytics.logEvent("CallFlash_Request_First");
        } else {
            Analytics.logEvent("CallFlash_Request");
        }
        Preferences.getDefault().doOnce(() -> Analytics.logEvent("CallFlash_Request_FirstSession"), PREFS_CALLFLASH_REQUEST_FIRST);
        hasLoggedRequestCategory = true;
        HttpManager.getInstance().getAllCategories(new Callback<AllCategoryBean>() {
            @Override
            public void onFailure(String errorMsg) {
                if (isFirstRequestData) {
                    Analytics.logEvent("CallFlash_Request_First_Failed", "type", errorMsg);
                    if (mMainNetWorkErrView != null) {
                        mMainNetWorkErrView.setVisibility(VISIBLE);
                    }
                    isFirstRequestData = false;
                }
                Analytics.logEvent("CallFlash_Request_Failed", "type", errorMsg);
                Preferences.getDefault().doOnce(() -> Analytics.logEvent("CallFlash_Request_FirstSession_Failed"), PREFS_CALLFLASH_RESPONSE_FIRST);

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.theme_page_not_network_toast, findViewById(R.id.toast_layout));
                Toast toast = new Toast(getBaseContext());
                toast.setGravity(Gravity.CENTER | Gravity.TOP, 0, Dimensions.pxFromDp(80));
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(layout);
                toast.show();
            }

            @Override
            public void onSuccess(AllCategoryBean allCategoryBean) {
                if (allCategoryBean.getCategories() != null && allCategoryBean.getCategories().size() > 1) {
                    categoryList = allCategoryBean.getCategories();
                    holder.load(true);
                    mainPagerAdapter.notifyDataSetChanged();
                    refreshMainPageTab();
                } else {
                    onFailure("网络异常");
                }
            }
        });
    }

    private void refreshMainPageTab() {
        try {
            for (int i = 0; i < mMainPageTab.getTabCount(); i++) {
                TabLayout.Tab tab = mMainPageTab.getTabAt(i);
                if (tab != null) {
                    Field fieldView = tab.getClass().getDeclaredField("mView");
                    fieldView.setAccessible(true);
                    View view = (View) fieldView.get(tab);
                    Field fieldTxt = view.getClass().getDeclaredField("mTextView");
                    fieldTxt.setAccessible(true);
                    TextView tabSelect = (TextView) fieldTxt.get(view);

                    tabSelect.setTypeface(FontUtils.getTypeface(FontUtils.Font.ROBOTO_REGULAR));
                    tabSelect.setText(tab.getText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (mAdapter != null) {
            final int adapterPosition = mAdapter.themePositionToAdapterPosition(exitPos);
            if (mRecyclerView != null) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(adapterPosition);
                if (viewHolder != null && viewHolder.itemView != null) {
                    ImageView imageView = viewHolder.itemView.findViewById(R.id.card_preview_img);
                    sharedElementCallback.setSharedElementViews(imageView);
                }
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
                if (mInterceptView.getVisibility() != VISIBLE) {
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

    public class ThemePagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((MainPagerHolder) object).getItemView();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            MainPagerHolder holder;
            if (mainPagerRecyclePool.size() > 0) {
                int lastIndex = mainPagerRecyclePool.size() - 1;
                holder = mainPagerRecyclePool.get(lastIndex);
                mainPagerRecyclePool.remove(lastIndex);
            } else {
                holder = new MainPagerHolder(View.inflate(ColorPhoneActivity.this, R.layout.main_tab_pager_item, null));
            }
            container.addView(holder.getItemView());
            mainPagerCachedPool.put(position, holder);
            if (firstShowPager && position == 0) {
                firstShowPager = false;
                mRecyclerView = holder.recyclerView;
                requestCategories(holder);
            }
            return holder;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(((MainPagerHolder) object).getItemView());
            mainPagerRecyclePool.remove(object);
            mainPagerCachedPool.remove(position);
            ThemeList.getInstance().clearCategoryThemes(categoryList.get(position).getId());
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return categoryList.get(position).getName();
        }
    }

    private class MainPagerHolder {
        private View itemView;
        private SmartRefreshLayout refreshLayout;
        private RecyclerView recyclerView;

        MainPagerHolder(View itemView) {
            this.itemView = itemView;
            this.refreshLayout = itemView.findViewById(R.id.refresh_layout);
            this.recyclerView = itemView.findViewById(R.id.recycler_view);
        }

        public View getItemView() {
            return itemView;
        }

        public void load(boolean autoRefresh) {
            initRecyclerView();
            initRefreshView(refreshLayout, autoRefresh);
        }

        private boolean loaded() {
            boolean hasRefreshListener = false;
            try {
                Field listener = mSmartRefreshLayout.getClass().getDeclaredField("mRefreshListener");
                listener.setAccessible(true);
                hasRefreshListener = listener.get(mSmartRefreshLayout) != null;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return recyclerView.getAdapter() != null && recyclerView.getAdapter().getItemCount() > 1 && hasRefreshListener;
        }
    }

    private void setArrowOnClickAnimation(View frame, final GridView categoryView,
                                          final TextView categoryTitle,
                                          final ImageView arrowLeftPart,
                                          final ImageView arrowRightPart) {
        arrowContainer.setOnClickListener(v -> arrowClicked(frame, categoryView, categoryTitle, arrowLeftPart, arrowRightPart, "ArrowClicked"));
    }

    private void arrowClicked(View frame, final GridView categoryView,
                              final TextView categoryTitle,
                              final ImageView arrowLeftPart,
                              final ImageView arrowRightPart, final String flurryClickType) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }
        final int start = Math.abs((int) arrowLeftPart.getRotation() % 360);
        HSLog.d("WallpaperAnimator ", "start value " + start + "");
        float startAlpha = start == 0 ? 0 : 1;
        ObjectAnimator alpha = ObjectAnimator.ofFloat(categoryView, "alpha", startAlpha, 1f - startAlpha);
        alpha.setDuration(300);
        alpha.setInterpolator(new AccelerateInterpolator());
        int degree = !Dimensions.isRtl() ? 90 : -90;
        // shrink
        if (start == 90) {
            mMainPageCover.setVisibility(GONE);
            frame.findViewById(R.id.tab_layout_container)
                    .setElevation(Dimensions.pxFromDp(1));

            ObjectAnimator arrowRotateLeft = ObjectAnimator.ofFloat(arrowLeftPart, "rotation", -degree, 0);
            arrowRotateLeft.setDuration(300);

            ObjectAnimator arrowRotateRight = ObjectAnimator.ofFloat(arrowRightPart, "rotation", degree, 0);
            arrowRotateRight.setDuration(300);

            mMainPageTab.setVisibility(VISIBLE);
            categoryTitle.setVisibility(GONE);
            ObjectAnimator transY = ObjectAnimator.ofFloat(categoryView, "translationY", 0, -categoryView.getHeight());
            transY.setInterpolator(new AccelerateInterpolator());
            transY.setDuration(160);

            transY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    categoryView.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    categoryView.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(arrowRotateLeft, arrowRotateRight, transY);
            mAnimatorSet.start();
        }
        // expand
        else {
            mMainPageCover.setVisibility(VISIBLE);
            frame.findViewById(R.id.tab_layout_container)
                    .setElevation(0);

            ObjectAnimator arrowRotateLeft = ObjectAnimator.ofFloat(arrowLeftPart, "rotation", 0, -degree);
            arrowRotateLeft.setDuration(300);

            ObjectAnimator arrowRotateRight = ObjectAnimator.ofFloat(arrowRightPart, "rotation", 0, degree);
            arrowRotateRight.setDuration(300);

            mMainPageTab.setVisibility(GONE);
            categoryTitle.setVisibility(VISIBLE);
            categoryView.setVisibility(VISIBLE);

            categoryView.setTranslationY(-categoryView.getHeight());

            ObjectAnimator transY = ObjectAnimator.ofFloat(categoryView, "translationY", -categoryView.getHeight(), 0);
            transY.setInterpolator(new AccelerateInterpolator());
            transY.setDuration(300);

            AnimatorSet title = (AnimatorSet) AnimatorInflater.loadAnimator(ColorPhoneActivity.this, R.animator.online_wallpaper_categories_title_in);
            title.setTarget(categoryTitle);

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(arrowRotateLeft, arrowRotateRight, title, alpha, transY);
            mAnimatorSet.start();
            mainPagerAdapter.notifyDataSetChanged();
            Analytics.logEvent("ThemeCategory_Tabbar_ShowMore");
        }
    }

    class MainPageGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) View.inflate(ColorPhoneActivity.this, R.layout.main_page_grid_item, null);
            textView.setText(categoryList.get(position).getName());
            if (position == mainPagerPosition) {
                textView.setBackgroundResource(R.drawable.main_page_grid_item_bg_selected);
            } else {
                textView.setBackgroundResource(R.drawable.main_page_grid_item_bg_normal);
            }

            return textView;
        }
    }

    private void initGridViewListener(View frame) {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= categoryList.size() || position == mainPagerPosition) {
                    return;
                }

                MainPageGridAdapter adapter = (MainPageGridAdapter) parent.getAdapter();
//                ((TextView)adapter.getItem(mainPagerPosition)).setBackgroundResource(R.drawable.main_page_grid_item_bg_normal);
//                ((TextView)adapter.getItem(position)).setBackgroundResource(R.drawable.main_page_grid_item_bg_selected);
                mainPagerPosition = position;
                mViewPager.setCurrentItem(position, true);
                adapter.notifyDataSetChanged();
                arrowClicked(frame, mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart, "TabClicked");
            }
        });
    }

    public void hideLoadingMainPage(boolean clean) {
        if (mDotsPictureView != null) {
            mDotsPictureView.setVisibility(View.INVISIBLE);
            mDotsPictureView.stopAnimation();
            if (clean) {
                mDotsPictureView.releaseBitmaps();
            }
        }
    }
}

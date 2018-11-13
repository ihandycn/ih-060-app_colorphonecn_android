package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.bumptech.glide.Glide;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.boost.BoostActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.dialer.AP;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.AvatarAutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
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
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hugo.weaving.DebugLog;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, INotificationObserver {

    public static final String NOTIFICATION_ON_REWARDED = "notification_on_rewarded";

    public static final String PREFS_THEME_APPLY = "theme_apply_array";
    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_SCROLL_TO_BOTTOM = "prefs_main_scroll_to_bottom";

    private static final int WELCOME_REQUEST_CODE = 2;
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 3;

    private RecyclerView mRecyclerView;
    private ThemeSelectorAdapter mAdapter;
    private ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();
    private RewardVideoView mRewardVideoView;

    private SwitchCompat mainSwitch;
    private SwitchCompat notificationToolbarSwitch;
    private SwitchCompat defaultDialer;

    private TextView mainSwitchTxt;

    private final static int RECYCLER_VIEW_SPAN_COUNT = 2;
    private int defaultThemeId = 1;
    private boolean initCheckState;

    private Handler mHandler = new Handler();

    private Runnable UpdateRunnable = new Runnable() {

        @Override
        public void run() {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                runOnUiThread(this);
                return;
            }

            if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
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

    private boolean logOpenEvent;
    private boolean pendingShowRateAlert = false;
    private boolean showAllFeatureGuide = false;
    private boolean isCreate = false;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContactManager.getInstance().update();
        // TODO pro show condition ( SESSION_START, or Activity onStart() )
       if (ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_GUIDE_START)
                && !GuideAllFeaturesActivity.isStarted()
                && !ModuleUtils.isAllModuleEnabled()) {
            GuideAllFeaturesActivity.start(this);
            HSAlertMgr.delayRateAlert();
            pendingShowRateAlert = true;
            showAllFeatureGuide = true;
        } else if (NotificationUtils.isShowNotificationGuideAlertInFirstSession(this)) {
            Intent intent = new Intent(this, NotificationAccessGuideAlertActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, true);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_APP_IS_FIRST_SESSION, true);
            startActivity(intent);
            HSAlertMgr.delayRateAlert();
            HSPreferenceHelper.getDefault().putBoolean(NotificationUtils.PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, true);
        } else if (ModuleUtils.isShowPromoteLockerAlert(PromoteLockerActivity.WHEN_APP_LAUNCH)) {
            PromoteLockerActivity.startPromoteLockerActivity(this, PromoteLockerActivity.WHEN_APP_LAUNCH);
            HSAlertMgr.delayRateAlert();
        }
        setTheme(R.style.AppLightStatusBarTheme);

        setContentView(R.layout.activity_main);
        initMainFrame();
        AdManager.getInstance().preload();
        isCreate = true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (defaultDialer != null) {
            defaultDialer.setChecked(DefaultPhoneUtils.isDefaultPhone());
        }
        if (hasFocus) {
            if (!PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
                mAdapter.setHeaderTipVisible(false);
                mAdapter.notifyDataSetChanged();
            }
        }
        HSLog.d("XXX", " focus change:" + hasFocus);
    }

    public void showRewardVideoView(String themeName) {
        requestRewardAd(themeName);
    }

    @DebugLog
    @NonNull
    private DrawerLayout initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        logOpenEvent = true;
        Utils.configActivityStatusBar(this, toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                LauncherAnalytics.logEvent("Colorphone_Settings_Boost_Icon_Shown");
                LauncherAnalytics.logEvent("Colorphone_Sidebar_Shown");
            }
        };
        DrawerArrowDrawable arrowDrawable = toggle.getDrawerArrowDrawable();
        arrowDrawable.getPaint().setStrokeCap(Paint.Cap.ROUND);
        arrowDrawable.getPaint().setStrokeJoin(Paint.Join.ROUND);
        arrowDrawable.setBarThickness(arrowDrawable.getBarThickness() * 1.5f);
        arrowDrawable.setBarLength(arrowDrawable.getBarLength() * 0.86f);

        drawer.setDrawerListener(toggle);
        toggle.syncState();
        View leftDrawer = findViewById(R.id.left_drawer);
        mainSwitch = leftDrawer.findViewById(R.id.main_switch);
        mainSwitchTxt = leftDrawer.findViewById(R.id.settings_main_switch_txt);


        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && AP.dialerEnable();
        leftDrawer.findViewById(R.id.settings_default_dialer_switch)
                .setVisibility(dialerEnable ? View.VISIBLE : View.GONE);

        defaultDialer = leftDrawer.findViewById(R.id.default_dialer_switch);
        defaultDialer.setChecked(DefaultPhoneUtils.isDefaultPhone());
        defaultDialer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isChecked) {
                        DefaultPhoneUtils.checkDefaultPhoneSettings();
                    } else {
                        DefaultPhoneUtils.resetDefaultPhone();
                    }
                }
            }
        });
        initCheckState = ScreenFlashSettings.isScreenFlashModuleEnabled();
        mainSwitch.setChecked(initCheckState);
        mainSwitchTxt.setText(getString(initCheckState ? R.string.color_phone_enabled : R.string.color_phone_disable));

        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainSwitchTxt.setText(getString(isChecked ? R.string.color_phone_enabled : R.string.color_phone_disable));

                ScreenFlashSettings.setScreenFlashModuleEnabled(isChecked);
            }
        });

//        notificationToolbarSwitch = leftDrawer.findViewById(R.id.notification_toolbar_switch);
//
//        initCheckState = UserSettings.isNotificationToolbarEnabled();
//        notificationToolbarSwitch.setChecked(initCheckState);
//
//        notificationToolbarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                UserSettings.setNotificationToolbarEnabled(isChecked);
//                NotificationManager.getInstance().showNotificationToolbarIfEnabled();
//            }
//        });

        if (Utils.ATLEAST_JELLY_BEAN) {
            leftDrawer.findViewById(R.id.settings_boost).setVisibility(View.VISIBLE);
        } else {
            leftDrawer.findViewById(R.id.settings_boost).setVisibility(View.GONE);
        }

        leftDrawer.findViewById(R.id.settings_main_switch).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_default_dialer_switch).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_led_flash).setOnClickListener(this);
//        leftDrawer.findViewById(R.id.settings_notification_toolbar).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_feedback).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_boost).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_setting).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_contacts).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_about).setOnClickListener(this);
        leftDrawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return drawer;
    }

    @DebugLog
    private void initMainFrame() {
        initDrawer();
        initData();
        initRecyclerView();
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME, this);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED, this);
        HSGlobalNotificationCenter.addObserver(PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED, this);
        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));

        Button avatar = findViewById(R.id.avatar_btn);
        if (AvatarAutoPilotUtils.isAvatarBtnShow()) {
            avatar.setVisibility(View.VISIBLE);
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ColorPhoneActivity.this, AvatarVideoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
//                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                }
            });
            AvatarAutoPilotUtils.logAvatarButtonShown();
        } else {
            avatar.setVisibility(View.GONE);
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
//        if (pendingShowRateAlert && SessionMgr.getInstance().getCurrentSessionId() >= 3) {
//            HSAlertMgr.showRateAlert();
//            pendingShowRateAlert = false;
//        }
        mHandler.postDelayed(mainViewRunnable, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        HSLog.d("ColorPhoneActivity", "onPause" + mAdapter.getLastSelectedLayoutPos() + "");
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.getLastSelectedLayoutPos());
        if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).stopAnimation();
        }
        mRecyclerView.getRecycledViewPool().clear();
        mHandler.removeCallbacks(mainViewRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mainSwitch != null) {
            boolean nowEnable = mainSwitch.isChecked();
            if (nowEnable != initCheckState) {
                initCheckState = nowEnable;
                ColorPhoneApplication.getConfigLog().getEvent().onColorPhoneEnableFromSetting(nowEnable);
            }
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

        ((ColorPhoneApplication) ColorPhoneApplication.getContext().getApplicationContext()).logOnceFirstSessionEndStatus();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        mRecyclerViewData.clear();
        mRecyclerViewData.addAll(Theme.themes());
        final int count = mRecyclerViewData.size();
        int selectedThemeId = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1);
        if (selectedThemeId == -1) {
            selectedThemeId = defaultThemeId;
            ThemePreviewView.saveThemeApplys(defaultThemeId);
            ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, defaultThemeId);
        }
        String[] likeThemes = getThemeLikes();
        for (int i = 0; i < count; i++) {
            final Theme theme = mRecyclerViewData.get(i);
            // Like ?
            boolean isLike = isLikeTheme(likeThemes, theme.getValue());
            if (isLike) {
                theme.setDownload(theme.getDownload() + 1);
            }
            theme.setLike(isLike);
            // Selected ?
            if (theme.getId() == selectedThemeId) {
                theme.setSelected(true);
            }

            if (theme.isMedia()) {
                TasksManager.getImpl().addTask(theme);
            }
        }

        Collections.sort(mRecyclerViewData, new Comparator<Theme>() {
            @Override
            public int compare(Theme o1, Theme o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });

    }

    private boolean isLikeTheme(String[] likeThemes, int themeId) {
        for (String likeThemeId : likeThemes) {
            if (TextUtils.isEmpty(likeThemeId)) {
                continue;
            }
            if (themeId == Integer.parseInt(likeThemeId)) {
                return true;
            }
        }
        return false;
    }

    private void initRecyclerView() {
        View contentView = findViewById(R.id.recycler_view_content);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ThemeSelectorAdapter(this, mRecyclerViewData);
        mRecyclerView.setLayoutManager(mAdapter.getLayoutManager());
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
        if (PermissionChecker.getInstance().hasNoGrantedPermissions(PermissionChecker.ScreenFlash)) {
            mAdapter.setHeaderTipVisible(true);
        } else {
            mAdapter.setHeaderTipVisible(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_main_switch:
                mainSwitch.toggle();
                break;
            case  R.id.settings_default_dialer_switch:
                defaultDialer.toggle();
                break;
            case R.id.settings_led_flash:
                LedFlashSettingsActivity.start(this);
                break;
//            case R.id.settings_notification_toolbar:
//                toggleNotificationToolbar();
//                break;
            case R.id.settings_feedback:
                feedBack();
                ColorPhoneApplication.getConfigLog().getEvent().onFeedBackClick();
                break;
            case R.id.settings_boost:
                BoostActivity.start(ColorPhoneActivity.this, false);
                LauncherAnalytics.logEvent("Colorphone_Settings_Boost_Icon_Clicked");
                break;
            case R.id.settings_setting:
                SettingsActivity.start(this);
                break;
            case R.id.settings_contacts:
                ContactsActivity.startEdit(this);
                LauncherAnalytics.logEvent("Colorphone_Settings_ContactTheme_Clicked");
                break;
            case R.id.settings_about:
                AboutActivity.start(this);
                break;
        }
    }

    private void feedBack() {
        Utils.sentEmail(this, new String[]{Constants.FEED_BACK_EMAIL}, null, null);
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
            mainSwitch.setChecked(true);
        } else if (NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME.equals(s)) {
            initData();
            mAdapter.notifyDataSetChanged();
        } else if (HSNotificationConstant.HS_SESSION_START.equals(s)) {
            ChargingPreferenceUtil.setChargingModulePreferenceEnabled(SmartChargingSettings.isChargingScreenEnabled());
            ChargingPreferenceUtil.setChargingReportSettingEnabled(SmartChargingSettings.isChargingReportEnabled());
            ColorPhoneApplication.checkChargingReportAdPlacement();
//            if (!pendingShowRateAlert) {
//                HSLog.i("Permissions", "show Permission dialog");
//                dispatchPermissionRequest();
//            }
        } else if (PermissionHelper.NOTIFY_NOTIFICATION_PERMISSION_GRANTED.equals(s)
                || PermissionHelper.NOTIFY_OVERLAY_PERMISSION_GRANTED.equals(s)) {
            boolean visible = mAdapter.isTipHeaderVisible();
            updatePermissionHeader();
            if (visible != mAdapter.isTipHeaderVisible()) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }


}

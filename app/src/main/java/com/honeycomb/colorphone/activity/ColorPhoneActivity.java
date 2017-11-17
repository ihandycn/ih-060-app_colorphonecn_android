package com.honeycomb.colorphone.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.acb.call.constant.CPConst;
import com.acb.call.themes.Type;
import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.acb.utils.PermissionUtils;
import com.bumptech.glide.Glide;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import hugo.weaving.DebugLog;

public class ColorPhoneActivity extends HSAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, INotificationObserver {

    public static final String NOTIFY_WINDOW_INVISIBLE = "notify_window_invisible";
    public static final String NOTIFY_WINDOW_VISIBLE = "notify_window_visible";
    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final String PREFS_NOTIFICATION_ACCESS_TOAST_HAS_SHOWED = "prefs_notification_access_toast_showed";

    private RecyclerView mRecyclerView;
    private ThemeSelectorAdapter mAdapter;
    private ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();

    private SwitchCompat mainSwitch;
    private TextView mainSwitchTxt;

    private ViewGroup notificationToast;

    private final static int RECYCLER_VIEW_SPAN_COUNT = 2;
    private int defaultThemeId = 14;
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
    private boolean logOpenEvent;
    private boolean pendingShowRateAlert = true;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(NotificationUtils.isShowNotificationGuideAlertInFirstSession(this)) {
            Intent intent = new Intent(this, NotificationAccessGuideAlertActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, true);
            startActivity(intent);
            HSPreferenceHelper.getDefault().putBoolean(NotificationUtils.PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, true);
        } else {
            if (ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_GUIDE_START)
                    && !GuideLockerAssistantActivity.isStarted()
                    && !ModuleUtils.isAllModuleEnabled()) {
                GuideLockerAssistantActivity.start(this);
                HSAlertMgr.delayRateAlert();
                pendingShowRateAlert = true;
            }
        }
        setTheme(R.style.AppLightStatusBarTheme);

        setContentView(R.layout.activity_main);
        initMainFrame();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        HSLog.d("XXX", " focus change:" + hasFocus);
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
                if (PermissionUtils.isNotificationAccessGranted(ColorPhoneActivity.this)) {
                    if (notificationToast != null) {
                        notificationToast.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && !PermissionUtils.isNotificationAccessGranted(ColorPhoneActivity.this)
                        && !HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_ACCESS_TOAST_HAS_SHOWED, false)) {
                    doNotificationAccessToastAnim();
                    HSAnalytics.logEvent("Colorphone_Settings_NotificationTips_Show");
                }
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
        mainSwitch = (SwitchCompat) leftDrawer.findViewById(R.id.main_switch);
        mainSwitchTxt = (TextView) leftDrawer.findViewById(R.id.settings_main_switch_txt);

        initCheckState = CPSettings.isScreenFlashModuleEnabled();
        mainSwitch.setChecked(initCheckState);
        mainSwitchTxt.setText(getString(initCheckState ? R.string.color_phone_enabled : R.string.color_phone_disable));

        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainSwitchTxt.setText(getString(isChecked ? R.string.color_phone_enabled : R.string.color_phone_disable));

                CPSettings.setScreenFlashModuleEnabled(isChecked);
            }
        });
        leftDrawer.findViewById(R.id.settings_main_switch).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_feedback).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_setting).setOnClickListener(this);
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
        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));
    }

    @Override
    protected void onResume() {
        super.onResume();
        HSGlobalNotificationCenter.sendNotification(NOTIFY_WINDOW_VISIBLE);
        if (pendingShowRateAlert && SessionMgr.getInstance().getCurrentSessionId() >= 2) {
            HSAlertMgr.showRateAlert();
            pendingShowRateAlert = false;
        }
        if (logOpenEvent) {
            logOpenEvent = false;
            mainSwitch.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ColorPhoneApplication.getConfigLog().getEvent().onMainViewOpen();
                }
            }, 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        HSGlobalNotificationCenter.sendNotification(NOTIFY_WINDOW_INVISIBLE);
        mRecyclerView.getRecycledViewPool().clear();
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
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(null);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        ArrayList<Type> themeTypes = Type.values();
        final int count = themeTypes.size();
        int selectedThemeId = CPSettings.getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, -1);
        if (selectedThemeId == -1) {
            selectedThemeId = defaultThemeId;
            CPSettings.putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, defaultThemeId);
        }
        String[] likeThemes = getThemeLikes();
        for (int i = 0; i < count; i++) {
            final Type type = themeTypes.get(i);
            if (type.getValue() == Type.NONE) {
                continue;
            }
            final Theme theme = (Theme) type;
            // Avatar
            theme.configAvatar();
            // Like ?
            boolean isLike = isLikeTheme(likeThemes, type.getValue());
            if (isLike) {
                theme.setDownload(theme.getDownload() + 1);
            }
            theme.setLike(isLike);
            // Selected ?
            if (theme.getId() == selectedThemeId) {
                theme.setSelected(true);
            }

            if (type.isMedia()) {
                TasksManager.getImpl().addTask(type);
            }

            mRecyclerViewData.add(theme);

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
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GlideApp.with(ColorPhoneActivity.this).resumeRequests();
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    GlideApp.with(ColorPhoneActivity.this).pauseRequests();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        RecyclerView.RecycledViewPool pool = mRecyclerView.getRecycledViewPool();

        // TODO: set proper view count.
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_LED, 1);
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_TECH, 1);
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO, 2);
        pool.setMaxRecycledViews(ThemeSelectorAdapter.THEME_SELECTOR_ITEM_TYPE_THEME_GIF, 2);

    }

    private void doNotificationAccessToastAnim() {
        notificationToast = findViewById(R.id.notification_access_toast);
        notificationToast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.requestNotificationPermission(ColorPhoneActivity.this, true, new Handler(), "settings");
                HSAnalytics.logEvent("Colorphone_SystemNotificationAccessView_Show", "from", "settings");
                NotificationAutoPilotUtils.logSettingsAlertShow();
                HSAnalytics.logEvent("Colorphone_Settings_NotificationTips_Clicked");
            }
        });
        notificationToast.setVisibility(View.VISIBLE);
        ViewGroup about = findViewById(R.id.settings_about);
        float translationY = Utils.pxFromDp(40) + about.getY() + about.getHeight() + Utils.getNavigationBarHeight(this) - Utils.getPhoneHeight(this);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(notificationToast, "translationY", 0,
                translationY);
        objectAnimator.setDuration(400);
        objectAnimator.start();
        HSPreferenceHelper.getDefault().putBoolean(PREFS_NOTIFICATION_ACCESS_TOAST_HAS_SHOWED, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_main_switch:
                toggle();
                break;
            case R.id.settings_feedback:
                feedBack();
                ColorPhoneApplication.getConfigLog().getEvent().onFeedBackClick();
                break;
            case R.id.settings_setting:
                SettingsActivity.start(this);
                break;
            case R.id.settings_about:
                AboutActivity.start(this);
                break;
        }
    }

    private void feedBack() {
        sentEmail(this, new String[]{Constants.FEED_BACK_EMAIL}, null, null);
    }

    private void toggle() {
        boolean isChecked = mainSwitch.isChecked();
        mainSwitch.setChecked(!isChecked);
    }

    public static void sentEmail(Context mContext, String[] addresses, String subject, String body) {

        try {
            Intent sendIntentGmail = new Intent(Intent.ACTION_VIEW);
            sendIntentGmail.setType("plain/text");
            sendIntentGmail.setData(Uri.parse(TextUtils.join(",", addresses)));
            sendIntentGmail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, addresses);
            if (subject != null) sendIntentGmail.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (body != null) sendIntentGmail.putExtra(Intent.EXTRA_TEXT, body);
            mContext.startActivity(sendIntentGmail);
        } catch (Exception e) {
            //When Gmail App is not installed or disable
            Intent sendIntentIfGmailFail = new Intent(Intent.ACTION_SENDTO);
            sendIntentIfGmailFail.setData(Uri.parse("mailto:")); // only email apps should handle this
            sendIntentIfGmailFail.putExtra(Intent.EXTRA_EMAIL, addresses);
            if (subject != null) sendIntentIfGmailFail.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (body != null) sendIntentIfGmailFail.putExtra(Intent.EXTRA_TEXT, body);
            if (sendIntentIfGmailFail.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.startActivity(sendIntentIfGmailFail);
            }
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s)) {
            mainSwitch.setChecked(true);
        }
    }

}

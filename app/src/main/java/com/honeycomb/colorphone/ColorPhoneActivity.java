package com.honeycomb.colorphone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyRes;
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
import android.widget.CompoundButton;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.preview.ThemePreviewActivity;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;

import static com.acb.call.themes.Type.NEON;
import static com.acb.call.themes.Type.STARS;
import static com.acb.call.themes.Type.SUN;
import static com.acb.call.themes.Type.TECH;


public class ColorPhoneActivity extends HSAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, INotificationObserver {

    private static final String PREFS_THEME_LIKE = "theme_like_array";
    private static final long START_PAGE_TIME = 2000;
    private RecyclerView mRecyclerView;
    private SwitchCompat mainSwitch;
    private TextView mainSwitchTxt;

    private final static int RECYCLER_VIEW_SPAN_COUNT = 2;
    private ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();
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
    private static boolean firstStart = true;
    private View mStartPage;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (firstStart) {
            firstStart = false;
            startFlashPage((ViewGroup) findViewById(R.id.drawer_layout).getParent());
        } else {
            initMainFrame();
        }
    }

    private void startFlashPage(final ViewGroup group) {
        mStartPage = getLayoutInflater().inflate(R.layout.start_page, group, false);
        group.addView(mStartPage, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mStartPage.animate().alpha(0).setDuration(400).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        group.removeViewInLayout(mStartPage);
                    }
                });
            }
        }, START_PAGE_TIME);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initMainFrame();
            }
        }, 600);

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

        ColorPhoneApplication.getConfigLog().getEvent().onMainViewOpen();

        Utils.configActivityStatusBar(this, toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
    protected void onStop() {
        super.onStop();
        if (mStartPage != null) {
            mStartPage.setVisibility(View.GONE);
            mStartPage.animate().cancel();
        }

        if (mainSwitch != null) {
            boolean nowEnable = mainSwitch.isChecked();
            if (nowEnable != initCheckState) {
                initCheckState = nowEnable;
                ColorPhoneApplication.getConfigLog().getEvent().onColorPhoneEnableFromSetting(nowEnable);
            }
        }
        saveThemeLikes();
    }


    private void saveThemeLikes() {
        StringBuilder sb = new StringBuilder(4);
        for (Theme theme : mRecyclerViewData) {
            if (theme.isLike()) {
                sb.append(theme.getThemeId());
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
        Type[] themeTypes = Type.values();
        final int count = themeTypes.length;
        int selectedThemeId = CPSettings.getInt(CPSettings.PREFS_SCREEN_FLASH_THEME_ID, -1);
        if (selectedThemeId == -1) {
            selectedThemeId = defaultThemeId;
            CPSettings.putInt(CPSettings.PREFS_SCREEN_FLASH_THEME_ID, defaultThemeId);
        }
        List<String> hotThemes =  ColorPhoneApplication.getConfigLog().getHotThemeList();
        String[] likeThemes = getThemeLikes();

        Map<String, Map<String,?>> orderMaps = (Map<String, Map<String,?>>) HSConfig.getMap("Application", "Theme", "Extras");

        for (int i = 0; i < count; i++) {
            final Type type = themeTypes[i];
            if(type.getValue() == Type.NONE) {
                continue;
            }
            final Theme theme = new Theme();
            fill(theme, orderMaps.get(type.getIdName()));
            theme.setName(type.getName());
            theme.setThemeId(type.getValue());
            theme.setBackgroundRes(getThemePreviewImage(type));
            if (type.getValue() == Type.TECH) {
                theme.setAvatarName("Alexis");
                theme.setAvatar(R.drawable.acb_phone_theme_default_technological_caller_avatar);
            } else {
                theme.setAvatar(avatars[theme.getIndex() % avatars.length]);
                theme.setAvatarName(avatarNames[theme.getIndex() % avatarNames.length]);
            }

            theme.setHot(isHotTheme(hotThemes, type.getIdName()));
            if (theme.getThemeId() == selectedThemeId) {
                theme.setSelected(true);
            }
            boolean isLike = isLikeTheme(likeThemes, type.getValue());
            if (isLike) {
                theme.setDownload(theme.getDownload() + 1);
            }
            theme.setLike(isLike);
            mRecyclerViewData.add(theme);
            if (type.isGif()) {
                TasksManager.getImpl().addTask(type);
            }

        }

        Collections.sort(mRecyclerViewData, new Comparator<Theme>() {
            @Override
            public int compare(Theme o1, Theme o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });

    }

    private void fill(Theme theme, Map<String, ?> map) {
        theme.setIndex((Integer)map.get(Type.CONFIG_KEY_INDEX));
        theme.setDownload((Integer)map.get(Theme.CONIFG_DOWNLOAD_NUM));
    }

    private static int[] avatars = new int[]{
            R.drawable.female_4,
            R.drawable.male_1,
            R.drawable.female_2,
            R.drawable.female_3,
            R.drawable.male_2,
            R.drawable.female_1,
            R.drawable.male_3,
            R.drawable.male_4,
    };

    private static String[] avatarNames = new String[]{
            "Grace",
            "Jackson",
            "Isabella",
            "Harper",
            "Noah",
            "Emma",
            "Oliver",
            "Lucas",

    };

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

    private boolean isHotTheme(List<String> hotThemes, String name) {
        for (String hotTheme : hotThemes) {
            if (name.equalsIgnoreCase(hotTheme)) {
                return true;
            }
        }
        return false;
    }

    private int getThemePreviewImage(Type type) {
        if (type.getValue() > NEON) {
            return getIdentifier(this, "theme_preview_".concat(type.getIdName().toLowerCase()), "drawable");
        }

        switch (type.getValue()) {
            case NEON:
                return R.drawable.theme_preview_neon;
            case STARS:
                return R.drawable.theme_preview_stars;
            case SUN:
                return R.drawable.theme_preview_sun;
            case TECH:
                return R.drawable.acb_phone_theme_technological_bg;
            default:
                break;
        }
        return 0;
    }

    @AnyRes
    private int getIdentifier(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    private void initRecyclerView() {
        View contentView = findViewById(R.id.recycler_view_content);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(null);
        ThemeSelectorAdapter adapter = new ThemeSelectorAdapter(this, mRecyclerViewData);
        mRecyclerView.setLayoutManager(adapter.getLayoutManager());
        mRecyclerView.setAdapter(adapter);
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
        }
    }

    private void feedBack() {
        sentEmail(this, new String[] {Constants.FEED_BACK_EMAIL}, null, null);
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

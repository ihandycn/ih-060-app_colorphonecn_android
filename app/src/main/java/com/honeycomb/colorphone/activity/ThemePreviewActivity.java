package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.lock.fullscreen.NotchTools;
import com.colorphone.lock.fullscreen.helper.NotchStatusBarUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.preview.ThemeStateManager;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.view.ViewPagerFixed;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Threads;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.ArrayList;
import java.util.List;


public class ThemePreviewActivity extends HSAppCompatActivity {
    public static final String NOTIFY_THEME_SELECT = "notify_theme_select";
    public static final String NOTIFY_THEME_DOWNLOAD = "notify_theme_download";
    public static final String NOTIFY_THEME_KEY = "notify_theme_select_key";
    public static final String NOTIFY_CONTEXT_KEY = "notify_theme_context_key";
    public static final String FROM_MAIN = "notify_theme_context_key";
    public final static String NOTIFY_LIKE_COUNT_CHANGE = "theme_like_count_change";

    private Theme mTheme;
    private ArrayList<Theme> mThemes = new ArrayList<>();
    private ViewPagerFixed mViewPager;
    private View mNavBack;
    private ThemePagerAdapter mAdapter;
    private List<ThemePreviewView> mViews = new ArrayList<>();
    private int scrollCount = 0;
    private int lastPos = -1;

    public static void start(Context context, int position, Bundle options) {
        start(context, position, FROM_MAIN, options);
    }

    public static void start(Context context, int position, String from) {
        start(context, position, from, null);
    }

    private static void start(Context context, int position, String from, Bundle options) {
        Intent starter = new Intent(context, ThemePreviewActivity.class);
        starter.putExtra("position", position);
        starter.putExtra("from", from);
        if (context instanceof Activity) {
            ((Activity)context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        }

        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter, options);
    }


    public List<ThemePreviewView> getViews() {
        return mViews;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemes.addAll(getThemes());
        int pos = getIntent().getIntExtra("position", 0);
        String from = getIntent().getStringExtra("from");
        mTheme = mThemes.get(pos);
        ColorPhoneApplication.getConfigLog().getEvent().onThemePreviewOpen(mTheme.getIdName().toLowerCase());

        // Open music
        ThemeStateManager.getInstance().setAudioMute(false);

        setContentView(R.layout.activity_theme_preview);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            NotchTools.getFullScreenTools().showNavigation(false).fullScreenUseStatus(this);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        mViewPager = (ViewPagerFixed) findViewById(R.id.preview_view_pager);
        mAdapter = new ThemePagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(pos);
        //mViewPager.setCanScroll(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (lastPos != position) {
                    scrollCount++;
                    lastPos = position;
                }
                Ap.DetailAd.onPageScrollOnce();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mNavBack = findViewById(R.id.nav_back);
        mNavBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (mTheme.isLocked()) {
            Analytics.logEvent("Colorphone_Theme_Button_Unlock_show", "themeName", mTheme.getName());
        }
        if (ConfigSettings.showAdOnDetailView() && TextUtils.equals(from, FROM_MAIN)) {
            AdManager.getInstance().preload(this);
            Threads.postOnMainThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!ThemeGuide.isFromThemeGuide()) {
                        Ap.DetailAd.logEvent("colorphone_themedetail_ad_should_show");
                    }
                    boolean show = AdManager.getInstance().showInterstitialAd();
                    if (show) {
                        if (!ThemeGuide.isFromThemeGuide()) {
                            Ap.DetailAd.logEvent("colorphone_themedetail_ad_show");
                        }
                        if (ThemeGuide.isFromThemeGuide()) {
                            Analytics.logEvent("ThemeWireAd_Show_FromThemeGuide");
                        }
                    }
                }
            }, 200);
        }
        ThemeGuide.logThemeDetailShow();

        AcbAds.getInstance().setActivity(this);
        AcbInterstitialAdManager.getInstance().setForegroundActivity(this);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                NotchStatusBarUtils.setFullScreenWithSystemUi(getWindow(),false);
            }
        });
    }

    protected List<Theme> getThemes() {
        return ThemeList.themes();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (ThemePreviewView previewView : mViews) {
            previewView.setBlockAnimationForPageChange(false);
            if (previewView.isSelectedPos()) {
                previewView.onStart();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (ThemePreviewView previewView : mViews) {
            if (previewView.isSelectedPos()) {
                previewView.onStop();
            }
        }
    }

    @Override
    public void onBackPressed() {
        boolean intercept = false;
        for (ThemePreviewView previewView : mViews) {
            if (previewView.isRewardVideoLoading()) {
                previewView.stopRewardVideoLoading();
                intercept = true;
            }
            if (previewView.isRingtoneSettingShow()) {
                previewView.dismissRingtoneSettingPage();
                intercept = true;
            }
            if (previewView.isThemeSettingShow()){
                previewView.returnThemeSettingPage();
                intercept = true;
            }
        }
        if (intercept) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewPager instanceof ViewPagerFixed
                && ((ViewPagerFixed) mViewPager).isCanScroll()) {
            Ap.DetailAd.onPageScroll(scrollCount);
        }
    }

    private class ThemePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mThemes.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ThemePreviewView controller = new ThemePreviewView(ThemePreviewActivity.this);
            controller.init(ThemePreviewActivity.this, mThemes, position, mNavBack);
            controller.setPageSelectedPos(mViewPager.getCurrentItem());
            if (position == mViewPager.getCurrentItem()) {
                controller.setBlockAnimationForPageChange(false);
            } else {
                controller.setNoTransition(true);
            }
            container.addView(controller);
            controller.setTag(position);
            mViews.add(controller);
            mViewPager.addOnPageChangeListener(controller);

            return controller;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mViews.remove(object);
            mViewPager.removeOnPageChangeListener((ViewPager.OnPageChangeListener) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}

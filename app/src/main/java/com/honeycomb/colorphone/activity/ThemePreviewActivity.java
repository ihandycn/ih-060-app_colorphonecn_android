package com.honeycomb.colorphone.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.lock.fullscreen.NotchTools;
import com.colorphone.lock.fullscreen.helper.NotchStatusBarUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ad.AdManager;
import com.honeycomb.colorphone.ad.ConfigSettings;
import com.honeycomb.colorphone.preview.PreviewAdManager;
import com.honeycomb.colorphone.preview.ThemeAdView;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.preview.ThemeStateManager;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.util.AcbNativeAdAnalytics;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.MediaSharedElementCallback;
import com.honeycomb.colorphone.util.TransitionUtil;
import com.honeycomb.colorphone.view.ViewPagerFixed;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
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
    private ThemePagerAdapter mAdapter;
    private List<ThemePreviewView> mViews = new ArrayList<>();
    private int scrollCount = 0;
    private int lastPos = -1;
    private MediaSharedElementCallback mediaSharedElementCallback;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemes.addAll(getThemes());
        int pos = getIntent().getIntExtra("position", 0);
        String from = getIntent().getStringExtra("from");
        mTheme = mThemes.get(pos);
        ColorPhoneApplication.getConfigLog().getEvent().onThemePreviewOpen(mTheme.getIdName().toLowerCase());
        lastThemeFullAdIndex = pos;

        // Open music
        ThemeStateManager.getInstance().resetState();

        setContentView(R.layout.activity_theme_preview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            NotchTools.getFullScreenTools().showNavigation(true).fullScreenUseStatus(this);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    HSLog.d("SharedElement start");
                    for (ThemePreviewView previewView : mViews) {
                        previewView.setBlockAnimationForPageChange(false);
                        if (previewView.isSelectedPos()) {
                            previewView.onWindowTransitionStart();
                        }
                    }
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    HSLog.d("SharedElement end");
                    for (ThemePreviewView previewView : mViews) {
                        previewView.setBlockAnimationForPageChange(false);
                        if (previewView.isSelectedPos()) {
                            previewView.onWindowTransitionEnd();
                        }
                    }
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        }

        mViewPager = findViewById(R.id.preview_view_pager);
        mAdapter = new ThemePagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(pos);
        //mViewPager.setCanScroll(false);

        // Window transition
        mediaSharedElementCallback = new MediaSharedElementCallback();
        ActivityCompat.setEnterSharedElementCallback(this, mediaSharedElementCallback);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            List<Integer> shouldShowAdIndex = new ArrayList<>();
            @Override
            public void onPageSelected(int position) {
                if (lastPos != -1 && isShowThemeFullAd(position + position - lastPos) && mAdapter != null) {
                    shouldShowAdIndex.add(position + position - lastPos);
                    if (PreviewAdManager.getInstance().getNativeAd() != null) {
                        HSLog.i("ThemeFullAd", "onPageSelected addAdView: " + (position + position - lastPos));
                        addAdIndex = position;
                        mAdapter.addAdView();
                        mAdapter.notifyDataSetChanged();
                    }

                    HSLog.i("ThemeFullAd", "ThemeScroll_Ad_Should_Show");
                    Analytics.logEvent("ThemeScroll_Ad_Should_Show");
                }

                if (lastPos != position) {
                    scrollCount++;
                    lastPos = position;
                }
                Ap.DetailAd.onPageScrollOnce();

                if (lastThemeFullAdIndex == position) {
                    HSLog.i("ThemeFullAd", "ThemeScroll_Ad_Show");
                    Analytics.logEvent("ThemeScroll_Ad_Show");
                }

                if (shouldShowAdIndex.contains(position)) {
                    HSLog.i("ThemeFullAd", "AcbAdNative_Viewed_In_App: " + (position == lastThemeFullAdIndex));
                    AcbNativeAdAnalytics.logAppViewEvent(Placements.THEME_DETAIL_NATIVE, (position == lastThemeFullAdIndex));
                    shouldShowAdIndex.remove(Integer.valueOf(position));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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

        PreviewAdManager.getInstance().setEnable(HSConfig.optBoolean(true, "Application", "Theme", "ScrollShowAds"));
        PreviewAdManager.getInstance().preload(this);
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


    @TargetApi(22)
    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        int index = getThemeIndexByPosition(mViewPager.getCurrentItem());
        data.putExtra("index", index);
        setResult(RESULT_OK, data);
        super.supportFinishAfterTransition();
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
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewPager != null
                && mViewPager.isCanScroll()) {
            Ap.DetailAd.onPageScroll(scrollCount);
        }
    }

    private int lastThemeFullAdIndex = -1;
    private int addAdIndex = -1;
    private List<Integer> themeFullAdIndexList = new ArrayList<>();

    private boolean isShowThemeFullAd(int position) {
        if (HSConfig.optBoolean(true, "Application", "Theme", "ScrollShowAds") && position >= 2) {
            int cha = Math.abs(position - lastThemeFullAdIndex);
            int interval = HSConfig.optInteger(4, "Application", "Theme", "CallThemeIntervalShowAd");
            HSLog.i("ThemeFullAd", "isShowThemeFullAd cha: " + cha + "  interval: " + interval);
            return cha > interval;
        }
        return false;
    }

    private int getThemeIndexByPosition(int position) {
        int themeIndex = position;
        if (themeFullAdIndexList.size() > 0) {
            for (int i : themeFullAdIndexList) {
                if (position > i && addAdIndex < i) {
                    themeIndex--;
                } else if (position < i && addAdIndex > i) {
                    themeIndex++;
                }
            }
        }

        themeIndex = Math.max(0, Math.min(themeIndex, mThemes.size() - 1));
        HSLog.i("ThemeFullAd", "getThemeIndexByPosition index: " + themeIndex + "  pos: " + position);
        return themeIndex;
    }

    private class ThemePagerAdapter extends PagerAdapter {
        private int adCount = 0;
        public void addAdView() {
            adCount++;
        }

        public void removeAdView() {
            adCount--;
        }

        @Override
        public int getCount() {
            return mThemes.size() + adCount;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View page;
            if (isShowThemeFullAd(position) && PreviewAdManager.getInstance().getNativeAd() != null) {
                ThemeAdView adView = new ThemeAdView(ThemePreviewActivity.this);
                adView.init(ThemePreviewActivity.this, position, null);
                page = adView;
                themeFullAdIndexList.add(position);
                lastThemeFullAdIndex = position;
                HSLog.i("ThemeFullAd", "instantiateItem ThemeAdView: " + position);
            } else {
                int themeIndex = getThemeIndexByPosition(position);

                HSLog.i("ThemeFullAd", "instantiateItem ThemePreviewView: " + position + "  index: " + themeIndex);
                ThemePreviewView controller = new ThemePreviewView(ThemePreviewActivity.this);
                controller.init(ThemePreviewActivity.this, mThemes.get(themeIndex), position, null);
                controller.setPageSelectedPos(mViewPager.getCurrentItem());
                if (position == mViewPager.getCurrentItem()) {
                    controller.setBlockAnimationForPageChange(false);
                    controller.setWindowInTransition(true);
                } else {
                    controller.setNoTransition(true);
                }
                page = controller;
                mViews.add(controller);
                mViewPager.addOnPageChangeListener(controller);
            }

            container.addView(page);
            page.setTag(position);

            return page;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mViews.remove(object);
            mViewPager.removeOnPageChangeListener((ViewPager.OnPageChangeListener) object);

            HSLog.i("ThemeFullAd", "destroyItem releaseAD: " + (lastThemeFullAdIndex == position));
            if (lastThemeFullAdIndex == position && themeFullAdIndexList.contains(position)) {
                themeFullAdIndexList.remove(Integer.valueOf(position));
                removeAdView();
                PreviewAdManager.getInstance().releaseNativeAd();
                int current = mViewPager.getCurrentItem();
                if (current > position && addAdIndex < position) {
                    current--;
                    lastThemeFullAdIndex--;
                } else if (current < position && addAdIndex > position) {
                    current++;
                    lastThemeFullAdIndex++;
                }
                mViewPager.setAdapter(null);
                mViewPager.setAdapter(this);

                mViewPager.setCurrentItem(current);
            }
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View itemView = (View) object;
            if (itemView instanceof ThemePreviewView) {
                int index = getThemeIndexByPosition(position);

                ViewCompat.setTransitionName(itemView.findViewById(R.id.ringtone_image),
                        TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREIVIEW_RINTONE, mThemes.get(index)));
                ViewCompat.setTransitionName(mViewPager,
                        TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, mThemes.get(index)));
                mediaSharedElementCallback.setSharedElementViews(mViewPager);
            } else {
                ViewCompat.setTransitionName(mViewPager, "");
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}

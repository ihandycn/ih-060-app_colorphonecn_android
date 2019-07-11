package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;


/**
 * Created by zhe.wang on 19/7/2.
 */

// TODO : clean Theme & Ringtone logic
public class ThemeAdView extends FrameLayout implements ViewPager.OnPageChangeListener, INotificationObserver {

    private static final String TAG = ThemePreviewWindow.class.getSimpleName();

    private static final boolean DEBUG_LIFE_CALLBACK = true & BuildConfig.DEBUG;

    private static final long ANIMATION_DURATION = 300;
    private static final long CHANGE_MODE_DURTION = 200;

    private ThemePreviewActivity mActivity;

    private View mNavBack;

    private static final int THEME_ENJOY_FOLDING = 1;
    private static final int NAV_VISIBLE = 0;
    private TextView mThemeTitle;

    public static boolean ifShowThemeApplyView = false;
    public static boolean isSelected = false;
    private TextView mEnjoyApplyBtn;
    private LottieAnimationView mThemeLikeAnim;

    private int foldingOrNot = THEME_ENJOY_FOLDING;
    public static int navFadeInOrVisible = NAV_VISIBLE;
    private RelativeLayout mEnjoyThemeLayout;

    private Interpolator mInter;

    private int mPosition = -1;
    private int mPageSelectedPos = -1;
    /**
     * Play no Transition animation when page scroll.
     * TODO remove Call views
     */
    private boolean mNoTransition = false;
    private boolean triggerPageChangeWhenIdle = false;
    /**
     * Normally, We block animation until page scroll idle, but
     * 1 first time that theme view show
     * 2 activity pause or resume
     * in those two conditions we start animation directly.
     */
    private boolean mBlockAnimationForPageChange = true;
    private boolean hasStopped;
    private boolean resumed;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    return false;

            }
        }
    });

    private StateChangeObserver observer = new StateChangeObserver() {
        @Override
        public void onReceive(int themeMode) {

        }
    };

    private boolean mWaitContactResult;
    private boolean mWaitForAll;
    private boolean mWindowInTransition;
    private boolean mPendingResume;

    public ThemeAdView(@NonNull Context context) {
        super(context);
    }

    public ThemeAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeAdView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(ThemePreviewActivity activity, int position, View navBack) {
        mActivity = activity;
        mPosition = position;
        mNavBack = navBack;

        activity.getLayoutInflater().inflate(R.layout.page_theme_ad_page, this, true);

        onCreate();
    }

    public void dismissRingtoneSettingPage() {
        mEnjoyApplyBtn.setVisibility(VISIBLE);
    }

    protected void onCreate() {

        mNavBack = findViewById(R.id.nav_back);
        mNavBack.setOnClickListener(v -> mActivity.onBackPressed());

        mInter = new AccelerateDecelerateInterpolator();

        initNativeAdView();
    }


    FrameLayout adLayout;
    private void initNativeAdView() {
        adLayout = findViewById(R.id.preview_ad_layout);
        View adView = LayoutInflater.from(getContext()).inflate(R.layout.page_theme_ad_layout, adLayout, false);

        onFinishInflateResultView(adView);
    }

    private void changeModeToEnjoy() {
        mEnjoyThemeLayout.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mEnjoyThemeLayout.setVisibility(VISIBLE);
                    }
                })
                .start();
        mEnjoyApplyBtn.animate().alpha(1)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mEnjoyApplyBtn.setVisibility(VISIBLE);
                    }
                })
                .start();
    }

    private void showNavView(boolean show) {
        float offsetX = Dimensions.isRtl() ?  -Dimensions.pxFromDp(60) : Dimensions.pxFromDp(60);
        float targetX = show ? 0 : -offsetX;
        // State already right.
        if (Math.abs(mNavBack.getTranslationX() - targetX) <= 1) {
            return;
        }
        if (isSelectedPos()) {
            mNavBack.animate().translationX(targetX)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(mInter)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (show == true) {
                                mNavBack.setVisibility(VISIBLE);
                            }
                        }
                    })
                    .start();
        } else {
            mNavBack.setTranslationX(targetX);
        }
    }

    public void onStart() {
        changeModeToEnjoy();
    }

    public void onStop() {

        hasStopped = true;

        mHandler.removeCallbacksAndMessages(null);
    }

    public boolean isSelectedPos() {
        return mPosition == mPageSelectedPos;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        // Not called !!
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d("onVisibilityChanged = " + (visibility == VISIBLE));
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onAttachedToWindow");
        }
        super.onAttachedToWindow();
        onStart();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onDetachedFromWindow");
        }
        onStop();

        super.onDetachedFromWindow();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d("onPageSelected " + position);
        }
        mPageSelectedPos = position;

        boolean isCurrentPageActive = isSelectedPos();

        triggerPageChangeWhenIdle = true;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d("onPageScrollStateChanged " + state
                    + ", curSelect: " + mPageSelectedPos + ", trigger change: " + triggerPageChangeWhenIdle);
        }

        if (state == ViewPager.SCROLL_STATE_IDLE && triggerPageChangeWhenIdle) {
            triggerPageChangeWhenIdle = false;
        }
    }

    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;

    protected void onFinishInflateResultView(View resultView) {

        mEnjoyThemeLayout = resultView.findViewById(R.id.enjoy_layout);
        mThemeTitle = resultView.findViewById(R.id.description_title_tv);
        mEnjoyApplyBtn = resultView.findViewById(R.id.result_action_btn);
        mThemeLikeAnim = resultView.findViewById(R.id.like_count_icon);

        mAdImageContainer = resultView.findViewById(R.id.result_image_container_ad);

        AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
        adContainer.addContentView(resultView);
        adContainer.setAdTitleView(mThemeTitle);
        adContainer.setAdPrimaryView(mAdImageContainer);
        adContainer.setAdActionView(mEnjoyApplyBtn);

        adLayout.addView(adContainer);
        mAdContainer = adContainer;

        AcbNativeAd ad = PreviewAdManager.getInstance().getNativeAd();
        ad.indeedNeedShowFullAd(mAdContainer);
        ad.setMuted(false);
        mAdContainer.fillNativeAd(ad);
        PreviewAdManager.getInstance().preload(null);

        if (HSConfig.optBoolean(true, "Application", "Theme", "ScrollFullScreenAdClick")) {
            mAdContainer.setOnClickListener(v -> mEnjoyApplyBtn.performClick());
        }
    }

}

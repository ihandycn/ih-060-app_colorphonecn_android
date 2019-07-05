package com.honeycomb.colorphone.preview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
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
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

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
    private static final long ANIMATION_DURMATION_DELAY = 1000;
    private static final long CHANGE_MODE_DURTION = 200;
    private static final long WINDOW_ANIM_DURATION = 400;
    private static final int TRANS_IN_DURATION = 400;

    private static final int IMAGE_WIDTH = 1080;
    private static final int IMAGE_HEIGHT = 1920;

    private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

    private ThemePreviewActivity mActivity;
    private NetworkChangeReceiver networkChangeReceiver;
    private IntentFilter intentFilter;

    private View mNavBack;

    private AcbNativeAdPrimaryView previewImage;
    private FrameLayout adContainer;

    private static final int THEME_ENJOY_UNFOLDING = 0;
    private static final int THEME_ENJOY_FOLDING = 1;
    public static final int NAV_FADE_IN = 1;
    private static final int NAV_VISIBLE = 0;
    private TextView mThemeLikeCount;
    private TextView mThemeTitle;
    private FrameLayout rootView;

    public static boolean ifShowThemeApplyView = false;
    public static boolean isSelected = false;
    private TextView mEnjoyApplyBtn;
    private LottieAnimationView mThemeLikeAnim;

    private int foldingOrNot = THEME_ENJOY_FOLDING;
    public static int navFadeInOrVisible = NAV_VISIBLE;
    private RelativeLayout mEnjoyThemeLayout;

    /**
     * If button is playing animation
     */
    private boolean inTransition;
    private boolean themeReady;

    private long animationDelay = 500;
    private float bottomBtnTransY;
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

        rootView = findViewById(R.id.root_view);

//        mEnjoyThemeLayout = findViewById(R.id.enjoy_layout);
//        mThemeTitle = findViewById(R.id.description_title_tv);
////        mThemeTitle.setText(mTheme.getName());
//        mThemeLikeCount = findViewById(R.id.collect_num);
//        mEnjoyApplyBtn = findViewById(R.id.result_action_btn);
//        mThemeLikeAnim = findViewById(R.id.like_count_icon);
//
//        // set background
//        expandViewTouchDelegate(mThemeLikeAnim, Dimensions.pxFromDp(10), Dimensions.pxFromDp(37), Dimensions.pxFromDp(30), Dimensions.pxFromDp(72));
//

        mInter = new AccelerateDecelerateInterpolator();

        initNativeAdView();
    }


    FrameLayout adLayout;
    private void initNativeAdView() {
        adLayout = findViewById(R.id.preview_ad_layout);
        View adView = LayoutInflater.from(getContext()).inflate(R.layout.page_theme_ad_layout, adLayout, false);

        onFinishInflateResultView(adView);

//        adContainer = new AcbNativeAdContainerView(getContext());
//        previewImage = new AcbNativeAdPrimaryView(getContext());
//        previewImage.setImageViewScaleType(ImageView.ScaleType.CENTER_CROP);
//        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        adContainer.addView(previewImage, param);
//        adContainer.addContentView(layout);
//        adContainer.setAdTitleView(mThemeTitle);
//        adContainer.setAdPrimaryView(previewImage);
//        adContainer.setAdActionView(mEnjoyApplyBtn);
//        layout.addView(adContainer);
    }

    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }


    private static void setLottieProgress(LottieAnimationView animationView, float v) {
        if (animationView.getProgress() != v) {
            animationView.setProgress(v);
        }
    }

    private void setPreviewView() {
        mNavBack.setVisibility(GONE);
        changeModeToPreview();
    }

    private void changeModeToPreview() {
        mEnjoyThemeLayout.animate().alpha(0)
                .setDuration(CHANGE_MODE_DURTION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mEnjoyThemeLayout.setVisibility(GONE);
                    }
                })
                .start();
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

    private Interpolator getmInterForTheme() {
        Interpolator mInterForTheme = PathInterpolatorCompat.create(0.175f, 0.885f, 0.32f, 1.275f);
        return mInterForTheme;
    }


    private void mActionLayoutfadeInView() {
    }


    private void playButtonAnimation() {
        if (navIsShow()) {
            if (mNoTransition) {
                fadeInActionViewImmediately();
            } else {
                //fadeInActionView();
                if (needShowRingtoneSetButton()) {
                    showNavView(false);
                    fadeOutActionView();
                    navFadeInOrVisible = NAV_FADE_IN;
                    mWaitContactResult = false;
                }
            }
        } else {
            fadeOutActionViewImmediately();
        }
    }

    private boolean navIsShow() {
        return Math.abs(mNavBack.getTranslationX()) <= 1;
    }

    private CharSequence getString(int id) {
        return mActivity.getString(id);
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

    private void fadeInActionView() {
        fadeInActionView(true);
    }

    public void fadeInActionViewImmediately() {
        fadeInActionView(false);
    }

    private void fadeInActionView(boolean anim) {
        if (needShowRingtoneSetButton()) {
            showNavView(false);
            fadeOutActionView();
            navFadeInOrVisible = NAV_FADE_IN;
            mWaitContactResult = false;
            return;
        }

        if (anim) {
            int mActionLayoutHeight = Dimensions.pxFromDp(60);
            inTransition = true;
        } else {
            if (themeReady) {
                onActionButtonReady();
            }
        }
    }

    private boolean needShowRingtoneSetButton() {
        return false;
    }

    private void fadeOutActionView(boolean anim) {
        if (anim) {
            inTransition = true;
        }
    }

    private void fadeOutActionView() {
        fadeOutActionView(true);
    }

    public void fadeOutActionViewImmediately() {
        fadeOutActionView(false);
    }

    public void onActionButtonReady() {
    }

    public void onStart() {
        changeModeToEnjoy();
    }

    public void onStop() {

        hasStopped = true;
        pauseAnimation();

        mHandler.removeCallbacksAndMessages(null);
    }

    private void pauseAnimation() {
        if (themeReady) {
            resumed = false;
        }
    }

    private void resumeAnimation() {
        if (mWindowInTransition) {
            mPendingResume = true;
            return;
        }

        if (themeReady) {
            resumed = true;
        }
    }

    public boolean isSelectedPos() {
        return mPosition == mPageSelectedPos;
    }

    public void setPageSelectedPos(int pos) {
        mPageSelectedPos = pos;
    }

    private void registerForInternetChange() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();

        getContext().registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unregisterForInternetChange() {
        getContext().unregisterReceiver(networkChangeReceiver);
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
        HSGlobalNotificationCenter.addObserver(StartGuideActivity.NOTIFICATION_PERMISSION_GRANT, this);
        registerForInternetChange();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG_LIFE_CALLBACK) {
            HSLog.d(" onDetachedFromWindow");
        }
        unregisterForInternetChange();
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
            if (isSelectedPos()) {
                resumeAnimation();
            } else {
                HSLog.d("onPageUnSelected " + mPosition);
                pauseAnimation();
            }
        }
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        }
    }

    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;
    private View mAdChoice;

    protected void onFinishInflateResultView(View resultView) {

        mEnjoyThemeLayout = resultView.findViewById(R.id.enjoy_layout);
        mThemeTitle = resultView.findViewById(R.id.description_title_tv);
//        mThemeTitle.setText(mTheme.getName());
        mThemeLikeCount = resultView.findViewById(R.id.collect_num);
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
        mAdContainer.fillNativeAd(PreviewAdManager.getInstance().getNativeAd());
        PreviewAdManager.getInstance().preload(null);
    }

}

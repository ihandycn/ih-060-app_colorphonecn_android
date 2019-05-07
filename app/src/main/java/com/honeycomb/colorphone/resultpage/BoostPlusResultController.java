package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.graphics.drawable.ClipDrawable;
import android.os.Handler;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ViewUtils;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;


@SuppressWarnings("WeakerAccess")
class BoostPlusResultController extends ResultController {

    private static final int PERCENT_FROM_Y_DELTA = 156;
    private static final int BOOST_FROM_Y_DELTA = 216;

    public static final int DEVICE_SCREEN_HEIGHT_TAG = 1920;
    public static final long START_OFF_CIRCLE_ROTATE_MAIN = 0;

    // Result percentage
    private static final long DURATION_RESULT_START_OFF = START_OFF_CIRCLE_ROTATE_MAIN + 7 * FRAME;
    private static final long DURATION_RESULT_PERCENT_ALPHA_ADD = 5 * FRAME;

    // Result boosted
    private static final long DURATION_RESULT_BOOSTED_ALPHA_ADD = 8 * FRAME;

    private static final int TICK_LEVEL_ACCELERATE = 60;
    private static final int TICK_BG_LEVEL_ACCELERATE = 80;

    // Tick
    private static final long DURATION_TICK = 9 * FRAME;
    private static final long START_OFFSET_TICK = 3 * FRAME;
    private static final long START_OFFSET_MAIN_TICK = START_OFF_CIRCLE_ROTATE_MAIN;
    private static final int CLIP_LEVEL_TICK_BG_START = 1000;
    private static final int CLIP_LEVEL_TICK_START = 100;
    private static final int CLIP_LEVEL_TICK_BG_END = 9000;
    private static final int CLIP_LEVEL_TICK_END = 9500;
    private static final int CLIP_MAX_LEVEL = 10000;
    private static final int CLIP_BG_TIMES = 40;
    private static final int CLIP_TICK_TIMES = 40;
    private static final long CLIP_INTERVAL_BG = DURATION_TICK / CLIP_BG_TIMES;
    private static final long CLIP_INTERVAL_TICK = DURATION_TICK / CLIP_TICK_TIMES;
    private static final int TICK_BG_LEVEL_INTERVAL = (CLIP_LEVEL_TICK_BG_END - CLIP_LEVEL_TICK_BG_START) / CLIP_BG_TIMES;
    private static final int TICK_LEVEL_INTERVAL = (CLIP_LEVEL_TICK_END - CLIP_LEVEL_TICK_START) / CLIP_TICK_TIMES;

    private static final long DURATION_FADE_OUT = 200;
    private static final long DURATION_SLIDE_OUT = 400;
    private static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 640;

    private int mCleanedSizeMbs;

    private View mTitleAnchor;
    RelativeLayout mTickRl;
    BoostBgImageView mTickBgIv;
    ImageView mTickIv;
    TextView mOptimalTv;
    TextView mFreedUpNumberTv;
    private TextView mFreedUpTv;

    ClipDrawable mBoostTickClipDrawable;
    ClipDrawable mBoostTickBgClipDrawable;

     int mTickLevelInterval = TICK_LEVEL_INTERVAL;
     int mTickLevelBgInterval = TICK_BG_LEVEL_INTERVAL;

     boolean mIsTickBgFirstStart = true;
     boolean mIsTickFirstStart = true;

     Handler mHandler = new Handler();

    private View[] mSlideOutViews;
    private View[] mFadeOutViews;
    private boolean isAdReady;
    private View mFreedResultBtn;
    private Runnable mAdTransitionRunnable;


    BoostPlusResultController(ResultPageActivity activity, int resultType, int cleanedSizeMbs, Type type, List<CardData> cardDataList) {
        mCleanedSizeMbs = cleanedSizeMbs;
        HSLog.d(TAG, "BoostPlusResultController ***");
        super.init(activity, resultType, type, cardDataList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_boost_plus_transition;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        HSLog.d(TAG, "BoostPlusResultController onFinishInflateTransitionView");
        mTitleAnchor = ViewUtils.findViewById(transitionView, R.id.description_title_tag_tv);
        mTickRl = ViewUtils.findViewById(transitionView, R.id.tick_rl);
        mTickBgIv = ViewUtils.findViewById(transitionView, R.id.tick_bg);
        mTickIv = ViewUtils.findViewById(transitionView, R.id.tick_iv);
        mOptimalTv = ViewUtils.findViewById(transitionView, R.id.optimal_tv);
        mFreedUpNumberTv = ViewUtils.findViewById(transitionView, R.id.freed_up_number_tv);
        mFreedUpTv = ViewUtils.findViewById(transitionView, R.id.freed_up_tv);
        mFreedResultBtn = ViewUtils.findViewById(transitionView, R.id.freed_up_action_btn);

        mSlideOutViews = new View[]{mTickBgIv, mTickIv};
        mFadeOutViews = new View[]{mFreedUpNumberTv, mFreedUpTv};

        mBoostTickClipDrawable = (ClipDrawable) mTickIv.getDrawable();
        mBoostTickBgClipDrawable = (ClipDrawable) mTickBgIv.getDrawable();
    }

    @Override
    protected boolean onStartTransitionAnimation(View transitionView) {
        HSLog.d(TAG, "BoostPlusResultController onStartTransitionAnimation mTransitionView = " + transitionView);
        startCleanResultSizeAnimation();

        if (!popupInterstitialAdIfNeeded()) {
            if (!tryShowNativeAd(true)) {
                HSLog.d(TAG, "BoostPlusResultController NoAds here");
                startTickAnimation();
            }
        }

        return true;
    }

    @Override protected void onInterruptActionClosed() {
        HSLog.d(TAG, "BoostPlusResultController onInterruptActionClosed");
        if (!tryShowNativeAd()) {
            startTickAnimation();
        }
    }

    public boolean tryShowNativeAd() {
        return tryShowNativeAd(false);
    }

    public boolean tryShowNativeAd(boolean waitForBoostResult) {
        if (mResultType == ResultConstants.RESULT_TYPE_BOOST_TOOLBAR) {
            LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Should_Shown_FromToolbar");
        } else if (mResultType == ResultConstants.RESULT_TYPE_BOOST_PLUS) {
            LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Should_Shown_FromSettings");
        } else if (mResultType == ResultConstants.RESULT_TYPE_BOOST_PUSH) {
            LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Should_Shown_FromPush");
        }
        final AcbNativeAd ad = ResultPageManager.getInstance().getAd();
        isAdReady = ad != null;

        HSLog.d(TAG, "BoostPlusResultController showAdWithAnimation isAdReady == " + isAdReady);
        if (ad != null) {
            if (waitForBoostResult) {
                mAdTransitionRunnable = new Runnable() {
                    @Override
                    public void run() {
                        resetTextVisible();

                        if (isAdReady) {
                            showAd(ad);
                            showAdWithAnimation();
                            startRealTransitionAnimation();
                        } else {
                            startTickAnimation();
                        }
                    }
                };
                mHandler.postDelayed(mAdTransitionRunnable, 250);
            } else {
                resetTextVisible();
                showAd(ad);
                showAdWithAnimation();
                startRealTransitionAnimation();
            }
            return true;
        } else {
            return false;
        }
    }

    public void resetTextVisible() {
        mOptimalTv.setVisibility(View.VISIBLE);
    }

    private void startCleanResultSizeAnimation() {
        HSLog.d(TAG, "BoostPlusResultController startCleanResultSizeAnimation");
        String cleanPercentRandomText = mCleanedSizeMbs + getContext().getString(R.string.megabyte_abbr);

        mFreedUpNumberTv.setText(cleanPercentRandomText);

        float percentFromYDelta = mScreenHeight * PERCENT_FROM_Y_DELTA / DEVICE_SCREEN_HEIGHT_TAG;
        Animation cleanPercentAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(
                DURATION_RESULT_PERCENT_ALPHA_ADD, DURATION_RESULT_START_OFF);
        Runnable resultRunnable = new Runnable() {
            @Override
            public void run() {
                mFreedUpNumberTv.setVisibility(View.VISIBLE);
            }
        };
        mHandler.postDelayed(resultRunnable, DURATION_RESULT_START_OFF);

        Animation cleanPercentTranslateAnimation = LauncherAnimationUtils.getTranslateYAnimation(
                percentFromYDelta, 0, DURATION_RESULT_PERCENT_ALPHA_ADD,
                DURATION_RESULT_START_OFF, true, new DecelerateInterpolator());
        LauncherAnimationUtils.startSetAnimation(mFreedUpNumberTv, new LauncherAnimationUtils.AnimationListenerAdapter(){
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                HSLog.d(TAG, "BoostPlusResultController mAdTransitionRunnable == " + mAdTransitionRunnable);
                if (mAdTransitionRunnable == null) {
                    startRealTransitionAnimation();
                }
            }
        }, cleanPercentAlphaAppearAnimation, cleanPercentTranslateAnimation);

        float boostFromYDelta = mScreenHeight * BOOST_FROM_Y_DELTA / DEVICE_SCREEN_HEIGHT_TAG;
        Animation cleanBoostAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(
                DURATION_RESULT_BOOSTED_ALPHA_ADD, DURATION_RESULT_START_OFF);
        Animation cleanBoostTranslateAnimation = LauncherAnimationUtils.getTranslateYAnimation(
                boostFromYDelta, 0, DURATION_RESULT_PERCENT_ALPHA_ADD, DURATION_RESULT_START_OFF,
                true, new DecelerateInterpolator());
        LauncherAnimationUtils.startSetAnimation(mFreedUpTv, false,
                cleanBoostAlphaAppearAnimation, cleanBoostTranslateAnimation);
    }

    private void startRealTransitionAnimation() {
        HSLog.d(TAG, "BoostPlusResultController startRealTransitionAnimation isAdReady == " + isAdReady);
        if (!isAdReady) {
            mFreedResultBtn.setAlpha(0);
            mFreedResultBtn.setVisibility(View.VISIBLE);
            mFreedResultBtn.animate().alpha(1).setDuration(200).start();
            mFreedResultBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mActivity != null && !mActivity.isFinishing()) {
                        mActivity.finish();
                    }
                }
            });
            LauncherAnalytics.logEvent("Colorphone_BoostDone_Page_Optimal_Shown");
            return;
        }
        for (final View v : mFadeOutViews) {
            v.animate()
                    .alpha(0f)
                    .setDuration(DURATION_FADE_OUT)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }

        int[] location = new int[2];
        mTickBgIv.getLocationInWindow(location);
        int slideUpTranslation = location[1] + mTickBgIv.getHeight();

        for (View v : mSlideOutViews) {
            v.animate()
                    .translationYBy(-slideUpTranslation)
                    .alpha(0f)
                    .setDuration(DURATION_SLIDE_OUT)
                    .setInterpolator(LauncherAnimUtils.ACCELERATE_QUAD)
                    .start();
        }

        mOptimalTv.getLocationInWindow(location);
        int oldOptimalTvCenterY = location[1] + mOptimalTv.getHeight() / 2;
        mTitleAnchor.getLocationInWindow(location);
        int newOptimalTvCenterY = location[1] + mTitleAnchor.getHeight() / 2;

        TimeInterpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.79f, 0.37f, 0.28f, 1f);
        mOptimalTv.animate()
                .translationYBy(newOptimalTvCenterY - oldOptimalTvCenterY)
                .scaleX(1.8f)
                .scaleY(1.8f)
                .setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                .setInterpolator(softStopAccDecInterpolator)
                .start();
    }

    private void startTickAnimation() {
        HSLog.d(TAG, "BoostPlusResultController startTickAnimation");
        mIsTickBgFirstStart = true;
        Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                mTickRl.setVisibility(View.VISIBLE);
                int currentLevel = mBoostTickClipDrawable.getLevel() + mTickLevelInterval;
                mTickLevelInterval += TICK_LEVEL_ACCELERATE;
                if (mIsTickFirstStart) {
                    currentLevel = CLIP_LEVEL_TICK_START;
                }
                mIsTickFirstStart = false;

                if (mBoostTickClipDrawable.getLevel() < CLIP_LEVEL_TICK_END) {
                    mHandler.postDelayed(this, CLIP_INTERVAL_TICK);
                } else {
                    currentLevel = CLIP_MAX_LEVEL;
                }

                mBoostTickClipDrawable.setLevel(currentLevel);

                float currentAlpha = (float) currentLevel / CLIP_MAX_LEVEL;
                mTickIv.setAlpha(currentAlpha);
            }
        };

        Runnable tickBgRunnable = new Runnable() {
            @Override
            public void run() {
                mTickRl.setVisibility(View.VISIBLE);
                int currentLevel = mBoostTickBgClipDrawable.getLevel() + mTickLevelBgInterval;
                mTickLevelBgInterval += TICK_BG_LEVEL_ACCELERATE;
                if (mIsTickBgFirstStart) {
                    // optimal alpha appear animation
                    Animation optimalAlphaAppearAnimation = LauncherAnimationUtils.getAlphaAppearAnimation(DURATION_TICK, 0);
                    LauncherAnimationUtils.startAnimation(mOptimalTv, false, optimalAlphaAppearAnimation);
                    currentLevel = CLIP_LEVEL_TICK_BG_START;
                }
                mIsTickBgFirstStart = false;

                if (mBoostTickBgClipDrawable.getLevel() < CLIP_LEVEL_TICK_BG_END) {
                    mHandler.postDelayed(this, CLIP_INTERVAL_BG);
                } else {
                    currentLevel = CLIP_MAX_LEVEL;
                }
                mBoostTickBgClipDrawable.setLevel(currentLevel);

                float currentAlpha = (float) currentLevel / CLIP_MAX_LEVEL;
                mTickBgIv.setAlpha(currentAlpha);
            }
        };

        mHandler.postDelayed(tickRunnable, START_OFFSET_MAIN_TICK);
        mHandler.postDelayed(tickBgRunnable, START_OFFSET_MAIN_TICK + START_OFFSET_TICK);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        logClickEvent(mType);
    }
}

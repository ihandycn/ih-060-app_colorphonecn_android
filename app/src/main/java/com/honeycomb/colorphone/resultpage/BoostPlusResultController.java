package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.AnimatorListenerAdapter;
import com.colorphone.lock.util.CommonUtils;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Thunk;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

@SuppressWarnings("WeakerAccess") class BoostPlusResultController extends ResultController {

    private int mCleanedSizeMbs;

    @Thunk
    Handler mHandler = new Handler();

    private View sizeContainer;
    private TextView sizeTv;
    private TextView unitTv;
    private TextView freeUpTv;

    private ImageView leftStar;
    private ImageView middleStar;
    private ImageView rightStar;

    private ImageView rocket;

    private TextView optimalTv;
    private TextView titleAnchor;
    private LinearLayout mMiddleLayout;

    private float phoneHeight;

    BoostPlusResultController(ResultPageActivity activity, int cleanedSizeMbs, Type type, AcbInterstitialAd interstitialAd, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        super.init(activity, ResultConstants.RESULT_TYPE_BOOST_PLUS, type, interstitialAd, ad, cardDataList);
        HSLog.d(TAG, "BoostPlusResultController ***");
        mCleanedSizeMbs = cleanedSizeMbs;
        if (ad != null) {
            LauncherAnalytics.logEvent(AdPlacements.AD_RESULT_PAGE, "Type", "BoostPlusDone");
            ad.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override public void onAdClick(AcbAd acbAd) {
                    LauncherAnalytics.logEvent(AdPlacements.AD_RESULT_PAGE, "Type", "BoostPlusDone");
                    LauncherAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.AD);
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_transition_boost_plus;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        HSLog.d(TAG, "BoostPlusResultController onFinishInflateTransitionView");

        sizeContainer = ViewUtils.findViewById(transitionView, R.id.size_container);
        sizeTv = ViewUtils.findViewById(transitionView, R.id.label_title_size);
        unitTv = ViewUtils.findViewById(transitionView, R.id.label_title_unit);
        freeUpTv = ViewUtils.findViewById(transitionView, R.id.label_title_free_up);

        leftStar = ViewUtils.findViewById(transitionView, R.id.label_star_left);
        middleStar = ViewUtils.findViewById(transitionView, R.id.label_star_middle);
        rightStar = ViewUtils.findViewById(transitionView, R.id.label_star_right);

        rocket = ViewUtils.findViewById(transitionView, R.id.label_rocket);

        optimalTv = ViewUtils.findViewById(transitionView, R.id.label_title);
        titleAnchor = ViewUtils.findViewById(transitionView, R.id.anchor_title_tv);
        mMiddleLayout = ViewUtils.findViewById(transitionView, R.id.middle_layout);
        mMiddleLayout.post(new Runnable() {
            @Override public void run() {
                int height = mMiddleLayout.getHeight();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMiddleLayout.getLayoutParams();
                params.topMargin = (CommonUtils.getPhoneHeight(BoostPlusResultController.this.getContext()) - height) / 2;
                mMiddleLayout.setLayoutParams(params);
            }
        });

        phoneHeight = (float) CommonUtils.getPhoneHeight(getContext());
    }

    @Override
    protected void onStartTransitionAnimation(View transitionView) {
        HSLog.d(TAG, "BoostPlusResultController onStartTransitionAnimation mTransitionView = " + transitionView);

        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                BoostPlusResultController.this.startAnimation();
            }
        }, 200);
    }

    private void startAnimation() {
        final boolean shouldDisplaySize = mCleanedSizeMbs > 0;
//        JunkCleanUtils.setPositiveFeedDisplayType(JunkCleanUtils.POSITIVE_FEEDBACK_DISPLAY_TYPE_OPTIMAL);

        // text appear
        if (shouldDisplaySize) {
            sizeTv.setText(String.valueOf(mCleanedSizeMbs));
            unitTv.setText("MB");
            playTextAppearAnimation(sizeContainer, 2 * FRAME_HALF);
            playTextAppearAnimation(freeUpTv, 4 * FRAME_HALF);
        } else {
            playTextAppearAnimation(optimalTv, 2 * FRAME_HALF);
        }

        // star rotation
        playStarRotationAnimation(middleStar, 2 * FRAME_HALF);
        playStarRotationAnimation(leftStar, 4 * FRAME_HALF);
        playStarRotationAnimation(rightStar, 8 * FRAME_HALF);

        // rocket appear
        playRocketAnimation();
    }

    private void playStarRotationAnimation(final View view, long startDelayTime) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 180f);
        animator.setStartDelay(startDelayTime);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(22 * FRAME_HALF).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float frame = (float) valueAnimator.getAnimatedValue();
                view.setRotation(frame);
                if (frame <= 135f) {
                    view.setAlpha(frame / 135f);
                } else {
                    view.setAlpha((180f - frame) / 45f);
                }
            }
        });
        animator.start();
    }

    private void playTextAppearAnimation(final View view, long startDelay) {
        final float startTranslationY = phoneHeight * 0.25f;

        view.setTranslationY(startTranslationY);
        view.setAlpha(0f);

        long duration = 14 * FRAME_HALF - startDelay;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duration).setInterpolator(softStopAccDecInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float frame = (float) valueAnimator.getAnimatedValue();
                view.setAlpha(frame);
                view.setTranslationY(startTranslationY * (1 - frame));
            }
        });
        animator.setStartDelay(startDelay);
        animator.start();
    }

    private void playRocketAnimation() {
        final float startTranslationY = phoneHeight * 0.7f;

        rocket.setTranslationY(phoneHeight * 0.7f);
        rocket.setAlpha(0f);

        ValueAnimator p1 = ValueAnimator.ofFloat(0f, 1f);
        p1.setDuration(14 * FRAME_HALF).setInterpolator(softStopAccDecInterpolator);
        p1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float frame = (float) valueAnimator.getAnimatedValue();
                rocket.setAlpha(frame);
                rocket.setTranslationY(startTranslationY * (1 - frame));
            }
        });
        p1.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                popupInterstitialAdIfNeeded();
            }
        });
        p1.start();


    }

    @Override protected void onInterruptActionClosed() {
        final boolean shouldDisplaySize = mCleanedSizeMbs > 0;
        final float endTranslationY = -phoneHeight * 0.3f;

        ValueAnimator p2 = ValueAnimator.ofFloat(1f, 0f);
        p2.setStartDelay(8 * FRAME_HALF);
        p2.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION).setInterpolator(softStopAccDecInterpolator);
        p2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float frame = (float) valueAnimator.getAnimatedValue();
                rocket.setAlpha(frame);
                rocket.setTranslationY(endTranslationY * (1 - frame));
            }
        });
        p2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                int[] location = new int[2];
                optimalTv.getLocationInWindow(location);
                int oldOptimalTvCenterY = location[1] + optimalTv.getHeight() / 2;

                titleAnchor.getLocationInWindow(location);
                int newOptimalTvCenterY = location[1] + titleAnchor.getHeight() / 2;

                if (shouldDisplaySize) {
                    final float endTranslationY = newOptimalTvCenterY - oldOptimalTvCenterY - 0.5f * sizeContainer.getTop();
                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                    animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                            .setInterpolator(softStopAccDecInterpolator);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float frame = (float) valueAnimator.getAnimatedValue();
                            sizeContainer.setTranslationY(endTranslationY * frame);
                            freeUpTv.setTranslationY(endTranslationY * frame);
                        }
                    });
                    animator.start();
                } else {
                    final float endTranslationY = newOptimalTvCenterY - oldOptimalTvCenterY;
                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                    animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                            .setInterpolator(softStopAccDecInterpolator);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float frame = (float) valueAnimator.getAnimatedValue();
                            optimalTv.setTranslationY(endTranslationY * frame);
                        }
                    });
                    animator.start();
                }

                onTransitionAnimationEnd();
            }
        });
        p2.start();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        logClickEvent(mType);
    }
}

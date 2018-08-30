package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.cpucooler.CpuCoolerManager;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerUtils;
import com.honeycomb.colorphone.cpucooler.util.CpuPreferenceHelper;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.RevealFlashButton;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

class CpuCoolerResultController extends ResultController {

    private View sizeContainer;
    private TextView sizeTv;
    private TextView sizeDescribeTv;

    private View thermometer;
    private View thermometerMark;

    private TextView optimalTv;
    private TextView titleAnchor;
    private FrameLayout optimalLayout;

    private final int mAvailableHeight = Dimensions.getPhoneHeight(HSApplication.getContext())
            - Dimensions.getStatusBarHeight(HSApplication.getContext())
            - Dimensions.getNavigationBarHeight(HSApplication.getContext());

    private static final long[] TIME_SNOW_FALL_CONTROL_1 = {280, 320, 360};
    private static final long[] TIME_SNOW_FALL_CONTROL_2 = {280, 320, 480};
    private static final long[] TIME_SNOW_FALL_END = {960, 1120, 1280};

    private static final float[] ALPHA_FALL_CONTROL_1 = {0.3f, 0.5f, 0.7f};
    private static final float[] ALPHA_FALL_CONTROL_2 = {0.3f, 0.5f, 0.7f};

    private static final float[] SNOW_FALL_START_ANGLE = {40, 20, 0};
    private static final float[] SNOW_FALL_ROTATE_DEGREE = {85, 64, 45};

    private View[] fallingSnow;
    private float[] fallStartY;
    private float[] fallEndY;

    private long maxDuration;

    private View guideTip;

    private boolean shouldDisplaySize = CpuPreferenceHelper.getShouldResultDisplayTemperature();

    private RevealFlashButton okButton;

    CpuCoolerResultController(ResultPageActivity activity, Type type, List<CardData> cardDataList) {
        super.init(activity, ResultConstants.RESULT_TYPE_CPU_COOLER, type, cardDataList);
//        if (ad != null) {
//            LauncherAnalytics.logEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_EVENT_SHOWN_NAME_RESULT_PAGE, "Type", "CPUCoolerDone");
//            ad.setNativeClickListener(acbAd -> {
//                LauncherAnalytics.logEvent(AdPlacements.SHARED_POOL_NATIVE_AD_FLURRY_EVENT_CLICKED_NAME_RESULT_PAGE, "Type", "CPUCoolerDone");
//                LauncherAnalytics.logEvent("ResultPage_Cards_Click","Type", ResultConstants.AD);
//            });
//        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_transition_cpu_cooler;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        HSLog.d(TAG, "CpuCoolerResultController onFinishInflateTransitionView");

        ViewUtils.findViewById((ResultPageActivity) getContext(), R.id.bg_view).setBackgroundColor(getContext().getResources().getColor(R.color.white));

        optimalLayout = ViewUtils.findViewById(transitionView, R.id.optimal_layout);
        sizeContainer = ViewUtils.findViewById(transitionView, R.id.label_title_size_container);
        sizeTv = ViewUtils.findViewById(transitionView, R.id.label_title_size);
        sizeDescribeTv = ViewUtils.findViewById(transitionView, R.id.label_title_size_describe);

        thermometer = ViewUtils.findViewById(transitionView, R.id.label_thermometer);
        thermometerMark = ViewUtils.findViewById(transitionView, R.id.label_thermometer_mark);

        optimalTv = ViewUtils.findViewById(transitionView, R.id.label_title);
        titleAnchor = ViewUtils.findViewById(transitionView, R.id.anchor_title_tv);

        fallingSnow = new View[]{ViewUtils.findViewById(transitionView, R.id.left_falling_snow_view),
                ViewUtils.findViewById(transitionView, R.id.middle_falling_snow_view),
                ViewUtils.findViewById(transitionView, R.id.right_falling_snow_view)};

        fallStartY = new float[]{getContext().getResources().getFraction(R.fraction.cpu_result_left_falling_snow_start_y, mAvailableHeight, 1),
                getContext().getResources().getFraction(R.fraction.cpu_result_middle_falling_snow_start_y, mAvailableHeight, 1),
                getContext().getResources().getFraction(R.fraction.cpu_result_right_falling_snow_start_y, mAvailableHeight, 1)};

        fallEndY = new float[]{getContext().getResources().getFraction(R.fraction.cpu_result_left_falling_snow_end_y, mAvailableHeight, 1),
                getContext().getResources().getFraction(R.fraction.cpu_result_middle_falling_snow_end_y, mAvailableHeight, 1),
                getContext().getResources().getFraction(R.fraction.cpu_result_right_falling_snow_end_y, mAvailableHeight, 1)};

        maxDuration = Math.max(TIME_SNOW_FALL_END[1], TIME_SNOW_FALL_END[2]);

        guideTip = ViewUtils.findViewById(transitionView, R.id.label_title_guide_info);

        okButton = ViewUtils.findViewById(transitionView, R.id.page_button_ok);
        Drawable bgDrawable = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#2e8cf7"),
                Dimensions.pxFromDp(3), true);
        okButton.setBackgroundDrawable(bgDrawable);
        okButton.setOnClickListener(v -> dismiss());
    }

    @Override
    protected int getButtonBgColor() {
        return Color.parseColor("#1d64e7");
    }

    private void dismiss() {
        mActivity.finish();
    }

    private void initOptimalMargin() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) optimalLayout.getLayoutParams();
        params.topMargin = Dimensions.getPhoneHeight(getContext())
                - Dimensions.getStatusBarHeight(getContext())
                - Dimensions.getNavigationBarHeight(getContext())
                - Dimensions.pxFromDp(183);
        optimalLayout.setLayoutParams(params);
    }

    @Override
    protected boolean onStartTransitionAnimation(final View transitionView) {
        CpuCoolerUtils.FlurryLogger.logOptimalShow();

        if (shouldDisplaySize) {
            fillContentText();
        }

        boolean showAd = popupInterstitialAdIfNeeded();
        HSLog.d(TAG, "onStartTransitionAnimation showAd == " + showAd);
        if (showAd) {
            startTextAppear(false);
        } else {
            new Handler().postDelayed(this::onInterruptActionClosed, 200);
        }
        return true;
    }

    private void fillContentText() {
        int mRandomCoolDownInCelsius = CpuCoolerManager.getInstance().getRandomCoolDownTemperature();
        String temperatureText;
        if (CpuCoolerUtils.shouldDisplayFahrenheit()) {
            float temperatureFahrenheit = Utils.celsiusCoolerByToFahrenheit(mRandomCoolDownInCelsius);
            temperatureText = String.format("%.1f", temperatureFahrenheit) + "°F";
        } else {
            temperatureText = String.valueOf(mRandomCoolDownInCelsius) + "°C";
        }
        sizeTv.setText(temperatureText);
    }

    private void startAnimation() {
        setTitleColor(Color.parseColor("#1d1d1d"));
        // star snow fall animation
        startSnowFallAnimation();

        // thermometer appear
        playThermometerAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startTextAppearAnimation();
                new Handler().postDelayed(() -> startButtonAppearAnimation(), 260);
            }
        });
    }

    private void startButtonAppearAnimation() {
        okButton.setVisibility(View.VISIBLE);
        okButton.setRevealDuration(240);
        okButton.setFlashDuration(560);
        okButton.reveal();
        okButton.postDelayed(() -> okButton.flash(), 260);
    }

    void startTextAppearAnimation() {
        startTextAppear(true);
    }

    void startTextAppear(boolean anim) {
        // text appear
        if (shouldDisplaySize) {
            if (anim) {
                playTextAppearAnimation(sizeContainer);
            } else {
                sizeContainer.setAlpha(1);
                sizeContainer.setVisibility(View.VISIBLE);
            }
        } else {
            if (anim) {
                playTextAppearAnimation(optimalTv);
            } else {
                optimalTv.setAlpha(1);
                optimalTv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void playTextAppearAnimation(View view) {
//        view.setScaleX(0);
//        view.setScaleY(0);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        long duration = 200;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(duration);
        animator.addUpdateListener(valueAnimator -> {
            float frame = (float) valueAnimator.getAnimatedValue();
            view.setAlpha(frame);
//            view.setScaleX(frame);
//            view.setScaleY(frame);
        });
        animator.start();
    }

    private void playThermometerAnimation(AnimatorListenerAdapter animatorListenerAdapter) {

        thermometer.setScaleX(0f);
        thermometer.setScaleY(0f);
        thermometer.setAlpha(0f);

        thermometer.setVisibility(View.VISIBLE);

        ValueAnimator p1 = ValueAnimator.ofFloat(0f, 1f);
        p1.setDuration(360L);
        p1.setInterpolator(softStopAccDecInterpolator);
        p1.addUpdateListener(valueAnimator -> {
            float frame = (float) valueAnimator.getAnimatedValue();
            thermometer.setAlpha(frame);
            thermometer.setScaleX(frame);
            thermometer.setScaleY(frame);
        });

        float startTranslationY = thermometerMark.getTranslationY();
        float endTranslationY = thermometerMark.getHeight() * 0.6f;

        ValueAnimator p2 = ValueAnimator.ofFloat(0f, 1f);
        p2.setDuration(maxDuration).setInterpolator(new LinearInterpolator());
        p2.addUpdateListener(valueAnimator -> {
            float frame = (float) valueAnimator.getAnimatedValue();
            thermometerMark.setTranslationY((endTranslationY - startTranslationY) * frame + startTranslationY);
        });
        p2.addListener(animatorListenerAdapter);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(p1, p2);
        animatorSet.start();
    }


    private void startSnowFallAnimation() {
        ValueAnimator fallAnimator = ValueAnimator.ofFloat(0, 1);
        fallAnimator.addUpdateListener(animation -> {
            for (int i = 0; i < 3; i++) {
                float relativeFraction = animation.getAnimatedFraction() * maxDuration / TIME_SNOW_FALL_END[i];
                fallingSnow[i].setTranslationY(fallStartY[i] + relativeFraction * (fallEndY[i] - fallStartY[i]));
                fallingSnow[i].setRotation(SNOW_FALL_START_ANGLE[i] + relativeFraction * SNOW_FALL_ROTATE_DEGREE[i]);

                long playTime = animation.getCurrentPlayTime();
                if (playTime < TIME_SNOW_FALL_CONTROL_1[i]) {
                    fallingSnow[i].setAlpha(ALPHA_FALL_CONTROL_1[i] * playTime / TIME_SNOW_FALL_CONTROL_1[i]);
                } else if (playTime < TIME_SNOW_FALL_CONTROL_2[i]) {
                    fallingSnow[i].setAlpha(ALPHA_FALL_CONTROL_1[i] + (ALPHA_FALL_CONTROL_2[i] - ALPHA_FALL_CONTROL_1[i]) * (playTime - TIME_SNOW_FALL_CONTROL_1[i]) / (TIME_SNOW_FALL_CONTROL_2[i] - TIME_SNOW_FALL_CONTROL_1[i]));
                } else if (playTime < TIME_SNOW_FALL_END[i]) {
                    fallingSnow[i].setAlpha(ALPHA_FALL_CONTROL_2[i] * (1 - (float) (playTime - TIME_SNOW_FALL_CONTROL_2[i]) / (TIME_SNOW_FALL_END[i] - TIME_SNOW_FALL_CONTROL_2[i])));
                } else {
                    fallingSnow[i].setAlpha(0);
                }
            }
        });

        fallAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < 3; i++) {
                    fallingSnow[i].setVisibility(View.VISIBLE);
                    fallingSnow[i].setTranslationY(fallStartY[i]);
                    fallingSnow[i].setRotation(SNOW_FALL_START_ANGLE[i]);
                }
            }
        });

        fallAnimator.setDuration(maxDuration);
        fallAnimator.setStartDelay(7 * FRAME);
        fallAnimator.start();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        logClickEvent(mType);
    }

    @Override protected void onInterruptActionClosed() {

        AcbNativeAd ad = ResultPageManager.getInstance().getAd();
        LauncherAnalytics.logEvent("Colorphone_CPUDone_Ad_Should_Shown");

        HSLog.d(TAG, "Back from Ad Screen ad ==  " + ad);
        if (ad == null) {
            startAnimation();
            return;
        } else {
            startTextAppear(false);
            super.showAd(ad);
            super.showAdWithAnimation();

        }

        int[] location = new int[2];
        optimalTv.getLocationInWindow(location);
        int oldOptimalTvCenterY = location[1] + optimalTv.getHeight() / 2;

        titleAnchor.getLocationInWindow(location);
        int newOptimalTvCenterY = location[1] + titleAnchor.getHeight() / 2;

        if (shouldDisplaySize) {
            float endTranslationY = newOptimalTvCenterY - oldOptimalTvCenterY - 0.5f * sizeContainer.getTop();
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                    .setInterpolator(softStopAccDecInterpolator);
            animator.addUpdateListener(valueAnimator -> {
                float frame = (float) valueAnimator.getAnimatedValue();
                sizeContainer.setTranslationY(endTranslationY * frame);
            });
            animator.start();
            sizeContainer.setVisibility(View.VISIBLE);
            sizeTv.setTextColor(getContext().getResources().getColor(R.color.white));
            sizeDescribeTv.setTextColor(getContext().getResources().getColor(R.color.white));
            optimalTv.setVisibility(View.GONE);
            guideTip.setTranslationY(endTranslationY + mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_guide_tip_translation_top));
        } else {
            float endTranslationY = newOptimalTvCenterY - oldOptimalTvCenterY;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION)
                    .setInterpolator(softStopAccDecInterpolator);
            animator.addUpdateListener(valueAnimator -> {
                float frame = (float) valueAnimator.getAnimatedValue();
                optimalTv.setTranslationY(endTranslationY * frame);
            });
            animator.start();
            optimalTv.setVisibility(View.VISIBLE);
            optimalTv.setTextColor(getContext().getResources().getColor(R.color.white));
            sizeContainer.setVisibility(View.GONE);
            guideTip.setTranslationY(endTranslationY + mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_guide_tip_translation_top));
        }

        thermometer.setVisibility(View.INVISIBLE);
        ViewUtils.findViewById((ResultPageActivity) getContext(), R.id.bg_view).setBackgroundColor(getContext().getResources().getColor(R.color.cpu_cooler_primary_blue));


        showGuideTipIfNeeded();
    }

    private void showGuideTipIfNeeded() {
        if (mType == Type.CARD_VIEW) {
            ValueAnimator guideTipAppear = ValueAnimator.ofFloat(0f, 1f);
            guideTipAppear.setDuration(4 * DURATION_CARD_TRANSLATE);
            guideTipAppear.setInterpolator(softStopAccDecInterpolator);
            guideTipAppear.setStartDelay(3 * DURATION_CARD_TRANSLATE_DELAY);
            guideTipAppear.addUpdateListener(valueAnimator -> {
                float frame = (float) valueAnimator.getAnimatedValue();
                guideTip.setAlpha(frame);
            });
            guideTipAppear.start();
        }
    }
}

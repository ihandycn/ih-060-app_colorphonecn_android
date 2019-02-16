package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.view.RevealFlashButton;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

class BatteryResultController extends ResultController {

    private Handler handler = new Handler();

    private TextView titleAnchor;

    private boolean mIsOptimal;
    private int mExtendHour;
    private int mExtendMinute;

    private ImageView batteryBubbleImageView;
    private ImageView batteryImageView;

    private ImageView firstPlusImageView;
    private ImageView secondPlusImageView;
    private ImageView thirdPlusImageView;

    private ViewGroup saveTimeLayout;
    private TextView extendedTextView;

    private TextView optimalTextView;
    private FrameLayout optimalLayout;

    private TextView saveTimeHourTextView;
    private TextView saveTimeHourUnitTextView;
    private TextView saveTimeMinuteTextView;
    private TextView saveTimeMinuteUnitTextView;

    private View guideTip;

    BatteryResultController(ResultPageActivity activity, boolean isOptimal,
                            int extendHour, int extendMinute, Type type, List<CardData> cardDataList) {
        mIsOptimal = isOptimal;
        mExtendHour = extendHour;
        mExtendMinute = extendMinute;
        super.init(activity, ResultConstants.RESULT_TYPE_BATTERY, type, cardDataList);
        HSLog.d(TAG, "Battery : " + ",mIsOptimal = " + mIsOptimal
        + ",mExtendHour=" + mExtendHour
        +", mExtendMinute=" + mExtendMinute);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_transition_battery;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        HSLog.d(TAG, "Battery onFinishInflateTransitionView");
        ViewUtils.findViewById((ResultPageActivity) getContext(), R.id.bg_view).setBackgroundColor(getContext().getResources().getColor(R.color.white));

        firstPlusImageView = ViewUtils.findViewById(transitionView, R.id.first_plus);
        secondPlusImageView = ViewUtils.findViewById(transitionView, R.id.second_plus);
        thirdPlusImageView = ViewUtils.findViewById(transitionView, R.id.third_plus);

        batteryImageView = ViewUtils.findViewById(transitionView, R.id.clean_finish_battery);
        batteryBubbleImageView = ViewUtils.findViewById(transitionView, R.id.clean_finish_bubble);

        saveTimeLayout = ViewUtils.findViewById(transitionView, R.id.save_time_layout);
        extendedTextView = ViewUtils.findViewById(transitionView, R.id.extend);

        saveTimeHourTextView = ViewUtils.findViewById(transitionView, R.id.save_time_hour);
        saveTimeHourUnitTextView = ViewUtils.findViewById(transitionView, R.id.save_time_hour_unit);
        saveTimeMinuteTextView = ViewUtils.findViewById(transitionView, R.id.save_time_minute);
        saveTimeMinuteUnitTextView = ViewUtils.findViewById(transitionView, R.id.save_time_minute_unit);

        saveTimeHourTextView.setVisibility(View.GONE);
        saveTimeHourUnitTextView.setVisibility(View.GONE);
        saveTimeMinuteTextView.setVisibility(View.GONE);
        saveTimeMinuteUnitTextView.setVisibility(View.GONE);

        titleAnchor = ViewUtils.findViewById(transitionView, R.id.anchor_title_tv);
        optimalTextView = ViewUtils.findViewById(transitionView, R.id.optimal);
        optimalLayout = ViewUtils.findViewById(transitionView, R.id.optimal_layout);

        guideTip = ViewUtils.findViewById(transitionView, R.id.label_title_guide_info);

        if (mIsOptimal) {
            saveTimeLayout.setVisibility(View.INVISIBLE);
        } else {
            optimalTextView.setVisibility(View.INVISIBLE);
            if (mExtendHour > 0) {
                saveTimeHourTextView.setVisibility(View.VISIBLE);
                saveTimeHourUnitTextView.setVisibility(View.VISIBLE);
                saveTimeHourTextView.setText(String.valueOf(mExtendHour));
            }

            if (mExtendMinute > 0) {
                saveTimeMinuteTextView.setVisibility(View.VISIBLE);
                saveTimeMinuteUnitTextView.setVisibility(View.VISIBLE);
                saveTimeMinuteTextView.setText(String.valueOf(mExtendMinute));
            }
        }


        okButton = ViewUtils.findViewById(transitionView, R.id.page_button_ok);
        Drawable bgDrawable = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#62d337"),
                Dimensions.pxFromDp(3), true);
        okButton.setBackgroundDrawable(bgDrawable);
        okButton.setOnClickListener(v -> dismiss());
    }

    @Override
    protected int getButtonBgColor() {
        return Color.parseColor("#2573e2");
    }

    private void dismiss() {
        mActivity.finish();
    }

    @Override
    protected boolean onStartTransitionAnimation(View transitionView) {
        boolean showAd = popupInterstitialAdIfNeeded();
        if (showAd) {
            showTextLayout();
        } else {
            handler.postDelayed(() -> onInterruptActionClosed(), 200);
        }
        return true;
    }

    private void initOptimalMargin() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) optimalLayout.getLayoutParams();
        params.topMargin = Dimensions.getPhoneHeight(getContext())
                - Dimensions.getStatusBarHeight(getContext())
                - Dimensions.getNavigationBarHeight(getContext())
                - Dimensions.pxFromDp(183);
        optimalLayout.setLayoutParams(params);
    }

    private void startAnimation() {
        startBatteryAppearAnimator();
//        delayStartBatteryDisappearAnimator(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                popupInterstitialAdIfNeeded();
//            }
//        });
    }

    private void startExtendedTimeAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            if (mIsOptimal) {
                optimalTextView.setAlpha(value);
            } else {
                saveTimeLayout.setAlpha(value);
            }
        });
        valueAnimator.start();
    }

    private void showTextLayout() {
        if (mIsOptimal) {
            optimalTextView.setVisibility(View.VISIBLE);
            optimalTextView.setAlpha(1f);
        } else {
            optimalTextView.setVisibility(View.VISIBLE);
            saveTimeLayout.setAlpha(1f);
        }
    }

    private void startBatteryAppearAnimator() {
        batteryImageView.setVisibility(View.VISIBLE);

        setTitleColor(Color.parseColor("#1d1d1d"));

        final int translationY = Dimensions.pxFromDp(10);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(translationY, 0);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            batteryImageView.setAlpha(1 - value / translationY);
            batteryImageView.setTranslationY(value);
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startBubbleAnimation();
            }
        });

        valueAnimator.start();

        handler.postDelayed(() -> startPlusAnimator(firstPlusImageView), 150);

        handler.postDelayed(() -> startPlusAnimator(thirdPlusImageView), 230);

        handler.postDelayed(() -> startPlusAnimator(secondPlusImageView), 310);

        handler.postDelayed(() -> startExtendedTimeAnimator(), 1500);
        handler.postDelayed(() -> startButtonAppearAnimation(), 1800);
    }

    private RevealFlashButton okButton;

    private void startButtonAppearAnimation() {
        okButton.setVisibility(View.VISIBLE);
        okButton.setRevealDuration(240);
        okButton.setFlashDuration(560);
        okButton.reveal();
        okButton.postDelayed(() -> okButton.flash(), 260);
    }

    private void startBubbleAnimation() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(batteryBubbleImageView,
                "translationY", Dimensions.pxFromDp(45), -Dimensions.pxFromDp(45));
        objectAnimator.setDuration(1800);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                batteryBubbleImageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                batteryBubbleImageView.setVisibility(View.INVISIBLE);
            }
        });

        objectAnimator.start();
    }

    private void startPlusAnimator(View view) {
        ObjectAnimator alphaObjectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 0.6f);
        alphaObjectAnimator.setDuration(1050);
        alphaObjectAnimator.setRepeatCount(1);
        alphaObjectAnimator.setInterpolator(new FastOutSlowInInterpolator());
        alphaObjectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        alphaObjectAnimator.start();

        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(view, "translationY", 0, Dimensions.pxFromDp(10));
        translationYAnimator.setDuration(2100);
        translationYAnimator.setInterpolator(new FastOutSlowInInterpolator());
        translationYAnimator.start();
    }

    private void delayStartBatteryDisappearAnimator(AnimatorListenerAdapter animatorListenerAdapter) {
        handler.postDelayed(() -> {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(batteryImageView, "alpha", 1, 0);
            objectAnimator.setDuration(225);
            objectAnimator.addListener(animatorListenerAdapter);
            objectAnimator.start();
        }, 2500);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        logClickEvent(mType);
    }

    @Override protected void onInterruptActionClosed() {
        AcbNativeAd ad = ResultPageManager.getInstance().getAd();
        if (ResultPageManager.getInstance().isFromBatteryImprover()) {
            Analytics.logEvent("ColorPhone_CableImproverDone_Should_Show",
                    "From", ResultPageManager.getInstance().getFromTag());
            Ap.Improver.logEvent("cableimproverdone_should_show");
        } else {
            Analytics.logEvent("Colorphone_BatteryDone_Ad_Should_Shown");
        }

        HSLog.d(TAG, "Back from Ad Screen");
        if (ad == null) {
            startAnimation();
            return;
        } else {
            showTextLayout();
            super.showAd(ad);
            super.showAdWithAnimation();

        }

        int[] location = new int[2];
        optimalTextView.getLocationInWindow(location);
        int oldOptimalTvCenterY = location[1] + optimalTextView.getHeight() / 2;
        titleAnchor.getLocationInWindow(location);
        int newOptimalTvCenterY = location[1] + titleAnchor.getHeight() / 2;

        if (mIsOptimal) {
            float transitionY = newOptimalTvCenterY - oldOptimalTvCenterY;
            optimalTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            ObjectAnimator animator = ObjectAnimator.ofFloat(optimalTextView, "translationY", oldOptimalTvCenterY, transitionY);
            animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION);
            animator.setInterpolator(softStopAccDecInterpolator);
            animator.start();
            saveTimeLayout.setVisibility(View.GONE);
            guideTip.setTranslationY(transitionY + mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_guide_tip_translation_top));
        } else {
            float transitionY = newOptimalTvCenterY - oldOptimalTvCenterY - 2.0f * saveTimeLayout.getTop();
            extendedTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            saveTimeHourTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            saveTimeHourUnitTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            saveTimeMinuteTextView.setTextColor(getContext().getResources().getColor(R.color.white));
            saveTimeMinuteUnitTextView.setTextColor(getContext().getResources().getColor(R.color.white));

            ObjectAnimator animator = ObjectAnimator.ofFloat(saveTimeLayout, "translationY", oldOptimalTvCenterY, transitionY);
            animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION);
            animator.setInterpolator(softStopAccDecInterpolator);
            animator.start();
            optimalTextView.setVisibility(View.GONE);
            guideTip.setTranslationY(transitionY + mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_guide_tip_translation_top));
        }

        batteryImageView.setVisibility(View.GONE);
        batteryBubbleImageView.setVisibility(View.GONE);
        ViewUtils.findViewById((ResultPageActivity) getContext(), R.id.bg_view).setBackgroundColor(getContext().getResources().getColor(R.color.battery_green));

        onTransitionAnimationEnd();
//        showGuideTipIfNeeded();
    }

//    private void showGuideTipIfNeeded() {
//        if (mType == Type.CARD_VIEW) {
//            ValueAnimator guideTipAppear = ValueAnimator.ofFloat(0f, 1f);
//            guideTipAppear.setDuration(4 * DURATION_CARD_TRANSLATE);
//            guideTipAppear.setInterpolator(softStopAccDecInterpolator);
//            guideTipAppear.setStartDelay(3 * DURATION_CARD_TRANSLATE_DELAY);
//            guideTipAppear.addUpdateListener(valueAnimator -> {
//                float frame = (float) valueAnimator.getAnimatedValue();
//                guideTip.setAlpha(frame);
//            });
//            guideTipAppear.start();
//        }
//    }
}

package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.honeycomb.colorphone.util.ViewUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

class ThemeRecommendResultController extends ResultController {

//    private View sizeContainer;
//    private TextView sizeTv;
//    private TextView sizeDescribeTv;
//
//    private View thermometer;
//    private View thermometerMark;

    private TextView optimalTv;
    private TextView titleAnchor;
    private View optimalLayout;
    private LottieAnimationView successAnim;
    private View contentLayout;
    private ThemePreviewWindow mPreview;

    private View guideTip;

    private View okButton;
    private com.acb.call.themes.Type mThemeType;

    ThemeRecommendResultController(ResultPageActivity activity, com.acb.call.themes.Type themeType, Type type, List<CardData> cardDataList) {
        mThemeType = themeType;
        super.init(activity, ResultConstants.RESULT_TYPE_THEME_RECOMMEND, type, cardDataList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.result_page_transition_theme_recommend;
    }

    @Override
    protected void onFinishInflateTransitionView(View transitionView) {
        HSLog.d(TAG, "CpuCoolerResultController onFinishInflateTransitionView");

        mPreview = ViewUtils.findViewById(transitionView, R.id.prev_flash_window);

        optimalLayout = ViewUtils.findViewById(transitionView, R.id.optimal_layout);

        optimalTv = ViewUtils.findViewById(transitionView, R.id.label_title);
        titleAnchor = ViewUtils.findViewById(transitionView, R.id.anchor_title_tv);

        successAnim = ViewUtils.findViewById(transitionView, R.id.label_animation);
        contentLayout = ViewUtils.findViewById(transitionView, R.id.page_layout);

        guideTip = ViewUtils.findViewById(transitionView, R.id.label_title_guide_info);

        okButton = ViewUtils.findViewById(transitionView, R.id.page_button_ok);
        Drawable bgDrawable = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#ffe400"),
                Dimensions.pxFromDp(6), true);
        okButton.setBackgroundDrawable(bgDrawable);
        okButton.setOnClickListener(v -> {
            dismiss();
            Navigations.startActivitySafely(getContext(), new Intent(getContext(), ColorPhoneActivity.class));
            ThemeRecommendManager.logThemeRecommendResultPageFindMoreClicked();
        });

        ImageView closeButton = (ImageView) mActivity.findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getButtonBgColor() {
        return Color.parseColor("#ffe400");
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
        boolean showAd = popupInterstitialAdIfNeeded();
        if (showAd) {
//            startTextAppear(false);
        } else {
            mHandler.postDelayed(this::onInterruptActionClosed, 200);
        }
        return true;
    }

    void startTextAppear(boolean anim) {
        // text appear
//        if (shouldDisplaySize) {
//            if (anim) {
//                playTextAppearAnimation(sizeContainer);
//            } else {
//                sizeContainer.setAlpha(1);
//                sizeContainer.setVisibility(View.VISIBLE);
//            }
//        } else {
            if (anim) {
                playTextAppearAnimation(optimalTv);
            } else {
                optimalTv.setAlpha(1);
                optimalTv.setVisibility(View.VISIBLE);
            }
//        }
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        logClickEvent(mType);
    }

    @Override protected void onInterruptActionClosed() {
        mPreview.updateThemeLayout(mThemeType);
        mPreview.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);

        mPreview.playAnimation(mThemeType);

        View avatar = mPreview.findViewById(com.acb.call.R.id.caller_avatar);
        avatar.setVisibility(View.GONE);
        TextView firstLineTextView = (TextView) mPreview.findViewById(com.acb.call.R.id.first_line);
        firstLineTextView.setVisibility(View.GONE);
        TextView secondLineTextView = (TextView) mPreview.findViewById(com.acb.call.R.id.second_line);
        secondLineTextView.setVisibility(View.GONE);

        startTextAppear(false);
        successAnim.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showContentAnimation();
            }
        });
        successAnim.playAnimation();

    }

    private void showContentAnimation() {

        AcbNativeAd ad = ResultPageManager.getInstance().getAd();
        ThemeRecommendManager.logThemeRecommendDoneShouldShow();

        HSLog.d(TAG, "Back from Ad Screen ad ==  " + ad);
        if (ad == null) {
            contentLayout.setVisibility(View.VISIBLE);
            contentLayout.setAlpha(0);
            ThemeRecommendManager.logThemeRecommendResultPageShow();
        } else {
            contentLayout.setVisibility(View.GONE);
            super.showAd(ad);
            super.showAdWithAnimation();
        }

        int[] location = new int[2];
        optimalTv.getLocationInWindow(location);
        int oldOptimalTvCenterY = location[1] + optimalTv.getHeight() / 2;

        titleAnchor.getLocationInWindow(location);
        int newOptimalTvCenterY = location[1] + titleAnchor.getHeight() / 2;

        float endTranslationY = newOptimalTvCenterY - oldOptimalTvCenterY;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_OPTIMAL_TEXT_TRANSLATION * 2)
                .setInterpolator(softStopAccDecInterpolator);
        contentLayout.setTranslationY(endTranslationY);
        animator.addUpdateListener(valueAnimator -> {
            float frame = (float) valueAnimator.getAnimatedValue();
            optimalTv.setTranslationY(endTranslationY * frame);
            successAnim.setTranslationY(endTranslationY * frame);
            successAnim.setAlpha(1 - frame);
            contentLayout.setTranslationY(endTranslationY * (frame - 1));
            contentLayout.setAlpha(frame);
        });
        animator.start();

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

package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.AutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;
import net.appcloudbox.ads.common.utils.AcbError;

import java.util.List;

@SuppressWarnings("WeakerAccess")
abstract class ResultController implements View.OnClickListener {

    static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 360;
    static final long DURATION_CARD_TRANSLATE_DELAY = 0;
    static final long DURATION_AD_OR_FUNCTION_TRANSLATE_DELAY = 0;

    protected static final String TAG = "ResultController";
    static final long FRAME = 100;
    static final long FRAME_HALF = 50;

    static final long DURATION_BALL_TRANSLATE = 7 * FRAME_HALF;
    private static final long START_DELAY_CARDS = FRAME / 10 * 86;
     static final long DURATION_CARD_TRANSLATE = 8 * FRAME;
     static final long DURATION_BG_TRANSLATE = 7 * FRAME;

    // Ad / charging screen
    private static final long START_DELAY_AD_OR_CHARGING_SCREEN = 16 * FRAME;
     static final long START_DELAY_BUTTON_REVEAL = 3 * FRAME;
     static final long START_DELAY_BUTTON_FLASH = 23 * FRAME;
     static final float TRANSLATION_MULTIPLIER_SHADOW_1 = 1.4f;
     static final float TRANSLATION_MULTIPLIER_SHADOW_2 = 1.2f;
     static final float TRANSLATION_MULTIPLIER_DESCRIPTION = 1.25f;
     static final long DURATION_SLIDE_IN = 800;

    protected ResultPageActivity mActivity;
    int mScreenHeight;
    int mResultType;
    Type mType = Type.CARD_VIEW;

    private FrameLayout mTransitionView;
    private FrameLayout mAdOrChargingScreenContainerView;
    private RecyclerView mCardRecyclerView;
     View mResultView;
    private View mBgView;
    private View mHeaderTagView;

    // Ad or charging screen
//    private View mImageFrameShadow1;
//    private View mImageFrameShadow2;
    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;
    private ViewGroup mChargingScreenImageContainer;
    private ImageView mImageIv;
    private ViewGroup mAdChoice;
    private AcbNativeAdIconView mAdIconView;
    private TextView mTitleTv;
    private TextView mDescriptionTv;
    private View mActionBtn;

    private AcbNativeAd mAd;
    private List<CardData> mCardDataList;
    private View mResultContentView;
    private boolean mAdShown;
    protected final Handler mHandler = new Handler();
    public Interpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.26f, 1f, 0.48f, 1f);
    private boolean mInterstitialAdDisplaying;

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
    }

    enum Type {
        AD,
        CHARGE_SCREEN,
        NOTIFICATION_CLEANER,
        CARD_VIEW,
        APP_LOCK,
    }

    ResultController() {
    }

    ResultController(ResultPageActivity activity, int resultType, Type type, List<CardData> cardDataList) {
        init(activity, resultType, type, cardDataList);
    }

    protected void init(ResultPageActivity activity, int resultType, Type type,  List<CardData> cardDataList) {
        HSLog.d(TAG, "ResultController init *** resultType = " + resultType + " type = " + type);
        mActivity = activity;
        mType = type;

        mCardDataList = cardDataList;
        mResultType = resultType;
        mScreenHeight = Dimensions.getPhoneHeight(activity);

        logViewEvent();

        LayoutInflater layoutInflater = LayoutInflater.from(activity);


        mHeaderTagView = ViewUtils.findViewById(activity, R.id.result_header_tag_view);

        mTransitionView = ViewUtils.findViewById(activity, R.id.transition_view_container);
        if (null != mTransitionView) {
            mTransitionView.removeAllViews();

            int layoutId = getLayoutId();
            if (layoutId != 0) {
                layoutInflater.inflate(layoutId, mTransitionView, true);
                onFinishInflateTransitionView(mTransitionView);
            }
        }

        switch (type) {
            case AD:
            case CHARGE_SCREEN:
            case NOTIFICATION_CLEANER:
            case APP_LOCK:
                initAdOrFunctionView(activity, layoutInflater);
                break;
        }
        mResultView = ViewUtils.findViewById(activity, R.id.result_view);
    }

    private void initAdOrFunctionView(Activity activity, LayoutInflater layoutInflater) {
        HSLog.d(TAG, "initAdOrChargingScreenView");
        FrameLayout container = ViewUtils.findViewById(activity, R.id.ad_or_charging_screen_view_container);
        if (null != container) {
            int layoutId = R.layout.result_page_fullscreen_ad;
            mResultContentView = layoutInflater.inflate(layoutId, mAdOrChargingScreenContainerView, false);
            mAdOrChargingScreenContainerView = container;
            onFinishInflateResultView(mResultContentView);
            mAdOrChargingScreenContainerView.setVisibility(View.VISIBLE);
            if (null != mCardRecyclerView) {
                mCardRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    protected abstract int getLayoutId();

    protected abstract void onFinishInflateTransitionView(View transitionView);

    protected void onFinishInflateResultView(View resultView) {
        HSLog.d(TAG, "onFinishInflateResultView mType = " + mType);
        Context context = getContext();
        VectorDrawableCompat imageFrame = null;

        if (mType == Type.AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER || mType == Type.APP_LOCK) {
//            mImageFrameShadow1 = ViewUtils.findViewById(resultView, R.id.result_image_iv_shadow_1);
//            mImageFrameShadow2 = ViewUtils.findViewById(resultView, R.id.result_image_iv_shadow_2);
            mAdImageContainer = ViewUtils.findViewById(resultView, R.id.result_image_container_ad);
            mChargingScreenImageContainer = ViewUtils.findViewById(resultView, R.id.result_image_container_charging_screen);
            mImageIv = ViewUtils.findViewById(resultView, R.id.result_image_iv);
            mAdChoice = ViewUtils.findViewById(resultView, R.id.result_ad_choice);
            mAdIconView = ViewUtils.findViewById(resultView, R.id.result_ad_icon);
            mTitleTv = ViewUtils.findViewById(resultView, R.id.description_title_tv);
            mDescriptionTv = ViewUtils.findViewById(resultView, R.id.description_content_tv);
            mActionBtn = ViewUtils.findViewById(resultView, R.id.result_action_btn);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mActionBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                        getButtonBgColor(), Dimensions.pxFromDp(3), true));
            } else {
                mActionBtn.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(
                        getButtonBgColor(), Dimensions.pxFromDp(3), true));

            }

//            imageFrame = VectorDrawableCompat.create(context.getResources(),
//                    R.drawable.result_page_image_frame, context.getTheme());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                mImageFrameShadow1.setBackground(imageFrame);
//                mImageFrameShadow2.setBackground(imageFrame);
//            } else  {
//                mImageFrameShadow1.setBackgroundDrawable(imageFrame);
//                mImageFrameShadow2.setBackgroundDrawable(imageFrame);
//            }
            mAdImageContainer.setBitmapConfig(Bitmap.Config.RGB_565);
            int targetWidth = Utils.getPhoneWidth(context) - 2 * Utils.pxFromDp(27) - 2 * Utils.pxFromDp(20);
            int targetHeight = (int) (targetWidth / 1.9f);
            mAdImageContainer.setTargetSizePX(targetWidth, targetHeight);
            mAdIconView.setTargetSizePX(Utils.pxFromDp(60), Utils.pxFromDp(60));

        }
//        switch (mType) {
//            case AD:
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    mAdImageContainer.setBackground(imageFrame);
//                } else {
//                    mAdImageContainer.setBackgroundDrawable(imageFrame);
//                }
//                break;
//        }

    }

    protected int getButtonBgColor() {
        return Color.parseColor("#1fb70b");
    }

    protected void setTitleColor(int color) {
        TextView textView = (TextView) mActivity.findViewById(R.id.title_text);
        if (textView != null) {
            textView.setTextColor(color);
            textView.setAlpha(0.1f);
            textView.animate().alpha(1).setDuration(200).start();
        }
    }

    public void showAd(AcbNativeAd ad) {

        logBoostDoneAdShow();

        mAdShown = true;
        mAd = ad;
        ad.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
            @Override
            public void onAdClick(AcbAd acbAd) {
                logBoostDoneAdClicked();
            }
        });
        AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
        adContainer.addContentView(mResultContentView);
        adContainer.setAdTitleView(mTitleTv);
        adContainer.setAdBodyView(mDescriptionTv);
        adContainer.setAdPrimaryView(mAdImageContainer);
//        mChargingScreenImageContainer.setVisibility(View.INVISIBLE);
        adContainer.setAdChoiceView(mAdChoice);
        adContainer.setAdIconView(mAdIconView);
        adContainer.setAdActionView(mActionBtn);

        mAdOrChargingScreenContainerView.addView(adContainer);
        mAdContainer = adContainer;
        if (mAdContainer != null) {
            mAdContainer.fillNativeAd(ad);
        }
    }


    protected abstract boolean onStartTransitionAnimation(View transitionView);
    protected abstract void onInterruptActionClosed();

    protected void onFunctionCardViewShown() {
    }

    public void onTransitionAnimationEnd() {
        if (mType == Type.AD || mType == Type.CHARGE_SCREEN || mType == Type.NOTIFICATION_CLEANER || mType == Type.APP_LOCK) {
            startAdOrFunctionResultAnimation(DURATION_AD_OR_FUNCTION_TRANSLATE_DELAY);
        }
    }

    protected boolean popupInterstitialAdIfNeeded() {
        logInterstitialAdNeedShow();
        if (shouldShowInterstitialAd()) {
            HSLog.d(TAG, "popupInterstitialAdIfNeeded true ");
            popupInterstitialAd(ResultPageManager.getInstance().getInterstitialAd());
            return true;
        }
        HSLog.d(TAG, "popupInterstitialAdIfNeeded false ");
        return false;
    }

    private boolean shouldShowInterstitialAd() {
        if (ResultPageManager.getInstance().getInterstitialAd() == null) {
            return false;
        }
        return true;
    }

    private void popupInterstitialAd(AcbInterstitialAd ad) {
        mInterstitialAdDisplaying = true;
        mActivity.getIntent().putExtra("extra_ad_display", true);
            ad.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                @Override
                public void onAdDisplayed() {
                    logInterstitialAdShow();
                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdClosed() {
                    ResultPageManager.getInstance().releaseInterstitialAd();
                }

                public void onAdDisplayFailed(AcbError acbError) {
                HSLog.d(TAG, "onAdDisplayFailed");
                }
            });
            ad.show();
    }

    public void notifyInterstitialAdClosedByCustomer() {
        if (mInterstitialAdDisplaying
                || mActivity.getIntent().getBooleanExtra("extra_ad_display", false)) {
            mActivity.getIntent().putExtra("extra_ad_display", false);
            onInterruptActionClosed();
        }
    }

    public void startAdOrFunctionResultAnimation(long startDelay) {
        mHandler.postDelayed(() -> {

            mResultView.setVisibility(View.VISIBLE);

            startCardTranslationAnimation(mAdOrChargingScreenContainerView, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationEnd(animation);
//                    mActionBtn.startFlash();
                }
            });
        }, startDelay);
    }

    private void startCardTranslationAnimation(View view, AnimatorListenerAdapter animatorListenerAdapter) {
        float slideUpTranslationFrom = mScreenHeight - mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_header_height)
                - Dimensions.getStatusBarHeight(mActivity) - Dimensions.pxFromDp(15);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_CARD_TRANSLATE);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setTranslationY(slideUpTranslationFrom * (1 - value));
        });
        animator.setInterpolator(softStopAccDecInterpolator);
        animator.addListener(animatorListenerAdapter);
        animator.start();
        view.setVisibility(View.VISIBLE);
    }

    protected void startTransitionAnimation() {
        HSLog.d(TAG, "startTransitionAnimation mTransitionView = " + mTransitionView);
        if (null != mTransitionView) {
            boolean hasChildPageAnimation = onStartTransitionAnimation(mTransitionView);
        }
    }

    protected void showAdWithAnimation() {
        HSLog.d(TAG, "startAdOrChargingScreenResultAnimation run");
        View imageContainer = (mType == Type.AD) ? mAdImageContainer : mChargingScreenImageContainer;

        int[] location = new int[2];
        imageContainer.getLocationInWindow(location);
        float baseTranslationY = mScreenHeight - location[1];

        imageContainer.setTranslationY(baseTranslationY);
        mAdIconView.setTranslationY(baseTranslationY);
//        mImageFrameShadow1.setTranslationY(baseTranslationY * TRANSLATION_MULTIPLIER_SHADOW_1);
//        mImageFrameShadow2.setTranslationY(baseTranslationY * TRANSLATION_MULTIPLIER_SHADOW_2);
        mTitleTv.setTranslationY(baseTranslationY);
        mDescriptionTv.setTranslationY(baseTranslationY * TRANSLATION_MULTIPLIER_DESCRIPTION);
        mAdChoice.setAlpha(0f);

        mResultView.setVisibility(View.VISIBLE);

        View[] translatedViews = new View[]{
                imageContainer, mAdIconView,  mTitleTv, mDescriptionTv
        };
        for (View v : translatedViews) {
            v.animate()
                    .translationY(0f)
                    .setDuration(DURATION_SLIDE_IN)
                    .setInterpolator(LauncherAnimUtils.DECELERATE_QUINT)
                    .start();
        }
        if (mType == Type.AD) {
            // Choice view only applies to ad, no need to animate when charging screen is shown
            mAdChoice.animate()
                    .alpha(1f)
                    .setDuration(DURATION_SLIDE_IN)
                    .setInterpolator(LauncherAnimUtils.DECELERATE_QUAD)
                    .start();
        }

        // Close button
        ImageView closeButton = (ImageView) mActivity.findViewById(R.id.close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
        closeButton.setVisibility(View.VISIBLE);

    }


    public static Rect getLocationRect(View view) {
        Rect location = new Rect();
        view.getGlobalVisibleRect(location);
        return location;
    }

    private void startRealBgTranslateAnimation() {
        int bottom = getLocationRect(mHeaderTagView).bottom;
        float translateDistance = mScreenHeight - bottom;
        mBgView.animate()
                .translationYBy(-translateDistance)
                .setDuration(DURATION_BG_TRANSLATE)
                .setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE)
                .start();
    }

    protected Context getContext() {
        return mActivity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }

    public boolean isAdShown() {
        return mAdShown;
    }

    private void logViewEvent() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                if (ResultPageManager.getInstance().isFromBatteryImprover()) {
                    LauncherAnalytics.logEvent("ColorPhone_CableImprover_Clean_ResultPage_Show",
                            "From", ResultPageManager.getInstance().getFromTag());
                    Ap.Improver.logEvent("cleanpage_resultpage_show");
                } else {
                    LauncherAnalytics.logEvent("Colorphone_BatteryDone_Page_Shown");
                }
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Page_Shown_FromSettings");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PUSH:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Page_Shown_FromPush");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Page_Shown_FromToolbar");
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                LauncherAnalytics.logEvent("Colorphone_CPUDone_Page_Shown");
                break;
        }
    }

    protected void logClickEvent(Type type) {
//        if (type == Type.AD) {
//            // No log here, logged in onAdClick()
//        } else if (type == Type.CHARGE_SCREEN) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.CHARGING_SCREEN_FULL);
//        } else if (type == Type.NOTIFICATION_CLEANER) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.NOTIFICATION_CLEANER_FULL);
//        } else if (type == Type.APP_LOCK) {
//            LauncherAnalytics.logEvent("ResultPage_Cards_Click", "Type", ResultConstants.APPLOCK);
//        }
    }

    private void logInterstitialAdNeedShow() {
        HSLog.d(TAG, "logInterstitialAdNeedShow mResultType: " + mResultType);
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                if (ResultPageManager.getInstance().isFromBatteryImprover()) {
                    LauncherAnalytics.logEvent("ColorPhone_CableImproverWire_Should_Show",
                            "From", ResultPageManager.getInstance().getFromTag());
                    Ap.Improver.logEvent("cableimproverwire_should_show");
                } else {
                    LauncherAnalytics.logEvent("Colorphone_BatteryWire_Ad_Should_Shown");
                }
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                LauncherAnalytics.logEvent("Colorphone_BoostWire_Ad_Should_Shown_FromSettings");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PUSH:
                LauncherAnalytics.logEvent("Colorphone_BoostWire_Ad_Should_Shown_FromPush");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
                LauncherAnalytics.logEvent("Colorphone_BoostWire_Ad_Should_Shown_FromToolbar");
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                LauncherAnalytics.logEvent("Colorphone_CPUWire_Ad_Should_Shown");
                break;
        }
    }

    private void logInterstitialAdShow() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                if (ResultPageManager.getInstance().isFromBatteryImprover()) {
                    LauncherAnalytics.logEvent("ColorPhone_CableImproverWire_Show",
                            "From", ResultPageManager.getInstance().getFromTag());
                    Ap.Improver.logEvent("cableimproverwire_show");

                } else {
                    LauncherAnalytics.logEvent("Colorphone_BatteryWire_Ad_Shown");
                }
                AutoPilotUtils.logBatterywireAdShow();
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                LauncherAnalytics.logEvent("Colorphone_BoostWire_Ad_Shown_FromSettings");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PUSH:
                LauncherAnalytics.logEvent("Colorphone_BoostWire_Ad_Shown_FromPush");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
                LauncherAnalytics.logEvent("Colorphone_BoostWire_Ad_Shown_FromToolbar");
                AutoPilotUtils.logBoostwireAdShowFromToolbar();
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                LauncherAnalytics.logEvent("Colorphone_CPUWire_Ad_Shown");
                AutoPilotUtils.logCpuwireAdShow();
                break;
        }
    }

    private void logBoostDoneAdShow() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                if (ResultPageManager.getInstance().isFromBatteryImprover()) {
                    LauncherAnalytics.logEvent("ColorPhone_CableImproverDone_Show",
                            "From", ResultPageManager.getInstance().getFromTag());
                    Ap.Improver.logEvent("cableimproverdone_show");
                } else {
                    LauncherAnalytics.logEvent("Colorphone_BatteryDone_Ad_Shown");
                }
                AutoPilotUtils.logBatterydoneAdShow();
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Shown_FromSettings");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PUSH:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Shown_FromPush");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Shown_FromToolbar");
                AutoPilotUtils.logBoostdoneAdShowFromToolbar();
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                LauncherAnalytics.logEvent("Colorphone_CPUDone_Ad_Shown");
                AutoPilotUtils.logCpudoneAdShow();
                break;
        }
    }

    private void logBoostDoneAdClicked() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                LauncherAnalytics.logEvent("Colorphone_BatteryDone_Ad_Clicked");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Clicked_FromSettings");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Clicked_FromToolbar");
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PUSH:
                LauncherAnalytics.logEvent("Colorphone_BoostDone_Ad_Clicked_FromPush");
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                LauncherAnalytics.logEvent("Colorphone_CPUDone_Ad_Clicked");
                break;
        }
    }
}

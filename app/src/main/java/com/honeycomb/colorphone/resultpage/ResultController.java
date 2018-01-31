package com.honeycomb.colorphone.resultpage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ClipDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.AnimatorListenerAdapter;
import com.colorphone.lock.lockscreen.chargingscreen.view.FlashButton;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.RippleUtils;
import com.honeycomb.colorphone.util.Thunk;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;
import net.appcloudbox.common.ImageLoader.AcbImageLoader;
import net.appcloudbox.common.ImageLoader.AcbImageLoaderListener;
import net.appcloudbox.common.utils.AcbError;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public abstract class ResultController implements View.OnClickListener {

    protected static final String TAG = "ResultController";
    static final long FRAME = 100;
    static final long FRAME_HALF = 50;
    private static final int FRAME_40 = 40;

    private static final long START_DELAY_CARDS = FRAME / 10 * 86;
    @Thunk
    static final long DURATION_CARD_TRANSLATE = 440;

    @Thunk
    static final long DURATION_AD_OR_FUNCTION_TRANSLATE_DELAY = 0;

    @Thunk
    static final long DURATION_OPTIMAL_TEXT_TRANSLATION = 360;

    // Ad / charging defaultViewScreen
    private static final long START_DELAY_AD_OR_FUNCTION = 16 * FRAME;

    protected ResultPageActivity mActivity;
    int mScreenHeight;
    int mResultType;
    Type mType = Type.AD;

    private FrameLayout mTransitionView;
    private FrameLayout mAdOrFunctionContainerView;
    @Thunk View mResultView;
    private FrameLayout adOrFunctionView;
    private View mBgView;
    private View mHeaderTagView;
    private RelativeLayout mPageRootView;

    // Ad or charging defaultViewScreen
    private AcbNativeAdContainerView mAdContainer;
    private AcbNativeAdPrimaryView mAdImageContainer;
    private ViewGroup mAdChoice;
    private ImageView mAdIconView;
    private TextView mTitleTv;
    private TextView mDescriptionTv;
    private FlashButton mActionBtn;
//    private View primaryViewContainer;
    private View bottomContainer;
    private View iconContainer;
//    private View coverShader;

    public enum Type {
        AD,
        DEFAULT_VIEW,
    }

    public Interpolator softStopAccDecInterpolator = PathInterpolatorCompat.create(0.26f, 1f, 0.48f, 1f);

    //default view
    private View defaultViewPhone;
    private View defaultViewScreen;
    private View defaultViewStar1;
    private View defaultViewStar2;
    private View defaultViewStar3;
    private View defaultViewDescription;
    private ImageView defaultViewShield;
    private TextView defaultViewBtnOk;
    private ClipDrawable defaultViewTickDrawable;
    private CardOptimizedFlashView defaultViewFlashView;

    protected AcbInterstitialAd mInterstitialAd;

    ResultController() {
    }

    protected void init(ResultPageActivity activity, int resultType, Type type, @Nullable AcbInterstitialAd interstitialAd, @Nullable AcbNativeAd ad, List<CardData> cardDataList) {
        HSLog.d(TAG, "ResultController init *** resultType = " + resultType + " type = " + type);
        mActivity = activity;
        mType = type;
        logViewEvent(type);

        mResultType = resultType;
        mScreenHeight = Utils.getPhoneHeight(activity);

        LayoutInflater layoutInflater = LayoutInflater.from(activity);

        mPageRootView = ViewUtils.findViewById(activity, R.id.page_root_view);
        mPageRootView.post(new Runnable() {
            @Override public void run() {
                int height = mPageRootView.getHeight();
                if (mScreenHeight - height == Utils.getNavigationBarHeight(mActivity)) {
                    mScreenHeight = height;
                }
            }
        });
        mBgView = ViewUtils.findViewById(activity, R.id.bg_view);
        setBgViewSize();

        mHeaderTagView = ViewUtils.findViewById(activity, R.id.result_header_tag_view);

        mTransitionView = ViewUtils.findViewById(activity, R.id.transition_view_container);
        if (null != mTransitionView) {
            mTransitionView.removeAllViews();
            layoutInflater.inflate(getLayoutId(), mTransitionView, true);
            onFinishInflateTransitionView(mTransitionView);
        }
        mInterstitialAd = interstitialAd;

        switch (type) {
            case AD:
            case DEFAULT_VIEW:
                initAdOrFunctionView(activity, layoutInflater, ad);
                break;
        }
        mResultView = ViewUtils.findViewById(activity, R.id.result_view);
    }

    private void initAdOrFunctionView(Activity activity, LayoutInflater layoutInflater, AcbNativeAd ad) {
        HSLog.d(TAG, "initAdOrFunctionView");
        adOrFunctionView = ViewUtils.findViewById(activity, R.id.ad_or_function_view_container);
        if (null != adOrFunctionView) {
            int layoutId = getAdOrFunctionViewLayoutId();
            View resultContentView = layoutInflater.inflate(layoutId, mAdOrFunctionContainerView, false);
            mAdOrFunctionContainerView = adOrFunctionView;
            onFinishInflateResultView(resultContentView, ad);
            initActionButton(activity);
            mAdOrFunctionContainerView.setVisibility(View.VISIBLE);
        }
    }

    private int getAdOrFunctionViewLayoutId() {
        if (mType == Type.DEFAULT_VIEW) {
            return R.layout.result_page_default_view;
        }
        if (mType == Type.AD) {
            return R.layout.result_page_fullscreen_ad_container;
        }
        return R.layout.result_page_fullscreen;
    }

    private void initActionButton(Context context) {
        if (null != mActionBtn) {
            mActionBtn.setBackgroundDrawable(RippleUtils.createRippleDrawable(mActivity.getBackgroundColor(), 2));
        }
    }

    protected abstract int getLayoutId();

    protected abstract void onFinishInflateTransitionView(View transitionView);

    @SuppressWarnings("RestrictedApi")
    protected void onFinishInflateResultView(View resultView, final AcbNativeAd ad) {
        HSLog.d(TAG, "onFinishInflateResultView mType = " + mType);
        final Context context = getContext();

        if (mType == Type.DEFAULT_VIEW) {
            defaultViewPhone = ViewUtils.findViewById(resultView, R.id.phone);
            defaultViewScreen = ViewUtils.findViewById(resultView, R.id.screen);
            defaultViewStar1 = ViewUtils.findViewById(resultView, R.id.star_1);
            defaultViewStar2 = ViewUtils.findViewById(resultView, R.id.star_2);
            defaultViewStar3 = ViewUtils.findViewById(resultView, R.id.star_3);
            defaultViewShield = ViewUtils.findViewById(resultView, R.id.shield);
            ImageView tick = ViewUtils.findViewById(resultView, R.id.tick);
            defaultViewTickDrawable = (ClipDrawable) tick.getDrawable();
            defaultViewDescription = ViewUtils.findViewById(resultView, R.id.description);
            defaultViewBtnOk = ViewUtils.findViewById(resultView, R.id.btn_ok);
            defaultViewFlashView = ViewUtils.findViewById(resultView, R.id.flash_view);

            defaultViewShield.setImageResource(R.drawable.result_page_card_optimized_icon_boost);

            defaultViewBtnOk.setTextColor(((ResultPageActivity) context).getBackgroundColor());
            defaultViewBtnOk.setBackgroundDrawable(RippleUtils.createRippleDrawable(0xffffffff, 0xffeeeeee, 2));
            defaultViewBtnOk.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ((ResultPageActivity) context).finishAndNotify();
                }
            });

            defaultViewPhone.setTranslationY(Utils.pxFromDp(77));
            defaultViewPhone.setAlpha(0);
            defaultViewScreen.setTranslationY(Utils.pxFromDp(77));
            defaultViewScreen.setAlpha(0);
            defaultViewDescription.setAlpha(0);
            defaultViewBtnOk.setScaleX(0);
            defaultViewBtnOk.setAlpha(0);
            defaultViewTickDrawable.setLevel(0);
            defaultViewStar1.setAlpha(0);
            defaultViewStar2.setAlpha(0);
            defaultViewStar3.setAlpha(0);

            mAdOrFunctionContainerView.addView(resultView);
            return;
        }

        if (mType == Type.AD) {
            final View containerView = LayoutInflater.from(getContext()).inflate(
                    R.layout.result_page_fullscreen_ad, (ViewGroup) resultView, false);

            mAdImageContainer = ViewUtils.findViewById(containerView, R.id.result_image_container_ad);
            mAdImageContainer.setBitmapConfig(Bitmap.Config.RGB_565);
            int targetWidth = Dimensions.getPhoneWidth(context) - 2 * Dimensions.pxFromDp(27) - 2 * Dimensions.pxFromDp(20);
            int targetHeight = (int) (targetWidth / 1.9f);
            mAdImageContainer.setTargetSizePX(targetWidth, targetHeight);

            mAdChoice = ViewUtils.findViewById(containerView, R.id.result_ad_choice);
            mTitleTv = ViewUtils.findViewById(containerView, R.id.promote_charging_title);
            mDescriptionTv = ViewUtils.findViewById(containerView, R.id.promote_charging_content);
            mActionBtn = ViewUtils.findViewById(containerView, R.id.promote_charging_button);
            mAdIconView = ViewUtils.findViewById(containerView, R.id.result_ad_icon);
            mActionBtn.setRepeatCount(0);

            ImageView btn = ViewUtils.findViewById(resultView, R.id.result_page_fullscreen_ad_dismiss_btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mActivity.finishAndNotify();
                }
            });

//            primaryViewContainer = ViewUtils.findViewById(resultView, R.id.promote_charging_content_top_container);
//            bottomContainer = ViewUtils.findViewById(containerView, R.id.promote_charging_bottom_container);
//            iconContainer = ViewUtils.findViewById(resultView, R.id.promote_charging_icon_container);
//            coverShader = ViewUtils.findViewById(resultView, R.id.cover_icon);
//            coverShader.setBackgroundColor(mActivity.getBackgroundColor());
//
//            mAdImageContainer.setBitmapConfig(Bitmap.Config.RGB_565);
//            int targetWidth = Utils.getPhoneWidth(context) - 2 * Utils.pxFromDp(27) - 2 * Utils.pxFromDp(20);
//            int targetHeight = (int) (targetWidth / 1.9f);
//            mAdImageContainer.setTargetSizePX(targetWidth, targetHeight);

            AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
            adContainer.addContentView(containerView);
            adContainer.setAdTitleView(mTitleTv);
            adContainer.setAdBodyView(mDescriptionTv);
            adContainer.setAdPrimaryView(mAdImageContainer);
            adContainer.setAdChoiceView(mAdChoice);
            adContainer.setAdActionView(mActionBtn);

            mAdImageContainer.setVisibility(View.VISIBLE);
            final boolean valid = new AcbImageLoader(getContext()).loadRemote(getContext(), ad.getIconUrl(),
                    ad.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_ICON), new AcbImageLoaderListener() {
                        @Override
                        public void imageLoaded(Bitmap bitmap) {
                            mAdIconView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void imageFailed(AcbError acbError) {
                            new AcbImageLoader(getContext()).loadRemote(getContext(), ad.getImageUrl(),
                                    ad.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_IMAGE), new AcbImageLoaderListener() {
                                        @Override
                                        public void imageLoaded(Bitmap bitmap) {
                                            Bitmap newBitmap;
                                            try {
                                                newBitmap = createCenterCropBitmap(bitmap, mAdIconView.getWidth(), mAdIconView.getHeight());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                newBitmap = bitmap;
                                            }

                                            mAdIconView.setImageBitmap(newBitmap);
                                        }

                                        @Override
                                        public void imageFailed(AcbError acbError1) {

                                        }
                                    }, null);
                        }
                    }, null);
            if (!valid) {
                new AcbImageLoader(getContext()).loadRemote(getContext(), ad.getImageUrl(),
                        ad.getResourceFilePath(AcbNativeAd.LOAD_RESOURCE_TYPE_IMAGE), new AcbImageLoaderListener() {
                            @Override
                            public void imageLoaded(Bitmap bitmap) {
                                Bitmap newBitmap;
                                try {
                                    newBitmap = createCenterCropBitmap(bitmap, mAdIconView.getWidth(), mAdIconView.getHeight());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    newBitmap = bitmap;
                                }
                                mAdIconView.setImageBitmap(newBitmap);
                            }

                            @Override
                            public void imageFailed(AcbError acbError) {

                            }
                        }, null);
            }

            List<View> clickViews = new ArrayList<>();
            if (adContainer.getAdActionView() != null) {
                clickViews.add(adContainer.getAdActionView());
            }

            if (adContainer.getAdBodyView() != null) {
                clickViews.add(adContainer.getAdBodyView());
            }

            if (adContainer.getAdTitleView() != null) {
                clickViews.add(adContainer.getAdTitleView());
            }

            if (adContainer.getAdIconView() != null) {
                clickViews.add(adContainer.getAdIconView());
            }

            if (adContainer.getAdPrimaryView() != null) {
                clickViews.add(adContainer.getAdPrimaryView());
            }

            adContainer.setClickViewList(clickViews);

            mAdContainer = adContainer;
            fillNativeAd(ad);
            FrameLayout container = resultView.findViewById(R.id.result_page_fullscreen_ad);
            container.addView(adContainer);
            mAdOrFunctionContainerView.addView(resultView);
        }
    }

    void fillNativeAd(AcbNativeAd ad) {
        if (mAdContainer != null) {
            mAdContainer.fillNativeAd(ad);
        }
    }

    protected abstract void onStartTransitionAnimation(View transitionView);

    protected void onFunctionCardViewShown() {

    }

    void startTransitionAnimation() {
        HSLog.d(TAG, "startTransitionAnimation mTransitionView = " + mTransitionView);
        if (null != mTransitionView) {
            onStartTransitionAnimation(mTransitionView);
            if (mType == Type.AD) {
                startAdOrFunctionResultAnimation(START_DELAY_AD_OR_FUNCTION);
            }
        }
    }

    public void onTransitionAnimationEnd() {
        if (mType == Type.AD) {
            startAdOrFunctionResultAnimation(DURATION_AD_OR_FUNCTION_TRANSLATE_DELAY);
        } else {
            startDefaultViewAnimation();
        }
    }

    public void startDefaultViewAnimation() {
        mResultView.setVisibility(View.VISIBLE);
        adOrFunctionView.setTranslationY(0);

        defaultViewPhone.animate().alpha(1).translationY(0).setDuration(8 * FRAME_40).start();
        defaultViewScreen.animate().alpha(1).translationY(0).setDuration(8 * FRAME_40).start();
        defaultViewDescription.animate().alpha(1).setDuration(7 * FRAME_40).setStartDelay(5 * FRAME_40).start();
        defaultViewBtnOk.animate().alpha(1).scaleX(1).setDuration(8 * FRAME_40).setStartDelay(FRAME_40).start();

        ValueAnimator tickAnimator = ValueAnimator.ofInt(0, 10000);
        tickAnimator.setDuration(7 * FRAME_40);
        tickAnimator.setStartDelay(20 * FRAME_40);
        tickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                int level = (int) animation.getAnimatedValue();
                defaultViewTickDrawable.setLevel(level);
            }
        });
        tickAnimator.start();

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(defaultViewShield,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.1f, 1.14f, 1.17f, 1.2f, 1.21f, 1.2f, 1.17f, 1.13f, 1.06f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.1f, 1.14f, 1.17f, 1.2f, 1.21f, 1.2f, 1.17f, 1.13f, 1.06f, 1f));
        animator.setDuration(10 * FRAME_40);
        animator.setStartDelay(21 * FRAME_40);
        animator.start();

        ObjectAnimator star1Animator = ObjectAnimator.ofFloat(defaultViewStar1, View.ALPHA, 0f, 1f, 0.15f, 0.89f);
        star1Animator.setInterpolator(new LinearInterpolator());
        star1Animator.setDuration(26 * FRAME_40);
        star1Animator.setStartDelay(28 * FRAME_40);
        star1Animator.start();

        ObjectAnimator star2Animator = ObjectAnimator.ofFloat(defaultViewStar2, View.ALPHA, 0f, 0.8f, 0.1f, 0.8f);
        star2Animator.setInterpolator(new LinearInterpolator());
        star2Animator.setDuration(28 * FRAME_40);
        star2Animator.setStartDelay(34 * FRAME_40);
        star2Animator.start();

        defaultViewStar3.animate()
                .setInterpolator(new LinearInterpolator())
                .alpha(.6f)
                .setDuration(15 * FRAME_40)
                .setStartDelay(37 * FRAME_40)
                .start();

        defaultViewScreen.postDelayed(new Runnable() {
            @Override public void run() {
                defaultViewFlashView.startFlashAnimation();
            }
        }, 8 * FRAME_40);
    }

    public void startAdOrFunctionResultAnimation(long startDelay) {
        mResultView.postDelayed(new Runnable() {
            @Override public void run() {

                mResultView.setVisibility(View.VISIBLE);
//                if (coverShader != null) {
//                    coverShader.setVisibility(View.VISIBLE);
//                }

//                primaryViewContainer.setAlpha(1.0f);
//                bottomContainer.setAlpha(1f);
//                iconContainer.setAlpha(1f);
//                iconContainer.setScaleX(1.0f);
//                iconContainer.setScaleY(1.0f);

                startCardTranslationAnimation(adOrFunctionView, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });

            }
        }, startDelay);
    }

    private void startCardTranslationAnimation(final View view, AnimatorListenerAdapter animatorListenerAdapter) {
        final float slideUpTranslationFrom = mScreenHeight - mActivity.getResources().getDimensionPixelSize(R.dimen.result_page_header_height)
                - Dimensions.getStatusBarHeight(mActivity) - Dimensions.pxFromDp(15);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_CARD_TRANSLATE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.setTranslationY(slideUpTranslationFrom * (1 - value));
            }
        });
        animator.setInterpolator(softStopAccDecInterpolator);
        animator.addListener(animatorListenerAdapter);
        animator.start();
    }

    private void setBgViewSize() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBgView.getLayoutParams();
        params.width = Utils.getPhoneWidth(getContext());
        params.height = Utils.getPhoneHeight(getContext());
        mBgView.setLayoutParams(params);
    }

    protected Context getContext() {
        return mActivity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.promote_charging_button:
                switch (mType) {
                }
                break;
            default:
                break;
        }
    }

    private void logViewEvent(Type type) {
        if (type == Type.AD) {
            LauncherAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.AD);
        }

        LauncherAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.AD);
    }

    protected void logClickEvent(Type type) {
    }

    private String getLogEventType(int order) {
        switch (order) {
            case 1:
                return "First";
            case 2:
                return "Second";
            case 3:
                return "Third";
            default:
                return "other";
        }
    }

    private Bitmap createCenterCropBitmap(@NonNull Bitmap bitmap, float width, float height) throws Exception {
        if (width <= 0 || height <= 0) {
            return bitmap;
        }

        final float bWidth = bitmap.getWidth();
        final float bHeight = bitmap.getHeight();

        if (width / height >= bWidth / bHeight) {
            return Bitmap.createBitmap(bitmap, 0, 0, (int) bWidth, (int) (bWidth * height / width));
        }

        final float offset = (bWidth - bHeight * width / height) / 2f;
        return Bitmap.createBitmap(bitmap, (int) offset, 0, (int) (bWidth - 2f * offset), (int) bHeight);
    }

    protected void popupInterstitialAdIfNeeded() {
        if (mInterstitialAd != null) {
            popupInterstitialAd();
        } else {
            onInterruptActionClosed();
        }
    }

    private void popupInterstitialAd() {
        mInterstitialAd.setCustomTitle(HSApplication.getContext().getString(R.string.boost_plus_optimal));
        mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
            @Override
            public void onAdDisplayed() {

            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.release();
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        onInterruptActionClosed();
                    }
                }, 250);
            }
        });
        mInterstitialAd.show();
    }

    protected abstract void onInterruptActionClosed();
}

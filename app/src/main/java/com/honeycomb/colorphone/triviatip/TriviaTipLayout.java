package com.honeycomb.colorphone.triviatip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.download2.Downloader;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.messagecenter.views.RipplePopupView;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Preferences;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;



public class TriviaTipLayout extends FrameLayout implements View.OnClickListener, HomeKeyWatcher.OnHomePressedListener {

    public static final String NOTIFICATION_TRIVIA_TIP_DISABLE_CLICKED = "trivia_tip_disable_clicked";

    private static final String PREF_KEY_SHOW_TIME = "show_time";

    private ImageView mTipBg;
    private TextView mTipTitle;
    private TextView mTipBtn;
    private View mTipDisable;
    private RipplePopupView mDisablePopup;
    private View mTipClose;
    private TextView mTurnOff;
    private ViewGroup mTipContainer;
    private View mTrulyFact;
    private TextView mBottomDesc;
    private ImageView mContentBg;
    private View mContentMask;
    private ViewGroup mTopDescContainer;
    private TextView mTopDesc;
    private ViewGroup mNativeAdContainer;
    private ViewGroup mNativeAdContent;
    private TextView mTipHeadline;
    private View mCloseAd;
    private View mButtonSetTheme;

    private GradientDrawable mGradientDrawable;
    private BitmapDrawable mBgBitmapDrawable;

    private AcbInterstitialAd mInterstitialAd;
    private AcbNativeAd mNativeAd;

    private boolean mExecuteBackFromAd;

    private HomeKeyWatcher mHomeKeyWatcher;

    private int mShowTime;

    private boolean mShowingTip = true;
    private boolean mShowInterstitialAd;

    private onTipDismissListener mOnDismissListener;
    private String mNativeSource;
    private int mItemId;
    private String mImageFilePath;

    public TriviaTipLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + Dimensions.getNavigationBarHeight(context));
        mHomeKeyWatcher = new HomeKeyWatcher(context);
        mHomeKeyWatcher.setOnHomePressedListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTipBg = findViewById(R.id.tip_bg);
        mTipTitle = findViewById(R.id.tip_title);
        mTipBtn = findViewById(R.id.tip_button);
        mTipClose = findViewById(R.id.tip_close_btn);
        mTipBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFF448AFF, Dimensions.pxFromDp(22), true));
        mTipBtn.setOnClickListener(this);
        mTipDisable = findViewById(R.id.tip_disable);
        mTipDisable.setOnClickListener(this);
        mTipClose.setOnClickListener(this);
        mTipContainer = findViewById(R.id.tip_container);
        mTipContainer.setOnClickListener(this);
        mGradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xB3000000, 0x00000000});
        mTrulyFact = findViewById(R.id.content_truly_fact);
        mBottomDesc = findViewById(R.id.bottom_desc);
        mContentBg = findViewById(R.id.content_bg_iv);
        mContentMask = findViewById(R.id.content_bg_mask);
        mContentMask.setBackground(mGradientDrawable);
        mTopDescContainer = findViewById(R.id.top_des_container);
        mTopDesc = findViewById(R.id.content_top_desc);
        mNativeAdContainer = findViewById(R.id.native_ad_container);
        mTipHeadline = findViewById(R.id.tip_headline);
        mNativeAdContent = findViewById(R.id.native_ad_content);
        mCloseAd = findViewById(R.id.close_ad_iv);
        mCloseAd.setOnClickListener(this);
        mTipBg.setOnClickListener(this);

        mButtonSetTheme = findViewById(R.id.content_top_set_as_theme);

        View turnOffContainer = View.inflate(getContext(), com.messagecenter.R.layout.acb_alert_disable_popup_view, null);
        mTurnOff = turnOffContainer.findViewById(com.messagecenter.R.id.tv_turn_off);
        mTurnOff.setText(getResources().getString(com.messagecenter.R.string.acb_alert_disable_call_alert));
        mTurnOff.measure(0, 0);
        mDisablePopup = new RipplePopupView(ActivityUtils.contextToActivitySafely(getContext()));
        mDisablePopup.setOutSideBackgroundColor(Color.TRANSPARENT);
        mDisablePopup.setContentView(turnOffContainer);
        mDisablePopup.setOutSideClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisablePopup.dismiss();

            }
        });
        mTurnOff.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(TriviaTip.PREF_KEY_TRIVIA_TIP_DISABLE_CLICKED, true);
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_TRIVIA_TIP_DISABLE_CLICKED);
                LauncherAnalytics.logEvent("Fact_Alert_Disable_Click_New", true, "ClickTimes", String.valueOf(mShowTime));
                LauncherAnalytics.logEvent("Fact_Alert_Dismiss_New", true, "CloseMethod", "AlertDisableBtn");

                LauncherAnalytics.logEvent("trivia_settings_close_click");
            }
        });

        mButtonSetTheme.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Ap.TriviaTip.logEvent("trivia_detail_apply_btn_click");
                LauncherAnalytics.logEvent("trivia_detail_apply_btn_click",
                        "From" , mNativeAdContainer.getVisibility() == VISIBLE ? "wirepage" : "donepage",
                        "ThemeName", String.valueOf(mItemId)
                );

                Utils.showToast(getResources().getString(R.string.apply_success));
                ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, Type.STATIC);
                ScreenFlashSettings.putStaticImagePath(mImageFilePath);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
            mInterstitialAd = null;
        }

        if (mNativeAd != null) {
            mNativeAd.release();
            mNativeAd = null;
        }
    }

    void setOnDismissListener(onTipDismissListener listener) {
        mOnDismissListener = listener;
    }

    void bind(TriviaItem triviaItem) {
        mImageFilePath = Downloader.getDownloadPath(TriviaTip.DOWNLOAD_DIRECTORY, triviaItem.imgUrl);
        mBgBitmapDrawable = new BitmapDrawable(getResources(), mImageFilePath);
        mTipBg.setImageDrawable(mBgBitmapDrawable);
        mTipTitle.setText(triviaItem.title);
        mBottomDesc.setText(triviaItem.desc);
        mTopDesc.setText(triviaItem.desc);
        mTipBtn.setText(Ap.TriviaTip.buttonDesc());
        mTipHeadline.setText(triviaItem.headLine);
        mItemId = triviaItem.id;
    }


    void show() {
        mShowTime = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_SHOW_TIME);
        HSLog.d("TriviaTip", "Show " + mShowTime + " times");
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
            mInterstitialAd = null;
        }

//        ActivityUtils.setNavigationBarColor(mLauncher, Color.BLACK);
        mHomeKeyWatcher.startWatch();
        LauncherAnalytics.logEvent("Fact_Alert_Shown_New", true, "ShowTimes", String.valueOf(mShowTime));
    }

    void onResume() {
        if (mShowInterstitialAd) {
            postDelayed(this::backFromAd, 750);
        }
    }

    void onBackPressed() {
        boolean allowBack = HSConfig.optBoolean(false, "Application", "TriviaFact", "AllowBack");
        HSLog.d("TriviaTip", "allowBack: " + allowBack);
        LauncherAnalytics.logEvent("trivia_close", "type", "back");
        if (!mShowingTip || allowBack) {
            dismiss();
        }
        if (mShowingTip && allowBack) {
            LauncherAnalytics.logEvent("Fact_Alert_Dismiss_New", true, "CloseMethod", "SystemBackBtn");
        }
    }

    private void showContent() {
        LauncherAnalytics.logEvent("trivia_detail_show");
        Ap.TriviaTip.logEvent("trivia_detail_show");

        final int translationY = Dimensions.pxFromDp(7);
        mTipContainer.animate().setDuration(200).alpha(0).withEndAction(() -> {
            mTipContainer.setVisibility(GONE);

            mContentBg.setTranslationY(translationY);
            mContentBg.setAlpha(0f);
            mContentBg.setImageDrawable(mBgBitmapDrawable);
            mContentBg.setVisibility(VISIBLE);

            mContentMask.setTranslationY(translationY);
            mContentMask.setAlpha(0);
            mContentMask.setVisibility(VISIBLE);

            mTrulyFact.setTranslationY(translationY);
            mTrulyFact.setAlpha(0);
            mTrulyFact.setVisibility(VISIBLE);

            mBottomDesc.setTranslationY(translationY);
            mBottomDesc.setAlpha(0);
            mBottomDesc.setVisibility(VISIBLE);

            if (Ap.TriviaTip.buttonApplyShow()) {
                mButtonSetTheme.setAlpha(0);
                mButtonSetTheme.setTranslationY(translationY);
                mButtonSetTheme.setVisibility(VISIBLE);
            }

            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(360);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedFraction = animation.getAnimatedFraction();
                    int currentTranslationY = (int) ((1 - animatedFraction) * translationY);

                    mContentBg.setAlpha(animatedFraction);
                    mContentBg.setTranslationY(currentTranslationY);

                    mContentMask.setAlpha(animatedFraction);
                    mContentMask.setTranslationY(currentTranslationY);

                    mTrulyFact.setAlpha(animatedFraction);
                    mTrulyFact.setTranslationY(currentTranslationY);

                    mBottomDesc.setAlpha(animatedFraction);
                    mBottomDesc.setTranslationY(currentTranslationY);

                    mButtonSetTheme.setAlpha(animatedFraction * 0.8f);
                    mButtonSetTheme.setTranslationY(currentTranslationY);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    showInterstitialAd();
                }
            });
            animator.start();
        }).start();
        mShowingTip = false;
    }

    public static AcbInterstitialAd tryGetInterstitialAd(String placement) {
        List<AcbInterstitialAd> fetchedAds = AcbInterstitialAdManager.fetch(placement, 1);
        if (fetchedAds != null && !fetchedAds.isEmpty()) {
            return fetchedAds.get(0);
        }
        return null;
    }

    public static AcbNativeAd tryGetNativeAd(String placement) {
        List<AcbNativeAd> fetchedAds = AcbNativeAdManager.fetch(placement, 1);
        if (fetchedAds != null && !fetchedAds.isEmpty()) {
            return fetchedAds.get(0);
        }
        return null;
    }

    private void showInterstitialAd() {
        mInterstitialAd = tryGetInterstitialAd(Placements.TRIVIA_TIP_INTERSTITIAL_AD_PLACEMENT_NAME);
        HSLog.d("TriviaTip", "1 mInterstitialAd = " + mInterstitialAd);

        LauncherAnalytics.logEvent("trivia_detail_wire_should_show");
        Ap.TriviaTip.logEvent("trivia_detail_wire_should_show");

        if (mInterstitialAd == null) {
            mInterstitialAd = tryGetInterstitialAd(Placements.BOOST_WIRE);
            if (mInterstitialAd != null) {
                LauncherAnalytics.logEvent("trivia_detail_wire_show", "From", "BoostWire");
            }
        } else {
            LauncherAnalytics.logEvent("trivia_detail_wire_show", "From", "TriviaWire");
        }
        HSLog.d("TriviaTip", "2 mInterstitialAd = " + mInterstitialAd);
        if (mInterstitialAd != null) {
            mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {

                @Override
                public void onAdDisplayed() {
                }

                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdDisplayFailed(AcbError acbError) {

                }
            });
            mInterstitialAd.show();
            mShowInterstitialAd = true;
            Ap.TriviaTip.logEvent("trivia_detail_wire_show");
        } else {
            postDelayed(this::backFromAd, 750);
        }
    }

    private void backFromAd() {
        if (mExecuteBackFromAd) {
            return;
        }
        if (mNativeAd != null) {
            mNativeAd.release();
            mNativeAd = null;
        }

        LauncherAnalytics.logEvent("Fact_Detail_Page_Shown_New", true);
        LauncherAnalytics.logEvent("trivia_detail_done_should_show");
        Ap.TriviaTip.logEvent("trivia_detail_done_should_show");
        mNativeAd = tryGetNativeAd(Placements.TRIVIA_TIP_NATIVE_AD_PLACEMENT_NAME);
        HSLog.d("TriviaTip", "mNativeAd: " + mNativeAd);
        if (mNativeAd == null) {
            mNativeAd = tryGetNativeAd(Placements.BOOST_DOWN);
            if (mNativeAd != null) {
                mNativeSource = "BoostDone";
            }
        } else {
            mNativeSource = "TriviaDone";
        }

        if (mNativeAd == null) {
            return;
        }
        mExecuteBackFromAd = true;
        final int translationY = -Dimensions.pxFromDp(7);
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(240);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                int currentTranslationY = (int) (animatedFraction * translationY);

                mTrulyFact.setAlpha(1 - animatedFraction);
                mTrulyFact.setTranslationY(currentTranslationY);

                mBottomDesc.setAlpha(1 - animatedFraction);
                mBottomDesc.setTranslationY(currentTranslationY);
            }
        });
        animator.start();
        postDelayed(this::showTopDescription, 120);
    }

    private void showTopDescription() {
        ValueAnimator maskAnimator = ValueAnimator.ofFloat(0f, 1f);
        maskAnimator.setDuration(320);
        maskAnimator.addUpdateListener(new GradientAnimatorUpdateListener());
        maskAnimator.start();

        final int translationY = Dimensions.pxFromDp(7);
        mTopDescContainer.setTranslationY(translationY);
        mTopDescContainer.setAlpha(0f);
        mTopDescContainer.setVisibility(VISIBLE);

        ValueAnimator topDescAnimator = ValueAnimator.ofFloat(0f, 1f);
        topDescAnimator.setDuration(240);
        topDescAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedFraction = animation.getAnimatedFraction();
                int currentTranslationY = (int) ((1 - animatedFraction) * translationY);

                mTopDescContainer.setTranslationY(currentTranslationY);
                mTopDescContainer.setAlpha(animatedFraction);
            }
        });
        topDescAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                showNativeAd();
            }
        });
        topDescAnimator.start();
    }

    private void showNativeAd() {
        if (mNativeAd != null) {
            View adView = View.inflate(getContext(), R.layout.trivia_tip_content_native_ad, null);
            AcbNativeAdContainerView adContainer = new AcbNativeAdContainerView(getContext());
            adContainer.addContentView(adView);

            View actionButton = adView.findViewById(R.id.action_button);
            actionButton.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFF5286EC, Dimensions.pxFromDp(5), true));

            adContainer.setAdChoiceView(adView.findViewById(R.id.ad_choice));
            adContainer.setAdActionView(actionButton);
            adContainer.setAdBodyView(adView.findViewById(R.id.description));
            adContainer.setAdTitleView(adView.findViewById(R.id.title));
            adContainer.setAdIconView(adView.findViewById(R.id.icon_view));
            adContainer.setAdPrimaryView(adView.findViewById(R.id.primary_view));

            mNativeAdContent.removeAllViews();
            mNativeAdContent.addView(adContainer);

            adContainer.fillNativeAd(mNativeAd);

            mNativeAdContainer.setAlpha(0f);
            mNativeAdContainer.setVisibility(VISIBLE);
            Interpolator interpolator = PathInterpolatorCompat.create(0.33f, 0f, 0.83f, 0.83f);
            mNativeAdContainer.animate().alpha(1).setInterpolator(interpolator).setDuration(480).start();
            LauncherAnalytics.logEvent("trivia_detail_done_show", "From", mNativeSource);
            Ap.TriviaTip.logEvent("trivia_detail_done_show");
        }
    }

    void dismiss() {
        mDisablePopup.dismiss();
        mHomeKeyWatcher.stopWatch();
//        ActivityUtils.setNavigationBarColor(mLauncher, Color.TRANSPARENT);
//        mLauncher.removeOverlay(this);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tip_bg:
                showContent();
                LauncherAnalytics.logEvent("trivia_click","ClickRange", "OutBtn");
                LauncherAnalytics.logEvent("Fact_Alert_Click_New", true, "ClickTimes", String.valueOf(mShowTime), "ClickRange", "OutBtn");
                break;
            case R.id.tip_button:
                showContent();
                LauncherAnalytics.logEvent("trivia_button_click");
                Ap.TriviaTip.logEvent("trivia_button_click");

                LauncherAnalytics.logEvent("trivia_click","ClickRange", "InBtn");
                LauncherAnalytics.logEvent("Fact_Alert_Click_New", true, "ClickTimes", String.valueOf(mShowTime), "ClickRange", "InBtn");
                break;
            case R.id.tip_disable:
                int offsetX = -(mTurnOff.getMeasuredWidth() - v.getWidth() / 2);
                int offsetY = -(v.getHeight() + (Dimensions.pxFromDp(22) - v.getHeight()) / 2);
                mDisablePopup.showAsDropDown(v, offsetX, offsetY);
                LauncherAnalytics.logEvent("trivia_settings_click");
                break;
            case R.id.tip_close_btn:
                dismiss();
                LauncherAnalytics.logEvent("trivia_close", "type", "closebtn");
                LauncherAnalytics.logEvent("Fact_Alert_Dismiss_New", true, "CloseMethod", "AlertCloseIcon");
                break;
            case R.id.close_ad_iv:
                mNativeAdContainer.setVisibility(GONE);
                break;
            case R.id.tip_container:
                dismiss();
                LauncherAnalytics.logEvent("Fact_Alert_Dismiss_New", true, "CloseMethod", "BlackArea");
                break;
        }
    }

    private static int argb(float alpha, float red, float green, float blue) {
        return ((int) (alpha * 255.0f + 0.5f) << 24) |
                ((int) (red * 255.0f + 0.5f) << 16) |
                ((int) (green * 255.0f + 0.5f) << 8) |
                (int) (blue * 255.0f + 0.5f);
    }

    @Override
    public void onHomePressed() {
        dismiss();
        LauncherAnalytics.logEvent("trivia_close", "home", "back");
        if (mShowingTip) {
            LauncherAnalytics.logEvent("Fact_Alert_Dismiss_New", true, "CloseMethod", "SystemHomeBtn");
        }
    }

    @Override
    public void onRecentsPressed() {

    }

    private class GradientAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private int[] gradientColor = new int[2];

        GradientAnimatorUpdateListener() {
            gradientColor[0] = 0xB3000000;
            gradientColor[1] = 0x00000000;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float animatedFraction = animation.getAnimatedFraction();
            gradientColor[1] = argb(0.7f * animatedFraction, 0f, 0f, 0f);
            mGradientDrawable.setColors(gradientColor);
        }
    }

    public interface onTipDismissListener {

        void onDismiss();
    }
}

package com.honeycomb.colorphone.resultpage;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.boost.AdUtils;
import com.honeycomb.colorphone.boost.BoostAutoPilotUtils;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.interstitialads.AcbInterstitialAdLoader;
import net.appcloudbox.ads.nativeads.AcbNativeAdLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents Boost+ / Battery / CPU Cooler / Junk Cleaner / Notification Cleaner result page contents.
 */
public class ResultPagePresenter implements ResultPageContracts.Presenter {

    public static final String TAG = ResultPagePresenter.class.getSimpleName();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_ALL_CARDS = false && BuildConfig.DEBUG;

    private static final String PREF_KEY_CARDS_SHOW_COUNT = "result_page_cards_show_count";

    private static final boolean SHOULD_ADD_GUIDE_CARD = HSConfig.optBoolean(true, "Application", "ResultPage", "ShowFunctionGuideCards");

    private static final int MAX_COUNT_SHOW_BP_PROMOTION_CARD = HSConfig.optInteger(3, "Application", "ResultPagePromotion", "BatteryProtection");
    private static final int MAX_COUNT_SHOW_NC_PROMOTION_CARD = HSConfig.optInteger(3, "Application", "ResultPagePromotion", "NotificationCleaner");
    private static final int MAX_COUNT_SHOW_AL_PROMOTION_CARD = HSConfig.optInteger(3, "Application", "ResultPagePromotion", "APPLock");

    private ResultPageContracts.View mView;

    private int mResultType;
    private ResultController.Type mType;
    private List<CardData> mCards = new ArrayList<>();
    private AcbNativeAd mNativeAd;
    private AcbInterstitialAd mInterstitialAd;
    private boolean mWillShowInterstitialAd = false;

    ResultPagePresenter(@NonNull ResultPageContracts.View view, int resultType) {
        mView = view;
        mResultType = resultType;
    }

    @Override
    public void show() {
        mType = ResultController.Type.DEFAULT_VIEW;

        fetchAds();
        AdUtils.preloadResultPageAds();

        if (mWillShowInterstitialAd) {
            HSLog.i("Boost", "show Interstitial");
            BoostAutoPilotUtils.logBoostPushAdShow();
        } else if (mType == ResultController.Type.AD) {
            HSLog.i("Boost", "show AD");
            BoostAutoPilotUtils.logBoostPushAdShow();
            logPageContent();
            mView.show(mType, mInterstitialAd, mNativeAd, mCards);
//        } else if (mType == ResultController.Type.CARD_VIEW) {
//            if (!tryToShowCardView()) showDefaultView();
        } else {
            HSLog.i("Boost", "show default");
            showDefaultView();
        }
    }

    private void fetchAds() {
        List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE_INTERSTITIAL, 1);
        mInterstitialAd = interstitialAds.isEmpty() ? null : interstitialAds.get(0);
        LauncherAnalytics.logEvent("InterstitialAdAnalysis", "ad_show_from", "ResultPage+" + (mInterstitialAd != null));
        LauncherAnalytics.logEvent("AcbAdNative_Viewed_In_App",  AdPlacements.AD_RESULT_PAGE_INTERSTITIAL, String.valueOf(mInterstitialAd != null));

        String adCombination;
        if (mInterstitialAd != null) {
//            mType = ResultController.Type.CARD_VIEW;
            mType = ResultController.Type.DEFAULT_VIEW;
            mWillShowInterstitialAd = true;

            List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE, 1);
            mNativeAd = ads.isEmpty() ? null : ads.get(0);
            LauncherAnalytics.logEvent("AcbAdNative_Viewed_In_App",  AdPlacements.AD_RESULT_PAGE, String.valueOf(mNativeAd != null));
        } else {
            List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE, 1);
            mNativeAd = ads.isEmpty() ? null : ads.get(0);
            LauncherAnalytics.logEvent("AcbAdNative_Viewed_In_App",  AdPlacements.AD_RESULT_PAGE, String.valueOf(mNativeAd != null));
            if (mNativeAd != null) {
                mType = ResultController.Type.AD;
                HSLog.d(TAG, "result page ad type is " + mNativeAd.getVendorConfig().name());
                if (AdUtils.isFacebookAd(mNativeAd)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            mView.showExitBtn();
                        }
                    }, 2500);
                }
            }
        }
    }

    private void showDefaultView() {
        mType = ResultController.Type.DEFAULT_VIEW;
        logPageContent();
        mView.show(mType, mInterstitialAd, mNativeAd, mCards);
    }

    private void logPageContent() {
        String pageContentDescription = null;
        switch (mType) {
            case AD:
                pageContentDescription = "AD";
                break;
            case DEFAULT_VIEW:
                pageContentDescription = "DefaultPage";
                break;
        }
        if (pageContentDescription != null) {
            LauncherAnalytics.logEvent("ResultPage_Content_Show", "type", pageContentDescription);
        }
    }
}

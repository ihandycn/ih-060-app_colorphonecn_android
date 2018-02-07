package com.honeycomb.colorphone.boost;

import com.honeycomb.colorphone.AdPlacements;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

public class AdUtils {

    private static final String TAG = AdUtils.class.getSimpleName();

    public static boolean isFacebookAd(AcbNativeAd ad) {
        return ad != null && "facebooknative".equalsIgnoreCase(ad.getVendorConfig().name());
    }

    public static void preloadResultPageAds() {
        HSLog.d(TAG, "result page preload ads.");

        AcbNativeAdManager.preload(HSApplication.getContext(), 1, AdPlacements.AD_RESULT_PAGE);
        AcbInterstitialAdManager.preload(HSApplication.getContext(), 1, AdPlacements.AD_RESULT_PAGE_INTERSTITIAL);
    }
}

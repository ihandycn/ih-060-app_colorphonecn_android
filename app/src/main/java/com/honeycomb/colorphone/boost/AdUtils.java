package com.honeycomb.colorphone.boost;

import com.honeycomb.colorphone.Placements;
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
        HSLog.d(TAG, "result page preloadForExitNews ads.");
        AcbNativeAdManager.getInstance().preload(1, Placements.getAdPlacement(Placements.BOOST_DONE));
        AcbInterstitialAdManager.getInstance().preload(1, Placements.getAdPlacement(Placements.BOOST_WIRE));
    }
}

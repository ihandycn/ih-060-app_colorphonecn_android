package com.honeycomb.colorphone.ad;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.List;

public class AdManager {

    private static final String TAG = AdManager.class.getSimpleName();

    private AcbNativeAd mAd = null;
    private AcbInterstitialAd mInterstitialAd = null;

    private static AdManager sInstance;


    private AdManager() {
    }

    public static AdManager getInstance() {
        if (sInstance == null) {
            synchronized (ResultPageManager.class) {
                if (sInstance == null) {
                    sInstance = new AdManager();
                }
            }
        }
        return sInstance;
    }

    public void preload() {
        HSLog.d(TAG, "preload");
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(getInterstitialAdPlacementName());
        AcbInterstitialAdManager.preload(1, getInterstitialAdPlacementName());
    }

    private static String getInterstitialAdPlacementName() {
        return Placements.BOOST_WIRE;
    }

    public AcbInterstitialAd getInterstitialAd() {
        if (mInterstitialAd == null) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(getInterstitialAdPlacementName(), 1);
            if (ads != null && ads.size() > 0) {
                mInterstitialAd = ads.get(0);
            }
        }
        return mInterstitialAd;
    }

    public void releaseInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
            mInterstitialAd = null;
        }
    }


}

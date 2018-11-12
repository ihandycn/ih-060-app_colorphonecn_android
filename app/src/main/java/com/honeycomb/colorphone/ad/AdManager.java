package com.honeycomb.colorphone.ad;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;

import java.util.List;

public class AdManager {

    private static final String TAG = AdManager.class.getSimpleName();

    private AcbNativeAd mAd = null;
    private AcbInterstitialAd mInterstitialAd = null;

    private static AdManager sInstance;
    private boolean mEnable;


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

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public void preload() {
        HSLog.d(TAG, "preload");
        if (!mEnable) {
            return;
        }
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

    public boolean showInterstitialAd() {
        AcbInterstitialAd ad = AdManager.getInstance().getInterstitialAd();
        if (ad != null) {
            ad.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                @Override
                public void onAdDisplayed() {

                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdClosed() {
                    AdManager.getInstance().releaseInterstitialAd();
                    AdManager.getInstance().preload();
                }

                @Override
                public void onAdDisplayFailed(AcbError acbError) {

                }
            });
            ad.show();
            return true;
        }
        return false;
    }


}

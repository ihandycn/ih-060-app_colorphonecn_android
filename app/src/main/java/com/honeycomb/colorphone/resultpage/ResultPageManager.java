package com.honeycomb.colorphone.resultpage;

import com.honeycomb.colorphone.Placements;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.ArrayList;
import java.util.List;

public class ResultPageManager {

    private static final String TAG = ResultPageManager.class.getSimpleName();
    public static final String RESULT_PAGE_AD_PLACEMENT_NAME = Placements.BOOST_DOWN;
    public static final String RESULT_PAGE_INTERSTITIAL_AD_PLACEMENT_NAME = Placements.BOOST_WIRE;

    private AcbNativeAd mAd = null;
    private AcbInterstitialAd mInterstitialAd = null;

    private static ResultPageManager sInstance;

    private List<String> mAppList = new ArrayList<>();
    private long mAppJunkSize;
    private long mAdJunkSize;
    private long mAppMemorySize;
    private boolean mAdDirty;

    private ResultPageManager() {
    }

    public static ResultPageManager getInstance() {
        if (sInstance == null) {
            synchronized (ResultPageManager.class) {
                if (sInstance == null) {
                    sInstance = new ResultPageManager();
                }
            }
        }
        return sInstance;
    }

    public static void preloadResultPageAds() {
        HSLog.d(TAG, "preloadResultPageAds");
        AcbNativeAdManager.getInstance().activePlacementInProcess(RESULT_PAGE_AD_PLACEMENT_NAME);
        AcbNativeAdManager.preload(1, RESULT_PAGE_AD_PLACEMENT_NAME);

        AcbInterstitialAdManager.getInstance().activePlacementInProcess(RESULT_PAGE_INTERSTITIAL_AD_PLACEMENT_NAME);
        AcbInterstitialAdManager.preload(1, RESULT_PAGE_INTERSTITIAL_AD_PLACEMENT_NAME);
    }

    public AcbNativeAd getAd() {
        if (mAd == null) {
            List<AcbNativeAd> ads = AcbNativeAdManager.fetch(RESULT_PAGE_AD_PLACEMENT_NAME, 1);
            if (ads != null && ads.size() > 0) {
                mAd = ads.get(0);
            }
        }
        return mAd;
    }

    public AcbInterstitialAd getInterstitialAd() {
        if (mInterstitialAd == null) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(RESULT_PAGE_INTERSTITIAL_AD_PLACEMENT_NAME, 1);
            if (ads != null && ads.size() > 0) {
                mInterstitialAd = ads.get(0);
            }
        }
        return mInterstitialAd;
    }

    public void releaseAd() {
        if (mAd != null) {
            mAd.release();
            mAd = null;
        }
    }

    public void releaseInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
            mInterstitialAd = null;
        }
    }

    public long getAppMemorySize() {
        return mAppMemorySize;
    }

    public long getAppJunkSize() {
        return mAppJunkSize;
    }

    public long getAdJunkSize() {
        return mAdJunkSize;
    }

    public List<String> getAppList() {
        return mAppList;
    }

    public void markAdDirty() {
        mAdDirty = true;
    }

    public void loadAd( AcbNativeAdLoader.AcbNativeAdLoadListener acbNativeAdLoadListener) {
        AcbNativeAdLoader loader = AcbNativeAdManager.createLoaderWithPlacement(RESULT_PAGE_AD_PLACEMENT_NAME);
        loader.load(1,acbNativeAdLoadListener);
    }
}

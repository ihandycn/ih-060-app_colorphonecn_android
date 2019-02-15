package com.honeycomb.colorphone.resultpage;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.ArrayList;
import java.util.List;

public class ResultPageManager {

    private static final String TAG = ResultPageManager.class.getSimpleName();

    private AcbNativeAd mAd = null;
    private AcbInterstitialAd mInterstitialAd = null;

    private static ResultPageManager sInstance;

    private List<String> mAppList = new ArrayList<>();
    private long mAppJunkSize;
    private long mAdJunkSize;
    private long mAppMemorySize;
    private boolean mAdDirty;
    private boolean inBatteryImprover;
    private boolean fromOkClick;
    private String mFromInterstitialAdPlacement;
    private String mFromAdPlacement;

    private boolean useThemeCommendAd = false;

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

    public void preloadThemeRecommendAds() {
        if (ThemeRecommendManager.isThemeRecommendEnable() && ThemeRecommendManager.isThemeRecommendAdShow()) {
            AcbNativeAdManager.getInstance().activePlacementInProcess(Placements.THEME_RECOMMEND_DONE);
            AcbNativeAdManager.preload(1, Placements.THEME_RECOMMEND_DONE);

            AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.THEME_RECOMMEND_WIRE);
            AcbInterstitialAdManager.preload(1, Placements.THEME_RECOMMEND_WIRE);
            useThemeCommendAd = true;
        }
    }

    public void preloadResultPageAds() {
        HSLog.d(TAG, "preloadResultPageAds");
        useThemeCommendAd = false;
        boolean hasNative = false;
        boolean hasInters = false;

        if (ResultPageManager.getInstance().isFromBatteryImprover()) {
            hasNative = getAd(Placements.BOOST_DOWN) != null;
            hasInters = getInterstitialAd(Placements.BOOST_WIRE) != null;
        }

        if (!hasNative) {
            AcbNativeAdManager.getInstance().activePlacementInProcess(ResultPageManager.getInstance().getExpressAdPlacement());
            AcbNativeAdManager.preload(1, ResultPageManager.getInstance().getExpressAdPlacement());
        }

        if (!hasInters) {
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(ResultPageManager.getInstance().getInterstitialAdPlacement());
            AcbInterstitialAdManager.preload(1, ResultPageManager.getInstance().getInterstitialAdPlacement());
        }
    }

    public AcbNativeAd getAd() {
        if (useThemeCommendAd) {
            return getAd(Placements.THEME_RECOMMEND_WIRE);
        }
        return getAd(ResultPageManager.getInstance().getExpressAdPlacement());
    }

    public AcbNativeAd getAd(String placement) {
        if (mAd == null) {
            List<AcbNativeAd> ads = AcbNativeAdManager.fetch(placement, 1);
            if (ads != null && ads.size() > 0) {
                mAd = ads.get(0);
                mFromAdPlacement = placement;
            }
        }
        return mAd;
    }

    public AcbInterstitialAd getInterstitialAd() {
        if (useThemeCommendAd) {
            return getInterstitialAd(Placements.THEME_RECOMMEND_WIRE);
        }
        return getInterstitialAd(ResultPageManager.getInstance().getInterstitialAdPlacement());
    }

    public AcbInterstitialAd getInterstitialAd(String placement) {
        if (mInterstitialAd == null) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(placement, 1);
            if (ads != null && ads.size() > 0) {
                mInterstitialAd = ads.get(0);
                mFromInterstitialAdPlacement = placement;
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

    public String getExpressAdPlacement() {
        return isFromBatteryImprover() ? Placements.CABLE_DOWN : Placements.BOOST_DOWN;
    }

    public String getInterstitialAdPlacement() {
        return  isFromBatteryImprover() ? Placements.CABLE_WIRE : Placements.BOOST_WIRE;
    }

    public String getFromAdPlacement() {
        return mFromAdPlacement;
    }

    public String getFromInterstitialAdPlacement() {
        return mFromInterstitialAdPlacement;
    }

    public boolean isFromBatteryImprover() {
        return inBatteryImprover;
    }

    public void setInBatteryImprover(boolean inBatteryImprover) {
        this.inBatteryImprover = inBatteryImprover;
    }

    public void setFromImproverOK(boolean fromOkClick) {
        this.fromOkClick = fromOkClick;
    }

    public boolean isFromOkClick() {
        return fromOkClick;
    }

    public String getFromTag() {
        return isFromOkClick() ? "OK" : "CleanPage";
    }
}

package com.honeycomb.colorphone.resultpage;

import android.app.Activity;

import com.honeycomb.colorphone.Placements;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.autopilot.AutopilotEvent;

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
    private boolean adJustShown;


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

    public void preloadResultPageAds() {
        HSLog.d(TAG, "preloadResultPageAds");
        boolean hasNative = false;
        boolean hasInters = false;
        if (ResultPageManager.getInstance().isFromBatteryImprover()) {
            hasNative = getAd(Placements.BOOST_DONE) != null;
            hasInters = getInterstitialAd(Placements.BOOST_WIRE) != null;
        }

        if (!hasNative) {
            AcbNativeAdManager.getInstance().activePlacementInProcess(ResultPageManager.getInstance().getExpressAdPlacement());
            AcbNativeAdManager.getInstance().preload(1, ResultPageManager.getInstance().getExpressAdPlacement());
        }

        if (!hasInters) {
            AcbInterstitialAdManager.getInstance().activePlacementInProcess(ResultPageManager.getInstance().getInterstitialAdPlacement());
            AcbInterstitialAdManager.getInstance().preload(1, ResultPageManager.getInstance().getInterstitialAdPlacement());
        }
    }

    public AcbNativeAd getAd() {
        return getAd(ResultPageManager.getInstance().getExpressAdPlacement());
    }

    public AcbNativeAd getAd(String placement) {
        if (mAd == null) {
            List<AcbNativeAd> ads = AcbNativeAdManager.getInstance().fetch(placement, 1);
            if (ads != null && ads.size() > 0) {
                mAd = ads.get(0);
                mFromAdPlacement = placement;
            }
        }
        return mAd;
    }

    public AcbInterstitialAd getInterstitialAd() {
        return getInterstitialAd(ResultPageManager.getInstance().getInterstitialAdPlacement());
    }

    public AcbInterstitialAd getInterstitialAd(String placement) {
        if (mInterstitialAd == null) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.getInstance().fetch(placement, 1);
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
        return Placements.BOOST_DONE;
    }

    public String getInterstitialAdPlacement() {
        return Placements.BOOST_WIRE;
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

    private Activity mActivity;

    public void finishAdWaitFinishActivity() {
        if (mActivity != null && !mActivity.isFinishing()) {
            mActivity.finish();
            mActivity = null;
        }
    }

    public boolean showInterstitialAd() {
        if (adJustShown) {
            adJustShown = false;
            return true;
        }
        if (getInterstitialAd() == null) {
            finishAdWaitFinishActivity();
            return false;
        }

        mInterstitialAd.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
            @Override
            public void onAdDisplayed() {
                finishAdWaitFinishActivity();
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdClosed() {
                ResultPageManager.getInstance().releaseInterstitialAd();
            }

            public void onAdDisplayFailed(AcbError acbError) {
                finishAdWaitFinishActivity();
                HSLog.d(TAG, "onAdDisplayFailed");
            }
        });
        mInterstitialAd.show(mActivity, "");
        double ecpm = mInterstitialAd.getEcpm() / 1000D;
        AutopilotEvent.logAppEvent("cpm_collection_other", ecpm);
        return true;
    }
}

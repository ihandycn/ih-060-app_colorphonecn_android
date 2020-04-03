package com.honeycomb.colorphone.ad;

import android.app.Activity;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.util.ADAutoPilotUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.AcbAds;
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
    private Activity activity;


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

    public void preload(Activity activity) {
        HSLog.d(TAG, "preloadForExitNews");
        if (!mEnable) {
            return;
        }

        if (!ADAutoPilotUtils.canShowThemeWireADThisTime()) {
            return;
        }

        if (activity != null) {
            this.activity = activity;
            AcbAds.getInstance().setActivity(activity);
            AcbInterstitialAdManager.getInstance().setForegroundActivity(activity);
        }
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(getInterstitialAdPlacementName());
        AcbInterstitialAdManager.getInstance().preload(1, getInterstitialAdPlacementName());
    }

    private static String getInterstitialAdPlacementName() {
        return Placements.getAdPlacement(Placements.THEME_WIRE);
    }

    public AcbInterstitialAd getInterstitialAd() {
        if (mInterstitialAd == null) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.getInstance().fetch(getInterstitialAdPlacementName(), 1);
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
        if (!ADAutoPilotUtils.canShowThemeWireADThisTime()) {
            return false;
        }

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
                    AdManager.getInstance().preload(activity);
                    ADAutoPilotUtils.recordShowThemeWireTime();
                }

                @Override
                public void onAdDisplayFailed(AcbError acbError) {

                }
            });
            ad.show(activity, "");
            if (Utils.isNewUser()) {
                Analytics.logEvent("ColorPhone_ThemeWire_Show");
            }
            Analytics.logEvent("ColorPhone_ThemeWireAd_Show");
            ADAutoPilotUtils.logThemeWireShow();
            ADAutoPilotUtils.recordShowThemeWireCount();
            return true;
        }
        return false;
    }


}

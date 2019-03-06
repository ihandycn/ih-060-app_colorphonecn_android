package com.honeycomb.colorphone.ad;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.honeycomb.colorphone.themeselector.ThemeGuideTest;
import com.honeycomb.colorphone.util.ADAutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
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

        if (!ADAutoPilotUtils.canShowThemeWireADThisTime()) {
            return;
        }

        AcbInterstitialAdManager.getInstance().activePlacementInProcess(getInterstitialAdPlacementName());
        AcbInterstitialAdManager.preload(1, getInterstitialAdPlacementName());
    }

    private static String getInterstitialAdPlacementName() {
        return Placements.THEME_WIRE;
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
                    AdManager.getInstance().preload();
                    ADAutoPilotUtils.recordShowThemeWireTime();
                }

                @Override
                public void onAdDisplayFailed(AcbError acbError) {

                }
            });
            ad.show();
            if (Utils.isNewUser()) {
                LauncherAnalytics.logEvent("ColorPhone_ThemeWire_Show");
            }
            LauncherAnalytics.logEvent("ColorPhone_ThemeWireAd_Show");
            LauncherAnalytics.logEvent("ColorPhone_ThemeWire_Show_QuickSetting");
            ADAutoPilotUtils.logThemeWireShow();
            ThemeGuideTest.logThemewireADShow();
            ADAutoPilotUtils.recordShowThemeWireCount();

            ThemeRecommendManager.logThemeRecommendThemeWireShow();
            return true;
        }
        return false;
    }


}

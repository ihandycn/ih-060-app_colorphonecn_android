package com.honeycomb.colorphone.preview;

import android.app.Activity;

import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;

public class PreviewAdManager {

    private static final String TAG = PreviewAdManager.class.getSimpleName();

    private AcbNativeAd mAd = null;

    private static PreviewAdManager sInstance;
    private boolean mEnable = true;

    private PreviewAdManager() {
    }

    public static PreviewAdManager getInstance() {
        if (sInstance == null) {
            synchronized (ResultPageManager.class) {
                if (sInstance == null) {
                    sInstance = new PreviewAdManager();
                }
            }
        }
        return sInstance;
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public void preload(Activity activity) {
        HSLog.d(TAG, "preload");
        if (!mEnable) {
            return;
        }

        if (activity != null) {
            AcbAds.getInstance().setActivity(activity);
        }
        AcbNativeAdManager.getInstance().activePlacementInProcess(getNativeAdPlacementName());
        AcbNativeAdManager.getInstance().preload(1, getNativeAdPlacementName());
    }

    private static String getNativeAdPlacementName() {
        return Placements.THEME_DETAIL_NATIVE;
//        return Placements.BOOST_DONE;
    }

    public AcbNativeAd getNativeAd() {
        if (mAd == null) {
            List<AcbNativeAd> ads = AcbNativeAdManager.getInstance().fetch(getNativeAdPlacementName(), 1);
            if (ads != null && ads.size() > 0) {
                mAd = ads.get(0);
                HSLog.i("ThemeFullAd", "new native ad");
            }
        }
        return mAd;
    }

    public void releaseNativeAd() {
        if (mAd != null) {
            mAd.release();
            mAd = null;
        }
    }

}

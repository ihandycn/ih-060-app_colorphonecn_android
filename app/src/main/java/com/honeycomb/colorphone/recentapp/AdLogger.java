package com.honeycomb.colorphone.recentapp;


import net.appcloudbox.ads.nativead.AcbNativeAdAnalytics;

/**
 * Handles common logic to log AdAnalytics.logAppViewEvent().
 */
public class AdLogger {

    /** Callback to do more custom logging. */
    public interface AdLoggerCallback {
        void onLogAppViewEvent(boolean adShown);
    }

    private String mPlacementName;
    private AdLoggerCallback mCallback;

    private boolean mShouldShowAd;
    private boolean mAdShown;

    public AdLogger(String placementName) {
        this(placementName, null);
    }

    public AdLogger(String placementName, AdLoggerCallback logCallback) {
        mPlacementName = placementName;
        mCallback = logCallback;
        reset();
    }

    /**
     * Marks that an ad session has started.
     *
     * An "ad session" is a period of time that starts when
     *
     *  - user enters the ad page
     *  - the ad page has just switched to a new ad from a previous ad
     *
     * and ends when
     *
     *  - user leaves the ad page
     *  - the ad page will switch to another ad
     */
    public void adSessionStart() {
        mShouldShowAd = true;
    }

    /**
     * Marks that an ad is actually shown.
     */
    public void adShow() {
        mAdShown = true;
    }

    /**
     * Notifies the logger that the (previously started) ad session has come to an end.
     * Event is fired at this call.
     * Positive event if ad is actually shown during the session, negative if not.
     */
    public void adSessionEnd() {
        if (mShouldShowAd) {
            AcbNativeAdAnalytics.logAppViewEvent(mPlacementName, mAdShown);
            if (mCallback != null) {
                mCallback.onLogAppViewEvent(mAdShown);
            }
            reset();
        }
    }

    private void reset() {
        mShouldShowAd = mAdShown = false;
    }
}

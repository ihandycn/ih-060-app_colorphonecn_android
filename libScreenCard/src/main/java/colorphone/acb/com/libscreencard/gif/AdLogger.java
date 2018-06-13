package colorphone.acb.com.libscreencard.gif;

import com.ihs.commons.utils.HSLog;

import colorphone.acb.com.libscreencard.CardCustomConfig;

/**
 * Handles common logic to log AdAnalytics.logAppViewEvent().
 */
public class AdLogger {

    private static final String TAG = AdLogger.class.getSimpleName();

    private String mPlacementName;

    private boolean mShouldShowAd;
    private boolean mAdShown;

    public AdLogger(String placementName) {
        mPlacementName = placementName;
        reset();
    }

    /**
     * Marks that an ad session has started.
     * <p>
     * An "ad session" is a period of time that starts when
     * <p>
     * - user enters the ad page
     * - the ad page has just switched to a new ad from a previous ad
     * <p>
     * and ends when
     * <p>
     * - user leaves the ad page
     * - the ad page will switch to another ad
     */
    public void adSessionStart() {
        HSLog.d(TAG, "ad session start!");
        mShouldShowAd = true;
    }

    /**
     * Marks that an ad is actually shown.
     */
    public void adShow() {
        HSLog.d(TAG, "ad show!");
        mAdShown = true;
    }

    /**
     * Notifies the logger that the (previously started) ad session has come to an end.
     * Event is fired at this call.
     * Positive event if ad is actually shown during the session, negative if not.
     */
    public void adSessionEnd() {
        HSLog.d(TAG, "ad session end!");
        if (mShouldShowAd) {
            //TODO
            CardCustomConfig.logAdViewEvent(mPlacementName, mAdShown);
            reset();
        }
    }

    public boolean isSessionStarted() {
        return mShouldShowAd;
    }

    private void reset() {
        mShouldShowAd = mAdShown = false;
    }
}

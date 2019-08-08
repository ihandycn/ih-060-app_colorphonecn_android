package com.colorphone.lock;

import android.content.Context;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;

/**
 * Created by sundxing on 17/9/5.
 */

public class LockerCustomConfig {

    private static LockerCustomConfig INSTANCE = new LockerCustomConfig();
    private String mChargingExpressAdName;
    private String mSPFileName;
    private String mLockerAdName;
    private int mLauncherIcon;
    private int mCustomScreenIcon;
    private Event mEventDelegate = new DefaultEvent();
    private RemoteLogger mRemoteLogger = new DefaultLogger();
    private GameCallback mGameCallback = new GameCallback() {
        @Override
        public void startGameCenter(Context context) {
            // Ignore
        }
    };

    public static LockerCustomConfig get() {
        return INSTANCE;
    }

    public String getChargingExpressAdName() {
        return mChargingExpressAdName;
    }

    public void setChargingExpressAdName(String chargingExpressAdName) {
        //TODO
        mChargingExpressAdName = chargingExpressAdName;
    }

    public String getSPFileName() {
        return mSPFileName;
    }

    public void setSPFileName(String SPFileName) {
        mSPFileName = SPFileName;
    }

    public String getLockerAdName() {
        return mLockerAdName;
    }

    public void setLockerAdName(String lockerAdName) {
        mLockerAdName = lockerAdName;
    }

    public int getLauncherIcon() {
        return mLauncherIcon;
    }

    public void setLauncherIcon(int launcherIcon) {
        mLauncherIcon = launcherIcon;
    }

    public void onEventLockerAdShow() {
        mEventDelegate.onEventLockerAdShow();
    }

    public void onEventLockerShow() {
        mEventDelegate.onEventLockerShow();
    }

    public void onEventLockerAdClick() {
        mEventDelegate.onEventLockerAdClick();
    }


    public void onEventChargingViewShow() {
        mEventDelegate.onEventChargingViewShow();
    }

    public void onEventChargingAdShow() {
        mEventDelegate.onEventChargingAdShow();
    }

    public void onEventChargingAdClick() {
        mEventDelegate.onEventChargingAdClick();
    }

    public void setEventDelegate(Event eventDelegate) {
        if (eventDelegate == null) {
            throw new IllegalStateException("eventDelegate should not be null!");
        }
        mEventDelegate = eventDelegate;
    }

    public RemoteLogger getRemoteLogger() {
        return mRemoteLogger;
    }

    public void setRemoteLogger(RemoteLogger remoteLogger) {
        mRemoteLogger = remoteLogger;
    }

    public int getCustomScreenIcon() {
        return mCustomScreenIcon;
    }

    public void setCustomScreenIcon(int customScreenIcon) {
        mCustomScreenIcon = customScreenIcon;
    }

    public boolean isGameEntranceEnable() {
        return mGameCallback.isGameEnable();
    }

    public GameCallback getGameCallback() {
        return mGameCallback;
    }

    public void setGameCallback(GameCallback gameCallback) {
        mGameCallback = gameCallback;
    }


    public static abstract class Event {
        public abstract void onEventLockerAdShow();

        public abstract void onEventLockerShow() ;

        public abstract void onEventLockerAdClick();

        public abstract void onEventChargingAdShow();

        public void onEventChargingAdClick() {
        }

        public abstract void onEventChargingViewShow();
    }

    private class DefaultEvent extends Event {

        @Override
        public void onEventLockerAdShow() {

        }

        @Override
        public void onEventLockerShow() {

        }

        @Override
        public void onEventLockerAdClick() {

        }

        @Override
        public void onEventChargingAdShow() {

        }

        @Override
        public void onEventChargingViewShow() {
            // Do nothing
        }
    }

    public static RemoteLogger getLogger() {
        return LockerCustomConfig.get().getRemoteLogger();
    }

    public interface RemoteLogger {
        void logEvent(String eventID);
        void logEvent(String eventID, String... vars);
    }

    public static abstract class GameCallback {
        public abstract void startGameCenter(Context context);

        public boolean isGameEnable() {
            return false;
        }
    }

    public static class DefaultLogger implements RemoteLogger {
        @Override
        public void logEvent(String eventID) {
            HSAnalytics.logEvent(eventID);
        }

        @Override
        public void logEvent(String eventID, String... vars) {
            HSAnalytics.logEvent(eventID, vars);
        }
    }

    public static void logAdViewEvent(String placementName, boolean success) {
        HSLog.d("ad analytics logAppViewEvent: " + placementName + " - " + success);
        getLogger().logEvent("Colorphone_AcbAdNative_Viewed_In_App", new String[]{placementName, String.valueOf(success)});
    }
}

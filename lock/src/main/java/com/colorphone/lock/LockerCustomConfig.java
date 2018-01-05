package com.colorphone.lock;

import com.ihs.app.analytics.HSAnalytics;

/**
 * Created by sundxing on 17/9/5.
 */

public class LockerCustomConfig {

    private static LockerCustomConfig INSTANCE = new LockerCustomConfig();
    private String mChargingExpressAdName;
    private String mSPFileName;
    private String mLockerAdName;
    private int mLauncherIcon;
    private Event mEventDelegate = new DefaultEvent();
    private RemoteLogger mRemoteLogger = new DefaultLogger();

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


    public static abstract class Event {
        public abstract void onEventLockerAdShow();

        public abstract void onEventLockerShow() ;

        public abstract void onEventLockerAdClick();

        public abstract void onEventChargingAdShow();

        public void onEventChargingAdClick() {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_Ad_Clicked");
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
}

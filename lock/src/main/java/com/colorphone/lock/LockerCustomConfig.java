package com.colorphone.lock;

import android.content.Context;

import com.ihs.app.analytics.HSAnalytics;

import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by sundxing on 17/9/5.
 */

public class LockerCustomConfig {

    private static LockerCustomConfig INSTANCE = new LockerCustomConfig();
    private String mSPFileName;
    private String mLockerAndChargingAdName;
    private String mNewsFeedNativeAdName;
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
    NewsLockerManager newsLockerManager;

    public static LockerCustomConfig get() {
        return INSTANCE;
    }

    public String getLockerAndChargingAdName() {
        return mLockerAndChargingAdName;
    }

    public void setLockerAndChargingAdName(String mSmartLockerAdName) {
        this.mLockerAndChargingAdName = mSmartLockerAdName;
    }

    public String getNewsFeedAdName() {
        return mNewsFeedNativeAdName;
    }

    public void setNewsFeedAdName(String newsFeedAdName) {
        this.mNewsFeedNativeAdName = newsFeedAdName;
    }

    public String getSPFileName() {
        return mSPFileName;
    }

    public void setSPFileName(String SPFileName) {
        mSPFileName = SPFileName;
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

    public NewsLockerManager getNewsLockerManager() {
        return newsLockerManager;
    }

    public void setNewsLockerManager(NewsLockerManager newsLockerManager) {
        this.newsLockerManager = newsLockerManager;
    }

    public static abstract class Event {
        public abstract void onEventLockerAdShow();

        public abstract void onEventLockerShow();

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

    public interface NewsLockerManager {
        boolean isRefresh();

        void logAdChance();

        void logAdShow();
    }
}

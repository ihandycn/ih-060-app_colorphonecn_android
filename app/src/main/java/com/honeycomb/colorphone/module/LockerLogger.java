package com.honeycomb.colorphone.module;

import com.colorphone.lock.LockerCustomConfig;
import com.honeycomb.colorphone.util.LauncherAnalytics;

/**
 * Created by sundxing on 2018/1/5.
 */

public class LockerLogger implements LockerCustomConfig.RemoteLogger{
    @Override
    public void logEvent(String eventID) {
        LauncherAnalytics.logEvent(eventID);
    }

    @Override
    public void logEvent(String eventID, String... vars) {
        LauncherAnalytics.logEvent(eventID, vars);
    }
}

package com.honeycomb.colorphone.module;

import com.colorphone.lock.LockerCustomConfig;
import com.honeycomb.colorphone.util.Analytics;

/**
 * Created by sundxing on 2018/1/5.
 */

public class LockerLogger implements LockerCustomConfig.RemoteLogger{
    @Override
    public void logEvent(String eventID) {
        Analytics.logEvent(eventID);
    }

    @Override
    public void logEvent(String eventID, String... vars) {
        Analytics.logEvent(eventID, vars);
    }
}

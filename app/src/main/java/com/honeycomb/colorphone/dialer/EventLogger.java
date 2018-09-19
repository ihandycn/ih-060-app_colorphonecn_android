package com.honeycomb.colorphone.dialer;

import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.dialer.util.GeoUtil;
import com.honeycomb.colorphone.dialer.util.HardwareProximitySensor;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;

public class EventLogger {

    private static EventLogger INSTANCE = new EventLogger();

    private long mDuration;
    private boolean mOutGoing;
    private String number;

    private EventLogger() {}

    public static EventLogger get() {
        return INSTANCE;
    }

    public void onOutGoing(String number) {
        this.number = number;
        HardwareProximitySensor.start(HSApplication.getContext());
        mOutGoing = true;
    }

    public void onIncomingCall(String number) {
        this.number = number;
        HardwareProximitySensor.start(HSApplication.getContext());
        mOutGoing = false;
    }
    public void setDuration(long duration) {
        mDuration = duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public boolean isInternational() {
        return false;
    }

    public void onEnd() {
        boolean near = HardwareProximitySensor.getInstance().getValue() > 0;
        HardwareProximitySensor.stop();

        LauncherAnalytics.logEvent(
                mOutGoing ? "ColorPhone_Outgoing_Call" : "ColorPhone_Incoming_Call",
                "State",
                mDuration > 0 ? "Success" : "Fail");

        LauncherAnalytics.logEvent("ColorPhone_Call_Finished2",
                "Timing", formatDuration(mDuration),
                "Closetoear", near ? "Yes" : "NO",
                "ForeignCall", PhoneNumberUtils.isInternationalNumber(number, GeoUtil.getCurrentCountryIso(HSApplication.getContext()))
        ? "Yes" : "NO");

    }

    private String formatDuration(long duration) {
        if (duration == 0) {
            return "0";
        }

        long seconds = duration / DateUtils.SECOND_IN_MILLIS;
        long min = seconds / 60;
        if (seconds <= 5) {
            return "0-5s";
        } else if (seconds <= 15) {
            return "5-15s";
        } else if (seconds <= 60){
            return "15-60s";
        } else if (min <= 3) {
            return "1-3m";
        } else if (min <= 10) {
            return "3-10m";
        } else {
            return "10m+";
        }

    }
}

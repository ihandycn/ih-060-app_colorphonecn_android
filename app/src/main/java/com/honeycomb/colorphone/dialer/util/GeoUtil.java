package com.honeycomb.colorphone.dialer.util;

import android.content.Context;
import android.telephony.TelephonyManager;

public class GeoUtil {
    public static String getCurrentCountryIso(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        return countryCode;
    }
}

package com.honeycomb.colorphone.dialer.util;

import android.content.Context;
import android.os.Build;
import android.telecom.TelecomManager;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.dialer.AP;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

public class DefaultPhoneUtils {

    public static boolean isDefaultPhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) HSApplication.getContext().getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                return HSApplication.getContext().getPackageName().equals(telecomManager.getDefaultDialerPackage());
            }
        }
        return false;
    }

    public static boolean checkGuideResult() {
        boolean needCheckDefaultSetResult =
                Preferences.get(Constants.DESKTOP_PREFS)
                        .getBoolean(Constants.PREFS_CHECK_DEFAULT_PHONE, false);
        if (needCheckDefaultSetResult) {
            Preferences.get(Constants.DESKTOP_PREFS)
                    .putBoolean(Constants.PREFS_CHECK_DEFAULT_PHONE, false);
            boolean defaultPhone = isDefaultPhone();
            HSLog.d("DefaultPhoneUtils", "default phone now : " + defaultPhone);
            if (defaultPhone) {
                AP.successSetAsDefault();
                return true;
            }
        }
        return false;
    }
}

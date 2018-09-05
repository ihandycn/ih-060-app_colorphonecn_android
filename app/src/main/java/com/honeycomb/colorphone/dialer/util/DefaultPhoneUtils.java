package com.honeycomb.colorphone.dialer.util;

import android.content.Context;
import android.os.Build;
import android.telecom.TelecomManager;

import com.ihs.app.framework.HSApplication;

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
}

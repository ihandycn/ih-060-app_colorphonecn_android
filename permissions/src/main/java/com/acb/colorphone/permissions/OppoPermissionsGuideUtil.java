package com.acb.colorphone.permissions;

import android.content.Context;
import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

public class OppoPermissionsGuideUtil {

    public static void showOverlayGuide() {
        Navigations.startActivitySafely(HSApplication.getContext(), OverlayOppoGuideActivity.class);
    }

    public static void showAutoStartGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartAboveOOppoGuideActivity.class);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartOppoGuideActivity.class);
        }
    }

    public static void showNAGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Navigations.startActivitySafely(HSApplication.getContext(), NAOppoGuideActivity.class);
        }
    }

    public static void showNotificationManageGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Navigations.startActivitySafely(HSApplication.getContext(), NotificationManagementOppoGuideActivity.class);
        }
    }

    public static void showPhoneGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Navigations.startActivitySafely(HSApplication.getContext(), PhoneOppoGuideActivity.class);
        }
    }

    public static void showDangerousPermissionsGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Navigations.startActivitySafely(HSApplication.getContext(), DangerousOppoGuideActivity.class);
        }
    }

}

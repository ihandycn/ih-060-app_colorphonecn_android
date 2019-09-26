package com.acb.colorphone.permissions;

import android.content.Context;
import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

public class OppoPermissionsGuideUtil {

    public static void showOverlayGuide(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StableToast.showStableToast(R.layout.toast_one_line_text,
                    R.string.acb_phone_oppo_overlay_permission_guide_above_26, 0, null);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StableToast.showStableToast(R.layout.toast_one_line_text,
                    R.string.acb_phone_oppo_overlay_permission_guide_above_24, 0, null);
        } else {
            // do nothing
        }
    }

    public static void showAutoStartGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StableToast.showStableToast(R.layout.toast_one_line_text,
                    R.string.acb_phone_oppo_autostart_permission_guide_above_26, 0, null);
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
package com.acb.colorphone.permissions;

import android.os.Build;

public class NotificationManagementOppoGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_oppo_notificationmanagement_permission_guide;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/auto_start_images/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_notification_management_permission_oppo.json";
    }

    @Override protected void showExitStableToast() {
        // do nothing since we can't show toast when our app isn't on the top on oppo device.
    }
}

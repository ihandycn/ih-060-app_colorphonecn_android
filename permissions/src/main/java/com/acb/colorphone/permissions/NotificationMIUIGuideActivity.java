package com.acb.colorphone.permissions;

public class NotificationMIUIGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_notification_access_title;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/auto_start_images/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_auto_start.json";
    }
}

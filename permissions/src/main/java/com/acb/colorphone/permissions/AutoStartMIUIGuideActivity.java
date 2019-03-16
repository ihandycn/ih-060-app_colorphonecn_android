package com.acb.colorphone.permissions;

public class AutoStartMIUIGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_autostart_access_title_miui;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/auto_start_images/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_auto_start.json";
    }
}

package com.acb.colorphone.permissions;

public class PhoneMiuiGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_phone_miui_title;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_phone_miui.json";
    }
}

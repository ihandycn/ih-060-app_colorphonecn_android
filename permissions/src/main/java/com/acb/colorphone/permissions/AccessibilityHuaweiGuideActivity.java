package com.acb.colorphone.permissions;

public class AccessibilityHuaweiGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_accessibility_title_huawei;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/accessibility_images/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_accessibility_huawei.json";
    }
}

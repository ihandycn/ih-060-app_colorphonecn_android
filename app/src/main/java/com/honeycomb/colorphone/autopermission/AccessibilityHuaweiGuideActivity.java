package com.honeycomb.colorphone.autopermission;

import com.acb.colorphone.permissions.LottiePermissionGuideActivity;

public class AccessibilityHuaweiGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return com.acb.colorphone.permissions.R.string.acb_phone_grant_accessibility_title_huawei;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/accessibility_images/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_accessibility_huawei.json";
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        StableToast.showHuaweiAccToast();
    }
}

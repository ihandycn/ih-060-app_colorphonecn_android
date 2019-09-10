package com.acb.colorphone.permissions;

import android.os.Build;

import com.superapps.util.Dimensions;

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

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override protected void showExitStableToast() {
        int layoutId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                ? R.layout.toast_huawei_acc
                : R.layout.toast_huawei_acc_8;

        StableToast.showStableToast(layoutId, 0, Dimensions.pxFromDp(85), "AccessibilityPageDuration");
    }
}

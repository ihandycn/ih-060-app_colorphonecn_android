package com.acb.colorphone.permissions;

import com.superapps.util.Dimensions;

public class VivoPermissionsGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_permissions_title_vivo;
    }

    @Override
    protected String getImageAssetFolder() {
        return "lottie/vivo_permissions_guide/images/";
    }

    @Override
    protected String getAnimationFromJson() {
        return "lottie/vivo_permissions_guide/data.json";
    }

    @Override
    protected void showExitStableToast() {
        int yOffset = Dimensions.pxFromDp(140);
        StableToast.showStableToast(R.layout.toast_one_line_text, getTitleStringResId(), yOffset, "AutoStartPageDuration");
    }
}

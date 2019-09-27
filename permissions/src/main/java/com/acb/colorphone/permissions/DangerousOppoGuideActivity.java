package com.acb.colorphone.permissions;

public class DangerousOppoGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_oppo_dangerous_permission_guide;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/auto_start_images/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_dangerous_permission_guide_oppo.json";
    }

    @Override protected void showExitStableToast() {
        // do nothing since we can't show toast when our app isn't on the top on oppo device.
    }
}

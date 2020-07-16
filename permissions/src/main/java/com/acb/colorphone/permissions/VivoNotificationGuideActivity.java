package com.acb.colorphone.permissions;

import com.superapps.util.Dimensions;

public class VivoNotificationGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_notification_title_vivo;
    }

    @Override
    protected String getImageAssetFolder() {
        return "lottie/vivo_notification_guide/images";
    }

    @Override
    protected String getAnimationFromJson() {
        return "lottie/vivo_notification_guide/data.json";
    }

    @Override
    protected void showExitStableToast() {
        int yOffset = Dimensions.pxFromDp(140);
        StableToast.showStableToast(R.layout.toast_one_line_text, getTitleStringResId(), yOffset, "AutoStartPageDuration");
    }
}

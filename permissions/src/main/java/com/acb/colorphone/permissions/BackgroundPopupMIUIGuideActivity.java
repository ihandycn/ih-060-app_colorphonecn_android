package com.acb.colorphone.permissions;

public class BackgroundPopupMIUIGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_background_popup_title_miui;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_bg_pop.json";
    }
}

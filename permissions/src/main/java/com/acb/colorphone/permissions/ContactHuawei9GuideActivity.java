package com.acb.colorphone.permissions;

public class ContactHuawei9GuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_background_popup_title_miui;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_contact_permission_huawei9.json";
    }
}

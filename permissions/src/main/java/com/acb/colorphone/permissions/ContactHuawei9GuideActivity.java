package com.acb.colorphone.permissions;

public class ContactHuawei9GuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_contact_huawei_9_title;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_contact_permission_contact_huawei9.json";
    }
}

package com.acb.colorphone.permissions;

public class PhoneHuawei8GuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_phone_title;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_huawei8.json";
    }
}

package com.acb.colorphone.permissions;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class ContactHuawei9GuideActivity extends LottiePermissionGuideActivity {

    private String permission = "";
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permission = getIntent().getStringExtra(Constants.INTENT_EXTRA_KEY_PERMISSION);
    }

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_contact_huawei_9_title;
    }

    @Override protected String getFormatString() {
        return permission;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_contact_permission_contact_huawei9.json";
    }
}

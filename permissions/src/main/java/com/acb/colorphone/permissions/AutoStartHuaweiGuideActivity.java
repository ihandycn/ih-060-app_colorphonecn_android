package com.acb.colorphone.permissions;

import android.os.Build;

public class AutoStartHuaweiGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return R.string.acb_phone_grant_autostart_access_title_huawei_above26;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return R.string.acb_phone_grant_autostart_access_title_huawei_above23;
        } else {
            return R.string.acb_phone_grant_autostart_access_title_huawei;
        }
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/auto_start_images/";
    }

    @Override protected String getAnimationFromJson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return "lottie/acb_phone_permission_auto_start_huawei.json";
        } else {
            return "lottie/acb_phone_permission_auto_start.json";
        }
    }
}

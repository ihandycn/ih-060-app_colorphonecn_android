package com.honeycomb.colorphone.autopermission;

import android.os.Build;

import com.acb.colorphone.permissions.LottiePermissionGuideActivity;

public class AutoStartHuaweiGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return com.acb.colorphone.permissions.R.string.acb_phone_grant_autostart_access_title_huawei_above26;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return com.acb.colorphone.permissions.R.string.acb_phone_grant_autostart_access_title_huawei_above23;
        } else {
            return com.acb.colorphone.permissions.R.string.acb_phone_grant_autostart_access_title_huawei;
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

    @Override protected void onDestroy() {
        super.onDestroy();
        StableToast.showHuaweiAutoStartToast();
    }
}

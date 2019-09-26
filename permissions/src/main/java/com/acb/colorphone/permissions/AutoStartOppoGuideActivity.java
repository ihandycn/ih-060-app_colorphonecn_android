package com.acb.colorphone.permissions;

import android.os.Build;

import com.superapps.util.Dimensions;

public class AutoStartOppoGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_oppo_autostart_permission_guide_above_24;
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

    @Override protected void showExitStableToast() {
        // do nothing since we can't show toast when our app isn't on the top on oppo device.
    }
}

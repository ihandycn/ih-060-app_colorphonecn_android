package com.acb.colorphone.permissions;

import android.os.Build;

import com.superapps.util.Dimensions;

public class AutoStartOppoGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_oppo_autostart_permission_guide_above_24;
    }

    @Override protected String getImageAssetFolder() {
        return "lottie/auto_start_oppo/";
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_auto_start_permission_guide_oppo.json";
    }

    @Override protected void showExitStableToast() {
        // do nothing since we can't show toast when our app isn't on the top on oppo device.
    }
}

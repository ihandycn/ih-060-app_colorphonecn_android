package com.acb.colorphone.permissions;

import android.os.Build;

import com.acb.colorphone.guide.AccGuideActivity;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

public class AccessibilityOppoGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_accessibility_title_oppo;
    }

    @Override
    protected String getImageAssetFolder() {
        return "lottie/acc_images_oppo/";
    }

    @Override
    protected String getAnimationFromJson() {
        return "lottie/acb_acc_permission_guide_oppo.json";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void showExitStableToast() {
        if (Compats.IS_OPPO_DEVICE) {
            Threads.postOnMainThreadDelayed(() -> AccGuideActivity.start(HSApplication.getContext()), 300);
        } else {
            int layoutId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? R.layout.toast_huawei_acc
                    : R.layout.toast_huawei_acc_8;

            StableToast.showStableToast(layoutId, 0, 0, "AccessibilityPageDuration");
        }
    }
}

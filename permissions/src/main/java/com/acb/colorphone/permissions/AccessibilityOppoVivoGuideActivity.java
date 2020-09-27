package com.acb.colorphone.permissions;

import android.os.Build;

import com.acb.colorphone.guide.AccGuideActivity;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Compats;
import com.superapps.util.Threads;

public class AccessibilityOppoVivoGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        if (Compats.IS_VIVO_DEVICE && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            return R.string.acb_phone_grant_accessibility_title_vivo_24;
        } else {
            return R.string.acb_phone_grant_accessibility_title_oppo;
        }
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
        } else if (Compats.IS_VIVO_DEVICE){
            int layoutId = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N
                    ? R.layout.toast_vivo_acc_7
                    : R.layout.toast_vivo_acc;

            StableToast.showStableToast(layoutId, 0, 0, "AccessibilityPageDuration");
        }
    }
}

package com.acb.colorphone.permissions;

import android.os.Build;

import com.acb.colorphone.PermissionsManager;
import com.acb.colorphone.guide.AccGuideActivity;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

public class AccessibilityHuaweiGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_accessibility_title_huawei;
    }

    @Override
    protected String getImageAssetFolder() {
        return "lottie/accessibility_images/";
    }

    @Override
    protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_accessibility_huawei.json";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void showExitStableToast() {
        if (Compats.IS_HUAWEI_DEVICE && PermissionsManager.getInstance().isShowActivityGuide()) {
            Threads.postOnMainThreadDelayed(() -> AccGuideActivity.start(HSApplication.getContext()), 300);
        } else {
            int layoutId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? R.layout.toast_huawei_acc
                    : R.layout.toast_huawei_acc_8;

            StableToast.showStableToast(layoutId, 0, Dimensions.pxFromDp(85), "AccessibilityPageDuration");
        }
    }
}

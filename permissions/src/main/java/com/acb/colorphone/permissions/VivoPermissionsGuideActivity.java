package com.acb.colorphone.permissions;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.acb.colorphone.PermissionsManager;
import com.airbnb.lottie.LottieAnimationView;
import com.superapps.util.Dimensions;

public class VivoPermissionsGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_anim);
        lottieAnimationView.setTranslationY(Dimensions.pxFromDp(20));
        PermissionsManager.getInstance().logEvent("Autostart_Alert_Show", false);
    }

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_permissions_title_vivo;
    }

    @Override
    protected String getImageAssetFolder() {
        return "lottie/vivo_permissions_guide/images/";
    }

    @Override
    protected String getAnimationFromJson() {
        return "lottie/vivo_permissions_guide/data.json";
    }

    @Override
    protected void showExitStableToast() {
        int yOffset = Dimensions.pxFromDp(0);
        StableToast.showStableToast(R.layout.toast_one_line_text, getTitleStringResId(), yOffset, "AutoStartPageDuration");
    }
}

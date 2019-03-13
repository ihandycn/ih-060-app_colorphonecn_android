package com.acb.colorphone.permissions;

import com.superapps.util.rom.RomUtils;

public class ShowOnLockScreenMIUIGuideActivity extends LottiePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        if (RomUtils.checkIsHuaweiRom()) {
            return R.string.acb_phone_grant_show_onlockscreen_access_title_huawei;
        }
        if (RomUtils.checkIsOppoRom()) {
            return R.string.acb_phone_grant_show_onlockscreen_access_title_oppo;
        }
        if (RomUtils.checkIsVivoRom()) {
            return R.string.acb_phone_grant_show_onlockscreen_access_title_vivo;
        }
        if (RomUtils.checkIsMiuiRom()) {
            return R.string.acb_phone_grant_show_onlockscreen_access_title_miui;
        }
        return R.string.acb_phone_grant_show_onlockscreen_access_title;
    }

    @Override protected String getImageAssetFolder() {
        return null;
    }

    @Override protected String getAnimationFromJson() {
        return "lottie/acb_phone_permission_show_on_lock_screen.json";
    }
}
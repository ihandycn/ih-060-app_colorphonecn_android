package com.acb.colorphone.permissions;

import com.superapps.util.rom.RomUtils;

public class NotificationGuideActivity extends SimplePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        if (RomUtils.checkIsHuaweiRom()) {
            return R.string.acb_phone_grant_notification_access_title_huawei;
        }
        if (RomUtils.checkIsOppoRom()) {
            return R.string.acb_phone_grant_notification_access_title_oppo;
        }
        if (RomUtils.checkIsVivoRom()) {
            return R.string.acb_phone_grant_notification_access_title_vivo;
        }
        if (RomUtils.checkIsMiuiRom()) {
            return R.string.acb_phone_grant_notification_access_title_miui;
        }
        return R.string.acb_phone_grant_notification_access_title;
    }
}

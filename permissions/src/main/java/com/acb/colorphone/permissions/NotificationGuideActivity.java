package com.acb.colorphone.permissions;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
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

    @Override protected void onStop() {
        super.onStop();

        HSBundle hsBundle = new HSBundle();
        hsBundle.putString(PermissionConstants.PERMISSION_GUIDE_EXIT, PermissionConstants.PERMISSION_NOTIFICATION_ACCESS);
        HSGlobalNotificationCenter.sendNotification(PermissionConstants.PERMISSION_GUIDE_EXIT, hsBundle);
    }
}

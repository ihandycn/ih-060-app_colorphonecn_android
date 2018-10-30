package com.honeycomb.colorphone.permission;

import com.honeycomb.colorphone.R;


public class NotificationGuideActivity extends SimplePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_notification_access_title;
    }
}

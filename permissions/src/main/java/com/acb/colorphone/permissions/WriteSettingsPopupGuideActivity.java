package com.acb.colorphone.permissions;

public class WriteSettingsPopupGuideActivity extends ImagePermissionGuideActivity {

    @Override
    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_write_settings_title;
    }

    @Override protected int getImageResId() {
        return R.drawable.permission_write_settings;
    }

}

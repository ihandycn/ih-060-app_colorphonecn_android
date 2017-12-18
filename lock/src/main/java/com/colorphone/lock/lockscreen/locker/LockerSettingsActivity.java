package com.colorphone.lock.lockscreen.locker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.util.ActivityUtils;
import com.colorphone.lock.util.ViewUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;

public class LockerSettingsActivity extends HSAppCompatActivity
        implements View.OnClickListener, SwitchCompat.OnCheckedChangeListener {

    private View mLockerEnabledLayout;
    private SwitchCompat mLockerEnabledToggle;

    protected int getLayoutId() {
        return R.layout.activity_locker_settings;
    }

    protected int getTitleId() {
        return R.string.locker_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ActivityUtils.configSimpleAppBar(this, getString(getTitleId()), ContextCompat.getColor(this, R.color.material_text_black_primary), Color.WHITE, true);
        ActivityUtils.configStatusBarColor(this);
        mLockerEnabledLayout = ViewUtils.findViewById(this, R.id.locker_enabled_cell);
        mLockerEnabledToggle = ViewUtils.findViewById(this, R.id.locker_enabled_button);

        mLockerEnabledToggle.setChecked(LockerSettings.isLockerEnabled());

        mLockerEnabledLayout.setOnClickListener(this);
        mLockerEnabledToggle.setOnCheckedChangeListener(this);
    }

    @Override public void onClick(View v) {
        if (v == mLockerEnabledLayout) {
            mLockerEnabledToggle.performClick();
        }
    }

    @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mLockerEnabledToggle) {
            LockerSettings.setLockerEnabled(isChecked);
            if (HSConfig.optBoolean(false, "Application", "Locker", "AutoOpenWhenSwitchOn") && !ChargingScreenSettings.isChargingScreenEverEnabled()) {
                ChargingScreenSettings.setChargingScreenEnabled(true);
            }
            HSAnalytics.logEvent("LauncherSettings_LockerSettings_Locker_Clicked", "type", isChecked ? "On" : "Off");
        }
    }
}

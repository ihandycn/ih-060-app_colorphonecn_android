package com.honeycomb.colorphone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.acb.call.CPSettings;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSPreferenceHelper;


public class SettingsActivity extends HSAppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);

        Utils.configActivityStatusBar(this, toolbar);

        final SwitchCompat mSwitchCompat = (SwitchCompat) findViewById(R.id.setting_item_call_assistant_toggle);
        boolean defaultEnable = ColorPhoneApplication.getConfigLog().isAssistantEnabledDefault();
        boolean checked = HSPreferenceHelper.getDefault().getBoolean("prefs_call_assistant", defaultEnable);
        mSwitchCompat.setChecked(checked);

        findViewById(R.id.setting_item_call_assistant).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitchCompat.toggle();
            }
        });

        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CPSettings.setCallAssistantModuleEnabled(isChecked);
                ColorPhoneApplication.getConfigLog().getEvent().onCallAssistantEnableFromSetting(isChecked);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

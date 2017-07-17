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


public class SettingsActivity extends HSAppCompatActivity {

    private SwitchCompat mSwitchCompat;
    private boolean initCheckState;

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

        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        initCheckState = CPSettings.isCallAssistantModuleEnabled();
        mSwitchCompat = (SwitchCompat) findViewById(R.id.setting_item_call_assistant_toggle);
        mSwitchCompat.setChecked(initCheckState);

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

    @Override
    protected void onDestroy() {
        boolean nowEnable = mSwitchCompat.isChecked();
        if (nowEnable != initCheckState) {
            ColorPhoneApplication.getConfigLog().getEvent().onCallAssistantEnableFromSetting(nowEnable);
        }
        super.onDestroy();
    }
}

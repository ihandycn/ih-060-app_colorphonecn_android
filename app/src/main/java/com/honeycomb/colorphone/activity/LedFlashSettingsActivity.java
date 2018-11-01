package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.FlashManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class LedFlashSettingsActivity extends HSAppCompatActivity {

    private static final String TAG = LedFlashSettingsActivity.class.getSimpleName();

    private List<ModuleState> mModuleStates = new ArrayList<>();

    public static void start(Context context) {
        Intent starter = new Intent(context, LedFlashSettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_settings_flash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.led_flash_switch_text);


        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        mModuleStates.add(new ModuleState(
                true,
                Preferences.get(Constants.DESKTOP_PREFS).getBoolean(Constants.PREFS_LED_FLASH_ENABLE,
                        HSConfig.optBoolean(false, "Application", "LEDReminder", "DefaultSwitch")),
                R.id.setting_item_call_toggle,
                R.id.setting_item_call) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(Constants.PREFS_LED_FLASH_ENABLE, isChecked);
                if (isChecked) {
                    FlashManager.getInstance().startFlash(3);
                    LauncherAnalytics.logEvent("LEDReminder_Enabled_FromSettings");
                } else {
                    LauncherAnalytics.logEvent("LEDReminder_Disabled_FromSettings");
                }
            }
        });

        // Disabled
        mModuleStates.add(new ModuleState(
                false,
                Preferences.get(Constants.DESKTOP_PREFS).getBoolean(Constants.PREFS_LED_SMS_ENABLE, false),
                R.id.setting_item_sms_toggle,
                R.id.setting_item_sms) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(Constants.PREFS_LED_SMS_ENABLE, isChecked);
            }
        });


        for (final ModuleState moduleState : mModuleStates) {
            View rootView = findViewById(moduleState.itemLayoutId);
            if (!moduleState.enabled) {
                rootView.setVisibility(View.GONE);
                continue;
            }
            moduleState.switchCompat.setChecked(moduleState.initState);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moduleState.switchCompat.toggle();
                }
            });

            moduleState.switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    moduleState.onCheckChanged(isChecked);
                }
            });

        }
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
    protected void onStop() {
        for (ModuleState moduleState : mModuleStates) {
            boolean nowEnable = moduleState.switchCompat.isChecked();

            if (nowEnable != moduleState.initState) {
                if (moduleState.itemLayoutId == R.id.setting_item_call_assistant) {
                    ColorPhoneApplication.getConfigLog().getEvent().onCallAssistantEnableFromSetting(nowEnable);
                }
                moduleState.initState = nowEnable;
            }
        }
        super.onStop();
    }

    private abstract class ModuleState {
        private final SwitchCompat switchCompat;
        boolean enabled;
        boolean initState;
        int toggleId;
        int itemLayoutId;

        public ModuleState(boolean enabled, boolean initState, int toggleId, int itemLayoutId) {
            this.enabled = enabled;
            this.initState = initState;
            this.toggleId = toggleId;
            this.itemLayoutId = itemLayoutId;
            switchCompat = ((SwitchCompat) findViewById(toggleId));
        }

        public abstract void onCheckChanged(boolean isChecked);
    }
}

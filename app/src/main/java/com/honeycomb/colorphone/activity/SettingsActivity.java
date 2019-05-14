package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.call.assistant.customize.CallAssistantSettings;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.BoostConfig;
import com.honeycomb.colorphone.recentapp.SmartAssistantUtils;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.UserSettings;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.chargingimprover.ChargingImproverUtils;
import com.messagecenter.customize.MessageCenterSettings;
import com.superapps.util.Navigations;

import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libscreencard.gif.GifCacheUtils;

public class SettingsActivity extends HSAppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private List<ModuleState> mModuleStates = new ArrayList<>();

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
        if (BuildConfig.DEBUG) {
            View testView = findViewById(R.id.test_entrance);
            testView.setVisibility(View.VISIBLE);
            testView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SettingsActivity.this, TestActivity.class));
                }
            });
        }


        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        mModuleStates.add(new ModuleState(ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_CALL_ASSISTANT), CallAssistantSettings.isCallAssistantModuleEnabled(),
                R.id.setting_item_call_assistant_toggle,
                R.id.setting_item_call_assistant) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Analytics.logEvent("Settings_CallAssistant_Clicked_" +
                        (isChecked ? "Enabled" : "Disabled"));
                CallAssistantSettings.setCallAssistantModuleEnabled(isChecked);
            }
        });

        mModuleStates.add(new ModuleState(ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT),
                MessageCenterSettings.isSMSAssistantModuleEnabled(),
                R.id.setting_item_sms_assistant_toggle,
                R.id.setting_item_sms_assistant) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Analytics.logEvent("Settings_SMSAssistant_Clicked_" +
                        (isChecked ? "Enabled" : "Disabled"));
                MessageCenterSettings.setSMSAssistantModuleEnabled(isChecked);
            }
        });


        boolean chargingImproverOpen = ModuleUtils.isChargingImproverEnabled();
        if (chargingImproverOpen) {
            TextView tv = findViewById(R.id.setting_item_charging_title);
            tv.setText(R.string.charging_improver_title);
        }
        mModuleStates.add(new ModuleState(
                chargingImproverOpen || SmartChargingSettings.isSmartChargingConfigEnabled(),
                chargingImproverOpen ?
                        ChargingImproverUtils.isChargingImproverUserEnabled() :
                        SmartChargingSettings.isSmartChargingUserEnabled(),
                R.id.setting_item_charging_toggle,
                R.id.setting_item_charging) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Analytics.logEvent("Settings_ChargingReport_Clicked_" +
                        (isChecked ? "Enabled" : "Disabled"));
                GifCacheUtils.cacheGif();
                SmartChargingSettings.setModuleEnabled(isChecked);
                ChargingImproverUtils.setChargingImproverUserEnabled(isChecked);
            }
        });


        mModuleStates.add(new ModuleState(ModuleUtils.isNotificationToolBarEnabled(),
                UserSettings.isNotificationToolbarEnabled(),
                R.id.notification_toolbar_switch,
                R.id.settings_notification_toolbar) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Analytics.logEvent("Settings_Toolbar_Clicked_" +
                        (isChecked ? "Enabled" : "Disabled"));
                UserSettings.setNotificationToolbarEnabled(isChecked);
                NotificationManager.getInstance().showNotificationToolbarIfEnabled();
            }
        });

        mModuleStates.add(new ModuleState(LockerSettings.isLockerConfigEnabled(),
                LockerSettings.isLockerUserEnabled(),
                R.id.setting_item_lockScreen_toggle,
                R.id.setting_item_lockScreen) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Analytics.logEvent("Settings_LockScreen_Clicked_" +
                        (isChecked ? "Enabled" : "Disabled"));
                MessageCenterSettings.setSMSAssistantModuleEnabled(isChecked);
                LockerSettings.setLockerEnabled(isChecked);
            }
        });

        mModuleStates.add(new ModuleState(SmartAssistantUtils.isConfigEnabled(),
                SmartAssistantUtils.isUserEnabled(),
                R.id.setting_item_recent_apps_toggle,
                R.id.setting_item_recent_apps) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                SmartAssistantUtils.setUserEnable(isChecked);
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

        if (BoostConfig.isBoostPushEnable()) {
            findViewById(R.id.setting_item_notification).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, NotificationSettingsActivity.class);
                    Navigations.startActivitySafely(SettingsActivity.this, intent);
                }
            });
        } else {
            findViewById(R.id.setting_item_notification).setVisibility(View.GONE);
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

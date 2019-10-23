package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.call.assistant.customize.CallAssistantSettings;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.BoostConfig;
import com.honeycomb.colorphone.lifeassistant.LifeAssistantConfig;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.UserSettings;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.messagecenter.customize.MessageCenterSettings;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends HSAppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private List<ModuleState> mModuleStates = new ArrayList<>();

    private View confirmDialog;
    private ModuleState lifeAssistant;
    private boolean confirmClose = true;

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
                if (!isChecked) {
                    Analytics.logEvent("MessageAssistant_Disable", "From", "Settings");
                }
            }
        });

        View itemUpload = findViewById(R.id.setting_item_upload);
        itemUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo upload color phone
            }
        });

        boolean chargingImproverOpen = ModuleUtils.isChargingImproverEnabled();
        if (chargingImproverOpen) {
            TextView tv = findViewById(R.id.setting_item_charging_title);
            tv.setText(R.string.charging_improver_title);
        }

        View itemCharging = findViewById(R.id.setting_item_charging);
        if (chargingImproverOpen || SmartChargingSettings.isSmartChargingConfigEnabled()) {
            itemCharging.setVisibility(View.VISIBLE);
            itemCharging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChargingSettingsActivity.start(SettingsActivity.this);
                }
            });
        } else {
            itemCharging.setVisibility(View.GONE);
        }

        View itemLocker = findViewById(R.id.setting_item_lockScreen);
        if (LockerSettings.isLockerConfigEnabled()) {
            itemLocker.setVisibility(View.VISIBLE);
            itemLocker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LockerSettingsActivity.start(SettingsActivity.this);
                }
            });
        } else {
            itemLocker.setVisibility(View.GONE);
        }

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

        lifeAssistant = new ModuleState(LifeAssistantConfig.isLifeAssistantConfigEnable(),
                LifeAssistantConfig.isLifeAssistantSettingEnable(),
                R.id.setting_item_life_assistant_toggle,
                R.id.setting_item_life_assistant) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                if (!isChecked && confirmClose) {
                    lifeAssistant.switchCompat.setChecked(true);
                    showConfirmDialog();
                } else {
                    confirmClose = true;
                    LifeAssistantConfig.setLifeAssistantSettingEnable(isChecked);
                }
//                Analytics.logEvent("Settings_Toolbar_Clicked_" +
//                        (isChecked ? "Enabled" : "Disabled"));
            }
        };

        mModuleStates.add(lifeAssistant);

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

    @Override public void onBackPressed() {
        if (confirmDialog != null && confirmDialog.getVisibility() == View.VISIBLE) {
            confirmDialog.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
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

        if (confirmDialog != null) {
            confirmDialog.setVisibility(View.GONE);
        }
    }

    private void showConfirmDialog() {
        confirmDialog = findViewById(R.id.close_confirm_dialog);

        View content = confirmDialog.findViewById(R.id.content_layout);
        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));

        View btn = confirmDialog.findViewById(R.id.tv_first);
        btn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(26), true));
        btn.setOnClickListener(v -> {
            confirmDialog.setVisibility(View.GONE);
        });

        btn = confirmDialog.findViewById(R.id.tv_second);
        btn.setOnClickListener(v -> {
            confirmDialog.setVisibility(View.GONE);
            confirmClose = false;
            lifeAssistant.switchCompat.setChecked(false);

            Analytics.logEvent("Life_Assistant_Settings_Disable", "Source", "Settings");
        });

        confirmDialog.setVisibility(View.VISIBLE);

        Analytics.logEvent("Life_Assistant_Settings_PopUp_Show", "Source", "Settings");
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

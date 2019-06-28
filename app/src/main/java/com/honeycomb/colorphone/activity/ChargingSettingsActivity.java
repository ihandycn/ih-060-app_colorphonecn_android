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

import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.UserSettings;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.chargingimprover.ChargingImproverUtils;
import com.messagecenter.customize.MessageCenterSettings;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;

public class ChargingSettingsActivity extends HSAppCompatActivity {

    private static final String TAG = ChargingSettingsActivity.class.getSimpleName();

    private List<ModuleState> mModuleStates = new ArrayList<>();

    public static void start(Context context) {
        Intent starter = new Intent(context, ChargingSettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_settings_charging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.smart_charging);

        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        boolean chargingImproverOpen = ModuleUtils.isChargingImproverEnabled();
        if (chargingImproverOpen) {
            TextView tv = findViewById(R.id.setting_item_charging_title);
            tv.setText(R.string.charging_improver_title);
        }
        TextView lockTextView = findViewById(R.id.screen_notification_hint);
        lockTextView.setText("在充电锁屏上显示通知消息");
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
                SmartChargingSettings.setModuleEnabled(isChecked);
                ChargingImproverUtils.setChargingImproverUserEnabled(isChecked);
            }
        });


        mModuleStates.add(new ModuleState(
                false, false,
                R.id.setting_item_lockScreen_toggle,
                R.id.setting_item_lockScreen) {
            @Override
            public void onCheckChanged(boolean isChecked) {
            }
        });

        mModuleStates.add(new ModuleState(true,
                LockerSettings.needShowNotificationCharging(),
                R.id.notification_screen_switch,
                R.id.settings_notification_on_screen) {
            @Override
            public void onCheckChanged(boolean isChecked) {
                Analytics.logEvent("ColorPhone_ChargingScreen_Notification_Close_By_Settings");
                Preferences.get(LOCKER_PREFS).putBoolean(LockerSettings.PREF_KEY_NOTIFICATION_CHARGING, isChecked);
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

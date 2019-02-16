package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Preferences;

public class NotificationSettingsActivity extends HSAppCompatActivity implements View.OnClickListener {

    public static final String PREF_KEY_NOTIFICATION_SETTINGS_BOOST = "PREF_KEY_NOTIFICATION_SETTINGS_BOOST";
    public static final String PREF_KEY_NOTIFICATION_SETTINGS_CLEAN = "PREF_KEY_NOTIFICATION_SETTINGS_CLEAN";
    public static final String PREF_KEY_NOTIFICATION_SETTINGS_CPU = "PREF_KEY_NOTIFICATION_SETTINGS_CPU";
    public static final String PREF_KEY_NOTIFICATION_SETTINGS_BATTERY = "PREF_KEY_NOTIFICATION_SETTINGS_BATTERY";
    public static final String PREF_KEY_NOTIFICATION_SETTINGS_VIRUS = "PREF_KEY_NOTIFICATION_SETTINGS_VIRUS";
    public static final String PREF_KEY_NOTIFICATION_SETTINGS_FILE = "PREF_KEY_NOTIFICATION_SETTINGS_FILE";

    private SwitchCompat boostSwitch;
    private SwitchCompat cleanSwitch;
    private SwitchCompat cpuSwitch;
    private SwitchCompat batterySwitch;
    private SwitchCompat virusSwitch;
    private SwitchCompat fileSwitch;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings_notification_settings);

        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

//        ActivityUtils.setCustomColorStatusBar(this, getResources().getColor(R.color.blue_primary));
//        ActivityUtils.configSimpleAppBar(this, getResources().getString(R.string.settings_notification_settings), getResources().getColor(R.color.blue_primary));

        findViewById(R.id.boost_switch).setOnClickListener(this);
//        findViewById(R.id.clean_switch).setOnClickListener(this);
//        findViewById(R.id.cpu_switch).setOnClickListener(this);
//        findViewById(R.id.battery_switch).setOnClickListener(this);
//        findViewById(R.id.virus_switch).setOnClickListener(this);
//        findViewById(R.id.file_switch).setOnClickListener(this);

        boostSwitch = (SwitchCompat) findViewById(R.id.boost_switch_indicator);
//        cleanSwitch = (SwitchCompat) findViewById(R.id.clean_switch_indicator);
//        cpuSwitch = (SwitchCompat) findViewById(R.id.cpu_switch_indicator);
//        batterySwitch = (SwitchCompat) findViewById(R.id.battery_switch_indicator);
//        virusSwitch = (SwitchCompat) findViewById(R.id.virus_switch_indicator);
//        fileSwitch = (SwitchCompat) findViewById(R.id.file_switch_indicator);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.boost_switch:
                boolean status = !isNotificationBoostOn();
                setNotificationBoostOn(status);
                boostSwitch.setChecked(status);
                if (!status) {
                    Analytics.logEvent("Settings_NotificationSettings_Closed", "Type", "Boost");
                }
                break;
//            case R.id.clean_switch:
//                status = !PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CLEAN, true);
//                PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).putBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CLEAN, status);
//                cleanSwitch.setChecked(status);
//                if (!status) {
//                    SecurityAnalytics.logEvent("Settings_NotificationSettings_Closed", "Type", "Clean");
//                }
//                break;
//            case R.id.cpu_switch:
//                status = !PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CPU, true);
//                PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).putBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CPU, status);
//                cpuSwitch.setChecked(status);
//                if (!status) {
//                    SecurityAnalytics.logEvent("Settings_NotificationSettings_Closed", "Type", "CPU");
//                }
//                break;
//            case R.id.battery_switch:
//                status = !PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BATTERY, true);
//                PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).putBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BATTERY, status);
//                batterySwitch.setChecked(status);
//                if (!status) {
//                    SecurityAnalytics.logEvent("Settings_NotificationSettings_Closed", "Type", "Battery");
//                }
//                break;
//            case R.id.virus_switch:
//                status = !PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_VIRUS, true);
//                PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).putBoolean(PREF_KEY_NOTIFICATION_SETTINGS_VIRUS, status);
//                virusSwitch.setChecked(status);
//                if (!status) {
//                    SecurityAnalytics.logEvent("Settings_NotificationSettings_Closed", "Type", "VirusScan");
//                }
//                break;
//            case R.id.file_switch:
//                status = !PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_FILE, true);
//                PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).putBoolean(PREF_KEY_NOTIFICATION_SETTINGS_FILE, status);
//                fileSwitch.setChecked(status);
//                if (!status) {
//                    SecurityAnalytics.logEvent("Settings_NotificationSettings_Closed", "Type", "FileScan");
//                }
//                break;
        }
    }

    private void refresh() {
        boostSwitch.setChecked(isNotificationBoostOn());
//        cleanSwitch.setChecked(PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CLEAN, true));
//        cpuSwitch.setChecked(PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CPU, true));
//        batterySwitch.setChecked(PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BATTERY, true));
//        virusSwitch.setChecked(PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_VIRUS, true));
//        fileSwitch.setChecked(PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_FILE, true));
    }

    public static boolean isNotificationBoostOn() {
        return Preferences.get(Constants.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BOOST, false);
    }

    public static void setNotificationBoostOn(boolean on) {
        Preferences.get(Constants.NOTIFICATION_PREFS).putBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BOOST, on);
    }

//    public static boolean isSwitchOn(NotificationType type) {
//        switch (type) {
//            case TYPE_BOOST:
//                return PreferenceHelper.get(Constants.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BOOST, true);
//            case TYPE_BATTERY:
//                return PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_BATTERY, true);
//            case TYPE_CPUCOOLER:
//                return PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CPU, true);
//            case TYPE_CLEAN:
//                return PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_CLEAN, true);
//            case TYPE_VIRUSSCAN:
//                return PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_VIRUS, true);
//            case TYPE_FILESCAN:
//                return PreferenceHelper.get(SecurityFiles.NOTIFICATION_PREFS).getBoolean(PREF_KEY_NOTIFICATION_SETTINGS_FILE, true);
//            case TYPE_APPLOCK:
//                return true;
//            case TYPE_VIRUSSCAN_INSTALL:
//                return true;
//            default:
//                return true;
//        }
//    }
}

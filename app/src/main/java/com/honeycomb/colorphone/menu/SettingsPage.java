package com.honeycomb.colorphone.menu;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashSettings;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.AboutActivity;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.LedFlashSettingsActivity;
import com.honeycomb.colorphone.activity.SettingsActivity;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

import colorphone.acb.com.libweather.WeatherActivity;

public class SettingsPage implements View.OnClickListener {
    private SwitchCompat mainSwitch;

    private TextView mainSwitchTxt;
    private boolean initCheckState;


    public void initPage(View rootView) {
        mainSwitch = rootView.findViewById(R.id.main_switch);
        mainSwitchTxt = rootView.findViewById(R.id.settings_main_switch_txt);

        boolean dialerEnable = false;
        rootView.findViewById(R.id.settings_default_dialer_switch)
                .setVisibility(dialerEnable ? View.VISIBLE : View.GONE);

        initCheckState = ScreenFlashSettings.isScreenFlashModuleEnabled();
        mainSwitch.setChecked(initCheckState);
        mainSwitchTxt.setText(getString(initCheckState ? R.string.color_phone_enabled : R.string.color_phone_disable));

        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainSwitchTxt.setText(getString(isChecked ? R.string.color_phone_enabled : R.string.color_phone_disable));
                ScreenFlashSettings.setScreenFlashModuleEnabled(isChecked);
                LauncherAnalytics.logEvent("ColorPhone_Settings_Enable_Icon_Clicked", "type", isChecked ? "on" : "off");
            }
        });

        rootView.findViewById(R.id.settings_main_switch).setOnClickListener(this);
        rootView.findViewById(R.id.settings_default_dialer_switch).setOnClickListener(this);
        rootView.findViewById(R.id.settings_led_flash).setOnClickListener(this);
//        rootView.findViewById(R.id.settings_notification_toolbar).setOnClickListener(this);
        rootView.findViewById(R.id.settings_feedback).setOnClickListener(this);
//        rootView.findViewById(R.id.settings_boost).setOnClickListener(this);
        rootView.findViewById(R.id.settings_setting).setOnClickListener(this);
        rootView.findViewById(R.id.settings_contacts).setOnClickListener(this);
        rootView.findViewById(R.id.settings_about).setOnClickListener(this);
        rootView.findViewById(R.id.settings_facebook).setOnClickListener(this);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }

    private String getString(int id) {
        return HSApplication.getContext().getString(id);
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        switch (v.getId()) {
            case R.id.settings_main_switch:
                mainSwitch.toggle();
                break;
            case R.id.settings_default_dialer_switch:
//                defaultDialer.toggle();
                break;
            case R.id.settings_led_flash:
                LedFlashSettingsActivity.start(context);
                break;
//            case R.id.settings_notification_toolbar:
//                toggleNotificationToolbar();
//                break;
            case R.id.settings_feedback:
                feedBack();
                ColorPhoneApplication.getConfigLog().getEvent().onFeedBackClick();
                break;
//            case R.id.settings_boost:
//                BoostActivity.start(ColorPhoneActivity.context, false);
//                LauncherAnalytics.logEvent("Colorphone_Settings_Boost_Icon_Clicked");
//                break;
            case R.id.settings_setting:
                LauncherAnalytics.logEvent("Colorphone_Settings_Clicked");
                SettingsActivity.start(context);
                break;
            case R.id.settings_contacts:
                ContactsActivity.startEdit(context);
                LauncherAnalytics.logEvent("Colorphone_Settings_ContactTheme_Clicked");
                break;
            case R.id.settings_about:
                AboutActivity.start(context);
                break;
            case R.id.settings_facebook:
                Navigations.openBrowser(context,
                        BuildConfig.FLAVOR.equals("colorflash") ?
                                "https://business.facebook.com/Color-Call-Call-Screen-LED-Flash-Ringtones-342916819531161"
                                :
                                "https://www.facebook.com/pg/Color-Phone-560161334373476");
                break;
        }
    }

    private void feedBack() {
        Utils.sentEmail(HSApplication.getContext(), new String[] {Constants.getFeedBackAddress()}, null, null);
    }

    public void onSaveToggleState() {
        if (mainSwitch != null) {
            boolean nowEnable = mainSwitch.isChecked();
            if (nowEnable != initCheckState) {
                initCheckState = nowEnable;
                ColorPhoneApplication.getConfigLog().getEvent().onColorPhoneEnableFromSetting(nowEnable);
            }
        }
    }

    public void onThemeSelected() {
        mainSwitch.setChecked(true);
    }
}

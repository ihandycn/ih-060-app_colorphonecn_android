package com.honeycomb.colorphone.menu;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashSettings;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.FlashManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.AboutActivity;
import com.honeycomb.colorphone.activity.ContactsActivity;
import com.honeycomb.colorphone.activity.LoginActivity;
import com.honeycomb.colorphone.activity.SettingsActivity;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.feedback.FeedbackActivity;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.uploadview.UploadAndPublishActivity;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

public class SettingsPage implements View.OnClickListener {
    private SwitchCompat mainSwitch;

    private TextView mainSwitchTxt;
    private boolean initCheckState;
    private SwitchCompat defaultDialer;
    private SwitchCompat ledSwitch;

    private boolean init = false;
    private View rootView;

    public boolean isInit() {
        return init;
    }

    public View getRootView() {
        return rootView;
    }

    public void initPage(View rootView) {
        init = true;
        this.rootView = rootView;
        mainSwitch = rootView.findViewById(R.id.main_switch);
        mainSwitchTxt = rootView.findViewById(R.id.settings_main_switch_txt);

        boolean dialerEnable = ConfigEvent.dialerEnable();
        rootView.findViewById(R.id.settings_default_dialer_switch)
                .setVisibility(dialerEnable ? View.VISIBLE : View.GONE);
        defaultDialer = rootView.findViewById(R.id.default_dialer_switch);
        defaultDialer.setChecked(DefaultPhoneUtils.isDefaultPhone());
        defaultDialer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isChecked) {
                        DefaultPhoneUtils.checkDefaultWithoutEvent((Activity) rootView.getContext());
                    } else {
                        DefaultPhoneUtils.resetDefaultPhone();
                    }
                }
            }
        });
        initCheckState = ScreenFlashSettings.isScreenFlashModuleEnabled();
        mainSwitch.setChecked(initCheckState);
        mainSwitchTxt.setText(getString(initCheckState ? R.string.color_phone_enabled : R.string.color_phone_disable));

        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainSwitchTxt.setText(getString(isChecked ? R.string.color_phone_enabled : R.string.color_phone_disable));
                ScreenFlashSettings.setScreenFlashModuleEnabled(isChecked);
                Analytics.logEvent("Settings_Enable_Icon_Clicked", "type", isChecked ? "on" : "off");
            }
        });

        ledSwitch = rootView.findViewById(R.id.led_flash_call_switch);
        ledSwitch.setChecked(Preferences.get(Constants.DESKTOP_PREFS).getBoolean(Constants.PREFS_LED_FLASH_ENABLE, false));
        ledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Preferences.get(Constants.DESKTOP_PREFS).putBoolean(Constants.PREFS_LED_FLASH_ENABLE, isChecked);
                if (isChecked) {
                    FlashManager.getInstance().startFlash(3);
                    Analytics.logEvent("LEDReminder_Enabled_FromSettings");
                } else {
                    Analytics.logEvent("LEDReminder_Disabled_FromSettings");
                }
            }
        });

        rootView.findViewById(R.id.settings_account).setOnClickListener(this);
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
        rootView.findViewById(R.id.settings_upload).setOnClickListener(this);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && defaultDialer != null) {
            defaultDialer.setChecked(DefaultPhoneUtils.isDefaultPhone());
        }
    }

    private String getString(int id) {
        return HSApplication.getContext().getString(id);
    }

    @Override
    public void onClick(View v) {
        Context context = v.getContext();
        switch (v.getId()) {
            case R.id.settings_account:
                LoginActivity.start(context);
                break;
            case R.id.settings_main_switch:
                mainSwitch.toggle();
                break;
            case R.id.settings_default_dialer_switch:
                Analytics.logEvent("Settings_Default_Icon_Clicked",
                        "Type", (defaultDialer.isChecked() ? "OFF" : "ON"));
                defaultDialer.toggle();
                break;
            case R.id.settings_led_flash:
                ledSwitch.toggle();
                break;
            case R.id.settings_feedback:
                feedBack();
                ColorPhoneApplication.getConfigLog().getEvent().onFeedBackClick();
                break;
            case R.id.settings_setting:
                Analytics.logEvent("Settings_Clicked");
                SettingsActivity.start(context);
                break;
            case R.id.settings_contacts:
                ContactsActivity.startEdit(context);
                Analytics.logEvent("Settings_ContactTheme_Clicked");
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
            case R.id.settings_upload:
                if (!"null".equals(HttpManager.getInstance().getUserToken())) {
                    UploadAndPublishActivity.start(context);
                } else {
                    Toasts.showToast(context.getResources().getString(R.string.not_login));
                }

                break;
            default:
                break;
        }
    }

    private void feedBack() {
//        Utils.sentEmail(HSApplication.getContext(), new String[] {Constants.getFeedBackAddress()}, null, null);
        Navigations.startActivitySafely(HSApplication.getContext(), FeedbackActivity.class);
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
        if (mainSwitch != null) {
            mainSwitch.setChecked(true);
        }
    }
}

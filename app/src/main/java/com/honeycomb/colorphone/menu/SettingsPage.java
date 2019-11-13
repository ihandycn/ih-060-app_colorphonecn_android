package com.honeycomb.colorphone.menu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.honeycomb.colorphone.activity.UserInfoEditorActivity;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.feedback.FeedbackActivity;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.bean.LoginUserBean;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.uploadview.UploadAndPublishActivity;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.honeycomb.colorphone.wallpaper.customize.activity.MyWallpaperActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;

public class SettingsPage implements View.OnClickListener {
    public static boolean avatarCropFinishied;
    private Context context;
    private SwitchCompat mainSwitch;
    private TextView mainSwitchTxt;
    private boolean initCheckState;
    private SwitchCompat defaultDialer;
    private SwitchCompat ledSwitch;
    private ImageView avatarView;
    private TextView nameView;
    private TextView signView;

    private boolean init = false;
    private View rootView;
    private LoginUserBean.UserInfoBean userInfo;

    public boolean isInit() {
        return init;
    }

    public View getRootView() {
        return rootView;
    }

    public void initPage(View rootView, Context context) {
        init = true;
        this.context = context;
        this.rootView = rootView;
        mainSwitch = rootView.findViewById(R.id.main_switch);
        mainSwitchTxt = rootView.findViewById(R.id.settings_main_switch_txt);
        avatarView = rootView.findViewById(R.id.settings_avatar_icon);
        nameView = rootView.findViewById(R.id.setting_name);
        signView = rootView.findViewById(R.id.setting_sign);

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
        if (!HSConfig.optBoolean(true, "Application", "Wallpapers", "Enabled")) {
            rootView.findViewById(R.id.settings_mywallpapers).setVisibility(View.GONE);
        } else {
            rootView.findViewById(R.id.settings_mywallpapers).setOnClickListener(this);
        }

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
        avatarCropFinishied = false;
        refreshUserInfo();
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
                onClickAccountView(context);
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
            case R.id.settings_mywallpapers:
                Navigations.startActivity(rootView.getContext(), MyWallpaperActivity.class);
                Analytics.logEvent("Settings_MyWallpaper_Clicked");
                break;
            case R.id.settings_upload:
                if (HttpManager.getInstance().isLogin()) {
                    UploadAndPublishActivity.start(context);
                } else {
                    Toasts.showToast(context.getResources().getString(R.string.not_login));
                }

                break;
            default:
                break;
        }
    }

    private void onClickAccountView(Context context) {
        if (userInfo != null) {
            UserInfoEditorActivity.start(context, userInfo);
        } else {
            LoginActivity.start(context);
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

    public void refreshUserInfo() {
        Resources resources = context.getResources();
        if (HttpManager.getInstance().isLogin()) {
            HttpManager.getInstance().getSelfUserInfo(new Callback<LoginUserBean>() {
                @Override
                public void onFailure(String errorMsg) {
                    avatarView.setImageResource(R.drawable.settings_icon_avatar);
                    nameView.setText("点击编辑用户信息");
                    nameView.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.settings_name_edit), null);
                    signView.setText(R.string.settings_sign);
                    signView.setVisibility(View.VISIBLE);
                    Toast.makeText(getRootView().getContext(), "获取用户信息失败！", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(LoginUserBean loginUserBean) {
                    showUserInfo(loginUserBean.getUser_info());
                }
            });

        } else {
            userInfo = null;
            avatarView.setImageResource(R.drawable.settings_icon_avatar);
            nameView.setText(R.string.settings_login);
            nameView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            signView.setVisibility(View.GONE);
        }
    }

    public void showUserInfo(LoginUserBean.UserInfoBean userInfo) {

        if (((Activity) context).isDestroyed()) {
            return;
        }

        this.userInfo = userInfo;
        GlideApp.with(context)
                .asBitmap()
                .load(userInfo.getHead_image_url())
                .placeholder(R.drawable.settings_icon_avatar)
                .into(avatarView);
        if (avatarCropFinishied) {
            avatarCropFinishied = false;
            avatarView.setImageBitmap(BitmapFactory.decodeFile(UserInfoEditorActivity.getTempImagePath()));
        }
        String name = userInfo.getName();
        if (TextUtils.isEmpty(name)) {
            nameView.setText("匿名");
        } else {
            nameView.setText(name);
        }
        String sign = userInfo.getSignature();
        if (TextUtils.isEmpty(sign)) {
            signView.setText(R.string.settings_sign);
        } else {
            signView.setText(sign);
        }
        nameView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.settings_name_edit), null);
        signView.setVisibility(View.VISIBLE);
    }
}

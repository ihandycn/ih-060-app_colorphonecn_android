package com.honeycomb.colorphone.weather;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.StringDef;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.acb.call.utils.FileUtils;
import com.acb.call.views.VideoPlayerView;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Preferences;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;

import colorphone.acb.com.libweather.base.BaseAppCompatActivity;

/**
 * Created by zqs on 2019/4/9.
 */
public class WeatherVideoActivity extends BaseAppCompatActivity {

    public static final String DISABLE_WEATHER_PUSH = "DISABLE_WEATHER_PUSH";
    private static final String WEATHER_TEXT_SHOW_TIME = "weather_text_show_time";
    public static final String SUNNY = "sunny";
    public static final String CLOUDY = "cloudy";
    public static final String RAIN = "rain";
    public static final String SNOW = "snow";
    public static final String REAL = "real";
    private static final String VIDEO_TYPE = "video_type";
    private ImageView ivSetting;
    private ImageView ivClose;
    private ImageView ivCallCccept;
    private VideoPlayerView videoPlayerView;

    @StringDef({SUNNY, CLOUDY, RAIN, SNOW, REAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VideoType {
    }

    public static String allVideoCategory[] = {SUNNY, CLOUDY, RAIN, SNOW, REAL};
    private String videoType;
    private boolean inMorning;
    private HomeKeyWatcher homeKeyWatcher;

    public static void start(Context context, @VideoType String videoType) {
        Intent intent = new Intent(context, WeatherVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(VIDEO_TYPE, videoType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            videoType = getIntent().getStringExtra(VIDEO_TYPE);
        }
        setContentView(colorphone.acb.com.libweather.R.layout.activity_weather_video);
        Preferences.getDefault().putLong(WeatherPushManager.SHOW_LEGAL_INTERVAL, System.currentTimeMillis());
        Ap.WeatherPush.logEvent("weather_forecast_show");
        initView();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        inMorning = 6 <= hourOfDay && hourOfDay < 9;
        LauncherAnalytics.logEvent("weather_forecast_show", "type", inMorning ? "morning" : "night", "videotype", videoType,
                "time", getShowTimeEventParameter());
        homeKeyWatcher = new HomeKeyWatcher(this);
        homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                LauncherAnalytics.logEvent("weather_forecast_close", "type", "home");
            }

            @Override
            public void onRecentsPressed() {

            }
        });
        homeKeyWatcher.startWatch();
    }

    private String getShowTimeEventParameter() {
        int showTime = Preferences.getDefault().getInt(WEATHER_TEXT_SHOW_TIME, 1);
        if (showTime < 5) {
            return String.valueOf(showTime);
        } else {
            return "other";
        }

    }

    private void initView() {
        ivSetting = findViewById(colorphone.acb.com.libweather.R.id.iv_setting);
        ivClose = findViewById(colorphone.acb.com.libweather.R.id.iv_close);
        videoPlayerView = findViewById(R.id.animation_view);

        File real = new File(FileUtils.getMediaDirectory(), videoType);
        videoPlayerView.setFileDirectory(real.getAbsolutePath());
        videoPlayerView.play();
        ivCallCccept = findViewById(colorphone.acb.com.libweather.R.id.iv_call_accept);
        ivCallCccept.setOnClickListener(onClickListener);
        ivSetting.setOnClickListener(onClickListener);
        ivClose.setOnClickListener(onClickListener);
        if (Ap.WeatherPush.allowFullScreenClick()) {
            videoPlayerView.setOnClickListener(onClickListener);
        }
        int showTime = Preferences.getDefault().getInt(WEATHER_TEXT_SHOW_TIME, 0);
        if (showTime > Ap.WeatherPush.maxShowTime()) {
            findViewById(R.id.tv_title).setVisibility(View.GONE);
            findViewById(R.id.tv_weather_content).setVisibility(View.GONE);
        }
        Preferences.getDefault().putInt(WEATHER_TEXT_SHOW_TIME, ++showTime);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == ivSetting) {
                LauncherAnalytics.logEvent("weather_forecast_settings_click");
                showPopupView();
            } else if (v == ivClose) {
                Ap.WeatherPush.logEvent("weather_forecast_closebtn_click");
                LauncherAnalytics.logEvent("weather_forecast_close", "type", "closebtn");
                WeatherVideoActivity.this.finish();
            } else if (v == ivCallCccept || v == videoPlayerView) {
                // TODO: 2019/4/13  pop up weather page
                Ap.WeatherPush.logEvent("weather_forecast_click");
                LauncherAnalytics.logEvent("weather_forecast_click", "from", v == ivCallCccept ? "answerbtn" :
                        "otherarea", "type", inMorning ? "morning" : "night", videoType, videoType, "time", getShowTimeEventParameter());
                ColorPhoneActivity.startWeatherPage(WeatherVideoActivity.this);

            }
        }
    };

    private void showPopupView() {
        View disableView = View.inflate(this, R.layout.weather_disable_popup_window, null);
        PopupWindow popupWindow = new PopupWindow(disableView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        disableView.findViewById(R.id.disable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    popupWindow.dismiss();
                } catch (Exception e) {
                }
                LauncherAnalytics.logEvent("weather_forecast_settings_disable_click");
                showDisableDialog();
            }
        });
        try {
            popupWindow.showAsDropDown(ivSetting, 0, Dimensions.pxFromDp(6));
        } catch (Exception e) {
        }

    }

    @Override
    public void onBackPressed() {
        if (Ap.WeatherPush.allowBack()) {
            LauncherAnalytics.logEvent("weather_forecast_close", "type", "back");
            super.onBackPressed();
        }
    }

    private void showDisableDialog() {
        Dialog dialog = new Dialog(this, R.style.BaseDialogTheme);
        View view = View.inflate(this, R.layout.dialog_weather_push, null);
        View disable = view.findViewById(R.id.tv_disable);
        View notNow = view.findViewById(R.id.tv_not_now);
        disable.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.button_disable), getResources().getDimensionPixelSize(R.dimen.dialog_btn_corner_radius), true));
        notNow.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.material_color_accent), getResources().getDimensionPixelSize(R.dimen.dialog_btn_corner_radius), true));
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = (int) (Dimensions.getPhoneWidth(this) * 0.82);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            dialogWindow.setAttributes(lp);
            disable.setOnClickListener(v -> {
                LauncherAnalytics.logEvent("weather_forecast_settings_disable_success");
                Preferences.getDefault().putBoolean(DISABLE_WEATHER_PUSH, true);
                dismissDialogSafely(dialog);
            });
            notNow.setOnClickListener(v -> {
                dismissDialogSafely(dialog);

            });
            if (!dialog.isShowing()) {
                try {
                    dialog.show();
                } catch (Exception e) {
                }
            }
        }


    }

    private void dismissDialogSafely(Dialog dialog) {
        if (dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (homeKeyWatcher != null) {
            homeKeyWatcher.stopWatch();
        }
    }
}

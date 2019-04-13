package com.honeycomb.colorphone.weather;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.acb.call.MediaDownloadManager;
import com.acb.call.utils.FileUtils;
import com.acb.call.views.VideoPlayerView;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;

import java.io.File;

import colorphone.acb.com.libweather.base.BaseAppCompatActivity;

/**
 * Created by zqs on 2019/4/9.
 */
public class WeatherVideoActivity extends BaseAppCompatActivity {

    private static final String DISABLE_WEATHER_PUSH = "DISABLE_WEATHER_PUSH";
    private ImageView ivSetting;
    private ImageView ivClose;
    private ImageView ivCallCccept;
    private VideoPlayerView videoPlayerView;
    private MediaDownloadManager mediaDownloadManager;

    public static void start(Context context) {
        Intent intent = new Intent(context, WeatherVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(colorphone.acb.com.libweather.R.layout.activity_weather_video);
        LauncherAnalytics.logEvent("weather_forecast_show");
        initView();
        download();
    }

    private void download() {
        mediaDownloadManager = new MediaDownloadManager();
        String url = HSConfig.optString("https://superapps-dev.s3.amazonaws.com/test/sunny.mp4", "Application", "WeatherVideo", "Real");

        mediaDownloadManager.downloadMedia(url, "Real", new MediaDownloadManager.DownloadCallback() {
            @Override
            public void onUpdate(long progress) {

            }

            @Override
            public void onFail(MediaDownloadManager.MediaDownLoadTask task, String msg) {

            }

            @Override
            public void onSuccess(MediaDownloadManager.MediaDownLoadTask task) {
                File real = new File(FileUtils.getMediaDirectory(), "Real");
                videoPlayerView.setFileDirectory(real.getAbsolutePath());
                videoPlayerView.play();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void initView() {
        ivSetting = findViewById(colorphone.acb.com.libweather.R.id.iv_setting);
        ivClose = findViewById(colorphone.acb.com.libweather.R.id.iv_close);
        videoPlayerView = findViewById(R.id.animation_view);

        ivCallCccept = findViewById(colorphone.acb.com.libweather.R.id.iv_call_accept);
        ivSetting.setOnClickListener(onClickListener);
        ivClose.setOnClickListener(onClickListener);
        ivCallCccept.setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == ivSetting) {
                showDisableDialog();
            } else if (v == ivClose) {
                WeatherVideoActivity.this.finish();
            } else if (v == ivCallCccept) {
                // TODO: 2019/4/13  pop up weather page
            }
        }
    };

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
                Preferences.getDefault().putBoolean(DISABLE_WEATHER_PUSH, true);
            });
            notNow.setOnClickListener(v -> {
                if (dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            });
            if (!dialog.isShowing()) {
                try {
                    dialog.show();
                } catch (Exception e) {
                }
            }
        }


    }
}

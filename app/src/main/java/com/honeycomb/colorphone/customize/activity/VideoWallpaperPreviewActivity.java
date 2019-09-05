package com.honeycomb.colorphone.customize.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.colorphone.lock.lockscreen.locker.Locker;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseAppCompatActivity;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.customize.view.TextureVideoView;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

/**
 * @author sundxing
 */
public class VideoWallpaperPreviewActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private TextureVideoView textureVideoView;
    private View ringtoneImage;
    private String path;
    private View audioMenuLayout;
    private View wallpaperSetButtton;
    private boolean hasAudio;
    private boolean audioSeletorVisible;
    private WallpaperInfo mWallpaperInfo;

    public static void start(Context context, String path, boolean hasAudio, WallpaperInfo info) {
        Intent starter = new Intent(context, VideoWallpaperPreviewActivity.class);
        starter.putExtra("path", path);
        starter.putExtra("audio", hasAudio);
        starter.putExtra("info", info);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CustomizeUtils.setWallpaperWindowFlags(this);

        path = getIntent().getStringExtra("path");
        hasAudio = getIntent().getBooleanExtra("audio", false);
        mWallpaperInfo = getIntent().getParcelableExtra("info");

        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_wallpaper_preview_video);
        ringtoneImage = findViewById(R.id.ringtone_image);
        ringtoneImage.setVisibility(hasAudio ? View.VISIBLE : View.GONE);
        ringtoneImage.setOnClickListener(this);
        findViewById(R.id.nav_back).setOnClickListener(this);

        wallpaperSetButtton = findViewById(R.id.set_wallpaper_button);
        wallpaperSetButtton.setOnClickListener(this);

        View useAudioView = findViewById(R.id.wallpaper_set_use_audio);
        useAudioView.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.white_87_transparent),
                Color.parseColor("#33000000"),
                Dimensions.pxFromDp(16),Dimensions.pxFromDp(16),
                0, 0,
                false,true));
        useAudioView.setOnClickListener(this);
        View noAudioView = findViewById(R.id.wallpaper_set_no_audio);
        noAudioView.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.white_87_transparent),
                Color.parseColor("#33000000"),
                0, 0,
                Dimensions.pxFromDp(16),Dimensions.pxFromDp(16),
                false,true));
        noAudioView.setOnClickListener(this);

        audioMenuLayout = findViewById(R.id.wallpaper_set_audio_select_layout);
        textureVideoView = findViewById(R.id.video_view);
        textureVideoView.setVideoPath(path);
        textureVideoView.setLooping(true);
        textureVideoView.setOnClickListener(this);
        muteOff();

        Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_show"), "Type", hasAudio ? "Video" : "Live");
    }

    @Override
    protected void onResume() {
        super.onResume();
        textureVideoView.setVisibility(View.VISIBLE);
        textureVideoView.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        textureVideoView.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nav_back:
                onBackPressed();
                break;
            case R.id.set_wallpaper_button:
                Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_set_click"),
                        "Type", hasAudio ? "Video" : "Live");
                if (hasAudio) {
                    if (enableAudio()) {
                        showAudioSelector();
                        Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_set_sound_alert_show"));
                    } else {
                        CustomizeUtils.setVideoAudioStatus(CustomizeUtils.VIDEO_AUDIO_OFF);
                        onSetWallpaper();
                    }

                } else {
                    CustomizeUtils.setVideoAudioStatus(CustomizeUtils.VIDEO_NO_AUDIO);
                    onSetWallpaper();
                }
                break;
            case R.id.ringtone_image:
                onRingtoneClick();
                break;
            case R.id.wallpaper_set_no_audio:
                Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_set_sound_alert_click"), "sound", "no");
                CustomizeUtils.setVideoAudioStatus(CustomizeUtils.VIDEO_AUDIO_OFF);
                onSetWallpaper();
                break;
            case R.id.wallpaper_set_use_audio:
                Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_set_sound_alert_click"), "sound", "yes");
                CustomizeUtils.setVideoAudioStatus(CustomizeUtils.VIDEO_AUDIO_ON);
                onSetWallpaper();
                break;
            case R.id.video_view:
                if (audioSeletorVisible) {
                    hideAudioSelector();
                }
                break;
            default:
                break;
        }
    }

    private boolean enableAudio() {
        return HSConfig.optBoolean( false,"Application", "Wallpaper", "DetailPageSoundChoiceEnable");
    }

    private void showAudioSelector() {
        audioSeletorVisible = true;
        wallpaperSetButtton.animate().alpha(0).setDuration(200).start();

        float pivotX = Dimensions.getPhoneWidth(getApplicationContext()) / 2 - Dimensions.pxFromDp(32);
        audioMenuLayout.setVisibility(View.VISIBLE);
        audioMenuLayout.setAlpha(0);
        audioMenuLayout.setScaleX(0.1f);
        audioMenuLayout.setScaleY(0.1f);
        audioMenuLayout.setPivotY(Dimensions.pxFromDp(100));
        audioMenuLayout.setPivotX(pivotX);
        audioMenuLayout.animate().alpha(1).scaleX(1).scaleY(1)
                .setDuration(200).start();

    }

    private void hideAudioSelector() {
        audioSeletorVisible = false;
        wallpaperSetButtton.animate().alpha(1).setDuration(200).start();
        audioMenuLayout.animate().alpha(0).scaleX(0.1f).scaleY(0.1f)
                .setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                audioMenuLayout.setVisibility(View.GONE);
            }
        }).start();

    }

    private void onRingtoneClick() {
        toggle();
    }

    private void onSetWallpaper() {
        hideAudioSelector();

        CustomizeUtils.setLockerWallpaperPath(path);
        HSGlobalNotificationCenter.sendNotification(Locker.EVENT_WALLPAPER_CHANGE);
        showSuccessToast();
        Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_set_success"),
                "Type", hasAudio ? "Video" : "Live");
    }

    private void showSuccessToast() {
        View toastView = findViewById(R.id.apply_success_text);
        toastView.setVisibility(View.VISIBLE);
        toastView.setAlpha(0);
        toastView.animate().alpha(1).setDuration(200).start();
        toastView.postDelayed(new Runnable() {
            @Override
            public void run() {
                toastView.animate().alpha(0).setDuration(200).start();
            }
        }, 3000);
    }

    private void toggle() {
        final boolean currentSelect = ringtoneImage.isActivated();
        if (currentSelect) {
            mute();
        } else {
            muteOff();
        }
    }

    private void mute() {
        ringtoneImage.setEnabled(true);
        ringtoneImage.setActivated(false);
        textureVideoView.mute();
    }

    private void muteOff() {
        ringtoneImage.setEnabled(true);
        ringtoneImage.setActivated(true);
        textureVideoView.resumeVolume();
    }

    @Override
    public void onBackPressed() {
        if (audioSeletorVisible) {
            hideAudioSelector();
        } else {
            super.onBackPressed();
        }
    }
}

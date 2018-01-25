package com.honeycomb.colorphone.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.VideoView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.AvatarAutoPilotUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.app.utils.HSMarketUtils;

/**
 * Created by zhewang on 23/01/2018.
 */

public class AvatarVideoActivity extends HSAppCompatActivity {
    public static String TAG = AvatarVideoActivity.class.getSimpleName();

    private static String CAMERA_PATH = "android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.camera;
    private static String HEAD_PATH = "android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.head;
    private static String ZMOJI_PATH = "android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.zmoji;

    private VideoView video;
    private String videoPath;
    private String pkgName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.avatar_video_activity);

        video = findViewById(R.id.avatar_video);

        initVideoData();
        startVideo();

        AvatarAutoPilotUtils.logAvatarViewShown();

    }

    private void initVideoData() {
        String type = AvatarAutoPilotUtils.getAvatarType();
        switch (type) {
            case AvatarAutoPilotUtils.CAMERA_NAME:
                videoPath = CAMERA_PATH;
                pkgName = AvatarAutoPilotUtils.CAMERA_PKG_NAME;
                break;
            case AvatarAutoPilotUtils.HEAD_NAME:
                videoPath = HEAD_PATH;
                pkgName = AvatarAutoPilotUtils.HEAD_PKG_NAME;
                break;
            case AvatarAutoPilotUtils.ZMOJI_NAME:
                videoPath = ZMOJI_PATH;
                pkgName = AvatarAutoPilotUtils.ZMOJI_PKG_NAME;
                break;
            default:
                finish();
                break;
        }
    }

    private void startVideo() {
        if (!TextUtils.isEmpty(videoPath)) {
            try {
                video.setVideoURI(Uri.parse(videoPath));
                video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        mp.setLooping(true);
                    }
                });
                video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override public void onCompletion(MediaPlayer mp) {
                        video.setVideoURI(Uri.parse(videoPath));
                        video.start();
                    }
                });
                video.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onBack(View view) {
        AvatarAutoPilotUtils.logAvatarViewBackButtonClicked();
        video.stopPlayback();
        finish();
    }

    public void onInstall(View view) {
        if (!TextUtils.isEmpty(pkgName)) {
            AvatarAutoPilotUtils.logAvatarViewInstallButtonClicked();
            HSMarketUtils.browseAPP(pkgName);
            finish();
        }
    }
}

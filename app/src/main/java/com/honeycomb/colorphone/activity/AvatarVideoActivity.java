package com.honeycomb.colorphone.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.AvatarAutoPilotUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.app.utils.HSMarketUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhewang on 23/01/2018.
 */

public class AvatarVideoActivity extends HSAppCompatActivity {
    public static String TAG = AvatarVideoActivity.class.getSimpleName();

    private static String VIDEO_ROOT_PATH = "android.resource://" + HSApplication.getContext().getPackageName() + "/";

    private static String HEAD_PATH = VIDEO_ROOT_PATH + R.raw.facemoji;
    private static Map<String, Integer> videoPathMap = new HashMap<>();
    static {
        videoPathMap.put("happy01_face", R.raw.happy01_face);
        videoPathMap.put("happy01_gif", R.raw.happy01_gif);
        videoPathMap.put("happy02_face", R.raw.happy02_face);
        videoPathMap.put("happy02_gif", R.raw.happy02_gif);
        videoPathMap.put("pick01_face", R.raw.pick01_face);
        videoPathMap.put("pick01_face_meme", R.raw.pick01_face_meme);
        videoPathMap.put("pick01_gif", R.raw.pick01_gif);
        videoPathMap.put("pick01_gif_meme", R.raw.pick01_gif_meme);
        videoPathMap.put("pick02_face", R.raw.pick02_face);
        videoPathMap.put("pick02_face_meme", R.raw.pick02_face_meme);
        videoPathMap.put("pick02_gif", R.raw.pick02_gif);
        videoPathMap.put("pick02_gif_meme", R.raw.pick02_gif_meme);
    }

    private VideoView video;
    private String videoPath;
    private String pkgName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.avatar_video_activity1);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        video = findViewById(R.id.avatar_video);

        TextView titleTv = findViewById(R.id.avatar_title);
        TextView button = findViewById(R.id.avatar_button_confirm);
        titleTv.setText(Ap.Avatar.getAvatarTitleString());
        button.setText(Ap.Avatar.getButtonTextString());

        initVideoData();

        AvatarAutoPilotUtils.logAvatarViewShown();

    }

    private void initVideoData() {
        String type = AvatarAutoPilotUtils.getAvatarType();
        Integer pathId = videoPathMap.get(type);
        if (pathId != null) {
            videoPath = VIDEO_ROOT_PATH + (int) pathId;
            pkgName = AvatarAutoPilotUtils.CAMERA_PKG_NAME;
        } else {
            finish();
        }

    }

    @Override protected void onResume() {
        super.onResume();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override public void run() {
                startVideo();
            }
        }, 400);
    }

    @Override protected void onPause() {
        super.onPause();
        video.stopPlayback();
    }

    private void startVideo() {
        if (!TextUtils.isEmpty(videoPath) && !video.isPlaying()) {
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
            video.stopPlayback();
            finish();
        }
    }
}

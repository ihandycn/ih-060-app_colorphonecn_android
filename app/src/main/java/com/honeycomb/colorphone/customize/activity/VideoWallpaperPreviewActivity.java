package com.honeycomb.colorphone.customize.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseAppCompatActivity;
import com.honeycomb.colorphone.customize.view.TextureVideoView;

/**
 * @author sundxing
 */
public class VideoWallpaperPreviewActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private TextureVideoView textureVideoView;
    private View ringtoneImage;

    public static void start(Context context, String path, boolean hasAudio) {
        Intent starter = new Intent(context, VideoWallpaperPreviewActivity.class);
        starter.putExtra("path", path);
        starter.putExtra("audio", hasAudio);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra("path");
        boolean hasAudio = getIntent().getBooleanExtra("audio", false);

        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_wallpaper_preview_video);
        ringtoneImage = findViewById(R.id.ringtone_image);
        ringtoneImage.setVisibility(hasAudio ? View.VISIBLE : View.GONE);
        ringtoneImage.setOnClickListener(this);
        findViewById(R.id.nav_back).setOnClickListener(this);
        findViewById(R.id.set_wallpaper_button).setOnClickListener(this);

        textureVideoView = findViewById(R.id.video_view);
        textureVideoView.setVideoPath(path);
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
                onSetWallpaper();
                break;
            case R.id.ringtone_image:

                break;
            default:
                break;
        }
    }

    private void onSetWallpaper() {

    }
}

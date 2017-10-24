package com.honeycomb.colorphone.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.WelcomeVideoView;
import com.ihs.app.framework.activity.HSActivity;

import java.io.IOException;

public class WelcomeActivity extends HSActivity {


    private WelcomeVideoView mVidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition fade = new Fade();
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setExitTransition(fade);
            getWindow().setEnterTransition(fade);
        }

        setContentView(R.layout.activity_welcome);
        mVidView = (WelcomeVideoView) findViewById(R.id.video_frame);
        mVidView.setPlayEndListener(new WelcomeVideoView.PlayEndListener() {
            @Override
            public void onEnd() {
                toMainView();
            }
        });
        showVideo(mVidView);
    }

    private void toMainView() {
        startActivity(new Intent(WelcomeActivity.this, ColorPhoneActivity.class));
    }

    @Override
    protected void onDestroy() {
        mVidView.stop();
        super.onDestroy();
    }

    private void showVideo(WelcomeVideoView playerViewTest) {
        AssetManager assetManager = getAssets();
        try {
            playerViewTest.setAssetFile(assetManager.openFd("welcome.mp4"));
            playerViewTest.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

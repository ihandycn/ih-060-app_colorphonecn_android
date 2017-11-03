package com.honeycomb.colorphone.activity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.WelcomeVideoView;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.app.utils.HSVersionControlUtils;

import java.io.IOException;

public class WelcomeActivity extends HSActivity {

    private WelcomeVideoView mVidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition fade = new Fade();
            fade.excludeTarget(android.R.id.statusBarBackground, true);
            fade.excludeTarget(android.R.id.navigationBarBackground, true);
            getWindow().setExitTransition(fade);
            getWindow().setEnterTransition(fade);
        }

        setContentView(R.layout.activity_welcome);
        mVidView = (WelcomeVideoView) findViewById(R.id.welcome_video);
        View cover = findViewById(R.id.welcome_cover);

        if (HSVersionControlUtils.isFirstSessionSinceInstallation()) {
            mVidView.setCover(cover);
            mVidView.setPlayEndListener(new WelcomeVideoView.PlayEndListener() {
                @Override
                public void onEnd() {
                    toMainView();
                }
            });
            showVideo(mVidView);
        } else {
            cover.setBackgroundResource(R.drawable.page_start_bg);
            toMainView();
        }
    }

    private void toMainView() {
        mVidView.destroy();
        startActivity(new Intent(WelcomeActivity.this, ColorPhoneActivity.class));
    }

    @Override
    protected void onDestroy() {
        mVidView.destroy();
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

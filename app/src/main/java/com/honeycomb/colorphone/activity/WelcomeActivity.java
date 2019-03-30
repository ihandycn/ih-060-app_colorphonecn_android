package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.WelcomeVideoView;
import com.superapps.util.rom.RomUtils;

import java.io.IOException;

public class WelcomeActivity extends Activity {

    private WelcomeVideoView mVidView;
    private static boolean coldLaunch = true;

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

        if (RomUtils.checkIsHuaweiRom() || RomUtils.checkIsMiuiRom()) {
            setContentView(R.layout.activity_welcome);
            mVidView = findViewById(R.id.welcome_video);
            View cover = findViewById(R.id.welcome_cover);

            if (coldLaunch) {
                mVidView.setCover(cover);
                mVidView.setPlayEndListener(() -> toMainView());
                showVideo(mVidView);
                coldLaunch = false;
            } else {
                cover.setBackgroundResource(R.drawable.page_start_bg);
                toMainView();
            }
        } else {
            toMainView();
        }

    }

    private void toMainView() {
        if (mVidView != null) {
            mVidView.destroy();
        }

        finish();
        startActivity(new Intent(WelcomeActivity.this, ColorPhoneActivity.class));
    }

    @Override
    protected void onDestroy() {
        if (mVidView != null) {
            mVidView.destroy();
        }
        super.onDestroy();
    }

    /**
     * Main activity may use MediaPlayer to play video, we release it here.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mVidView != null) {
            mVidView.destroy();
        }
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

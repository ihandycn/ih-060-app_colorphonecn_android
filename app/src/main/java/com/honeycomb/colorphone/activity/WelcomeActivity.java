package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.view.WelcomeVideoView;

import java.io.IOException;

public class WelcomeActivity extends Activity {

    private WelcomeVideoView mVidView;
    private static boolean coldLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Transition fade = new Fade();
//            fade.excludeTarget(android.R.id.statusBarBackground, true);
//            fade.excludeTarget(android.R.id.navigationBarBackground, true);
//            getWindow().setExitTransition(fade);
//            getWindow().setEnterTransition(fade);
//        }

        setContentView(R.layout.activity_welcome);
        mVidView = (WelcomeVideoView) findViewById(R.id.welcome_video);
        View cover = findViewById(R.id.welcome_cover);

        if (coldLaunch) {
            mVidView.setCover(cover);
            mVidView.setPlayEndListener(new WelcomeVideoView.PlayEndListener() {
                @Override
                public void onEnd() {
                    toMainView();
                }
            });
            showVideo(mVidView);
            coldLaunch = false;
        } else {
            cover.setBackgroundResource(R.drawable.page_start_bg);
            toMainView();
        }
    }

    private void toMainView() {
        mVidView.destroy();
        if (ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_GUIDE_START)
                && !GuideAllFeaturesActivity.isStarted()
                && !ModuleUtils.isAllModuleEnabled()) {

            startActivities(new Intent[] {
                    new Intent(WelcomeActivity.this, ColorPhoneActivity.class),
                    new Intent(this, GuideAllFeaturesActivity.class)});
        } else {
            startActivity(new Intent(WelcomeActivity.this, ColorPhoneActivity.class));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        mVidView.destroy();
        super.onDestroy();
    }

    /**
     * Main activity may use MediaPlayer to play video, we release it here.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mVidView.stop();
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

package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.view.WelcomeVideoView;
import com.ihs.app.alerts.HSAlertMgr;
import com.superapps.util.rom.RomUtils;

import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class WelcomeActivity extends Activity {

    private WelcomeVideoView mVidView;
    private static boolean coldLaunch = true;
    private boolean mediaFinished;

    private boolean isAgreePrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (!ChargingScreenUtils.isNativeLollipop()) {
            window.addFlags(FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(FLAG_TRANSLUCENT_STATUS);
            window.addFlags(FLAG_TRANSLUCENT_NAVIGATION);
        }

        if (needShowPermissionGuideActivity()) {
            toMainView();
            return;
        }

        boolean playSuccess = false;
        if (RomUtils.checkIsHuaweiRom() || RomUtils.checkIsMiuiRom()) {
            setContentView(R.layout.activity_welcome);
            View cover = findViewById(R.id.welcome_cover);
            mVidView = findViewById(R.id.welcome_video);

            if (coldLaunch) {
                mVidView.setCover(cover);
                mVidView.setPlayEndListener(() -> toMainView());
                playSuccess = showVideo(mVidView);

                coldLaunch = false;
            } else {
                cover.setBackgroundResource(R.drawable.page_start_bg);
            }
        }

        if (!playSuccess) {
            toMainView();
        }

    }

    private void showAccessibilityPermissionPage() {
        View view = findViewById(R.id.start_guide_function_page);
        view.setVisibility(View.GONE);

        view = findViewById(R.id.start_guide_permission_page);
        view.setVisibility(View.VISIBLE);

//        loadingForPermission();
    }

    private void toMainView() {
        if (mVidView != null) {
            mVidView.destroy();
        }

        launchMainActivityWithGuide();

        finish();
    }

    private boolean needShowPermissionGuideActivity() {
        boolean needShowGuidePermissionActivity =
                !StartGuideActivity.isStarted()
                        && (!AutoRequestManager.getInstance().isGrantAllPermission());
        return needShowGuidePermissionActivity;
    }

    public void launchMainActivityWithGuide() {
        Intent guideIntent = null;
        // Huawei & Xiaomi use auto permission guide window.

        if (needShowPermissionGuideActivity()) {
            guideIntent = StartGuideActivity.getIntent(WelcomeActivity.this, StartGuideActivity.FROM_KEY_GUIDE);
            HSAlertMgr.delayRateAlert();
        }

        Intent mainIntent = new Intent(WelcomeActivity.this, ColorPhoneActivity.class);
        if (guideIntent != null) {
            startActivities(new Intent[]{mainIntent, guideIntent});
        } else {
            startActivity(mainIntent);
        }

    }

    @Override
    protected void onDestroy() {
        if (mVidView != null) {
            mVidView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaFinished) {
            toMainView();
        }
    }

    /**
     * Main activity may use MediaPlayer to play video, we release it here.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mVidView != null) {
            mVidView.destroy();
            mediaFinished = true;
        }
    }

    private boolean showVideo(WelcomeVideoView playerViewTest) {
        AssetManager assetManager = getAssets();
        try {
            playerViewTest.setAssetFile(assetManager.openFd("welcome.mp4"));
            playerViewTest.play();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

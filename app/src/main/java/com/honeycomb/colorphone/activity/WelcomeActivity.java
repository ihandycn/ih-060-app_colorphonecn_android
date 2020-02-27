package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.WelcomeVideoView;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;
import com.superapps.util.rom.RomUtils;

import java.io.IOException;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

public class WelcomeActivity extends Activity {
    private static final String PREF_USER_IS_AGREE_PRIVACY = "pref_user_is_agree_privacy";
    private WelcomeVideoView mVidView;
    private static boolean coldLaunch = true;
    private boolean mediaFinished;
    private View privacyRootView;
    private boolean shouldShieldBackKey = false;

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


        if (RomUtils.checkIsHuaweiRom() || RomUtils.checkIsMiuiRom() || RomUtils.checkIsOppoRom()) {
            setContentView(R.layout.activity_welcome);
            mVidView = findViewById(R.id.welcome_video);
            View cover = findViewById(R.id.welcome_cover);

            if (coldLaunch) {
                mVidView.setCover(cover);
                mVidView.setPlayEndListener(() -> tryToShowPrivacy());
                boolean playSuccess = showVideo(mVidView);
                if (!playSuccess) {
                    tryToShowPrivacy();
                }
                coldLaunch = false;
            } else {
                cover.setBackgroundResource(R.drawable.page_start_bg);
                tryToShowPrivacy();
            }
        } else {
            tryToShowPrivacy();
        }

    }

    private void tryToShowPrivacy() {
        if (!canShowPrivacy() || isAgreePrivacy()) {
            toMainView();
            return;
        }
        initPrivacyView();
    }

    private boolean canShowPrivacy() {
        int currentVersionCode = HSApplication.getCurrentLaunchInfo().appVersionCode;
        return (currentVersionCode > 1044 || (currentVersionCode < 1000 && currentVersionCode > 170));
    }

    private void initPrivacyView() {
        shouldShieldBackKey = true;
        setContentView(R.layout.activity_privacy);
        privacyRootView = findViewById(R.id.privacy_root_view);

        View disagreeBtn = findViewById(R.id.button_disagree);
        disagreeBtn.setOnClickListener((view -> {
            Analytics.logEvent("Agreement_No_Click", false);
            Toasts.showToast(R.string.privacy_force_agree_text);
        }));
        View agreeBtn = findViewById(R.id.button_agree);
        agreeBtn.setOnClickListener(v -> onButtonAgreeClick());

        TextView textViewWithLink = findViewById(R.id.privacy_content_part5_with_link);
        textViewWithLink.setMovementMethod(LinkMovementMethod.getInstance());
        textViewWithLink.setText(getClickableSpan());

        Analytics.logEvent("Agreement_Show", false);
    }

    public void onButtonAgreeClick() {
        shouldShieldBackKey = false;
        Analytics.logEvent("Agreement_Click", false);
        agreePrivacy();
        toMainView();
    }

    private void toMainView() {
        if (mVidView != null) {
            mVidView.destroy();
        }

        launchMainActivityWithGuide();

        finish();
    }

    public void launchMainActivityWithGuide() {
        Intent guideIntent = null;
        // Huawei & Xiaomi use auto permission guide window.

        boolean needShowGuidePermissionActivity =
                !StartGuideActivity.isStarted()
                        && (!AutoRequestManager.getInstance().isGrantAllPermission());
        HSLog.i("AutoPermission", "started: " + !StartGuideActivity.isStarted() + "  AllP: " + !AutoRequestManager.getInstance().isGrantAllPermission());
        if (needShowGuidePermissionActivity) {
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
            tryToShowPrivacy();
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

    public boolean isAgreePrivacy() {
        return Preferences.getDefault().getBoolean(PREF_USER_IS_AGREE_PRIVACY, false);
    }

    public void agreePrivacy() {
        Preferences.getDefault().putBoolean(PREF_USER_IS_AGREE_PRIVACY, true);
    }

    private SpannableString getClickableSpan() {

        String originalString = getString(R.string.privacy_content_part5);
        SpannableString spanStr = new SpannableString(originalString);

        int firstSpanStart = originalString.indexOf("《");
        int firstSpanEnd = originalString.indexOf("》") + 1;

        int secondSpanStart = originalString.indexOf("《", firstSpanEnd);
        int secondSpanEnd = originalString.indexOf("》", firstSpanEnd) + 1;


        spanStr.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Analytics.logEvent("Agreement_Privacy_Click", false);
                WebLoadActivity.start(WelcomeActivity.this, Constants.getUrlTermServices());
            }
        }, firstSpanStart, firstSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(Color.parseColor("#337bff")), firstSpanStart, firstSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spanStr.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Analytics.logEvent("Agreement_Privacy_Click", false);
                WebLoadActivity.start(WelcomeActivity.this, Constants.getUrlPrivacy());
            }
        }, secondSpanStart, secondSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new ForegroundColorSpan(Color.parseColor("#337bff")), secondSpanStart, secondSpanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanStr;
    }

    @Override
    public void onBackPressed() {
        if (shouldShieldBackKey) {
            return;
        }
        super.onBackPressed();
    }
}

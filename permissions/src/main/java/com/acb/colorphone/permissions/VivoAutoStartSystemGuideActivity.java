package com.acb.colorphone.permissions;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;

public class VivoAutoStartSystemGuideActivity extends AppCompatActivity {

    private boolean playAnimation = true;
    private HomeKeyWatcher homeKeyWatcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acb_phone_auto_start_guide_lottie);
        View content = findViewById(R.id.container_view);
        if (content != null) {
            content.setBackgroundDrawable(null);
        }
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER_VERTICAL);
        initRes();

        callback();

        homeKeyWatcher = new HomeKeyWatcher(this);
        homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                finish();
            }

            @Override
            public void onRecentsPressed() {
                finish();
            }
        });
        homeKeyWatcher.startWatch();
    }

    private void callback() {
        new Handler().postDelayed(() -> {
            try {
                ActivityManager am = ((ActivityManager) VivoAutoStartSystemGuideActivity.this.getSystemService(Context.ACTIVITY_SERVICE));
                am.moveTaskToFront(VivoAutoStartSystemGuideActivity.this.getTaskId(), 0);
            } catch (Exception localException) {
                localException.printStackTrace();
            }
        }, 900L);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void initRes() {
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_anim);
        lottieAnimationView.useHardwareAcceleration();
        if (!TextUtils.isEmpty(getImageAssetFolder())) {
            lottieAnimationView.setImageAssetsFolder(getImageAssetFolder());
        }
        if (!TextUtils.isEmpty(getAnimationFromJson())) {
            lottieAnimationView.setAnimation(getAnimationFromJson());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            lottieAnimationView.enableMergePathsForKitKatAndAbove(true);
        }

//        setDescText();

        View action = findViewById(R.id.action_btn);
        action.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFF448AFF, Dimensions.pxFromDp(21.35f), true));
        action.setOnClickListener(v -> finish());

        View view = findViewById(R.id.content_view);
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFFFFFFF, 0x0, 0, 0, Dimensions.pxFromDp(12), Dimensions.pxFromDp(12), false, false));
        view = findViewById(R.id.lottie_anim_bg);
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFF5EB4ED, 0x0, Dimensions.pxFromDp(12), Dimensions.pxFromDp(12), 0, 0, false, false));

        if (playAnimation) {
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showExitStableToast();
        homeKeyWatcher.stopWatch();
    }

    protected void showExitStableToast() {
        int yOffset = Dimensions.pxFromDp(0);
        StableToast.showStableToast(R.layout.toast_one_line_text, getTitleStringResId(), yOffset, "AutoStartPageDuration");
    }

    protected int getTitleStringResId() {
        return R.string.acb_phone_grant_autostart_sys_vivo;
    }

    protected String getImageAssetFolder() {
        return "lottie/vivo_autostart_system_guide/images/";
    }

    protected String getAnimationFromJson() {
        return "lottie/vivo_autostart_system_guide/data.json";
    }


}

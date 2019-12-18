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

public abstract class LottiePermissionGuideActivity extends AppCompatActivity {

    private boolean playAnimation = true;
    private HomeKeyWatcher homeKeyWatcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acb_phone_permission_guide_lottie);
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
            @Override public void onHomePressed() {
                finish();
            }

            @Override public void onRecentsPressed() {
                finish();
            }
        });
        homeKeyWatcher.startWatch();
    }

    private void callback() {
        new Handler().postDelayed(() -> {
            try {
                ActivityManager am = ((ActivityManager) LottiePermissionGuideActivity.this.getSystemService(Context.ACTIVITY_SERVICE));
                am.moveTaskToFront(LottiePermissionGuideActivity.this.getTaskId(), 0);
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

        setDescText();

        View action = findViewById(R.id.action_btn);
        action.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFF448AFF, Dimensions.pxFromDp(6), true));
        action.setOnClickListener(v -> finish());

        View close = findViewById(R.id.close_btn);
        close.setOnClickListener(v -> finish());

        View view = findViewById(R.id.content_view);
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFFFFFFFF, 0x0, 0, 0, Dimensions.pxFromDp(6), Dimensions.pxFromDp(6), false, false));
        view = findViewById(R.id.lottie_anim_bg);
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xFF5EB4ED, 0x0, Dimensions.pxFromDp(6), Dimensions.pxFromDp(6), 0, 0, false, false));

        if (playAnimation) {
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        showExitStableToast();
        homeKeyWatcher.stopWatch();
    }

    protected void showExitStableToast() {
        StableToast.showStableToast(getTitleStringResId());
    }

    protected abstract @StringRes int getTitleStringResId();
    protected abstract String getImageAssetFolder();
    protected abstract String getAnimationFromJson();

    private void setDescText() {
        String descText = getString(getTitleStringResId());
        String icon_replace = getString(R.string.acb_app_icon_replace);
        TextView descTv = findViewById(R.id.description);

        if (descText.contains(icon_replace)) {
            int appIconIndex = descText.indexOf(icon_replace);
            if (appIconIndex >= 0) {
                int identifier = HSApplication.getContext().getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());
                Drawable appIcon = ContextCompat.getDrawable(HSApplication.getContext(), identifier);
                if (appIcon != null) {
                    SpannableString highlighted = new SpannableString(descText);

                    int size = Dimensions.pxFromDp(24);
                    appIcon.setBounds(0, 0, size, size);
                    ImageSpan span = new ImageSpan(appIcon, ImageSpan.ALIGN_BOTTOM);
                    highlighted.setSpan(span, appIconIndex, appIconIndex + icon_replace.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    descTv.setText(highlighted);
                    return;
                }
            }

        }
        descTv.setText(descText);
    }
}

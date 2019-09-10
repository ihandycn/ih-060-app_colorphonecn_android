package com.acb.colorphone.permissions;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;


public abstract class LottiePermissionGuideActivity extends AppCompatActivity {

    private boolean playAnimation = true;

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

        String hintTxt = getString(getTitleStringResId());
        setDescText(hintTxt);

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
    }

    protected void showExitStableToast() {
        StableToast.showStableToast(getTitleStringResId());
    }

    protected abstract int getTitleStringResId();
    protected abstract String getImageAssetFolder();
    protected abstract String getAnimationFromJson();

    private void setDescText(String descText) {
        TextView descTv = findViewById(R.id.description);
        descTv.setText(descText);
    }
}

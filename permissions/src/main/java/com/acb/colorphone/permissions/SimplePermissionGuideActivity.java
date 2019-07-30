package com.acb.colorphone.permissions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;


public abstract class SimplePermissionGuideActivity extends AppCompatActivity {

    private boolean playAnimatation = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acb_phone_permission_guide_text);
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
        finish();
        return super.onTouchEvent(event);
    }

    private void initRes() {
        ImageView appIcon = findViewById(R.id.app_icon);
        appIcon.setImageResource(R.drawable.app_icon);
        TextView appName = findViewById(R.id.app_name);
        appName.setText(R.string.app_name);
        String hintTxt = getString(getTitleStringResId());
        setDescText(hintTxt);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_anim);
        if (playAnimatation) {
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        } else {
            lottieAnimationView.setProgress(0.6f);
        }
    }

    protected abstract @StringRes int getTitleStringResId();

    private void setDescText(String descText) {
        TextView descTv = findViewById(R.id.description);
        descTv.setText(descText);
    }
}

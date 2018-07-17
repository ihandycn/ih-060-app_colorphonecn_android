package com.honeycomb.colorphone.permission;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;

public class OverlayGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acb_phone_notification_usage_access_center_tip);
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
        appIcon.setImageResource(R.mipmap.ic_launcher);
        TextView appName = findViewById(R.id.app_name);
        appName.setText(R.string.app_name);
        String hintTxt = getString(R.string.draw_overlay_window_hint);
        setDescText(hintTxt);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_anim);
        if (NotificationAutoPilotUtils.isNotificationAccessTipAnimated()) {
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        } else {
            lottieAnimationView.setProgress(0.6f);
        }
    }

    private void setDescText(String descText) {
        TextView descTv = findViewById(R.id.description);
        descTv.setText(descText);
    }
}

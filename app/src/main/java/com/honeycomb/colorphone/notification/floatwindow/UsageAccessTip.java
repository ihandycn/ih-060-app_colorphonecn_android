package com.honeycomb.colorphone.notification.floatwindow;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;

public class UsageAccessTip extends RelativeLayout {
    private WindowManager.LayoutParams layoutParams;

    public UsageAccessTip(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(getLayout(), this);
        initRes();
    }

    public void setParams(WindowManager.LayoutParams params) {
        layoutParams = params;
        setLayoutParams(layoutParams);
    }

    public void setDescText(String descText) {
        TextView descTv = findViewById(R.id.description);
        descTv.setText(descText);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_OUTSIDE:
                FloatWindowController.getInstance().removeUsageAccessTip();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        FloatWindowController.getInstance().removeUsageAccessTip();
        return super.dispatchKeyEvent(event);
    }

    private void initRes() {
        ImageView appIcon = findViewById(R.id.app_icon);
        appIcon.setImageResource(R.drawable.drawer_icon);

        TextView appName = findViewById(R.id.app_name);
        appName.setText(R.string.app_name);


        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_anim);
        if (NotificationAutoPilotUtils.isNotificationAccessTipAnimated()) {
            lottieAnimationView.playAnimation();
            lottieAnimationView.loop(true);
        } else {
            lottieAnimationView.setProgress(0.6f);
        }
    }

    private int getLayout() {
        if (NotificationAutoPilotUtils.isNotificationAccessTipAtBottom()) {
            return R.layout.acb_phone_notification_usage_access_bottom_tip;
        } else {
            return R.layout.acb_phone_notification_usage_access_center_tip;
        }
    }
}


package com.honeycomb.colorphone.dialer.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Chronometer;

import com.colorphone.lock.AnimatorListenerAdapter;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.FloatWindowMovableDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.dialer.InCallActivity;
import com.honeycomb.colorphone.util.Analytics;
import com.superapps.util.Dimensions;

public class CallFloatButton extends FloatWindowMovableDialog {

    private Chronometer mCallDurationView;
    private ValueAnimator mAnimator;
    private long startTimeMills;

    public CallFloatButton(Context context) {
        super(context);
        init();
    }

    public CallFloatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CallFloatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContentView = (ViewGroup) View.inflate(getContext(), R.layout.incall_float_button, this);
        mCallDurationView = mContentView.findViewById(R.id.call_chronometer);
        viewViewHeight = viewViewWidth = (int) (getResources().getDimensionPixelSize(R.dimen.call_button_height) * 1.2f);
        viewOriginalX = 0;
        mAnimator = ValueAnimator.ofFloat(1.0f, 1.1f).setDuration(800);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mContentView.setScaleX(value);
                mContentView.setScaleY(value);
                mContentView.invalidate();
            }
        });

    }

    public Chronometer getCallDurationView() {
        return mCallDurationView;
    }

    public void show() {
        Analytics.logEvent("Dialer_Icon_Show");
        FloatWindowManager.getInstance().showDialog(this);
        startTimeMills = System.currentTimeMillis();
    }

    @Override public void dismiss() {
        mAnimator.cancel();
        startTimeMills = 0;
        FloatWindowManager.getInstance().removeDialog(this);
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        mLayoutParams.type = getFloatWindowType();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.x = viewOriginalX;
        mLayoutParams.y = Dimensions.pxFromDp(200);
        mLayoutParams.width = viewViewWidth;
        mLayoutParams.height = viewViewHeight;

        this.setLayoutParams(mLayoutParams);
        return mLayoutParams;
    }

    private long clickTimeMills;
    @Override
    public void onClick() {
        String eventDuration = "None";
        long clickInterval = System.currentTimeMillis() - clickTimeMills;
        // Block clicks in 2 seconds.
        if (clickInterval < 2 * 1000) {
            return;
        }

        clickTimeMills = System.currentTimeMillis();
        if (startTimeMills > 0) {
            long duration = System.currentTimeMillis() - startTimeMills;
            if (duration < 4 * 1000) {
                eventDuration = "4s-";
            } else if (duration < 10 * 1000) {
                eventDuration = "4-10s";
            } else {
                eventDuration = "10s+";
            }
        }
        Analytics.logEvent("Dialer_Icon_Click", "Duration", eventDuration);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PendingIntent pendingIntent = createLaunchPendingIntent(true);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    private PendingIntent createLaunchPendingIntent(boolean isFullScreen) {
        Intent intent =
                InCallActivity.getIntent(
                        getContext(), false /* showDialpad */, false /* newOutgoingCall */, isFullScreen);

        // PendingIntent that can be used to launch the InCallActivity.  The
        // system fires off this intent if the user pulls down the windowshade
        // and clicks the notification's expanded view.  It's also used to
        // launch the InCallActivity immediately when when there's an incoming
        // call (see the "fullScreenIntent" field below).
        return PendingIntent.getActivity(getContext(), 1, intent, 0);
    }


    private void fadeOut() {
        mContentView.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dismiss();
                super.onAnimationEnd(animation);
            }
        }).start();
    }


    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimator.cancel();
        mAnimator.cancel();
    }
}

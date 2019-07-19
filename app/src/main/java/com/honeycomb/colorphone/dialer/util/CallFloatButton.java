package com.honeycomb.colorphone.dialer.util;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.colorphone.lock.AnimatorListenerAdapter;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.FloatWindowMovableDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.dialer.InCallActivity;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

public class CallFloatButton extends FloatWindowMovableDialog {

    private Chronometer mCallDurationView;

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
        viewViewHeight = viewViewWidth = getResources().getDimensionPixelSize(R.dimen.call_button_height);
        viewOriginalX = 0;
    }

    public Chronometer getCallDurationView() {
        return mCallDurationView;
    }

    public void show() {
        FloatWindowManager.getInstance().showDialog(this);
    }

    @Override public void dismiss() {
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


    @Override
    public void onClick() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent =
                    InCallActivity.getIntent(
                            getContext(), false /* showDialpad */,
                            false /* newOutgoingCall */, false);
            Navigations.startActivitySafely(getContext(), intent);

        }

        fadeOut();
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
}

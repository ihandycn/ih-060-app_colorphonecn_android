package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;

public abstract class BaseRateGuideDialog extends FullScreenDialog {

    public BaseRateGuideDialog(Context context) {
        this(context, null);
    }

    public BaseRateGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseRateGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
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
        mLayoutParams.gravity = Gravity.END | Gravity.BOTTOM;

        this.setLayoutParams(mLayoutParams);
        return mLayoutParams;
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return super.onTouchEvent(event);
    }

    protected abstract int getLayoutResId();

    @Override public abstract void onAddedToWindow(SafeWindowManager windowManager);
}

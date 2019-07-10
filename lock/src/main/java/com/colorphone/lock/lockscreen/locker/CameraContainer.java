package com.colorphone.lock.lockscreen.locker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class CameraContainer extends RelativeLayout {

    public CameraContainer(Context context) {
        this(context, null);
    }

    public CameraContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}

package com.honeycomb.colorphone.dialer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

class DialpadView  extends FrameLayout {
    public DialpadView(@NonNull Context context) {
        super(context);
    }

    public DialpadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DialpadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void animateShow() {
        // TODO
    }
}

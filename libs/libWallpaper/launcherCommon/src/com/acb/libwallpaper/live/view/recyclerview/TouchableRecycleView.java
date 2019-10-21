package com.acb.libwallpaper.live.view.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TouchableRecycleView extends RecyclerView {
    private boolean touchable = true;

    public TouchableRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !touchable || super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !touchable || super.onInterceptTouchEvent(event);
    }
}

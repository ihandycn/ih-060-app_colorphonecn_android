package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;


public class BannerScroller extends Scroller {
    private static final int DEFAULT_DURATION = 800;
    private int mDuration = DEFAULT_DURATION;

    public BannerScroller(Context context) {
        super(context);
    }

    public BannerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public BannerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }
}
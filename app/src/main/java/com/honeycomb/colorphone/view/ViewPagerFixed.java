package com.honeycomb.colorphone.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.honeycomb.colorphone.Ap;

/**
 * https://github.com/chrisbanes/PhotoView/issues/31
 */
public class ViewPagerFixed extends android.support.v4.view.ViewPager {


    private boolean isCanScroll = true;

    public ViewPagerFixed(Context context) {
        super(context);
    }

    public ViewPagerFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        isCanScroll = Ap.DetailAd.enableThemeSlide();
    }

    public void setCanScroll(boolean canScroll) {
        isCanScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (isCanScroll) {
                return super.onTouchEvent(ev);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            if (isCanScroll) {
                return super.onInterceptTouchEvent(ev);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isCanScroll) {
            super.scrollTo(x, y);
        }
    }
}
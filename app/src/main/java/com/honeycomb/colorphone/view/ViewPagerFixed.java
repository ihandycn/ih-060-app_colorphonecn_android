package com.honeycomb.colorphone.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * https://github.com/chrisbanes/PhotoView/issues/31
 */
public class ViewPagerFixed extends VerticalViewPager {


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
    }

    public void setCanScroll(boolean canScroll) {
        isCanScroll = canScroll;
    }

    public boolean isCanScroll() {
        return isCanScroll;
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
        super.scrollTo(x, y);
    }
}
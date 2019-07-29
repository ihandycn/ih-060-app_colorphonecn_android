package com.honeycomb.colorphone.customize.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class OnlineWallpaperTabLayout extends TabLayout {

    public OnScrollListener mOnScrollListener;
    private boolean mHasScrolledLeft;
    private boolean mHasScrolledRight;

    public interface OnScrollListener {
        void onScrollFinished(boolean isScrollLeft, boolean isScrollRight);
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mOnScrollListener = onScrollListener;
    }

    public OnlineWallpaperTabLayout(Context context) {
        this(context, null);
    }

    public OnlineWallpaperTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnlineWallpaperTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void scrollToRight() {
        this.post(() -> {
            if (getChildCount() > 0 && getChildAt(0) != null) {
                scrollBy(getChildAt(0).getWidth(), 0);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == KeyEvent.ACTION_UP && mOnScrollListener != null) {
            if (mHasScrolledLeft) {
                mOnScrollListener.onScrollFinished(true, false);
            } else if (mHasScrolledRight) {
                mOnScrollListener.onScrollFinished(false, true);
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (l > oldl) {
            mHasScrolledLeft = true;
            mHasScrolledRight = false;
        }
        if (l < oldl) {
            mHasScrolledLeft = false;
            mHasScrolledRight = true;
        }
    }
}

package com.colorphone.lock.lockscreen.locker;


import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.colorphone.lock.lockscreen.LockNotificationManager;


public class SlidingNotificationLayout extends RelativeLayout {


    private ViewDragHelper mViewDragHelper;

    int mDragOriLeft;
    int mDragOriTop;
    boolean mScrolling;
    private float touchDownX;
    private float touchDownY;


    public SlidingNotificationLayout(Context context) {
        this(context, null);
        initView();
    }

    public SlidingNotificationLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initView();
    }

    public SlidingNotificationLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();

    }

    private void initView() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
    }



    /**
     * 触摸事件相关
     * 拦截 or 处理/消费
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = ev.getX();
                touchDownY = ev.getY();
                mScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(touchDownX - ev.getX()) >= ViewConfiguration.get(getContext()).getScaledTouchSlop()||Math.abs(touchDownY - ev.getY()) >= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    mScrolling = true;
                } else {
                    mScrolling = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                mScrolling = false;
                break;
            default:
                break;
        }

        return mViewDragHelper.shouldInterceptTouchEvent(ev) && mScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        //何时开始检测触摸事件
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        //处理水平方向的滑动(也就是让其能够在水平方向进行滑动)
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            mDragOriLeft = capturedChild.getLeft();
            mDragOriTop = capturedChild.getTop();
        }


        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            final int childWidth = getWidth() / 2;
            float offset = (releasedChild.getLeft()) * 1.0f / childWidth;
            if (xvel > 0 && offset > 0.5f) {
                mViewDragHelper.settleCapturedViewAt(getWidth(), (int) mDragOriTop);
            } else if (xvel <= 0 && offset < -0.5f) {
                mViewDragHelper.settleCapturedViewAt(-getWidth(), (int) mDragOriTop);
            } else {
                mViewDragHelper.settleCapturedViewAt((int) mDragOriLeft, (int) mDragOriTop);
            }

            invalidate();
        }
    };

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}

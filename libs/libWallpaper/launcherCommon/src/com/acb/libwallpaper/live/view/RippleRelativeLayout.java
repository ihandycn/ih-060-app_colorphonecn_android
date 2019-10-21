package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.superapps.util.Threads;

import java.util.LinkedList;
import java.util.Queue;

public class RippleRelativeLayout extends RelativeLayout {

    private RippleView firstRippleView;
    private RippleView secondRippleView;
    private RippleView currentRippleView;
    public OnActionListener actionListener;
    private AnimEndRunnable animEndRunnable;

    private Queue<RippleView> queue = new LinkedList<>();

    public RippleRelativeLayout(Context context) {
        this(context, null);
    }

    public RippleRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        initView(context);
    }

    public void setColors(@ColorRes int middleColorRes, @ColorRes int edgeColorRes) {
        firstRippleView.setColors(middleColorRes, edgeColorRes);
        secondRippleView.setColors(middleColorRes, edgeColorRes);
    }

    public void resetStatus() {
        firstRippleView.resetStatus();
        secondRippleView.resetStatus();
    }

    public void setCouldRunUpAnim(boolean couldRunUpAnim) {
        firstRippleView.setCouldRunUpAnim(couldRunUpAnim);
        secondRippleView.setCouldRunUpAnim(couldRunUpAnim);
    }

    private void initView(Context context) {
        firstRippleView = new RippleView(context);
        secondRippleView = new RippleView(context);

        queue.add(firstRippleView);
        queue.add(secondRippleView);
    }

    public void setActionListener(OnActionListener actionListener) {
        this.actionListener = actionListener;
        animEndRunnable = new AnimEndRunnable(actionListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int pointerId = event.getPointerId(0);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event, pointerId);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                actionUp();
                break;
        }
        return super.onTouchEvent(event);
    }

    private float getPointerX(MotionEvent event, int pointerId) {
        int index = event.findPointerIndex(pointerId);
        if (index < 0) {
            return -1;
        }
        return event.getX(index);
    }

    private void actionDown(MotionEvent event, int pointerId) {
        if (!isClickable()) {
            return;
        }
        float downX = getPointerX(event, pointerId);
        if (downX != -1) {
            if (actionListener != null) {
                actionListener.actionDown();
            }
            if (animEndRunnable != null) {
                Threads.removeOnMainThread(animEndRunnable);
            }
            startPressedAnim(downX, getMeasuredWidth());
        }
    }

    private void actionUp() {
        if (actionListener != null) {
            actionListener.actionUpBeforeAnim();
        }
        if (currentRippleView != null) {
            if (animEndRunnable != null) {
                Threads.postOnMainThreadDelayed(animEndRunnable, 200);
            }
            currentRippleView.startUpAnim();
        }
    }

    private float getPointerY(MotionEvent event, int pointerId) {
        int index = event.findPointerIndex(pointerId);
        if (index < 0) {
            return -1;
        }
        return event.getY(index);
    }

    private void startPressedAnim(float downX, int width) {
        RippleView rippleView = queue.poll();
        if (indexOfChild(rippleView) != -1) {
            removeView(rippleView);
        }

        addRippleView(rippleView);
        rippleView.cancelAnim();
        rippleView.startDownAnim(downX, width);
        queue.add(rippleView);
        currentRippleView = rippleView;
    }

    private void addRippleView(View view) {
        int index = 0;
        if (getChildAt(0) instanceof RippleView) {
            index = 1;
        }
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, index, params);
    }

    public interface OnActionListener {
        void actionDown();

        void actionUpBeforeAnim();

        void actionUpAfterAnim();
    }

    private static class AnimEndRunnable implements Runnable {

        private OnActionListener actionListener;

        AnimEndRunnable(OnActionListener actionListener) {
            this.actionListener = actionListener;
        }

        @Override
        public void run() {
            if (actionListener != null) {
                actionListener.actionUpAfterAnim();
            }
        }
    }
}

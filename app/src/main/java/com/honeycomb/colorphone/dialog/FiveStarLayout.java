package com.honeycomb.colorphone.dialog;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class FiveStarLayout extends LinearLayout {

    private int mTouchSlop;
    private int mWidth;
    private float mLastDownX;
    private float mLastDownY;
    private float mLastX;
    private OnMoveListener mOnMoveListener;

    public FiveStarLayout(Context context) {
        this(context, null);
    }

    public FiveStarLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveStarLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mWidth = getWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                float downX = ev.getX();
                mLastDownX = downX;
                mLastX = downX;
                mLastDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                if (isMove(x, y)) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                if (isMove(moveX, moveY)) {
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onMove(isToRight(moveX), getPosition(moveX), getProgress(moveX));
                    }
                }
                mLastX = moveX;
                break;
            case MotionEvent.ACTION_UP:
                float upX = event.getX();
                float upY = event.getY();
                if (isMove(upX, upY)) {
                    if (mOnMoveListener != null) {
                        mOnMoveListener.onUp(getPosition(upX));
                    }
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean isMove(float x, float y) {
        return Math.abs(x - mLastDownX) > mTouchSlop && (Math.abs(x - mLastDownX) / Math.abs(y - mLastDownY) > 2);
    }

    private boolean isToRight(float x) {
        if (x >= mLastX) {
            return true;
        } else {
            return false;
        }
    }

    private int getPosition(float x) {
        int position;
        if (x <= mWidth / 5f) {
            position = 0;
        } else if (x <= 2 * mWidth / 5f) {
            position = 1;
        } else if (x <= 3 * mWidth / 5f) {
            position = 2;
        } else if (x <= 4 * mWidth / 5f) {
            position = 3;
        } else {
            position = 4;
        }
        return position;
    }

    private float getProgress(float x) {
        float progress;
        if (x <= 0) {
            progress = 0f;
        } else if (x > mWidth) {
            progress = 1.0f;
        } else {
            progress = x / (float) mWidth;
        }
        return progress;
    }

    public void setOnMoveListener(OnMoveListener listener) {
        mOnMoveListener = listener;
    }

    public interface OnMoveListener {
        void onMove(boolean isToRight, int position, float progress);

        void onUp(int position);
    }
}

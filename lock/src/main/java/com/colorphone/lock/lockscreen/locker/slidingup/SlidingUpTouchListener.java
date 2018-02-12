package com.colorphone.lock.lockscreen.locker.slidingup;

import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;

public class SlidingUpTouchListener implements View.OnTouchListener {

    public static final int TYPE_LEFT = 0;
    public static final int TYPE_RIGHT = 1;

    private int mType;

    private float mDownRawY;
    private float mLastRawY;
    private final int COMPLETE_UP_SLIDE_DURATION = 600;
    private final int DEFAULT_RETURN_ANIMATION_DURATION = 300;
    private int openAnimationDuration = COMPLETE_UP_SLIDE_DURATION;
    private float lastMoveDistance;
    private float mOffsetY;
    private int mPhoneHeight;
    private GestureDetector mGestureDetector;
    private int mNavigationBarHeight;
    private SlidingUpCallback mCallback;

    public SlidingUpTouchListener(int type, @NonNull SlidingUpCallback callback) {
        mType = type;
        mGestureDetector = new GestureDetector(HSApplication.getContext(), new GestureListener());
        mNavigationBarHeight = Dimensions.getNavigationBarHeight(HSApplication.getContext());
        mCallback = callback;
        mPhoneHeight = Dimensions.getPhoneHeight(HSApplication.getContext());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // fling gesture consume the event
        if (mGestureDetector.onTouchEvent(event)) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mCallback.onActionDown(mType);
                mDownRawY = event.getRawY();
                mLastRawY = event.getRawY();
                mOffsetY = Dimensions.pxFromDp(50) + mNavigationBarHeight;
                mCallback.doStartAnimator(-mOffsetY);
                break;

            case MotionEvent.ACTION_MOVE:
                int transY = (int) (event.getRawY() - mDownRawY);
                lastMoveDistance = event.getRawY() - mLastRawY;
                mLastRawY = event.getRawY();
                mCallback.translateY(transY);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float moveY = Math.abs(event.getRawY() - mDownRawY);
                if (moveY > (mPhoneHeight / 4) && !(lastMoveDistance >= 0)) {
                    openAnimationDuration = (int) (COMPLETE_UP_SLIDE_DURATION * (mPhoneHeight - moveY) / mPhoneHeight);
                    if (openAnimationDuration < 0) {
                        openAnimationDuration = 100;
                    }
                    mCallback.doSuccessAnimator(openAnimationDuration, mType);
                } else {
                    if (moveY > mOffsetY) {
                        mCallback.doAcceleratingEndAnimator((int) (DEFAULT_RETURN_ANIMATION_DURATION * moveY * 2 / mPhoneHeight));
                    } else {
                        mCallback.doEndAnimator();
                    }
                }
                mOffsetY = 0f;
                mDownRawY = 0.0f;
                mCallback.onActionUp();
                break;

            default:
                break;
        }
        return false;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private final int mMinFlingVelocity;

        public GestureListener() {
            ViewConfiguration vc = ViewConfiguration.get(HSApplication.getContext());
            mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 8;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (-velocityY > mMinFlingVelocity) {
                openAnimationDuration = (int) (COMPLETE_UP_SLIDE_DURATION * (mMinFlingVelocity / -velocityY));
                if (openAnimationDuration < 0) {
                    openAnimationDuration = 100;
                }
                if (openAnimationDuration < 100) {
                    openAnimationDuration = openAnimationDuration * 2;
                }
                mCallback.doSuccessAnimator(openAnimationDuration, mType);
                return true;
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}

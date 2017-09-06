package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.colorphone.lock.util.CommonUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SlidingFinishRelativeLayout extends RelativeLayout implements View.OnTouchListener {

    private static final String TAG = "SLIDING_FINISH_LAYOUT";

    private static final float COEFFICIENT_RIGHT_SLIDING_FINISH = 0.2f;
    private static final float COEFFICIENT_UP_SLIDING_FINISH = 0.333f;

    private static final int VELOCITY_TRACKER_UNIT = 1000;
    private static final int FAST_SCROLL_PIXEL_COUNT_EACH_UNIT_TIME = CommonUtils.pxFromDp(1166);
    private static final float RATIO_DURATION_TO_PIXEL_DISTANCE = 0.18f;

    /*卡片类型*/
    @IntDef({STATE_SLIDING_IDLE, STATE_SLIDING_UP, STATE_SLIDING_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SlidingState {
    }

    public static final int STATE_SLIDING_IDLE = 0;
    public static final int STATE_SLIDING_UP = 1;
    public static final int STATE_SLIDING_RIGHT = 2;

    public interface OnSlidingFinishListener {
        void onSlidingFinish(@SlidingState int slidingState);
    }

    private OnSlidingFinishListener slidingFinishListener;

    private Scroller scroller;
    private VelocityTracker velocityTracker;

    @SlidingState
    private int slidingState;

    private int downX;
    private int downY;
    private int lastMovingStartX;
    private int lastMovingStartY;

    private boolean isFinish;


    public SlidingFinishRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingFinishRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        scroller = new Scroller(context, new AccelerateInterpolator(), true);
        setOnTouchListener(this);
    }

    public void setSlidingFinishListener(OnSlidingFinishListener slidingFinishListener) {
        this.slidingFinishListener = slidingFinishListener;
    }


    private void scrollRight() {
        int delta = (getWidth() + getScrollX());

        scroller.startScroll(getScrollX(), 0, -delta + 1, 0,
            (int) (Math.abs(delta) * RATIO_DURATION_TO_PIXEL_DISTANCE));

        postInvalidate();
    }

    private void scrollUp() {
        int delta = (getHeight() - getScrollY());

        scroller.startScroll(0, getScrollY(), 0, delta - 1,
            (int) (Math.abs(delta) * RATIO_DURATION_TO_PIXEL_DISTANCE));

        postInvalidate();
    }

    private void scrollOrigin() {
        int deltaX = getScrollX();
        int deltaY = getScrollY();

        scroller.startScroll(getScrollX(), getScrollY(), -deltaX, -deltaY,
            Math.abs(deltaX) > Math.abs(deltaY) ? (int) (Math.abs(deltaX) * RATIO_DURATION_TO_PIXEL_DISTANCE)
                : (int) (Math.abs(deltaY) * RATIO_DURATION_TO_PIXEL_DISTANCE));

        postInvalidate();
    }

    @SlidingState
    private int judgeSlidingState(int downX, int downY, int moveX, int moveY) {

        if (moveX <= downX && moveY >= downY) {
            return STATE_SLIDING_IDLE;
        }

        if (moveX > downX && moveY < downY) {

            if ((moveX - downX) / COEFFICIENT_RIGHT_SLIDING_FINISH
                >= (downY - moveY) / COEFFICIENT_UP_SLIDING_FINISH) {

                return STATE_SLIDING_RIGHT;

            }

            return STATE_SLIDING_UP;
        }

        if (moveX > downX) {
            return STATE_SLIDING_RIGHT;
        }

        return STATE_SLIDING_UP;
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        if (isFinish) {
            return false;
        }

        /*if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }*/
        velocityTracker = VelocityTracker.obtain();

        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(VELOCITY_TRACKER_UNIT);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                slidingState = STATE_SLIDING_IDLE;
                downX = lastMovingStartX = (int) event.getX();
                downY = lastMovingStartY = (int) event.getY();

                break;

            case MotionEvent.ACTION_MOVE:

                int movingX = (int) event.getX();
                int movingY = (int) event.getY();

                int movingDistanceX = lastMovingStartX - movingX;
                int movingDistanceY = lastMovingStartY - movingY;

                lastMovingStartX = movingX;
                lastMovingStartY = movingY;

                int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (Math.abs(movingX - downX) < touchSlop && Math.abs(movingY - downY) < touchSlop) {
                    break;
                }

                // 若touchView是AbsListView，则当手指滑动，取消item的点击事件，不然我们滑动也伴随着item点击事件的发生
                if (view instanceof AbsListView) {
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                        | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    view.onTouchEvent(cancelEvent);
                }

                if (slidingState == STATE_SLIDING_IDLE) {
                    slidingState = judgeSlidingState(downX, downY, movingX, movingY);
                }

                switch (slidingState) {

                    case STATE_SLIDING_IDLE:
                        break;

                    case STATE_SLIDING_UP: {

                        if (velocityTracker.getYVelocity() < -FAST_SCROLL_PIXEL_COUNT_EACH_UNIT_TIME) {
                            isFinish = true;
                            scrollUp();
                            break;
                        }

                        if (getScrollY() + movingDistanceY >= 0) {
                            scrollBy(0, movingDistanceY);
                        }

                        break;
                    }

                    case STATE_SLIDING_RIGHT: {

                        if (velocityTracker.getXVelocity() > FAST_SCROLL_PIXEL_COUNT_EACH_UNIT_TIME) {
                            isFinish = true;
                            scrollRight();
                            break;
                        }

                        if (getScrollX() + movingDistanceX <= 0) {
                            scrollBy(movingDistanceX, 0);
                        }

                        break;
                    }

                    default:
                        break;
                }

                break;

            case MotionEvent.ACTION_UP:

                switch (slidingState) {

                    case STATE_SLIDING_RIGHT: {

                        if (getScrollX() < -getWidth() * COEFFICIENT_RIGHT_SLIDING_FINISH) {
                            isFinish = true;
                            scrollRight();
                            break;
                        }

                        break;
                    }

                    case STATE_SLIDING_UP: {

                        if (getScrollY() > getHeight() * COEFFICIENT_UP_SLIDING_FINISH) {
                            isFinish = true;
                            scrollUp();
                            break;
                        }

                        break;
                    }

                    case STATE_SLIDING_IDLE:

                        break;
                }

                velocityTracker.recycle();
                //velocityTracker = null;

                if (!isFinish) {
                    scrollOrigin();
                }

                break;

            default:
                if (!isFinish) {
                    scrollOrigin();
                }
                break;
        }

        return !(view instanceof ScrollView || view instanceof AbsListView)
            || view.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (!scroller.computeScrollOffset()) {
            return;
        }

        scrollTo(scroller.getCurrX(), scroller.getCurrY());
        invalidate();

        if (scroller.isFinished() && slidingFinishListener != null && isFinish) {
            slidingFinishListener.onSlidingFinish(slidingState);
        }
    }
}


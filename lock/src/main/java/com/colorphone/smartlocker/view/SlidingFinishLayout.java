package com.colorphone.smartlocker.view;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.colorphone.lock.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SlidingFinishLayout extends ConstraintLayout implements View.OnTouchListener {
    public interface OnSlidingFinishListener {
        void onSlidingFinish(@SlidingState int slidingState);
    }

    private static final String TAG = "SlidingFinishLayout";

    private static final float COEFFICIENT_RIGHT_SLIDING_FINISH = 0.4f;
    private static final float COEFFICIENT_UP_SLIDING_FINISH = 0.333f;

    private static final int VELOCITY_TRACKER_UNIT = 1000;
    private static final int FAST_SCROLL_PIXEL_COUNT_EACH_UNIT_TIME =
            HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.charging_screen_unlock_distance_per_second);
    private static final float RATIO_DURATION_TO_PIXEL_DISTANCE = 0.18f;

    /*卡片类型*/
    @IntDef({STATE_SLIDING_IDLE, STATE_SLIDING_UP, STATE_SLIDING_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SlidingState {
    }

    public static final int STATE_SLIDING_IDLE = 0;
    public static final int STATE_SLIDING_UP = 1;
    public static final int STATE_SLIDING_RIGHT = 2;

    private OnSlidingFinishListener slidingFinishListener;

    private Scroller scroller;
    private VelocityTracker velocityTracker;

    @SlidingState
    private int slidingState;

    private int downX, downY;
    private int lastMovingStartX, lastMovingStartY;

    private boolean isFinish;

    private boolean enableScrollUp = true;
    private boolean enableScrollRight = true;

    public SlidingFinishLayout(Context context) {
        super(context);
        init(context);
    }

    public SlidingFinishLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingFinishLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
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

    private int scrollPointerId = -1;
    private int initialTouchX = 0;
    private int initialTouchY = 0;
    private int touchSlop = 8;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e == null) {
            return false;
        }

        final int action = e.getAction();
        final int actionIndex = e.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                scrollPointerId = e.getPointerId(0);
                initialTouchX = (int) (e.getX() + 0.5f);
                initialTouchY = (int) (e.getY() + 0.5f);
                HSLog.d(TAG, "onInterceptTouchEvent: ACTION_DOWN scrollPointerId = " + scrollPointerId
                        + ", initialTouchX = " + initialTouchX + ", initialTouchY = " + initialTouchY);
                return super.onInterceptTouchEvent(e);

            case MotionEvent.ACTION_POINTER_DOWN:
                scrollPointerId = e.getPointerId(actionIndex);
                initialTouchX = (int) (e.getX(actionIndex) + 0.5f);
                initialTouchY = (int) (e.getY(actionIndex) + 0.5f);
                HSLog.d(TAG, "onInterceptTouchEvent: ACTION_POINTER_DOWN scrollPointerId = " + scrollPointerId
                        + ", initialTouchX = " + initialTouchX + ", initialTouchY = " + initialTouchY);
                return super.onInterceptTouchEvent(e);

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(scrollPointerId);
                if (index < 0) {
                    return false;
                }

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                HSLog.d(TAG, "onInterceptTouchEvent: ACTION_MOVE x = " + x + ", y = " + y);
                final int dx = x - initialTouchX;
                final int dy = y - initialTouchY;

                if (Math.abs(dy) > touchSlop && Math.abs(dx) >= Math.abs(dy)) {
                    HSLog.d(TAG, "onInterceptTouchEvent: ACTION_MOVE startScroll = true");
                    return true;
                }
                return super.onInterceptTouchEvent(e);
            }
            default:
                return super.onInterceptTouchEvent(e);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (isFinish) {
            return false;
        }

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
                    case STATE_SLIDING_UP: {
                        if (!enableScrollUp) {
                            break;
                        }
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
                        if (!enableScrollRight) {
                            break;
                        }
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

                    case STATE_SLIDING_IDLE:
                    default:
                        break;
                }
                break;

            case MotionEvent.ACTION_UP:
                switch (slidingState) {
                    case STATE_SLIDING_RIGHT: {
                        if (!enableScrollRight) {
                            break;
                        }
                        if (getScrollX() < -getWidth() * COEFFICIENT_RIGHT_SLIDING_FINISH) {
                            isFinish = true;
                            scrollRight();
                            break;
                        }
                        break;
                    }

                    case STATE_SLIDING_UP: {
                        if (!enableScrollUp) {
                            break;
                        }
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

    public void setEnableScrollUp(boolean enableScrollUp) {
        this.enableScrollUp = enableScrollUp;
    }

    public void setEnableScrollRight(boolean enableScrollRight) {
        this.enableScrollRight = enableScrollRight;
    }
}


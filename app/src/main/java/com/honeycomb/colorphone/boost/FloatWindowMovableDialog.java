package com.honeycomb.colorphone.boost;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;


public abstract class FloatWindowMovableDialog extends FloatWindowDialog {
    public abstract void onClick();

    private final static String TAG = FloatWindowMovableDialog.class.getSimpleName();
    private final int TOUCH_IGNORE = 10;
    protected ViewGroup mContentView;

    private static int statusBarHeight;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private float xInView;
    private float yInView;
    public static int viewX;
    public static int viewY;
    public static int viewOriginalX;
    public static int viewViewWidth;
    public static int viewViewHeight;
    private boolean isStop = true;

    private Point point = new Point();
    private Point oldPoint = new Point();

    public FloatWindowMovableDialog(Context context) {
        super(context);
    }

    public FloatWindowMovableDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatWindowMovableDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        statusBarHeight = Dimensions.getStatusBarHeight(getContext());
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isStop) {
            HSLog.d(TAG, "not onTouch");
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                isCancelAllAnimator = true;

                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - statusBarHeight;
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - statusBarHeight;
                HSLog.d(TAG, "ACTION_DOWN x == " + xDownInScreen + "  y == " + yDownInScreen);

//                actionDownStatus();
                break;
            case MotionEvent.ACTION_MOVE:
//                checkActionDownFinalStatus();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - statusBarHeight;
                updateViewPosition();

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                updateOnActionUpFloatStatue();

                if (isMisOperation()) {
                    onClick();
                }
                HSLog.d(TAG, "ACTION_UP " + "x == " + xInScreen + "  y == " + yInScreen);
                break;
            default:
                break;
        }
        return false;
    }

    private void updateViewPosition() {
        oldPoint.set(point.x, point.y);
        point.x = (int) (xInScreen - xInView);
        point.y = (int) (yInScreen - yInView);
//        if (listener != null) {
//            isRemoveBallView = listener.onPositionChange(point, isMisOperation(oldPoint, point));
//        }
        moveView(point.x, point.y);
    }

    private void updateOnActionUpFloatStatue() {
        if (!isMisOperation()) {
            startBallViewMoveToBorderAnim(() -> isStop = true);
        } else {
            isStop = true;
        }
    }

    private void startBallViewMoveToBorderAnim(final Runnable runnable) {
        final int borderX;
        if (mLayoutParams == null) {
            return;
        }
        if (mLayoutParams.x > ((Dimensions.getPhoneWidth(HSApplication.getContext()) - viewViewWidth) / 2f)) {
            borderX = viewOriginalX;
        } else {
            borderX = -Dimensions.pxFromDp(8);
        }

        int bottom = Dimensions.getPhoneHeight(getContext()) - Dimensions.pxFromDp(170);
        int top = Dimensions.getStatusBarHeight(getContext()) + Dimensions.pxFromDp(35);
        final int ballY = mLayoutParams.y;
        final int tranceY;

        if (ballY < top) {
            tranceY = top - ballY;
        } else if (ballY > bottom) {
            tranceY = bottom - ballY;
        } else {
            tranceY = 0;
        }

        ValueAnimator animator = ValueAnimator.ofInt(mLayoutParams.x, borderX);
        animator.setInterpolator(PathInterpolatorCompat.create(0.49f, 1.47f, 0.66f, 0.99f));
        animator.addUpdateListener(animation -> {
            int x = (int) animation.getAnimatedValue();
            int y = mLayoutParams.y;

            if (tranceY != 0) {
                y = (int) (ballY + tranceY * animation.getAnimatedFraction());
            }
            moveView(x, y);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        int x = Math.abs(mLayoutParams.x - borderX);
        animator.setDuration(getMoveToBorderAnimDuration(x));
        animator.start();
    }

    private long getMoveToBorderAnimDuration(int x) {
        return (long) (1.689f * Dimensions.dpFromPx(x)) + 200;
    }

    private void moveView(int x, int y) {
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        FloatWindowManager.getInstance().updateDialog(FloatWindowMovableDialog.this, mLayoutParams);
    }

    private boolean isMisOperation(Point p1, Point p2) {
        return (Math.abs(p1.x - p2.x) < TOUCH_IGNORE && Math.abs(p1.y - p2.y) < TOUCH_IGNORE);
    }

    private boolean isMisOperation() {
        return (Math.abs(xInScreen - xDownInScreen) < TOUCH_IGNORE && Math.abs(yInScreen - yDownInScreen) < TOUCH_IGNORE);
    }
}

package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class FlashCircleView extends View implements Runnable {

    public interface ViewListener {
        void onViewed();

        void onAnimationEnd();
    }

    private static final long DURATION_ENTER_ANIMATION = 752;

    private static final float DEFAULT_RING_BREADTH = 3f;

    private static final float ARC_SWEEP_MAX_ANGLE = 60f;
    private static final float ARC_START_ANGLE = -180f;

    private static final int COLOR_WHITE = 0xFFFFFFFF;

    private static final long INTERVAL = 16;
    private static final long TOTAL_COUNT = DURATION_ENTER_ANIMATION / INTERVAL;
    private int count;

    private RectF arcBound = new RectF();

    private float arcStartAngle;
    private float arcSweepAngle;
    private float radius;
    private boolean isOnDraw;
    private boolean isAnimationEnd;

    private Paint arcPaint;

    private ViewListener mViewListener;

    public FlashCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        count = 0;
        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(DEFAULT_RING_BREADTH);
        arcPaint.setColor(COLOR_WHITE);
        arcPaint.setAntiAlias(true);
        setWillNotDraw(false);
    }

    public void setViewListener(ViewListener viewListener) {
        mViewListener = viewListener;
    }

    public void startAnimation() {
        run();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getDefaultSize((int) (2 * radius), widthMeasureSpec);
        int height = getDefaultSize((int) (2 * radius), heightMeasureSpec);
        int maxRadius = Math.min(width, height);
        radius = (maxRadius - DEFAULT_RING_BREADTH) / 2;
        arcBound.set(DEFAULT_RING_BREADTH / 2, DEFAULT_RING_BREADTH / 2, maxRadius - DEFAULT_RING_BREADTH / 2, maxRadius - DEFAULT_RING_BREADTH / 2);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(arcBound, arcStartAngle, arcSweepAngle, false, arcPaint);
        if (!isOnDraw && null != mViewListener) {
            mViewListener.onViewed();
        }
        isOnDraw = true;
    }

    private void refreshFrame() {
        if (count >= TOTAL_COUNT) {
            isAnimationEnd = true;
            count = 0;
            if (null != mViewListener) {
                mViewListener.onAnimationEnd();
            }
            return;
        }
        float inputStart = (float) count / TOTAL_COUNT;
        if (inputStart < 0) {
            inputStart = 0;
        } else if(inputStart > 1) {
            inputStart = 1;
        }
        float fractionStart = getFraction(inputStart);

        int sweepCountLimit = (int) (TOTAL_COUNT * 0.75f);
        if (count < sweepCountLimit) {
            arcSweepAngle = ARC_SWEEP_MAX_ANGLE;
        } else {
            float inputSweep = (float) (count - sweepCountLimit) / TOTAL_COUNT;
            if (inputSweep < 0) {
                inputSweep = 0;
            } else if(inputSweep > 1) {
                inputSweep = 1;
            }
            arcSweepAngle = ARC_SWEEP_MAX_ANGLE * (1 - getFraction(inputSweep));
        }
        arcStartAngle = ARC_START_ANGLE + fractionStart * 360;
        count++;
    }


    @Override
    public void run() {
        invalidate();
        if (isAnimationEnd) {
            return;
        }
        refreshFrame();
        postDelayed(this, INTERVAL);
    }

    private float getFraction(float input) {
        return (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}

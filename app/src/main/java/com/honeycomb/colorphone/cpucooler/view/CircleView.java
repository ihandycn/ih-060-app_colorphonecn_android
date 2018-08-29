package com.honeycomb.colorphone.cpucooler.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

public class CircleView extends View {

    private static final boolean KITKAT_LOW = SDK_INT < KITKAT;

    private static final int TRANSPARENT_LIGHT = 0x1A;
    private static final float SCALE_FADE_OUT_EXTEND = 0.35f;

    private Paint mGaryRingPaint;
    private Paint mArcPaint;

    private float mRingRadius;
    private float mScaledRingRadius;
    private float mArcSweepAngle;
    private float mDefaultRingWidth;

    private RectF mArcBound = new RectF();

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDefaultRingWidth = getResources().getDimensionPixelSize(R.dimen.cpu_circle_stroke_width);
        int color = ContextCompat.getColor(context, R.color.cpu_cooler_primary_blue);

        mGaryRingPaint = new Paint();
        mGaryRingPaint.setStyle(Paint.Style.STROKE);
        mGaryRingPaint.setStrokeWidth(mDefaultRingWidth);
        mGaryRingPaint.setColor(Color.BLACK);
        mGaryRingPaint.setAlpha(TRANSPARENT_LIGHT);
        mGaryRingPaint.setAntiAlias(true);

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mDefaultRingWidth);
        mArcPaint.setColor(color);
        mArcPaint.setAntiAlias(true);
    }

    public void startAnimation(final long duration, final Runnable onAnimationEnd) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(animation -> {
            mArcSweepAngle = 360 * animation.getAnimatedFraction();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != onAnimationEnd) {
                    onAnimationEnd.run();
                }
            }
        });
        animator.setDuration(duration).start();
    }

    public void startFadeOutAnimation(final long duration, final Runnable onAnimationEnd) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            mScaledRingRadius = mRingRadius * (1 + fraction * SCALE_FADE_OUT_EXTEND);
            mArcBound.set(getWidth() / 2 - mScaledRingRadius, getHeight() / 2 - mScaledRingRadius, getWidth() / 2 + mScaledRingRadius, getHeight() / 2 + mScaledRingRadius);

            int alpha = (int) (255 * (1f - fraction));
            alpha = alpha > 235? alpha : (int) (alpha * 0.3f);

//            mGaryRingPaint.setStrokeWidth(mDefaultRingWidth * (1 - fraction));
            mGaryRingPaint.setAlpha(alpha);
//            mArcPaint.setStrokeWidth(mDefaultRingWidth * (1 - fraction));
            mArcPaint.setAlpha(alpha);

            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != onAnimationEnd) {
                    onAnimationEnd.run();
                }
            }
        });
        animator.setDuration(duration).start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getDefaultSize((int) (2 * mRingRadius), widthMeasureSpec);
        int height = getDefaultSize((int) (2 * mRingRadius), heightMeasureSpec);

        int maxRadius = Math.min(width, height);

        mRingRadius = maxRadius / 2 - mDefaultRingWidth / 2;
        mScaledRingRadius = mRingRadius;

        mArcBound.set(mDefaultRingWidth / 2, mDefaultRingWidth / 2, maxRadius - mDefaultRingWidth / 2, maxRadius - mDefaultRingWidth / 2);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mScaledRingRadius, mGaryRingPaint);

        canvas.drawArc(mArcBound, -90, mArcSweepAngle, false, mArcPaint);

        if (KITKAT_LOW) {
            ((View) getParent()).invalidate();
        }
    }

}

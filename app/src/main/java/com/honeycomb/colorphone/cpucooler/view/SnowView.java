package com.honeycomb.colorphone.cpucooler.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;

public class SnowView extends View {

    private static final float SCALE_CENTER_CIRCLE_RADIUS = 0.16f;
    private static final float SCALE_MAIN_BRANCH_LENGTH = 0.27f;
    private static final float SCALE_SUB_BRANCH_LENGTH = 0.14f;

    private static final float SCALE_SNOW_STROKE_WIDTH = 0.08f;

    private static final float SCALE_SUB_BRANCH_ON_MAIN = 0.5f;
    private static final float SCALE_CENTER_CIRCLE_ON_START = 0.7f;
    private static final int ROTATE_DEGREE = 138;

    private static final float ALPHA_IN_FRACTION = 0.03f;
    private static final float[] MAIN_BRANCH_START_GROW_FRACTION = {ALPHA_IN_FRACTION, 0.11f, 0.19f, 0.27f, 0.35f, 0.42f};
    private static final float[] MAIN_BRANCH_END_GROW_FRACTION = {0.11f, 0.19f, 0.27f, 0.35f, 0.43f, 0.5f};

    private Paint mCenterCirclePaint;
    private Paint mMainBranchPaint;
    private Paint mSubBranchPaint;
    private Paint mEndOralPaint;

    private float mSnowStrokeWidth;
    private int mSnowColor;
    private float mWidth;
    private float mHeight;
    private boolean mNeedGrow = false;
    private float[] mMainBranchStartX = new float[6];
    private float[] mMainBranchStartY = new float[6];
    private float[] mSubBranchStartX = new float[6];
    private float[] mSubBranchStartY = new float[6];

    private float mRotateDegree;
    private float mCenterCircleRadius;
    private float[] mMainBranchLength = new float[6];
    private float[] mSubBranchLength = new float[6];
    private float[] mMainBranchEndX = new float[6];
    private float[] mMainBranchEndY = new float[6];
    private float[] mLeftSubBranchEndX = new float[6];
    private float[] mLeftSubBranchEndY = new float[6];
    private float[] mRightSubBranchEndX = new float[6];
    private float[] mRightSubBranchEndY = new float[6];

    public SnowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSnowColor = getResources().getColor(R.color.cpu_cooler_primary_blue);
        initPaint();
    }

    public void setNeedGrow(boolean mNeedGrow) {
        this.mNeedGrow = mNeedGrow;
    }

    public void startRotateAnimation(final long duration, final Runnable onAnimationEnd) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float alpha = fraction > ALPHA_IN_FRACTION ? 1 : fraction / ALPHA_IN_FRACTION;

                calculateAnimatedValues(fraction);
                mCenterCirclePaint.setAlpha((int)(255 * alpha));
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(VISIBLE);
            }

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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        mSnowStrokeWidth = mWidth * SCALE_SNOW_STROKE_WIDTH;
        mCenterCirclePaint.setStrokeWidth(mSnowStrokeWidth);
        mMainBranchPaint.setStrokeWidth(mSnowStrokeWidth);
        mSubBranchPaint.setStrokeWidth(mSnowStrokeWidth);

        calculateStaticCoordinates();

        if (!mNeedGrow) {
            calculateAnimatedValues(0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(mRotateDegree, mWidth / 2, mHeight / 2);

        canvas.drawCircle(mWidth / 2, mHeight / 2, mCenterCircleRadius, mCenterCirclePaint); // 中心圆圈

        for (int i = 0;  i < 6; i ++) { // 六个分叉
            if (mMainBranchLength[i] > 0) {
                canvas.drawLine(mMainBranchStartX[i], mMainBranchStartY[i], mMainBranchEndX[i], mMainBranchEndY[i], mMainBranchPaint);
                canvas.drawCircle(mMainBranchEndX[i], mMainBranchEndY[i], mSnowStrokeWidth / 2, mEndOralPaint);
            }
            if (mSubBranchLength[i] > 0) {
                canvas.drawLine(mSubBranchStartX[i], mSubBranchStartY[i], mLeftSubBranchEndX[i], mLeftSubBranchEndY[i], mSubBranchPaint);
                canvas.drawCircle(mLeftSubBranchEndX[i], mLeftSubBranchEndY[i], mSnowStrokeWidth / 2, mEndOralPaint);
                canvas.drawLine(mSubBranchStartX[i], mSubBranchStartY[i], mRightSubBranchEndX[i], mRightSubBranchEndY[i], mSubBranchPaint);
                canvas.drawCircle(mRightSubBranchEndX[i], mRightSubBranchEndY[i], mSnowStrokeWidth / 2, mEndOralPaint);
            }
        }
    }

    private void initPaint() {
        mCenterCirclePaint = new Paint();
        mCenterCirclePaint.setColor(mSnowColor);
        mCenterCirclePaint.setStyle(Paint.Style.STROKE);
        mCenterCirclePaint.setStrokeWidth(mSnowStrokeWidth);
        mCenterCirclePaint.setAntiAlias(true);

        mMainBranchPaint = new Paint();
        mMainBranchPaint.setColor(mSnowColor);
        mMainBranchPaint.setStyle(Paint.Style.STROKE);
        mMainBranchPaint.setStrokeWidth(mSnowStrokeWidth);
        mMainBranchPaint.setAntiAlias(true);

        mSubBranchPaint = new Paint();
        mSubBranchPaint.setColor(mSnowColor);
        mSubBranchPaint.setStyle(Paint.Style.STROKE);
        mSubBranchPaint.setStrokeWidth(mSnowStrokeWidth);
        mSubBranchPaint.setAntiAlias(true);

        mEndOralPaint = new Paint();
        mEndOralPaint.setColor(mSnowColor);
        mEndOralPaint.setStyle(Paint.Style.FILL);
        mEndOralPaint.setAntiAlias(true);
    }

    private void calculateStaticCoordinates() {
        for (int i = 0;  i < 6; i ++) {
            double branchAngle = - Math.PI / 2 - i * Math.PI / 3;
            float maxCenterCircleRadius = Math.min(mWidth, mHeight) * SCALE_CENTER_CIRCLE_RADIUS;
            mMainBranchStartX[i] = mWidth / 2 + (float) Math.cos(branchAngle) * maxCenterCircleRadius;
            mMainBranchStartY[i] = mHeight / 2 + (float) Math.sin(branchAngle) * maxCenterCircleRadius;
            float maxMainBranchLength = Math.min(mWidth, mHeight) * SCALE_MAIN_BRANCH_LENGTH;
            mSubBranchStartX[i] = mMainBranchStartX[i] + (float) Math.cos(branchAngle) * maxMainBranchLength * SCALE_SUB_BRANCH_ON_MAIN;
            mSubBranchStartY[i] = mMainBranchStartY[i] + (float) Math.sin(branchAngle) * maxMainBranchLength * SCALE_SUB_BRANCH_ON_MAIN;
        }
    }

    private void calculateAnimatedValues(float fraction) {
        float maxMainBranchLength = Math.min(mWidth, mHeight) * SCALE_MAIN_BRANCH_LENGTH;
        float maxSubBranchLength = Math.min(mWidth, mHeight) * SCALE_SUB_BRANCH_LENGTH;
        float maxCenterCircleRadius = Math.min(mWidth, mHeight) * SCALE_CENTER_CIRCLE_RADIUS;
        float minCenterCircleRadius = Math.min(mWidth, mHeight) * SCALE_CENTER_CIRCLE_RADIUS * SCALE_CENTER_CIRCLE_ON_START;

        mCenterCircleRadius = (mNeedGrow && fraction < ALPHA_IN_FRACTION) ?
                minCenterCircleRadius + (maxCenterCircleRadius - minCenterCircleRadius) * fraction / ALPHA_IN_FRACTION : maxCenterCircleRadius;
        mRotateDegree = mNeedGrow ? fraction * ROTATE_DEGREE : 0;

        for (int i = 0;  i < 6; i ++) {
            float growingPeriod = MAIN_BRANCH_END_GROW_FRACTION[i] - MAIN_BRANCH_START_GROW_FRACTION[i];
            float mainGrowingFraction = fraction - MAIN_BRANCH_START_GROW_FRACTION[i];
            float subDelayFraction = growingPeriod * SCALE_SUB_BRANCH_ON_MAIN;

            if (mNeedGrow && fraction < MAIN_BRANCH_START_GROW_FRACTION[i]) {
                mMainBranchLength[i] = 0;
                mSubBranchLength[i] = 0;
            } else if (mNeedGrow && fraction < MAIN_BRANCH_END_GROW_FRACTION[i]) {
                mMainBranchLength[i] = maxMainBranchLength * mainGrowingFraction / growingPeriod;
                mSubBranchLength[i] = mainGrowingFraction > subDelayFraction ? maxSubBranchLength * (mainGrowingFraction - subDelayFraction) / growingPeriod : 0;
            } else if (mNeedGrow && fraction < MAIN_BRANCH_END_GROW_FRACTION[i] + subDelayFraction) {
                mMainBranchLength[i] = maxMainBranchLength;
                mSubBranchLength[i] = maxSubBranchLength * (mainGrowingFraction - subDelayFraction) / growingPeriod;
            } else {
                mMainBranchLength[i] = maxMainBranchLength;
                mSubBranchLength[i] = maxSubBranchLength;
            }

            double branchAngle = - Math.PI / 2 - i * Math.PI / 3;
            mMainBranchEndX[i] = mMainBranchStartX[i] + (float) Math.cos(branchAngle) * mMainBranchLength[i];
            mMainBranchEndY[i] = mMainBranchStartY[i] + (float) Math.sin(branchAngle) * mMainBranchLength[i];

            double leftSubAngle = branchAngle + Math.PI / 4;
            mLeftSubBranchEndX[i] = mSubBranchStartX[i] + (float) Math.cos(leftSubAngle) * mSubBranchLength[i];
            mLeftSubBranchEndY[i] = mSubBranchStartY[i] + (float) Math.sin(leftSubAngle) * mSubBranchLength[i];

            double rightSubAngle = branchAngle - Math.PI / 4;
            mRightSubBranchEndX[i] = mSubBranchStartX[i] + (float) Math.cos(rightSubAngle) * mSubBranchLength[i];
            mRightSubBranchEndY[i] = mSubBranchStartY[i] + (float) Math.sin(rightSubAngle) * mSubBranchLength[i];
        }
    }

}

package com.honeycomb.colorphone.cpucooler.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;

public class CpuScanTwinkleView extends View{

    private static final int SQUARE_PER_ROW = 8;
    private static final int ROW_COUNT = 8;
    private static final long PERIOD_PER_TWINKLE = 400;
    private static final long DURATION_ALPHA_ANIMATION = 370;

    private float mSquareMargin;
    private float mWidth;
    private float mHeight;

    private Paint mSquarePaint;
    private Runnable mTwinkleRunnable;
    private ValueAnimator mAlphaAnimator;

    private int[][] mCurrentAlphaMatrix;
    private int[][] mPreviousRandomAlphaMatrix;
    private int[][] mNextRandomAlphaMatrix;

    public CpuScanTwinkleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSquareMargin = context.getResources().getDimensionPixelSize(R.dimen.cpu_square_margin);

        mCurrentAlphaMatrix = new int[ROW_COUNT][SQUARE_PER_ROW];
        mPreviousRandomAlphaMatrix = new int[ROW_COUNT][SQUARE_PER_ROW];
        mNextRandomAlphaMatrix = new int[ROW_COUNT][SQUARE_PER_ROW];

        mSquarePaint = new Paint();
        mSquarePaint.setStyle(Paint.Style.FILL);
        mSquarePaint.setColor(Color.WHITE);
        mSquarePaint.setAntiAlias(true);
    }

    public void startTwinkle() {
        if (null == mTwinkleRunnable) {
            mTwinkleRunnable = new Runnable() {
                @Override
                public void run() {
                    startAlphaAnimation();
                    postDelayed(this, PERIOD_PER_TWINKLE);
                }
            };
        }
        post(mTwinkleRunnable);
    }

    public void stopTwinkle() {
        removeCallbacks(mTwinkleRunnable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float squareWidth = (mWidth - (mSquareMargin * (SQUARE_PER_ROW - 1))) / SQUARE_PER_ROW;
        float squareHeight = (mHeight - (mSquareMargin * (SQUARE_PER_ROW - 1))) / SQUARE_PER_ROW;
        for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex ++) {
            for (int colIndex = 0; colIndex < SQUARE_PER_ROW; colIndex ++) {
                mSquarePaint.setAlpha(mCurrentAlphaMatrix[rowIndex][colIndex]);
                float startX = colIndex * (squareWidth + mSquareMargin);
                float startY = rowIndex * (squareHeight + mSquareMargin);
                canvas.drawRect(startX, startY, startX + squareWidth, startY + squareHeight, mSquarePaint);
            }
        }

    }

    private void startAlphaAnimation() {
        if (null == mAlphaAnimator) {
            mAlphaAnimator = ValueAnimator.ofFloat(0, 1);
            mAlphaAnimator.addUpdateListener(animation -> {
                float fraction = animation.getAnimatedFraction();
                for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex ++) {
                    for (int colIndex = 0; colIndex < SQUARE_PER_ROW; colIndex ++) {
                        mCurrentAlphaMatrix[rowIndex][colIndex] = (int)(mPreviousRandomAlphaMatrix[rowIndex][colIndex]
                                + fraction * (mNextRandomAlphaMatrix[rowIndex][colIndex] - mPreviousRandomAlphaMatrix[rowIndex][colIndex]));
                    }
                }
                invalidate();
            });
            refreshRandomAlphaMatrix();
        }
        refreshRandomAlphaMatrix();
        mAlphaAnimator.cancel();
        mAlphaAnimator.setDuration(DURATION_ALPHA_ANIMATION).start();
    }

    private void refreshRandomAlphaMatrix() {
        for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex ++) {
            for (int colIndex = 0; colIndex < SQUARE_PER_ROW; colIndex++) {
                mPreviousRandomAlphaMatrix[rowIndex][colIndex] = mNextRandomAlphaMatrix[rowIndex][colIndex];
                mNextRandomAlphaMatrix[rowIndex][colIndex] = (int)(235 * Math.random());
            }
        }
    }

}

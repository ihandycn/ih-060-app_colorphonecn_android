package com.acb.libwallpaper.live.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

 import com.honeycomb.colorphone.R;

public class SmoothProgressBar extends View {

    private static final int BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int BAR_START_COLOR = 0xff33ddcf;
    private static final int BAR_END_COLOR = 0xff00aeff;
    private float mProgress;
    private boolean mShouldFadeOut;
    private Paint mShaderPaint;
    private Paint mLightPaint;
    private Bitmap mLightBitmap;
    private Rect mLightRect;
    private Rect mClipRect;
    private ValueAnimator mAnimator;
    private float mAnimValue = 0;
    private boolean mIsRunning;

    public SmoothProgressBar(Context context) {
        this(context, null);
    }

    public SmoothProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mShaderPaint = new Paint();
        mLightPaint = new Paint();
        mLightBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.news_detail_progressbar_light);
        mLightRect = new Rect();
        mClipRect = new Rect();
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.setDuration(1500);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != 0) {
            mShaderPaint.setShader(new LinearGradient(0, 0, w, 0, BAR_START_COLOR, BAR_END_COLOR, Shader.TileMode.CLAMP));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(BACKGROUND_COLOR);
        mClipRect.set(0, 0, (int) (getWidth() * mProgress / 100), getHeight());
        canvas.drawRect(mClipRect, mShaderPaint);
        canvas.clipRect(mClipRect);
        mLightRect.set((int) (getWidth() * mAnimValue - mLightBitmap.getWidth()), 0, (int) (getWidth() * mAnimValue), getHeight());
        canvas.drawBitmap(mLightBitmap, null, mLightRect, mLightPaint);
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        if (progress > 90f && mShouldFadeOut) {
            float alphaRatio = 1f - (progress - 90f) / 10f;
            mShaderPaint.setAlpha((int) (0xFF * alphaRatio));
            mLightPaint.setAlpha((int) (0xFF * alphaRatio));
        } else {
            mShaderPaint.setAlpha(0xFF);
            mLightPaint.setAlpha(0xFF);
        }
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public void startAnimator() {
        mIsRunning = true;
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsRunning) {
                    mAnimator.start();
                } else {
                    mAnimator.cancel();
                }
            }
        }, 100);
    }

    public void stopAnimator() {
        mIsRunning = false;
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    public void setFadeOut(boolean shouldFadeOut) {
        this.mShouldFadeOut = shouldFadeOut;
    }
}

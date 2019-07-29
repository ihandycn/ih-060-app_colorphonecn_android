package com.honeycomb.colorphone.customize.livewallpaper.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.graphics.PathMeasure;

import java.text.ParseException;

public class PathAnimator {
    private static final String svgPath = "M41,464.539062 C67.6985242,391.343454 173.881006,249.836262 328.578125,194.523438";
    private static int svgWidthPx = 360;
    private static int svgHeghtPx = 640;

    Path mPath;
    PathMeasure mPathMeasure;
    float[] points = new float[2];
    float[] tan = new float[2];
    private float mLength;
    private float mProgress;
    private int mDuration = 150;

    private Callback mCallback;
    private int mCurrentRepeat;
    private int loopCount = 1;
    private float scaleX = 1;
    private float scaleY = 1;
    private ValueAnimator animator;

    public void init() throws ParseException {
        SvgPathParser pathParser = new SvgPathParser();
        mPath = pathParser.parsePath(svgPath);
        mPathMeasure = new PathMeasure(mPath, false);
        mLength = mPathMeasure.getLength();

        animator = ValueAnimator
                .ofFloat(0, 1f)
                .setDuration(mDuration / loopCount);
//        animator.setInterpolator(PathInterpolatorCompat.create(.43f, .14f, .58f, .91f));
        animator.setRepeatCount(loopCount - 1);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fragmentProgress = (float) animation.getAnimatedValue();
                float progress = fragmentProgress / loopCount + (float) mCurrentRepeat / loopCount;
                onProgress(progress);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                mCurrentRepeat++;
            }
        });
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public void setTargetSizePixel(float width, float height) {
        scaleX = width / svgWidthPx;
        scaleY = height / svgHeghtPx;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void startAnim() {
        if (mPath == null) {
            try {
                init();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        animator.start();
    }

    public void addListener(Animator.AnimatorListener listener) {
        if (animator != null) {
            animator.addListener(listener);
        }
    }

    private void onProgress(float progress) {
        mProgress = progress;
        float distance = progress * mLength;
        mPathMeasure.getPosTan(distance, points, tan);
        if (mCallback != null) {
            mCallback.onPoint(points[0] * scaleX, points[1] * scaleY);
        }
    }

    public float getProgress() {
        return mProgress;
    }

    public void cancel() {
        if (animator.isRunning()) {
            animator.cancel();
        }
    }

    public interface Callback {
        void onPoint(float x, float y);
    }
}

package com.acb.libwallpaper.live.livewallpaper.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

public class RotationMaker {
    private Interpolator interpolator;
    private final AnimatorSet animatorSet = new AnimatorSet();
    private List<Callback> mCallback = new ArrayList<>();
    private boolean isEnding;

    public void start() {
        do3DAnimation();
    }

    private void do3DAnimation() {
        if (animatorSet.isRunning()) {
            animatorSet.cancel();
        }

        interpolator = PathInterpolatorCompat.create(.5f, .01f, .5f, .98f);

        List<Animator> valueAnimators = new ArrayList<>(6);

        // TODO , use more efficient way.
        valueAnimators.add(buildRotateYAnimator(0f, 30f));
        valueAnimators.add(buildRotateYAnimator(30f, -30f));
        valueAnimators.add(buildRotateYAnimator(-30f, 0f));
        valueAnimators.add(buildRotateXAnimator(0f, 20f));
        valueAnimators.add(buildRotateXAnimator(20f, -20f));
        valueAnimators.add(buildRotateXAnimator(-20f, 0f));

        animatorSet.playSequentially(valueAnimators);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isEnding = true;

                for (Callback callback : mCallback) {
                    if (callback != null) {
                        callback.onRotateEnd();
                    }
                }
            }
        });
        animatorSet.start();
        isEnding = false;
    }

    private ValueAnimator buildRotateXAnimator(float start, float end) {
        ValueAnimator rotateX = ValueAnimator.ofFloat(start, end)
                .setDuration(500);
        rotateX.setInterpolator(interpolator);
        rotateX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = (float) animation.getAnimatedValue();
                for (Callback callback : mCallback) {
                    if (callback != null) {
                        callback.onRotateX(angle);
                    }
                }

            }

        });
        return rotateX;
    }

    private ValueAnimator buildRotateYAnimator(float start, float end) {
        ValueAnimator rotateY = ValueAnimator.ofFloat(start, end)
                .setDuration(500);
        rotateY.setInterpolator(interpolator);
        rotateY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float angle = (float) animation.getAnimatedValue();
                for (Callback callback : mCallback) {
                    if (callback != null) {
                        callback.onRotateY(angle);
                    }
                }
            }
        });
        return rotateY;
    }

    public void addCallback(Callback callback) {
        mCallback.add(callback);
    }

    public void removeCallback(Callback callback) {
        mCallback.remove(callback);
    }

    public boolean isRunning() {
        return !isEnding && animatorSet.isRunning();
    }

    public void cancel() {
        animatorSet.cancel();
    }

    public interface Callback {
        void onRotateX(float angle);

        void onRotateY(float angle);

        void onRotateEnd();
    }

}


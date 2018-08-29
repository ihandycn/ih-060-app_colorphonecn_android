package com.honeycomb.colorphone.battery;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

public class IconAnimations {

    public static void startHintAnimation(View icon) {
        if (icon == null) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(2);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setEvaluator(new SpringEvaluator(0.4f, 5f, 1 / 3f, 1 / 4f));
        valueAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            icon.setScaleX(scale);
            icon.setScaleY(scale);
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                icon.setScaleX(1);
                icon.setScaleY(1);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                icon.setScaleX(1);
                icon.setScaleY(1);
            }
        });
        valueAnimator.start();
    }
}

package com.honeycomb.colorphone.boost;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;

import java.util.WeakHashMap;

public class LauncherAnimUtils {

    public static final float FRAME_PERIOD_MILLIS = 1000f / 60f;

    /*
     * Frequently used interpolators.
     */

    public static final TimeInterpolator LINEAR = new LinearInterpolator();

    public static final TimeInterpolator OVERSHOOT = new OvershootInterpolator();

    public static final TimeInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();

    public static final TimeInterpolator ACCELERATE_QUAD =
            AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.accelerate_quad);

    /**
     * Interpolator for snap-to-page slide animation, used by standard {@link android.support.v4.view.ViewPager}.
     */
    public static final TimeInterpolator DECELERATE_QUINT =
            AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.decelerate_quint);

    public static final TimeInterpolator DECELERATE_QUART =
            AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.decelerate_quart);

    public static final TimeInterpolator DECELERATE_TIRPLE =
            AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.decelerate_triple);

    public static final TimeInterpolator DECELERATE_QUAD =
            AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.decelerate_quad);

    /*
     * Icon appear bounce animation constants.
     */
    private static final float BOUNCE_ANIMATION_TENSION = 1.3f;
    private static final int BOUNCE_DURATION = 450;
    private static final int BOUNCE_APPEAR_STAGGER_DELAY = 85;

    private static WeakHashMap<Animator, Object> sAnimators = new WeakHashMap<>();

    private static Animator.AnimatorListener sEndAnimListener = new Animator.AnimatorListener() {
        public void onAnimationStart(Animator animation) {
            sAnimators.put(animation, null);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            sAnimators.remove(animation);
        }

        public void onAnimationCancel(Animator animation) {
            sAnimators.remove(animation);
        }
    };

    public static void cancelOnDestroyActivity(Animator a) {
        a.addListener(sEndAnimListener);
    }


    public static AnimatorSet createAnimatorSet() {
        AnimatorSet anim = new AnimatorSet();
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ValueAnimator ofFloat(float... values) {
        return ofFloat(null, values);
    }

    public static ValueAnimator ofFloat(View target, float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static void expandViewAnamationo(final View v, long duration, int targetHeight) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public static void startAlphaAppearAnimation(final View v, long duration) {
        ObjectAnimator transaction = ObjectAnimator.ofFloat(v, "alpha", 0f, 1.0f);
        transaction.setDuration(duration);
        transaction.start();
    }

    public static void startAlphaDisappearAnimation(final View v, long duration) {
        ObjectAnimator transaction = ObjectAnimator.ofFloat(v, "alpha", 1.0f, 0.0f);
        transaction.setDuration(duration);
        transaction.start();
    }
}

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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;

import java.util.HashSet;
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

    private static long sShortAnimDuration;
    static {
        sShortAnimDuration = HSApplication.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public static long getShortAnimDuration() {
        return sShortAnimDuration;
    }

    public static Animator createIconAppearAnimation(final View icon, int delayFactor) {
        icon.setAlpha(0f);
        icon.setScaleX(0f);
        icon.setScaleY(0f);
        ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(icon,
                PropertyValuesHolder.ofFloat("alpha", 1f),
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));
        bounceAnim.setDuration(BOUNCE_DURATION);
        bounceAnim.setStartDelay(delayFactor * BOUNCE_APPEAR_STAGGER_DELAY);
        bounceAnim.setInterpolator(getIconBounceInterpolator(false));
        bounceAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                icon.setVisibility(View.VISIBLE);
            }
        });
        return bounceAnim;
    }

    public static Animator createIconDisappearAnimation(View icon) {
        icon.setAlpha(1f);
        icon.setScaleX(1f);
        icon.setScaleY(1f);
        ValueAnimator bounceAnim = LauncherAnimUtils.ofPropertyValuesHolder(icon,
                PropertyValuesHolder.ofFloat("alpha", 0f),
                PropertyValuesHolder.ofFloat("scaleX", 0f),
                PropertyValuesHolder.ofFloat("scaleY", 0f));
        bounceAnim.setDuration(BOUNCE_DURATION);
        bounceAnim.setInterpolator(getIconBounceInterpolator(true));
        return bounceAnim;
    }

    private static TimeInterpolator getIconBounceInterpolator(boolean inverse) {
        final TimeInterpolator interpolator = new OvershootInterpolator(BOUNCE_ANIMATION_TENSION);
        if (inverse) {
            return new TimeInterpolator() {
                @Override
                public float getInterpolation(float input) {
                    return 1f - interpolator.getInterpolation(1f - input);
                }
            };
        }
        return interpolator;
    }

    public static Animator createIconHighlightAnimation(View icon) {
        AnimatorSet animSet = createAnimatorSet();
        Animator zoomOutX = ObjectAnimator.ofFloat(icon, "scaleX", 1.0f, 1.2f);
        zoomOutX.setDuration(200);
        Animator zoomOutY = ObjectAnimator.ofFloat(icon, "scaleY", 1.0f, 1.2f);
        zoomOutY.setDuration(200);
        Animator zoomInX = ObjectAnimator.ofFloat(icon, "scaleX", 1.2f, 1.0f);
        zoomInX.setDuration(200);
        Animator zoomInY = ObjectAnimator.ofFloat(icon, "scaleY", 1.2f, 1.0f);
        zoomInY.setDuration(200);
        animSet.play(zoomOutX).with(zoomOutY);
        animSet.play(zoomInX).with(zoomInY).after(zoomOutX);
        return animSet;
    }

    public static void setHardwareLayerDuringAnimation(Animator animation, final View... views) {
        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                for (View view : views) {
                    view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (View view : views) {
                    view.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            }
        });
    }

    public static void cancelOnDestroyActivity(Animator a) {
        a.addListener(sEndAnimListener);
    }

    // Helper method. Assumes a draw is pending, and that if the animation's duration is 0
    // it should be cancelled

    @TargetApi(16)
    public static void startAnimationAfterNextDraw(final Animator animator, final View view) {
        view.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            private boolean mStarted = false;
            public void onDraw() {
                if (mStarted) return;
                mStarted = true;
                // Use this as a signal that the animation was cancelled
                if (animator.getDuration() == 0) {
                    return;
                }
                animator.start();

                final ViewTreeObserver.OnDrawListener listener = this;
                view.post(new Runnable() {
                    public void run() {
                        view.getViewTreeObserver().removeOnDrawListener(listener);
                    }
                });
            }
        });
    }

    public static void onDestroyActivity() {
        HashSet<Animator> animators = new HashSet<>(sAnimators.keySet());
        for (Animator a : animators) {
            if (a.isRunning()) {
                a.cancel();
            }
            sAnimators.remove(a);
        }
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

    public static ObjectAnimator ofFloat(View target, String propertyName, float... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setPropertyName(propertyName);
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        new FirstFrameAnimatorHelper(anim, target);
        return anim;
    }

    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ObjectAnimator ofPropertyValuesHolder(View target,
                                                        PropertyValuesHolder... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setValues(values);
        cancelOnDestroyActivity(anim);
        new FirstFrameAnimatorHelper(anim, target);
        return anim;
    }

    public static ObjectAnimator ofPropertyValuesHolder(Object target,
                                                        View view, PropertyValuesHolder... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setValues(values);
        cancelOnDestroyActivity(anim);
        new FirstFrameAnimatorHelper(anim, view);
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

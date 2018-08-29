package com.honeycomb.colorphone.resultpage;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class LauncherAnimationUtils {
    public static final int REPEAT_MODE_NO = -100;

    public static final AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    public static final LinearInterpolator linearInterpolator = new LinearInterpolator();
    public static final AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    public static class AnimationListenerAdapter implements Animation.AnimationListener {
        /**
         * <p>Notifies the start of the animation.</p>
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animation animation) {
        }

        /**
         * <p>Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animation animation) {
        }

        /**
         * <p>Notifies the repetition of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    public static Animation getAlphaAnimation(float fromAlpha, float toAlpha) {
        return new AlphaAnimation(fromAlpha, toAlpha);
    }

    public static Animation getAlphaAnimation(float fromAlpha, float toAlpha, long duration) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(duration);
        return alphaAnimation;
    }

    public static Animation getAlphaAnimation(float fromAlpha, float toAlpha, long duration, Animation.AnimationListener animationListener) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setAnimationListener(animationListener);
        return alphaAnimation;
    }

    public static Animation getAlphaAnimation(float fromAlpha, float toAlpha, long duration, long startOff) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setStartOffset(startOff);
        return alphaAnimation;
    }

    public static Animation getAlphaAppearAnimation() {
        return new AlphaAnimation(0f, 1.0f);
    }

    public static Animation getAlphaAppearAnimation(long duration) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(duration);
        return alphaAnimation;
    }

    public static Animation getAlphaAppearAnimation(long duration, Interpolator interpolator) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setInterpolator(interpolator);
        return alphaAnimation;
    }

    public static Animation getAlphaAppearAnimation(long duration, boolean isFillAfter) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(isFillAfter);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        return alphaAnimation;
    }

    public static Animation getAlphaAppearAnimation(long duration, long startOffset, Interpolator interpolator) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setStartOffset(startOffset);
        alphaAnimation.setInterpolator(interpolator);
        return alphaAnimation;
    }

    public static Animation getAlphaAppearAnimation(long duration, long startOffSet) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1.0f);
        alphaAnimation.setDuration(duration);
        if (startOffSet != 0) {
            alphaAnimation.setStartOffset(startOffSet);
        }
        return alphaAnimation;
    }

    public static Animation getAlphaDisAppearAnimation (long duration, long startOffSet, Animation.AnimationListener animationListener) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
        alphaAnimation.setDuration(duration);
        if (startOffSet != 0) {
            alphaAnimation.setStartOffset(startOffSet);
        }
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        alphaAnimation.setInterpolator(linearInterpolator);
        alphaAnimation.setAnimationListener(animationListener);
        return alphaAnimation;
    }

    public static Animation getAlphaDisAppearAnimation(long duration, long startOffSet) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
        alphaAnimation.setDuration(duration);
        if (startOffSet != 0) {
            alphaAnimation.setStartOffset(startOffSet);
        }
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        alphaAnimation.setInterpolator(linearInterpolator);
        return alphaAnimation;
    }

    public static Animation getAlphaDisAppearAnimation(long duration, Interpolator interpolator) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setInterpolator(interpolator);
        return alphaAnimation;
    }

    public static Animation getAlphaDisAppearAnimation(long duration, long startOffSet, boolean isFillAfter) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setStartOffset(startOffSet);
        alphaAnimation.setFillAfter(isFillAfter);
        LinearInterpolator linearInterpolator = new LinearInterpolator();
        alphaAnimation.setInterpolator(linearInterpolator);
        return alphaAnimation;
    }

    public static Animation getAlphaAppearNoChangeAnimation(long duration, long startOffSet) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
        alphaAnimation.setDuration(duration);
        if (startOffSet != 0) {
            alphaAnimation.setStartOffset(startOffSet);
        }
        return alphaAnimation;
    }

    public static Animation getRotateAnimation (long duration, int toDegrees) {
        final RotateAnimation animation =new RotateAnimation(0f,toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(duration);
        animation.setRepeatCount(0);
        AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        animation.setInterpolator(accelerateDecelerateInterpolator);
        return animation;
    }

    public static Animation getRotateAnimation(long duration, long startOffSet, float toDegrees) {
        final RotateAnimation animation = new RotateAnimation(0f, toDegrees, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setStartOffset(startOffSet);
        animation.setRepeatCount(0);
        AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        animation.setInterpolator(accelerateDecelerateInterpolator);
        return animation;
    }

    public static Animation getTranslateYAnimation(float fromYDelta, float toYDelta, long duration, long startOffSet, boolean fillAfter, Interpolator interpolator) {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, fromYDelta, toYDelta);
        translateAnimation.setDuration(duration);
        if (startOffSet != 0) {
            translateAnimation.setStartOffset(startOffSet);
        }
        translateAnimation.setFillAfter(fillAfter);
        translateAnimation.setInterpolator(interpolator);
        return translateAnimation;
    }

    public static Animation getTranslateYAnimation(float fromYDelta, float toYDelta) {
        return new TranslateAnimation(0, 0, fromYDelta, toYDelta);
    }

    public static Animation getTranslateYAnimation(float fromYDelta, float toYDelta, boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, fromYDelta, toYDelta);
        translateAnimation.setFillAfter(fillAfter);
        return translateAnimation;
    }

    public static Animation getTranslateXAnimation(float fromXDelta, float toXDelta) {
        return new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
    }

    public static Animation getTranslateXAnimation(float fromXDelta, float toXDelta, boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
        translateAnimation.setFillAfter(fillAfter);
        return translateAnimation;
    }

    public static Animation getTranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        return new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
    }

    public static Animation getTranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, long duration, boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        translateAnimation.setFillAfter(fillAfter);
        translateAnimation.setDuration(duration);
        return translateAnimation;
    }

    public static Animation getScaleAnimation (float fromX, float toX, float fromY, float toY, long duration, long startOffSet, boolean fillAfter, Interpolator interpolator, Animation.AnimationListener animationListener) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        if (startOffSet != 0) {
            scaleAnimation.setStartOffset(startOffSet);
        }
        scaleAnimation.setFillAfter(fillAfter);
        scaleAnimation.setAnimationListener(animationListener);
        scaleAnimation.setInterpolator(interpolator);
        return scaleAnimation;
    }

    public static Animation getScaleAnimation(float fromX, float toX, float fromY, float toY, long duration, long startOffSet, boolean fillAfter) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        if (startOffSet != 0) {
            scaleAnimation.setStartOffset(startOffSet);
        }
        scaleAnimation.setFillAfter(fillAfter);
        return scaleAnimation;
    }

    public static Animation getScaleAnimation(float fromX, float toX, float fromY, float toY) {
        return new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    }

    public static Animation getScaleAnimation(float fromX, float toX, float fromY, float toY, long duration, long startOffSet, boolean fillAfter, Interpolator interpolator) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        if (startOffSet != 0) {
            scaleAnimation.setStartOffset(startOffSet);
        }
        scaleAnimation.setInterpolator(interpolator);
        scaleAnimation.setFillAfter(fillAfter);
        return scaleAnimation;
    }

    public static Animation getScaleYAnimation(float fromY, float toY, long duration, long startOffSet) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, fromY, toY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        if (startOffSet != 0) {
            scaleAnimation.setStartOffset(startOffSet);
        }
        return scaleAnimation;
    }

    public static Animation getScaleXAnimation(float fromX, float toX, long duration, long startOffSet) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, toX, 1.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        if (startOffSet != 0) {
            scaleAnimation.setStartOffset(startOffSet);
        }
        return scaleAnimation;
    }

    public static Animation getBoostRotateAnimation (long duration) {
        final RotateAnimation animation =new RotateAnimation(0f,3600, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(duration);
        animation.setRepeatCount(0);
        AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        animation.setInterpolator(accelerateDecelerateInterpolator);
        return animation;
    }

    public static void startLoopScaleAnimation(final View view, final long duration, final float toMin, boolean isFirstStart) {
        if (null != view.getTag() && view.getTag() instanceof Boolean && (boolean) view.getTag()) {
            return;
        }

        if (!isFirstStart || (view.getAnimation() == null)) {
            Animation scaleToMinAnimation = getScaleAnimation(1.0f, toMin, 1.0f, toMin, duration, 0 , true);
            LauncherAnimationUtils.startAnimation(view, scaleToMinAnimation,
                    new LauncherAnimationUtils.AnimationListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            super.onAnimationStart(animation);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            super.onAnimationEnd(animation);
                            startScaleResumeAnimation(view, duration, toMin);
                        }
                    });
        }
    }

    private static void startScaleResumeAnimation(final View view, final long duration, final float toMin) {
        Animation scaleToMinAnimation = getScaleAnimation(toMin, 1.0f, toMin, 1.0f, duration, 0 , true);
        LauncherAnimationUtils.startAnimation(view, scaleToMinAnimation,
                new LauncherAnimationUtils.AnimationListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        super.onAnimationStart(animation);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        startLoopScaleAnimation(view, duration, toMin, false);
                    }
                });
    }

    public static void startLoopRotateAnimation(final View view, final long duration, final long startOffSet, final float toDegrees, boolean isFirstStart) {
        if (null != view.getTag() && view.getTag() instanceof Boolean && (boolean) view.getTag()) {
            return;
        }

        if (!isFirstStart || (view.getAnimation() == null)) {
            Animation rotateAnimation = getRotateAnimation(duration, isFirstStart ? 0 : startOffSet, toDegrees);
            LauncherAnimationUtils.startAnimation(view, rotateAnimation,
                    new LauncherAnimationUtils.AnimationListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            super.onAnimationStart(animation);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            super.onAnimationEnd(animation);
                            startLoopRotateAnimation(view, duration, startOffSet, toDegrees, false);
                        }
                    });
        }
    }

    public static void startAnimation(final View v, Animation animation) {
        if (null == v || null == animation) {
            return;
        }

        v.clearAnimation();
        v.startAnimation(animation);
    }

    public static void startAnimation(final View v, Animation animation, AnimationListenerAdapter animationListenerAdapter) {
        if (null == v || null == animation) {
            return;
        }

        animation.setAnimationListener(animationListenerAdapter);
        v.clearAnimation();
        v.startAnimation(animation);
    }

    public static void startAnimation(final View v, final boolean isEndGone, Animation animation) {
        if (null == v || null == animation) {
            return;
        }

        v.clearAnimation();
        v.startAnimation(animation);
        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isEndGone) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void startAnimation(final View v, long duration, long startOff, Interpolator interpolator, AnimationListenerAdapter animationListenerAdapter, Animation animation) {
        if (null == v) {
            return;
        }

        animation.setInterpolator(interpolator);
        if (0 != duration) {
            animation.setDuration(duration);
        }
        if (0 != startOff) {
            animation.setStartOffset(startOff);
        }
        animation.setAnimationListener(animationListenerAdapter);
        v.startAnimation(animation);
    }

    public static void startSetAnimation (final View v, final boolean isEndGone, long duration, Interpolator interpolator, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (int i = 0; i < animations.length; i++) {
            animationSet.addAnimation(animations[i]);
        }
        animationSet.setInterpolator(interpolator);
        if (0 != duration) {
            animationSet.setDuration(duration);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isEndGone) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void startSetAnimation(final View v, final boolean isEndGone, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isEndGone) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void startSetAnimation(final View v, AnimationListenerAdapter animationListenerAdapter, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(animationListenerAdapter);
    }

    public static void startSetAnimation(final View v, long startOff, Interpolator interpolator, final boolean isEndGone, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        animationSet.setInterpolator(interpolator);
        if (0 != startOff) {
            animationSet.setStartOffset(startOff);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isEndGone) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void startSetAnimation(final View v, long duration, long startOff, final boolean isEndGone, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        if (0 != duration) {
            animationSet.setDuration(duration);
        }
        if (0 != startOff) {
            animationSet.setStartOffset(startOff);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isEndGone) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void startSetAnimation(final View v, long duration, long startOff, Interpolator interpolator, final boolean isEndGone, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        animationSet.setInterpolator(interpolator);
        if (0 != duration) {
            animationSet.setDuration(duration);
        }
        if (0 != startOff) {
            animationSet.setStartOffset(startOff);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isEndGone) {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static void startSetAnimation(final View v, long startOff, Interpolator interpolator, AnimationListenerAdapter animationListenerAdapter, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        animationSet.setInterpolator(interpolator);
        if (0 != startOff) {
            animationSet.setStartOffset(startOff);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(animationListenerAdapter);
    }

    public static void startSetAnimation(final View v, long duration, long startOff, Interpolator interpolator, AnimationListenerAdapter animationListenerAdapter, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        animationSet.setInterpolator(interpolator);
        if (0 != duration) {
            animationSet.setDuration(duration);
        }
        if (0 != startOff) {
            animationSet.setStartOffset(startOff);
        }
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(animationListenerAdapter);
    }

    public static void startSetAnimation(final View v, long duration, long startOff, boolean isFillAfter, Interpolator interpolator, AnimationListenerAdapter animationListenerAdapter, Animation... animations) {
        if (null == v) {
            return;
        }

        final AnimationSet animationSet = new AnimationSet(false);
        for (Animation animation : animations) {
            animationSet.addAnimation(animation);
        }
        animationSet.setInterpolator(interpolator);
        if (0 != duration) {
            animationSet.setDuration(duration);
        }
        if (0 != startOff) {
            animationSet.setStartOffset(startOff);
        }
        animationSet.setFillAfter(isFillAfter);
        v.startAnimation(animationSet);
        animationSet.setAnimationListener(animationListenerAdapter);
    }

    public static float[] calculateParabola(float[][] points) {
        float x1 = points[0][0];
        float y1 = points[0][1];
        float x2 = points[1][0];
        float y2 = points[1][1];
        float x3 = points[2][0];
        float y3 = points[2][1];

        final float a = (y1 * (x2 - x3) + y2 * (x3 - x1) + y3 * (x1 - x2))
                / (x1 * x1 * (x2 - x3) + x2 * x2 * (x3 - x1) + x3 * x3 * (x1 - x2));
        final float b = (y1 - y2) / (x1 - x2) - a * (x1 + x2);
        final float c = y1 - (x1 * x1) * a - x1 * b;
        return new float[]{a, b, c};
    }

    /**
     * AccelerateDecelerateInterpolator
     * @param input 0-1
     * @return 0-1
     */
    public static float getAdInterpolator(float input) {
        return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
    }

}
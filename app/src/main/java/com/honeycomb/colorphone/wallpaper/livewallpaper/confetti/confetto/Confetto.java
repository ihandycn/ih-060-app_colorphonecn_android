/**
 * Copyright (C) 2016 Robinhood Markets, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.confetto;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.VelocityTracker;
import android.view.animation.Interpolator;

import com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.render.ConfettiRenderer;

import java.nio.FloatBuffer;

/**
 * Abstract class that represents a single confetto on the screen. This class holds all of the
 * internal states for the confetto to help it animate.
 *
 * <p>All of the configured states are in milliseconds, e.g. pixels per millisecond for velocity.
 */
public abstract class Confetto {

    private static final int MAX_ALPHA = 255;
    private static final long RESET_ANIMATION_INITIAL_DELAY = -1;

    private final Matrix matrix = new Matrix();
    private final Paint workPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Configured coordinate states
    // Note that all (X, Y) representations refer to the center of confetto image.
    protected Rect bound;
    private long initialDelay;
    private float initialX, destinationX, initialY, destinationY, initialVelocityX, initialVelocityY,
            accelerationX, accelerationY;
    private Float targetVelocityX, targetVelocityY;
    private Long millisToReachTargetVelocityX, millisToReachTargetVelocityY;

    // Configured rotation states
    private float initialRotation, initialRotationalVelocity, rotationalAcceleration;
    private Float targetRotationalVelocity;
    private Long millisToReachTargetRotationalVelocity;

    // Configured scale states
    private float initialScale, initialScaleVelocity, scaleAcceleration;
    private Float targetScaleVelocity;
    private Long millisToReachTargetScaleVelocity;

    private float alpha;

    // Configured click state
    private boolean clickable;

    // Configured touch state
    private boolean fromTouch;

    // Configured animation states
    private long ttl;
    private Interpolator fadeOutInterpolator;
    private Interpolator scaleInterpolator;

    private float millisToReachBound;
    private float percentageAnimated;

    // Current draw states
    protected float currentX, currentY;
    protected float currentRotation;
    protected float currentScale;
    protected float currentRealScale;
    // currentAlpha is [0, 255]
    private int currentAlpha;
    private boolean startedAnimation, terminated;

    // Touch events
    private boolean touchOverride;
    private VelocityTracker velocityTracker;
    private float overrideX, overrideY;
    private float overrideDeltaX, overrideDeltaY;

    private static final RectF sTempBound = new RectF();
    private static final RectF sTempBoundWithMargin = new RectF();
    private static final Matrix sTempMatrix = new Matrix();
    private static final float[] sTempPoint = new float[2];

    /**
     * This method should be called after all of the confetto's state variables are configured
     * and before the confetto gets animated.
     *
     * @param bound the space in which the confetto can display in.
     */
    public void prepare(Rect bound) {
        this.bound = bound;

        millisToReachTargetVelocityX = computeMillisToReachTarget(targetVelocityX,
                initialVelocityX, accelerationX);
        millisToReachTargetVelocityY = computeMillisToReachTarget(targetVelocityY,
                initialVelocityY, accelerationY);
        millisToReachTargetRotationalVelocity = computeMillisToReachTarget(targetRotationalVelocity,
                initialRotationalVelocity, rotationalAcceleration);

        // Compute how long it would take to reach x/y bounds or reach TTL.
        millisToReachBound = ttl >= 0 ? ttl : Long.MAX_VALUE;
        int maxDistanceFromCenter = getMaxDistanceFromCenter();

        // Confetto coordinates are at the center.
        float[] maxBound = adjustInitialCoord(bound, maxDistanceFromCenter);

        final long timeToReachXBound = computeBound(maxBound[0], initialVelocityX, accelerationX,
                millisToReachTargetVelocityX, targetVelocityX,
                destinationX != 0 ? (int)destinationX : bound.left - maxDistanceFromCenter,
                destinationX != 0 ? (int)destinationX : bound.right + maxDistanceFromCenter);
        millisToReachBound = Math.min(timeToReachXBound, millisToReachBound);
        final long timeToReachYBound = computeBound(maxBound[1], initialVelocityY, accelerationY,
                millisToReachTargetVelocityY, targetVelocityY,
                destinationY != 0 ? (int)destinationY : bound.top - maxDistanceFromCenter,
                destinationY != 0 ? (int)destinationY : bound.bottom + maxDistanceFromCenter);
        millisToReachBound = Math.min(timeToReachYBound, millisToReachBound);

        configurePaint(workPaint);
    }

    // Synchronized to use shared static fields. No contention at all for now.
    private synchronized float[] adjustInitialCoord(Rect bound, int maxDistanceFromCenter) {
        sTempBound.set(bound);
        sTempBoundWithMargin.set(bound);
        sTempBoundWithMargin.inset(-maxDistanceFromCenter, -maxDistanceFromCenter);

        sTempMatrix.setRectToRect(sTempBound, sTempBoundWithMargin, Matrix.ScaleToFit.FILL);

        sTempPoint[0] = initialX;
        sTempPoint[1] = initialY;
        sTempMatrix.mapPoints(sTempPoint);
        return sTempPoint;
    }

    /**
     * @return the width of the confetto.
     */
    public abstract int getWidth();

    /**
     * @return the height of the confetto.
     */
    public abstract int getHeight();

    public abstract int getMaxDistanceFromCenter();

    // Visible for testing
    protected static Long computeMillisToReachTarget(Float targetVelocity, float initialVelocity,
            float acceleration) {
        if (targetVelocity != null) {
            if (acceleration != 0f) {
                final long time = (long) ((targetVelocity - initialVelocity) / acceleration);
                return time > 0 ? time : 0;
            } else {
                if (targetVelocity < initialVelocity) {
                    return 0L;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    // Visible for testing
    protected static long computeBound(float initialPos, float velocity, float acceleration,
            Long targetTime, Float targetVelocity, int minBound, int maxBound) {
        if (acceleration != 0) {
            // non-zero acceleration
            final int bound = acceleration > 0 ? maxBound : minBound;

            if (targetTime == null || targetTime < 0) {
                // https://www.wolframalpha.com/input/
                // ?i=solve+for+t+in+(d+%3D+x+%2B+v+*+t+%2B+0.5+*+a+*+t+*+t)

                final double tmp = Math.sqrt(
                        2 * acceleration * bound - 2 * acceleration * initialPos
                                + velocity * velocity);

                final double firstTime = (-tmp - velocity) / acceleration;
                if (firstTime > 0) {
                    return (long) firstTime;
                }

                final double secondTime = (tmp - velocity) / acceleration;
                if (secondTime > 0) {
                    return (long) secondTime;
                }

                return Long.MAX_VALUE;
            } else {
                // d = x + v * tm + 0.5 * a * tm * tm + tv * (t - tm)
                // d - x - v * tm - 0.5 * a * tm * tm = tv * t - tv * tm
                // d - x - v * tm - 0.5 * a * tm * tm + tv * tm = tv * t
                // t = (d - x - v * tm - 0.5 * a * tm * tm + tv * tm) / tv

                final double time =
                        (bound - initialPos - velocity * targetTime -
                                0.5 * acceleration * targetTime * targetTime +
                                targetVelocity * targetTime) /
                        targetVelocity;

                return time > 0 ? (long) time : Long.MAX_VALUE;
            }
        } else {
            float actualVelocity = targetTime == null ? velocity : targetVelocity;
            final int bound = actualVelocity > 0 ? maxBound : minBound;
            if (actualVelocity != 0) {
                final double time = (bound - initialPos) / actualVelocity;
                return time > 0 ? (long) time : Long.MAX_VALUE;
            } else {
                return Long.MAX_VALUE;
            }
        }
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public float getCurrentWidth() {
        return currentRealScale * getWidth();
    }

    public float getCurrentHeight() {
        return currentRealScale * getHeight();
    }

    public void terminate() {
        terminated = true;
    }

    public boolean isClickable() {
        return clickable;
    }

    /**
     * Reset this confetto object's internal states so that it can be re-used.
     */
    public void reset() {
        initialDelay = 0;
        initialX = initialY = 0f;
        initialVelocityX = initialVelocityY = 0f;
        accelerationX = accelerationY = 0f;
        targetVelocityX = targetVelocityY = null;
        millisToReachTargetVelocityX = millisToReachTargetVelocityY = null;

        initialRotation = 0f;
        initialRotationalVelocity = 0f;
        rotationalAcceleration = 0f;
        targetRotationalVelocity = null;
        millisToReachTargetRotationalVelocity = null;

        initialScale = 1f;
        initialScaleVelocity = 0f;
        scaleAcceleration = 0f;
        targetScaleVelocity = null;
        millisToReachTargetScaleVelocity = null;

        ttl = 0;
        millisToReachBound = 0f;
        percentageAnimated = 0f;
        fadeOutInterpolator = null;
        scaleInterpolator = null;

        currentX = currentY = 0f;
        currentRotation = 0f;
        currentAlpha = (int) (alpha * MAX_ALPHA);
        startedAnimation = false;
        terminated = false;
    }

    /**
     * Hook to configure the global paint states before any animation happens.
     *
     * @param paint the paint object that will be used to perform all draw operations.
     */
    protected void configurePaint(Paint paint) {
        paint.setAlpha(currentAlpha);
    }

    /**
     * Update the confetto internal state based on the provided passed time.
     *
     * @param passedTime time since the beginning of the animation.
     * @return whether this particular confetto is still animating.
     */
    public boolean applyUpdate(long passedTime) {
        if (initialDelay == RESET_ANIMATION_INITIAL_DELAY) {
            initialDelay = passedTime;
        }

        final long animatedTime = passedTime - initialDelay;
        startedAnimation = animatedTime >= 0;

        if (startedAnimation && !terminated) {
            currentX = computeDistance(animatedTime, initialX, initialVelocityX, accelerationX,
                    millisToReachTargetVelocityX, targetVelocityX);
            currentY = computeDistance(animatedTime, initialY, initialVelocityY, accelerationY,
                    millisToReachTargetVelocityY, targetVelocityY);
            currentRotation = computeDistance(animatedTime, initialRotation,
                    initialRotationalVelocity, rotationalAcceleration,
                    millisToReachTargetRotationalVelocity, targetRotationalVelocity);
            currentScale = computeDistance(animatedTime, initialScale,
                    initialScaleVelocity, scaleAcceleration,
                    millisToReachTargetScaleVelocity, targetScaleVelocity);

            if (fadeOutInterpolator != null) {
                final float interpolatedTime =
                        fadeOutInterpolator.getInterpolation(animatedTime / millisToReachBound);
                currentAlpha = (int) (interpolatedTime * alpha * MAX_ALPHA);
            } else {
                currentAlpha = (int) (alpha * MAX_ALPHA);
            }

            if (scaleInterpolator != null) {
                final float interpolatedTime =
                        scaleInterpolator.getInterpolation(animatedTime / millisToReachBound);
                currentRealScale = currentScale * interpolatedTime;
            } else {
                currentRealScale = currentScale;
            }

            terminated = !touchOverride && animatedTime >= millisToReachBound;
            percentageAnimated = Math.min(1f, animatedTime / millisToReachBound);
        }

        return !terminated;
    }

    private float computeDistance(long t, float xi, float vi, float ai, Long targetTime,
            Float vTarget) {
        if (targetTime == null || t < targetTime) {
            // distance covered with linear acceleration
            // distance = xi + vi * t + 1/2 * a * t^2
            return xi + vi * t + 0.5f * ai * t * t;
        } else {
            // distance covered with linear acceleration + distance covered with max velocity
            // distance = xi + vi * targetTime + 1/2 * a * targetTime^2
            //     + (t - targetTime) * vTarget;
            return xi + vi * targetTime + 0.5f * ai * targetTime * targetTime
                    + (t - targetTime) * vTarget;
        }
    }

    /**
     * Primary method for rendering this confetto on the canvas.
     */
    public void draw(ConfettiRenderer.GLContext glContext) {
        if (touchOverride) {
            draw(glContext, overrideX + overrideDeltaX, overrideY + overrideDeltaY, currentRotation,
                    currentRealScale, percentageAnimated);
        } else if (startedAnimation && !terminated) {
            draw(glContext, currentX, currentY, currentRotation, currentRealScale, percentageAnimated);
        }
    }

    private void draw(ConfettiRenderer.GLContext glContext,
                      float x, float y, float rotation, float scale, float percentageAnimated) {
        drawImpl(glContext, x, y, rotation, scale, (float) currentAlpha / MAX_ALPHA);
    }

    /**
     * Subclasses need to override this method to optimize for the way to draw the appropriate
     * confetto on the canvas.
     * @param x the x position of the confetto relative to the canvas.
     * @param y the y position of the confetto relative to the canvas.
     * @param rotation the rotation (in degrees) to draw the confetto.
     * @param scale the scale (relative to original texture size) to draw the confetto.
     */
    protected abstract void drawImpl(ConfettiRenderer.GLContext drawingInfo,
                                     float x, float y, float rotation, float scale, float alpha);

    public abstract void writeSizeToBuffer(FloatBuffer buffer);
    public abstract void writeOffsetToBuffer(FloatBuffer buffer);
    public abstract void writeRotationToBuffer(FloatBuffer buffer);
    public abstract void writeScaleToBuffer(FloatBuffer buffer);
    public abstract int getTextureId();

    /**
     * Helper methods to set all of the necessary values for the confetto.
     */

    public void setInitialDelay(long val) {
        this.initialDelay = val;
    }

    public void setInitialX(float val) {
        this.initialX = val;
    }

    public void setInitialY(float val) {
        this.initialY = val;
    }

    public void setInitialVelocityX(float val) {
        this.initialVelocityX = val;
    }

    public void setInitialVelocityY(float val) {
        this.initialVelocityY = val;
    }

    public float getInitialVelocityScalar() {
        return (float) (1000f * Math.sqrt(initialVelocityX * initialVelocityX
                        + initialVelocityY * initialVelocityY));
    }

    public void setAccelerationX(float val) {
        this.accelerationX = val;
    }

    public void setAccelerationY(float val) {
        this.accelerationY = val;
    }

    public void setTargetVelocityX(Float val) {
        this.targetVelocityX = val;
    }

    public void setTargetVelocityY(Float val) {
        this.targetVelocityY = val;
    }

    public void setInitialRotation(float val) {
        this.initialRotation = val;
    }

    public void setInitialRotationalVelocity(float val) {
        this.initialRotationalVelocity = val;
    }

    public void setRotationalAcceleration(float val) {
        this.rotationalAcceleration = val;
    }

    public void setTargetRotationalVelocity(Float val) {
        this.targetRotationalVelocity = val;
    }

    public void setInitialScale(float val) {
        this.initialScale = val;
    }

    public void setInitialScaleVelocity(float val) {
        this.initialScaleVelocity = val;
    }

    public void setScaleAcceleration(float val) {
        this.scaleAcceleration = val;
    }

    public void setTargetScaleVelocity(Float val) {
        this.targetScaleVelocity = val;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setDestinationY(float destinationY) {
        this.destinationY = destinationY;
    }

    public void setDestinationX(float destinationX) {
        this.destinationX = destinationX;
    }

    public void setTTL(long val) {
        this.ttl = val;
    }

    public void setFadeOut(Interpolator fadeOutInterpolator) {
        this.fadeOutInterpolator = fadeOutInterpolator;
    }

    public void setScale(Interpolator scaleInterpolator) {
        this.scaleInterpolator = scaleInterpolator;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void setFromTouch(boolean fromTouch) {
        this.fromTouch = fromTouch;
    }
}

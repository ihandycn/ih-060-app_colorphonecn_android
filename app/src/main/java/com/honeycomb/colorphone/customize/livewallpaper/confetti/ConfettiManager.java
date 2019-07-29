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

package com.honeycomb.colorphone.customize.livewallpaper.confetti;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.support.v4.os.TraceCompat;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.honeycomb.colorphone.customize.livewallpaper.confetti.confetto.Confetto;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.render.ConfettiRenderer;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * A helper manager class for configuring a set of confetti and displaying them on the UI.
 */
public class ConfettiManager {

    public static final float DEVICE_INDEPENDENT_CORRECTION = Dimensions.getDensityRatio() / 3f;

    public static final int MAX_CONFETTO_COUNT = 512;
    static final long INFINITE_DURATION = Long.MAX_VALUE;
    private static final long NANOS_PER_MILLI = 1000L* 1000L;

    private static final String TRACE_TAG_DRAW = "ConfettiDraw";
    private static final String TRACE_TAG_UPDATE = "ConfettiUpdate";

    private static final float IDEAL_FPS = 60;
    private static final float CRITICAL_FPS = 25;
    private static final float FPS_FILTER_COEF = 0.1f;

    private final Random random = new Random();
    private final ConfettoGenerator confettoGenerator;
    private final ConfettiFilter confettoFilter;
    private volatile ConfettiSource confettiSource;

    private final Queue<Confetto> recycledConfetti = new LinkedList<>();
    private final List<Confetto> confetti = new ArrayList<>(300);
    private boolean isAnimating;
    private long animationStartTime;
    private long lastEmittedTimestamp;
    private float lastFps = IDEAL_FPS;

    // All of the below configured values are in milliseconds despite the setter methods take them
    // in seconds as the parameters. The parameters for the setters are in seconds to allow for
    // users to better understand/visualize the dimensions.

    // Configured attributes for the entire confetti group
    private int numInitialCount;
    private volatile long emissionDuration;
    private float emissionRate, emissionRateInverse;
    private Interpolator fadeOutInterpolator;
    private Interpolator scaleInterpolator;
    private Rect bound;

    // Configured attributes for each confetto
    private float delay, delayDeviation;
    private float velocityX, velocityDeviationX;
    private float velocityY, velocityDeviationY;
    private float accelerationX, accelerationDeviationX;
    private float accelerationY, accelerationDeviationY;
    private Float targetVelocityX, targetVelocityXDeviation;
    private Float targetVelocityY, targetVelocityYDeviation;
    private int initialRotation, initialRotationDeviation;
    private float rotationalVelocity, rotationalVelocityDeviation;
    private float rotationalAcceleration, rotationalAccelerationDeviation;
    private Float targetRotationalVelocity, targetRotationalVelocityDeviation;
    private float initialScale = 1f, initialScaleDeviation;
    private float scaleVelocity, scaleVelocityDeviation;
    private float scaleAcceleration, scaleAccelerationDeviation;
    private Float targetScaleVelocity, targetScaleVelocityDeviation;
    private float alpha, alphaDeviation;
    private float destinationY,destinationYDeviation;
    private float destinationX,destinationXDeviation;
    private long ttl;
    private boolean clickable;
    private boolean fromTouch;

    private ConfettiAnimationListener animationListener;

    ConfettiManager(ConfettoGenerator confettoGenerator,
                    ConfettiFilter confettiFilter,
                    ConfettiSource confettiSource,
                    int width, int height) {
        this.confettoGenerator = confettoGenerator;
        this.confettoFilter = confettiFilter;
        this.confettiSource = confettiSource;

        // Set the defaults
        this.ttl = -1;
        this.bound = new Rect(0, 0, width, height);
    }

    public void setConfettiSource(ConfettiSource confettiSource) {
        this.confettiSource = confettiSource;
    }

    /**
     * The number of confetti initially emitted before any time has elapsed.
     *
     * @param numInitialCount the number of initial confetti.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setNumInitialCount(int numInitialCount) {
        this.numInitialCount = numInitialCount;
        return this;
    }

    /**
     * Configures how long this manager will emit new confetti after the animation starts.
     *
     * @param emissionDurationInMillis how long to emit new confetti in millis. This value can be
     *   {@link #INFINITE_DURATION} for a never-ending emission.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setEmissionDuration(long emissionDurationInMillis) {
        this.emissionDuration = emissionDurationInMillis;
        return this;
    }

    /**
     * Configures how frequently this manager will emit new confetti after the animation starts
     * if {@link #emissionDuration} is a positive value.
     *
     * @param emissionRate the rate of emission in # of confetti per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setEmissionRate(float emissionRate) {
        this.emissionRate = emissionRate / 1000f;
        this.emissionRateInverse = 1f / this.emissionRate;
        return this;
    }

    public ConfettiManager setDelay(float delay, float delayDeviation) {
        this.delay = delay;
        this.delayDeviation = delayDeviation;
        return this;
    }

    /**
     * Set the velocityX used by this manager. This value defines the initial X velocity
     * for the generated confetti. The actual confetti's X velocity will be
     * (velocityX +- [0, velocityDeviationX]).
     *
     * @param velocityX the X velocity in pixels per second.
     * @param velocityDeviationX the deviation from X velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setVelocityX(float velocityX, float velocityDeviationX) {
        this.velocityX = velocityX / 1000f;
        this.velocityDeviationX = velocityDeviationX / 1000f;
        return this;
    }

    /**
     * Set the velocityY used by this manager. This value defines the initial Y velocity
     * for the generated confetti. The actual confetti's Y velocity will be
     * (velocityY +- [0, velocityDeviationY]). A positive Y velocity means that the velocity
     * is going down (because Y coordinate increases going down).
     *
     * @param velocityY the Y velocity in pixels per second.
     * @param velocityDeviationY the deviation from Y velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setVelocityY(float velocityY, float velocityDeviationY) {
        this.velocityY = velocityY / 1000f;
        this.velocityDeviationY = velocityDeviationY / 1000f;
        return this;
    }

    /**
     * Set the accelerationX used by this manager. This value defines the X acceleration
     * for the generated confetti. The actual confetti's X acceleration will be
     * (accelerationX +- [0, accelerationDeviationX]).
     *
     * @param accelerationX the X acceleration in pixels per second^2.
     * @param accelerationDeviationX the deviation from X acceleration in pixels per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setAccelerationX(float accelerationX, float accelerationDeviationX) {
        this.accelerationX = accelerationX / 1000000f;
        this.accelerationDeviationX = accelerationDeviationX / 1000000f;
        return this;
    }

    /**
     * Set the accelerationY used by this manager. This value defines the Y acceleration
     * for the generated confetti. The actual confetti's Y acceleration will be
     * (accelerationY +- [0, accelerationDeviationY]). A positive Y acceleration means that the
     * confetto will be accelerating downwards.
     *
     * @param accelerationY the Y acceleration in pixels per second^2.
     * @param accelerationDeviationY the deviation from Y acceleration in pixels per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setAccelerationY(float accelerationY, float accelerationDeviationY) {
        this.accelerationY = accelerationY / 1000000f;
        this.accelerationDeviationY = accelerationDeviationY / 1000000f;
        return this;
    }

    /**
     * Set the target X velocity that confetti can reach during the animation. The actual confetti's
     * target X velocity will be (targetVelocityX +- [0, targetVelocityXDeviation]).
     *
     * @param targetVelocityX the target X velocity in pixels per second.
     * @param targetVelocityXDeviation  the deviation from target X velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setTargetVelocityX(Float targetVelocityX,
            float targetVelocityXDeviation) {
        this.targetVelocityX = targetVelocityX == null ? null : targetVelocityX / 1000f;
        this.targetVelocityXDeviation = targetVelocityXDeviation / 1000f;
        return this;
    }

    /**
     * Set the target Y velocity that confetti can reach during the animation. The actual confetti's
     * target Y velocity will be (targetVelocityY +- [0, targetVelocityYDeviation]).
     *
     * @param targetVelocityY the target Y velocity in pixels per second.
     * @param targetVelocityYDeviation  the deviation from target Y velocity in pixels per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setTargetVelocityY(Float targetVelocityY,
            float targetVelocityYDeviation) {
        this.targetVelocityY = targetVelocityY == null ? null : targetVelocityY / 1000f;
        this.targetVelocityYDeviation = targetVelocityYDeviation / 1000f;
        return this;
    }

    /**
     * Set the initialRotation used by this manager. This value defines the initial rotation in
     * degrees for the generated confetti. The actual confetti's initial rotation will be
     * (initialRotation +- [0, initialRotationDeviation]).
     *
     * @param initialRotation the initial rotation in degrees.
     * @param initialRotationDeviation the deviation from initial rotation in degrees.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setInitialRotation(int initialRotation, int initialRotationDeviation) {
        this.initialRotation = initialRotation;
        this.initialRotationDeviation = initialRotationDeviation;
        return this;
    }

    /**
     * Set the rotationalVelocity used by this manager. This value defines the the initial
     * rotational velocity for the generated confetti. The actual confetti's initial
     * rotational velocity will be (rotationalVelocity +- [0, rotationalVelocityDeviation]).
     *
     * @param rotationalVelocity the initial rotational velocity in degrees per second.
     * @param rotationalVelocityDeviation the deviation from initial rotational velocity in
     *   degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setRotationalVelocity(float rotationalVelocity,
            float rotationalVelocityDeviation) {
        this.rotationalVelocity = rotationalVelocity / 1000f;
        this.rotationalVelocityDeviation = rotationalVelocityDeviation / 1000f;
        return this;
    }

    /**
     * Set the rotationalAcceleration used by this manager. This value defines the the
     * acceleration of the rotation for the generated confetti. The actual confetti's rotational
     * acceleration will be (rotationalAcceleration +- [0, rotationalAccelerationDeviation]).
     *
     * @param rotationalAcceleration the rotational acceleration in degrees per second^2.
     * @param rotationalAccelerationDeviation the deviation from rotational acceleration in degrees
     *   per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setRotationalAcceleration(float rotationalAcceleration,
            float rotationalAccelerationDeviation) {
        this.rotationalAcceleration = rotationalAcceleration / 1000000f;
        this.rotationalAccelerationDeviation = rotationalAccelerationDeviation / 1000000f;
        return this;
    }

    /**
     * Set the target rotational velocity that confetti can reach during the animation. The actual
     * confetti's target rotational velocity will be
     * (targetRotationalVelocity +- [0, targetRotationalVelocityDeviation]).
     *
     * @param targetRotationalVelocity the target rotational velocity in degrees per second.
     * @param targetRotationalVelocityDeviation the deviation from target rotational velocity
     *   in degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setTargetRotationalVelocity(Float targetRotationalVelocity,
            float targetRotationalVelocityDeviation) {
        this.targetRotationalVelocity = targetRotationalVelocity == null ?
                null : targetRotationalVelocity / 1000f;
        this.targetRotationalVelocityDeviation = targetRotationalVelocityDeviation / 1000f;
        return this;
    }

    /**
     * Set the initialRotation used by this manager. This value defines the initial rotation in
     * degrees for the generated confetti. The actual confetti's initial rotation will be
     * (initialRotation +- [0, initialRotationDeviation]).
     *
     * @param initialScale the initial scale.
     * @param initialScaleDeviation the deviation from initial scale.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setInitialScale(float initialScale, float initialScaleDeviation) {
        this.initialScale = initialScale;
        this.initialScaleDeviation = initialScaleDeviation;
        return this;
    }

    /**
     * Set the scaleVelocity used by this manager. This value defines the the initial
     * scale velocity for the generated confetti. The actual confetti's initial
     * scale velocity will be (scaleVelocity +- [0, scaleVelocityDeviation]).
     *
     * @param scaleVelocity the initial scale velocity in units per second.
     * @param scaleVelocityDeviation the deviation from initial scale velocity in
     *   degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setScaleVelocity(float scaleVelocity,
                                            float scaleVelocityDeviation) {
        this.scaleVelocity = scaleVelocity / 1000f;
        this.scaleVelocityDeviation = scaleVelocityDeviation / 1000f;
        return this;
    }

    /**
     * Set the scaleAcceleration used by this manager. This value defines the the
     * acceleration of the scale change for the generated confetti. The actual confetti's scale
     * acceleration will be (scaleAcceleration +- [0, scaleAccelerationDeviation]).
     *
     * @param scaleAcceleration the scale acceleration in units per second^2.
     * @param scaleAccelerationDeviation the deviation from scale acceleration.
     *   per second^2.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setScaleAcceleration(float scaleAcceleration,
                                                float scaleAccelerationDeviation) {
        this.scaleAcceleration = scaleAcceleration / 1000000f;
        this.scaleAccelerationDeviation = scaleAccelerationDeviation / 1000000f;
        return this;
    }

    /**
     * Set the target scale velocity that confetti can reach during the animation. The actual
     * confetti's target scale velocity will be
     * (targetScaleVelocity +- [0, targetScaleVelocityDeviation]).
     *
     * @param targetScaleVelocity the target scale velocity in degrees per second.
     * @param targetScaleVelocityDeviation the deviation from target scale velocity
     *   in degrees per second.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setTargetScaleVelocity(Float targetScaleVelocity,
                                                  float targetScaleVelocityDeviation) {
        this.targetScaleVelocity = targetScaleVelocity == null ? null : targetScaleVelocity / 1000f;
        this.targetScaleVelocityDeviation = targetScaleVelocityDeviation / 1000f;
        return this;
    }

    public ConfettiManager setAlpha(float alpha, float alphaDeviation) {
        this.alpha = alpha;
        this.alphaDeviation = alphaDeviation;
        return this;
    }

    public ConfettiManager setDestinationY(float destinationY, float destinationYDeviation) {
        this.destinationY = destinationY;
        this.destinationYDeviation = destinationYDeviation;
        return this;
    }

    public ConfettiManager setDestinationX(float destinationX, float destinationXDeviation) {
        this.destinationX = destinationX;
        this.destinationXDeviation = destinationXDeviation;
        return this;
    }

    public ConfettiManager setClickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    public ConfettiManager setFromTouchPoint(boolean fromTouchPoint) {
        this.fromTouch = fromTouchPoint;
        return this;
    }

    /**
     * Specifies a custom bound that the confetti will clip to. By default, the confetti will be
     * able to animate throughout the entire screen. The dimensions specified in bound is
     * global dimensions, e.g. x=0 is the top of the screen, rather than relative dimensions.
     *
     * @param bound the bound that clips the confetti as they animate.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setBound(Rect bound) {
        this.bound = bound;
        return this;
    }

    /**
     * Specifies a custom time to live for the confetti generated by this manager. When a confetti
     * reaches its time to live timer, it will disappear and terminate its animation.
     *
     * <p>The time to live value does not include the initial delay of the confetti.
     *
     * @param ttlInMillis the custom time to live in milliseconds.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setTTL(long ttlInMillis) {
        this.ttl = ttlInMillis;
        return this;
    }

    /**
     * Enables fade out for all of the confetti generated by this manager. Fade out means that
     * the confetti will animate alpha according to the fadeOutInterpolator according
     * to its TTL or, if TTL is not set, its bounds.
     *
     * @param fadeOutInterpolator an interpolator that interpolates animation progress [0, 1] into
     *   an alpha value [0, 1], 0 being transparent and 1 being opaque.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager enableFadeOut(Interpolator fadeOutInterpolator) {
        this.fadeOutInterpolator = fadeOutInterpolator;
        return this;
    }

    /**
     * Enables scale in and out for all of the confetti generated by this manager. Scale out means that
     * the confetti will animate scale according to the scaleInterpolator according
     * to its TTL or, if TTL is not set, its bounds.
     *
     * @param scaleInterpolator an interpolator that interpolates animation progress [0, 1] into
     *   an scale value [0, 1], 0 being size 0 and 1 being actual size.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager enableScale(Interpolator scaleInterpolator) {
        this.scaleInterpolator = scaleInterpolator;
        return this;
    }

    /**
     * Disables fade out for all of the confetti generated by this manager.
     *
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager disableFadeOut() {
        this.fadeOutInterpolator = null;
        return this;
    }

    /**
     * Sets a {@link ConfettiAnimationListener} for this confetti manager.
     *
     * @param listener the animation listener, or null to clear out the existing listener.
     * @return the confetti manager so that the set calls can be chained.
     */
    public ConfettiManager setConfettiAnimationListener(ConfettiAnimationListener listener) {
        this.animationListener = listener;
        return this;
    }

    /**
     * Start the confetti animation configured by this manager.
     *
     * @return the confetti manager itself that just started animating.
     */
    public ConfettiManager animate() {
        if (animationListener != null) {
            animationListener.onAnimationStart(this);
        }

        cleanupExistingAnimation();
        addNewConfetti(numInitialCount, 0);
        startNewAnimation();
        return this;
    }

    /**
     * Terminate the currently running animation if there is any.
     */
    public void terminate() {
        isAnimating = false;

        if (animationListener != null) {
            animationListener.onAnimationEnd(this);
        }
    }

    public boolean isTerminated() {
        return !isAnimating;
    }

    private void cleanupExistingAnimation() {
        isAnimating = false;

        lastEmittedTimestamp = 0;
        synchronized (confetti) {
        final Iterator<Confetto> iterator = confetti.iterator();
            while (iterator.hasNext()) {
                onRemoveConfetto(iterator.next());
                iterator.remove();
            }
        }
    }

    private void addNewConfetti(int numConfetti, long initialDelay) {
        for (int i = 0; i < numConfetti; i++) {
            Confetto confetto = recycledConfetti.poll();
            if (confetto == null) {
                confetto = confettoGenerator.generateConfetto(random);
                if (confetto == null) {
                    continue;
                }
            }
            int iterationCount = 0;
            do {
                iterationCount++;
                configureConfetto(confetto, confettiSource, random, initialDelay);
            } while (!confettoFilter.filter(confetto)
                    && iterationCount < confettoFilter.maxIterationCount());
            synchronized (confetti) {
                confetti.add(confetto);
            }
            onAddConfetto(confetto);
        }
    }

    private void startNewAnimation() {
        isAnimating = true;
        animationStartTime = System.nanoTime();
    }

    private boolean isUnderDeviationX(Confetto confetto, float target) {
        return Math.abs(confetto.getCurrentX() - target) <= confetto.getCurrentWidth() / 2;
    }

    private boolean isUnderDeviationY(Confetto confetto, float target) {
        return Math.abs(confetto.getCurrentY() - target) <= confetto.getCurrentHeight() / 2;
    }

    public void adjustStartTimeAfterPause(long pauseDuration) {
        animationStartTime += pauseDuration;
    }

    public Confetto touchAt(MotionEvent e) {
        synchronized (confetti) {
            Confetto find = null;
            final Iterator<Confetto> iterator = confetti.iterator();
            while (iterator.hasNext()) {
                Confetto confetto = iterator.next();
                if (confetto.isClickable()
                        && isUnderDeviationX(confetto, e.getX())
                        && isUnderDeviationY(confetto, e.getY())) {
                    confetto.terminate();
                    find = confetto;
                }
            }
            return find;
        }
    }

    public void seekFrames(long startTime, long seek) {
        int fps = 40;

        long seekStride = 1000 / fps * NANOS_PER_MILLI;
        for (long seekValue = 0; seekValue < seek; seekValue += seekStride) {
            final long elapsedTime = (startTime + seekValue - animationStartTime) / NANOS_PER_MILLI;
            processNewEmission(elapsedTime, fps);
            updateConfetti(elapsedTime);
        }
    }

    @SuppressLint("NewApi")
    public List<Confetto> onAnimationFrame(ConfettiRenderer.GLContext glContext,
                                           long frameNanoTime, int fps) {
        final long elapsedTime = (frameNanoTime - animationStartTime) / NANOS_PER_MILLI;
        if (isAnimating) {
            TraceCompat.beginSection(TRACE_TAG_UPDATE);
            try {
                // Take a copy to avoid being altered by a subsequent scheduleUpdate() call during update.
                processNewEmission(elapsedTime, fps);
                updateConfetti(elapsedTime);
            } finally {
                TraceCompat.endSection();
            }
        }
        if (confetti.size() == 0 && elapsedTime >= emissionDuration) {
            terminate();
        }

        TraceCompat.beginSection(TRACE_TAG_DRAW);
        try {
            for (int i = 0; i < confetti.size(); i++) {
                Confetto confetto = confetti.get(i);
                confetto.draw(glContext);
            }
        } finally {
            TraceCompat.endSection();
        }
        return confetti;
    }

    private void processNewEmission(long elapsedTime, int fps) {
        if (elapsedTime < emissionDuration) {
            if (lastEmittedTimestamp == 0) {
                lastEmittedTimestamp = elapsedTime;
            } else {
                final long timeSinceLastEmission = elapsedTime - lastEmittedTimestamp;

                // Randomly determine how many confetti to emit
                float fpsSaver = adjustEmissionForCurrentFps(fps);
                int numNewConfetti = (int)
                        (random.nextFloat() * fpsSaver * emissionRate * timeSinceLastEmission);
                if (confetti.size() + numNewConfetti > MAX_CONFETTO_COUNT) {
                    numNewConfetti = MAX_CONFETTO_COUNT - confetti.size();
                }
                if (numNewConfetti > 0) {
                    lastEmittedTimestamp += emissionRateInverse * numNewConfetti;
                    addNewConfetti(numNewConfetti, elapsedTime);
                }
            }
        }
    }

    private float adjustEmissionForCurrentFps(int fps) {
        float filteredFps = FPS_FILTER_COEF * fps + (1f - FPS_FILTER_COEF) * lastFps;
        lastFps = filteredFps;
        if (filteredFps >= IDEAL_FPS) {
            return 1f;
        }
        if (filteredFps <= CRITICAL_FPS) {
            return 0f;
        }
        return (filteredFps - CRITICAL_FPS) / (IDEAL_FPS - CRITICAL_FPS);
    }

    private void updateConfetti(long elapsedTime) {
        final Iterator<Confetto> iterator = confetti.iterator();
        while (iterator.hasNext()) {
            final Confetto confetto = iterator.next();
            if (!confetto.applyUpdate(elapsedTime)) {
                synchronized (confetti) {
                    iterator.remove();
                }
                onRemoveConfetto(confetto);
            }
        }
    }

    private void onAddConfetto(Confetto confetto) {
        if (animationListener != null) {
            animationListener.onConfettoEnter(confetto);
        }
    }

    private void onRemoveConfetto(Confetto confetto) {
        if (this.animationListener != null) {
            this.animationListener.onConfettoExit(confetto);
        }
        recycledConfetti.add(confetto);
    }

    private void configureConfetto(Confetto confetto, ConfettiSource confettiSource,
            Random random, long initialDelay) {
        confetto.reset();

        confetto.setInitialDelay((long) (initialDelay + getVarianceAmount(delay, delayDeviation, random)));

        confetto.setInitialX(confettiSource.getInitialX(random));
        confetto.setInitialY(confettiSource.getInitialY(random));
        confetto.setInitialVelocityX(getVarianceAmount(velocityX, velocityDeviationX, random));
        confetto.setInitialVelocityY(getVarianceAmount(velocityY, velocityDeviationY, random));
        confetto.setAccelerationX(getVarianceAmount(accelerationX, accelerationDeviationX, random));
        confetto.setAccelerationY(getVarianceAmount(accelerationY, accelerationDeviationY, random));
        confetto.setTargetVelocityX(targetVelocityX == null ? null
                : getVarianceAmount(targetVelocityX, targetVelocityXDeviation, random));
        confetto.setTargetVelocityY(targetVelocityY == null ? null
                : getVarianceAmount(targetVelocityY, targetVelocityYDeviation, random));

        confetto.setInitialRotation(
                getVarianceAmount(initialRotation, initialRotationDeviation, random));
        confetto.setInitialRotationalVelocity(
                getVarianceAmount(rotationalVelocity, rotationalVelocityDeviation, random));
        confetto.setRotationalAcceleration(
                getVarianceAmount(rotationalAcceleration, rotationalAccelerationDeviation, random));
        confetto.setTargetRotationalVelocity(targetRotationalVelocity == null ? null
                : getVarianceAmount(targetRotationalVelocity, targetRotationalVelocityDeviation,
                        random));

        confetto.setInitialScale(
                getVarianceAmount(initialScale, initialScaleDeviation, random));
        confetto.setInitialScaleVelocity(
                getVarianceAmount(scaleVelocity, scaleVelocityDeviation, random));
        confetto.setScaleAcceleration(
                getVarianceAmount(scaleAcceleration, scaleAccelerationDeviation, random));
        confetto.setTargetScaleVelocity(targetScaleVelocity == null ? null
                : getVarianceAmount(targetScaleVelocity, targetScaleVelocityDeviation,
                random));
        confetto.setAlpha(getVarianceAmount(alpha, alphaDeviation, random));
        confetto.setDestinationY(getVarianceAmount(destinationY, destinationYDeviation, random));
        confetto.setDestinationX(getVarianceAmount(destinationX, destinationXDeviation, random));

        confetto.setTTL(ttl);
        confetto.setFadeOut(fadeOutInterpolator);
        confetto.setScale(scaleInterpolator);
        confetto.setClickable(clickable);
        confetto.setFromTouch(fromTouch);

        confetto.prepare(bound);
    }

    public static float getVarianceAmount(float base, float deviation, Random random) {
        // Normalize random to be [-1, 1] rather than [0, 1]
        return base + (deviation * (random.nextFloat() * 2 - 1));
    }

    public interface ConfettiAnimationListener {
        void onAnimationStart(ConfettiManager confettiManager);
        void onAnimationEnd(ConfettiManager confettiManager);
        void onConfettoEnter(Confetto confetto);
        void onConfettoExit(Confetto confetto);
    }

    public static class ConfettiAnimationListenerAdapter implements ConfettiAnimationListener {
        @Override public void onAnimationStart(ConfettiManager confettiManager) {}
        @Override public void onAnimationEnd(ConfettiManager confettiManager) {}
        @Override public void onConfettoEnter(Confetto confetto) {}
        @Override public void onConfettoExit(Confetto confetto) {}
    }
}

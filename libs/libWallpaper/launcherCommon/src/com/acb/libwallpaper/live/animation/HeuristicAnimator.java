package com.acb.libwallpaper.live.animation;

import android.os.Looper;
import android.os.SystemClock;
import android.util.AndroidRuntimeException;
import android.view.Choreographer;

import com.acb.libwallpaper.live.util.Thunk;
import com.acb.libwallpaper.BuildConfig;
import com.ihs.commons.utils.HSLog;

/**
 * An value animator for implementing smooth progress bar animation with multiple intermediate
 * progress callback, but each at unpredictable time. This class is thread safe.
 */
public class HeuristicAnimator {

    public interface AnimatorListener {
        void onAnimationStart(HeuristicAnimator animation);

        void onAnimationEnd(HeuristicAnimator animation);
    }

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(HeuristicAnimator animation);
    }

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_VERBOSE = false && BuildConfig.DEBUG;

    private static final float HUNDRED_PERCENT = 0.995f;

    @Thunk AnimatorListener mListener;
    @Thunk AnimatorUpdateListener mUpdateListener;

    private final float mStartValue;
    private final float mEndValue;
    private final long mInitialEstimatedDuration;

    private long mStartTime;
    private Interval mCurrentInterval;
    private boolean mEndValuePosted;
    private float mConstringencyRatio = 1;

    @Thunk final Choreographer mChoreographer;

    private final Object mStateLock = new Object();

    /**
     * @param initialEstimatedDuration An estimation of total animation duration. Animation progress before the first
     *                                 call to {@link #postValue(float)} would depends on this.
     */
    public HeuristicAnimator(float startValue, float endValue, long initialEstimatedDuration) {
        mStartValue = startValue;
        mEndValue = endValue;
        mInitialEstimatedDuration = initialEstimatedDuration;

        mChoreographer = Choreographer.getInstance();
    }

    public void setListener(AnimatorListener listener) {
        mListener = listener;
    }

    public void setUpdateListener(AnimatorUpdateListener listener) {
        mUpdateListener = listener;
    }

    /**
     * Start the animation.
     */
    public void start() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        synchronized (mStateLock) {
            mCurrentInterval = new Interval(mStartValue, mEndValue, mInitialEstimatedDuration);
            mStartTime = mCurrentInterval.startTime;
        }
        mChoreographer.postFrameCallback(mUpdateCallback);
    }

    public void cancel() {
        mChoreographer.removeFrameCallback(mUpdateCallback);
    }

    /**
     * Post an intermediate value in [start value, end value] as a heuristic of progress. Animation speed would change
     * by values posted during animation. Post a value close enough to end value for the last call, or the animation
     * will never ends.
     *
     * @param current The intermediate value.
     */
    public void postValue(float current) {
        if (current < mStartValue || current > mEndValue) {
            throw new IllegalArgumentException("Posted value out of interval [start value, end value].");
        }

        long currentTime = SystemClock.uptimeMillis();
        long timeSinceStart = currentTime - mStartTime;
        long estimatedTotalTime = (long) ((mEndValue - mStartValue) / ((current - mStartValue) / timeSinceStart));
        boolean isEndValue = isEndValue(current);
        float animatedValue = getAnimatedValue();
        long remainingTime;
        if (!isEndValue) {
            remainingTime = estimatedTotalTime - timeSinceStart;
        } else {
            remainingTime = (long) (estimatedTotalTime * ((mEndValue - animatedValue) / (mEndValue - mStartValue)));
        }
        synchronized (mStateLock) {
            mEndValuePosted = isEndValue;
            mCurrentInterval.set(animatedValue, mEndValue, remainingTime);
        }
    }

    public void setConstringencyRatio(float ratio) {
        this.mConstringencyRatio = ratio;
    }

    /**
     * @return Animated value at time of invocation. If it has passed animation end time, this method would return the
     * end value.
     */
    public float getAnimatedValue() {
        long currentTime = SystemClock.uptimeMillis();
        float value;
        synchronized (mStateLock) {
            long timeInInterval = currentTime - mCurrentInterval.startTime;
            if (mEndValuePosted) {
                if (mCurrentInterval.estimatedDuration > 0) {
                    // Linear interpolation
                    value = mCurrentInterval.startValue +
                            mCurrentInterval.getDifference() * (float) timeInInterval / mCurrentInterval.estimatedDuration;
                } else {
                    value = mCurrentInterval.endValue;
                }
            } else {
                // Exponential deceleration
                value = (float) (mCurrentInterval.startValue + mCurrentInterval.getDifference()
                        * (1f - Math.exp(-(float) timeInInterval / mCurrentInterval.estimatedDuration)));
                if (mConstringencyRatio < 1
                        && value > (mCurrentInterval.startValue + mCurrentInterval.getDifference() * 0.9f)) {
                    value = mCurrentInterval.startValue + mCurrentInterval.getDifference() * 0.9f;
                }
            }
            if (value > mCurrentInterval.endValue) {
                // Caller may invoke this method after animation end time, so we need to clamp
                value = mCurrentInterval.endValue;
            }
            if (DEBUG_VERBOSE) {
                HSLog.v("HeuristicAnimator", "Interval: " + mCurrentInterval + ", timeInInterval: " + timeInInterval +
                        ", mEndValuePosted " + mEndValuePosted + ", value: " + value);
            }
        }
        return value;
    }

    private Choreographer.FrameCallback mUpdateCallback = new Choreographer.FrameCallback() {
        private boolean mStartCallbackInvoked;

        @Override
        public void doFrame(long frameTimeNanos) {
            if (!mStartCallbackInvoked && mListener != null) {
                mStartCallbackInvoked = true;
                mListener.onAnimationStart(HeuristicAnimator.this);
            }
            if (isAnimationEnd()) {
                if (mUpdateListener != null) {
                    // Ensure to invoke update listener with completely end state once
                    mUpdateListener.onAnimationUpdate(HeuristicAnimator.this);
                }
                if (mListener != null) {
                    mListener.onAnimationEnd(HeuristicAnimator.this);
                }
            } else {
                if (mUpdateListener != null) {
                    mUpdateListener.onAnimationUpdate(HeuristicAnimator.this);
                }
                mChoreographer.postFrameCallback(this);
            }
        }
    };

    private boolean isAnimationEnd() {
        return mEndValuePosted
                && SystemClock.uptimeMillis() - mCurrentInterval.startTime >= mCurrentInterval.estimatedDuration;
    }

    private boolean isEndValue(float value) {
        return (value - mStartValue) / (mEndValue - mStartValue) >= HUNDRED_PERCENT;
    }

    private static class Interval {
        long startTime;
        long estimatedDuration;
        float startValue;
        float endValue;

        Interval(float startValue, float endValue, long estimatedDuration) {
            set(startValue, endValue, estimatedDuration);
        }

        private void set(float startValue, float endValue, long estimatedDuration) {
            this.startTime = SystemClock.uptimeMillis();
            this.estimatedDuration = estimatedDuration;
            this.startValue = startValue;
            this.endValue = endValue;
        }

        float getDifference() {
            return endValue - startValue;
        }

        @Override
        public String toString() {
            return "startTime: " + startTime + ", estimatedDuration: " + estimatedDuration +
                    ", startValue: " + startValue + ", endValue: " + endValue;
        }
    }
}

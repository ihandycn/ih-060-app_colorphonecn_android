package com.honeycomb.colorphone.wallpaper.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;

import com.honeycomb.colorphone.util.Thunk;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.animation.LauncherAnimUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

/**
 * Custom progress bar.
 */
public class LauncherProgressBar extends View {

    private static final float RATIO_POINT_RADIUS_TO_DISTANCE = 0.23f;
    private static final float RATIO_AMPLITUDE_TO_POINT_RADIUS = 3.3f;

    public static final long DURATION_BOUNCE_CYCLE = 900;
    private static final long DURATION_FADE_IN_OUT = 230;

    private static final float TIME_RATIO_BOUNCE = 0.72f;
    private static final float TIME_RATIO_ALPHA = 0.25f;

    private final float mPointDistance;
    private final float mPointRadius;
    private final int[] mPointColors;
    private final float mAmplitude;

    private ValueAnimator mBounceAnimator;
    private ValueAnimator mAlphaAnimator;

    // Reused arrays
    @Thunk float[] mAnimateProgresses = new float[3];
    @Thunk float[] mOffsetYs = new float[3];
    @Thunk float[] mAlphas = new float[3];

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean mIsRtl;

    @Thunk TimeInterpolator mBounceInterpolator = new TimeInterpolator() {
        @Override
        public float getInterpolation(float input) {
            if (input <= 0.5f) {
                return LauncherAnimUtils.ACCELERATE_QUAD.getInterpolation(input * 2f);
            } else {
                return 1f - LauncherAnimUtils.DECELERATE_QUAD.getInterpolation((input - 0.5f) * 2f);
            }
        }
    };

    private enum State {
        INVISIBLE,
        FADING_IN,
        RUNNING,
        FADING_OUT,
    }

    private State mState = State.INVISIBLE;

    public LauncherProgressBar(Context context) {
        this(context, null);
    }

    public LauncherProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LauncherProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LauncherProgressBar, defStyleAttr, 0);
        final int pointColorsId = a.getResourceId(R.styleable.LauncherProgressBar_colorArray,
                R.array.progress_bar_points_default);
        mPointDistance = a.getDimensionPixelSize(R.styleable.LauncherProgressBar_pointDistance,
                res.getDimensionPixelSize(R.dimen.progress_bar_point_distance_default));
        a.recycle();

        mPointRadius = mPointDistance * RATIO_POINT_RADIUS_TO_DISTANCE;
        mAmplitude = mPointRadius * RATIO_AMPLITUDE_TO_POINT_RADIUS;

        mPointColors = res.getIntArray(pointColorsId);
        if (mPointColors.length != 3) {
            throw new InflateException("Exactly 3 point colors must be provided.");
        }
        mPaint.setStyle(Paint.Style.FILL);

        mIsRtl = Dimensions.isRtl();
        initAnimators();
    }

    private void initAnimators() {
        mBounceAnimator = LauncherAnimUtils.ofFloat(this, 0f, 1f);
        mBounceAnimator.setDuration(DURATION_BOUNCE_CYCLE);
        mBounceAnimator.setInterpolator(LauncherAnimUtils.LINEAR);
        mBounceAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mBounceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cycleProgress = animation.getAnimatedFraction();
                calculateAnimationProgresses(cycleProgress, TIME_RATIO_BOUNCE);
                for (int i = 0; i < 3; i++) {
                    mOffsetYs[i] = mAmplitude * mBounceInterpolator.getInterpolation(mAnimateProgresses[i]);
                }
                invalidate();
            }
        });

        mAlphaAnimator = LauncherAnimUtils.ofFloat(this, 0f, 1f);
        mAlphaAnimator.setDuration(DURATION_FADE_IN_OUT);
        mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = animation.getAnimatedFraction();
                calculateAnimationProgresses(progress, TIME_RATIO_ALPHA);
                for (int i = 0; i < 3; i++) {
                    mAlphas[i] = mState == State.FADING_IN ? mAnimateProgresses[i] : (1f - mAnimateProgresses[i]);
                }
                invalidate();
            }
        });
    }

    public boolean isRunning() {
        return mState == State.FADING_IN || mState == State.RUNNING;
    }

    public void fadeIn() {
        if (isRunning()) {
            return;
        }
        mState = State.FADING_IN;

        // Start bouncing
        if (!mBounceAnimator.isRunning()) {
            mBounceAnimator.start();
        }

        if (mAlphaAnimator.isRunning()) {
            mAlphaAnimator.cancel();
        }
        mAlphaAnimator.start();
        mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAlphaAnimator.removeListener(this);
                mState = State.RUNNING;
            }
        });
    }

    public void fadeOut() {
        if (mState == State.FADING_OUT || mState == State.INVISIBLE) {
            return;
        }
        mState = State.FADING_OUT;

        if (mAlphaAnimator.isRunning()) {
            mAlphaAnimator.cancel();
        }
        mAlphaAnimator.start();
        mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAlphaAnimator.removeListener(this);
                if (mState == State.FADING_OUT) {
                    mState = State.INVISIBLE;

                    // Stop bouncing
                    if (mBounceAnimator.isRunning()) {
                        mBounceAnimator.cancel();
                    }
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
                || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            HSLog.w("Warning: exact size of LauncherProgressBar is neglected, set pointDistance instead");
        }

        int width = (int) Math.ceil(2f * (mPointDistance + mPointRadius));
        int height = (int) Math.ceil(2f * mPointRadius + mAmplitude);
        setMeasuredDimension(width, height);
    }

    /**
     * Map      overallProgress = 0.3              sectionRatio = (length of overall period) / (length of one section)
     *                 |
     *                 V
     *     |-----------------------------------|
     *                 |
     * to              V
     *     |--------------|                         mAnimateProgress[0] = 0.8
     *                 |
     *                 V
     *               |---------------|              mAnimateProgress[1] = 0.1
     *                 |
     *                 V
     *                          |--------------|    mAnimateProgress[2] = 0.0
     */
    private void calculateAnimationProgresses(float overallProgress, float sectionRatio) {
        // First point
        if (overallProgress <= sectionRatio) {
            mAnimateProgresses[0] = overallProgress / sectionRatio;
        } else {
            mAnimateProgresses[0] = 1f;
        }

        // Second point
        if (overallProgress <= 0.5f - sectionRatio / 2f) {
            mAnimateProgresses[1] = 0f;
        } else if (overallProgress <= 0.5 + sectionRatio / 2f) {
            mAnimateProgresses[1] = 0.5f + (overallProgress - 0.5f) * (1f / sectionRatio);
        } else {
            mAnimateProgresses[1] = 1f;
        }

        // Third point
        if (overallProgress > 1 - sectionRatio) {
            mAnimateProgresses[2] = 1f - (1f - overallProgress) / sectionRatio;
        } else {
            mAnimateProgresses[2] = 0f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 3; i++) {
            mPaint.setColor(mPointColors[i]);
            mPaint.setAlpha((int) (0xff * mAlphas[i]));
            if (mIsRtl) {
                canvas.drawCircle(getMeasuredWidth() - mPointRadius - i * mPointDistance,
                        mPointRadius + mOffsetYs[i], mPointRadius, mPaint);
            } else {
                canvas.drawCircle(mPointRadius + i * mPointDistance, mPointRadius + mOffsetYs[i], mPointRadius, mPaint);
            }
        }
    }
}

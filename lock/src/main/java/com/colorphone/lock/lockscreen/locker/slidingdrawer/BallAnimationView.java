package com.colorphone.lock.lockscreen.locker.slidingdrawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.colorphone.lock.R;
import com.superapps.util.Dimensions;


public class BallAnimationView extends LinearLayout {

    private static final int PROGRESS_GREEN = 40;
    private static final int PROGRESS_ORANGE = 80;

    private int mProgress;
    private int red;
    private int orange;
    private int green;

    private BallWaveView mWave;

    private static final int DEFAULT_PROGRESS = 80;

    public BallAnimationView(Context context) {
        this(context, null);
    }

    public BallAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallAnimationView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, Dimensions.pxFromDp(3), 1f, 0.1f, Color.RED);
    }

    @SuppressWarnings("deprecation")
    private BallAnimationView(Context context, AttributeSet attrs, int defStyle, int waveHeight, float waveMultiple, float waveHz, int waveColor) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);

        red = getResources().getColor(R.color.clean_app_red);
        orange = getResources().getColor(R.color.clean_app_orange);
        green = getResources().getColor(R.color.clean_app_green);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

        mProgress = DEFAULT_PROGRESS;

        mWave = new BallWaveView(context, null);
        mWave.initializeWaveSize(waveHeight, waveMultiple, waveHz);
        mWave.setWaveColor(waveColor);
        mWave.initializePainters();
        mWave.startWaveAnimation();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mWave.stopWaveAnimation();
            }
        }, 1000);

        addView(mWave);

        setProgress(mProgress);
    }

    public void setProgress(int progress, int... animationStartEnd) {
        int color;
        if (animationStartEnd.length == 0) {
            color = computeColorDiscrete(progress);
        } else {
            color = computeColorGradient(progress, animationStartEnd[0], animationStartEnd[1]);
        }

        mProgress = progress > 100 ? 100 : progress;
        mWave.setProgress(progress);
        mWave.setColor(color);

        computeWaveToTop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            computeWaveToTop();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    private void computeWaveToTop() {
        int mWaveToTop = (int) (getHeight() * (1f - mProgress / 100f));
        ViewGroup.LayoutParams params = mWave.getLayoutParams();
        if (params != null) {
            ((LayoutParams) params).topMargin = mWaveToTop;
        }
        mWave.setLayoutParams(params);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.progress = mProgress;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.progress);
    }

    private int computeColorDiscrete(int progress) {
        if (progress < PROGRESS_GREEN) {
            return green;
        } else if (progress < PROGRESS_ORANGE) {
            return orange;
        } else {
            return red;
        }
    }

    /**
     * Calculate corresponding color.
     * Gradient color from start color to end color as ram usage raise up.
     */
    private int computeColorGradient(int progress, int startProgress, int endProgress) {
        int startColor = computeColorDiscrete(startProgress);
        int endColor = computeColorDiscrete(endProgress);
        if (startColor == endColor) {
            return startColor;
        } else {
            if (startColor == red && endColor == green) {
                if (progress > (startProgress + endProgress) / 2) {
                    endColor = orange;
                } else {
                    startColor = orange;
                }
            }
            return Color.rgb(
                    Color.red(startColor) * (progress - endProgress) / (startProgress - endProgress) + Color.red(endColor) * (startProgress - progress) / (startProgress - endProgress),
                    Color.green(startColor) * (progress - endProgress) / (startProgress - endProgress) + Color.green(endColor) * (startProgress - progress) / (startProgress - endProgress),
                    Color.blue(startColor) * (progress - endProgress) / (startProgress - endProgress) + Color.blue(endColor) * (startProgress - progress) / (startProgress - endProgress)
            );
        }
    }

    private static class SavedState extends BaseSavedState {

        int progress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public BallAnimationView.SavedState createFromParcel(Parcel in) {
                return new BallAnimationView.SavedState(in);
            }

            public BallAnimationView.SavedState[] newArray(int size) {
                return new BallAnimationView.SavedState[size];
            }
        };
    }
}

package colorphone.acb.com.libweather.background;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.animation.OvershootInterpolator;

import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.animation.DeviceEvaluator;

public abstract class BaseWeatherAnimBackground {

    protected WeatherAnimView mView;
    protected DrawMode mMode = DrawMode.INITIALIZATION;
    ValueAnimator mBeginAnimator;
    ValueAnimator mCycleAnimator;
    float mAlpha = 1f;
    protected final Point mScreenSize;
    protected Paint mPaint;

    private boolean mEnabled;

    enum DrawMode {
        INITIALIZATION,
        BEGIN_ANIMATION,
        CYCLE_ANIMATION,
    }

    public BaseWeatherAnimBackground(WeatherAnimView view) {
        mView = view;

        // Disable this animation for POOR devices to keep usability.
        mEnabled = (DeviceEvaluator.getEvaluation() >= DeviceEvaluator.DEVICE_EVALUATION_BELOW_AVERAGE);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mScreenSize = new Point(Dimensions.getPhoneWidth(view.getContext()), Dimensions.getPhoneHeight(view.getContext()));

        mBeginAnimator = ValueAnimator.ofFloat(0f, 1f);
        mBeginAnimator.setInterpolator(new OvershootInterpolator());
        mBeginAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startCycleAnimation();
            }
        });

        mCycleAnimator = ValueAnimator.ofFloat(0f, 1f);
        mCycleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mCycleAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    public void onDraw(Canvas canvas) {
        if (!mEnabled || mMode == null || Float.compare(mAlpha,0) == 0) {
            return;
        }

        switch (mMode) {
            case INITIALIZATION:
                drawInitializationFrame(canvas);
                break;
            case BEGIN_ANIMATION:
                drawBeginAnimationFrame(canvas);
                break;
            case CYCLE_ANIMATION:
                drawCycleAnimationFrame(canvas);
                break;
        }
    }

    public boolean shouldFadeOutOnVerticalScroll() {
        return true;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
        mPaint.setAlpha((int) (0xFF * alpha));
    }

    public void reset() {
        HSLog.d("AnimationLeak", "Reset background animation: " + this);
        if(mBeginAnimator.isRunning()) {
            mBeginAnimator.cancel();
        }
        if(mCycleAnimator.isRunning()) {
            HSLog.d("AnimationLeak", "Cancel cycle animator: " + this);
            mCycleAnimator.cancel();
        }
        mMode = DrawMode.INITIALIZATION;
    }

    public void startBeginAnimation() {
        mMode = DrawMode.BEGIN_ANIMATION;
    }

    public void startCycleAnimation() {
        mMode = DrawMode.CYCLE_ANIMATION;
        if (mCycleAnimator.isRunning()) {
            mCycleAnimator.cancel();
        }
        mCycleAnimator.start();
    }

    public abstract void drawInitializationFrame(Canvas canvas);

    public abstract void drawBeginAnimationFrame(Canvas canvas);

    public abstract void drawCycleAnimationFrame(Canvas canvas);
}

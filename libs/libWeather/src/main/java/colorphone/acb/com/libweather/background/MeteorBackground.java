package colorphone.acb.com.libweather.background;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.animation.AccelerateInterpolator;

import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.util.Random;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;

public class MeteorBackground extends BaseWeatherAnimBackground {

    private static final int TOP_RANGE = Dimensions.pxFromDp(100f);
    private static final int BOTTOM_RANGE = Dimensions.pxFromDp(300f);
    private static final int DEGREE = 60;

    private Bitmap mMeteorBitmap;
    private float mAnimProgress;
    private Matrix mMatrix = new Matrix();
    private float mMeteorWidth;
    private float mMeteorHeightSin;
    private float mMeteorHeightCos;
    private Random mRandom;
    private float mStartX;
    private float mStartY = TOP_RANGE;
    private float mEndX;
    private float mEndY;
    private boolean shouldRepeat = false;

    public MeteorBackground(WeatherAnimView view) {
        super(view);

        mMeteorBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_fallingstar);
        mMeteorWidth = mMeteorBitmap.getWidth();
        float meteorHeight = mMeteorBitmap.getHeight();
        mMeteorHeightSin = (float) (meteorHeight * Math.sin(Math.toRadians(DEGREE)));
        mMeteorHeightCos = (float) (meteorHeight * Math.cos(Math.toRadians(DEGREE)));
        mEndY = BOTTOM_RANGE + mMeteorHeightCos;
        mRandom = new Random();

        mCycleAnimator.setRepeatCount(0);
        mCycleAnimator.setInterpolator(new AccelerateInterpolator());
        mCycleAnimator.setDuration(700);
        mCycleAnimator.setStartDelay(3000);
        mCycleAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                float xRandomRatio = mRandom.nextFloat() * 0.5f + 0.5f;
                float yRandomRatio = mRandom.nextFloat();
                mStartX = mScreenSize.x * xRandomRatio;
                mStartY = TOP_RANGE + (BOTTOM_RANGE - TOP_RANGE) / 4 * yRandomRatio;
                mEndX = (float) (mStartX - (mEndY - mStartY) * Math.tan(Math.toRadians(DEGREE)));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (shouldRepeat) {
                    int delayTime = (mRandom.nextInt(3) + 3) * 1000;
                    animation.setStartDelay(delayTime);
                    animation.start();
                }
            }
        });
        mCycleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!shouldRepeat) {
                    animation.cancel();
                    mAnimProgress = 0;
                } else {
                    mAnimProgress = (float) animation.getAnimatedValue();
                    mView.invalidate();
                }
            }
        });

    }

    @Override
    public void reset() {
        shouldRepeat = false;
        super.reset();
        mAnimProgress = 0;
    }

    @Override
    public void startBeginAnimation() {
        HSLog.d("MeteorBackground", "startBeginAnimation");
        shouldRepeat = true;
        startCycleAnimation();
    }

    @Override
    public void drawInitializationFrame(Canvas canvas) {
        //Empty
    }

    @Override
    public void drawBeginAnimationFrame(Canvas canvas) {
        //Empty
    }

    @Override
    public void drawCycleAnimationFrame(Canvas canvas) {
        mMatrix.reset();
        float transX = mStartX - (mStartX - mEndX) * mAnimProgress;
        float transY = mStartY + (mEndY - mStartY) * mAnimProgress;
        if (shouldDraw(transX, transY)) {
            mMatrix.setTranslate(transX, transY);
            mMatrix.postRotate(DEGREE, transX + (mMeteorWidth / 2), transY);
            float alpha = 1 - Math.abs(mAnimProgress - 0.5f) / 0.5f;
            mPaint.setAlpha((int) (0xFF * alpha * mAlpha));
            if (!mMeteorBitmap.isRecycled()) {
                canvas.drawBitmap(mMeteorBitmap, mMatrix, mPaint);
            }
        }
    }

    private boolean shouldDraw(float topX, float topY) {
        float bottomX = topX - mMeteorHeightSin;
        float bottomY = topY + mMeteorHeightCos;
        if ((topX > 0 && topX < mScreenSize.x) && (topY > TOP_RANGE && topY < BOTTOM_RANGE)) {
            // Top point is in display rect
            return true;
        }
        return (bottomX > 0 && bottomX < mScreenSize.x)
                && (bottomY > TOP_RANGE && bottomY < BOTTOM_RANGE);
    }
}

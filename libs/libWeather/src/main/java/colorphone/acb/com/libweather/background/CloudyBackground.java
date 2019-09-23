package colorphone.acb.com.libweather.background;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.superapps.util.Dimensions;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;

public class CloudyBackground extends BaseWeatherAnimBackground {

    private Bitmap mLeftCloud;
    private Bitmap mMidCloud;
    private Bitmap mRightCloud;

    private int mLeftTop = Dimensions.pxFromDp(230f);
    private int mMidTop = Dimensions.pxFromDp(230f);
    private int mRightTop = Dimensions.pxFromDp(180f);

    private final int mLeftStartX;
    private final int mLeftBaseX;
    private final int mMidBaseX;
    private final int mRightBaseX;

    private int mTempLeftX;
    private int mTempMidX;
    private int mTempRightX;

    private int mOffset = Dimensions.pxFromDp(30f);

    public CloudyBackground(WeatherAnimView view) {
        super(view);

        mLeftCloud = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_cloudy_bg_cloud_left);
        mMidCloud = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_cloudy_bg_cloud_middle);
        mRightCloud = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_cloudy_bg_cloud_right);

        mLeftStartX = (int) (-0.3f * mScreenSize.x);
        mLeftBaseX = (int) (0.5f * mScreenSize.x);
        mMidBaseX = (int) (0.45f * mScreenSize.x);
        mRightBaseX = (int) (0.53f * mScreenSize.x);

        mBeginAnimator.setDuration(2300);
        mBeginAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mBeginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                mTempLeftX = (int) (mLeftStartX + animatedValue * (mLeftBaseX - mLeftStartX));
                mTempMidX = (int) (animatedValue * mMidBaseX);
                mTempRightX = (int) (mRightBaseX + (1 - animatedValue) * mOffset);
                mView.invalidate();
            }
        });

        mCycleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mCycleAnimator.setDuration(2000);
        mCycleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                mTempLeftX = (int) (mLeftBaseX - mOffset * animatedValue);
                mTempMidX = (int) (mMidBaseX - mOffset * animatedValue);
                mTempRightX = (int) (mRightBaseX + mOffset * animatedValue);
                mView.invalidate();
            }
        });
    }

    @Override
    public void startBeginAnimation() {
        super.startBeginAnimation();
        if (mBeginAnimator.isRunning()) {
            mBeginAnimator.cancel();
        }
        mBeginAnimator.start();
    }

    @Override
    public void drawInitializationFrame(Canvas canvas) {
        if (!mLeftCloud.isRecycled() && !mMidCloud.isRecycled() && !mRightCloud.isRecycled()) {
            canvas.drawBitmap(mLeftCloud, 0, mLeftTop, mPaint);
            canvas.drawBitmap(mMidCloud, 0, mMidTop, mPaint);
            canvas.drawBitmap(mRightCloud, mRightBaseX + mOffset, mRightTop, mPaint);
        }
    }

    @Override
    public void drawBeginAnimationFrame(final Canvas canvas) {
        if (!mLeftCloud.isRecycled() && !mMidCloud.isRecycled() && !mRightCloud.isRecycled()) {
            canvas.drawBitmap(mLeftCloud, mTempLeftX, mLeftTop, mPaint);
            canvas.drawBitmap(mMidCloud, mTempMidX, mMidTop, mPaint);
            canvas.drawBitmap(mRightCloud, mTempRightX, mRightTop, mPaint);
        }
    }

    @Override
    public void drawCycleAnimationFrame(Canvas canvas) {
        if (!mLeftCloud.isRecycled() && !mMidCloud.isRecycled() && !mRightCloud.isRecycled()) {
            canvas.drawBitmap(mLeftCloud, mTempLeftX, mLeftTop, mPaint);
            canvas.drawBitmap(mMidCloud, mTempMidX, mMidTop, mPaint);
            canvas.drawBitmap(mRightCloud, mTempRightX, mRightTop, mPaint);
        }
    }
}

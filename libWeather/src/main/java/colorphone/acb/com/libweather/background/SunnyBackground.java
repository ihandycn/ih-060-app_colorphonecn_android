package colorphone.acb.com.libweather.background;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;
import colorphone.acb.com.libweather.launcher.LauncherAnimUtils;
import colorphone.acb.com.libweather.util.Thunk;

@SuppressWarnings("WeakerAccess")
public class SunnyBackground extends BaseWeatherAnimBackground {

    private static final int SUN_BITMAP_SIZE_MULTIPLIER = 4;

    private Bitmap mSunBitmap;
    private Bitmap mLightBitmap;
    private Bitmap mHexagonBig;
    private Bitmap mHexagonSmall;

    @Thunk
    Matrix mLightMatrix;
    @Thunk Matrix mHexagonBigMatrix;
    @Thunk Matrix mHexagonSmallMatrix;

    private int mScreenWidth;
    private float mLightBeginDegree = 50f;
    private float mLightBeginDelta = 20f;
    private float mLightStartCycleDegree = mLightBeginDegree - mLightBeginDelta;
    private float mLightCycleDelta = 15f;
    private int mLightLeft;
    private int mLightTop = Dimensions.pxFromDp(20f);
    private int mBigHexagonBaseBottom;
    private int mSmallHexagonBaseBottom;
    private int mSunLeft;
    private int mRadiusDelta = Dimensions.pxFromDp(20f);

    private Rect mRect = new Rect();

    public SunnyBackground(WeatherAnimView view) {
        super(view);

        mScreenWidth = Dimensions.getPhoneWidth(mView.getContext());

        mSunBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_sunny_bg_sun);
        mLightBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_sunny_bg_light);
        mHexagonBig = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_sunny_bg_hexagon_big);
        mHexagonSmall = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_sunny_bg_hexagon_small);

        int sunWidth = mSunBitmap.getWidth() * SUN_BITMAP_SIZE_MULTIPLIER;
        mSunLeft = mScreenWidth - sunWidth;
        int lightWidth = mLightBitmap.getWidth();
        mLightLeft = mScreenWidth - lightWidth / 2;
        mBigHexagonBaseBottom = mSunBitmap.getHeight() * SUN_BITMAP_SIZE_MULTIPLIER - Dimensions.pxFromDp(60f);
        mSmallHexagonBaseBottom = mSunBitmap.getHeight() * SUN_BITMAP_SIZE_MULTIPLIER - Dimensions.pxFromDp(80f);

        mLightMatrix = new Matrix();
        mHexagonBigMatrix = new Matrix();
        mHexagonSmallMatrix = new Matrix();

        mBeginAnimator.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        mBeginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                mLightMatrix.reset();
                mHexagonBigMatrix.reset();
                mHexagonSmallMatrix.reset();
                mLightMatrix.setTranslate(mLightLeft, mLightTop);
                mLightMatrix.postRotate(mLightBeginDegree - (mLightBeginDelta * animatedValue), mScreenWidth, mLightTop);
                mHexagonBigMatrix.setTranslate(mScreenWidth - mHexagonBig.getWidth() / 2, mBigHexagonBaseBottom + (mRadiusDelta * animatedValue));
                mHexagonBigMatrix.postRotate(mLightBeginDegree - (mLightBeginDelta * animatedValue), mScreenWidth, mLightTop);
                mHexagonSmallMatrix.setTranslate(mScreenWidth - mHexagonSmall.getWidth() / 2, mSmallHexagonBaseBottom - (mRadiusDelta * animatedValue));
                mHexagonSmallMatrix.postRotate(mLightBeginDegree - (mLightBeginDelta * animatedValue), mScreenWidth, mLightTop);
                mView.invalidate();
            }
        });
        mBeginAnimator.setDuration(2300);

        mCycleAnimator.setInterpolator(LauncherAnimUtils.ACCELERATE_DECELERATE);
        mCycleAnimator.setDuration(10000);
        mCycleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                mLightMatrix.reset();
                mHexagonBigMatrix.reset();
                mHexagonSmallMatrix.reset();
                mLightMatrix.setTranslate(mLightLeft, mLightTop);
                mLightMatrix.postRotate(mLightStartCycleDegree + (mLightCycleDelta * animatedValue), mScreenWidth, mLightTop);
                mHexagonBigMatrix.setTranslate(mScreenWidth - mHexagonBig.getWidth() / 2, mBigHexagonBaseBottom + mRadiusDelta - (mRadiusDelta * animatedValue));
                mHexagonBigMatrix.postRotate(mLightStartCycleDegree + (mLightCycleDelta * animatedValue), mScreenWidth, mLightTop);
                mHexagonSmallMatrix.setTranslate(mScreenWidth - mHexagonSmall.getWidth() / 2, mSmallHexagonBaseBottom - mRadiusDelta + (mRadiusDelta *
                        animatedValue));
                mHexagonSmallMatrix.postRotate(mLightStartCycleDegree + (mLightCycleDelta * animatedValue), mScreenWidth, mLightTop);
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
        HSLog.d("AnimationLeak", "Start sunny background animation: " + this);
        mBeginAnimator.start();
    }

    @Override
    public void drawInitializationFrame(Canvas canvas) {
        if (!mSunBitmap.isRecycled() && !mLightBitmap.isRecycled() && !mHexagonBig.isRecycled() && !mHexagonSmall.isRecycled()) {
            mRect.set(mSunLeft, 0, mScreenWidth, mSunBitmap.getHeight() * SUN_BITMAP_SIZE_MULTIPLIER);
            canvas.drawBitmap(mSunBitmap, null, mRect, mPaint);
            mLightMatrix.setTranslate(mLightLeft, mLightTop);
            mLightMatrix.postRotate(mLightBeginDegree, mScreenWidth, mLightTop);
            canvas.drawBitmap(mLightBitmap, mLightMatrix, mPaint);
            mHexagonBigMatrix.setTranslate(mScreenWidth - mHexagonBig.getWidth() / 2, mBigHexagonBaseBottom);
            mHexagonBigMatrix.postRotate(mLightBeginDegree, mScreenWidth, mLightTop);
            canvas.drawBitmap(mHexagonBig, mHexagonBigMatrix, mPaint);
            mHexagonSmallMatrix.setTranslate(mScreenWidth - mHexagonSmall.getWidth() / 2, mSmallHexagonBaseBottom);
            mHexagonSmallMatrix.postRotate(mLightBeginDegree, mScreenWidth, mLightTop);
            canvas.drawBitmap(mHexagonSmall, mHexagonSmallMatrix, mPaint);
        }
    }

    @Override
    public void drawBeginAnimationFrame(Canvas canvas) {
        if (!mSunBitmap.isRecycled() && !mLightBitmap.isRecycled() && !mHexagonBig.isRecycled() && !mHexagonSmall.isRecycled()) {
            mRect.set(mSunLeft, 0, mScreenWidth, mSunBitmap.getHeight() * SUN_BITMAP_SIZE_MULTIPLIER);
            canvas.drawBitmap(mSunBitmap, null, mRect, mPaint);
            canvas.drawBitmap(mLightBitmap, mLightMatrix, mPaint);
            canvas.drawBitmap(mHexagonBig, mHexagonBigMatrix, mPaint);
            canvas.drawBitmap(mHexagonSmall, mHexagonSmallMatrix, mPaint);
        }
    }

    @Override
    public void drawCycleAnimationFrame(Canvas canvas) {
        if (!mSunBitmap.isRecycled() && !mLightBitmap.isRecycled() && !mHexagonBig.isRecycled() && !mHexagonSmall.isRecycled()) {
            mRect.set(mSunLeft, 0, mScreenWidth, mSunBitmap.getHeight() * SUN_BITMAP_SIZE_MULTIPLIER);
            canvas.drawBitmap(mSunBitmap, null, mRect, mPaint);
            canvas.drawBitmap(mLightBitmap, mLightMatrix, mPaint);
            canvas.drawBitmap(mHexagonBig, mHexagonBigMatrix, mPaint);
            canvas.drawBitmap(mHexagonSmall, mHexagonSmallMatrix, mPaint);
        }
    }
}

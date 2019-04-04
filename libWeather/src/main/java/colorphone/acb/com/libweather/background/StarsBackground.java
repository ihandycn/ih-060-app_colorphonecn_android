package colorphone.acb.com.libweather.background;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;
import colorphone.acb.com.libweather.launcher.LauncherAnimUtils;
import colorphone.acb.com.libweather.util.Thunk;

@SuppressWarnings("WeakerAccess")
public class StarsBackground extends BaseWeatherAnimBackground {

    private static final int TOP_RANGE = Dimensions.pxFromDp(100f);
    private static final int BOTTOM_RANGE = Dimensions.pxFromDp(300f);

    private Bitmap[] mStarBitmaps;
    private int mScreenWidth;
    @Thunk
    Float mAnimatedValue;
    private long mStartTime;
    private int mLineNum = 4;
    private int mColumnNum = 5;
    private int mPerRectStarNum = 1;
    private int mStarsNum = mLineNum * mColumnNum * mPerRectStarNum;
    private List<Star> mStars = new ArrayList<>(mStarsNum);
    private float mA;
    private float mB;
    private float mC;

    public StarsBackground(WeatherAnimView view) {
        super(view);

        Bitmap bigLeftBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_star_left_l);
        Bitmap bigRightBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_star_right_l);
        Bitmap mediumLeftBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_star_left_m);
        Bitmap mediumRightBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_star_right_m);
        Bitmap smallLeftBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_star_left_s);
        Bitmap smallRightBitmap = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_night_bg_star_right_s);
        mStarBitmaps = new Bitmap[6];
        mStarBitmaps[0] = bigLeftBitmap;
        mStarBitmaps[1] = bigRightBitmap;
        mStarBitmaps[2] = mediumLeftBitmap;
        mStarBitmaps[3] = mediumRightBitmap;
        mStarBitmaps[4] = smallLeftBitmap;
        mStarBitmaps[5] = smallRightBitmap;

        mScreenWidth = Dimensions.getPhoneWidth(view.getContext());
        int heightDelta = Dimensions.pxFromDp(100f);
        mA = (4f * heightDelta) / (3 * mScreenWidth * mScreenWidth);
        mB = -mA * mScreenWidth;
        mC = BOTTOM_RANGE;
        initStar();

        mCycleAnimator.setDuration(40000);
        mCycleAnimator.setRepeatMode(ValueAnimator.RESTART);
        mCycleAnimator.setInterpolator(LauncherAnimUtils.LINEAR);
        mCycleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedValue = (Float) animation.getAnimatedValue();
                mView.invalidate();
            }
        });
        mCycleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                for (Star star : mStars) {
                    star.positionY = star.initPositionY;
                }
            }
        });
    }

    private void initStar() {
        Random random = new Random();
        int rectWidth = mScreenWidth / mColumnNum;
        int rectHeight = (BOTTOM_RANGE - TOP_RANGE) / mLineNum;
        for (int i = 0; i < mStarsNum; i++) {
            int rectPos = i / mPerRectStarNum;
            int columnPos = rectPos % mColumnNum;
            int linePos = rectPos / mColumnNum;
            int positionX = random.nextInt(rectWidth) + columnPos * rectWidth;
            int positionY = random.nextInt(rectHeight) + TOP_RANGE + linePos * rectHeight;
            int status = random.nextInt(3) + 4;
            int type = random.nextInt(6);
            long delayTime = random.nextInt(3) * 1000;
            int alphaDelta = random.nextInt(3) + 4;
            Star newStar = new Star(positionX, positionY, type, status, delayTime, alphaDelta);
            mStars.add(newStar);
        }
    }

    @Override
    public void startBeginAnimation() {
        super.startCycleAnimation();
        mStartTime = System.currentTimeMillis();
    }

    @Override
    public void startCycleAnimation() {
    }

    @Override
    public void drawInitializationFrame(Canvas canvas) {
        for (int i = 0; i < mStarsNum; i++) {
            Star star = mStars.get(i);
            star.positionY = mA * star.initPositionX * star.initPositionX + mB * star.initPositionX + mC - (BOTTOM_RANGE - star.initPositionY);
            if (!mStarBitmaps[star.type].isRecycled()) {
                canvas.drawBitmap(mStarBitmaps[star.type], star.initPositionX, star.positionY, mPaint);
            }
        }
    }

    @Override
    public void drawBeginAnimationFrame(Canvas canvas) {
        // Empty
    }

    @Override
    public void drawCycleAnimationFrame(Canvas canvas) {
        long duration = System.currentTimeMillis() - mStartTime;
        for (int i = 0; i < mStarsNum; i++) {
            Star star = mStars.get(i);
            star.positionX = star.initPositionX - mScreenWidth * mAnimatedValue;
            if (star.positionX < 0) {
                star.positionX += mScreenWidth;
            }
            if (star.status == Star.STATUS_DIMMING && duration > star.delayTime) {
                star.alpha -= star.alphaDelta;
                if (star.alpha <= 0) {
                    star.alpha = 0;
                    star.status = Star.STATUS_BRIGHTENING;
                }
            } else if (star.status == Star.STATUS_BRIGHTENING && duration > star.delayTime) {
                star.alpha += star.alphaDelta;
                if (star.alpha >= 0xFF) {
                    star.alpha = 0xFF;
                    star.setBright();
                }
            }
            if (star.status == Star.STATUS_BRIGHT && star.checkAndDecreaseBrightTime()) {
                star.status = Star.STATUS_DIMMING;
            }
            mPaint.setAlpha((int) (star.alpha * mAlpha));
            star.positionY = mA * star.positionX * star.positionX + mB * star.positionX + mC - (BOTTOM_RANGE - star.initPositionY);
            if (!mStarBitmaps[star.type].isRecycled()) {
                canvas.drawBitmap(mStarBitmaps[star.type], star.positionX, star.positionY, mPaint);
            }
        }
    }

    private static class Star {
        final static int STATUS_BRIGHTENING = 4;
        final static int STATUS_DIMMING = 5;
        final static int STATUS_BRIGHT = 0;

        float initPositionX;
        float initPositionY;
        float positionX;
        float positionY;
        public int type;
        public int status;
        public int alpha;
        long delayTime;
        int alphaDelta;

        private int mBrightTimeCounter;

        public Star(float x, float y, int type, int status, long delayTime, int alphaDelta) {
            this.initPositionX = x;
            this.initPositionY = y;
            this.positionY = y;
            this.positionX = x;
            this.type = type;
            this.status = status;
            this.delayTime = delayTime;
            this.alphaDelta = alphaDelta;
            if (status == STATUS_DIMMING) {
                alpha = 0x00;
            } else {
                alpha = 0xff;
            }
        }

        void setBright() {
            status = STATUS_BRIGHT;
            mBrightTimeCounter = 20;
        }

        boolean checkAndDecreaseBrightTime() {
            mBrightTimeCounter--;
            return mBrightTimeCounter == 0;
        }
    }
}

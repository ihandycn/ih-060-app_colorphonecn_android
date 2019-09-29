package colorphone.acb.com.libweather.background;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.launcher.LauncherAnimUtils;
import colorphone.acb.com.libweather.util.Utils;

public class RainyBackground extends BaseWeatherAnimBackground {

    private static final float ANGLE = 23;
    private static final float HEIGHT_DP = 280;
    private static final long PERIOD = 1400;
    private static final float DIST_THRESHOLD = 8;

    private float mBeginAnimatedValue;
    private float mCycleAnimatedValue;

    private List<RainDrop> mDrops = new ArrayList<>(32);
    private Random mRand;

    private final RectF mRainDrop;
    private final Point mScreenSize;
    private final float mHeightPx;
    private final float mSinAngle;
    private final float mCosAngle;
    private final int mDistThreshold;

    private boolean mIsRtl;

    public enum Intensity {
        DRIZZLE,
        NORMAL,
        SHOWER,
    }

    public RainyBackground(WeatherAnimView view, Intensity intensity) {
        super(view);
        mIsRtl = Dimensions.isRtl();

        mPaint.setColor(Color.WHITE);

        mRand = new Random(System.currentTimeMillis());

        mRainDrop = new RectF(0, 0, Dimensions.pxFromDp(60), Dimensions.pxFromDp(1f));
        mScreenSize = Utils.getScreenSize((Activity) view.getContext());
        mHeightPx = Dimensions.pxFromDp(HEIGHT_DP);
        double radianAngle = Math.toRadians(ANGLE);
        mSinAngle = (float) Math.sin(radianAngle);
        mCosAngle = (float) Math.cos(radianAngle);
        mDistThreshold = Dimensions.pxFromDp(DIST_THRESHOLD);

        mBeginAnimator.setInterpolator(LauncherAnimUtils.LINEAR);
        mBeginAnimator.setDuration(PERIOD);
        mBeginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBeginAnimatedValue = (Float) animation.getAnimatedValue();
                mView.invalidate();
            }
        });

        mCycleAnimator.setInterpolator(LauncherAnimUtils.LINEAR);
        mCycleAnimator.setDuration(PERIOD);
        mCycleAnimator.setRepeatMode(ValueAnimator.RESTART);
        mCycleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCycleAnimatedValue = (Float) animation.getAnimatedValue();
                mView.invalidate();
            }
        });

        generateRainDrops(intensity);
    }

    private void generateRainDrops(Intensity intensity) {
        int dropCount = 0;
        int minimumLength = 30, lengthRange = 50;
        float velocity = Dimensions.pxFromDp(800);
        switch (intensity) {
            case DRIZZLE:
                dropCount = 13;
                minimumLength = 20;
                lengthRange = 30;
                velocity = Dimensions.pxFromDp(500);
                break;
            case NORMAL:
                dropCount = 20;
                minimumLength = 30;
                lengthRange = 50;
                velocity = Dimensions.pxFromDp(800);
                break;
            case SHOWER:
                dropCount = 27;
                minimumLength = 40;
                lengthRange = 60;
                velocity = Dimensions.pxFromDp(1000);
                break;
        }
        for (int i = 0; i < dropCount; i++) {
            mDrops.add(new RainDrop(
                    mRand.nextInt(mScreenSize.x) + (mIsRtl ? -mScreenSize.x : mScreenSize.x) / 2, // Horizontal entry position
                    0.1f + 0.3f * mRand.nextFloat(), // Alpha
                    Dimensions.pxFromDp(minimumLength + mRand.nextInt(lengthRange)), // Length
                    velocity,
                    mRand.nextFloat() // Entry time
            ));
        }

        // Adjust position of rain drops that are too close
        //noinspection unchecked
        Collections.sort(mDrops);
        for (int i = 1, count = mDrops.size(); i < count - 1; i++) {
            RainDrop self = mDrops.get(i);
            RainDrop left = mDrops.get(i - 1);
            RainDrop right = mDrops.get(i + 1);
            if (self.entryX - left.entryX < mDistThreshold || right.entryX - self.entryX < mDistThreshold) {
                self.entryX = (left.entryX + right.entryX) / 2;
            }
        }
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
        // Empty
    }

    @Override
    public void drawBeginAnimationFrame(final Canvas canvas) {
        doDraw(canvas);
    }

    @Override
    public void drawCycleAnimationFrame(final Canvas canvas) {
        doDraw(canvas);
    }

    private void doDraw(Canvas canvas) {
        float time = (mMode == DrawMode.BEGIN_ANIMATION) ? mBeginAnimatedValue : mCycleAnimatedValue;
        float rtl = mIsRtl ? -1f : 1f;
        for (RainDrop drop : mDrops) {
            float delta = time - drop.entryTime;
            if (delta < 0f) delta += 1f;
            float x = drop.entryX - rtl * drop.velocity * delta * mSinAngle + rtl * drop.length * mCosAngle;
            float y = drop.velocity * delta * mCosAngle - drop.length * mCosAngle;
            float fadeOutAlpha = getFadeoutAlpha(y);
            if (fadeOutAlpha > 0f) {
                mPaint.setAlpha((int) (0xff * drop.alpha * mAlpha * fadeOutAlpha));
                drawRainDrop(canvas, x, y, drop.length);
            }
        }
    }

    private float getFadeoutAlpha(float y) {
        if (y < 0.8f * mHeightPx) return 1f;
        if (y > 1.0f * mHeightPx) return 0f;
        return 1f - (y - 0.8f * mHeightPx) / (0.2f * mHeightPx);
    }

    private void drawRainDrop(Canvas canvas, float x, float y, float l) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(90 + (mIsRtl ? -ANGLE : ANGLE));

        mRainDrop.set(0, 0, l, mRainDrop.bottom);
        canvas.drawRect(mRainDrop, mPaint);

        canvas.restore();
    }

    private static class RainDrop implements Comparable {
        int entryX;
        public float alpha;
        public float length;
        float velocity;
        float entryTime;

        RainDrop(int entryX, float alpha, float length, float velocity, float entryTime) {
            this.entryX = entryX;
            this.alpha = alpha;
            this.length = length;
            this.velocity = velocity;
            this.entryTime = entryTime;
        }

        @Override
        public int compareTo(@NonNull Object another) {
            if (another instanceof RainDrop) {
                return entryX - ((RainDrop) another).entryX;
            }
            return 0;
        }
    }
}

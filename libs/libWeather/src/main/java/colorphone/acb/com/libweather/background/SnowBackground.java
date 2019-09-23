package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Pair;

import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;

public class SnowBackground extends BaseWeatherAnimBackground {

    private static final float STD_SCREEN_WIDTH = 1080f;
    private static final float STD_SCREEN_HEIGHT = 1920f;

    private static final int[] POS_X = new int[]{
            368, 444, 486, 533, 587,
            715, 759, 766, 798, 892,
            975, 1068
    };
    private static final int[] POS_Y = new int[]{
            924, 332, 812, 666, 389,
            563, 889, 729, 424, 487,
            804, 654
    };
    private static final float[] SIZE = new float[]{
            5.7f, 5.7f, 19f, 10.7f, 10.7f,
            10.7f, 5.7f, 5.7f, 10.7f, 19f,
            5.7f, 10.7f
    };

    private List<Pair<Float, Float>> mPositions = new ArrayList<>(23);
    private List<Integer> mSizes = new ArrayList<>(23);

    private Bitmap mSnow;
    private Rect mDstRect = new Rect();

    public SnowBackground(WeatherAnimView view) {
        super(view);

        mSnow = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_snow_bg_snow);

        for (int i = 0; i < POS_X.length; i++) {
            int x = POS_X[i];
            int y = POS_Y[i];
            mPositions.add(new Pair<>(x * (mScreenSize.x / STD_SCREEN_WIDTH), y * (mScreenSize.y / STD_SCREEN_HEIGHT)));
            mSizes.add(Dimensions.pxFromDp(SIZE[i]));
        }
    }

    @Override
    public void drawInitializationFrame(Canvas canvas) {
        doDraw(canvas);
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
        for (int i = 0; i < mPositions.size(); i++) {
            float x = mPositions.get(i).first;
            float y = mPositions.get(i).second;
            mDstRect.set((int) x, (int) y, (int) x + mSizes.get(i), (int) y + mSizes.get(i));
            if (!mSnow.isRecycled()) {
                canvas.drawBitmap(mSnow, null, mDstRect, mPaint);
            }
        }
    }
}

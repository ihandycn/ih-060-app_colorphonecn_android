package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Pair;


import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;

public class DustBackground extends BaseWeatherAnimBackground {

    private static final float STD_SCREEN_WIDTH = 1080f;
    private static final float STD_SCREEN_HEIGHT = 1920f;

    private static final int[] POS_X = new int[]{
            250, 281, 262, 376, 434,
            495, 509, 560, 574, 636,
            696, 700, 763, 770, 789,
            792, 816, 811, 885, 885,
            945, 1070, 595
    };
    private static final int[] POS_Y = new int[]{
            581, 728, 801, 694, 768,
            710, 591, 690, 811, 665,
            593, 442, 359, 574, 503,
            621, 470, 695, 471, 559,
            549, 468, 498
    };
    private static final boolean[] LARGE = new boolean[]{
            true, false, false, false, false,
            false, false, false, false, false,
            false, false, true, false, false,
            false, true, false, false, false,
            false, false, false
    };

    private List<Pair<Float, Float>> mPositions = new ArrayList<>(23);

    private Bitmap mDust;
    private Rect mSmallRect = new Rect();
    private Rect mLargeRect = new Rect();

    public DustBackground(WeatherAnimView view) {
        super(view);

        mDust = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_dust_bg_dust);
        mLargeRect.set(0, 0, mDust.getWidth(), mDust.getHeight());
        mSmallRect.set(0, 0, mDust.getWidth() / 2, mDust.getHeight() / 2);

        for (int i = 0; i < POS_X.length; i++) {
            int x = POS_X[i];
            int y = POS_Y[i];
            mPositions.add(new Pair<>(x * (mScreenSize.x / STD_SCREEN_WIDTH), y * (mScreenSize.y / STD_SCREEN_HEIGHT)));
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
            Rect dstRect = LARGE[i] ? mLargeRect : mSmallRect;
            float x = mPositions.get(i).first;
            float y = mPositions.get(i).second;
            dstRect.set((int) x, (int) y, (int) x + dstRect.width(), (int) y + dstRect.height());
            if (!mDust.isRecycled()) {
                canvas.drawBitmap(mDust, null, dstRect, mPaint);
            }
        }
    }
}

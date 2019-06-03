package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;

public class HazeBackground extends BaseWeatherAnimBackground {

    private Bitmap mHaze;
    private Rect mRect = new Rect();

    public HazeBackground(WeatherAnimView view) {
        super(view);
        mHaze = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_haze_bg);
    }

    @Override
    public void drawInitializationFrame(final Canvas canvas) {
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
        mRect.set(0, 0, mScreenSize.x, (int) (mScreenSize.x * ((float) mHaze.getHeight() / mHaze.getWidth())));
        if (!mHaze.isRecycled()) {
            canvas.drawBitmap(mHaze, null, mRect, mPaint);
        }
    }
}

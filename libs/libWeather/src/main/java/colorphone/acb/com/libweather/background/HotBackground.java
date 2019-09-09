package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;

public class HotBackground extends BaseWeatherAnimBackground {

    private Bitmap mHotAir;
    private Rect mRect = new Rect();

    public HotBackground(WeatherAnimView view) {
        super(view);
        mHotAir = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_hot_bg);
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
        mRect.set(mScreenSize.x - mHotAir.getWidth(), 0, mScreenSize.x, mHotAir.getHeight());
        if (!mHotAir.isRecycled()) {
            canvas.drawBitmap(mHotAir, null, mRect, mPaint);
        }
    }
}

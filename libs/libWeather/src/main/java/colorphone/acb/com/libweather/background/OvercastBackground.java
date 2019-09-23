package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;


public class OvercastBackground extends BaseWeatherAnimBackground {

    private Bitmap mOvercast;
    private Rect mRect = new Rect();

    public OvercastBackground(WeatherAnimView view) {
        super(view);
        mOvercast = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_overcast_bg);
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
        mRect.set(0, 0, mScreenSize.x, (int) (mScreenSize.x * ((float) mOvercast.getHeight() / mOvercast.getWidth())));
        if (!mOvercast.isRecycled()) {
            canvas.drawBitmap(mOvercast, null, mRect, mPaint);
        }
    }
}

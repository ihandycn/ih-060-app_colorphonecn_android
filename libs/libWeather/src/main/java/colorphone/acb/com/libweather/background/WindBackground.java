package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.superapps.util.Dimensions;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;


public class WindBackground extends BaseWeatherAnimBackground {

    private Bitmap mWind;
    private Rect mRect = new Rect();

    public WindBackground(WeatherAnimView view) {
        super(view);
        mWind = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_wind_bg);
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
        int marginTop = Dimensions.pxFromDp(80);
        mRect.set(0, marginTop, mScreenSize.x,
                marginTop + (int) (mScreenSize.x * ((float) mWind.getHeight() / mWind.getWidth())));
        if (!mWind.isRecycled()) {
            canvas.drawBitmap(mWind, null, mRect, mPaint);
        }
    }
}

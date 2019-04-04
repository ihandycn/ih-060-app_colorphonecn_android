package colorphone.acb.com.libweather.background;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import colorphone.acb.com.libweather.R;
import colorphone.acb.com.libweather.WeatherAnimView;
import colorphone.acb.com.libweather.WeatherUtils;
import colorphone.acb.com.libweather.animation.DeviceEvaluator;


public class BottomShaderBackground extends BaseWeatherAnimBackground {

    private Bitmap mShader;
    private Rect mRect = new Rect();

    private boolean mEnabled;

    public BottomShaderBackground(WeatherAnimView view) {
        super(view);

        // Disable this page bottom color effect for BELOW_AVERAGE and POOR devices to reduce overdraw by 1 time
        // for almost half of the screen area.
        mEnabled = (DeviceEvaluator.getEvaluation() >= DeviceEvaluator.DEVICE_EVALUATION_DEFAULT);

        if (mEnabled) {
            mShader = WeatherUtils.getWeatherBackgroundAnimBitmap(R.drawable.weather_detail_bottom_pic);
        }
    }

    @Override
    public boolean shouldFadeOutOnVerticalScroll() {
        return false;
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
        if (mEnabled && !mShader.isRecycled()) {
            int height = (int) (mScreenSize.x * ((float) mShader.getHeight() / mShader.getWidth()));
            int realScreenHeight = mScreenSize.y;
            mRect.set(0, realScreenHeight - height, mScreenSize.x, realScreenHeight);
            canvas.drawBitmap(mShader, null, mRect, mPaint);
        }
    }
}

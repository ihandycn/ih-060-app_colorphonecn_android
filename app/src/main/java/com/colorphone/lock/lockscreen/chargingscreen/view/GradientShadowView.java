package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

/**
 * Created by zhouzhenliang on 17/2/7.
 */

public class GradientShadowView extends View {

    private static final String TAG = "GRADIENT_VIEW";

    private static final int START_COLOR = 0x3B4181E3;
    private static final int END_COLOR = 0x004588F0;

    private float x;
    private float y;

    private float startX, startY, endX, endY;

    private Paint paint;

    public GradientShadowView(Context context) {
        super(context);

        init();
    }

    public GradientShadowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public GradientShadowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void init() {
        x = getResources().getDimensionPixelSize(R.dimen.smart_charging_spread_battery_bg_x);
        y = getResources().getDimensionPixelSize(R.dimen.smart_charging_spread_battery_bg_y);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        paint.setStrokeWidth((float) Math.sqrt(x * x + y * y));

        HSLog.d(TAG, "onSizeChanged(), w = " + w + ", h = " + h + ", StrokeWidth = " + paint.getStrokeWidth());

        final float dx = w - x;
        final float dy = dx * x / y;

        startX = x / 2f;
        startY = y / 2f;
        endX = (dx + w) / 2f;
        endY = (y + dy + dy) / 2f;

        LinearGradient linearGradient = new LinearGradient(startX, startY, endX, endY,
            START_COLOR, END_COLOR, Shader.TileMode.MIRROR);

        paint.setShader(linearGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            paint.setColor(START_COLOR);
            paint.setShader(null);
            canvas.drawLine(startX, startY, endX, endY, paint);

            return;
        }

        canvas.drawLine(startX, startY, endX, endY, paint);
    }
}

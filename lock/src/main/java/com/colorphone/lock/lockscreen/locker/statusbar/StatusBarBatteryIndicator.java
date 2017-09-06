package com.colorphone.lock.lockscreen.locker.statusbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.colorphone.lock.boost.DeviceManager;


public class StatusBarBatteryIndicator extends View {
    private int percentage = 19;
    private int redColor = Color.parseColor("#ee4458");
    private int greenColor = Color.parseColor("#97e413");
    private int whiteColor = Color.parseColor("#ffffff");
    private Paint paint = new Paint();

    public StatusBarBatteryIndicator(Context context, AttributeSet set) {
        super(context, set);
    }

    public void setPercentage(int percent) {
        percentage = percent;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (DeviceManager.getInstance().isCharging()) {
            paint.setColor(greenColor);
        } else {
            if (percentage <= 20) {
                paint.setColor(redColor);
            } else {
                paint.setColor(whiteColor);
            }
        }

        canvas.drawRect(0, 0, width * percentage / 100, height, paint);
    }
}
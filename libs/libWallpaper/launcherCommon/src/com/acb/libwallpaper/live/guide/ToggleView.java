package com.acb.libwallpaper.live.guide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class ToggleView extends View {

    private static final int COLOR_TOGGLE_ON = 0xFF009688;
    private static final int COLOR_TOGGLE_LINE_ON = 0xFF7FCAC3;
    private static final int COLOR_TOGGLE_OFF = 0xFFF1F1F1;
    private static final int COLOR_TOGGLE_LINE_OFF = 0xFFC6C5C5;

    private Paint mCirclePaint;
    private float mCoefficient;
    private float mDensity;
    private boolean mFinish;
    private Paint mPaint;
    private boolean mToggling;

    public ToggleView(Context context) {
        this(context, null);
    }

    public ToggleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ToggleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCirclePaint = new Paint();
        this.mCirclePaint.setAntiAlias(true);
        this.mCirclePaint.setColor(COLOR_TOGGLE_OFF);
        //this.mCirclePaint.setShadowLayer(5.0f, 3.0f, 3.0f, Integer.MIN_VALUE);
        setLayerType(1, this.mCirclePaint);
        this.mPaint = new Paint();
        this.mPaint.setColor(COLOR_TOGGLE_LINE_OFF);
        this.mDensity = getResources().getDisplayMetrics().density;
    }

    public void setCoefficient(float f) {
        this.mCoefficient = f;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    protected void onDraw(Canvas canvas) {
        float height = (float) getHeight();
        float width = (float) getWidth();
        if (this.mToggling) {
            this.mPaint.setColor(COLOR_TOGGLE_LINE_ON);
            this.mCirclePaint.setColor(COLOR_TOGGLE_ON);
        }
        float f = 7.6f * this.mDensity;
        float f2 = 3.8f * this.mDensity;
        float f3 = (height - f) / 2.0f;
        float f4 = (width - (15.0f * this.mDensity)) / 2.0f;

        canvas.drawCircle(f4, height / 2.0f, f2, this.mPaint);
        canvas.drawCircle(width - f4, height / 2.0f, f2, this.mPaint);
        canvas.drawRect(f4, f3, width - f4, height - f3, this.mPaint);
        canvas.drawCircle(f4 + (this.mCoefficient * (width - (2.0f * f4))), height / 2.0f, f / 1.2f, this.mCirclePaint);
    }

    public void startToggle() {
        this.mToggling = true;
        this.mCoefficient = 0.0f;
    }

    public void init() {
        this.mToggling = false;
        this.mFinish = false;
        this.mCoefficient = 0.0f;
        this.mCirclePaint.setColor(COLOR_TOGGLE_OFF);
        this.mPaint.setColor(COLOR_TOGGLE_LINE_OFF);
        invalidate();
    }

    public void stopToggle() {
        this.mFinish = true;
    }
}

package com.acb.libwallpaper.live.guide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.superapps.util.Dimensions;

public class RippleView extends LinearLayout {
    private static final int COLOR_RIPPLE = 0xFF48AEFF;
    private static final float RIPPLE_SCALE_FACTOR = 1.5f;
    private static final float WIDTH_STROKE_RECTANGLE = Dimensions.pxFromDp(5);

    private int mDefaultAnimationColor;
    private float mFraction;
    private boolean mHighlight;
    private Paint mHighlightPaint;
    private int mHlAlpha;
    private Paint mPaint;
    private boolean mRippling;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RippleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mPaint = new Paint();
        this.mHighlightPaint = new Paint();
        this.mHighlightPaint.setStyle(Style.STROKE);
        this.mHighlightPaint.setStrokeWidth(WIDTH_STROKE_RECTANGLE);
        this.mHighlightPaint.setColor(COLOR_RIPPLE);
        this.mDefaultAnimationColor = COLOR_RIPPLE;
    }

    public void setHlAlpha(int i) {
        this.mHlAlpha = i;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setFraction(float f) {
        this.mFraction = f;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mRippling) {
            float width = (float) getWidth();
            float height = (float) getHeight();
            float cx =  width - (float) Dimensions.pxFromDp(33);
            float cy = height / 2.0f - (float) Dimensions.pxFromDp(5);
            float max = Math.max(width * RIPPLE_SCALE_FACTOR, height * RIPPLE_SCALE_FACTOR);
            int round = Math.round((1.0f - this.mFraction) * ((float) (this.mDefaultAnimationColor >>> 24)));
            int color = this.mPaint.getColor();
            this.mPaint.setColor((round << 24) | (this.mDefaultAnimationColor & ViewCompat.MEASURED_SIZE_MASK));
            canvas.drawCircle(cx, cy, max * this.mFraction, this.mPaint);
            this.mPaint.setColor(color);
        }
        if (this.mHighlight) {
            int color2 = this.mHighlightPaint.getColor();
            this.mHighlightPaint.setColor((color2 & ViewCompat.MEASURED_SIZE_MASK) | (this.mHlAlpha << 24));
            canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.mHighlightPaint);
            this.mHighlightPaint.setColor(color2);
        }
        super.dispatchDraw(canvas);
    }

    public void performRipple(boolean z) {
        this.mRippling = z;
    }

    public void performHighlight(boolean z) {
        this.mHighlight = z;
    }
}

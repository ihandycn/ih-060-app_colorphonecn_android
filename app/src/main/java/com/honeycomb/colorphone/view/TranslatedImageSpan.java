package com.honeycomb.colorphone.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

public class TranslatedImageSpan extends ImageSpan {
    private float translationX;
    private float translationY;

    public TranslatedImageSpan(Drawable drawable, int verticalAlignment) {
        super(drawable, verticalAlignment);
    }

    public void setTranslation(float translationX, float translationY) {
        this.translationX = translationX;
        this.translationY = translationY;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        Drawable b = getDrawable();
        canvas.save();

        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        } else if (mVerticalAlignment == ALIGN_CENTER) {
            transY = (bottom - top) / 2 - b.getBounds().height() / 2;
        }

        canvas.translate(x + translationX, transY + translationY);
        b.draw(canvas);
        canvas.restore();
    }
}

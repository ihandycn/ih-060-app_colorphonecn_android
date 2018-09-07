package com.honeycomb.colorphone.resultpage;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class BoostBgImageView extends android.support.v7.widget.AppCompatImageView {

    public BoostBgImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoostBgImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BoostBgImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(45, canvas.getWidth()/2 , canvas.getHeight()/2);
        super.onDraw(canvas);
    }
}

package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Region;
import android.util.AttributeSet;

import com.superapps.view.TypefacedTextView;

public class ShadowTypefacedTextView extends TypefacedTextView {

    public static final float SHADOW_LARGE_RADIUS = 7.0f;
    public static final float SHADOW_SMALL_RADIUS = 3.5f;
    public static final float SHADOW_Y_OFFSET = 3.0f;
    public static final int SHADOW_LARGE_COLOUR = 0x44000000;
    public static final int SHADOW_SMALL_COLOUR = 0x33000000;

    public ShadowTypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void draw(Canvas canvas) {
        getPaint().setShadowLayer(SHADOW_LARGE_RADIUS, 0.0f, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOUR);
        super.draw(canvas);
        canvas.save();
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(),
                getScrollX() + getWidth(),
                getScrollY() + getHeight(), Region.Op.INTERSECT);
        getPaint().setShadowLayer(SHADOW_SMALL_RADIUS, 0.0f, 0.0f, SHADOW_SMALL_COLOUR);
        super.draw(canvas);
        canvas.restore();
    }
}

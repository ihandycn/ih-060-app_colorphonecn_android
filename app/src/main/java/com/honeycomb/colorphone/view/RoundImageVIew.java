package com.honeycomb.colorphone.view;

import android.content.Context;
import android.util.AttributeSet;

public class RoundImageVIew extends ShapeImageView {

    public RoundImageVIew(Context context) {
        this(context, null, 0);
    }

    public RoundImageVIew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageVIew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mShape = Shape.RECTANGLE;
    }
}

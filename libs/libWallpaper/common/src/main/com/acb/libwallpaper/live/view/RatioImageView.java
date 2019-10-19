package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.util.ViewUtils;

/**
 * ratio = width / height;
 */
public class RatioImageView extends AppCompatImageView {
    private int mBase;
    private float mRatio;

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioAttr);
        mBase = a.getInt(R.styleable.RatioAttr_base, ViewUtils.DEFAULT_BASE);
        mRatio = a.getFloat(R.styleable.RatioAttr_ratio, 0f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        boolean isRatio = ViewUtils.ratio(mBase, mRatio, width, height, (w, h) -> setMeasuredDimension(w, h));
        if (!isRatio) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setAspectRatioAndInvalidate(float aspectRatioOverride) {
        mRatio = aspectRatioOverride;
        invalidate();
    }
}

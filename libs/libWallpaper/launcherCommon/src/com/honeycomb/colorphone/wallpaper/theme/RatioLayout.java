package com.honeycomb.colorphone.wallpaper.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;

/**
 * ViewGroup with specified aspect ratio
 * ratio = width / height;
 */
public class RatioLayout extends FrameLayout {

    private int mBase;
    private float mRatio;

    public RatioLayout(Context context) {
        this(context, null);
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioAttr);
        mBase = a.getInt(R.styleable.RatioAttr_base, ViewUtils.DEFAULT_BASE);
        mRatio = a.getFloat(R.styleable.RatioAttr_ratio, 0f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        if (mRatio > 0f) {
            int width;
            int height;
            if (mBase == ViewUtils.BASE_WIDTH) {
                width = getMeasuredWidth();
                height = (int) Math.ceil(width / mRatio);
            } else {
                height = getMeasuredHeight();
                width = (int) Math.ceil(height * mRatio);
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setRatio(int width, int height) {
        this.mBase = ViewUtils.BASE_WIDTH;
        mRatio = width / (float) height;
        requestLayout();
    }
}

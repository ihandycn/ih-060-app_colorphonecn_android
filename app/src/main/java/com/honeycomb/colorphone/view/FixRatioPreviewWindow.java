package com.honeycomb.colorphone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import com.honeycomb.colorphone.R;

public class FixRatioPreviewWindow extends CardView {

    public FixRatioPreviewWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FixRatioPreviewWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalBannerImageView);
        mAspectRatioOverride = a.getFloat(R.styleable.HorizontalBannerImageView_aspectRatioOverride, -1f);
        a.recycle();
    }

    private float mAspectRatioOverride;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        Drawable d = getBackground();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (mAspectRatioOverride >= 0f) {
            height = (int) Math.ceil(width / mAspectRatioOverride);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    public void setAspectRatioAndInvalidate(float aspectRatioOverride) {
        mAspectRatioOverride = aspectRatioOverride;
        invalidate();
    }
}

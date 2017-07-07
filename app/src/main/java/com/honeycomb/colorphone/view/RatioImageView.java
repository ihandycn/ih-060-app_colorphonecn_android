package com.honeycomb.colorphone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.honeycomb.colorphone.R;


/**
 * A {@link ImageView} for use as a horizontal banner, which takes up all width of parent, and measures height
 * of itself with the aspect ratio of its background image (NOTICE: not source image). Or aspect ratio may be override
 * to set a fixed value.
 * FIXME: RoundCornerImageView use source image, not background.
 */
public class RatioImageView extends AppCompatImageView {

    private float mAspectRatioOverride;

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalBannerImageView);
        mAspectRatioOverride = a.getFloat(R.styleable.HorizontalBannerImageView_aspectRatioOverride, -1f);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        Drawable d = getBackground();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (d != null && mAspectRatioOverride < 0f) {
            height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
            setMeasuredDimension(width, height);
        } else if (mAspectRatioOverride >= 0f) {
            height = (int) Math.ceil(width / mAspectRatioOverride);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setAspectRatioAndInvalidate(float aspectRatioOverride) {
        mAspectRatioOverride = aspectRatioOverride;
        invalidate();
    }

}

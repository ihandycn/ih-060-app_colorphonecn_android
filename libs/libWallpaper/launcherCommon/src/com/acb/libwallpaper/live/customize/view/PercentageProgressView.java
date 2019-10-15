package com.acb.libwallpaper.live.customize.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.acb.libwallpaper.R;
import com.superapps.util.Dimensions;

public class PercentageProgressView extends FrameLayout {

    private TextView mProgressText;
    private PercentageProgressBar mProgressBar;

    private final float mTextDistance;

    private int mLastPercentageInt = -1;

    public PercentageProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PercentageProgressView);
        Resources res = context.getResources();

        mTextDistance = a.getDimension(R.styleable.PercentageProgressView_textDistance,
                res.getDimension(R.dimen.percentage_progress_view_default_text_distance));

        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressText = findViewById(R.id.progress_dialog_percentage_text);
        mProgressBar = findViewById(R.id.progress_dialog_bar);
    }

    public void setProgress(float progress) {
        mProgressBar.setProgress(progress);
        int percentageInt = Math.round(100f * progress);
        if (percentageInt != mLastPercentageInt) {
            mLastPercentageInt = percentageInt;
            mProgressText.setText(getContext().getString(
                    R.string.live_wallpaper_loading_percentage, percentageInt));
        }
        mProgressText.requestLayout();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!isInEditMode()) {
            LayoutParams textLp = (LayoutParams) mProgressText.getLayoutParams();
            if (isChildrenSizeNotReady()) {
                // Use hardcoded value
            } else {
                // Normal case
                if (Dimensions.isRtl()) {
                    LayoutParams progressBarParams = (LayoutParams) mProgressBar.getLayoutParams();
                    float thumbCenterX = mProgressBar.getThumbCenterX();
                    textLp.rightMargin = Math.round(right - left - progressBarParams.leftMargin - thumbCenterX - mProgressText.getWidth() / 2f);
                } else {
                    float thumbCenterX = mProgressBar.getLeft() + mProgressBar.getThumbCenterX();
                    textLp.leftMargin = Math.round(thumbCenterX - mProgressText.getWidth() / 2f);
                }

                float textTopY = mProgressBar.getTop() - mTextDistance - mProgressText.getHeight();
                textLp.topMargin = Math.round(textTopY);
            }
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private boolean isChildrenSizeNotReady() {
        return mProgressBar.getWidth() == 0 || mProgressBar.getHeight() == 0
                || mProgressText.getWidth() == 0 || mProgressText.getHeight() == 0;
    }
}

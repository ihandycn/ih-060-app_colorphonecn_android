package com.honeycomb.colorphone.customize.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.superapps.util.Dimensions;

public class PercentageProgressBar extends View {

    private float mProgress;

    private final float mTrackHeight;
    private final float mThumbHeight;
    private final float mThumbCenterHeight;
    private final @ColorInt int mBarColorLeft;
    private final @ColorInt int mBarColorRight;

    private Paint mTrackPaint;
    private Paint mBarPaint;
    private Paint mThumbCenterPaint;

    private RectF mRectF;
    private Path mPath;
    private boolean mIsRtl;

    public PercentageProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mIsRtl = Dimensions.isRtl();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PercentageProgressBar);
        Resources res = context.getResources();
        mTrackHeight = a.getDimension(R.styleable.PercentageProgressBar_barTrackHeight,
                res.getDimension(R.dimen.percentage_progress_bar_default_track_height));
        mThumbHeight = a.getDimension(R.styleable.PercentageProgressBar_thumbHeight,
                res.getDimension(R.dimen.percentage_progress_bar_default_thumb_height));
        mThumbCenterHeight = a.getDimension(R.styleable.PercentageProgressBar_thumbCenterHeight,
                res.getDimension(R.dimen.percentage_progress_bar_default_thumb_center_height));
        a.recycle();

        mBarColorLeft = ContextCompat.getColor(context,
                R.color.live_wallpaper_loading_progress_bar_left);
        mBarColorRight = ContextCompat.getColor(context,
                R.color.live_wallpaper_loading_progress_bar_right);

        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackPaint.setColor(Color.GRAY);
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setColor(Color.BLUE);
        mThumbCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbCenterPaint.setColor(Color.WHITE);

        mRectF = new RectF();
        mPath = new Path();
    }

    public void setProgress(float progress) {
        if (!equals(progress, mProgress)) {
            mProgress = progress;
            invalidate();
        }
    }

    private static boolean equals(float a, float b) {
        return Math.abs(a - b) < 0.0005;
    }

    float getThumbCenterX() {
        if (mIsRtl) {
            return getWidth() - getPaddingRight() - mThumbHeight / 2 - mProgress * getThumbMoveDistance();
        } else {
            return getPaddingLeft() + mThumbHeight / 2 + mProgress * getThumbMoveDistance();
        }

    }

    private float getThumbMoveDistance() {
        return getWidth() - getPaddingLeft() - getPaddingRight() - mThumbHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = (int) Math.round(Math.max(Math.ceil(mTrackHeight), Math.ceil(mThumbHeight)));
        setMeasuredDimension(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float parallaxCorrectionRatio;
        if (mIsRtl) {
            drawRtlTrack(canvas);
            parallaxCorrectionRatio = drawRtlBar(canvas);
        } else {
            drawLtrTrack(canvas);
            parallaxCorrectionRatio = drawLtrBar(canvas);
        }
        drawThumbCenter(canvas, parallaxCorrectionRatio);
    }

    private void drawLtrTrack(Canvas canvas) {
        float left = getPaddingLeft();
        float right = getWidth() - getPaddingLeft();
        float centerY = getHeight() / 2f;
        float halfTrackHeight = mTrackHeight / 2f;
        float halfThumbHeight = mThumbHeight / 2f;
        float trackTopY = centerY - halfTrackHeight;
        float trackBottomY = centerY + halfTrackHeight;

        Path path = mPath;
        path.reset();
        path.moveTo(left + halfThumbHeight, trackTopY);
        path.lineTo(right - halfThumbHeight, trackTopY);
        mRectF.set(right - halfThumbHeight - halfTrackHeight, trackTopY,
                right - halfThumbHeight + halfTrackHeight, trackBottomY);
        path.arcTo(mRectF, -90f, 180f);
        path.lineTo(left + halfThumbHeight, trackBottomY);
        mRectF.set(left + halfThumbHeight - halfTrackHeight, trackTopY,
                left + halfThumbHeight + halfTrackHeight, trackBottomY);
        path.arcTo(mRectF, 90f, 180f);
        path.close();

        canvas.drawPath(path, mTrackPaint);
    }

    private void drawRtlTrack(Canvas canvas) {
        float right = getWidth() - getPaddingRight();
        float left = getPaddingLeft();
        float centerY = getHeight() / 2f;
        float halfTrackHeight = mTrackHeight / 2f;
        float halfThumbHeight = mThumbHeight / 2f;
        float trackTopY = centerY - halfTrackHeight;
        float trackBottomY = centerY + halfTrackHeight;

        Path path = mPath;
        path.reset();
        path.moveTo(right - halfThumbHeight, trackTopY);
        path.lineTo(left + halfThumbHeight, trackTopY);
        mRectF.set(left + halfThumbHeight - halfTrackHeight, trackTopY,
                left + halfThumbHeight + halfTrackHeight, trackBottomY);
        path.arcTo(mRectF, -90f, -180f);
        path.lineTo(right - halfThumbHeight, trackBottomY);
        mRectF.set(right - halfThumbHeight - halfTrackHeight, trackTopY,
                right - halfThumbHeight + halfTrackHeight, trackBottomY);
        path.arcTo(mRectF, 90f, -180f);
        path.close();

        canvas.drawPath(path, mTrackPaint);
    }

    private float drawLtrBar(Canvas canvas) {
        float thumbCenterX = getThumbCenterX();
        float halfTrackHeight = mTrackHeight / 2f;
        float halfThumbHeight = mThumbHeight / 2f;
        float parallaxCorrectionRatio = 0f;
        float centerY = getHeight() / 2f;
        float trackTopY = centerY - halfTrackHeight;
        float trackBottomY = centerY + halfTrackHeight;
        float thumbTopY = centerY - halfThumbHeight;
        float thumbBottomY = centerY + halfThumbHeight;
        float left = getPaddingLeft();
        float right = thumbCenterX + halfThumbHeight;
        float trackLeftCenterX = left + halfThumbHeight;
        float trackLeftX = trackLeftCenterX - halfTrackHeight;
        float thumbCenterXThreshold = trackLeftCenterX
                + (float) Math.sqrt(halfThumbHeight * halfThumbHeight - halfTrackHeight * halfTrackHeight);
        Shader background = new LinearGradient(
                Math.min(left, right - 4f * mTrackHeight), centerY, right, centerY,
                mBarColorLeft, mBarColorRight, Shader.TileMode.CLAMP);
        mBarPaint.setShader(background);

        Path path = mPath;
        path.reset();
        // Case 1: thumb circle completely overlaps left half-circle of the track
        if (thumbCenterX - halfThumbHeight < trackLeftX) {
            // Just draw the thumb as a circle
            canvas.drawCircle(thumbCenterX, centerY, halfThumbHeight, mBarPaint);
        }

        // Case 2: thumb circle reveals part of left half-circle of the track,
        //         but not any part of horizontal top / bottom edges
        else if (thumbCenterX < thumbCenterXThreshold) {
            // Just draw very left part of the track and the thumb as two circles
            canvas.drawCircle(trackLeftCenterX, centerY, halfTrackHeight, mBarPaint);
            canvas.drawCircle(thumbCenterX, centerY, halfThumbHeight, mBarPaint);
        } else {
            float tailEndX;
            parallaxCorrectionRatio = Math.min(1f, (thumbCenterX - thumbCenterXThreshold) / halfThumbHeight);

            // Case 3: thumb circle reveals horizontal top / bottom edges of the track,
            //         but not long enough to contain full-length "tail" of the thumb
            if (right - 3f * halfThumbHeight < trackLeftCenterX) {
                tailEndX = trackLeftCenterX;
            }

            // Case 4: normal case
            else {
                tailEndX = right - 3 * halfThumbHeight;
            }

            path.moveTo(trackLeftCenterX, trackTopY);
            path.lineTo(tailEndX, trackTopY);
            path.cubicTo(
                    0.33f * thumbCenterX + 0.67f * tailEndX, trackTopY, // Control 1
                    0.67f * thumbCenterX + 0.33f * tailEndX, thumbTopY, // Control 2
                    thumbCenterX, thumbTopY); // Dst
            mRectF.set(right - mThumbHeight, thumbTopY, right, thumbBottomY);
            path.arcTo(mRectF, -90f, 180f);
            path.cubicTo(
                    0.67f * thumbCenterX + 0.33f * tailEndX, thumbBottomY, // Control 2
                    0.33f * thumbCenterX + 0.67f * tailEndX, trackBottomY, // Control 1
                    tailEndX, trackBottomY); // Dst
            path.lineTo(trackLeftCenterX, trackBottomY);
            mRectF.set(trackLeftX, trackTopY,
                    trackLeftX + mTrackHeight, trackBottomY);
            path.arcTo(mRectF, 90f, 180f);
            path.close();
            canvas.drawPath(path, mBarPaint);
        }

        return parallaxCorrectionRatio;
    }

    private float drawRtlBar(Canvas canvas) {
        float thumbCenterX = getThumbCenterX();
        float halfTrackHeight = mTrackHeight / 2f;
        float halfThumbHeight = mThumbHeight / 2f;
        float parallaxCorrectionRatio = 0f;
        float centerY = getHeight() / 2f;
        float trackTopY = centerY - halfTrackHeight;
        float trackBottomY = centerY + halfTrackHeight;
        float thumbTopY = centerY - halfThumbHeight;
        float thumbBottomY = centerY + halfThumbHeight;
        float thumbCenterXThreshold;
        float left = getWidth() - getPaddingRight();
        float right = thumbCenterX - halfThumbHeight;
        float trackLeftCenterX = left - halfThumbHeight;
        float trackLeftX = trackLeftCenterX + halfTrackHeight;
        thumbCenterXThreshold = trackLeftCenterX
                - (float) Math.sqrt(halfThumbHeight * halfThumbHeight - halfTrackHeight * halfTrackHeight);
        Shader background = new LinearGradient(
                Math.min(left, right - 4f * mTrackHeight), centerY, right, centerY,
                mBarColorLeft, mBarColorRight, Shader.TileMode.CLAMP);
        mBarPaint.setShader(background);

        Path path = mPath;
        path.reset();

        // Case 1: thumb circle completely overlaps left half-circle of the track
        if (thumbCenterX + halfThumbHeight > trackLeftX) {
            // Just draw the thumb as a circle
            canvas.drawCircle(thumbCenterX, centerY, halfThumbHeight, mBarPaint);
        }

        // Case 2: thumb circle reveals part of left half-circle of the track,
        //         but not any part of horizontal top / bottom edges
        else if (thumbCenterX > thumbCenterXThreshold) {
            // Just draw very left part of the track and the thumb as two circles
            canvas.drawCircle(trackLeftCenterX, centerY, halfTrackHeight, mBarPaint);
            canvas.drawCircle(thumbCenterX, centerY, halfThumbHeight, mBarPaint);
        } else {
            float tailEndX;
            parallaxCorrectionRatio = Math.min(1f, (thumbCenterXThreshold - thumbCenterX) / halfThumbHeight);

            // Case 3: thumb circle reveals horizontal top / bottom edges of the track,
            //         but not long enough to contain full-length "tail" of the thumb
            if (right + 3f * halfThumbHeight > trackLeftCenterX) {
                tailEndX = trackLeftCenterX;
            }

            // Case 4: normal case
            else {
                tailEndX = right + 3 * halfThumbHeight;
            }

            path.moveTo(trackLeftCenterX, trackTopY);
            path.lineTo(tailEndX, trackTopY);
            path.cubicTo(
                    thumbCenterX + 0.67f * (tailEndX - thumbCenterX), trackTopY, // Control 1
                    thumbCenterX + 0.33f * (tailEndX - thumbCenterX), thumbTopY, // Control 2
                    thumbCenterX, thumbTopY); // Dst
            mRectF.set(right, thumbTopY, right + mThumbHeight, thumbBottomY);
            path.arcTo(mRectF, -90f, -180f);
            path.cubicTo(
                    thumbCenterX + 0.33f * (tailEndX - thumbCenterX), thumbBottomY, // Control 2
                    thumbCenterX + 0.67f * (tailEndX - thumbCenterX), trackBottomY, // Control 1
                    tailEndX, trackBottomY); // Dst
            path.lineTo(trackLeftCenterX, trackBottomY);
            mRectF.set(trackLeftX - mTrackHeight, trackTopY,
                    trackLeftX, trackBottomY);
            path.arcTo(mRectF, 90f, -180f);
            path.close();
            canvas.drawPath(path, mBarPaint);
        }
        return parallaxCorrectionRatio;
    }

    private void drawThumbCenter(Canvas canvas, float parallaxCorrectionRatio) {
        mPath.reset();

        float temp = -(0.15f * parallaxCorrectionRatio * mTrackHeight);
        if (mIsRtl) {
            temp = -temp;
        }

        // Horizontally offset a maximum amount of 0.15f * mTrackHeight to remove parallax
        canvas.drawCircle(getThumbCenterX() + temp, getHeight() / 2f,
                mThumbCenterHeight / 2f, mThumbCenterPaint);
    }
}

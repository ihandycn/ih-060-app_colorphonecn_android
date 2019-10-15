package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.R;
import com.superapps.util.Dimensions;

public class SeekCircleProgressBar extends View {

    private static final int TRIANGLE_MARGIN = 6;
    private static final int TRIANGLE_VERTICAL_LENGTH = 18;
    protected float mRadius;
    protected int mMaxProgress = 100;
    protected float mCenterX;
    protected float mCenterY;
    protected boolean mIsTriangleEnable;
    private int mCurProgress = 0;
    private float barWidth = 3.3f;
    private float barHeight = 36f;
    private int barBackColor = Color.parseColor("#d8d7d7");
    private int barForeColor = Color.WHITE;
    private int mTriangleMargin = TRIANGLE_MARGIN;
    private int mTriangleVerticalLength = TRIANGLE_VERTICAL_LENGTH;
    private float mProgressFactor = 1.0f;
    private RectF mSectionRect = new RectF();
    private Paint mPaint;

    {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public SeekCircleProgressBar(Context context) {
        super(context);
    }

    public SeekCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekCircleProgressBar);
        mMaxProgress = a.getInt(R.styleable.SeekCircleProgressBar_seekBarMaxProgress, 100);
        barBackColor = a.getColor(R.styleable.SeekCircleProgressBar_seekBarBackColor, Color.parseColor("#d8d7d7"));
        barForeColor = a.getColor(R.styleable.SeekCircleProgressBar_seekBarForeColor, Color.WHITE);
        barWidth = a.getDimension(R.styleable.SeekCircleProgressBar_seekBarLineWidth, Dimensions.pxFromDp(12));
        barHeight = a.getDimension(R.styleable.SeekCircleProgressBar_seekBarLineHeight, Dimensions.pxFromDp(12));
        mIsTriangleEnable = a.getBoolean(R.styleable.SeekCircleProgressBar_seekBarTriangleEnable, false);
        mProgressFactor = (float)mMaxProgress / 100f;
        int screenHeight = Dimensions.getPhoneHeight(context);
        mTriangleMargin = TRIANGLE_MARGIN * screenHeight / CommonUtils.DEFAULT_DEVICE_SCREEN_HEIGHT;
        mTriangleVerticalLength = TRIANGLE_VERTICAL_LENGTH * screenHeight / CommonUtils.DEFAULT_DEVICE_SCREEN_HEIGHT;
        a.recycle();
    }

    /**
     * progress:（0-100）
     */
    public void setProgress(int progress) {
        this.mCurProgress = progress > 100 ? 100 : progress;
        this.mCurProgress = this.mCurProgress < 0 ? 0 : this.mCurProgress;
        this.mCurProgress = Math.round(mCurProgress * mProgressFactor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateDimensions(getWidth(), getHeight());
        canvas.translate(mCenterX, mCenterY);
        float rotation = 360.0f / (float) mMaxProgress;
        for (int i = 0; i < mMaxProgress; ++i) {
            canvas.save();
            canvas.rotate((float) i * rotation);
            canvas.translate(0, -mRadius);
            mPaint.setColor(i < mCurProgress ? barForeColor : barBackColor);
            canvas.drawRect(mSectionRect, mPaint);
            if (i == mCurProgress - 1) {
                drawTriangle(canvas);
            }
            canvas.restore();
        }
    }

    private void updateDimensions(int width, int height) {
        // Update center position
        mCenterX = width / 2.0f;
        mCenterY = height / 2.0f;

        // Find shortest dimension
        int diameter = Math.min(width, height);

        int triangleHeightWithMargin = getTriangleHeightWithMargin();
        float outerRadius = diameter / 2 - triangleHeightWithMargin;
        mRadius = outerRadius - barHeight / 2;

        mSectionRect.set(-barWidth / 2, -barHeight / 2, barWidth / 2, barHeight / 2);
    }

    private void drawTriangle(Canvas canvas) {
        if (!mIsTriangleEnable) {
            return;
        }
        Point a = new Point(0, (int)(-barHeight / 2) - mTriangleMargin);
        Point b = new Point(-mTriangleVerticalLength, (int)(-barHeight / 2) - mTriangleMargin - mTriangleVerticalLength);
        Point c = new Point(mTriangleVerticalLength, (int)(-barHeight / 2) - mTriangleMargin - mTriangleVerticalLength);

        Path path = new Path();
        path.moveTo(a.x, a.y);
        path.lineTo(b.x, b.y);
        path.lineTo(c.x, c.y);
        path.close();
        mPaint.setColor(Color.WHITE);
        canvas.drawPath(path, mPaint);
    }

    public int getTriangleHeightWithMargin() {
        return mIsTriangleEnable ? mTriangleMargin + mTriangleVerticalLength : 0;
    }
}

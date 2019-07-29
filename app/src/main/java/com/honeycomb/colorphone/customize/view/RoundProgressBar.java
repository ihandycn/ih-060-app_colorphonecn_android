package com.honeycomb.colorphone.customize.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

/**
 * 可在后台线程中更新进度的进度条
 */
public class RoundProgressBar extends View {

    public static final int STYLE_STROKE = 0;
    public static final int STYLE_FILL = 1;

    /**
     * 圆环的颜色
     */
    private int mRoundColor;

    /**
     * 圆环进度的颜色
     */
    private int mRoundProgressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private int mTextColor;

    /**
     * 中间进度百分比的字符串的字号
     */
    private float mTextSize;

    /**
     * 圆环的宽度
     */
    private float mRoundWidth;

    /**
     * 最大进度
     */
    private int mMax;

    /**
     * 当前进度
     */
    private int mProgress;

    /**
     * 是否显示中间的进度
     */
    private boolean mDisplayProgressText;

    /**
     * 进度的风格，实心或者空心
     */
    private int mStyle;

    private Paint mPaint;
    private RectF mRectF = new RectF();

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBar);

        // 获取自定义属性和默认值
        mRoundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor,
                ContextCompat.getColor(context, R.color.wallpaper_progressbar_bg));
        mRoundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor,
                ContextCompat.getColor(context, R.color.wallpaper_progressbar_progress));
        mTextColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.WHITE);
        mTextSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);
        mRoundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
        mMax = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        mDisplayProgressText = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
        mStyle = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画最外层的大圆环
        int centre = getWidth() / 2; // 获取圆心的 x 坐标
        int radius = (int) (centre - mRoundWidth / 2); // 圆环的半径
        mPaint.setColor(mRoundColor); // 设置圆环的颜色
        mPaint.setStyle(Paint.Style.STROKE); // 设置空心
        mPaint.setStrokeWidth(mRoundWidth); // 设置圆环的宽度
        mPaint.setAntiAlias(true);  // 消除锯齿
        canvas.drawCircle(centre, centre, radius, mPaint); // 画出圆环
        HSLog.e("log", centre + "");

        // 画进度百分比
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
        int percent = (int) (((float) mProgress / (float) mMax) * 100);  // 中间的进度百分比
        float textWidth = mPaint.measureText(percent + "%");   // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间

        if (mDisplayProgressText && percent != 0 && mStyle == STYLE_STROKE) {
            canvas.drawText(percent + "%", centre - textWidth / 2, centre + mTextSize / 2, mPaint); // 画出进度百分比
        }

        // 画圆弧 ，画圆环的进度
        // 设置进度是实心还是空心
        mPaint.setStrokeWidth(mRoundWidth); // 设置圆环的宽度
        mPaint.setColor(mRoundProgressColor); // 设置进度的颜色
        mRectF.set(centre - radius, centre - radius, centre + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限

        switch (mStyle) {
            case STYLE_STROKE: {
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(mRectF, 0, 360 * mProgress / mMax, false, mPaint);  // 根据进度画圆弧
                break;
            }
            case STYLE_FILL: {
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (mProgress != 0) {
                    canvas.drawArc(mRectF, 0, 360 * mProgress / mMax, true, mPaint);  // 根据进度画圆弧
                }
                break;
            }
        }
    }

    public synchronized int getMax() {
        return mMax;
    }

    /**
     * 设置进度的最大值
     */
    public synchronized void setMax(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max not less than 0");
        }
        this.mMax = max;
    }

    /**
     * 获取进度.需要同步
     */
    public synchronized int getProgress() {
        return mProgress;
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线程的问题，需要同步
     * 刷新界面调用postInvalidate()能在非 UI 线程刷新
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > mMax) {
            progress = mMax;
        }
        if (progress <= mMax) {
            this.mProgress = progress;
            postInvalidate();
        }
    }

    public int getCricleColor() {
        return mRoundColor;
    }

    public void setCricleColor(int cricleColor) {
        this.mRoundColor = cricleColor;
    }

    public int getCricleProgressColor() {
        return mRoundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {
        this.mRoundProgressColor = cricleProgressColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    public float getRoundWidth() {
        return mRoundWidth;
    }

    public void setRoundWidth(float roundWidth) {
        this.mRoundWidth = roundWidth;
    }
}

package com.honeycomb.colorphone.battery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

public class BatteryTextView extends View {

    private static final int DEFAULT_SYMBOL_INTERVAL = Dimensions.pxFromDp(2);

    private Paint mNumPaint;
    private Paint mSymbolPaint;

    private String mHour = "";
    private String mMin;

    private boolean mEmptyHour;

    public BatteryTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY || widthSize <= 0 || heightMode != MeasureSpec.EXACTLY ||
                heightSize <= 0) {
            throw new IllegalStateException("Width and height must be fixed");
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init() {
        Typeface typeface = Fonts.getTypeface(Fonts.Font.ROBOTO_MEDIUM, Typeface.NORMAL);
        mNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNumPaint.setTypeface(typeface);
        mNumPaint.setTextSize(Dimensions.pxFromDp(43));
        mNumPaint.setColor(Color.BLACK);
        mSymbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSymbolPaint.setTypeface(typeface);
        mSymbolPaint.setTextSize(Dimensions.pxFromDp(22));
        mSymbolPaint.setColor(Color.BLACK);
    }

    public void setHour(int hour) {
        if (hour == 0) {
            mHour = "";
            mEmptyHour = true;
        } else {
            mHour = String.valueOf(hour);
            mEmptyHour = false;
        }
        invalidate();
    }

    public void setMinute(int min) {
        mMin = String.valueOf(min);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (TextUtils.isEmpty(mMin)) {
            return;
        }

        float numAscent = -mNumPaint.ascent();
        float numDescent = mNumPaint.descent();
        float numHeight = numAscent + numDescent;
        float commonBaseline = getHeight() / 2 + numHeight / 2 - numDescent;
        float offsetX = 0;

        if (!mEmptyHour) {
            canvas.drawText(mHour, 0, commonBaseline, mNumPaint);
            offsetX = mNumPaint.measureText(mHour);
            canvas.drawText("H", offsetX, commonBaseline, mSymbolPaint);
            offsetX += (mSymbolPaint.measureText("H") + DEFAULT_SYMBOL_INTERVAL);
        }

        canvas.drawText(mMin, offsetX, commonBaseline, mNumPaint);
        offsetX += mNumPaint.measureText(mMin);
        canvas.drawText("M", offsetX, commonBaseline, mSymbolPaint);
    }
}

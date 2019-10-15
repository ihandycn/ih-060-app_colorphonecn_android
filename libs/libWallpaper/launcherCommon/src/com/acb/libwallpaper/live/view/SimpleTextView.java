package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.acb.libwallpaper.R;
import com.superapps.util.Dimensions;

public class SimpleTextView extends View {

    private String mText;
    private Paint mPaint;
    private int mTextHeight;
    private int mTextWidth;
    private int mTextTop;

    public SimpleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleTextView);
        int textSize = (int) a.getDimension(R.styleable.SimpleTextView_simpleTextSize, Dimensions.pxFromDp(12));
        int textColor = a.getColor(R.styleable.SimpleTextView_simpleTextColor, Color.BLACK);
        a.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
        Paint.FontMetrics hMetrics = mPaint.getFontMetrics();
        mTextHeight = (int) (hMetrics.bottom - hMetrics.top);
        mTextTop = (int) Math.abs(hMetrics.top);
    }

    public void setText(String text) {
        mText = text;
        mTextWidth = (int) mPaint.measureText(text);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (!TextUtils.isEmpty(mText)) {
            canvas.drawText(mText, (width - mTextWidth) / 2, (height - mTextHeight) / 2 + mTextTop, mPaint);
        }
    }
}

package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

 import com.honeycomb.colorphone.R;
import com.superapps.util.Fonts;
import com.superapps.view.TypefacedTextView;


public class NoPaddingTextView extends TypefacedTextView {

    private final Paint mPaint = new Paint();
    private final Rect mBounds = new Rect();
    private float mTextSize;
    private boolean mPaddingTop;

    public NoPaddingTextView(Context context) {
        this(context, null, android.R.attr.textViewStyle);
    }

    public NoPaddingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public NoPaddingTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NoPaddingTextView, defStyle, 0);
        mPaddingTop = a.getBoolean(0, false);
    }

    private void init() {
        setIncludeFontPadding(false);
        Typeface face = Fonts.getTypeface(getResources().getString(R.string.weather_clock_widget_time_typeface_file_name));
        mPaint.setTypeface(face);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        final String text = calculateTextParams();
        int dy = getMeasuredHeight() + mBounds.top;
        // modify clock position, it should in the center
        canvas.translate(0, dy/2);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getCurrentTextColor());
        canvas.drawText(text, -mBounds.left, -mBounds.top, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculateTextParams();
        int measuredHeight = -mBounds.top + 3;
        if (mPaddingTop) measuredHeight += 3;
        setMeasuredDimension(mBounds.width() + 1, measuredHeight);
    }

    @Override
    public void setTextSize(float size) {
        mTextSize =size;
        super.setTextSize(size);
    }

    private String calculateTextParams() {
        final String text = getText().toString();
        final int textLength = text.length();
        if (mTextSize == 0){
            mTextSize = getTextSize();
        }
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(text, 0, textLength, mBounds);
        if (textLength == 0) {
            mBounds.right = mBounds.left;
        }
        return text;
    }
}

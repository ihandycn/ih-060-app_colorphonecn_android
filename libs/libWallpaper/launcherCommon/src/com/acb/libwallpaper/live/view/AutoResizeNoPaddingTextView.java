package com.acb.libwallpaper.live.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import com.acb.libwallpaper.R;
import com.superapps.util.Fonts;
import com.superapps.view.TypefacedTextView;

/**
 * From: https://github.com/AndroidDeveloperLB/AutoFitTextView/blob/master/AutoFitTextViewLibrary/src/com/lb/auto_fit_textview/AutoResizeTextView.java
 * <p>
 * A textView that is able to self-adjust its font size depending on the min and max size of the font, and its own size.<br/>
 * code is heavily based on this StackOverflow thread:
 * http://stackoverflow.com/questions/16017165/auto-fit-textview-for-android/21851239#21851239 <br/>
 * It should work fine with most Android versions, but might have some issues on Android 3.1 - 4.04, as setTextSize will only work for the first time. <br/>
 * More info here: https://code.google.com/p/android/issues/detail?id=22493 and here in case you wish to fix it: http://stackoverflow.com/a/21851239/878126
 */
public class AutoResizeNoPaddingTextView extends TypefacedTextView {

    private static final int NO_LINE_LIMIT = -1;
    private static final String TAG = AutoResizeNoPaddingTextView.class.getSimpleName();

    private final RectF _availableSpaceRect = new RectF();
    private final SizeTester _sizeTester;
    private float _textScale = 1f;
    private float _maxTextSize;
    private float _minTextSize;
    private int _maxLines;
    private boolean _initialized = false;
    private TextPaint _paint;

    private final Rect mBounds = new Rect();
    private boolean mBottom = false;

    public AutoResizeNoPaddingTextView(final Context context) {
        this(context, null, android.R.attr.textViewStyle);
    }

    public AutoResizeNoPaddingTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public AutoResizeNoPaddingTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AutoResizeNoPaddingTextView, defStyle, 0);
        mBottom = a.getBoolean(0, false);

        setIncludeFontPadding(false);
        Typeface face = Fonts.getTypeface(getResources().getString(R.string.weather_clock_widget_time_typeface_file_name));
        _paint = new TextPaint(getPaint());
        _paint.setTypeface(face);

        // using the minimal recommended font size
        _minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        _maxTextSize = getTextSize();
        if (_maxLines == 0)
            // no value was assigned during construction
            _maxLines = NO_LINE_LIMIT;
        // prepare size tester:
        _sizeTester = new SizeTester() {
            final RectF textRect = new RectF();

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public int onTestSize(final int suggestedSize, final RectF availableSpace) {
                _paint.setTextSize(suggestedSize);
                final TransformationMethod transformationMethod = getTransformationMethod();
                final String text;
                if (transformationMethod != null)
                    text = transformationMethod.getTransformation(getText(), AutoResizeNoPaddingTextView.this).toString();
                else
                    text = getText().toString();
                _paint.getTextBounds(text, 0, text.length(), mBounds);
                textRect.bottom = mBounds.height() + 3;
                textRect.right = mBounds.width();
                textRect.offsetTo(0, 0);
                if (availableSpace.contains(textRect))
                    // may be too small, don't worry we will find the best match
                    return -1;
                // else, too big
                return 1;
            }
        };
        _initialized = true;
    }

    public void setTextScale(float scale) {
        _textScale = scale;
    }

    @Override
    public void setAllCaps(boolean allCaps) {
        super.setAllCaps(allCaps);
        adjustTextSize();
    }

    @Override
    public void setTypeface(final Typeface tf) {
        super.setTypeface(tf);
        adjustTextSize();
    }

    @Override
    public void setTextSize(final float size) {
        _maxTextSize = size;
        adjustTextSize();
    }

    @Override
    public void setMaxLines(final int maxLines) {
        super.setMaxLines(maxLines);
        _maxLines = maxLines;
        adjustTextSize();
    }

    @Override
    public int getMaxLines() {
        return _maxLines;
    }

    @Override
    public void setSingleLine() {
        super.setSingleLine();
        _maxLines = 1;
        adjustTextSize();
    }

    @Override
    public void setSingleLine(final boolean singleLine) {
        super.setSingleLine(singleLine);
        if (singleLine)
            _maxLines = 1;
        else _maxLines = NO_LINE_LIMIT;
        adjustTextSize();
    }

    @Override
    public void setLines(final int lines) {
        super.setLines(lines);
        _maxLines = lines;
        adjustTextSize();
    }

    @Override
    public void setTextSize(final int unit, final float size) {
        final Context c = getContext();
        Resources r;
        if (c == null)
            r = Resources.getSystem();
        else r = c.getResources();
        _maxTextSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        adjustTextSize();
    }

    @Override
    public void setLineSpacing(final float add, final float mult) {
        super.setLineSpacing(add, mult);
    }

    private void adjustTextSize() {
        // This is a workaround for truncated text issue on ListView, as shown here: https://github.com/AndroidDeveloperLB/AutoFitTextView/pull/14
        // TODO think of a nicer, elegant solution.
        if (!_initialized)
            return;
        final int startSize = (int) _minTextSize;
        final int heightLimit = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        int _widthLimit = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
        if (_widthLimit <= 0)
            return;
        _availableSpaceRect.right = _widthLimit;
        _availableSpaceRect.bottom = heightLimit;
        superSetTextSize(startSize);
    }

    private void superSetTextSize(int startSize) {
        // each time binary search will decrease text size
        int textSize = binarySearch(startSize, (int) _maxTextSize, _sizeTester, _availableSpaceRect);
        textSize = Math.round(_textScale * textSize);
        _maxTextSize = textSize;
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    private int binarySearch(final int start, final int end, final SizeTester sizeTester, final RectF availableSpace) {
        int lastBest = start, lo = start, hi = end, mid;
        while (lo <= hi) {
            mid = lo + hi >>> 1;
            final int midValCmp = sizeTester.onTestSize(mid, availableSpace);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
                lastBest = hi;
            } else return mid;
        }
        // make sure to return last best
        // this is what should always be returned
        return lastBest;
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        super.onTextChanged(text, start, before, after);
        adjustTextSize();
    }

    @Override
    protected void onSizeChanged(final int width, final int height, final int oldwidth, final int oldheight) {
        super.onSizeChanged(width, height, oldwidth, oldheight);
        if (width != oldwidth || height != oldheight) {
            adjustTextSize();
        }
    }

    private interface SizeTester {
        /**
         * @param suggestedSize  Size of text to be tested
         * @param availableSpace available space in which text must fit
         * @return an integer < 0 if after applying {@code suggestedSize} to
         * text, it takes less space than {@code availableSpace}, > 0
         * otherwise
         */
        int onTestSize(int suggestedSize, RectF availableSpace);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int dy = getMeasuredHeight() + mBounds.top;
        // modify clock position, it should in the center
        if (!mBottom) dy /= 2;
        else if (dy >= 3) dy -= 3;
        Log.i(TAG, "onDraw: measureHeight " + getMeasuredHeight() + " mBounds " + -mBounds.top + " Dy " + dy);
        canvas.translate(0, dy);
        _paint.setAntiAlias(true);
        _paint.setColor(getCurrentTextColor());
        canvas.drawText(getText().toString(), -mBounds.left, -mBounds.top, _paint);
    }
}

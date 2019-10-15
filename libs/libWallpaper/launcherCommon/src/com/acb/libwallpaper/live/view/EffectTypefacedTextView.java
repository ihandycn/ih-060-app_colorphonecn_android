package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.acb.libwallpaper.R;
import com.superapps.util.Fonts;
import com.superapps.view.DebuggableTextView;

public class EffectTypefacedTextView extends DebuggableTextView {

    private Drawable mTopDrawable;

    public EffectTypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
        int fontType = styledAttrs.getResourceId(R.styleable.TypefacedTextView_typeface, R.string.roboto_regular);
        int fontStyle = styledAttrs.getInt(R.styleable.TypefacedTextView_font_style, 0);
        int drawableWidth = styledAttrs.getDimensionPixelSize(R.styleable.TypefacedTextView_drawable_width, -1);
        int drawableHeight = styledAttrs.getDimensionPixelSize(R.styleable.TypefacedTextView_drawable_height, -1);
        styledAttrs.recycle();

        Typeface typeface = Fonts.getTypeface(Fonts.Font.ofFontResId(fontType), fontStyle);
        if (typeface != null) {
            setTypeface(typeface);
        }

        if (drawableWidth > 0 && drawableHeight > 0) {
            Drawable[] drawables = getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable == null) {
                    continue;
                }
                Rect bounds = new Rect(drawable.getBounds());
                bounds.set(bounds.left, bounds.top, bounds.left + drawableWidth, bounds.top + drawableHeight);
                drawable.setBounds(bounds);
            }
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        }

        mTopDrawable = getCompoundDrawables()[1];
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        mTopDrawable = getCompoundDrawables()[1];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTopDrawable != null) {
            int height = (int) (mTopDrawable.getBounds().bottom - mTopDrawable.getBounds().top + 1.0f / getLineSpacingMultiplier() * getTextSize() +
                    getPaddingTop() + getPaddingBottom() + getCompoundDrawablePadding() + getLineHeight());
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

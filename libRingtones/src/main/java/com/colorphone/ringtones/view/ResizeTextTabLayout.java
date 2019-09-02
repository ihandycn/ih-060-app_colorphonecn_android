package com.colorphone.ringtones.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colorphone.ringtones.R;
import com.colorphone.ringtones.module.Column;
import com.superapps.util.Dimensions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sundxing
 */
public class ResizeTextTabLayout extends LinearLayout implements View.OnClickListener{

    private int mTabCount;
    private int mSelectedPos = -1;
    @ColorInt
    private int mSelectedTextColor = Color.WHITE;
    private int mSelectedTextSize = Dimensions.pxFromDp(26);

    @ColorInt
    private int mUnSelectedTextColor = 0xaaffffff;
    private int mUnselectedTextSize = Dimensions.pxFromDp(16);


    private TextPaint mMeasurePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private ArgbEvaluator mColorEvaluator = new ArgbEvaluator();
    private FloatEvaluator mSizeEvaluator = new FloatEvaluator();

    private ValueAnimator mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(200);

    private OnTabSelectListener mOnTabSelectListener;
    private int TAB_PADDING = Dimensions.pxFromDp(8);

    public ResizeTextTabLayout(Context context) {
        super(context);
        init();
    }

    public ResizeTextTabLayout(Context context, @Nullable  AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResizeTextTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.ringtone_tab_layout,this);
        setOrientation(HORIZONTAL);
        mTabCount = getChildCount();

        for (int i = 0; i < mTabCount; i++) {
            getChildAt(i).setOnClickListener(this);
        }
    }

    public boolean setSelected(int position) {
        if (position < 0 || position >= mTabCount) {
            throw new IllegalStateException("Selected tab at invalid pos : " + position);
        }
        if (mSelectedPos == position) {
            return false;
        }

        resetAnimator();

        final int lastPos = mSelectedPos;
        final int curPos = mSelectedPos = position;
        doResize(lastPos, curPos);
        mValueAnimator.start();

        if (mOnTabSelectListener != null) {
            mOnTabSelectListener.onTabSelected(position);
        }
        return true;
    }

    private void resetAnimator() {
        mValueAnimator.cancel();
        mValueAnimator.removeAllUpdateListeners();
        mValueAnimator.removeAllListeners();
    }

    private void doResize(int lastPos, int curPos) {
        TextView lastTextView = lastPos >= 0 ? (TextView) getChildAt(lastPos) : null;
        TextView curTextView = (TextView) getChildAt(curPos);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                if (lastTextView != null) {
                    updateTextView(fraction, lastTextView, false);
                }
                updateTextView(fraction, curTextView, true);
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (lastTextView != null) {
                    lastTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mUnselectedTextSize);
                    lastTextView.setTextColor(mUnSelectedTextColor);
                }

                curTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mSelectedTextSize);
                curTextView.setTextColor(mSelectedTextColor);
            }
        });
    }

    private void updateTextView(float fraction, TextView textView, boolean toScaleUp) {
        ViewSizeCache sizeCache = getSizeCache(textView.getText().toString());

        int color = (int) mColorEvaluator.evaluate(fraction,
                toScaleUp ? mUnSelectedTextColor : mSelectedTextColor,
                toScaleUp ? mSelectedTextColor : mUnSelectedTextColor);
        textView.setTextColor(color);

        float size = mSizeEvaluator.evaluate(fraction,
                toScaleUp ? mUnselectedTextSize : mSelectedTextSize,
                toScaleUp ? mSelectedTextSize : mUnselectedTextSize);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

        float textWidth = mSizeEvaluator.evaluate(fraction,
                toScaleUp ? sizeCache.smallSize : sizeCache.largeSize,
                toScaleUp ? sizeCache.largeSize : sizeCache.smallSize
        );

        updateTextWidth(textView, Math.round(textWidth));
    }

    private void updateTextWidth(TextView textView, int textWidth) {
        ViewGroup.LayoutParams lp = textView.getLayoutParams();
        lp.width = textWidth + 2 * TAB_PADDING;
        textView.setLayoutParams(lp);
    }

    private ViewSizeCache getSizeCache(String text) {
        ViewSizeCache sizeCache = mSizeCacheMap.get(text);
        if (sizeCache == null) {
            sizeCache = new ViewSizeCache();

            mMeasurePaint.setTextSize(mSelectedTextSize);
            sizeCache.largeSize = mMeasurePaint.measureText(text);

            mMeasurePaint.setTextSize(mUnselectedTextSize);
            sizeCache.smallSize = mMeasurePaint.measureText(text);
            mSizeCacheMap.put(text, sizeCache);
        }
        return sizeCache;
    }

    public int getSelectedPos() {
        return mSelectedPos;
    }

    public int getSelectedTextColor() {
        return mSelectedTextColor;
    }

    public void setSelectedTextColor(@ColorInt int selectedTextColor) {
        mSelectedTextColor = selectedTextColor;
    }

    public int getSelectedTextSize() {
        return mSelectedTextSize;
    }

    public void setSelectedTextSize(int selectedTextSize) {
        mSelectedTextSize = selectedTextSize;
    }

    @Override
    public void onClick(View view) {
        int index = indexOfChild(view);
        setSelected(index);
    }

    public OnTabSelectListener getOnTabSelectListener() {
        return mOnTabSelectListener;
    }

    public void setOnTabSelectListener(OnTabSelectListener onTabSelectListener) {
        mOnTabSelectListener = onTabSelectListener;
    }

    Map<String, ViewSizeCache> mSizeCacheMap = new HashMap<>();
    public void bindData(@NonNull List<Column> tabColumns) {
        int dataLength = tabColumns.size();
        for (int i = 0; i < mTabCount; i++) {
           TextView tabItemView = (TextView) getChildAt(i);
           if (i < dataLength) {
               String text = tabColumns.get(i).getName();
               final ViewSizeCache sizeCache = getSizeCache(text);
               updateTextWidth(tabItemView, (int) sizeCache.smallSize);
               tabItemView.setText(text);
           } else {
               tabItemView.setVisibility(GONE);
           }
        }
    }

    public interface OnTabSelectListener {
        void onTabSelected(int index);
    }

    private static class ViewSizeCache {
        float smallSize;
        float largeSize;
    }
}

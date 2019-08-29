package com.colorphone.ringtones.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colorphone.ringtones.R;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

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

    private ArgbEvaluator mColorEvaluator = new ArgbEvaluator();
    private FloatEvaluator mSizeEvaluator = new FloatEvaluator();

    private ValueAnimator mValueAnimator = ValueAnimator.ofFloat(0, 1).setDuration(200);

    private OnTabSelectListener mOnTabSelectListener;

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
        if (lastPos >= 0) {
            doResize(getChildAt(lastPos), mUnSelectedTextColor, mUnselectedTextSize);
        }
        doResize(getChildAt(curPos), mSelectedTextColor, mSelectedTextSize);
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

    private void doResize(View view, final int endColor, final int endSize) {
        final TextView textView = (TextView) view;
        final float curSize = textView.getTextSize();
        final int curColor = textView.getCurrentTextColor();
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                int color = (int) mColorEvaluator.evaluate(fraction, curColor, endColor);
                textView.setTextColor(color);
                float size = mSizeEvaluator.evaluate(fraction, curSize, endSize);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, endSize);
                textView.setTextColor(endColor);
            }
        });
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
        Toasts.showToast("Select tab = " + index);
        setSelected(index);
    }

    public OnTabSelectListener getOnTabSelectListener() {
        return mOnTabSelectListener;
    }

    public void setOnTabSelectListener(OnTabSelectListener onTabSelectListener) {
        mOnTabSelectListener = onTabSelectListener;
    }

    public interface OnTabSelectListener {
        void onTabSelected(int index);
    }
}

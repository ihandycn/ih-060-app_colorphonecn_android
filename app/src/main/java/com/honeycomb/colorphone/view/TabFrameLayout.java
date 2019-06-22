package com.honeycomb.colorphone.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class TabFrameLayout extends FrameLayout {

    private int mSelectedFramePos = -1;
    private FrameChangeListener mFrameChangeListener;
    private FrameProvider mFrameProvider;

    private SparseArray<View> mTabContentLayoutList = new SparseArray<>();

    private @Nullable View getFrameItem(int pos) {
        if (pos < 0) {
            return null;
        }
        View targetView = mTabContentLayoutList.get(pos);
        if (targetView == null) {
            targetView = mFrameProvider.getFrame(this, pos);
            addView(targetView);
            mTabContentLayoutList.put(pos, targetView);
        }
        return targetView;
    }

    public TabFrameLayout(@NonNull Context context) {
        super(context);
    }

    public TabFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TabFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCurrentItem(int tabPos) {
        boolean changed = mSelectedFramePos != tabPos;
        if (changed) {

            View oldFrame = getFrameItem(mSelectedFramePos);
            if (oldFrame != null) {
                oldFrame.setVisibility(GONE);
            }

            mSelectedFramePos = tabPos;
            View selectedFrame = getFrameItem(tabPos);
            if (selectedFrame != null) {
                selectedFrame.setVisibility(VISIBLE);
            }

            if (mFrameChangeListener != null) {
                mFrameChangeListener.onFrameChanged(tabPos);
            }
        }
    }

    public int getSelectedFramePos() {
        return mSelectedFramePos;
    }

    public FrameChangeListener getFrameChangeListener() {
        return mFrameChangeListener;
    }

    public void setFrameChangeListener(FrameChangeListener frameChangeListener) {
        mFrameChangeListener = frameChangeListener;
    }

    public void setFrameProvider(FrameProvider frameProvider) {
        mFrameProvider = frameProvider;
    }

    public interface FrameChangeListener {
        /**
         * Notify frame item change
         * @param pos
         */
        void onFrameChanged(int pos);
    }

    public interface FrameProvider {
        /**
         * 提供内部内容View
         * @param viewGroup
         * @param pos
         * @return
         */
        View getFrame(ViewGroup viewGroup, int pos);
    }
}

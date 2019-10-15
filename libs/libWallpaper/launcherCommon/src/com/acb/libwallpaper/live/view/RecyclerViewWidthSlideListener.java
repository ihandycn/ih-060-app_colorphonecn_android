package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.ihs.commons.utils.HSLog;

public class RecyclerViewWidthSlideListener extends RecyclerView {

    private static final String TAG = RecyclerViewWidthSlideListener.class.getSimpleName();

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        private int mScrollY;
        private int mStartDraggingScrollY;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    mStartDraggingScrollY = mScrollY;
                    break;
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (mScrollY > mStartDraggingScrollY) {
                        if (mOnSlideListener != null) {
                            mOnSlideListener.slideUp();
                        }
                        HSLog.d(TAG, "Slide up");
                    } else {
                        if (mOnSlideListener != null) {
                            mOnSlideListener.slideDown();
                        }
                        HSLog.d(TAG, "Slide down");
                    }
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mScrollY += dy;
        }
    };

    private OnSlideListener mOnSlideListener;

    public RecyclerViewWidthSlideListener(Context context) {
        this(context, null);
    }

    public RecyclerViewWidthSlideListener(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewWidthSlideListener(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnScrollListener(mOnScrollListener);
    }

    public void setOnSlideListener(OnSlideListener listener) {
        mOnSlideListener = listener;
    }

    public interface OnSlideListener {

        void slideUp();

        void slideDown();
    }

}

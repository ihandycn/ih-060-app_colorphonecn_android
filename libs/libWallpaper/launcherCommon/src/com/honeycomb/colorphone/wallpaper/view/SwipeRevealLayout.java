package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.superapps.util.Dimensions;

public class SwipeRevealLayout extends FrameLayout {

    private ViewDragHelper mDragHelper;
    protected View mBackView;
    protected View mFrontView;

    private boolean mDragEnabled;
    private boolean mIsRtl;

    private Status mStatus = Status.CLOSE;
    private OnSwipeChangeListener mOnSwipeChangeListener;

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        this.mStatus = status;
    }

    public OnSwipeChangeListener getOnSwipeChangeListener() {
        return mOnSwipeChangeListener;
    }

    public void setOnSwipeChangeListener(OnSwipeChangeListener onSwipeChangeListener) {
        this.mOnSwipeChangeListener = onSwipeChangeListener;
    }

    public enum Status {
        CLOSE,
        OPEN,
        DRAGGING,
    }

    public interface OnSwipeChangeListener {
        void onOpen(SwipeRevealLayout layout);

        void onClose(SwipeRevealLayout layout);

        void onDragging(SwipeRevealLayout layout);

        /**
         * May not be called in fast sliding
         */
        void onStartOpen(SwipeRevealLayout layout);

        void onStartClose(SwipeRevealLayout layout);
    }

    public SwipeRevealLayout(Context context) {
        this(context, null);
    }

    public SwipeRevealLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRevealLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDragHelper = ViewDragHelper.create(this, callback);
        mIsRtl = Dimensions.isRtl();
    }

    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mFrontView;
        }

        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (!mDragEnabled)
                return 0;
            if (child == mFrontView) {
                if (mIsRtl) {
                    return Math.max(0, Math.min(left, mRange));
                } else {
                    return Math.max(-mRange, Math.min(left, 0));
                }
            } else if (child == mBackView) {
                return 0;
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            dispatchDragEvent();
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);

            boolean moreThanHalf =
                    mIsRtl ? (mFrontView.getLeft() > mRange * 0.5f) : (mFrontView.getLeft() < -mRange * 0.5f);
            boolean movingOpen = mIsRtl ? (xvel > 0) : (xvel < 0);
            if (xvel == 0 && moreThanHalf) {
                open();
            } else if (movingOpen) {
                open();
            } else {
                close();
            }
        }
    };

    private int mHeight;
    private int mWidth;
    private int mRange;

    protected void setDragEnabled(boolean enabled) {
        mDragEnabled = enabled;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        layoutContent(false);
    }

    protected void dispatchDragEvent() {
        Status preStatus = mStatus;

        mStatus = updateStatus();

        if (mOnSwipeChangeListener == null) {
            return;
        }

        mOnSwipeChangeListener.onDragging(this);

        if (preStatus != mStatus) {
            if (mStatus == Status.CLOSE) {
                mOnSwipeChangeListener.onClose(this);
            } else if (mStatus == Status.OPEN) {
                mOnSwipeChangeListener.onOpen(this);
            } else if (mStatus == Status.DRAGGING) {
                if (preStatus == Status.CLOSE) {
                    mOnSwipeChangeListener.onStartOpen(this);
                } else if (preStatus == Status.OPEN) {
                    mOnSwipeChangeListener.onStartClose(this);
                }
            }

        }

    }

    private Status updateStatus() {
        int left = mFrontView.getLeft();
        if (left == 0) {
            return Status.CLOSE;
        } else if (left == (mIsRtl ? mRange : -mRange)) {
            return Status.OPEN;
        }

        return Status.DRAGGING;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mFrontView, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(false);
        }
    }

    public void open() {
        open(true);
    }

    public void open(boolean isSmooth) {
        if (isSmooth) {
            if (mDragHelper.smoothSlideViewTo(mFrontView, mIsRtl ? mRange : -mRange, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(true);
        }
    }

    private void layoutContent(boolean isOpen) {
        Rect frontRect = computeFrontRect(isOpen);
        mFrontView.layout(frontRect.left, frontRect.top, frontRect.right, frontRect.bottom);

        Rect backRect = computeBackRectViaFront(frontRect);
        mBackView.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);

        bringChildToFront(mFrontView);
    }

    private Rect computeBackRectViaFront(Rect frontRect) {
        if (mIsRtl) {
            int left = frontRect.left;
            return new Rect(left, 0, left + mRange, mHeight);
        } else {
            int right = frontRect.right;
            return new Rect(right - mRange, 0, right, mHeight);
        }
    }

    private Rect computeFrontRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            left = mIsRtl ? mRange : -mRange;
        }
        return new Rect(left, 0, left + mWidth, mHeight);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mHeight = mFrontView.getMeasuredHeight();
        mWidth = mFrontView.getMeasuredWidth();

        mRange = mBackView.getMeasuredWidth();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBackView = getChildAt(0);
        mFrontView = getChildAt(1);
    }
}

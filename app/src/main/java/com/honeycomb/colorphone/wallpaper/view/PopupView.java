package com.honeycomb.colorphone.wallpaper.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Size;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.wallpaper.util.ViewUtils;


public class PopupView {

    private Context mContext;
    private ViewGroup mRootView;
    protected ContainerView mContentViewParent;
    protected View mContentView;
    private ViewGroup.LayoutParams mContentLp;
    protected View mAnchorView;

    private int mBgColor = 0xAA000000;

    private View.OnClickListener mOutSideOnClickListener;

    public PopupView(Activity activity) {
        this(activity, ViewUtils.findViewById(activity, android.R.id.content));
    }

    public PopupView(Context context, ViewGroup rootView) {
        mContext = context;
        mRootView = rootView;
        mContentViewParent = new ContainerView(context);
        mContentViewParent.setFocusableInTouchMode(true);
        mContentViewParent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // Key code home is never delivered to applications
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        mContentLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setContentView(View contentView) {
        if (mContentView != null) {
            mContentViewParent.removeView(mContentView);
        }
        mContentView = contentView;
    }

    protected void show() {
        mContentViewParent.setBackgroundColor(mBgColor);
        mContentViewParent.setOnClickListener(mOutSideOnClickListener);
        if (mContentViewParent.getParent() != null) {
            ((ViewGroup) mContentViewParent.getParent()).removeAllViews();
        }
        mRootView.addView(mContentViewParent, mContentLp);
        mContentViewParent.requestFocus();
    }

    public void showInCenter() {
        // Measure content view
        ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
        int widthSpec;
        if (layoutParams != null && layoutParams.width > 0) {
            widthSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY);
        } else {
            widthSpec = View.MeasureSpec.makeMeasureSpec(mRootView.getWidth(), View.MeasureSpec.AT_MOST);
        }
        int heightSpec;
        if (layoutParams != null && layoutParams.height > 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY);
        } else {
            heightSpec = View.MeasureSpec.makeMeasureSpec(mRootView.getHeight(), View.MeasureSpec.AT_MOST);
        }
        mContentView.measure(widthSpec, heightSpec);

        int left = (mRootView.getWidth() - mContentView.getMeasuredWidth()) / 2;
        int top = (mRootView.getHeight() - mContentView.getMeasuredHeight()) / 2;
        showAtPosition(left, top);
    }

    public void setDropDownAnchor(View anchor) {
        mAnchorView = anchor;
    }

    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    public void showAsDropDown() {
        if (mAnchorView == null) {
            throw new IllegalStateException("Anchor view must not be null");
        }
        int[] dropDownPosition = getDropDownPosition(mAnchorView, 0, 0);
        showAtPosition(dropDownPosition[0], dropDownPosition[1]);
    }

    public void showAsDropDown(View anchor, int xOffset, int yOffset) {
        int[] dropDownPosition = getDropDownPosition(anchor, xOffset, yOffset);
        showAtPosition(dropDownPosition[0], dropDownPosition[1]);
    }

    private @Size(2) int[] getDropDownPosition(View anchor, int xOffset, int yOffset) {
        final int anchorHeight = anchor.getHeight();
        final int[] drawingLocation = new int[2];
        anchor.getLocationInWindow(drawingLocation);
        int left = drawingLocation[0] + xOffset;
        int top = drawingLocation[1] + anchorHeight + yOffset;

        // TODO: 1/5/17 To be optimized like PopupWindow
        // Get display bounds
        final Rect displayFrame = new Rect();
        anchor.getWindowVisibleDisplayFrame(displayFrame);

        // Measure content view
        ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
        int widthSpec;
        if (layoutParams != null && layoutParams.width > 0) {
            widthSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.width, View.MeasureSpec.EXACTLY);
        } else {
            widthSpec = View.MeasureSpec.makeMeasureSpec(mRootView.getWidth(), View.MeasureSpec.AT_MOST);
        }
        int heightSpec;
        if (layoutParams != null && layoutParams.height > 0) {
            heightSpec = View.MeasureSpec.makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY);
        } else {
            heightSpec = View.MeasureSpec.makeMeasureSpec(mRootView.getHeight(), View.MeasureSpec.AT_MOST);
        }
        mContentView.measure(widthSpec, heightSpec);

        // Fix position
        boolean outOfLeftBounds = left < displayFrame.left;
        boolean outOfRightBounds = left + mContentView.getMeasuredWidth() > displayFrame.right;
        if (mContentView.getMeasuredWidth() <= displayFrame.right - displayFrame.left) {
            if (outOfLeftBounds) {
                left = xOffset;
            } else if (outOfRightBounds) {
                left = displayFrame.right + xOffset - mContentView.getMeasuredWidth();
            }
        }
        int[] position = new int[2];
        position[0] = left;
        position[1] = top;
        return position;
    }

    protected void showAtPosition(int x, int y) {
        if (mContentView != null) {
            mContentViewParent.setPopuptPosition(x, y);
            if (mContentView.getParent() != null) {
                mContentView.requestLayout();
            } else {
                ViewGroup.LayoutParams lp;
                if (mContentView.getLayoutParams() != null) {
                    lp = mContentView.getLayoutParams();
                } else {
                    lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                            .LayoutParams.WRAP_CONTENT);
                }
                mContentViewParent.addView(mContentView, lp);
            }
        }
        show();
    }

    public void dismiss() {
        mRootView.removeView(mContentViewParent);
    }

    public void setOutSideBackgroundColor(@ColorInt int color) {
        mBgColor = color;
    }

    protected boolean shouldDispatchTouchEvent() {
        return true;
    }

    public void setOutSideClickListener(View.OnClickListener onClickListener) {
        mOutSideOnClickListener = onClickListener;
    }

    protected class ContainerView extends FrameLayout {

        private int mChildStart;
        private int mChildTop;

        public ContainerView(Context context) {
            super(context);
        }

        void setPopuptPosition(int start, int top) {
            this.mChildStart = start;
            this.mChildTop = top;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            if (getChildCount() > 0) {
                View child = getChildAt(0);
                child.layout(mChildStart, mChildTop, mChildStart + child.getMeasuredWidth(), mChildTop + child.getMeasuredHeight());
            }
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            //noinspection SimplifiableConditionalExpression
            return shouldDispatchTouchEvent() ? super.dispatchTouchEvent(ev) : true;
        }
    }
}

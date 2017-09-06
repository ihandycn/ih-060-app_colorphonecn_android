package com.colorphone.lock.lockscreen.chargingscreen.tipview;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.R;


public class ToolTipView extends LinearLayout implements ViewTreeObserver.OnPreDrawListener, View.OnClickListener {

    private ImageView topPointerView;
    private ImageView bottomPointerView;
    private ViewGroup containerView;
    private TextView toolTipTextView;

    private ToolTip toolTip;
    private View mView;

    private boolean mDimensionsKnown;
    private int mWidth;

    public ToolTipView(final Context context) {
        super(context);

        init();
    }

    private void init() {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
        LayoutInflater.from(getContext()).inflate(R.layout.charging_screen_tooltip, this, true);

        topPointerView = (ImageView) findViewById(R.id.tooltip_pointer_up);
        containerView = (ViewGroup) findViewById(R.id.tooltip_container);
        toolTipTextView = (TextView) findViewById(R.id.tooltip_text_view);
        bottomPointerView = (ImageView) findViewById(R.id.tooltip_pointer_down);

        setOnClickListener(this);
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    @Override
    public boolean onPreDraw() {
        getViewTreeObserver().removeOnPreDrawListener(this);
        mDimensionsKnown = true;

        mWidth = containerView.getWidth();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.width = mWidth;
        setLayoutParams(layoutParams);

        if (toolTip != null) {
            try {
                applyToolTipPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void setToolTip(final ToolTip toolTip, final View view) {
        this.toolTip = toolTip;
        mView = view;

        if (this.toolTip.getText() != null) {
            toolTipTextView.setText(this.toolTip.getText());
        }

        if (this.toolTip.getTextColor() != 0) {
            toolTipTextView.setTextColor(this.toolTip.getTextColor());
        }

        if (this.toolTip.getColor() != 0) {
            setColor(this.toolTip.getColor());
        }

        if (mDimensionsKnown) {
            try {
                applyToolTipPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyToolTipPosition() throws Exception {
        int[] masterViewScreenPosition = new int[2];
        mView.getLocationOnScreen(masterViewScreenPosition);

        Rect viewDisplayFrame = new Rect();
        mView.getWindowVisibleDisplayFrame(viewDisplayFrame);

        int[] parentViewScreenPosition = new int[2];

        View parentView = (View) getParent();
        if(parentView != null) {
            parentView.getLocationOnScreen(parentViewScreenPosition);
        }

        final int masterViewWidth = mView.getWidth();
        final int masterViewHeight = mView.getHeight();

        final int mRelativeMasterViewX = masterViewScreenPosition[0] - parentViewScreenPosition[0];
        final int mRelativeMasterViewY = masterViewScreenPosition[1] - parentViewScreenPosition[1];
        final int relativeMasterViewCenterX = mRelativeMasterViewX + masterViewWidth / 2;

        int toolTipViewAboveY = mRelativeMasterViewY - getHeight();
        int toolTipViewBelowY = Math.max(0, mRelativeMasterViewY + masterViewHeight);

        int toolTipViewX = Math.max(0, relativeMasterViewCenterX - mWidth / 2);
        if (toolTipViewX + mWidth > viewDisplayFrame.right) {
            toolTipViewX = viewDisplayFrame.right - mWidth;
        }

        setX(toolTipViewX);
        setPointerCenterX(relativeMasterViewCenterX);

        final boolean showBelow = toolTipViewAboveY < 0;

        topPointerView.setVisibility(showBelow ? VISIBLE : GONE);
        bottomPointerView.setVisibility(showBelow ? GONE : VISIBLE);

        int toolTipViewY;
        if (showBelow) {
            toolTipViewY = toolTipViewBelowY;
        } else {
            toolTipViewY = toolTipViewAboveY;
        }

        if (toolTip.getAnimationType() == ToolTip.ANIMATOR_TYPE_NONE) {
            ViewCompat.setTranslationY(this, toolTipViewY);
            ViewCompat.setTranslationX(this, toolTipViewX);
        }
    }

    public void setPointerCenterX(final int pointerCenterX) {
        int pointerWidth = Math.max(topPointerView.getMeasuredWidth(), bottomPointerView.getMeasuredWidth());

        ViewCompat.setX(topPointerView, pointerCenterX - pointerWidth / 2 - (int) getX());
        ViewCompat.setX(bottomPointerView, pointerCenterX - pointerWidth / 2 - (int) getX());
    }

    public void setColor(final int color) {
        topPointerView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        bottomPointerView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        //containerView.setBackgroundColor(color);
    }

    public void remove() {
        if (toolTip.getAnimationType() == ToolTip.ANIMATOR_TYPE_NONE) {
            if (getParent() != null) {
                ((ViewManager) getParent()).removeView(this);
            }
        }
    }

    @Override
    public void onClick(final View view) {
        remove();
    }
}

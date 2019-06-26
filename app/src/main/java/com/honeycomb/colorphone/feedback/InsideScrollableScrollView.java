package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.honeycomb.colorphone.util.Utils;

/**
 * A {@link ScrollView} allows inside widgets to be scrollable too.
 */
public class InsideScrollableScrollView extends ScrollView {

    private Rect mScrollableArea = new Rect();
    private Rect mScrollableAreaWithScroll = new Rect();

    public InsideScrollableScrollView(Context context) {
        super(context);
    }

    public InsideScrollableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InsideScrollableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollableDescendant(View descendant) {
        int[] coord = new int[2];
        Utils.getDescendantCoordRelativeToParent(descendant, this, coord, true);
        mScrollableArea.set(coord[0], coord[1], coord[0] + descendant.getWidth(), coord[1] + descendant.getHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mScrollableAreaWithScroll.set(mScrollableArea);
        mScrollableAreaWithScroll.offset(0, -getScrollY());
        if (mScrollableAreaWithScroll.contains((int) ev.getX(), (int) ev.getY())) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}

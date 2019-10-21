package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TabLayout} with {@link #addOnScrollChangeListenerCompat(OnScrollChangeListenerCompat)}.
 */
public class TabLayoutWithScrollListener extends TabLayout {

    /**
     * Interface definition for a callback to be invoked when the scroll
     * X or Y positions of a view change.
     * <p>
     * <b>Note:</b> Some views handle scrolling independently from View and may
     * have their own separate listeners for scroll-type events. For example,
     * {@link android.widget.ListView ListView} allows clients to register an
     * {@link android.widget.ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener) AbsListView.OnScrollListener}
     * to listen for changes in list scroll position.
     *
     * @see #setOnScrollChangeListener(OnScrollChangeListener)
     */
    public interface OnScrollChangeListenerCompat {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v The view whose scroll position has changed.
         * @param scrollX Current horizontal scroll origin.
         * @param scrollY Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    private final List<OnScrollChangeListenerCompat> mOnScrollChangeListeners = new ArrayList<>();

    public TabLayoutWithScrollListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addOnScrollChangeListenerCompat(OnScrollChangeListenerCompat l) {
        if (!mOnScrollChangeListeners.contains(l)) {
            mOnScrollChangeListeners.add(l);
        }
    }

    public void removeOnScrollChangeListenerCompat(OnScrollChangeListenerCompat l) {
        mOnScrollChangeListeners.remove(l);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        for (OnScrollChangeListenerCompat listener : mOnScrollChangeListeners) {
            listener.onScrollChange(this, l, t, oldl, oldt);
        }
    }
}

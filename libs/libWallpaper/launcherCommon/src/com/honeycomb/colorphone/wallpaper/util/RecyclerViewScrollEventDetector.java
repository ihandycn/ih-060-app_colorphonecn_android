package com.honeycomb.colorphone.wallpaper.util;

import android.support.v7.widget.RecyclerView;

/**
 * A {@link RecyclerView.OnScrollListener} for detecting scroll up / down events. This is used to log events.
 */
public class RecyclerViewScrollEventDetector extends RecyclerView.OnScrollListener {

    public interface ScrolledListener {
        void onScrolled(int dx, int dy);
    }

    private final Runnable mUpAction;
    private final Runnable mDownAction;

    private boolean mUpDetected;
    private boolean mDownDetected;

    private ScrolledListener listener;

    public RecyclerViewScrollEventDetector(Runnable upAction, Runnable downAction) {
        this(upAction, downAction, null);
    }

    public RecyclerViewScrollEventDetector(Runnable upAction, Runnable downAction, ScrolledListener listener) {
        mUpAction = upAction;
        mDownAction = downAction;
        this.listener = listener;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            // Reset
            mUpDetected = false;
            mDownDetected = false;
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy != 0) {
            if (dy > 0 && !mUpDetected) {
                mUpDetected = true;
                if (mUpAction != null) {
                    mUpAction.run();
                }
            }
            if (dy < 0 && !mDownDetected) {
                mDownDetected = true;
                if (mDownAction != null) {
                    mDownAction.run();
                }
            }
        }

        if (listener != null) {
            listener.onScrolled(dx, dy);
        }
    }
}

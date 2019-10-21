package com.honeycomb.colorphone.wallpaper.view.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


import java.util.ArrayList;

/**
 * Imported from library https://github.com/lynnchurch/PullToRefresh with code formatting and implementation tweaks.
 * <p>
 * License: https://github.com/lynnchurch/PullToRefresh/blob/master/LICENSE
 */
public class WrapRecyclerView extends RecyclerView {

    private ArrayList<View> mHeaderViews = new ArrayList<>();

    private ArrayList<View> mFootViews = new ArrayList<>();

    public WrapRecyclerView(Context context) {
        super(context);
    }

    public WrapRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addHeaderView(View view) {
        mHeaderViews.clear();
        mHeaderViews.add(view);
        Adapter adapter = getAdapter();
        if (adapter != null) {
            if (!(adapter instanceof RecyclerWrapAdapter)) {
                adapter = new RecyclerWrapAdapter(mHeaderViews, mFootViews, adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void addFootView(View view) {
        mFootViews.clear();
        mFootViews.add(view);
        Adapter adapter = getAdapter();
        if (adapter != null) {
            if (!(adapter instanceof RecyclerWrapAdapter)) {
                adapter = new RecyclerWrapAdapter(mHeaderViews, mFootViews, adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (!mHeaderViews.isEmpty() || !mFootViews.isEmpty()) {
            adapter = new RecyclerWrapAdapter(mHeaderViews, mFootViews, adapter);

        }
        super.setAdapter(adapter);
    }

    /**
     * 获取页眉的高度
     *
     * @return
     */
    public int getHeaderHeight() {
        int height = 0;
        if (!mHeaderViews.isEmpty()) {
            for (int i = 0; i < mHeaderViews.size(); i++) {
                mHeaderViews.get(i).measure(0, 0);
                height += mHeaderViews.get(i).getHeight();
            }
        }
        return height;
    }
}

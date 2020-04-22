package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.viewholder.LoadMoreViewHolder;

public class LoadMoreItem implements INewsListItem<RecyclerView.ViewHolder> {

    public LoadMoreItem(boolean hasMore) {
    }

    @Override
    public int getLayoutRes() {
        return R.layout.load_more_item;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(Context context) {
        return new LoadMoreViewHolder(LayoutInflater.from(context).inflate(getLayoutRes(), null));
    }

    @Override
    public void bindViewHolder(Context context, RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void release() {

    }

    @Override
    public void detachedFromWindow() {

    }

    @Override
    public void attachedToWindow() {

    }
}

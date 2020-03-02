package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.viewholder.LoadMoreViewHolder;
import com.colorphone.smartlocker.viewholder.TouTiaoLoadMoreViewHolder;

public class LoadMoreItem implements IDailyNewsListItem<RecyclerView.ViewHolder> {
    private boolean hasMore;

    public LoadMoreItem(boolean hasMore) {
        this.hasMore = hasMore;
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
        if (!(holder instanceof TouTiaoLoadMoreViewHolder)) {
            return;
        }

        TouTiaoLoadMoreViewHolder viewHolder = (TouTiaoLoadMoreViewHolder) holder;
        if (hasMore) {
            viewHolder.loadTextView.setText(context.getString(R.string.loading_more));
        } else {
            viewHolder.loadTextView.setText(context.getString(R.string.no_more_data));
        }
    }

    @Override
    public void release() {

    }

    @Override
    public void logViewedEvent() {

    }

    @Override
    public boolean hasViewed() {
        return true;
    }
}

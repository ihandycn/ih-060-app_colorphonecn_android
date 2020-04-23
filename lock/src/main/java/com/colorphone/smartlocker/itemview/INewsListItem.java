package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;

public interface INewsListItem<VH extends RecyclerView.ViewHolder> {
    @LayoutRes
    int getLayoutRes();

    VH createViewHolder(Context context);

    void bindViewHolder(Context context, RecyclerView.ViewHolder holder, int position);

    void release();

    void detachedFromWindow();

    void attachedToWindow();
}

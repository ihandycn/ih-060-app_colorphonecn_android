package com.colorphone.smartlocker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.smartlocker.itemview.INewsListItem;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private class ExceptionViewHolder extends RecyclerView.ViewHolder {

        ExceptionViewHolder() {
            super(new View(context));
        }
    }

    private Context context;
    private SparseArray<INewsListItem<? extends RecyclerView.ViewHolder>> typeInstances = new SparseArray<>();
    private List<INewsListItem<? extends RecyclerView.ViewHolder>> items = new ArrayList<>();

    public NewsAdapter(Context context, List<INewsListItem<RecyclerView.ViewHolder>> items) {
        this.context = context;
        this.items.addAll(items);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        INewsListItem<? extends RecyclerView.ViewHolder> item = typeInstances.get(viewType);
        if (item == null) {
            return new ExceptionViewHolder();
        }

        return item.createViewHolder(context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        INewsListItem<? extends RecyclerView.ViewHolder> item = items.get(position);
        if (item == null) {
            return;
        }

        item.bindViewHolder(context, holder, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof INewsListItem) {
            ((INewsListItem) holder).detachedFromWindow();
        }
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemViewType(int position) {
        INewsListItem<? extends RecyclerView.ViewHolder> item = items.get(position);
        if (item == null) {
            return 0;
        }

        if (typeInstances.indexOfKey(item.getLayoutRes()) < 0) {
            typeInstances.put(item.getLayoutRes(), item);
        }

        return item.getLayoutRes();
    }

    public void release() {
        for (INewsListItem<? extends RecyclerView.ViewHolder> item : items) {
            item.release();
        }

        items.clear();
    }

    public void addItems(int position, List<INewsListItem<? extends RecyclerView.ViewHolder>> items) {
        if (position < 0) {
            position = 0;
        }
        this.items.addAll(position, items);
        notifyItemRangeInserted(position, items.size());
    }

    public void addItem(int position, INewsListItem<RecyclerView.ViewHolder> item) {
        if (position < 0) {
            position = 0;
        }
        this.items.add(position, item);
        notifyItemInserted(position);
    }

    public INewsListItem<? extends RecyclerView.ViewHolder> getItem(int position) {
        return items.get(position);
    }

    public void removeAllItem() {
        release();

        items.clear();
        notifyDataSetChanged();
    }

    public List<INewsListItem<? extends RecyclerView.ViewHolder>> getData() {
        return items;
    }

}

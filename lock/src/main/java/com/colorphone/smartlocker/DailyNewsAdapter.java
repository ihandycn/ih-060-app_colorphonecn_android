package com.colorphone.smartlocker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.smartlocker.itemview.IDailyNewsListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hao.li on 2019/4/25.
 */

public class DailyNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private class ExceptionViewHolder extends RecyclerView.ViewHolder {

        ExceptionViewHolder() {
            super(new View(context));
        }
    }

    private Context context;
    private SparseArray<IDailyNewsListItem<? extends RecyclerView.ViewHolder>> typeInstances = new SparseArray<>();
    private List<IDailyNewsListItem<? extends RecyclerView.ViewHolder>> items = new ArrayList<>();

    public DailyNewsAdapter(Context context, List<IDailyNewsListItem<RecyclerView.ViewHolder>> items) {
        this.context = context;
        this.items.addAll(items);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        IDailyNewsListItem<? extends RecyclerView.ViewHolder> item = typeInstances.get(viewType);
        if (item == null) {
            return new ExceptionViewHolder();
        }

        return item.createViewHolder(context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        IDailyNewsListItem<? extends RecyclerView.ViewHolder> item = items.get(position);
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
    public int getItemViewType(int position) {
        IDailyNewsListItem<? extends RecyclerView.ViewHolder> item = items.get(position);
        if (item == null) {
            return 0;
        }

        if (typeInstances.indexOfKey(item.getLayoutRes()) < 0) {
            typeInstances.put(item.getLayoutRes(), item);
        }

        return item.getLayoutRes();
    }

    public void release() {
        for (IDailyNewsListItem<? extends RecyclerView.ViewHolder> item : items) {
            item.release();
        }

        items.clear();
    }

    public void addItems(int position, List<IDailyNewsListItem<? extends RecyclerView.ViewHolder>> items) {
        if (position < 0) {
            position = 0;
        }
        this.items.addAll(position, items);
        notifyItemRangeInserted(position, items.size());
    }

    public void addItem(int position, IDailyNewsListItem<RecyclerView.ViewHolder> item) {
        if (position < 0) {
            position = 0;
        }
        this.items.add(position, item);
        notifyItemInserted(position);
    }

    public IDailyNewsListItem<? extends RecyclerView.ViewHolder> getItem(int position) {
        return items.get(position);
    }

    public void removeAllItem() {
        release();

        items.clear();
        notifyDataSetChanged();
    }

    public List<IDailyNewsListItem<? extends RecyclerView.ViewHolder>> getData() {
        return items;
    }

}

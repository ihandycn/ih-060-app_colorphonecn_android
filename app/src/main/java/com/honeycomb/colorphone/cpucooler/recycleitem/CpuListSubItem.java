package com.honeycomb.colorphone.cpucooler.recycleitem;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.ThreeStatesCheckBox;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;

public class CpuListSubItem extends AbstractFlexibleItem<CpuListSubItem.SubViewHolder>
        implements ISectionable<CpuListSubItem.SubViewHolder, IHeader>, IFilterable{

    static class SubViewHolder extends FlexibleViewHolder {

        ImageView icon;
        TextView appName;
        ThreeStatesCheckBox checkBox;

        SubViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            icon = view.findViewById(R.id.app_icon);
            appName = view.findViewById(R.id.app_name);
            checkBox = view.findViewById(R.id.sub_item_checkbox);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }
    }

    private CpuListHeadItem header;
    public String packageName;
    public CharSequence appName;
    public Drawable icon;
    private boolean isChecked = true;

    public CpuListSubItem(String packageName, CharSequence appName, Drawable icon) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
    }

    public void setSubItemCheckStatus(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public void refreshHeadCheckStatus() {
        header.refreshHeadCheckStatus();
    }

    public boolean getCheckStatus() {
        return isChecked;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.cpu_cooler_item_cpu_list_sub;
    }

    @Override
    public SubViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new SubViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, SubViewHolder holder, int position, List payloads) {
        holder.icon.setBackgroundDrawable(icon);
        holder.appName.setText(appName);
        holder.checkBox.setTag(this);
        holder.checkBox.setCheckedState(isChecked ? ThreeStatesCheckBox.ALL_CHECKED : ThreeStatesCheckBox.ALL_UNCHECKED);
        holder.checkBox.setOnCheckBoxClickListener(new ThreeStatesCheckBox.OnCheckBoxClickListener() {
            @Override
            public void onClick(ThreeStatesCheckBox checkBox, @ThreeStatesCheckBox.CheckedState int checkState) {
                CpuListSubItem cpuListSubItem = (CpuListSubItem) checkBox.getTag();
                cpuListSubItem.isChecked = checkState == ThreeStatesCheckBox.ALL_CHECKED;
                cpuListSubItem.refreshHeadCheckStatus();
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean equals(Object object) {
        return true;
    }


    @Override
    public IHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(IHeader header) {
        this.header = (CpuListHeadItem)header;
    }

    @Override
    public boolean filter(String constraint) {
        return false;
    }
}


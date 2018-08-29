package com.honeycomb.colorphone.cpucooler.recycleitem;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.ThreeStatesCheckBox;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.ExpandableViewHolder;

public class CpuListHeadItem extends AbstractFlexibleItem<CpuListHeadItem.HeadViewHolder>
        implements IExpandable<CpuListHeadItem.HeadViewHolder, CpuListSubItem>,
        IHeader<CpuListHeadItem.HeadViewHolder> {


    private List<CpuListSubItem> mSubItemList= new ArrayList<>();;
    private OnSelectListener mOnSelectListener;

    private int mCheckedState = ThreeStatesCheckBox.ALL_CHECKED;
    private boolean mIsExpanded = true;

    public interface OnSelectListener {
        void onSelectionChange(boolean isAllUnselected);
    }

    static class HeadViewHolder extends ExpandableViewHolder {
        View gapView;
        TextView headTitleTextView;
        ThreeStatesCheckBox checkBox;

        HeadViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            gapView = view.findViewById(R.id.gap);
            headTitleTextView = (TextView) view.findViewById(R.id.head_title);
            checkBox = (ThreeStatesCheckBox) view.findViewById(R.id.head_checkbox);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }
    }

    public CpuListHeadItem(OnSelectListener listener) {
        setSelectable(false);
        setEnabled(false);
        this.mOnSelectListener = listener;
    }

    @Override
    public HeadViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new HeadViewHolder(inflater.inflate(R.layout.cpu_cooler_item_cpu_list_head, parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, HeadViewHolder holder, int position, List payloads) {
        holder.checkBox.setTag(this);
        holder.checkBox.setCheckedState(mCheckedState);
        holder.checkBox.setOnCheckBoxClickListener((checkBox, checkedState1) -> {
            CpuListHeadItem cpuListHeadItem = (CpuListHeadItem) checkBox.getTag();
            cpuListHeadItem.mCheckedState = checkedState1;
            cpuListHeadItem.setSubItemCheckStatus(checkedState1 == ThreeStatesCheckBox.ALL_CHECKED);
            mOnSelectListener.onSelectionChange(checkedState1 == ThreeStatesCheckBox.ALL_UNCHECKED);

            adapter.notifyDataSetChanged();
        });

        holder.gapView.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
    }

    public void setSubItemCheckStatus(boolean isChecked) {
        for (CpuListSubItem item : mSubItemList) {
            item.setSubItemCheckStatus(isChecked);
        }
    }

    public void refreshHeadCheckStatus() {
        mCheckedState = ThreeStatesCheckBox.ALL_CHECKED;
        boolean hasAtLeastOneSelected = false;
        for (CpuListSubItem item : mSubItemList) {
            if (item.getCheckStatus()) {
                hasAtLeastOneSelected = true;
            } else {
                mCheckedState = ThreeStatesCheckBox.PART_CHECKED;
            }
        }
        if (!hasAtLeastOneSelected) {
            mCheckedState = ThreeStatesCheckBox.ALL_UNCHECKED;
        }
        mOnSelectListener.onSelectionChange(!hasAtLeastOneSelected);
    }

    @Override
    public int getLayoutRes() {
        return R.layout.cpu_cooler_item_cpu_list_head;
    }

    @Override
    public boolean equals(Object object) {
        return (this == object || object instanceof CpuListHeadItem);
    }

    public void addSubItem(CpuListSubItem item) {
        mSubItemList.add(item);
    }

    @Override
    public boolean isExpanded() {
        return mIsExpanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        mIsExpanded = expanded;
    }

    @Override
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<CpuListSubItem> getSubItems() {
        return mSubItemList;
    }

}


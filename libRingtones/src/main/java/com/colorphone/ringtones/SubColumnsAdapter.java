package com.colorphone.ringtones;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colorphone.ringtones.module.Column;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sundxing
 */
public class SubColumnsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int sLastPositionFirstLine = 3;

    final private List<Column> mColumns = new ArrayList<>();
    private Drawable mSelectedDrawable;
    private Drawable mNormalDrawable;

    private OnColumnSelectedListener mOnColumnSelectedListener;
    private View mExpandButton;
    private boolean isExpand = false;

    public SubColumnsAdapter() {
        mSelectedDrawable = BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(20), true);
        mNormalDrawable = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#ff2c2b36"), Dimensions.pxFromDp(20), true);
        mColumns.addAll(RingtoneManager.getInstance().getSubColumns());
        for (int i = 0; i < mColumns.size(); i++) {
            if (i == 0) {
                mColumns.get(i).setSelected(true);
            } else {
                mColumns.get(i).setSelected(false);
            }
        }
    }

    public List<Column> getColumns() {
        return mColumns;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_ringtone_classification, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) itemView.getTag();

                onSelectColumn(position);
            }
        });
        return new InnerViewHolder(itemView);
    }

    private void swapData(int position, int positionInTurn) {
        Column data = mColumns.remove(position);
        mColumns.add(positionInTurn, data);
    }

    private void onSelectColumn(int position) {
        Column column = mColumns.get(position);
        if (column.isSelected()) {
            // No change
            return;
        }

        // Update selected status
        for (Column col : mColumns) {
            col.setSelected(false);
        }
        column.setSelected(true);


        // Data not in first line
        if (position > sLastPositionFirstLine) {
            swapData(position, sLastPositionFirstLine);
        }

        if (isExpand) {
            toggleList();
        }
        notifyDataSetChanged();

        if (mOnColumnSelectedListener != null) {
            mOnColumnSelectedListener.onColumnSelect(column);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Column column = mColumns.get(position);
        InnerViewHolder innerViewHolder = (InnerViewHolder) holder;
        innerViewHolder.mTitleView.setText(column.getName());
        innerViewHolder.itemView.setTag(position);
        if (column.isSelected()) {
            innerViewHolder.itemView.setBackground(mSelectedDrawable);
            innerViewHolder.mTitleView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ringtone_text_black_main));
        } else {
            innerViewHolder.itemView.setBackground(mNormalDrawable);
            innerViewHolder.mTitleView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.ringtone_button_color));
        }
    }

    @Override
    public int getItemCount() {
        return isExpand ? mColumns.size() : (Math.min(sLastPositionFirstLine + 1, mColumns.size()));
    }

    public void setColumns(List<Column> columns) {
        mColumns.clear();
        mColumns.addAll(columns);
        notifyDataSetChanged();
    }

    public void setOnColumnSelectedListener(OnColumnSelectedListener onColumnSelectedListener) {
        mOnColumnSelectedListener = onColumnSelectedListener;
    }

    public void setExpandButton(View expandBtn) {
        mExpandButton = expandBtn;
        mExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleList();
            }
        });
        updateIndicator();
    }

    private void toggleList() {
        isExpand = !isExpand;
        notifyDataSetChanged();
        updateIndicator();
    }

    private void updateIndicator() {
        if (mExpandButton != null) {
            mExpandButton.animate().rotation(isExpand ? 180 : 0).setDuration(300).start();
        }
    }

    private static class InnerViewHolder extends RecyclerView.ViewHolder {

        TextView mTitleView;

        public InnerViewHolder(View itemView) {
            super(itemView);
            mTitleView = itemView.findViewById(R.id.title_view);
        }
    }

    public interface OnColumnSelectedListener {
        void onColumnSelect(Column column);
    }
}

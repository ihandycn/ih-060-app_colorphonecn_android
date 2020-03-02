package com.colorphone.smartlocker.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zhouzhenliang on 18/2/11.
 */

public abstract class DonePageListViewHolder extends RecyclerView.ViewHolder {
    public DonePageListViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void clearViewHolder();
}

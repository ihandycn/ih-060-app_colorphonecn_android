package com.honeycomb.colorphone.customize.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.R;

public class FootViewHolder extends RecyclerView.ViewHolder {

    TextView tvFoot;

    public FootViewHolder(View itemView) {
        super(itemView);
        tvFoot = itemView.findViewById(R.id.tv_foot);
    }
}

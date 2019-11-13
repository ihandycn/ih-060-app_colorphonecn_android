package com.honeycomb.colorphone.wallpaper.customize.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;

public class FootViewHolder extends RecyclerView.ViewHolder {

    TextView tvFoot;

    public FootViewHolder(View itemView) {
        super(itemView);
        tvFoot = ViewUtils.findViewById(itemView, R.id.tv_foot);
    }
}

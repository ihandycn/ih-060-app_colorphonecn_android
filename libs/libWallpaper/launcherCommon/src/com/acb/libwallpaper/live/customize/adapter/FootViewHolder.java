package com.acb.libwallpaper.live.customize.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.acb.libwallpaper.live.util.ViewUtils;
import com.acb.libwallpaper.R;

public class FootViewHolder extends RecyclerView.ViewHolder {

    TextView tvFoot;

    public FootViewHolder(View itemView) {
        super(itemView);
        tvFoot = ViewUtils.findViewById(itemView, R.id.tv_foot);
    }
}

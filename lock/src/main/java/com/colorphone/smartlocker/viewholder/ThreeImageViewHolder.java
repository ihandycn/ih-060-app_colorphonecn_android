package com.colorphone.smartlocker.viewholder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.DisplayUtils;

public class ThreeImageViewHolder extends RecyclerView.ViewHolder {

    public final TextView title;
    public final TextView source;
    public final TextView stick;
    public final TextView comment;
    public final TextView time;
    public final ImageView imageView1;
    public final ImageView imageView2;
    public final ImageView imageView3;

    public final View rootView;

    public ThreeImageViewHolder(View itemView) {
        super(itemView);

        rootView = itemView;
        rootView.setPadding(DisplayUtils.dpToPx(3), 0, DisplayUtils.dpToPx(3), 0);

        title = itemView.findViewById(R.id.title);
        source = itemView.findViewById(R.id.source);
        stick = itemView.findViewById(R.id.stick);
        comment = itemView.findViewById(R.id.comment);
        time = itemView.findViewById(R.id.time);
        imageView1 = itemView.findViewById(R.id.image_1);
        imageView2 = itemView.findViewById(R.id.image_2);
        imageView3 = itemView.findViewById(R.id.image_3);
        itemView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
        itemView.findViewById(R.id.divider_1).setVisibility(View.GONE);
        itemView.setBackgroundColor(Color.WHITE);
    }

}

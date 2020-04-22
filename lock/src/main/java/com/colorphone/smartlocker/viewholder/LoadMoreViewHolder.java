package com.colorphone.smartlocker.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.colorphone.lock.R;

public class LoadMoreViewHolder extends RecyclerView.ViewHolder {

    public final TextView loadTextView;

    public LoadMoreViewHolder(View itemView) {
        super(itemView);

        loadTextView = itemView.findViewById(R.id.load_text_view);
    }
}

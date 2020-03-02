package com.colorphone.smartlocker.viewholder;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.R;


public class TouTiaoLoadMoreViewHolder extends DonePageListViewHolder {

    public final TextView loadTextView;

    @LayoutRes
    private static int layoutRes = -1;

    public TouTiaoLoadMoreViewHolder(View itemView) {
        super(itemView);

        if (BuildConfig.DEBUG && layoutRes == -1) {
            throw new RuntimeException("TouTiaoLoadMoreViewHolder, itemView error");
        }

        loadTextView = itemView.findViewById(R.id.load_text_view);
    }

    @Override
    public void clearViewHolder() {

    }

    @LayoutRes
    public static int getLayoutRes() {
        if (layoutRes != -1) {
            return layoutRes;
        }

        layoutRes = R.layout.load_more_item;
        return layoutRes;
    }

    public static View createItemView(Context context) {
        return LayoutInflater.from(context).inflate(getLayoutRes(), null);
    }

}

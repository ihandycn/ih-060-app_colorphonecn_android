package com.honeycomb.colorphone.cmgame;

import android.content.Context;
import android.widget.ImageView;

import com.cmcm.cmgame.IImageLoader;
import com.honeycomb.colorphone.view.GlideApp;


public class CmGameImageLoader implements IImageLoader {
    @Override
    public void loadImage(Context context, String imageUrl, ImageView imageView, int defRsid) {
        GlideApp.with(context)
                .load(imageUrl)
                .placeholder(defRsid)
                .into(imageView);
    }
}
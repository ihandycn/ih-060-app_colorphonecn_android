package com.honeycomb.colorphone.view;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by sundxing on 17/9/26.
 */

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    public static RequestOptions sRequestOptions = new RequestOptions();
    static {
        sRequestOptions.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .dontTransform()
                .format(DecodeFormat.DEFAULT);
    }
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setDefaultRequestOptions(sRequestOptions);
    }
}

package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.honeycomb.colorphone.module.ContactPhotoDecoder;

/**
 * Created by sundxing on 17/9/26.
 */

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    public static RequestOptions sRequestOptions = new RequestOptions();
    static {
        sRequestOptions.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontAnimate()
                .dontTransform()
                .format(DecodeFormat.PREFER_RGB_565);
    }
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setDefaultRequestOptions(sRequestOptions);
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.prepend(String.class, Bitmap.class, new ContactPhotoDecoder());
    }
}

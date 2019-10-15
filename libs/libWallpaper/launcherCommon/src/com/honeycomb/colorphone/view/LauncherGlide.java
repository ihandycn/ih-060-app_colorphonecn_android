package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.glide.AdMarkedBitmap;
import com.acb.libwallpaper.live.glide.AdMarkedBitmapTranscoder;
import com.acb.libwallpaper.live.glide.EmojiDrawableTranscoder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.ihs.app.framework.HSApplication;


/**
 * Global configurations for Glide.
 */

@GlideModule
@SuppressWarnings("unused")

public class LauncherGlide extends AppGlideModule {

    private static final String GLIDE_CACHE_DIR = "glide";
    private static final int DISK_CACHE_SIZE = 256 * 1024 * 1024; // 256 MB

    private static final int GLIDE_MEMORY_CACHE_SIZE_MAIN =
            (int) Math.max(Runtime.getRuntime().maxMemory() / 8, 20 * 1024 * 1024);

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Assign view tag ID: allow use of View#setTag() when the view is used as a glide target view
        ViewTarget.setTagId(R.string.glide_tag_id);

        // Configure disk & memory caches
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context,
                GLIDE_CACHE_DIR, DISK_CACHE_SIZE));

        String packageName = context.getPackageName();
        String processName = HSApplication.getProcessName();
        if (TextUtils.equals(processName, packageName)) {
            builder.setMemoryCache(new LruResourceCache(GLIDE_MEMORY_CACHE_SIZE_MAIN));
        }
        // Configure default Bitmap config
        builder.setDefaultRequestOptions(RequestOptions.formatOf(DecodeFormat.PREFER_ARGB_8888));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.register(GifDrawable.class,
                pl.droidsonroids.gif.GifDrawable.class, new EmojiDrawableTranscoder());
        registry.register(Bitmap.class, AdMarkedBitmap.class, new AdMarkedBitmapTranscoder());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

}

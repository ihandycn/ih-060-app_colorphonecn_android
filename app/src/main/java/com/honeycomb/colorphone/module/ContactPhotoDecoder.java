package com.honeycomb.colorphone.module;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.honeycomb.colorphone.contact.ContactUtils;
import com.ihs.app.framework.HSApplication;

import java.io.IOException;

public class ContactPhotoDecoder implements ResourceDecoder<String, Bitmap> {

    @Override
    public boolean handles(String source, Options options) throws IOException {
        if (!TextUtils.isEmpty(source) && source.contains("com.android.contacts")) {
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Resource<Bitmap> decode(String source, int width, int height, Options options) throws IOException {
        Bitmap bitmap = ContactUtils.loadContactPhotoThumbnail(HSApplication.getContext(), source);
        return BitmapResource.obtain(bitmap, Glide.get(HSApplication.getContext()).getBitmapPool());
    }
}

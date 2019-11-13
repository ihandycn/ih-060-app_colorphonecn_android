package com.honeycomb.colorphone.wallpaper.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class AdMarkedBitmapTranscoder implements ResourceTranscoder<Bitmap, AdMarkedBitmap> {

    @Override
    public Resource<AdMarkedBitmap> transcode(Resource<Bitmap> toTranscode) {
        if (toTranscode == null) {
            return null;
        }
        AdMarkedBitmap result = new AdMarkedBitmap(toTranscode.get());
        return new MarkedBitmapResource(result, toTranscode.getSize());
    }

    private static class MarkedBitmapResource implements Resource<AdMarkedBitmap> {
        private AdMarkedBitmap mSource;
        private int mSize;

        public MarkedBitmapResource(AdMarkedBitmap source, int size) {
            mSource = source;
            mSize = size;
        }

        @Override
        public Class<AdMarkedBitmap> getResourceClass() {
            return AdMarkedBitmap.class;
        }

        @Override
        public AdMarkedBitmap get() {
            return mSource;
        }

        @Override
        public int getSize() {
            return mSize;
        }

        @Override
        public void recycle() {
            if (mSource != null) {
                mSource.recycle();
            }
        }
    }
}

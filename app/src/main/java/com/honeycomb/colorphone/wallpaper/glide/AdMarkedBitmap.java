package com.honeycomb.colorphone.wallpaper.glide;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

public class AdMarkedBitmap {

    private static final String TAG = AdMarkedBitmap.class.getSimpleName();

    private final Bitmap mResource;
    private final Drawable mAdDrawable;

    public AdMarkedBitmap(Bitmap resource) {
        this.mResource = resource;
        // todo: null context
        mAdDrawable = ContextCompat.getDrawable(null, R.drawable.theme_promotions_ad_mark);
        mark();
    }

    private void mark() {
        if (mResource == null || mAdDrawable == null) {
            return;
        }
        HSLog.d(TAG, "bitmap:" + mResource.hashCode() + " start mark...");
        Canvas c = new Canvas(mResource);
        mAdDrawable.setBounds(0, 0,
                Math.round(mResource.getWidth() * 0.4f), Math.round(mResource.getHeight() * 0.4f));
        mAdDrawable.draw(c);
    }

    public Bitmap getBitmap() {
        return mResource;
    }

    public void recycle() {
        if (mResource != null) {
            mResource.recycle();
        }
    }
}

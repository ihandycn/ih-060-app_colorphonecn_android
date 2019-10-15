package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.acb.libwallpaper.R;
import com.superapps.util.Threads;

import java.io.InputStream;

/**
 * An {@link ImageView} that loads its src image off UI thread when set through {@link ImageView#setImageResource(int)}
 * or {@code src} attribute.
 */
public class AsyncImageView extends ImageView {

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
        @DrawableRes int resId = a.getResourceId(R.styleable.AsyncImageView_asyncSrc, 0);
        if (resId > 0) {
            setImageResource(resId);
        }
        a.recycle();
    }

    @Override
    public void setImageResource(final int resId) {
        setImageResource(this, resId);
    }

    public static void setImageResource(final ImageView view, final int resId) {
        if (resId == 0) {
            view.setImageDrawable(null);
            return;
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                InputStream is = view.getContext().getResources().openRawResource(+resId);
                Bitmap imageBitmap = null;
                try {
                    imageBitmap = BitmapFactory.decodeStream(is);
                } catch (OutOfMemoryError e) {
                    view.setImageDrawable(null);
                }
                final Bitmap imageBitmapFinal = imageBitmap;

                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (imageBitmapFinal != null) {
                            view.setImageBitmap(imageBitmapFinal);
                        } else {
                            view.setImageDrawable(null);
                        }
                    }
                });
            }
        });
    }
}

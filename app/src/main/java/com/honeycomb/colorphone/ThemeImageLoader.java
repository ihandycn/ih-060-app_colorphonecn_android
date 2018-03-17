package com.honeycomb.colorphone;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.honeycomb.colorphone.view.GlideRequests;
import com.ihs.commons.utils.HSLog;

import hugo.weaving.DebugLog;


/**
 * Created by sundxing on 17/9/20.
 */

public class ThemeImageLoader extends ScreenFlashManager.DefaultImageLoader {

    public static final int IMAGE_ORIG_WIDTH = 1080;
    public static final int IMAGE_ORIG_HEIGHT = 1920;

    @DebugLog
    @Override
    public void load(Type type, String s, int holderImage, ImageView imageView) {
        if (Type.RES_REMOTE_URL.equals(type.getResType())) {
            GlideRequests requests;
            if (imageView.getContext() instanceof Activity) {
                requests = GlideApp.with((Activity)imageView.getContext());
            } else {
                requests = GlideApp.with(imageView);
            }

            GlideRequest<Bitmap> bR = requests.asBitmap().load(s)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .dontAnimate();

            if (holderImage != 0) {
                bR.placeholder(holderImage)
                        .error(holderImage);
            }
            bR.into(imageView);

        } else if (Type.RES_LOCAL_ID.equals(type.getResType())) {
            GlideApp.with(imageView).load(Integer.parseInt(s)).into(imageView);
        } else  {
            super.load(type, s, holderImage, imageView);
        }
    }

    @Override
    public void load(Type type, String s) {
//        Glide.with(HSApplication.getContext()).load(s).preload();
    }



}

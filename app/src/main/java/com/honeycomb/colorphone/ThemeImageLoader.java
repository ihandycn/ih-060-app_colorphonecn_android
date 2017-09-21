package com.honeycomb.colorphone;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.acb.call.customize.AcbCallManager;
import com.acb.call.themes.Type;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import hugo.weaving.DebugLog;


/**
 * Created by sundxing on 17/9/20.
 */

public class ThemeImageLoader extends AcbCallManager.DefaultImageLoader {
    RequestOptions previewOption = new RequestOptions();
    RequestOptions iconwOption = new RequestOptions();
    ThemeImageLoader() {
        previewOption.apply(sRequestOptions);
        previewOption.override(350, 622);
        iconwOption.apply(sRequestOptions);
        iconwOption.override(278, 278);
    }

    @DebugLog
    @Override
    public void load(Type type, String s, int holderImage, ImageView imageView) {
        if (Type.RES_REMOTE_URL.equals(type.getResType())) {
            RequestOptions requestOptions = sRequestOptions;
            if (TextUtils.equals(s, type.getPreviewImage())) {
                requestOptions = previewOption;
            } else if (TextUtils.equals(s, type.getAcceptIcon()) || TextUtils.equals(s, type.getRejectIcon())) {
                requestOptions = iconwOption;
            }
            if (holderImage != 0) {
                requestOptions.placeholder(holderImage);
            }

            RequestManager requestManager;
            if (imageView.getContext() instanceof Activity) {
                requestManager = Glide.with((Activity)imageView.getContext());
            } else {
                requestManager = Glide.with(imageView);
            }
            requestManager.applyDefaultRequestOptions(requestOptions).load(s).into(imageView);
        } else {
            super.load(type, s, holderImage, imageView);
        }
    }

    @Override
    public void load(Type type, String s) {
//        Glide.with(HSApplication.getContext()).load(s).preload();
    }


    public static RequestOptions sRequestOptions = new RequestOptions();
    static {
        sRequestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .dontTransform()
                .format(DecodeFormat.DEFAULT);
    }
}

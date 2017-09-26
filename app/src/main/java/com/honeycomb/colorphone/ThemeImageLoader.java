package com.honeycomb.colorphone;

import android.app.Activity;
import android.widget.ImageView;

import com.acb.call.customize.AcbCallManager;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequests;

import hugo.weaving.DebugLog;


/**
 * Created by sundxing on 17/9/20.
 */

public class ThemeImageLoader extends AcbCallManager.DefaultImageLoader {

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

//            if (TextUtils.equals(s, type.getPreviewImage())) {
//                requests
//.
//            } else if (TextUtils.equals(s, type.getAcceptIcon()) || TextUtils.equals(s, type.getRejectIcon())) {
//                requestOptions = iconwOption;
//            }
            requests.load(s)
                    .placeholder(holderImage)
                    .error(holderImage)
                    .fitCenter()
                    .into(imageView);
        } else {
            super.load(type, s, holderImage, imageView);
        }
    }

    @Override
    public void load(Type type, String s) {
//        Glide.with(HSApplication.getContext()).load(s).preload();
    }



}

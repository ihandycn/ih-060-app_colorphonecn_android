package com.colorphone.ringtones;

import android.content.Context;
import android.widget.ImageView;

public interface RingtoneImageLoader {
    void loadImage(Context context, String imageUrl, ImageView imageView, int defaultResId);
}

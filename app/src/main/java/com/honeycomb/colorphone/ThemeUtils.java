package com.honeycomb.colorphone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.themes.Type;
import com.ihs.commons.utils.HSLog;

public class ThemeUtils {
    private static int[] sThemeItemTxts;
    private static int[] sThemeItemImgs;

    private static final String GIF1_URL = "http://cdn.appcloudbox.net/sunspotmix/gifs/Stars.gif";
    private static final String GIF2_URL = "http://cdn.appcloudbox.net/sunspotmix/gifs/Sun.gif";
    private static final String GIF3_URL = "http://cdn.appcloudbox.net/sunspotmix/gifs/Neon.gif";

    public static int getThemeNameRes(Context context, int index) {
        if (sThemeItemTxts == null) {
            TypedArray iconResArray = context.getResources().obtainTypedArray(R.array.theme_item_txts);
            sThemeItemTxts = new int[iconResArray.length()];
            for (int j = 0; j < iconResArray.length(); ++j) {
                sThemeItemTxts[j] = iconResArray.getResourceId(j, 0);
            }
            iconResArray.recycle();
        }
        return sThemeItemTxts[index];
    }

    public static int getThemeIconRes(Context context, int index) {
        if (sThemeItemImgs == null) {
            TypedArray iconResArray = context.getResources().obtainTypedArray(R.array.theme_item_imgs);
            sThemeItemImgs = new int[iconResArray.length()];
            for (int j = 0; j < iconResArray.length(); ++j) {
                sThemeItemImgs[j] = iconResArray.getResourceId(j, 0);
            }
            iconResArray.recycle();
        }
        return sThemeItemTxts[index];
    }


    public static String getGifUrl(Type type) {
        switch (type) {
            case STARS:
                return GIF1_URL;
            case SUN:
                return GIF2_URL;
            case NEON:
                return GIF3_URL;
        }
        HSLog.e("GetGifUrl", "error gif type " + type);
        return "";
    }

    public static void updateStyle(View container,  Theme theme) {
        TextView name = (TextView) container.findViewById(R.id.caller_name);
        TextView number = (TextView) container.findViewById(R.id.caller_number);
        name.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_REGULAR));
        number.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));

        name.setShadowLayer(Utils.pxFromDp(1), 0, Utils.pxFromDp(2), Color.BLACK);
        number.setShadowLayer(Utils.pxFromDp(1), 0, Utils.pxFromDp(1), Color.BLACK);

        ImageView avatar = (ImageView) container.findViewById(R.id.caller_avatar);
        avatar.setImageDrawable(ContextCompat.getDrawable(container.getContext(), theme.getAvatar()));
    }

}

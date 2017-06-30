package com.honeycomb.colorphone;

import android.content.Context;
import android.content.res.TypedArray;

public class ThemeUtils {
    private static int[] sThemeItemTxts;
    private static int[] sThemeItemImgs;

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
}

package com.honeycomb.colorphone;

import android.graphics.Typeface;
import android.util.SparseArray;

import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for text fonts.
 */
public class FontUtils {

    public enum Font {
        ROBOTO_LIGHT(0,  R.string.roboto_light),
        ROBOTO_REGULAR(1, R.string.roboto_regular),
        ROBOTO_MEDIUM(2, R.string.roboto_medium),
        ROBOTO_THIN(3, R.string.roboto_thin),
        ROBOTO_CONDENSED(4, R.string.roboto_condensed),
        DS_DIGIB(5, R.string.ds_digib),
        AKROBAT_LIGHT(6, R.string.akrobat_light),
        PROXIMA_NOVA_REGULAR(7, R.string.proxima_nova_regular),
        PROXIMA_NOVA_LIGHT(8, R.string.proxima_nova_light),
        PROXIMA_NOVA_THIN(9, R.string.proxima_nova_thin),
        PROXIMA_NOVA_SEMIBOLD(11, R.string.proxima_nova_semibold),
        PROXIMA_NOVA_BOLD(13, R.string.proxima_nova_bold),
        PROXIMA_NOVA_REGULAR_CONDENSED(12, R.string.proxima_nova_regular_condensed);

        private int mValue;
        private int mResId;

        Font(int value, int resId) {
            mValue = value;
            mResId = resId;
        }

        int getResId() {
            return mResId;
        }

        public static Font ofFontResId(int resId) {
            for (Font font : Font.values()) {
                if (font.getResId() == resId) {
                    return  font;
                }
            }
            return null;
        }
    }

    private static SparseArray<Typeface> sFontCache = new SparseArray<>(5);

    private static List<Integer> sCustomFontsResIds = new ArrayList<>(8);
    static {
        sCustomFontsResIds.add(R.string.ds_digib);
        sCustomFontsResIds.add(R.string.akrobat_light);
        sCustomFontsResIds.add(R.string.proxima_nova_regular);
        sCustomFontsResIds.add(R.string.proxima_nova_light);
        sCustomFontsResIds.add(R.string.proxima_nova_thin);
        sCustomFontsResIds.add(R.string.proxima_nova_semibold);
        sCustomFontsResIds.add(R.string.proxima_nova_bold);
        sCustomFontsResIds.add(R.string.proxima_nova_regular_condensed);
    }

    public static Typeface getTypeface(Font font) {
        return getTypeface(font, Typeface.NORMAL);
    }

    public static Typeface getTypeface(Font font, int style) {
        if (font != null) {
            int fontResId = font.getResId();
            Typeface typeface = sFontCache.get(fontResId);
            if (sCustomFontsResIds.contains(fontResId)) {
                if (typeface != null) {
                    return typeface;
                }
                try {
                    typeface = Typeface.createFromAsset(HSApplication.getContext().getAssets(),
                            "fonts/" + HSApplication.getContext().getString(fontResId) + ".ttf");
                } catch (RuntimeException e) {
                    try {
                        typeface = Typeface.createFromAsset(HSApplication.getContext().getAssets(),
                                "fonts/" + HSApplication.getContext().getString(fontResId) + ".otf");
                    } catch (RuntimeException ingored) {
                        return null;
                    }
                }
                sFontCache.put(fontResId, typeface);
            } else {
                // Already cached by framework.
                typeface = Typeface.create(HSApplication.getContext().getString(fontResId), style);
            }
            return typeface;
        }
        return null;
    }
}

package com.themelab.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaper;
import com.honeycomb.colorphone.wallpaper.theme.ThemeConstants;
import com.honeycomb.colorphone.wallpaper.theme.ThemeResources;
import com.ihs.app.framework.HSApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ThemeTextureProvider {
    public static Bitmap getDefaultTextureBitmap(String name) {
        Context context = HSApplication.getContext();
        ThemeResources res = ThemeResources.ofTheme(context, context.getPackageName());
        int layerCount;
        try {
            layerCount = res.getInteger(ThemeConstants.RES_NAME_3D_WALLPAPER_LAYER_COUNT);
        } catch (Resources.NotFoundException e) {
            return null;
        }

        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }

        int num = Integer.valueOf(matcher.group());

        for (int i = 0; i < layerCount; i++) {
            if (num == i + 1) {
                int resId = res.getIdentifier(
                        ThemeConstants.RES_NAME_3D_WALLPAPER_LAYER_PREFIX + Integer.toString(i + 1), "drawable");
                if (resId != 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                    return bitmap;
                }
            }
        }

        return null;
    }

    public static Bitmap getTextureBitmap(String name, LiveWallpaper liveWallpaper) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(name);
        if (!matcher.find()) {
            return null;
        }
        int num = Integer.valueOf(matcher.group());
        if (num > liveWallpaper.shaderTexTureUrls.size()) {
            return null;
        }
        return decodeBitmap(liveWallpaper.shaderTexTureUrls.get(num - 1));
    }

    public static Bitmap decodeBitmap(String name) {
        Context context = HSApplication.getContext();
        ThemeResources res = ThemeResources.ofTheme(context, context.getPackageName());
        int resId = res.getIdentifier(name, "drawable");
        if (resId != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeResource(HSApplication.getContext().getResources(), resId);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return bitmap;
        }

        return null;
    }
}

package com.acb.libwallpaper.live.livewallpaper;


import android.graphics.Bitmap;
import android.net.Uri;

import com.acb.libwallpaper.live.livewallpaper.confetti.render.ConfettiRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseWallpaperManager {

    protected static final String TAG = BaseWallpaperManager.class.getSimpleName();

    protected abstract int getType(String wallpaperName);

    public abstract boolean is3D(String wallpaperName);
    public abstract boolean touchable(String wallpaperName);

        // For shader-and-confetti wallpapers

    protected abstract String getShader(String wallpaperName);
    protected abstract Bitmap getShaderTexture(String wallpaperName, String name);

    public abstract ArrayList<HashMap<String, Object>> getConfettiAttrs(String wallpaperName, long category);
    public abstract ArrayList<ConfettiRenderer.TextureRecord> getConfettiTextures(String wallpaperName);

    // For video wallpapers

    protected abstract Uri getVideo(String wallpaperName);
}

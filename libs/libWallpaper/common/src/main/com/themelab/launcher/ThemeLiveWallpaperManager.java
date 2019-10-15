package com.themelab.launcher;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.acb.libwallpaper.live.livewallpaper.BaseWallpaperManager;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.acb.libwallpaper.live.livewallpaper.Program;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaper;
import com.acb.libwallpaper.live.livewallpaper.confetti.render.ConfettiRenderer;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * ThemeLiveWallpaperManager should ONLY be created once
 */
public class ThemeLiveWallpaperManager extends BaseWallpaperManager {

    private static final String PARALLAX_3D_SHADER_NAME = "parallax_3d_3.glsl";

    private String shader;
    private LiveWallpaper liveWallpaper;

    public ThemeLiveWallpaperManager(String themeName) {
        liveWallpaper = new LiveWallpaper(themeName, "");
    }

    @Override
    protected int getType(String wallpaperName) {
        return LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI;
    }

    public boolean touchable(String wallpaperName) {
        LiveWallpaper wallpaper = liveWallpaper;
        return wallpaper.touchable() || wallpaper.isRipple();
    }

    public boolean is3D(String wallpaperName) {
        LiveWallpaper wallpaper = liveWallpaper;
        return wallpaper.is3D() && !wallpaper.touchable();
    }

    @Override
    protected String getShader(String wallpaperName) {
        if (!TextUtils.isEmpty(shader)) {
            return shader;
        }
        try {
            if (isCustomize()) {
                shader = liveWallpaper.shader;
            } else {
                shader = Program.loadShaderResource(HSApplication.getContext(), PARALLAX_3D_SHADER_NAME);
            }
        } catch (IOException e) {
            HSLog.e(TAG, "Shader not loaded: " + PARALLAX_3D_SHADER_NAME);
            shader = "";
        }
        return shader;
    }

    @Override
    protected Bitmap getShaderTexture(String wallpaperName, String name) {
        if (isCustomize()) {
            return ThemeTextureProvider.getTextureBitmap(name, liveWallpaper);
        }
        return ThemeTextureProvider.getDefaultTextureBitmap(name);
    }

    @Override
    public ArrayList<HashMap<String, Object>> getConfettiAttrs(String wallpaperName, long category) {
        if (isCustomize()) {
            if (LiveWallpaperConsts.BACKGROUND == category) {
                return liveWallpaper.confettiBgSettings;
            } else if (LiveWallpaperConsts.CLICK == category) {
                return liveWallpaper.confettiClickSettings;
            } else if (LiveWallpaperConsts.TOUCH == category) {
                return liveWallpaper.confettiTouchSettings;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public @NonNull
    ArrayList<ConfettiRenderer.TextureRecord> getConfettiTextures(String wallpaperName) {
        ArrayList<ConfettiRenderer.TextureRecord> confettiTextures = new ArrayList<>();

        if (isCustomize()) {
            Stream.of(liveWallpaper.confettiSources)
                    .withoutNulls()
                    .filter(source -> !TextUtils.isEmpty(source.getUrl()))
                    .map(source -> {
                        Bitmap bitmap = ThemeTextureProvider.decodeBitmap(source.getUrl());
                        if (bitmap == null) {
                            return null;
                        }
                        return new ConfettiRenderer.TextureRecord(bitmap,
                                source.getRatio(), source.getId(), source.getCategory());
                    })
                    .withoutNulls()
                    .forEach(confettiTextures::add);
        }

        return confettiTextures;
    }

    @Override
    protected Uri getVideo(String wallpaperName) {
        return null;
    }

    private boolean isCustomize() {
        return liveWallpaper != null && liveWallpaper.successFlag();
    }
}

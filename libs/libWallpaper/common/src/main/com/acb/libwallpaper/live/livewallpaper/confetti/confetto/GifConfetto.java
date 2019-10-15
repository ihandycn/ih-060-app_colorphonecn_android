package com.acb.libwallpaper.live.livewallpaper.confetti.confetto;


import com.acb.libwallpaper.live.livewallpaper.confetti.render.ConfettiRenderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GifConfetto extends Confetto {

    private final List<ConfettiRenderer.TextureRecord> textures = new ArrayList<>();

    public GifConfetto(List<ConfettiRenderer.TextureRecord> textures) {
        this.textures.addAll(textures);
    }


    @Override public int getWidth() {
        int width = 0;
        for (ConfettiRenderer.TextureRecord texture : textures) {
            if (width < texture.width) {
                width = texture.width;
            }
        }
        return width;
    }

    @Override public int getHeight() {
        int height = 0;
        for (ConfettiRenderer.TextureRecord texture : textures) {
            if (height < texture.height) {
                height = texture.height;
            }
        }
        return height;
    }

    @Override public int getMaxDistanceFromCenter() {
        int width = getWidth();
        int height = getHeight();
        return (int) Math.ceil(Math.sqrt(width * width + height * height) / 2.0);
    }

    @Override
    protected void drawImpl(ConfettiRenderer.GLContext drawingInfo, float x, float y, float rotation, float scale, float alpha) {

    }

    @Override public void writeSizeToBuffer(FloatBuffer buffer) {
        buffer.put(getWidth());
        buffer.put(getHeight());
    }

    @Override public void writeOffsetToBuffer(FloatBuffer buffer) {
        buffer.put(currentX);
        buffer.put(bound.height() - currentY);
    }

    @Override public void writeRotationToBuffer(FloatBuffer buffer) {
        buffer.put(currentRotation);
    }

    @Override public void writeScaleToBuffer(FloatBuffer buffer) {
        buffer.put(currentRealScale);
    }

    @Override public int getTextureId() {
        return 0;
    }
}

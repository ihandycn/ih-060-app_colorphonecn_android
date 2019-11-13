package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.confetto;



import com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.render.ConfettiRenderer;

import java.nio.FloatBuffer;

public class ShaderConfetto extends Confetto {

    @Override public int getWidth() {
        return 0;
    }

    @Override public int getHeight() {
        return 0;
    }

    @Override public int getMaxDistanceFromCenter() {
        return 0;
    }

    @Override
    protected void drawImpl(ConfettiRenderer.GLContext drawingInfo, float x, float y, float rotation, float scale, float alpha) {

    }

    @Override public void writeSizeToBuffer(FloatBuffer buffer) {

    }

    @Override public void writeOffsetToBuffer(FloatBuffer buffer) {

    }

    @Override public void writeRotationToBuffer(FloatBuffer buffer) {

    }

    @Override public void writeScaleToBuffer(FloatBuffer buffer) {

    }

    @Override public int getTextureId() {
        return 0;
    }
}

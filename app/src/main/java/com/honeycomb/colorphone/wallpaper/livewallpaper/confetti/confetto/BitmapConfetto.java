/**
 * Copyright (C) 2016 Robinhood Markets, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.confetto;

import android.opengl.GLES20;


import com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.ConfettiManager;
import com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.render.ConfettiRenderer;
import com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.render.ConfettiRendererGLES20;

import java.nio.FloatBuffer;

public class BitmapConfetto extends Confetto {

    private final float[] vec2 = new float[2];

    private final ConfettiRenderer.TextureRecord texture;

    public BitmapConfetto(ConfettiRenderer.TextureRecord texture) {
        this.texture = texture;
    }

    @Override
    public int getWidth() {
        return texture.width;
    }

    @Override
    public int getHeight() {
        return texture.height;
    }

    @Override
    public int getMaxDistanceFromCenter() {
        int width = getWidth();
        int height = getHeight();
        return (int) Math.ceil(Math.sqrt(width * width + height * height) / 2.0);
    }

    @Override
    protected void drawImpl(ConfettiRenderer.GLContext glContext,
                            float x, float y, float rotation, float scale, float alpha) {
        if (glContext instanceof ConfettiRendererGLES20.GLContext
                && passConfettoInfo(
                        (ConfettiRendererGLES20.GLContext) glContext, x, y, rotation, scale, alpha)) {
            GLES20.glDrawArrays(
                    GLES20.GL_TRIANGLE_STRIP,
                    0,
                    4);
        }
    }

    @Override
    public void writeSizeToBuffer(FloatBuffer buffer) {
        buffer.put(getWidth());
        buffer.put(getHeight());
    }

    @Override
    public void writeOffsetToBuffer(FloatBuffer buffer) {
        buffer.put(currentX);
        buffer.put(bound.height() - currentY);
    }

    @Override
    public void writeRotationToBuffer(FloatBuffer buffer) {
        buffer.put(currentRotation);
    }

    @Override
    public void writeScaleToBuffer(FloatBuffer buffer) {
        buffer.put(currentRealScale);
    }

    @Override
    public int getTextureId() {
        return texture.textureId;
    }

    private boolean passConfettoInfo(ConfettiRendererGLES20.GLContext glContext,
                                     float x, float y, float rotation, float scale, float alpha) {
        if (scale <= 0f || alpha <= 0f) {
            return false;
        }

        if (glContext.sizeLoc > -1) {
            vec2[0] = getWidth() * scale * ConfettiManager.DEVICE_INDEPENDENT_CORRECTION;
            vec2[1] = getHeight() * scale * ConfettiManager.DEVICE_INDEPENDENT_CORRECTION;
            GLES20.glUniform2fv(
                    glContext.sizeLoc,
                    1,
                    vec2,
                    0);
        } else {
            return false;
        }

        if (glContext.offsetLoc > -1) {
            vec2[0] = x - vec2[0] /* width */ / 2;
            vec2[1] = bound.height() - y - vec2[1] /* height */ / 2;
            GLES20.glUniform2fv(
                    glContext.offsetLoc,
                    1,
                    vec2,
                    0);
        } else {
            return false;
        }

        if (glContext.rotationLoc > -1) {
            GLES20.glUniform1f(
                    glContext.rotationLoc,
                    rotation);
        }

        if (glContext.alphaLoc > -1) {
            GLES20.glUniform1f(
                    glContext.alphaLoc,
                    alpha);
        }

        glContext.textureBinder.reset();
        glContext.textureBinder.bind(
                glContext.textureLoc,
                GLES20.GL_TEXTURE_2D,
                texture.textureId);

        return true;
    }

    @Override
    public String toString() {
        return "BitmapConfetto (" + getWidth() + "x" + getHeight() +
                "), textureId: " + texture.textureId;
    }
}

package com.honeycomb.colorphone.customize.livewallpaper.confetti.render;

import android.opengl.GLES20;

import com.honeycomb.colorphone.customize.livewallpaper.BaseWallpaperManager;
import com.honeycomb.colorphone.customize.livewallpaper.ShaderRenderer;
import com.honeycomb.colorphone.customize.livewallpaper.TextureParameters;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.CommonConfetti;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.ConfettiManager;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.confetto.Confetto;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public class ConfettiRendererGLES20 extends ConfettiRenderer {

    private static final String PRECISION_HEADER =
            "#ifdef GL_FRAGMENT_PRECISION_HIGH\n" +
                    "precision highp float;\n" +
                    "#else\n" +
                    "precision mediump float;\n" +
                    "#endif\n";

    private static final String ROTATION_FUNC =
            "vec2 rotate(vec2 original, vec2 pivot, float direction) {" +
                    "float radian = direction * radians(rotation);" +
                    "float cosVal = cos(radian);" +
                    "float sinVal = sin(radian);" +
                    "mat2 rotationM = mat2(" +
                    "    cosVal, -sinVal," +
                    "    sinVal, cosVal);" +
                    "return pivot + rotationM * (original - pivot);" +
                    "}";

    private static final String VERTEX_SHADER =
            PRECISION_HEADER +
                    "attribute vec2 position;" +
                    "uniform vec2 resolution;" +
                    "uniform vec2 size;" +
                    "uniform vec2 offset;" +
                    "uniform float rotation;" +
                    ROTATION_FUNC +
                    "void main() {" +
                    "vec2 transformed = vec2(-1.0, -1.0) + 2.0 * " +
                    "(offset + size * (position - vec2(-1.0, -1.0)) / 2.0) / resolution;" +
                    "vec2 pivot = vec2(-1.0, -1.0) + 2.0 * " +
                    "(offset + 0.5 * size) / resolution;" +
                    "vec2 rotated = rotate(transformed, pivot, 1.0);" +
                    "gl_Position = vec4(rotated, 0.0, 1.0);" +
                    "}";
    private static final String FRAGMENT_SHADER =
            PRECISION_HEADER +
                    "uniform vec2 size;" +
                    "uniform vec2 offset;" +
                    "uniform float rotation;" +
                    "uniform float alpha;" +
                    "uniform sampler2D texture;" +
                    ROTATION_FUNC +
                    "void main(void) {" +
                    "vec2 pivot = offset + 0.5 * size;" +
                    "vec2 rotated = rotate(gl_FragCoord.xy, pivot, -1.0);" +
                    "vec2 texCoords = (rotated - offset) / size.xy;" +
                    "vec4 texturePixel = texture2D(texture, vec2(texCoords.x, 1.0 - texCoords.y)).rgba;" +
                    "gl_FragColor = vec4(texturePixel.rgb, texturePixel.a * alpha);" +
                    "}";

    public static class GLContext extends ConfettiRenderer.GLContext {
        public int sizeLoc;
        public int offsetLoc;
        public int rotationLoc;
        public int alphaLoc;
        public int textureLoc;
        public ShaderRenderer.TextureBinder textureBinder;

        GLContext(int sizeLoc, int offsetLoc, int rotationLoc, int alphaLoc, int textureLoc,
                  ShaderRenderer.TextureBinder textureBinder) {
            this.sizeLoc = sizeLoc;
            this.offsetLoc = offsetLoc;
            this.rotationLoc = rotationLoc;
            this.alphaLoc =  alphaLoc;
            this.textureLoc = textureLoc;
            this.textureBinder = textureBinder;
        }
    }

    private GLContext glContext;

    private int particlePositionLoc;
    private int particleResolutionLoc;

    public ConfettiRendererGLES20(ByteBuffer vertexBuffer,
                                  ShaderRenderer.TextureBinder textureBinder, BaseWallpaperManager manager, String wallpaperName) {
        super(vertexBuffer, textureBinder, manager, wallpaperName);
    }

    @Override
    void getShaders(String[] shaders) {
        shaders[0] = VERTEX_SHADER;
        shaders[1] = FRAGMENT_SHADER;
    }

    @Override
    public void createTextures() {
        deleteTextures();

        // Bitmap count = SHAPE_TYPE_COUNT * confettoColors.length
        generateConfettiBitmaps(textures);
        for (int i = 0; i < textures.size(); i++) {
            TextureRecord textureRecord = textures.get(i);
            int[] textureId = new int[1];
            GLES20.glGenTextures(1, textureId, 0);
            textureRecord.textureId = textureId[0];
            ShaderRenderer.createTexture(textureId[0], textureRecord.bitmap,
                    new TextureParameters(
                            GLES20.GL_LINEAR, GLES20.GL_LINEAR, // Anti-aliasing
                            GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE));
            textureRecord.releaseBitmap(); // Uploaded to video memory, no need to keep it here
        }
    }

    @Override
    void deleteTextures() {
        if (textures.isEmpty()) {
            return;
        }
        int[] textureIds = new int[textures.size()];
        int index = 0;
        for (TextureRecord texture : textures) {
            textureIds[index++] = texture.textureId;
        }
        if (index > 0) {
            GLES20.glDeleteTextures(index, textureIds, 0);
        }
        textures.clear();
    }

    @Override
    public void indexLocations() {
        particlePositionLoc = GLES20.glGetAttribLocation(
                particleProgram, "position");
        particleResolutionLoc = GLES20.glGetUniformLocation(
                particleProgram, "resolution");
        int particleSizeLoc = GLES20.glGetUniformLocation(
                particleProgram, "size");
        int particleOffsetLoc = GLES20.glGetUniformLocation(
                particleProgram, "offset");
        int particleRotationLoc = GLES20.glGetUniformLocation(
                particleProgram, "rotation");
        int particleAlphaLoc = GLES20.glGetUniformLocation(
                particleProgram, "alpha");
        int particleTextureLoc = GLES20.glGetUniformLocation(
                particleProgram, "texture");

        // Context passed to drawing methods
        glContext = new GLContext(
                particleSizeLoc,
                particleOffsetLoc,
                particleRotationLoc,
                particleAlphaLoc,
                particleTextureLoc,
                textureBinder);
    }

    @Override
    public void setupBuffers() {
        // No buffer to setup, quad vertex buffer is passed in from ShaderRenderer
    }

    @Override
    public void drawFrame(long now, int fps) {
        if (resolution[0] == 0 && resolution[1] == 0) {
            return;
        }

        GLES20.glUseProgram(particleProgram);
        GLES20.glVertexAttribPointer(
                particlePositionLoc,
                2,
                GLES20.GL_BYTE,
                false,
                0,
                vertexBuffer);

        if (particleResolutionLoc > -1) {
            GLES20.glUniform2fv(
                    particleResolutionLoc,
                    1,
                    resolution,
                    0);
        }

        if (srcBlendMode != GLES20.GL_ONE) {
            GLES20.glBlendFunc(srcBlendMode, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        }

        if (needFastForward) {
            forwardTime = 4000000000L; // fast forward 4s
            needFastForward = false;
            for (int i = 0; i < bgConfettis.size(); i++) {
                CommonConfetti bgConfetti = bgConfettis.get(i);
                bgConfetti.getConfettiManager().seekFrames(now, forwardTime);
            }
        }
        for (int i = 0; i < bgConfettis.size(); i++) {
            // bgConfettis may fast forward some frames to get better effects.
            CommonConfetti bgConfetti = bgConfettis.get(i);
            bgConfetti.getConfettiManager().onAnimationFrame(glContext, now + forwardTime, fps);
        }

        synchronized (touchConfettis) {
            for (Iterator<CommonConfetti> iterator = touchConfettis.iterator(); iterator.hasNext(); ) {
                CommonConfetti confetti = iterator.next();
                ConfettiManager confettiManager = confetti.getConfettiManager();
                List<Confetto> drawn = confettiManager.onAnimationFrame(glContext, now, fps);
                if (confettiManager.isTerminated() && drawn.isEmpty()) {
                    iterator.remove();
                }
            }

            for (Iterator<CommonConfetti> iterator = clickConfettis.iterator(); iterator.hasNext(); ) {
                CommonConfetti confetti = iterator.next();
                ConfettiManager confettiManager = confetti.getConfettiManager();
                List<Confetto> drawn = confettiManager.onAnimationFrame(glContext, now, fps);
                if (confettiManager.isTerminated() && drawn.isEmpty()) {
                    iterator.remove();
                }
            }
        }

        if (srcBlendMode != GLES20.GL_ONE) {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }
}

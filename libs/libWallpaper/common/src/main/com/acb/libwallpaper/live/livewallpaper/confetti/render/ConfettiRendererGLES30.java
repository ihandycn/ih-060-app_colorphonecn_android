package com.acb.libwallpaper.live.livewallpaper.confetti.render;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.opengl.GLES30;

import com.acb.libwallpaper.live.livewallpaper.BaseWallpaperManager;
import com.acb.libwallpaper.live.livewallpaper.ShaderRenderer;
import com.acb.libwallpaper.live.livewallpaper.TextureParameters;
import com.acb.libwallpaper.live.livewallpaper.confetti.CommonConfetti;
import com.acb.libwallpaper.live.livewallpaper.confetti.ConfettiManager;
import com.acb.libwallpaper.live.livewallpaper.confetti.confetto.Confetto;
import com.ihs.commons.utils.HSLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: use instanced rendering to boost performance.
 */
@SuppressLint("NewApi")
public class ConfettiRendererGLES30 extends ConfettiRenderer {

    private static final String TAG = ConfettiRendererGLES30.class.getSimpleName();

    private static final String VERTEX_SHADER =
            "#version 300 es\n" +
                    "in vec2 position;" +
                    "in vec2 size;" +
                    "in vec2 offset;" +
                    "in vec4 atlasIndex;" +
                    "uniform vec2 resolution;" +
                    "out vec2 vSize;" +
                    "out vec2 vOffset;" +
                    "out vec4 vAtlasIndex;" +
                    "void main() {" +
                    "vec2 transformed = vec2(-1.0, -1.0) + 2.0 * " +
                    "(offset + size * (position - vec2(-1.0, -1.0)) / 2.0) / resolution;" +
                    "gl_Position = vec4(transformed, 0.0, 1.0);" +
                    "vSize = size;" + // Pass through
                    "vOffset = offset;" + // Pass through
                    "vAtlasIndex = atlasIndex;" + // Pass through
                    "}";
    private static final String FRAGMENT_SHADER =
            "#version 300 es\n" +
                    "#ifdef GL_FRAGMENT_PRECISION_HIGH\n" +
                    "precision highp float;\n" +
                    "#else\n" +
                    "precision mediump float;\n" +
                    "#endif\n" +
                    "in vec2 vSize;" +
                    "in vec2 vOffset;" +
                    "in vec4 vAtlasIndex;" +
                    "uniform sampler2D texture;" +
                    "out vec4 outColor;" +
                    "void main(void) {" +
                    "vec2 textureCoord = (gl_FragCoord.xy - vOffset) / vSize.xy;" +
                    "outColor = texture(texture, " +
                    "vAtlasIndex.xy + textureCoord * vAtlasIndex.zw).rgba;" +
                    "}";

    private static final int VERTEX_BUFFER_COUNT = 4;
    private static final int POSITION_ATTRIB = 0;
    private static final int SIZE_ATTRIB = 1;
    private static final int OFFSET_ATTRIB = 2;
    private static final int ATLAS_INDEX_ATTRIB = 3;

    private static final int PER_INSTANCE_ATTRIB_COUNT = 3;
    private static final int[] PER_INSTANCE_BUFFER_INDICES = new int[]{
            SIZE_ATTRIB,
            OFFSET_ATTRIB,
            ATLAS_INDEX_ATTRIB,
    };
    private static final int[] PER_INSTANCE_ATTRIB_SIZES = new int[]{
            2, // Size
            2, // Offset
            4, // Atlas
    };

    private static final int SIZEOF_FLOAT = 4;

    private static class GLContext extends ConfettiRenderer.GLContext {
    }

    private GLContext glContext;

    private int positionLoc;
    private int resolutionLoc;
    private int sizeLoc;
    private int offsetLoc;
    private int atlasIndexLoc;
    private int textureLoc;

    private TextureAtlas textureAtlas;
    private float[] atlasIndex = new float[4];

    private int[] vertexBufferState;
    private int[] vertexBufferLocs;


    public ConfettiRendererGLES30(ByteBuffer vertexBuffer,
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

        generateConfettiBitmaps(textures);

        textureAtlas = TextureAtlas.create(textures);

        int[] textureId = new int[1];
        GLES30.glGenTextures(1, textureId, 0);
        textureAtlas.atlasId = textureId[0];

        // We don't generating mipmap for the atlas to prevent bleeding between textures
        ShaderRenderer.createTexture(textureId[0], textureAtlas.atlasBitmap,
                new TextureParameters(
                        GLES30.GL_LINEAR, GLES30.GL_LINEAR, // Anti-aliasing
                        GLES30.GL_CLAMP_TO_EDGE, GLES30.GL_CLAMP_TO_EDGE), false);
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
            GLES30.glDeleteTextures(index, textureIds, 0);
        }
        textures.clear();
    }

    @Override
    public void indexLocations() {
        positionLoc = GLES30.glGetAttribLocation(
                particleProgram, "position");
        sizeLoc = GLES30.glGetAttribLocation(
                particleProgram, "size");
        offsetLoc = GLES30.glGetAttribLocation(
                particleProgram, "offset");
        atlasIndexLoc = GLES30.glGetAttribLocation(
                particleProgram, "atlasIndex");

        resolutionLoc = GLES30.glGetUniformLocation(
                particleProgram, "resolution");
        textureLoc = GLES30.glGetUniformLocation(
                particleProgram, "texture");

        // Context passed to drawing methods
        glContext = new GLContext();
    }

    @Override
    public void setupBuffers() {
        // VAO
        vertexBufferState = new int[1];
        GLES30.glGenVertexArrays(
                1,
                vertexBufferState,
                0);
        GLES30.glBindVertexArray(
                vertexBufferState[0]);

        vertexBufferLocs = new int[VERTEX_BUFFER_COUNT];
        GLES30.glGenBuffers(
                VERTEX_BUFFER_COUNT,
                vertexBufferLocs,
                0);

        // Position
        GLES30.glBindBuffer(
                GLES30.GL_ARRAY_BUFFER,
                vertexBufferLocs[POSITION_ATTRIB]);
        FloatBuffer vertexBufferFloat = (FloatBuffer) ByteBuffer.allocateDirect(2 * 4 * SIZEOF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(new float[]{
                        -1, 1,
                        -1, -1,
                        1, 1,
                        1, -1
                })
                .position(0);
        GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                2 * 4 * SIZEOF_FLOAT,
                vertexBufferFloat,
                GLES30.GL_STATIC_DRAW);

        // Size, offset, atlasIndex
        int[] locs = new int[]{sizeLoc, offsetLoc, atlasIndexLoc};
        for (int i = 0; i < PER_INSTANCE_ATTRIB_COUNT; i++) {
            int bufferIndex = PER_INSTANCE_BUFFER_INDICES[i];
            GLES30.glBindBuffer(
                    GLES30.GL_ARRAY_BUFFER,
                    vertexBufferLocs[bufferIndex]);
            GLES30.glBufferData(
                    GLES30.GL_ARRAY_BUFFER,
                    ConfettiManager.MAX_CONFETTO_COUNT * PER_INSTANCE_ATTRIB_SIZES[i] * SIZEOF_FLOAT,
                    null,
                    GLES30.GL_DYNAMIC_DRAW);
        }

        // Position
        GLES30.glBindBuffer(
                GLES30.GL_ARRAY_BUFFER,
                vertexBufferLocs[POSITION_ATTRIB]);
        GLES30.glVertexAttribPointer(
                positionLoc,
                2,
                GLES30.GL_FLOAT,
                false,
                0,
                0);
        GLES30.glEnableVertexAttribArray(positionLoc);
        GLES30.glVertexAttribDivisor(positionLoc, 0);

        // Size, offset, atlasIndex
        for (int i = 0; i < PER_INSTANCE_ATTRIB_COUNT; i++) {
            int bufferIndex = PER_INSTANCE_BUFFER_INDICES[i];
            int loc = locs[i];
            int size = PER_INSTANCE_ATTRIB_SIZES[i];
            GLES30.glBindBuffer(
                    GLES30.GL_ARRAY_BUFFER,
                    vertexBufferLocs[bufferIndex]);
            GLES30.glVertexAttribPointer(
                    loc,
                    size,
                    GLES30.GL_FLOAT,
                    false,
                    0,
                    0);
            GLES30.glEnableVertexAttribArray(loc);
            GLES30.glVertexAttribDivisor(loc, 1);
        }

        GLES30.glBindVertexArray(0);
    }

    @Override
    public void drawFrame(long now, int fps) {
        if (resolution[0] == 0 && resolution[1] == 0) {
            return;
        }

        GLES30.glUseProgram(particleProgram);
        GLES30.glBindVertexArray(vertexBufferState[0]);

        List<Confetto> confettiToDraw = new ArrayList<>();
        for (int i = 0; i < bgConfettis.size(); i++) {
            CommonConfetti bgConfetti = bgConfettis.get(i);
            confettiToDraw.addAll(bgConfetti.getConfettiManager().onAnimationFrame(glContext, now, fps));
        }
        synchronized (touchConfettis) {
            for (Iterator<CommonConfetti> iterator = touchConfettis.iterator(); iterator.hasNext(); ) {
                CommonConfetti confetti = iterator.next();
                ConfettiManager confettiManager = confetti.getConfettiManager();
                List<Confetto> touchConfetti = confettiManager.onAnimationFrame(glContext, now, fps);
                if (confettiManager.isTerminated() && touchConfetti.isEmpty()) {
                    iterator.remove();
                } else {
                    confettiToDraw.addAll(touchConfetti);
                }
            }
        }
        int confettiSize = confettiToDraw.size();
        if (confettiSize == 0) {
            return;
        }

        for (int i = 0; i < PER_INSTANCE_ATTRIB_COUNT; i++) {
            GLES30.glBindBuffer(
                    GLES30.GL_ARRAY_BUFFER,
                    vertexBufferLocs[PER_INSTANCE_BUFFER_INDICES[i]]);
            ByteBuffer byteBuffer = (ByteBuffer) GLES30.glMapBufferRange(
                    GLES30.GL_ARRAY_BUFFER,
                    0,
                    confettiSize * PER_INSTANCE_ATTRIB_SIZES[i] * SIZEOF_FLOAT,
                    GLES30.GL_MAP_WRITE_BIT | GLES30.GL_MAP_INVALIDATE_BUFFER_BIT);
            if (byteBuffer == null) {
                int error = GLES30.glGetError();
                HSLog.w(TAG, "Error code: " + error);
                continue;
            }
            FloatBuffer buffer = byteBuffer
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();

            buffer.position(0);
            for (int cIndex = 0; cIndex < confettiToDraw.size(); cIndex++) {
                Confetto confetto = confettiToDraw.get(cIndex);
                switch (i) {
                    case 0:
                        confetto.writeSizeToBuffer(buffer);
                        break;
                    case 1:
                        confetto.writeOffsetToBuffer(buffer);
                        break;
                    case 2:
                        int textureId = confetto.getTextureId();
                        textureAtlas.getTextureIndex(textureId, atlasIndex);
                        buffer.put(atlasIndex);
                        break;
                }
            }
            buffer.position(0);

            GLES30.glUnmapBuffer(GLES30.GL_ARRAY_BUFFER);
        }

        GLES30.glUniform2fv(
                resolutionLoc,
                1,
                resolution,
                0);
        textureBinder.bind(
                textureLoc,
                GLES30.GL_TEXTURE_2D,
                textureAtlas.atlasId);

        GLES30.glDrawArraysInstanced(
                GLES30.GL_TRIANGLE_STRIP,
                0,
                4,
                confettiSize);

        GLES30.glBindVertexArray(0);
    }

    private static class TextureAtlas {
        int numTextures;
        Bitmap atlasBitmap;
        int atlasId;
        int atlasWidth, atlasHeight;

        /** (offsetX, offsetY, sizeX, sizeY) tuples in space [0f,0f-1f,1f]. */
        float[] indices;

        static TextureAtlas create(List<TextureRecord> textures) {
            TextureAtlas atlas = new TextureAtlas();

            atlas.numTextures = 1; // TODO
            atlas.indices = new float[atlas.numTextures * 4];
            atlas.buildAtlas(textures);

            return atlas;
        }

        /**
         * Pack given textures into {@link #atlasBitmap}, write position index of each texture
         * into {@link #indices}.
         */
        private void buildAtlas(List<TextureRecord> textures) {
            // TODO: replace this trivial implementation with a real one.
            atlasBitmap = textures.get(0).bitmap;
            atlasWidth = atlasBitmap.getWidth();
            atlasHeight = atlasBitmap.getHeight();
        }

        void getTextureIndex(int textureId, float[] outIndex) {
            outIndex[0] = outIndex[1] = 0f;
            outIndex[2] = outIndex[3] = 1f;
        }
    }
}

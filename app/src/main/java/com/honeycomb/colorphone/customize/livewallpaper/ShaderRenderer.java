package com.honeycomb.colorphone.customize.livewallpaper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.honeycomb.colorphone.customize.livewallpaper.confetti.render.ConfettiRenderer;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.render.ConfettiRendererGLES20;
import com.honeycomb.colorphone.customize.livewallpaper.confetti.render.RenderThread;
import com.honeycomb.colorphone.customize.livewallpaper.guide.FakeGravity;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.AccelerometerListener;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.CameraListener;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.GyroscopeListener;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.LightListener;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.MagneticFieldListener;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.PressureListener;
import com.honeycomb.colorphone.customize.livewallpaper.hardware.ProximityListener;
import com.honeycomb.colorphone.util.Thunk;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hugo.weaving.DebugLog;

public class ShaderRenderer implements GLSurfaceView.Renderer {

	private boolean indexLocated;
	private float[] mProjMatrix = new float[16];
	private float[] mMMatrix = new float[16];
	private float[] mVMatrix = new float[16];

	private float[] mMVPMatrix = new float[16];

	public interface OnRendererListener {
		void onInfoLogs(List<String> infoLogs);
		void onFramesPerSecond(int fps);
	}

	@SuppressWarnings("WeakerAccess")
    @Thunk static final int TEXTURE_UNITS[] = {
			GLES20.GL_TEXTURE0,
			GLES20.GL_TEXTURE1,
			GLES20.GL_TEXTURE2,
			GLES20.GL_TEXTURE3,
			GLES20.GL_TEXTURE4,
			GLES20.GL_TEXTURE5,
			GLES20.GL_TEXTURE6,
			GLES20.GL_TEXTURE7,
			GLES20.GL_TEXTURE8,
			GLES20.GL_TEXTURE9,
			GLES20.GL_TEXTURE10,
			GLES20.GL_TEXTURE11,
			GLES20.GL_TEXTURE12,
			GLES20.GL_TEXTURE13,
			GLES20.GL_TEXTURE14,
			GLES20.GL_TEXTURE15,
			GLES20.GL_TEXTURE16,
			GLES20.GL_TEXTURE17,
			GLES20.GL_TEXTURE18,
			GLES20.GL_TEXTURE19,
			GLES20.GL_TEXTURE20,
			GLES20.GL_TEXTURE21,
			GLES20.GL_TEXTURE22,
			GLES20.GL_TEXTURE23,
			GLES20.GL_TEXTURE24,
			GLES20.GL_TEXTURE25,
			GLES20.GL_TEXTURE26,
			GLES20.GL_TEXTURE27,
			GLES20.GL_TEXTURE28,
			GLES20.GL_TEXTURE29,
			GLES20.GL_TEXTURE30,
			GLES20.GL_TEXTURE31};

	private static final float NS_PER_SECOND = 1000000000f;
	private static final long FPS_UPDATE_FREQUENCY_NS = 200000000L;
	private static final long BATTERY_UPDATE_INTERVAL = 10000000000L;
	private static final long DATE_UPDATE_INTERVAL = 1000000000L;

	private static final String CAMERA_BACK = "cameraBack";
	private static final String CAMERA_FRONT = "cameraFront";
	private static final String SAMPLER_2D = "2D";
	private static final String SAMPLER_EXTERNAL_OES = "ExternalOES";

	private static final String TEXTURE_NAME_PATTERN = "[a-zA-Z0-9_]+";
	private static final Pattern PATTERN_SAMPLER = Pattern.compile(
			String.format(
					"uniform[ \t]+sampler(" +
							SAMPLER_2D + "|" +
							SAMPLER_EXTERNAL_OES +
							")+[ \t]+(%s);[ \t]*(.*)",
					TEXTURE_NAME_PATTERN));
	private static final Pattern PATTERN_FTIME = Pattern.compile(
			"^#define[ \\t]+FTIME_PERIOD[ \\t]+([0-9\\.]+)[ \\t]*$",
			Pattern.MULTILINE);
	private static final String OES_EXTERNAL =
			"#extension GL_OES_EGL_image_external : require\n";

    private static final String TRIVIAL_VERTEX_SHADER_MATRIX =
            "attribute vec2 position;" +
                    "uniform mat4 mvpMatrix;"+
					"attribute vec2 vCoordinate;"+
					"varying vec2 aCoordinate;"+
                    "void main() {" +
                    "gl_Position = mvpMatrix * vec4(position, 0., 1.);" +
					"aCoordinate=vCoordinate;"+
                    "}";

	private static final String TRIVIAL_VERTEX_SHADER =
			"attribute vec2 position;" +
					"void main() {" +
					"gl_Position = vec4(position, 0., 1.);" +
					"}";
	public static final String TRIVIAL_FRAGMENT_SHADER =
			"#ifdef GL_FRAGMENT_PRECISION_HIGH\n" +
					"precision highp float;\n" +
					"#else\n" +
					"precision mediump float;\n" +
					"#endif\n" +
					"uniform vec2 resolution;" +
					"uniform sampler2D frame;" +
					"varying vec2 aCoordinate;"+
					"void main(void) {" +
					"gl_FragColor = texture2D(frame," +
					"aCoordinate).rgba;" +
					"}";

	private final TextureBinder textureBinder = new TextureBinder();
	private final ArrayList<String> textureNames = new ArrayList<>();
	private final ArrayList<TextureParameters> textureParameters =
			new ArrayList<>();
	private final TextureParameters backBufferTextureParams =
			new TextureParameters(
					GLES20.GL_NEAREST,
					GLES20.GL_NEAREST,
					GLES20.GL_CLAMP_TO_EDGE,
					GLES20.GL_CLAMP_TO_EDGE);

	private final int fb[] = new int[]{0, 0};
	private final int tx[] = new int[]{0, 0};
	private final float[] sCoord={
			0.0f,1.0f,
			0.0f,0.0f,
			1.0f,1.0f,
			1.0f,0.0f,
	};
    private int numberOfTextures = 0;
	private final int textureLocs[] = new int[32];
	private final int textureSizeLocs[] = new int[32];
	private final int textureSizes[][] = new int[32][2];
	private final int textureTargets[] = new int[32];
	private final int textureIds[] = new int[32];
	private final float surfaceResolution[] = new float[]{0, 0};
	private final float resolution[] = new float[]{0, 0};
	private final float touch[] = new float[]{0, 0};
	private final float mouse[] = new float[]{0, 0};
	private final float pointers[] = new float[30];
	private final float pointerRingBuffer[] = new float[15 * 3]; // Maximum history size: 15
	private final float offset[] = new float[]{0, 0};
	private final float dateTime[] = new float[]{0, 0, 0, 0};
	private final float rotationMatrix[] = new float[9];
	private final float orientation[] = new float[]{0, 0, 0};

	private final Context context;
	private final BaseWallpaperManager manager;
	private volatile String wallpaperName;
	private final ByteBuffer vertexBuffer;
	private final FloatBuffer textureBuffer;

	private AccelerometerListener accelerometerListener;
	private CameraListener cameraListener;
	private GyroscopeListener gyroscopeListener;
	private MagneticFieldListener magneticFieldListener;
	private LightListener lightListener;
	private PressureListener pressureListener;
	private ProximityListener proximityListener;

	private OnRendererListener onRendererListener;

	private String fragmentShader;
	private int surfaceProgram = 0;
	private int surfacePositionLoc;
	private int surfaceResolutionLoc;
	private int surfaceFrameLoc;
	private int surfaceMatrixLoc;
	private int surfaceCoordinate;

    private ConfettiRenderer particleRenderer;

	private int program = 0;
	private int positionLoc;
	private int timeLoc;
	private int densityRatioLoc;
	private int secondLoc;
	private int subSecondLoc;
	private int fTimeLoc;
	private int resolutionLoc;
	private int touchLoc;
	private int mouseLoc;
	private int pointerCountLoc;
	private int pointersLoc;
	private int pointerHistorySizeLoc;
	private int pointerHistoryLoc;
	private int gravityLoc;
	private int linearLoc;
	private int rotationLoc;
	private int magneticLoc;
	private int orientationLoc;
	private int lightLoc;
	private int pressureLoc;
	private int proximityLoc;
	private int offsetLoc;
	private int batteryLoc;
	private int dateTimeLoc;
	private int startRandomLoc;
	private int backBufferLoc;
	private int cameraOrientationLoc;
	private int cameraAddentLoc;

	private int flipYLoc;
	int gravityConLoc;
	int offsetRatioLoc;
	// Members
	private volatile int pointerCount;
	private volatile int pointerHistorySize;
	private volatile int pointerBufferNextIndex;
	private int frontTarget = 0;
	private int backTarget = 1;
	private volatile long startTime;
	private long pauseTime;
	private volatile long lastRender;
	private long lastBatteryUpdate;
	private long lastDateUpdate;
	private float batteryLevel;
	private volatile float quality = 1f;
	private float startRandom;
	private float fTimeMax;

	private volatile long nextFpsUpdate = 0;
	private volatile float sum;
	private volatile float samples;
	private volatile int lastFps;

	private volatile boolean needRefresh;
	/**
	 * Flip all texture2Ds from bitmap.
	 */
	private boolean needFlipY = true;

	/**
	 * Auto rotate x, y; Only active at beginning of wallpaper-preview.
	 */
	private FakeGravity fakeGravity;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

	ShaderRenderer(Context context, BaseWallpaperManager manager, String wallpaperName) {
		this.context = context;
		this.manager = manager;
		this.wallpaperName = wallpaperName;

		vertexBuffer = ByteBuffer.allocateDirect(8);
		vertexBuffer.put(new byte[]{
				-1, 1,
				-1, -1,
				1, 1,
				1, -1}).position(0);
		ByteBuffer cc = ByteBuffer.allocateDirect(sCoord.length * 4);
		cc.order(ByteOrder.nativeOrder());
		textureBuffer = cc.asFloatBuffer().put(sCoord);
		textureBuffer.position(0);

		if (GLES.supportsGLES30()) {
			particleRenderer = new ConfettiRendererGLES20(vertexBuffer, textureBinder, manager, wallpaperName);
		} else {
			particleRenderer = new ConfettiRendererGLES20(vertexBuffer, textureBinder, manager, wallpaperName);
		}
	}

	void setFragmentShader(String source, float quality) {
		setQuality(quality);
		setFragmentShader(source);
	}

	@MainThread
	void setFakeGravity(FakeGravity fakeGravity) {
		this.fakeGravity = fakeGravity;
	}

	@DebugLog
	private void setFragmentShader(String source) {
		fTimeMax = parseFTime(source);
		resetFps();
		fragmentShader = indexTextureNames(source);
	}

	private void setQuality(float quality) {
		this.quality = quality;
	}

	void setOnRendererListener(OnRendererListener listener) {
		onRendererListener = listener;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		HSLog.d("ShaderRenderer", "onSurfaceCreated");

		GLES20.glClearColor(1f, 1f, 1f, 1f);

		if (surfaceProgram != 0) {
			// Don't glDeleteProgram( surfaceProgram ) because
			// GLSurfaceView::onPause() destroys the GL context
			// what also deletes all programs.
			// With glDeleteProgram():
			// <core_glDeleteProgram:594>: GL_INVALID_VALUE
			surfaceProgram = 0;
		}

		if (program != 0) {
			// Don't glDeleteProgram( program );
			// same as above
			program = 0;
			deleteFrameBuffers();
		}

		initWallpaper();

		resetFps();
		indexSurfaceLocations();
		GLES20.glEnableVertexAttribArray(surfacePositionLoc);
		GLES20.glEnableVertexAttribArray(surfaceCoordinate);

		particleRenderer.onSurfaceCreated();
	}

	@MainThread
    synchronized void updateWallpaperName(String wallpaperName) {
		if (!TextUtils.equals(this.wallpaperName, wallpaperName)) {
			this.wallpaperName = wallpaperName;
			this.needRefresh = true;
		}
	}

	@RenderThread
	private void initWallpaper() {
		HSLog.d("Render", "initWallpaper");
		if (fragmentShader != null && fragmentShader.length() > 0) {
			createShaderTextures();
			loadPrograms();
			indexLocations();
			GLES20.glEnableVertexAttribArray(positionLoc);
			registerListeners();
            startRandomWaterDropIfNeeded();
		}

		particleRenderer.initWallpaper();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (surfaceResolution[0] != width || surfaceResolution[1] != height) {
            // Restart only when surface size actually changes
            // (at initialization time or on screen orientation change).
            startTime = lastRender = System.nanoTime();
            startRandom = (float) Math.random();

            surfaceResolution[0] = width;
            surfaceResolution[1] = height;
            if (accelerometerListener != null) {
				accelerometerListener.isOrientationLand = width > height;
			}

            HSLog.d("SUNDXING", "Surface size " + width + "," + height + "；quality = " + quality);

			Matrix.setLookAtM(mVMatrix, 0, 0, 0, GLParams.eyeZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
			Matrix.perspectiveM(mProjMatrix, 0,GLParams.fovy, GLParams.aspect, GLParams.near, GLParams.far);

			float w = Math.round(width * quality);
            float h = Math.round(height * quality);

            if (w != resolution[0] || h != resolution[1]) {
                deleteFrameBuffers();
            }

            resolution[0] = w;
            resolution[1] = h;
            particleRenderer.onSurfaceChanged(w, h);

            resetFps();
        }
	}

	@MainThread
	@DebugLog
	void onPause() {
		unregisterListeners();
        pauseTime = System.nanoTime();
        mainHandler.removeCallbacksAndMessages(null);
    }

	@MainThread
	@DebugLog
	void onResume() {
		// index default value is zero, all listeners will be registered.
		if (indexLocated) {
			registerListeners();
		}
        long pauseDuration = System.nanoTime() - pauseTime;
        startTime += pauseDuration; // Apply an offset on startTime to equivalently skip pause period
        particleRenderer.adjustStartTimeAfterPause(pauseDuration);
        startRandomWaterDropIfNeeded();
	}

	private void startRandomWaterDropIfNeeded() {
        if (pointerHistorySizeLoc > -1 || pointerHistoryLoc > -1) {
            mainHandler.removeCallbacksAndMessages(null);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MotionEvent e = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN,
                            (float) Math.random() * surfaceResolution[0],
                            (float) Math.random() * surfaceResolution[1], 0);
                    ShaderRenderer.this.updatePointerHistory(e);
                    e.recycle();
                    mainHandler.postDelayed(this, getRandomWaterDropTime());
                }
            }, getRandomWaterDropTime());
        }
    }

	private long getRandomWaterDropTime() {
        return (long) (Math.random() * 4000 + 1000);
    }

	@RenderThread
	private synchronized void updateLiveWallpaper() {
		if (needRefresh) {
			needRefresh = false;
			particleRenderer.updateWallpaper(wallpaperName);
			setFragmentShader(manager.getShader(wallpaperName));
			initWallpaper();
		}
	}
	float angle = 0;
	@Override
	public void onDrawFrame(GL10 gl) {
		updateLiveWallpaper();

		if (surfaceProgram == 0 || program == 0) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT |
					GLES20.GL_DEPTH_BUFFER_BIT);

			return;
		}

		final long now = System.nanoTime();

        // first draw custom shader in framebuffer
        drawToFrameBuffer(now);

        // then draw framebuffer on screen
        drawFrameBufferToSurface();

		swapFrameBuffers();

		if (onRendererListener != null) {
			updateFps(now);
		}
	}

    private void drawToFrameBuffer(long now) {
        ensureFrameBuffers(
                (int) resolution[0],
                (int) resolution[1]);

        GLES20.glBindFramebuffer(
                GLES20.GL_FRAMEBUFFER,
                fb[frontTarget]);

        GLES20.glViewport(
                0,
                0,
                (int) resolution[0],
                (int) resolution[1]);

        drawShaderEffects(now);
        particleRenderer.drawFrame(now, lastFps);
    }

    private void drawShaderEffects(long now) {
        GLES20.glUseProgram(program);

        GLES20.glVertexAttribPointer(
                positionLoc,
                2,
                GLES20.GL_BYTE,
                false,
                0,
                vertexBuffer);

        passUniformValues(now);

        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_STRIP,
                0,
                4);
    }

	private void passUniformValues(long now) {
		float delta = (now - startTime) / NS_PER_SECOND;

		if (timeLoc > -1) {
			GLES20.glUniform1f(
					timeLoc,
					delta);
		}

		if (densityRatioLoc > -1) {
			GLES20.glUniform1f(densityRatioLoc,
					Dimensions.getDensityRatio());
		}

		if (secondLoc > -1) {
			GLES20.glUniform1i(
					secondLoc,
					(int) delta);
		}

		if (subSecondLoc > -1) {
			GLES20.glUniform1f(
					subSecondLoc,
					delta - (int) delta);
		}

		if (fTimeLoc > -1) {
			GLES20.glUniform1f(
					fTimeLoc,
					((delta % fTimeMax) / fTimeMax * 2f - 1f));
		}

		if (resolutionLoc > -1) {
			GLES20.glUniform2fv(
					resolutionLoc,
					1,
					resolution,
					0);
		}

		if (touchLoc > -1) {
			GLES20.glUniform2fv(
					touchLoc,
					1,
					touch,
					0);
		}

		if (mouseLoc > -1) {
			GLES20.glUniform2fv(
					mouseLoc,
					1,
					mouse,
					0);
		}

		if (pointerCountLoc > -1) {
			GLES20.glUniform1i(
					pointerCountLoc,
					pointerCount);
		}

		if (pointersLoc > -1) {
			GLES20.glUniform3fv(
					pointersLoc,
					pointerCount,
					pointers,
					0);
		}

		if (pointerHistorySizeLoc > -1 || pointerHistoryLoc > -1) {
            synchronized (pointerRingBuffer) {
                if (pointerHistorySizeLoc > -1) {
                    GLES20.glUniform1i(
                            pointerHistorySizeLoc,
                            pointerHistorySize);
                }

                if (pointerHistoryLoc > -1) {
                    GLES20.glUniform3fv(
                            pointerHistoryLoc,
                            pointerHistorySize,
                            pointerRingBuffer,
                            0);
                }
            }
        }

		if (gravityLoc > -1 && accelerometerListener != null) {
			GLES20.glUniform3fv(
					gravityLoc,
					1,
					adjustGravity(accelerometerListener.gravity),
					0);
		}

		if (linearLoc > -1 && accelerometerListener != null) {
			GLES20.glUniform3fv(
					linearLoc,
					1,
					accelerometerListener.linear,
					0);
		}

		if (rotationLoc > -1 && gyroscopeListener != null) {
			GLES20.glUniform3fv(
					rotationLoc,
					1,
					gyroscopeListener.rotation,
					0);
		}

		if (magneticLoc > -1 && magneticFieldListener != null) {
			GLES20.glUniform3fv(
					magneticLoc,
					1,
					magneticFieldListener.values,
					0);
		}

		if (orientationLoc > -1 && accelerometerListener != null &&
				magneticFieldListener != null) {
			SensorManager.getRotationMatrix(
					rotationMatrix,
					null,
					accelerometerListener.gravity,
					magneticFieldListener.filtered);
			SensorManager.getOrientation(rotationMatrix, orientation);
			GLES20.glUniform3fv(
					orientationLoc,
					1,
					orientation,
					0);
		}

		if (lightLoc > -1 && lightListener != null) {
			GLES20.glUniform1f(
					lightLoc,
					lightListener.ambient);
		}

		if (pressureLoc > -1 && pressureListener != null) {
			GLES20.glUniform1f(
					pressureLoc,
					pressureListener.pressure);
		}

		if (proximityLoc > -1 && proximityListener != null) {
			GLES20.glUniform1f(
					proximityLoc,
					proximityListener.centimeters);
		}

		if (offsetLoc > -1) {
			GLES20.glUniform2fv(
					offsetLoc,
					1,
					offset,
					0);
		}

		if (flipYLoc > -1) {
			GLES20.glUniform1i(flipYLoc, needFlipY ? 1 : 0);
		}

		if (gravityConLoc > -1) {
			GLES20.glUniform4fv(
					gravityConLoc,
					1,
					GLParams.gravityConFactors,
					0);
		}

		if (offsetRatioLoc > -1) {
			GLES20.glUniform1f(offsetRatioLoc, GLParams.offsetRatio);
		}


		if (batteryLoc > -1) {
			if (now - lastBatteryUpdate > BATTERY_UPDATE_INTERVAL) {
				// profiled getBatteryLevel() on slow/old devices
				// and it can take up to 6ms, so better do that
				// not for every frame but only once in a while
				batteryLevel = getBatteryLevel();
				lastBatteryUpdate = now;
			}

			GLES20.glUniform1f(
					batteryLoc,
					batteryLevel);
		}

		if (dateTimeLoc > -1) {
			if (now - lastDateUpdate > DATE_UPDATE_INTERVAL) {
				Calendar calendar = Calendar.getInstance();
				dateTime[0] = calendar.get(Calendar.YEAR);
				dateTime[1] = calendar.get(Calendar.MONTH);
				dateTime[2] = calendar.get(Calendar.DAY_OF_MONTH);
				dateTime[3] = calendar.get(Calendar.HOUR_OF_DAY) * 3600f +
						calendar.get(Calendar.MINUTE) * 60f +
						calendar.get(Calendar.SECOND);

				lastDateUpdate = now;
			}

			GLES20.glUniform4fv(
					dateTimeLoc,
					1,
					dateTime,
					0);
		}

		if (startRandomLoc > -1) {
			GLES20.glUniform1f(
					startRandomLoc,
					startRandom);
		}

        textureBinder.reset();

        if (backBufferLoc > -1) {
            textureBinder.bind(
                    backBufferLoc,
                    GLES20.GL_TEXTURE_2D,
                    tx[backTarget]);
        }

        for (int i = 0; i < numberOfTextures; ++i) {
            textureBinder.bind(
                    textureLocs[i],
                    textureTargets[i],
                    textureIds[i],
                    textureSizeLocs[i],
                    textureSizes[i]);
        }

        if (cameraListener != null) {
            if (cameraOrientationLoc > -1) {
                GLES20.glUniformMatrix2fv(
                        cameraOrientationLoc,
                        1,
                        false,
                        cameraListener.getOrientationMatrix());
            }

            if (cameraAddentLoc > -1) {
                GLES20.glUniform2fv(
                        cameraAddentLoc,
                        1,
                        cameraListener.addent,
                        0);
            }

            cameraListener.update();
        }
	}

	private float[] adjustGravity(float[] gravity) {
		if (fakeGravity != null && fakeGravity.enable) {
			fakeGravity.adjust(gravity);
		}
		return gravity;
	}

	private void drawFrameBufferToSurface() {
		GLES20.glBindFramebuffer(
				GLES20.GL_FRAMEBUFFER,
				0);

		GLES20.glViewport(
				0,
				0,
				(int) surfaceResolution[0],
				(int) surfaceResolution[1]);

		GLES20.glUseProgram(surfaceProgram);

		GLES20.glVertexAttribPointer(
				surfacePositionLoc,
				2,
				GLES20.GL_BYTE,
				false,
				0,
				vertexBuffer);

		GLES20.glVertexAttribPointer(
				surfaceCoordinate,
				2,
				GLES20.GL_FLOAT,
				false,
				0,
				textureBuffer);

		GLES20.glUniform2fv(
				surfaceResolutionLoc,
				1,
				surfaceResolution,
				0);

		if (surfaceMatrixLoc > -1) {
			GLES20.glUniformMatrix4fv(surfaceMatrixLoc, 1, false, getMvpMatrix(), 0);
		}
		GLES20.glUniform1i(surfaceFrameLoc, 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(
				GLES20.GL_TEXTURE_2D,
				tx[frontTarget]);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);

		GLES20.glDrawArrays(
				GLES20.GL_TRIANGLE_STRIP,
				0,
				4);
	}

	private double degreeX;
	private double degreeY;
	private float[] getMvpMatrix() {
		Matrix.setIdentityM(mMMatrix, 0);
		if (gravityLoc > -1 && accelerometerListener != null) {
			float factor = GLParams.rotateFactor;
			degreeX = Math.asin(accelerometerListener.gravity[0] / 9.8f * factor) * 180f / Math.PI;
			degreeY = Math.asin(accelerometerListener.gravity[1] / 9.8f * factor) * 180f / Math.PI;
			float limit = GLParams.rotateMaxDegree;
			degreeX = Math.max(-limit, Math.min(degreeX, limit));
			degreeY = Math.max(-limit, Math.min(degreeY, limit));

			Matrix.rotateM(mMMatrix, 0, -(float) degreeX, 0, 1, 0);
			Matrix.rotateM(mMMatrix, 0, (float) degreeY, 1, 0, 0);
		}
		Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
		return mMVPMatrix;
	}

	private void swapFrameBuffers() {
		// swap buffers so the next image will be rendered
		// over the current backbuffer and the current image
		// will be the backbuffer for the next image
		int t = frontTarget;
		frontTarget = backTarget;
		backTarget = t;
	}

	void unregisterListeners() {
		if (accelerometerListener != null) {
			accelerometerListener.unregister();
		}

		if (gyroscopeListener != null) {
			gyroscopeListener.unregister();
		}

		if (magneticFieldListener != null) {
			magneticFieldListener.unregister();
		}

		if (lightListener != null) {
			lightListener.unregister();
		}

		if (pressureListener != null) {
			pressureListener.unregister();
		}

		if (proximityListener != null) {
			proximityListener.unregister();
		}

		if (cameraListener != null) {
			cameraListener.unregister();
		}
	}

	@MainThread
	void touchAt(MotionEvent e) {
		float x = e.getX() * quality;
		float y = e.getY() * quality;

		touch[0] = x;
		touch[1] = resolution[1] - y;

		// to be compatible with http://glslsandbox.com/
		mouse[0] = x / resolution[0];
		mouse[1] = 1 - y / resolution[1];

		particleRenderer.touchAt(e);

		switch (e.getActionMasked()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				pointerCount = 0;
				return;
		}

		pointerCount = Math.min(
				e.getPointerCount(),
				pointers.length / 3);

		for (int i = 0, offset = 0; i < pointerCount; ++i) {
			pointers[offset++] = e.getX(i) * quality;
			pointers[offset++] = resolution[1] - e.getY(i) * quality;
			pointers[offset++] = e.getTouchMajor(i);
		}

		updatePointerHistory(e);
	}

	private void updatePointerHistory(MotionEvent e) {
        synchronized (pointerRingBuffer) {
            float currentTime = (lastRender - startTime) / NS_PER_SECOND;
            if (pointerHistorySize == 0) {
                addPointerToHistoryLocked(e, currentTime);
            } else {
                int lastTimeIndex;
                if (pointerBufferNextIndex == 0) {
                    lastTimeIndex = pointerRingBuffer.length - 1;
                } else {
                    lastTimeIndex = pointerBufferNextIndex - 1;
                }
                float lastTime = pointerRingBuffer[lastTimeIndex];

                // Enforce a minimum interval between pointers for performance
                if (currentTime - lastTime > 0.0) {
                    addPointerToHistoryLocked(e, currentTime);
                }
            }
        }
    }

    private void addPointerToHistoryLocked(MotionEvent e, float timestamp) {
        if (pointerHistorySize < pointerRingBuffer.length / 3) {
            pointerHistorySize++;
        }
        pointerRingBuffer[pointerBufferNextIndex++] = e.getX() * quality;
        pointerRingBuffer[pointerBufferNextIndex++] = resolution[1] - e.getY() * quality;
        pointerRingBuffer[pointerBufferNextIndex++] = timestamp;
        if (pointerBufferNextIndex + 2 >= pointerRingBuffer.length) {
            pointerBufferNextIndex = 0;
        }
    }

	public void setOffset(float x, float y) {
		offset[0] = x;
		offset[1] = y;
	}

	private void resetFps() {
		sum = samples = 0;
		lastFps = 0;
		nextFpsUpdate = 0;
	}

	private void loadPrograms() {
                // 1. program to render frame buffer to surface
		if (((surfaceProgram = Program.loadProgram(
				TRIVIAL_VERTEX_SHADER_MATRIX,
				TRIVIAL_FRAGMENT_SHADER)) == 0 ||

                // 2. program to render shader generated contents to the frame buffer
				(program = Program.loadProgram(
						TRIVIAL_VERTEX_SHADER,
						fragmentShader)) == 0 ||

                // 3. program to render particles to the frame buffer
                !particleRenderer.loadProgram()) &&

				onRendererListener != null) {

            // If any of the three programs failed to load, log the error
			onRendererListener.onInfoLogs(Program.getInfoLogs());
		}
	}

	private void indexSurfaceLocations() {
		surfacePositionLoc = GLES20.glGetAttribLocation(
				surfaceProgram, "position");
		surfaceCoordinate = GLES20.glGetAttribLocation(
				surfaceProgram, "vCoordinate");
		surfaceResolutionLoc = GLES20.glGetUniformLocation(
				surfaceProgram, "resolution");
		surfaceFrameLoc = GLES20.glGetUniformLocation(
				surfaceProgram, "frame");

		surfaceMatrixLoc = GLES20.glGetUniformLocation(
				surfaceProgram, "mvpMatrix"
		);

	}

	private void indexLocations() {
		indexLocated = true;
		positionLoc = GLES20.glGetAttribLocation(
				program, "position");
		timeLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_TIME);
		densityRatioLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_DENSITY_RATIO);
		secondLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_SECOND);
		subSecondLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_SUBSECOND);
		fTimeLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_FTIME);
		resolutionLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_RESOLUTION);
		touchLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_TOUCH);
		mouseLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_MOUSE);
		pointerCountLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_POINTER_COUNT);
		pointersLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_POINTERS);
        pointerHistorySizeLoc = GLES20.glGetUniformLocation(
                program, ShaderConstants.UNIFORM_POINTER_HISTORY_SIZE);
        pointerHistoryLoc = GLES20.glGetUniformLocation(
                program, ShaderConstants.UNIFORM_POINTER_HISTORY);
		gravityLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_GRAVITY);
		linearLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_LINEAR);
		rotationLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_ROTATION);
		magneticLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_MAGNETIC);
		orientationLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_ORIENTATION);
		lightLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_LIGHT);
		pressureLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_PRESSURE);
		proximityLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_PROXYMITY);
		offsetLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_OFFSET);
		batteryLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_BATTERY);
		dateTimeLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_DATE);
		startRandomLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_START_RANDOM);
		backBufferLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_BACKBUFFER);
		cameraOrientationLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_CAMERA_ORIENTATION);
		cameraAddentLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_CAMERA_ADDENT);
		flipYLoc = GLES20.glGetUniformLocation(
				program, ShaderConstants.UNIFORM_FLIP_Y);

		// TODO remove test code
		gravityConLoc = GLES20.glGetUniformLocation(
				program, "sensitivity");
		offsetRatioLoc = GLES20.glGetUniformLocation(
				program, "offsetRatio");
		for (int i = numberOfTextures; i-- > 0; ) {
			String textureName = textureNames.get(i);
			textureLocs[i] = GLES20.glGetUniformLocation(
					program,
					textureName);
			textureSizeLocs[i] = GLES20.glGetUniformLocation(
					program,
					textureName + "Size");
		}
	}

	void registerListeners() {
		if (gravityLoc > -1 || linearLoc > -1 || orientationLoc > -1) {
			if (accelerometerListener == null) {
				accelerometerListener = new AccelerometerListener(context);
			}
			accelerometerListener.register();
		}

		if (rotationLoc > -1) {
			if (gyroscopeListener == null) {
				gyroscopeListener = new GyroscopeListener(context);
			}
			gyroscopeListener.register();
		}

		if (magneticLoc > -1 || orientationLoc > -1) {
			if (magneticFieldListener == null) {
				magneticFieldListener = new MagneticFieldListener(context);
			}
			magneticFieldListener.register();
		}

		if (lightLoc > -1) {
			if (lightListener == null) {
				lightListener = new LightListener(context);
			}
			lightListener.register();
		}

		if (pressureLoc > -1) {
			if (pressureListener == null) {
				pressureListener = new PressureListener(context);
			}
			pressureListener.register();
		}

		if (proximityLoc > -1) {
			if (proximityListener == null) {
				proximityListener = new ProximityListener(context);
			}
			proximityListener.register();
		}
	}

	private void updateFps(long now) {
		long delta = now - lastRender;

		// because sum and samples are volatile
		synchronized (this) {
			sum += Math.min(NS_PER_SECOND / delta, 60f);

			if (++samples > 0xffff) {
				sum = sum / samples;
				samples = 1;
			}
		}

		if (now > nextFpsUpdate) {
			int fps = Math.round(sum / samples);

			if (fps != lastFps) {
				onRendererListener.onFramesPerSecond(fps);
				lastFps = fps;
			}

			nextFpsUpdate = now + FPS_UPDATE_FREQUENCY_NS;
		}

		lastRender = now;
	}

	private void ensureFrameBuffers(int width, int height) {
        if (fb[0] == 0) {
            GLES20.glGenFramebuffers(2, fb, 0);
            GLES20.glGenTextures(2, tx, 0);

            createFrameBuffer(frontTarget, width, height, backBufferTextureParams);
            createFrameBuffer(backTarget, width, height, backBufferTextureParams);

            // unbind textures that were bound in createFrameBuffer()
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }
	}

    private void deleteFrameBuffers() {
        if (fb[0] == 0) {
            return;
        }

        GLES20.glDeleteFramebuffers(2, fb, 0);
        GLES20.glDeleteTextures(2, tx, 0);

        fb[0] = 0;
    }

	private void createFrameBuffer(
			int idx,
			int width,
			int height,
			TextureParameters tp) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tx[idx]);
		GLES20.glTexImage2D(
				GLES20.GL_TEXTURE_2D,
				0,
				GLES20.GL_RGBA,
				width,
				height,
				0,
				GLES20.GL_RGBA,
				GLES20.GL_UNSIGNED_BYTE,
				null);

		tp.set(GLES20.GL_TEXTURE_2D);
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

		GLES20.glBindFramebuffer(
				GLES20.GL_FRAMEBUFFER,
				fb[idx]);
		GLES20.glFramebufferTexture2D(
				GLES20.GL_FRAMEBUFFER,
				GLES20.GL_COLOR_ATTACHMENT0,
				GLES20.GL_TEXTURE_2D,
				tx[idx],
				0);

		// clear texture because some drivers
		// don't initialize texture memory
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT |
				GLES20.GL_DEPTH_BUFFER_BIT);
	}

	private void deleteTextures() {
		if (textureIds[0] == 1 || numberOfTextures < 1) {
			return;
		}
		GLES20.glDeleteTextures(numberOfTextures, textureIds, 0);
	}

	private void createShaderTextures() {
		deleteTextures();
		GLES20.glGenTextures(numberOfTextures, textureIds, 0);

		for (int i = 0; i < numberOfTextures; ++i) {
			String name = textureNames.get(i);

			if (CAMERA_BACK.equals(name) ||
					CAMERA_FRONT.equals(name)) {
				openCameraListener(name, textureIds[i],
						textureParameters.get(i));
				continue;
			}

			Bitmap bitmap = manager.getShaderTexture(wallpaperName, name);
			if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
				continue;
			}

			switch (textureTargets[i]) {
				default:
					continue;
				case GLES20.GL_TEXTURE_2D:
					textureSizes[i][0] = bitmap.getWidth();
					textureSizes[i][1] = bitmap.getHeight();
					createTexture(textureIds[i], bitmap,
							textureParameters.get(i));
					break;
			}

			bitmap.recycle();
		}
	}

    public static void createTexture(
            int id,
            Bitmap bitmap,
            TextureParameters tp) {
        createTexture(id, bitmap, tp, true);
    }

	public static void createTexture(
			int id,
			Bitmap bitmap,
			TextureParameters tp,
            boolean generateMipmap) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
		tp.set(GLES20.GL_TEXTURE_2D);

		// flip bitmap because 0/0 is bottom left in OpenGL
		// TODO flip texture.
//        final Matrix flipMatrix = new Matrix();
//        flipMatrix.postScale(1f, -1f);
//		Bitmap flippedBitmap = Bitmap.createBitmap(
//				bitmap,
//				0,
//				0,
//				bitmap.getWidth(),
//				bitmap.getHeight(),
//				flipMatrix,
//				true);
		GLUtils.texImage2D(
				GLES20.GL_TEXTURE_2D,
				0,
				GLES20.GL_RGBA,
				bitmap,
				GLES20.GL_UNSIGNED_BYTE,
				0);

        if (generateMipmap) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
	}

	private void openCameraListener(String name, int id, TextureParameters tp) {
		int cameraId = CameraListener.findCameraIdFacing(
				CAMERA_BACK.equals(name) ?
						Camera.CameraInfo.CAMERA_FACING_BACK :
						Camera.CameraInfo.CAMERA_FACING_FRONT);

		if (cameraId < 0) {
			return;
		}

		if (cameraListener == null ||
				cameraListener.cameraId != cameraId) {
			if (cameraListener != null) {
				cameraListener.unregister();
				cameraListener = null;
			}

			requestCameraPermission();
			setCameraTextureProperties(id, tp);
			cameraListener = new CameraListener(
					context,
					id,
					cameraId,
					(int) resolution[0],
					(int) resolution[1]);
		}

		cameraListener.register();
	}

	private void requestCameraPermission() {
		String permission = android.Manifest.permission.CAMERA;
		if (ContextCompat.checkSelfPermission(context,
				permission) != PackageManager.PERMISSION_GRANTED) {
			Activity activity;
			try {
				activity = (Activity) context;
			} catch (ClassCastException e) {
				return;
			}
			ActivityCompat.requestPermissions(
					activity,
					new String[]{permission},
					1);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	private static void setCameraTextureProperties(int id, TextureParameters tp) {
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, id);
		tp.set(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
	}

	private static float parseFTime(String source) {
		if (source != null) {
			Matcher m = PATTERN_FTIME.matcher(source);
			if (m.find() && m.groupCount() > 0) {
				return Float.parseFloat(m.group(1));
			}
		}
		return 3f;
	}

	private String indexTextureNames(String source) {
		if (source == null) {
			return null;
		}

		textureNames.clear();
		textureParameters.clear();
		numberOfTextures = 0;

		final int maxTextures = textureIds.length;

		for (Matcher m = PATTERN_SAMPLER.matcher(source);
				m.find() && numberOfTextures < maxTextures; ) {
			String type = m.group(1);
			String name = m.group(2);
			String params = m.group(3);

			if (type == null || name == null) {
				continue;
			}

			if (ShaderConstants.UNIFORM_BACKBUFFER.equals(name)) {
				backBufferTextureParams.parse(params);
				continue;
			}

			int target;

			switch (type) {
				case SAMPLER_2D:
					target = GLES20.GL_TEXTURE_2D;
					break;
				case SAMPLER_EXTERNAL_OES:
					// needs to be done here or lint won't recognize
					// we're checking SDK version
					target = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
					if (!source.contains(OES_EXTERNAL)) {
						source = OES_EXTERNAL + source;
					}
					break;
				default:
					continue;
			}

			textureTargets[numberOfTextures++] = target;
			textureNames.add(name);
			textureParameters.add(new TextureParameters(params));
		}

		return source;
	}

	private float getBatteryLevel() {
		Intent batteryStatus = context.registerReceiver(
				null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		if (batteryStatus == null) {
			return 0;
		}

		int level = batteryStatus.getIntExtra(
				BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(
				BatteryManager.EXTRA_SCALE, -1);

		return (float) level / scale;
	}

	public static class TextureBinder {
		private int index;

		public void reset() {
			index = 0;
		}

		public void bind(int loc, int target, int textureId) {
			bind(loc, target, textureId, -1, null);
		}

		@Thunk void bind(int loc, int target, int textureId, int textureSizeLoc, int[] textureSize) {
			if (loc < 0 || index >= TEXTURE_UNITS.length) {
				return;
			}

			GLES20.glUniform1i(loc, index);
			GLES20.glActiveTexture(TEXTURE_UNITS[index]);
			GLES20.glBindTexture(target, textureId);
			if (textureSizeLoc > -1) {
				GLES20.glUniform2i(textureSizeLoc, textureSize[0], textureSize[1]);
			}

			++index;
		}
	}
}

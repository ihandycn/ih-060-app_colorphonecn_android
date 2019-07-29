package com.honeycomb.colorphone.customize.livewallpaper.hardware;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class CameraListener {
	public final int cameraId;
	public final float addent[] = new float[]{0, 0};

	private final int cameraTextureId;

	private int frameWidth;
	private int frameHeight;
	private int frameOrientation;
	private boolean pausing = true;
	private boolean opening = false;
	private boolean available = false;
	private Camera camera;
	private SurfaceTexture surfaceTexture;
	private FloatBuffer orientationMatrix;

	public static int findCameraIdFacing(int facing) {
		for (int i = 0, l = Camera.getNumberOfCameras(); i < l; ++i) {
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == facing) {
				return i;
			}
		}
		return -1;
	}

	public CameraListener(
			Context context,
			int cameraTextureId,
			int cameraId,
			int width,
			int height) {
		this.cameraTextureId = cameraTextureId;
		this.cameraId = cameraId;
		frameOrientation = getCameraDisplayOrientation(context, cameraId);
		frameWidth = width;
		frameHeight = height;
		setOrientationAndFlip(frameOrientation);
	}

	public FloatBuffer getOrientationMatrix() {
		return orientationMatrix;
	}

	public void register() {
		if (!pausing) {
			return;
		}
		pausing = false;
		openCameraAsync();
	}

	public void unregister() {
		pausing = true;
		stopPreview();
	}

	public synchronized void update() {
		if (surfaceTexture != null && available) {
			surfaceTexture.updateTexImage();
			available = false;
		}
	}

	private void openCameraAsync() {
		if (pausing || opening) {
			return;
		}
		opening = true;

		new AsyncTask<Void, Void, Camera>() {
			@Override
			protected Camera doInBackground(Void... nothings) {
				stopPreview();
				try {
					return Camera.open(cameraId);
				} catch (RuntimeException e) {
					return null;
				}
			}

			@Override
			protected void onPostExecute(Camera camera) {
				opening = false;
				startPreview(camera);
			}
		}.execute();
	}

	private void stopPreview() {
		if (camera == null) {
			return;
		}

		camera.stopPreview();
		camera.release();
		camera = null;

		surfaceTexture.release();
		surfaceTexture = null;
	}

	private void startPreview(Camera camera) {
		if (pausing || camera == null) {
			return;
		}

		camera.setDisplayOrientation(frameOrientation);

		Camera.Parameters parameters = camera.getParameters();
		parameters.setRotation(frameOrientation);
		setPreviewSize(parameters);
		setFastestFps(parameters);
		setFocusMode(parameters);
		camera.setParameters(parameters);

		surfaceTexture = new SurfaceTexture(cameraTextureId);
		surfaceTexture.setOnFrameAvailableListener(
				new SurfaceTexture.OnFrameAvailableListener() {
			@Override
			public void onFrameAvailable(SurfaceTexture st) {
				synchronized (CameraListener.this) {
					available = true;
				}
			}
		});

		try {
			camera.setPreviewTexture(surfaceTexture);
		} catch (IOException e) {
			return;
		}

		this.camera = camera;
		camera.startPreview();
	}

	private static int getCameraDisplayOrientation(
			Context context,
			int cameraId) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		return (info.orientation - getDeviceRotation(context) + 360) % 360;
	}

	private static int getDeviceRotation(Context context) {
		switch (((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay()
				.getRotation()) {
			default:
			case Surface.ROTATION_0:
				return 0;
			case Surface.ROTATION_90:
				return 90;
			case Surface.ROTATION_180:
				return 180;
			case Surface.ROTATION_270:
				return 270;
		}
	}

	private void setOrientationAndFlip(int orientation) {
		switch (orientation) {
		default:
		case 0:
			orientationMatrix = FloatBuffer.wrap(new float[] {
				1f, 0f,
				0f, -1f,
			});
			addent[0] = 0f;
			addent[1] = 1f;
			break;
		case 90:
			orientationMatrix = FloatBuffer.wrap(new float[] {
				0f, -1f,
				-1f, 0f,
			});
			addent[0] = 1f;
			addent[1] = 1f;
			break;
		case 180:
			orientationMatrix = FloatBuffer.wrap(new float[] {
				-1f, 0f,
				0f, 1f,
			});
			addent[0] = 1f;
			addent[1] = 0f;
			break;
		case 270:
			orientationMatrix = FloatBuffer.wrap(new float[] {
				0f, 1f,
				1f, 0f,
			});
			addent[0] = 0f;
			addent[1] = 0f;
			break;
		}
	}

	private void setPreviewSize(Camera.Parameters parameters) {
		List<Camera.Size> supportedPreviewSizes =
				parameters.getSupportedPreviewSizes();

		if (supportedPreviewSizes != null) {
			Camera.Size size = getOptimalSize(
					supportedPreviewSizes,
					frameWidth,
					frameHeight,
					frameOrientation);

			if (size != null) {
				frameWidth = size.width;
				frameHeight = size.height;
			}
		}

		parameters.setPreviewSize(frameWidth, frameHeight);
	}

	private static Camera.Size getOptimalSize(
			List<Camera.Size> sizes,
			int width,
			int height,
			int orientation) {
		if (sizes == null) {
			return null;
		}

		switch (orientation) {
			default:
				break;
			case 90:
			case 270:
				// swap dimensions to match orientation
				// of preview sizes
				int tmp = width;
				width = height;
				height = tmp;
				break;
		}

		double targetRatio = (double) width / height;
		double minDiff = Double.MAX_VALUE;
		double minDiffAspect = Double.MAX_VALUE;
		Camera.Size optimalSize = null;
		Camera.Size optimalSizeAspect = null;

		for (Camera.Size size : sizes) {
			double diff = Math.abs(size.height - height) +
					Math.abs(size.width - width);

			if (diff < minDiff) {
				optimalSize = size;
				minDiff = diff;
			}

			double ratio = (double) size.width / size.height;

			if (Math.abs(ratio - targetRatio) < 0.1 &&
					diff < minDiffAspect) {
				optimalSizeAspect = size;
				minDiffAspect = diff;
			}
		}

		return optimalSizeAspect != null ? optimalSizeAspect : optimalSize;
	}

	private static void setFastestFps(Camera.Parameters parameters) {
		try {
			int range[] = findFastestFpsRange(
					parameters.getSupportedPreviewFpsRange());

			if (range[0] > 0) {
				parameters.setPreviewFpsRange(range[0], range[1]);
			}
		} catch (RuntimeException e) {
			// silently ignore that exception;
			// if the fps range can't be increased,
			// there's nothing to do
		}
	}

	private static int[] findFastestFpsRange(List<int[]> ranges) {
		int fastest[] = new int[]{0, 0};

		for (int n = ranges.size(); n-- > 0;) {
			int range[] = ranges.get(n);

			if (range[0] >= fastest[0] && range[1] > fastest[1]) {
				fastest = range;
			}
		}

		return fastest;
	}

	private static void setFocusMode(Camera.Parameters parameters) {
		// best for taking pictures
		String continuousPicture =
				Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
		// less aggressive than CONTINUOUS_PICTURE
		String continuousVideo =
				Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
		// last resort
		String autoFocus = Camera.Parameters.FOCUS_MODE_AUTO;

		// prefer feature detection instead of checking BUILD.VERSION
		List<String> focusModes = parameters.getSupportedFocusModes();

		if (focusModes.contains(continuousPicture)) {
			parameters.setFocusMode(continuousPicture);
		} else if (focusModes.contains(continuousVideo)) {
			parameters.setFocusMode(continuousVideo);
		} else if (focusModes.contains(autoFocus)) {
			parameters.setFocusMode(autoFocus);
		}
	}
}

package com.honeycomb.colorphone.wallpaper.livewallpaper;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.FakeGravity;


public class ShaderView extends GLSurfaceView {

	private ShaderRenderer mRenderer;

	public ShaderView(Context context, int renderMode, BaseWallpaperManager manager, String wallpaperName) {
		super(context);
		init(context, renderMode, manager, wallpaperName);
	}

	@Override
	public void onPause() {
		super.onPause();
		mRenderer.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mRenderer.onResume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mRenderer.touchAt(event);
		return true;
	}

	public void setFragmentShader(String src, float quality) {
		onPause();
		mRenderer.setFragmentShader(src, quality);
		onResume();
	}

	public void setFakeGravity(FakeGravity fakeGravity) {
		if (mRenderer != null) {
			mRenderer.setFakeGravity(fakeGravity);
		}
	}

	public ShaderRenderer getRenderer() {
		return mRenderer;
	}

	private void init(Context context, int renderMode, BaseWallpaperManager manager, String wallpaperName) {
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);
		setRenderer(mRenderer = new ShaderRenderer(context, manager, wallpaperName));
		setRenderMode(renderMode);
	}
}

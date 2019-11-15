package com.honeycomb.colorphone.wallpaper.livewallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.FakeGravity;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.GuideHelper;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.RotationMaker;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.WallPaper3dGuide;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.WallPaperTouchGuide;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.WallpaperSetGuide;
import com.honeycomb.colorphone.wallpaper.livewallpaper.guide.WallpaperTestWindow;
import com.honeycomb.colorphone.wallpaper.livewallpaper.particleflow.ParticlesSurfaceView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.util.Threads;

import java.util.List;

import hugo.weaving.DebugLog;

public abstract class BaseWallpaperService extends WallpaperService {

    protected static final String TAG = BaseWallpaperService.class.getSimpleName();

    /**
     * Broadcast with this action is sent when a live wallpaper is applied successfully
     * (inferred from {@link Activity#onActivityResult(int, int, Intent)} call).
     */
    public static final String LIVE_WALLPAPER_APPLIED = "com.acb.live.wallpaper.applied";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean WAIT_FOR_DEBUGGER = false && BuildConfig.DEBUG;

    protected abstract BaseWallpaperManager getManager();

    private Handler mMainHandler;

    @Override
    public Engine onCreateEngine() {
        HSLog.d(TAG, "onCreateEngine");
        if (LiveWallpaperConsts.DEBUG_PARTICLE_FLOW_WALLPAPER) {
            return new ParticleFlowWallpaperEngine();
        } else {
            BaseWallpaperManager manager = getManager();
            String wallpaperName = getWallpaperName();

            switch (manager.getType(wallpaperName)) {
                case LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI:
                    return new ShaderWallpaperEngine(manager, wallpaperName);
                case LiveWallpaperConsts.TYPE_VIDEO:
                    return new VideoWallpaperEngine(manager, wallpaperName);
                case LiveWallpaperConsts.TYPE_PARTICLE_FLOW:
                    return new ParticleFlowWallpaperEngine();
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        if (WAIT_FOR_DEBUGGER) {
            Debug.waitForDebugger();
        }

        super.onCreate();

        mMainHandler = new Handler(getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (WAIT_FOR_DEBUGGER) {
            Debug.waitForDebugger();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private abstract class WallpaperEngine extends Engine {
        protected String wallpaperName;

        private boolean registeredBroadcasts = false;

        private BroadcastListener applyReceiver = new BroadcastListener() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String name = getWallpaperName();
                onLiveWallpaperApplied(name);
                checkApplySuccess(wallpaperName);
            }
        };

        protected abstract void onLiveWallpaperApplied(String newWallpaper);

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            boolean isPreview = isPreview();
            HSLog.d(TAG + ".Switch", "Create engine, isPreview: " + isPreview);
            if (!isPreview && wallpaperName != null) {
                checkApplySuccess(wallpaperName);
            }

            if (!isPreview && !registeredBroadcasts) {
                BroadcastCenter.register(BaseWallpaperService.this, applyReceiver,
                        new IntentFilter(LIVE_WALLPAPER_APPLIED));
                registeredBroadcasts = true;
            }
        }

        protected void showGuideWindow() {
            WallpaperSetGuide.show(getApplicationContext(), GuideHelper.TYPE_NORMAL);
            GuideHelper.logWallpaperPreview(GuideHelper.TYPE_NORMAL);
        }

        protected void removeGuideWindow() {
            WallpaperSetGuide.hide(GuideHelper.TYPE_NORMAL);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (registeredBroadcasts) {
                BroadcastCenter.unregister(BaseWallpaperService.this, applyReceiver);
                registeredBroadcasts = false;
            }
        }
    }

    private void checkApplySuccess(String wallpaperName) {
        int type = GuideHelper.getWallpaperType(getManager(), wallpaperName);
        WallpaperSetGuide.hideOnApplySuccess(type);
        if (type == GuideHelper.TYPE_3D) {
            WallPaper3dGuide.hideOnApplySuccess();
        }
    }

    private class ShaderWallpaperEngine extends WallpaperEngine
            implements ShaderRenderer.OnRendererListener {

        private ShaderWallpaperView view;
        private BaseWallpaperManager manager;
        private SurfaceHolder mSurfaceHolder;
        private INotificationObserver mINotificationObserver = new INotificationObserver() {

            boolean touched = false;

            @Override
            public void onReceive(String s, HSBundle hsBundle) {

                MotionEvent event;
                long now = SystemClock.uptimeMillis();
                if (hsBundle != null) {
                    float targetX = hsBundle.getFloat("x");
                    float targetY = hsBundle.getFloat("y");
                    event = MotionEvent.obtain(now, now, touched ? MotionEvent.ACTION_MOVE : MotionEvent.ACTION_DOWN,
                            targetX, targetY, 0);
                    touched = true;
                } else {
                    event = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL,
                            0, 0, 0);
                }

                handleTouch(event, false);

            }
        };

        private boolean firstFrame = true;
        private int guideWindowType = -1;

        @DebugLog
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mSurfaceHolder = holder;

            view = new ShaderWallpaperView(manager, wallpaperName);

            view.getRenderer().setOnRendererListener(this);
            setShader(wallpaperName);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            removeGuideWindow();
            if (view != null) {
                view.destroy();
                view = null;
            }
        }

        @Override
        protected void onLiveWallpaperApplied(String newWallpaper) {
            if (view != null) {
                wallpaperName = newWallpaper;
                view.getRenderer().updateWallpaperName(wallpaperName);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            boolean isCreating = mSurfaceHolder.isCreating();
            HSLog.d(TAG, "onVisibilityChanged visible " + visible + "， isCreating " + isCreating);

            if (view != null && !isCreating) {
                if (visible) {
                    view.onResume();
                } else {
                    view.onPause();
                    removeGuideWindow();
                }
            }
        }

        @Override
        public void onTouchEvent(MotionEvent e) {
            super.onTouchEvent(e);
            handleTouch(e, true);
        }

        /**
         * handle touch
         *
         * @param e
         * @param systemEvent If false, event may be made by ourself, Not system.
         */
        private void handleTouch(MotionEvent e, boolean systemEvent) {
            if (view != null) {
                view.getRenderer().touchAt(e);
                if (systemEvent && e.getAction() == MotionEvent.ACTION_DOWN) {
                    HSGlobalNotificationCenter.removeObserver(mINotificationObserver);
                    WallPaperTouchGuide.cancel();
                    GuideHelper.logLiveGestureCancel();
                }
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep,
                                     int xPixels, int yPixels) {
            if (view != null) {
                view.getRenderer().setOffset(isPreview() ? 0.5f : xOffset, yOffset);
            }
        }

        private ShaderWallpaperEngine(BaseWallpaperManager manager, String wallpaperName) {
            super();
            this.manager = manager;
            this.wallpaperName = wallpaperName;
            setTouchEventsEnabled(true);
        }

        @DebugLog
        private void setShader(String wallpaperName) {
            if (view != null) {
                view.setFragmentShader(
                        manager.getShader(wallpaperName),
                        1f);
            }
        }

        @Override
        public void onInfoLogs(List<String> infoLogs) {
            for (String infoLog : infoLogs) {
                HSLog.w(TAG, "Info log: " + infoLog);
            }
        }

        @Override
        public void onFramesPerSecond(int fps) {
            // Called in render thread.
            if (firstFrame) {
                firstFrame = false;
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        showGuideWindow();
                    }
                });
            }
            HSLog.d(TAG, "FPS: " + fps);
        }

        @Override
        protected void showGuideWindow() {
            if (!isPreview()) {
                HSLog.d(TAG, "Not in preview, not guide");
                return;
            }

            if (guideWindowType > 0) {
                removeGuideWindow();
            }

            guideWindowType = GuideHelper.getWallpaperType(getManager(), wallpaperName);

            if (guideWindowType == GuideHelper.TYPE_LIVE_TOUCH) {
                HSGlobalNotificationCenter.addObserver(WallPaperTouchGuide.EVENT_POINT, mINotificationObserver);
                WallPaperTouchGuide.show(getApplicationContext());
                GuideHelper.logLiveGestureCancelStartOrEnd(true);
                GuideHelper.logWallpaperPreview(guideWindowType);

            } else if (guideWindowType == GuideHelper.TYPE_3D) {
                boolean needGuide3d = !getManager().is3D(getLastUsedWallpaper());
                FakeGravity fakeGravity = new FakeGravity();
                RotationMaker rotationMaker = new RotationMaker();
                rotationMaker.addCallback(fakeGravity);
//                WallpaperTestWindow.show(getApplicationContext(), 0);

                if (needGuide3d) {
                    // WallPaper3dGuide will take over control of  RotationMaker. We should not start it.
                    WallPaper3dGuide.show(getApplicationContext(), rotationMaker);
                } else {
                    rotationMaker.start();
                }
                if (view != null) {
                    view.setFakeGravity(fakeGravity);
                }
                GuideHelper.logWallpaperPreview(guideWindowType);
            } else {
                super.showGuideWindow();
            }
        }

        @Override
        protected void removeGuideWindow() {
            if (guideWindowType == GuideHelper.TYPE_LIVE_TOUCH) {
                HSGlobalNotificationCenter.removeObserver(mINotificationObserver);
                WallPaperTouchGuide.hide();
                GuideHelper.logLiveGestureCancelStartOrEnd(false);

            } else if (guideWindowType == GuideHelper.TYPE_3D) {
                if (view != null) {
                    view.setFakeGravity(null);
                }
                WallPaper3dGuide.hide();
                WallpaperTestWindow.hide(0);
            }
            WallpaperSetGuide.hide(guideWindowType);

        }

        private class ShaderWallpaperView extends ShaderView {
            public ShaderWallpaperView(BaseWallpaperManager manager, String wallpaperName) {
                super(BaseWallpaperService.this, GLSurfaceView.RENDERMODE_CONTINUOUSLY, manager, wallpaperName);
            }

            @Override
            public final SurfaceHolder getHolder() {
                return ShaderWallpaperEngine.this.getSurfaceHolder();
            }

            public void destroy() {
                super.onDetachedFromWindow();
            }
        }
    }

    private class VideoWallpaperEngine extends WallpaperEngine {
        private BaseWallpaperManager manager;

        private SurfaceHolder surfaceHolder;
        private MediaPlayer mediaPlayer;
        private int times = 0;

        VideoWallpaperEngine(BaseWallpaperManager manager, String wallpaperName) {
            this.manager = manager;
            this.wallpaperName = wallpaperName;
        }

        @Override
        protected void onLiveWallpaperApplied(String newWallpaper) {
            wallpaperName = newWallpaper;
            if (surfaceHolder != null) {
                setupMediaPlayer(surfaceHolder);
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            times = 0;
            surfaceHolder = holder;
            setupMediaPlayer(holder);

        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            if (isPreview()) {
                removeGuideWindow();
            }
        }

        private void setupMediaPlayer(SurfaceHolder holder) {
            if (!holder.getSurface().isValid()){
                return;
            }
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setSurface(holder.getSurface());
            try {
                mediaPlayer.setDataSource(getApplicationContext(), manager.getVideo(wallpaperName));
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0, 0);
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                mp.release();
                if (times < 3 && surfaceHolder != null) {
                    if (times >= 2) {
                        mMainHandler.postDelayed(() -> {
                            if (surfaceHolder != null) {
                                setupMediaPlayer(surfaceHolder);
                            }
                        }, 500);
                    } else {
                        setupMediaPlayer(surfaceHolder);
                    }
                }
                times++;
                return false;
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    mp.start();
                    if (isPreview()) {
                        showGuideWindow();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) ->
                    HSLog.d("VideoLiveWallpaperSize", width + " x " + height));
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (mediaPlayer != null) {
                try {
                    if (visible) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.pause();
                        if (surfaceHolder != null && !surfaceHolder.isCreating()) {
                            removeGuideWindow();
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ParticleFlowWallpaperEngine extends Engine {
        private WPSurfaceView mGLView;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mGLView = new WPSurfaceView(BaseWallpaperService.this);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mGLView.resetAttractionPoints();
                mGLView.onResume();
            } else {
                mGLView.onPause();
            }
        }

        @Override
        public void onDestroy() {
            mGLView.onPause();
            super.onDestroy();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            mGLView.onTouchEvent(event);
        }

        // Create a simple subclass of ParticlesSurfaceView and override getHolder in order to
        // draw on the correct surface.
        class WPSurfaceView extends ParticlesSurfaceView {
            public WPSurfaceView(Context context) {
                super(context, null);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }
        }
    }

    private String getWallpaperName() {
        HSPreferenceHelper prefs = HSPreferenceHelper.getDefault();
        boolean isPreviewMode = prefs.getBoolean(LiveWallpaperConsts.PREF_KEY_IS_PREVIEW_MODE, false);
        return prefs.getString(isPreviewMode ? LiveWallpaperConsts.PREF_KEY_PREVIEW_WALLPAPER_NAME : LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, "");
    }

    private String getLastUsedWallpaper() {
        return HSPreferenceHelper.getDefault().getString(LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, "");
    }
}

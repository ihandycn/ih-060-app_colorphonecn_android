package com.honeycomb.colorphone.customize.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewDebug;
import android.view.ViewGroup;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

public class TextureVideoView extends TextureView implements TextureView.SurfaceTextureListener, MediaPlayer
        .OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, Handler.Callback, INotificationObserver {

    private static final String TAG = TextureVideoView.class.getSimpleName();
    private static final HandlerThread sHandlerThread = new HandlerThread("TextureVideoView");

    private static final int PLAY_STATE_ERROR = -1;
    private static final int PLAY_STATE_INIT = 0;
    private static final int PLAY_STATE_PREPARE = 1;
    private static final int PLAY_STATE_READY = 2;
    private static final int PLAY_STATE_PLAYING = 3;
    private static final int PLAY_STATE_PAUSE = 4;
    private static final int PLAY_STATE_COMPLETE = 5;
    private static final int PLAY_STATE_STOP = 6;
    private static final int PLAY_STATE_SEEK = 7;

    private Uri mVideoUri;
    @ViewDebug.ExportedProperty
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;
    private Handler mUiThreadHandler;
    private Handler mWorkThreadHandler;
    private ScaleType mScaleType;
    private boolean mIsLoop;
    @ViewDebug.ExportedProperty
    private volatile int mCurrentPlayState = PLAY_STATE_INIT;
    @ViewDebug.ExportedProperty
    private volatile int mTargetPlayState;
    private boolean mGainAudioFocus = false;
    private boolean mIsSurfaceActive;
    private volatile int mStartPosition = -1;
    private volatile long mCallSeekTime = -1;

    private PlayListener mPlayListener;

    private OnSurfaceAvailableListener mOnSurfaceAvailableListener;

    public enum ScaleType {
        NONE,
        FIT_XY,
        FIT_START,
        FIT_CENTER,
        FIT_END,
        LEFT_TOP,
        LEFT_CENTER,
        LEFT_BOTTOM,
        CENTER_TOP,
        CENTER,
        CENTER_BOTTOM,
        RIGHT_TOP,
        RIGHT_CENTER,
        RIGHT_BOTTOM,
        LEFT_TOP_CROP,
        LEFT_CENTER_CROP,
        LEFT_BOTTOM_CROP,
        CENTER_TOP_CROP,
        CENTER_CROP,
        CENTER_BOTTOM_CROP,
        RIGHT_TOP_CROP,
        RIGHT_CENTER_CROP,
        RIGHT_BOTTOM_CROP,
        START_INSIDE,
        CENTER_INSIDE,
        END_INSIDE,
    }

    private enum Gravity {
        LEFT_TOP,
        LEFT_CENTER,
        LEFT_BOTTOM,
        CENTER_TOP,
        CENTER,
        CENTER_BOTTOM,
        RIGHT_TOP,
        RIGHT_CENTER,
        RIGHT_BOTTOM,
    }

    public interface PlayListener {
        void onInfo(int what, int extra);

        void onError(int what, int extra);

        void onCompletion();

        void onSurfaceDestroyed();
    }

    static {
        sHandlerThread.start();
    }

    private boolean mMute;

    public TextureVideoView(Context context) {
        this(context, null);
    }

    public TextureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.TextureVideoView, 0, 0);
            if (obtainStyledAttributes != null) {
                int scaleType = obtainStyledAttributes.getInt(R.styleable.TextureVideoView_videoScaleType, ScaleType.NONE.ordinal());
                obtainStyledAttributes.recycle();
                mScaleType = ScaleType.values()[scaleType];
            }
        }
        init();
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        // do noting, to fix http://crashes.to/s/07d278563fb
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        HSLog.d(TAG, "onWindowVisibilityChanged: " + visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            pause();
        } else if (mCurrentPlayState == PLAY_STATE_PAUSE && visibility == VISIBLE) {
            resumePlayback();
        }
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        HSLog.d(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        HSLog.d(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        HSGlobalNotificationCenter.removeObserver(this);
        stop();
        synchronized (this) {
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
                mIsSurfaceActive = false;
                this.notifyAll();
            }
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        stop();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    public void setGainAudioFocus(boolean gainAudioFocus) {
        mGainAudioFocus = gainAudioFocus;
    }

    private void init() {
        setSurfaceTextureListener(this);
        mUiThreadHandler = new Handler();
        mWorkThreadHandler = new Handler(sHandlerThread.getLooper(), this);
    }

    private void configTransform(int width, int height) {
        if (width != 0 && height != 0) {
            Matrix matrix = new MatrixManager(new Size(getWidth(), getHeight()), new Size(width, height)).create(mScaleType);
            if (matrix != null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    setTransform(matrix);
                } else {
                    mUiThreadHandler.postAtFrontOfQueue(() -> setTransform(matrix));
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        HSLog.d(TAG, "onSurfaceTextureAvailable");
        synchronized (this) {
            if (mSurface != null && mSurfaceTexture != null) {
                setSurfaceTexture(mSurfaceTexture);
            } else {
                mSurface = new Surface(surface);
                mSurfaceTexture = surface;
            }
            mIsSurfaceActive = true;
            this.notifyAll();
        }
        if (mTargetPlayState == PLAY_STATE_PLAYING) {
            play();
        } else if (mCurrentPlayState == PLAY_STATE_PAUSE) {
            resumePlayback();
        }
        if (mOnSurfaceAvailableListener != null) {
            mOnSurfaceAvailableListener.onSurfaceAvailable();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        HSLog.d(TAG, "onSurfaceTextureDestroyed");
        if (mPlayListener != null) {
            mPlayListener.onSurfaceDestroyed();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        configTransform(width, height);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        HSLog.d(TAG, "onPrepared");
        if (mTargetPlayState == PLAY_STATE_PREPARE && mCurrentPlayState == PLAY_STATE_PREPARE) {
            mCurrentPlayState = PLAY_STATE_READY;
            if (hasPreparedMediaPlayer()) {
                HSLog.d(TAG, "real start");
                mMediaPlayer.start();
                mCurrentPlayState = PLAY_STATE_PLAYING;
                mTargetPlayState = PLAY_STATE_PLAYING;
            }
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        HSLog.d(TAG, "onInfo what: " + what + " extra: " + extra);
        if (mPlayListener != null) {
            Threads.postOnMainThread(() -> {
                if (mPlayListener != null) {
                    mPlayListener.onInfo(what, extra);
                }
            });
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        HSLog.d(TAG, "onCompletion");
        mCurrentPlayState = PLAY_STATE_COMPLETE;
        mTargetPlayState = PLAY_STATE_COMPLETE;
        if (mPlayListener != null) {
            Threads.postOnMainThread(() -> {
                if (mPlayListener != null) {
                    mPlayListener.onCompletion();
                }
            });
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        HSLog.d(TAG, "onError what: " + what + " extra: " + extra);
        mCurrentPlayState = PLAY_STATE_ERROR;
        mTargetPlayState = PLAY_STATE_ERROR;
        if (mPlayListener != null) {
            Threads.postOnMainThread(() -> {
                if (mPlayListener != null) {
                    mPlayListener.onError(what, extra);
                }
            });
        }
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        synchronized (TextureVideoView.class) {
            switch (msg.what) {
                case PLAY_STATE_PREPARE:
                    HSLog.d(TAG, "handleMessage: PLAY_STATE_PREPARE");
                    doPlay();
                    break;
                case PLAY_STATE_PAUSE:
                    HSLog.d(TAG, "handleMessage: PLAY_STATE_PAUSE");
                    if (mMediaPlayer != null) {
                        mMediaPlayer.pause();
                        mCurrentPlayState = PLAY_STATE_PAUSE;
                    }
                    break;
                case PLAY_STATE_PLAYING:
                    HSLog.d(TAG, "handleMessage: PLAY_STATE_PLAYING");
                    if (mMediaPlayer != null && mCurrentPlayState != PLAY_STATE_PLAYING) {
                        mMediaPlayer.start();
                        mCurrentPlayState = PLAY_STATE_PLAYING;
                    }
                    break;
                case PLAY_STATE_STOP:
                    HSLog.d(TAG, "handleMessage: PLAY_STATE_STOP");
                    release();
                    break;
                case PLAY_STATE_SEEK:
                    if (hasPreparedMediaPlayer() && mStartPosition != -1) {
                        long currentTimeMillis = System.currentTimeMillis();
                        int pass = (int) (currentTimeMillis - mCallSeekTime);
                        mMediaPlayer.seekTo(mStartPosition + pass);
                    }
                    break;
            }
        }
        return true;
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        configTransform(getVideoWidth(), getVideoHeight());
    }

    public void setVideoPath(String str) {
        setVideoURI(Uri.parse(str));
    }

    public void setVideoURI(Uri uri) {
        mVideoUri = uri;
    }

    public void setPlayListener(PlayListener listener) {
        mPlayListener = listener;
    }

    public void setOnSurfaceAvailableListener(OnSurfaceAvailableListener listener) {
        mOnSurfaceAvailableListener = listener;
    }

    public void play() {
        HSLog.d(TAG, "play");
        mTargetPlayState = PLAY_STATE_PLAYING;
        if (hasPreparedMediaPlayer()) {
            mWorkThreadHandler.obtainMessage(PLAY_STATE_STOP).sendToTarget();
        }
        if (mVideoUri != null && mSurface != null) {
            mWorkThreadHandler.obtainMessage(PLAY_STATE_PREPARE).sendToTarget();
        }
    }

    public void seekTo(int msec) {
        if (isPlaying()) {
            mStartPosition = msec;
            mCallSeekTime = System.currentTimeMillis();
            mWorkThreadHandler.obtainMessage(PLAY_STATE_SEEK).sendToTarget();
        }
    }

    public void stop() {
        HSLog.d(TAG, "stop");
        mTargetPlayState = PLAY_STATE_COMPLETE;
        if (hasPreparedMediaPlayer()) {
            mWorkThreadHandler.obtainMessage(PLAY_STATE_STOP).sendToTarget();
        }
    }

    private void pause() {
        HSLog.d(TAG, "pause");
        mTargetPlayState = PLAY_STATE_PAUSE;
        if (hasPreparedMediaPlayer()) {
            mWorkThreadHandler.obtainMessage(PLAY_STATE_PAUSE).sendToTarget();
        }
    }

    private void resumePlayback() {
        HSLog.d(TAG, "resumePlayback");
        mTargetPlayState = PLAY_STATE_PLAYING;
        if (hasPreparedMediaPlayer()) {
            mWorkThreadHandler.obtainMessage(PLAY_STATE_PLAYING).sendToTarget();
        }
    }

    public void mute() {
        mMute = true;
        try {
            MediaPlayer mediaPlayer = mMediaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0.0f, 0.0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resumeVolume() {
        mMute = false;
        try {
            MediaPlayer mediaPlayer = mMediaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1f, 1f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMute() {
        return mMute;
    }

    private void release() {
        HSLog.d(TAG, "release");
        mStartPosition = -1;
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentPlayState = PLAY_STATE_INIT;
        }
    }

    private void doPlay() {
        if (mVideoUri != null && mSurface != null && mTargetPlayState == PLAY_STATE_PLAYING) {
            if (mGainAudioFocus) {
                mAudioManager = (AudioManager) HSApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            release();
            awaitSurfaceActive();
            try {
                mMediaPlayer = new MediaPlayer();
                if (mMute) {
                    mMediaPlayer.setVolume(0, 0);
                }
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnInfoListener(this);
                mMediaPlayer.setOnVideoSizeChangedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setDataSource(HSApplication.getContext(), mVideoUri);
                mMediaPlayer.setSurface(mSurface);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setLooping(mIsLoop);
                mMediaPlayer.prepareAsync();
                mCurrentPlayState = PLAY_STATE_PREPARE;
                mTargetPlayState = PLAY_STATE_PREPARE;
            } catch (Throwable e2) {
                mCurrentPlayState = PLAY_STATE_ERROR;
                mTargetPlayState = PLAY_STATE_ERROR;
            }
        }
    }

    private void awaitSurfaceActive() {
        synchronized (this) {
            while (!mIsSurfaceActive) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasPreparedMediaPlayer() {
        return (mMediaPlayer == null || mCurrentPlayState == PLAY_STATE_ERROR || mCurrentPlayState == PLAY_STATE_INIT || mCurrentPlayState == PLAY_STATE_PREPARE) ? false : true;
    }

    public int getVideoHeight() {
        return mMediaPlayer != null ? mMediaPlayer.getVideoHeight() : 0;
    }

    public int getVideoWidth() {
        return mMediaPlayer != null ? mMediaPlayer.getVideoWidth() : 0;
    }

    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public void setLooping(boolean isLoop) {
        mIsLoop = isLoop;
    }

    public boolean isPlaying() {
        return hasPreparedMediaPlayer() && mMediaPlayer.isPlaying();
    }

    private class Size {
        private int mWidth;
        private int mHeight;

        public Size(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }
    }

    public interface onStartListener {

        void onStart();
    }

    public interface onErrorListener {

        void onError();
    }

    public interface OnCompletionListener {

        void onCompletion();
    }

    public interface OnSurfaceAvailableListener {

        void onSurfaceAvailable();
    }

    private class MatrixManager {
        private Size mViewSize;
        private Size mVideoSize;

        MatrixManager(Size viewSize, Size videoSize) {
            mViewSize = viewSize;
            mVideoSize = videoSize;
        }

        private Matrix getMatrix() {
            return getMatrix(((float) mVideoSize.getWidth()) / ((float) mViewSize.getWidth()),
                    ((float) mVideoSize.getHeight()) / ((float) mViewSize.getHeight()), Gravity.LEFT_TOP);
        }

        private Matrix getMatrix(float scaleX, float scaleY, float pivotX, float pivotY) {
            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleY, pivotX, pivotY);
            return matrix;
        }

        private Matrix getMatrix(float f, float f2, Gravity gravity) {
            switch (gravity) {
                case LEFT_TOP:
                    return getMatrix(f, f2, 0.0f, 0.0f);
                case LEFT_CENTER:
                    return getMatrix(f, f2, 0.0f, ((float) mViewSize.getHeight()) / 2.0f);
                case LEFT_BOTTOM:
                    return getMatrix(f, f2, 0.0f, (float) mViewSize.getHeight());
                case CENTER_TOP:
                    return getMatrix(f, f2, ((float) mViewSize.getWidth()) / 2.0f, 0.0f);
                case CENTER:
                    return getMatrix(f, f2, ((float) mViewSize.getWidth()) / 2.0f, ((float) mViewSize.getHeight()) / 2.0f);
                case CENTER_BOTTOM:
                    return getMatrix(f, f2, ((float) mViewSize.getWidth()) / 2.0f, (float) mViewSize.getHeight());
                case RIGHT_TOP:
                    return getMatrix(f, f2, (float) mViewSize.getWidth(), 0.0f);
                case RIGHT_CENTER:
                    return getMatrix(f, f2, (float) mViewSize.getWidth(), ((float) mViewSize.getHeight()) / 2.0f);
                case RIGHT_BOTTOM:
                    return getMatrix(f, f2, (float) mViewSize.getWidth(), (float) mViewSize.getHeight());
                default:
                    throw new IllegalArgumentException("Illegal PivotPoint");
            }
        }

        private Matrix getMatrix(Gravity gravity) {
            float a = ((float) mViewSize.getWidth()) / ((float) mVideoSize.getWidth());
            float b = ((float) mViewSize.getHeight()) / ((float) mVideoSize.getHeight());
            float min = Math.min(a, b);
            return getMatrix(min / a, min / b, gravity);
        }

        private Matrix getMatrixNoCrop() {
            return getMatrix(1.0f, 1.0f, Gravity.LEFT_TOP);
        }

        private Matrix getMatrixNoCrop(Gravity gravity) {
            return getMatrix(((float) mVideoSize.getWidth()) / ((float) mViewSize.getWidth()), ((float) mVideoSize.getHeight()) / ((float) mViewSize.getHeight()), gravity);
        }

        private Matrix getMatrixCrop() {
            return getMatrix(Gravity.LEFT_TOP);
        }

        private Matrix getMatrixCrop(Gravity gravity) {
            float a = ((float) mViewSize.getWidth()) / ((float) mVideoSize.getWidth());
            float b = ((float) mViewSize.getHeight()) / ((float) mVideoSize.getHeight());
            float max = Math.max(a, b);
            return getMatrix(max / a, max / b, gravity);
        }

        private Matrix getMatrixFitCenter() {
            return getMatrix(Gravity.CENTER);
        }

        private Matrix getMatrixFitEnd() {
            return getMatrix(Gravity.RIGHT_BOTTOM);
        }

        private Matrix getMatrixStartInside() {
            return (mVideoSize.getHeight() > mViewSize.getWidth() || mVideoSize.getHeight() > mViewSize.getHeight()) ? getMatrixCrop() : getMatrixNoCrop(Gravity.LEFT_TOP);
        }

        private Matrix getMatrixCenterInside() {
            return (mVideoSize.getHeight() > mViewSize.getWidth() || mVideoSize.getHeight() > mViewSize.getHeight()) ? getMatrixFitCenter() : getMatrixNoCrop(Gravity.CENTER);
        }

        private Matrix getMatrixEndInside() {
            return (mVideoSize.getHeight() > mViewSize.getWidth() || mVideoSize.getHeight() > mViewSize.getHeight()) ? getMatrixFitEnd() : getMatrixNoCrop(Gravity.RIGHT_BOTTOM);
        }

        Matrix create(ScaleType scaleType) {
            switch (scaleType) {
                case NONE:
                    return getMatrix();
                case FIT_XY:
                    return getMatrixNoCrop();
                case FIT_CENTER:
                    return getMatrixFitCenter();
                case FIT_START:
                    return getMatrixCrop();
                case FIT_END:
                    return getMatrixFitEnd();
                case LEFT_TOP:
                    return getMatrixNoCrop(Gravity.LEFT_TOP);
                case LEFT_CENTER:
                    return getMatrixNoCrop(Gravity.LEFT_CENTER);
                case LEFT_BOTTOM:
                    return getMatrixNoCrop(Gravity.LEFT_BOTTOM);
                case CENTER_TOP:
                    return getMatrixNoCrop(Gravity.CENTER_TOP);
                case CENTER:
                    return getMatrixNoCrop(Gravity.CENTER);
                case CENTER_BOTTOM:
                    return getMatrixNoCrop(Gravity.CENTER_BOTTOM);
                case RIGHT_TOP:
                    return getMatrixNoCrop(Gravity.RIGHT_TOP);
                case RIGHT_CENTER:
                    return getMatrixNoCrop(Gravity.RIGHT_CENTER);
                case RIGHT_BOTTOM:
                    return getMatrixNoCrop(Gravity.RIGHT_BOTTOM);
                case LEFT_TOP_CROP:
                    return getMatrixCrop(Gravity.LEFT_TOP);
                case LEFT_CENTER_CROP:
                    return getMatrixCrop(Gravity.LEFT_CENTER);
                case LEFT_BOTTOM_CROP:
                    return getMatrixCrop(Gravity.LEFT_BOTTOM);
                case CENTER_TOP_CROP:
                    return getMatrixCrop(Gravity.CENTER_TOP);
                case CENTER_CROP:
                    return getMatrixCrop(Gravity.CENTER);
                case CENTER_BOTTOM_CROP:
                    return getMatrixCrop(Gravity.CENTER_BOTTOM);
                case RIGHT_TOP_CROP:
                    return getMatrixCrop(Gravity.RIGHT_TOP);
                case RIGHT_CENTER_CROP:
                    return getMatrixCrop(Gravity.RIGHT_CENTER);
                case RIGHT_BOTTOM_CROP:
                    return getMatrixCrop(Gravity.RIGHT_BOTTOM);
                case START_INSIDE:
                    return getMatrixStartInside();
                case CENTER_INSIDE:
                    return getMatrixCenterInside();
                case END_INSIDE:
                    return getMatrixEndInside();
                default:
                    throw new IllegalArgumentException("Illegal scale type");
            }
        }
    }
}


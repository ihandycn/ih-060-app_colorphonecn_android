package com.honeycomb.colorphone.view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by ihandysoft on 2017/10/17.
 */

public class WelcomeVideoView extends SurfaceView {

    private MediaPlayer mediaPlayer;

    private SurfaceHolder surfaceHolder;

    private AssetFileDescriptor mAssetFile;

    private PlayEndListener mPlayEndListener;
    private View mCover;
    public boolean mSurfaceReady;
    private boolean mPendingPlayForSurface;

    public WelcomeVideoView(Context context) {
        super(context);
    }

    public WelcomeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WelcomeVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private void init() {
        mediaPlayer = new MediaPlayer();
        surfaceHolder = getHolder();
        // Add this to avoid black background before video playing.
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceReady = true;
                if (mPendingPlayForSurface) {
                    doPlay();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private Runnable mVideoTask = new Runnable() {
        @Override
        public void run() {
            try {
                if (mediaPlayer != null) {
                    mediaPlayer = new MediaPlayer();
                }

                mediaPlayer.reset();
                //detect if file exists
                setVisibility(VISIBLE);
                mediaPlayer.setDataSource(mAssetFile.getFileDescriptor(), mAssetFile.getStartOffset(), mAssetFile.getLength());
                mediaPlayer.setDisplay(surfaceHolder);
                mediaPlayer.prepareAsync();
                mediaPlayer.setLooping(false);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        setCenterCrop();
                        mp.start();

                    }
                });
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START && mCover != null) {
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mCover.setVisibility(INVISIBLE);
                                }
                            }, 100);
                        }
                        return false;
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mPlayEndListener != null) {
                            mPlayEndListener.onEnd();
                        }
                    }
                });


            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e2) {
                e2.printStackTrace();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        }
    };

    public void play() {
        if (mSurfaceReady) {
            doPlay();
        } else {
            mPendingPlayForSurface = true;
        }
    }

    private void doPlay() {
        post(mVideoTask);
    }

    public void stop() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.prepareAsync();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e2) {
            e2.printStackTrace();
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setCenterCrop() {
        if (getMeasuredHeight() > 0 && getMeasuredWidth() > 0) {

            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            if (width > 0 && height > 0) {
                final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();

                float scaleVideo =  9f / 16f;
                float scaleSurface = width / (float) height;
                if (scaleVideo < scaleSurface) {
                    params.width = width;
                    params.height = (int) (width / scaleVideo);
                    params.setMargins(0, (height - params.height) / 2, 0, (height - params.height) / 2);
                } else {
                    params.height = height;
                    params.width = (int) (height * scaleVideo);
                    params.setMargins((width - params.width) / 2, 0, (width - params.width) / 2, 0);
                }

                setLayoutParams(params);
            }
        }
    }

    public void setAssetFile(AssetFileDescriptor assetFile) {
        mAssetFile = assetFile;
    }

    public void setPlayEndListener(PlayEndListener playEndListener) {
        mPlayEndListener = playEndListener;
    }

    public void setCover(View cover) {
        mCover = cover;
    }

    public View getCover() {
        return mCover;
    }


    public interface PlayEndListener {
        void onEnd();
    }
}

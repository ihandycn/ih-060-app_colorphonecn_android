package com.colorphone.ringtones;

import android.media.MediaPlayer;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MusicPlayer implements MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener{

    private static final String TAG = MusicPlayer.class.getSimpleName();

    public static final int STATE_IDLE = 0, STATE_INITIALIZED = 1, STATE_PREPARING = 2,
            STATE_PREPARED = 3, STATE_STARTED = 4, STATE_PAUSED = 5, STATE_STOPPED = 6,
            STATE_COMPLETED = 7, STATE_RELEASED = 8, STATE_ERROR = -1;

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        setPlayerState(STATE_PREPARED);
        doStartPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setPlayerState(STATE_COMPLETED);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        setPlayerState(STATE_ERROR);
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
    }

    @IntDef({STATE_IDLE, STATE_INITIALIZED, STATE_PREPARING,
            STATE_PREPARED, STATE_STARTED, STATE_PAUSED,
            STATE_STOPPED, STATE_COMPLETED, STATE_RELEASED,
            STATE_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private @State int mState = STATE_IDLE;

    private MediaPlayer mPlayer = null;

    private PlayStateChangeListener mStateListener;

    public void onDestroy() {
        if (mStateListener != null) {
            mStateListener.onShutdown();
        }
        Log.v(TAG, "onDestroy");
    }

    private void setPlayerState (@State int state) {
        if (mState == state) {
            return;
        }
        mState = state;
        if (mStateListener != null) {
            mStateListener.onStateChanged(mState);
        }
    }

    private void ensurePlayer () {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        setPlayerState(STATE_IDLE);
        mPlayer.setOnInfoListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnSeekCompleteListener(this);
    }

    public void startPlayer (String path) {
        //releasePlayer();
        ensurePlayer();
        try {
            mPlayer.setDataSource(path);
            mPlayer.setLooping(true);
            setPlayerState(STATE_INITIALIZED);
            mPlayer.prepareAsync();
            setPlayerState(STATE_PREPARING);
        } catch (IOException e) {
            e.printStackTrace();
            releasePlayer();
        }
    }

    private void doStartPlayer () {
        mPlayer.start();
        setPlayerState(STATE_STARTED);
    }

    public void resumePlayer () {
        if (isPaused()) {
            doStartPlayer();
        }
    }

    public void pausePlayer () {
        if (isStarted()) {
            mPlayer.pause();
            setPlayerState(STATE_PAUSED);
        }
    }

    public void stopPlayer () {
        if (isStarted() || isPaused()) {
            mPlayer.stop();
            setPlayerState(STATE_STOPPED);
        }
    }

    public void releasePlayer () {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            setPlayerState(STATE_RELEASED);
        }
    }

    public boolean isStarted () {
        return mState == STATE_STARTED;
    }

    public boolean isPaused () {
        return mState == STATE_PAUSED;
    }

    public boolean isReleased () {
        return mState == STATE_RELEASED;
    }

    public @State int getState () {
        return mState;
    }

    public int getPosition () {
        if (mPlayer == null) {
            return 0;
        }

        try {
            return mPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void seekTo (int position) {
        if (isStarted() || isPaused()) {
            mPlayer.seekTo(position);
        }
    }

    public void setPlayStateChangeListener (PlayStateChangeListener listener) {
        mStateListener = listener;
    }

    public interface PlayStateChangeListener {
        void onStateChanged (@State int state);
        void onShutdown ();
    }
}


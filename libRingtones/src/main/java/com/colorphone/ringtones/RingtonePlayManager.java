package com.colorphone.ringtones;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.colorphone.ringtones.module.Ringtone;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

public class RingtonePlayManager implements MusicPlayer.PlayStateChangeListener {
    private static final String TAG = "RingtonePlayManager";

    private static RingtonePlayManager sManager = null;

    public synchronized static RingtonePlayManager getInstance () {
        if (sManager == null) {
            sManager = new RingtonePlayManager(HSApplication.getContext().getApplicationContext());
        }
        return sManager;
    }

    private SimpleBroadcastReceiver mNoisyReceiver = new SimpleBroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // Pause the playback
                pause(false);
            }
        }
    };
    private Context mContext;
    private Ringtone mSong;
    private MusicPlayer mService;

    private int mState = MusicPlayer.STATE_IDLE;
    private boolean isPausedByUser;

    private int mPeriod = 1000;
    private boolean isProgressUpdating = false;

    private Handler mHandler = null;
    private List<Callback> mCallbacks;
    private List<ProgressCallback> mProgressCallbacks;

    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCallbacks != null && !mCallbacks.isEmpty()
                    && mService != null && mSong != null && mService.isStarted()) {
                for (ProgressCallback callback : mProgressCallbacks) {
                    callback.onProgress(mService.getPosition(), mSong.getDurationSeconds());
                }
                mHandler.postDelayed(this, mPeriod);
                isProgressUpdating = true;
            } else {
                isProgressUpdating = false;
            }
        }
    };

    private RingtonePlayManager(Context context) {
        mContext = context;
        mCallbacks = new ArrayList<>();
        mProgressCallbacks = new ArrayList<>();
        mHandler = new Handler();
    }

    private void registerNoisyReceiver () {
        mNoisyReceiver.register(mContext, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }

    private void unregisterNoisyReceiver () {
        mNoisyReceiver.unregister(mContext);
    }

    private AudioManager.OnAudioFocusChangeListener mAfListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.v(TAG, "onAudioFocusChange = " + focusChange);
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (isPlaying()) {
                    pause(false);
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (isPaused() && !isPausedByUser()) {
                    resume();
                }
            }
        }
    };

    private int requestAudioFocus () {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.requestAudioFocus(
                mAfListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private int releaseAudioFocus () {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.abandonAudioFocus(mAfListener);
    }
    /**
     *  dispatch the current song
     */
    public void dispatch() {
        dispatch(mSong, "dispatch ()");
    }

    /**
     * dispatch a song.
     * If the song is paused, then resume.
     * If the song is not started, then start it.
     * If the song is playing, then pause it.
     */
    public void dispatch(final Ringtone song, String by) {
        Log.v(TAG, "dispatch BY=" + by);
        Log.v(TAG, "dispatch song=" + song);
        if (song == null) {
            return;
        }

        if (mService == null) {
            mService = new MusicPlayer();
            mService.setPlayStateChangeListener(this);
        }

        if (mService != null) {
             if (song.equals(mSong)) {
                if (mService.isStarted()) {
                    //Do really this action by user
                    pause();
                } else if (mService.isPaused()){
                    resume();
                } else {
                    mService.releasePlayer();
                    if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == requestAudioFocus()) {
                        mSong = song;
                        mService.startPlayer(song.getFilePath());
                    }
                }
            } else {
                mService.releasePlayer();
                if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == requestAudioFocus()) {
                    mSong = song;
                    mService.startPlayer(song.getFilePath());
                }
            }
        }
    }

    public void registerCallback (Callback callback) {
        registerCallback(callback, false);
    }

    public void registerCallback (Callback callback, boolean updateOnceNow) {
        if (mCallbacks.contains(callback)) {
            return;
        }
        mCallbacks.add(callback);
        if (updateOnceNow) {
            callback.onPlayStateChanged(mState, mSong);
        }
    }

    public void unregisterCallback (Callback callback) {
        if (mCallbacks.contains(callback)) {
            mCallbacks.remove(callback);
        }
    }

    public void registerProgressCallback (ProgressCallback callback) {
        if (mProgressCallbacks.contains(callback)) {
            return;
        }
        mProgressCallbacks.add(callback);
        startUpdateProgressIfNeed();
    }

    public void unregisterProgressCallback (ProgressCallback callback) {
        if (mProgressCallbacks.contains(callback)) {
            mProgressCallbacks.remove(callback);
        }
    }

    private void startUpdateProgressIfNeed () {
        if (!isProgressUpdating) {
            mHandler.post(mProgressRunnable);
        }
    }

    @Override
    public void onStateChanged(@MusicPlayer.State int state) {
        mState = state;
        switch (state) {
            case MusicPlayer.STATE_IDLE:
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_INITIALIZED:
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_PREPARING:
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_PREPARED:
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_STARTED:
                registerNoisyReceiver();
                startUpdateProgressIfNeed();
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_PAUSED:
                unregisterNoisyReceiver();
                break;
            case MusicPlayer.STATE_ERROR:
                unregisterNoisyReceiver();
                releaseAudioFocus();
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_STOPPED:
                Log.v(TAG, "onStateChanged STATE_STOPPED");
                unregisterNoisyReceiver();
                releaseAudioFocus();
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_COMPLETED:
                Log.v(TAG, "onStateChanged STATE_COMPLETED");
                unregisterNoisyReceiver();
                releaseAudioFocus();
                isPausedByUser = false;
                break;
            case MusicPlayer.STATE_RELEASED:
                Log.v(TAG, "onStateChanged STATE_RELEASED");
                unregisterNoisyReceiver();
                releaseAudioFocus();
                isPausedByUser = false;
                break;

            default:
                break;
        }
        for (Callback callback : mCallbacks) {
            callback.onPlayStateChanged(state, mSong);
        }
    }

    @Override
    public void onShutdown() {
        releaseAudioFocus();
        NotificationManagerCompat notifyManager = NotificationManagerCompat.from(mContext);
        notifyManager.cancelAll();
        for (Callback callback : mCallbacks) {
            callback.onShutdown();
        }
    }
    /**
     * resume play
     */
    public void resume () {
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == requestAudioFocus()) {
            mService.resumePlayer();
        }
    }

    /**
     * pause a playing song by user action
     */
    public void pause () {
        if (mService != null) {
            pause(true);
        }
    }

    /**
     * pause a playing song
     * @param isPausedByUser false if triggered by {@link AudioManager#AUDIOFOCUS_LOSS} or
     *                       {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT}
     */
    private void pause (boolean isPausedByUser) {
        mService.pausePlayer();
        this.isPausedByUser = isPausedByUser;
    }

    public void stop () {
        if (mService != null) {
            mService.stopPlayer();
        }
    }

    /**
     * release a playing song
     */
    public void release () {
        if (mService != null) {
            mService.releasePlayer();
            mService.setPlayStateChangeListener(null);
            mService = null;
        }
    }

    public boolean isPlaying () {
        return mService != null && mService.isStarted();
    }

    public boolean isPaused () {
        return  mService != null && mService.isPaused();
    }

    public boolean isPausedByUser () {
        return isPausedByUser;
    }

    public void seekTo (int position) {
        if (mService != null) {
            mService.seekTo(position);
        }
    }
    public interface Callback {
        void onPlayStateChanged (@MusicPlayer.State int state, Ringtone song);
        void onShutdown ();
    }

    public interface ProgressCallback {
        void onProgress (int progress, int durationSeconds);
    }
}

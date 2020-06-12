package com.honeycomb.colorphone.guide;

import com.honeycomb.colorphone.util.ColorPhoneException;
import com.honeycomb.colorphone.util.SoundManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import java.util.concurrent.TimeUnit;

public class AccVoiceGuide {

    private static final String TAG = "AccVoiceGuide";

    private AccVoiceGuide() {
    }

    private static class ClassHolder {
        private static final AccVoiceGuide INSTANCE = new AccVoiceGuide();
    }

    public static AccVoiceGuide getInstance() {
        return ClassHolder.INSTANCE;
    }

    private static final int FORBID_PLAY_VOICE = -1;
    private int playVoiceCount = FORBID_PLAY_VOICE;

    private int voiceStreamId;
    private boolean isStart = false;

    private Runnable playVoiceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCouldPlay()) {
                HSLog.d(TAG, "stop acc voice guide, times is enough!!!");
                return;
            }

            playVoice();
            Threads.postOnMainThreadDelayed(this, TimeUnit.SECONDS.toMillis(10));
        }

        private boolean isCouldPlay() {
            return playVoiceCount != FORBID_PLAY_VOICE && playVoiceCount <= 3;
        }
    };

    public void start() {
        HSLog.d(TAG, "start acc voice guide, isStart = " + isStart);
        if (isStart) {
            ColorPhoneException.handleException("isStart should not be true!!!");
            return;
        }
        isStart = true;
        playVoiceCount = 0;
        Threads.postOnMainThread(playVoiceRunnable);
    }

    public void stop() {
        HSLog.d(TAG, "stop acc voice guide, isStart = " + isStart);
        if (!isStart) {
            return;
        }
        isStart = false;
        stopVoice();
        playVoiceCount = FORBID_PLAY_VOICE;
        Threads.removeOnMainThread(playVoiceRunnable);
    }

    private void playVoice() {
        playVoiceCount++;
        voiceStreamId = SoundManager.getInstance().playAccGuideVoice1();
    }

    private void stopVoice() {
        if (voiceStreamId != 0) {
            SoundManager.getInstance().stopAccGuideVoice(voiceStreamId);
        }

    }
}

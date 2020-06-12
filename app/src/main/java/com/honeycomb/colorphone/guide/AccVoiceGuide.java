package com.honeycomb.colorphone.guide;

import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ColorPhoneException;
import com.honeycomb.colorphone.util.SoundManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Compats;
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
                if (playVoiceCount >= 3) {
                    AccGuideAutopilotUtils.logVoiceGuideEnd();
                    Analytics.logEvent("Voice_Guide_End", "reason", "end", "times", String.valueOf(playVoiceCount));
                }
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

    public boolean isEnable() {
        return (Compats.IS_HUAWEI_DEVICE || Compats.IS_OPPO_DEVICE) && AccGuideAutopilotUtils.isEnable();
    }

    public int getPlayVoiceCount() {
        return playVoiceCount;
    }

    public void start() {
        HSLog.d(TAG, "start acc voice guide, isStart = " + isStart);
        if (isStart) {
            ColorPhoneException.handleException("isStart should not be true!!!");
            return;
        }
        AccGuideAutopilotUtils.logVoiceGuideStart();
        Analytics.logEvent("Voice_Guide_Start");
        isStart = true;
        playVoiceCount = 0;
        Threads.postOnMainThread(playVoiceRunnable);
    }

    public void stop(String source) {
        HSLog.d(TAG, "stop acc voice guide, isStart = " + isStart);
        if (!isStart) {
            return;
        }
        isStart = false;
        AccGuideAutopilotUtils.logVoiceGuideEnd();
        Analytics.logEvent("Voice_Guide_End", "reason", source, "times", String.valueOf(playVoiceCount));
        stopVoice();
        playVoiceCount = FORBID_PLAY_VOICE;
        Threads.removeOnMainThread(playVoiceRunnable);
    }

    private void playVoice() {
        playVoiceCount++;
        switch (AccGuideAutopilotUtils.getVoiceType()) {
            default:
            case 1:
                voiceStreamId = SoundManager.getInstance().playAccGuideVoice1();
                break;
            case 2:
                voiceStreamId = SoundManager.getInstance().playAccGuideVoice2();
                break;
            case 3:
                voiceStreamId = SoundManager.getInstance().playAccGuideVoice3();
                break;
        }
    }

    private void stopVoice() {
        if (voiceStreamId != 0) {
            SoundManager.getInstance().stopAccGuideVoice(voiceStreamId);
        }

    }
}

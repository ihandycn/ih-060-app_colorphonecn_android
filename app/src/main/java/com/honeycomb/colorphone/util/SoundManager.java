package com.honeycomb.colorphone.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.honeycomb.colorphone.R;

public class SoundManager {

    private SoundManager() {
    }

    private static class ClassHolder {
        private static final SoundManager INSTANCE = new SoundManager();
    }

    public static SoundManager getInstance() {
        return ClassHolder.INSTANCE;
    }

    private SoundPool soundPool;
    private boolean isSoundPoolInitFinish = false;

    private int idAccGuideVoice1;
    private int idAccGuideVoice2;
    private int idAccGuideVoice3;
    private int idAccGuideVoiceXiaoMi;
    private int idAccGuideVoiceVivo;

    public void init(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
        idAccGuideVoice1 = soundPool.load(context, R.raw.acc_voice_guide_1, 1);
        idAccGuideVoice2 = soundPool.load(context, R.raw.acc_voice_guide_2, 1);
        idAccGuideVoice3 = soundPool.load(context, R.raw.acc_voice_guide_3, 1);
        idAccGuideVoiceXiaoMi = soundPool.load(context, R.raw.acc_voice_guide_xiaomi, 1);
        idAccGuideVoiceVivo = soundPool.load(context, R.raw.acc_voice_guide_vivo, 1);

        isSoundPoolInitFinish = true;
    }

    public int playAccGuideVoice1() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAccGuideVoice1, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public void stopAccGuideVoice(int streamId) {
        if (isSoundPoolInitFinish) {
            soundPool.stop(streamId);
        }
    }

    public int playAccGuideVoice2() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAccGuideVoice2, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playAccGuideVoice3() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAccGuideVoice3, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playAccGuideVoiceXiaoMi() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAccGuideVoiceXiaoMi, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playAccGuideVoiceVivo() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAccGuideVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }
}

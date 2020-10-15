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
    private int idPermissionGuideVoiceVivo;
    private int idPermissionNoAutoStartVoiceVivo;
    private int idAutoStartSystemGuideVivo;
    private int idNAGuideVoiceVivo;
    private int idPhoneGuideVoiceVivo;
    private int idContactVoiceVivo;
    private int idAutoTaskVoiceVivo;

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
        idPermissionGuideVoiceVivo = soundPool.load(context, R.raw.auto_start_voice_guide_vivo, 1);
        idPermissionNoAutoStartVoiceVivo = soundPool.load(context, R.raw.no_auto_start_voice_guide_vivo, 1);
        idAutoStartSystemGuideVivo = soundPool.load(context, R.raw.auto_start_system_voice_guide_vivo, 1);
        idNAGuideVoiceVivo = soundPool.load(context, R.raw.notification_voice_guide_vivo, 1);
        idPhoneGuideVoiceVivo = soundPool.load(context, R.raw.phone_voice_guide_vivo, 1);
        idContactVoiceVivo = soundPool.load(context, R.raw.contact_voice_guide_vivo, 1);
        idAutoTaskVoiceVivo = soundPool.load(context, R.raw.auto_task_start_voice_vivo, 1);

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

    public int playVivoPermissionGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPermissionGuideVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoNoAutoStartGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPermissionNoAutoStartVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoAutoStartSystemPermissionGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoStartSystemGuideVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoNAGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idNAGuideVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoPhoneGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPhoneGuideVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoContactGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idContactVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoAutoStartGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoTaskVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }
}

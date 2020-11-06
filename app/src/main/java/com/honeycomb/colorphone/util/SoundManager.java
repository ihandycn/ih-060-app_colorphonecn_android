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
    private int idAccGuideVoiceXiaomi;
    private int idAccGuideVoiceVivo;

    private int idAutoTaskVoice;

    private int idPermissionGuideVoiceVivo;
    private int idNAGuideVoiceVivo;
    private int idPhoneGuideVoiceVivo;
    private int idContactVoiceVivo;

    private int idAutoStartVoiceOppo;
    private int idAutoStartVoiceOppo10;
    private int idRuntimeVoiceOppo;
    private int idDrawOverlayVoiceOppo;
    private int idNAGuideVoiceOppo;
    private int idPNGuideVoiceOppo;
    private int idPhoneGuideVoiceOppo;

    private int idAutoStartVoiceHuawei;
    private int idPhoneGuideVoiceHuawei;

    private int idAutoStartVoiceXiaomi;
    private int idPhoneGuideVoiceXiaomi;
    private int idLockGuideVoiceXiaomi;
    private int idBackgroundGuideVoiceXiaomi;


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
        idAccGuideVoiceXiaomi = soundPool.load(context, R.raw.acc_voice_guide_xiaomi, 1);
        idAccGuideVoiceVivo = soundPool.load(context, R.raw.acc_voice_guide_vivo, 1);

        idAutoTaskVoice = soundPool.load(context, R.raw.auto_task_start_voice, 1);

        idPermissionGuideVoiceVivo = soundPool.load(context, R.raw.auto_start_voice_guide_vivo, 1);
        idNAGuideVoiceVivo = soundPool.load(context, R.raw.notification_voice_guide_vivo, 1);
        idPhoneGuideVoiceVivo = soundPool.load(context, R.raw.phone_voice_guide_vivo, 1);
        idContactVoiceVivo = soundPool.load(context, R.raw.contact_voice_guide_vivo, 1);

        idAutoStartVoiceOppo = soundPool.load(context, R.raw.auto_start_voice_oppo, 1);
        idAutoStartVoiceOppo10 = soundPool.load(context, R.raw.auto_start_voice_oppo10, 1);
        idRuntimeVoiceOppo = soundPool.load(context, R.raw.runtime_voice_oppo, 1);
        idDrawOverlayVoiceOppo = soundPool.load(context, R.raw.draw_overlay_voice_oppo, 1);
        idNAGuideVoiceOppo = soundPool.load(context, R.raw.notification_voice_oppo, 1);
        idPNGuideVoiceOppo = soundPool.load(context, R.raw.post_notification_voice_oppo, 1);
        idPhoneGuideVoiceOppo = soundPool.load(context, R.raw.phone_voice_guide_oppo, 1);

        idAutoStartVoiceHuawei = soundPool.load(context, R.raw.auto_start_voice_huawei, 1);
        idPhoneGuideVoiceHuawei = soundPool.load(context, R.raw.phone_voice_guide_huawei, 1);

        idAutoStartVoiceXiaomi = soundPool.load(context, R.raw.auto_start_voice_xiaomi, 1);
        idPhoneGuideVoiceXiaomi = soundPool.load(context, R.raw.phone_voice_guide_xiaomi, 1);
        idLockGuideVoiceXiaomi = soundPool.load(context, R.raw.lock_voice_guide_xiaomi, 1);
        idBackgroundGuideVoiceXiaomi = soundPool.load(context, R.raw.background_voice_guide_xiaomi, 1);

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
            return soundPool.play(idAccGuideVoiceXiaomi, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playAccGuideVoiceVivo() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAccGuideVoiceVivo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playAutoTaskGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoTaskVoice, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playVivoPermissionGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPermissionGuideVoiceVivo, 1, 1, 1, 0, 1);
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

    public int playOppoAutoStartGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoStartVoiceOppo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playOppo10AutoStartGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoStartVoiceOppo10, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playOppoRuntimeGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idRuntimeVoiceOppo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playOppoDrawOverlayGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idDrawOverlayVoiceOppo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playCommonNAGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idNAGuideVoiceOppo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playOppoPNGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPNGuideVoiceOppo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playOppoPhoneGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPhoneGuideVoiceOppo, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playHuaweiAutoStartGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoStartVoiceHuawei, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playHuaweiPhoneGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPhoneGuideVoiceHuawei, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playXiaomiAutoStartGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idAutoStartVoiceXiaomi, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playXiaomiPhoneGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idPhoneGuideVoiceXiaomi, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playXiaomiLockGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idLockGuideVoiceXiaomi, 1, 1, 1, 0, 1);
        }
        return 0;
    }

    public int playXiaomiBackgroundGuide() {
        if (isSoundPoolInitFinish) {
            return soundPool.play(idBackgroundGuideVoiceXiaomi, 1, 1, 1, 0, 1);
        }
        return 0;
    }
}

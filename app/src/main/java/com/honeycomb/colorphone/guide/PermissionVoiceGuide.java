package com.honeycomb.colorphone.guide;

import android.os.Build;

import com.honeycomb.colorphone.util.ColorPhoneException;
import com.honeycomb.colorphone.util.SoundManager;
import com.ihs.permission.HSPermissionRequestMgr;
import com.superapps.util.Compats;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

import java.util.concurrent.TimeUnit;

public class PermissionVoiceGuide {

    private static final String TAG = PermissionVoiceGuide.class.getSimpleName();
    private static final int DEFAULT_DELAY_SECONDS = 10;


    private static class ClassHolder {
        private static final PermissionVoiceGuide INSTANCE = new PermissionVoiceGuide();
    }

    public static PermissionVoiceGuide getInstance() {
        return ClassHolder.INSTANCE;
    }

    private static final int FORBID_PLAY_VOICE = -1;
    private int playVoiceCount = FORBID_PLAY_VOICE;

    private int voiceStreamId;
    private int delaySeconds = DEFAULT_DELAY_SECONDS;
    private boolean isStart = false;
    private String permission;

    private Runnable playVoiceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCouldPlay()) {
                if (playVoiceCount >= 3) {
                    VoiceGuideAutopilotUtils.logVoiceGuideEnd();
                }
                return;
            }

            playVoice();
            Threads.postOnMainThreadDelayed(this, TimeUnit.SECONDS.toMillis(delaySeconds));
        }

        private boolean isCouldPlay() {
            return playVoiceCount != FORBID_PLAY_VOICE && playVoiceCount < 3;
        }
    };

    public boolean isEnable() {
        if (Compats.IS_XIAOMI_DEVICE) {
            return AccGuideAutopilotUtils.isXiaoMiEnable();
        } else {
            return AccGuideAutopilotUtils.isEnable();
        }
    }


    public void start(String permission) {
        if (isStart) {
            ColorPhoneException.handleException("isStart should not be true!!!");
            return;
        }
        VoiceGuideAutopilotUtils.logVoiceGuideStart();
        isStart = true;
        playVoiceCount = 0;
        this.permission = permission;
        Threads.postOnMainThread(playVoiceRunnable);
    }

    public void stop() {
        if (!isStart) {
            return;
        }
        isStart = false;
        VoiceGuideAutopilotUtils.logVoiceGuideEnd();
        stopVoice();
        playVoiceCount = FORBID_PLAY_VOICE;
        Threads.removeOnMainThread(playVoiceRunnable);
    }

    private void playVoice() {
        if (VoiceGuideAutopilotUtils.isEnable()) {
            playVoiceNew();
        } else {
            if (RomUtils.checkIsVivoRom() || (RomUtils.checkIsOppoRom()
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                playVoiceOld();
            }
        }
    }

    private void playVoiceOld() {
        playVoiceCount++;
        switch (permission) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
                if (Compats.IS_OPPO_DEVICE) {
                    voiceStreamId = SoundManager.getInstance().playOppo10AutoStartGuide();
                    delaySeconds = 10;
                    break;
                }
            case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
            case HSPermissionRequestMgr.TYPE_BACKGROUND_POPUP:
                voiceStreamId = SoundManager.getInstance().playVivoPermissionGuide();
                delaySeconds = 9;
                break;
            case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:
                if (Compats.IS_OPPO_DEVICE) {
                    voiceStreamId = SoundManager.getInstance().playOppoNAGuide();
                    delaySeconds = 8;
                } else {
                    voiceStreamId = SoundManager.getInstance().playVivoNAGuide();
                    delaySeconds = 9;
                }
                break;
            case HSPermissionRequestMgr.TYPE_PHONE:
                voiceStreamId = SoundManager.getInstance().playVivoPhoneGuide();
                delaySeconds = 7;
                break;
            case HSPermissionRequestMgr.TYPE_CALL_LOG:
            case HSPermissionRequestMgr.TYPE_CONTACT_READ:
            case HSPermissionRequestMgr.TYPE_CONTACT_WRITE:
            case HSPermissionRequestMgr.TYPE_STORAGE:
                if (Compats.IS_OPPO_DEVICE) {
                    voiceStreamId = SoundManager.getInstance().playOppoRuntimeGuide();
                    delaySeconds = 10;
                } else {
                    voiceStreamId = SoundManager.getInstance().playVivoContactGuide();
                    delaySeconds = 9;
                }
                break;
        }
    }

    private void playVoiceNew() {
        playVoiceCount++;
        if (Compats.IS_OPPO_DEVICE) {
            playOppoVoice();
        } else if (Compats.IS_VIVO_DEVICE) {
            playVivoVoice();
        } else if (Compats.IS_XIAOMI_DEVICE) {
            playXiaomiVoice();
        } else if (Compats.IS_HUAWEI_DEVICE) {
            playHuaweiVoice();
        }
    }


    private void playOppoVoice() {
        switch (permission) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    voiceStreamId = SoundManager.getInstance().playOppo10AutoStartGuide();
                    delaySeconds = 10;
                } else {
                    voiceStreamId = SoundManager.getInstance().playOppoAutoStartGuide();
                    delaySeconds = 7;
                }
                break;
            case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:
                voiceStreamId = SoundManager.getInstance().playOppoNAGuide();
                delaySeconds = 8;
                break;
            case HSPermissionRequestMgr.TYPE_CALL_LOG:
            case HSPermissionRequestMgr.TYPE_CONTACT_READ:
            case HSPermissionRequestMgr.TYPE_CONTACT_WRITE:
            case HSPermissionRequestMgr.TYPE_STORAGE:
                voiceStreamId = SoundManager.getInstance().playOppoRuntimeGuide();
                delaySeconds = 10;
                break;
            case HSPermissionRequestMgr.TYPE_DRAW_OVERLAY:
                voiceStreamId = SoundManager.getInstance().playOppoDrawOverlayGuide();
                delaySeconds = 7;
                break;
            case HSPermissionRequestMgr.TYPE_POST_NOTIFICATION:
                voiceStreamId = SoundManager.getInstance().playOppoPNGuide();
                delaySeconds = 8;
                break;
            case HSPermissionRequestMgr.TYPE_PHONE:
                voiceStreamId = SoundManager.getInstance().playOppoPhoneGuide();
                delaySeconds = 8;
                break;
        }
    }

    private void playVivoVoice() {
        switch (permission) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
            case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
            case HSPermissionRequestMgr.TYPE_BACKGROUND_POPUP:
                voiceStreamId = SoundManager.getInstance().playVivoPermissionGuide();
                delaySeconds = 9;
                break;
            case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:
                voiceStreamId = SoundManager.getInstance().playVivoNAGuide();
                delaySeconds = 9;
                break;
            case HSPermissionRequestMgr.TYPE_PHONE:
                voiceStreamId = SoundManager.getInstance().playVivoPhoneGuide();
                delaySeconds = 7;
                break;
            case HSPermissionRequestMgr.TYPE_CALL_LOG:
            case HSPermissionRequestMgr.TYPE_CONTACT_READ:
            case HSPermissionRequestMgr.TYPE_CONTACT_WRITE:
            case HSPermissionRequestMgr.TYPE_STORAGE:
                voiceStreamId = SoundManager.getInstance().playVivoContactGuide();
                delaySeconds = 9;
                break;
        }
    }

    private void playXiaomiVoice() {
        switch (permission) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
                voiceStreamId = SoundManager.getInstance().playXiaomiAutoStartGuide();
                delaySeconds = 9;
                break;
            case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:
                voiceStreamId = SoundManager.getInstance().playVivoNAGuide();
                delaySeconds = 9;
                break;
            case HSPermissionRequestMgr.TYPE_PHONE:
                voiceStreamId = SoundManager.getInstance().playXiaomiPhoneGuide();
                delaySeconds = 6;
                break;
            case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                voiceStreamId = SoundManager.getInstance().playXiaomiLockGuide();
                delaySeconds = 8;
                break;
            case HSPermissionRequestMgr.TYPE_BACKGROUND_POPUP:
                voiceStreamId = SoundManager.getInstance().playXiaomiBackgroundGuide();
                delaySeconds = 9;
                break;
        }
    }

    private void playHuaweiVoice() {
        switch (permission) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
                voiceStreamId = SoundManager.getInstance().playHuaweiAutoStartGuide();
                delaySeconds = 11;
                break;
            case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:
                voiceStreamId = SoundManager.getInstance().playVivoNAGuide();
                delaySeconds = 9;
                break;
            case HSPermissionRequestMgr.TYPE_PHONE:
                voiceStreamId = SoundManager.getInstance().playHuaweiPhoneGuide();
                delaySeconds = 8;
                break;
        }
    }

    private void stopVoice() {
        if (voiceStreamId != 0) {
            SoundManager.getInstance().stopAccGuideVoice(voiceStreamId);
        }

    }
}

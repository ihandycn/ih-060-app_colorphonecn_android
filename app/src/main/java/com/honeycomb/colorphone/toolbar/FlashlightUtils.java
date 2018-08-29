package com.honeycomb.colorphone.toolbar;

import android.support.annotation.Nullable;

import com.honeycomb.colorphone.FlashManager;
import com.ihs.flashlight.FlashlightManager;

public class FlashlightUtils {

    public static final int FLASHLIGHT_STATUS_FAIL = -1;
    public static final int FLASHLIGHT_STATUS_OFF = 0;
    public static final int FLASHLIGHT_STATUS_ON = 1;

    public static int toggleFlashlight(@Nullable Runnable turnOnRunnable, @Nullable Runnable turnOffRunnable) {
        FlashlightManager manager = FlashlightManager.getInstance();
        if (!manager.init()) {
            return FLASHLIGHT_STATUS_FAIL;
        }
        if (FlashManager.getInstance().isSOS()) {
            FlashManager.getInstance().stopSOS();
            if (turnOffRunnable != null) {
                turnOffRunnable.run();
            }
            manager.release();
            return FLASHLIGHT_STATUS_OFF;
        }
        if (manager.isOn()) {
            manager.turnOff();
            if (turnOffRunnable != null) {
                turnOffRunnable.run();
            }
            manager.release();
            return FLASHLIGHT_STATUS_OFF;
        } else {
            if (manager.turnOn()) {
                if (turnOnRunnable != null) {
                    turnOnRunnable.run();
                }
                return FLASHLIGHT_STATUS_ON;
            }
        }
        return FLASHLIGHT_STATUS_FAIL;
    }

}

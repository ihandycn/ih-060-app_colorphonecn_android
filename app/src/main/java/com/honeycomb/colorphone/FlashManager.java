package com.honeycomb.colorphone;

import android.os.Handler;
import android.os.Looper;

import com.ihs.flashlight.FlashlightManager;

public class FlashManager {

    private static FlashManager sInstance;
    private Handler mainHandler;

    public synchronized static FlashManager getInstance() {
        if (sInstance == null) {
            sInstance = new FlashManager();
        }
        return sInstance;
    }

    FlashManager() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private static final int INCOMING_FLASH_INTERVAL = 500;
    private boolean isFlash = false;
    private FlashThread flashThread;

    public void startFlash() {
        isFlash = true;
        if (flashThread == null) {
            flashThread = new FlashThread();
        }
        if (!flashThread.isAlive()) {
            flashThread.start();
        }
    }

    public void startFlash(int count) {
        mainHandler.postDelayed(new Runnable() {
            @Override public void run() {
                startFlash();
            }
        }, 200);

        mainHandler.postDelayed(new Runnable() {
            @Override public void run() {
                stopFlash();
            }
        }, INCOMING_FLASH_INTERVAL * 2 * count + 200);
    }

    public void stopFlash() {
        isFlash = false;
        turnOffLight();
        if (flashThread != null) {
            flashThread.interrupt();
            flashThread = null;
        }
    }

    public boolean isFlash() {
        return isFlash;
    }

    private class FlashThread extends Thread {
        @Override
        public void run() {
            while (isFlash) {
                turnOnLight();
                try {
                    sleep(INCOMING_FLASH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                turnOffLight();
                try {
                    sleep(INCOMING_FLASH_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final int SOS_LONG_INTERVAL = 1000;
    private static final int SOS_SHORT_INTERVAL = 300;
    private boolean isSOSOn = false;
    private SOSThread sosThread;

    public void startSOS() {
        isSOSOn = true;
        if (sosThread == null) {
            sosThread = new SOSThread();
        }
        sosThread.start();
    }

    public void stopSOS() {
        isSOSOn = false;
        turnOffLight();
        if (sosThread != null) {
            sosThread.interrupt();
            sosThread = null;
        }
    }

    public boolean isSOS() {
        return isSOSOn;
    }

    private class SOSThread extends Thread {
        private int long_times = 0;
        private int short_times = 0;
        private boolean isOn = false;

        @Override
        public void run() {
            while (isSOSOn) {
                if (isOn) {
                    turnOffLight();
                } else {
                    turnOnLight();
                }
                isOn = !isOn;
                if (long_times < 6) {
                    try {
                        sleep(SOS_LONG_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long_times++;
                } else if (short_times < 6) {
                    try {
                        sleep(SOS_SHORT_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    short_times++;
                }
                if (long_times == 6 && short_times == 6) {
                    long_times = 0;
                    short_times = 0;
                }
            }
        }
    }

    private void turnOnLight() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                FlashlightManager.getInstance().turnOn();
            }
        });
    }

    private void turnOffLight() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                FlashlightManager.getInstance().turnOff();
            }
        });
    }
}

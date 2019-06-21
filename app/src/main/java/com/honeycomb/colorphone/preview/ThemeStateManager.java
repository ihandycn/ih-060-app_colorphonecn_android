package com.honeycomb.colorphone.preview;


import java.util.ArrayList;
import java.util.List;

public class ThemeStateManager {

    public static final int ENJOY_MODE = 0;
    public static final int PREVIEW_MODE = 1;
    private static ThemeStateManager themeStateManager;
    private int themeMode = ENJOY_MODE;
    private List<StateChangeObserver> list = new ArrayList<>();
    private boolean mAudioMute = true;

    public static ThemeStateManager getInstance() {
        if (themeStateManager == null) {
            themeStateManager = new ThemeStateManager();
        }
        return themeStateManager;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void sendNotification(int themeMode) {
        this.themeMode = themeMode;
        for (StateChangeObserver observer : list) {
            observer.onReceive(themeMode);
        }
    }

    public void registerForThemeStateChange(StateChangeObserver observer) {
        list.add(observer);
    }

    public void unregisterForThemeStateChange(StateChangeObserver observer) {
        list.remove(observer);
    }

    public boolean isAudioMute() {
        return mAudioMute;
    }

    public void setAudioMute(boolean audioMute) {
        mAudioMute = audioMute;
    }
}

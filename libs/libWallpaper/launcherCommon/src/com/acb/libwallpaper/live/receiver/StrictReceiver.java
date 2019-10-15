package com.acb.libwallpaper.live.receiver;


/**
 * This receiver enabled when screen on, disabled when screen off ( after 5 min)
 */
public interface StrictReceiver {
    void register();
    void unRegister();
}

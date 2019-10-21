package com.acb.libwallpaper.live;

public interface EventsDelegate {
    void logEvent(String eventID);
    void logEvent(String eventID, String... vars);
}

package com.honeycomb.colorphone.wallpaper;

public interface EventsDelegate {
    void logEvent(String eventID);
    void logEvent(String eventID, String... vars);
}

package com.honeycomb.colorphone;

public interface EventsDelegate {
    void logEvent(String eventID);
    void logEvent(String eventID, String... vars);
}

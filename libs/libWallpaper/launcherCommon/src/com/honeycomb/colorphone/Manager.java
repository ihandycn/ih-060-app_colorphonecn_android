package com.honeycomb.colorphone;

import com.honeycomb.colorphone.EventsDelegate;

public final class Manager {
    private Manager() {
    }

    private static final Manager sInstance = new Manager();

    public static Manager getInstance() {
        return sInstance;
    }

    public EventsDelegate getDelegate() {
        return mDelegate;
    }

    public void setDelegate(EventsDelegate delegate) {
        this.mDelegate = delegate;
    }

    private EventsDelegate mDelegate = new EventsDelegate() {
        @Override
        public void logEvent(String eventID) {

        }

        @Override
        public void logEvent(String eventID, String... vars) {

        }
    };
}

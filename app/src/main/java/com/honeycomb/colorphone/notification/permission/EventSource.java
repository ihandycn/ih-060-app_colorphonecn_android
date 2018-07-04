package com.honeycomb.colorphone.notification.permission;

import android.support.annotation.NonNull;

public enum EventSource {

    FirstScreen("FirstScreen"), List("List");
    @NonNull
    private final String name;

    EventSource(@NonNull String name) {
        this.name = name;
    }

    public @NonNull String getName() {
        return name;
    }
}

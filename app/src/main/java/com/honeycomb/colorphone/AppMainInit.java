package com.honeycomb.colorphone;

public abstract class AppMainInit implements AppInit {

    @Override
    public boolean onlyInMainProcess() {
        return true;
    }
}

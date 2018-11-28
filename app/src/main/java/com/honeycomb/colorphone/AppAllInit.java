package com.honeycomb.colorphone;

public abstract class AppAllInit implements AppInit {

    @Override
    public boolean onlyInMainProcess() {
        return false;
    }
}

package com.acb.colorphone.permissions;

public abstract class Module {
    String name;
    String[] needPermissions;
    public Module(String name, String[] needPermissions) {
        this.name = name;
        this.needPermissions = needPermissions;
    }

    public abstract boolean enabled();
}

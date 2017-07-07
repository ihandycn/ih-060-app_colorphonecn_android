package com.honeycomb.colorphone;

import com.acb.call.AcbCallFactoryImpl;


public class CallConfigFactory extends AcbCallFactoryImpl {

    @Override
    public boolean isModuleEnable() {
        return true;
    }

    @Override
    public boolean isSettingsOpenDefault() {
        return true;
    }
}

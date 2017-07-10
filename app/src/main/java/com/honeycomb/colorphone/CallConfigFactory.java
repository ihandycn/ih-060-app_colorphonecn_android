package com.honeycomb.colorphone;

import com.acb.call.AcbCallFactoryImpl;
import com.acb.call.views.CallIdleAlert;


public class CallConfigFactory extends AcbCallFactoryImpl {

    @Override
    public boolean isModuleEnable() {
        return true;
    }

    @Override
    public boolean isSettingsOpenDefault() {
        return true;
    }

    @Override
    public CallIdleAlert.Config getCallIdleConfig() {
        return new CPCallIdleConfig();
    }

    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return "ColorPhone_A(NativeAds)CallOff";
        }
    }
}

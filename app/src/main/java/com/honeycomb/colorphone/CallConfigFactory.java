package com.honeycomb.colorphone;

import com.acb.call.AcbCallFactoryImpl;
import com.acb.call.ViewConfig;
import com.acb.call.views.CallIdleAlert;

import java.util.Random;


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

    @Override
    public ViewConfig getViewConfig() {
        return new CPViewConfig();
    }

    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return "ColorPhone_A(NativeAds)CallOff";
        }
    }

    private static class CPViewConfig extends ViewConfig {
        int[] faces = new int[]{
                R.drawable.face_1,
                R.drawable.face_2,
                R.drawable.face_3,
                R.drawable.face_4,
                R.drawable.face_5,
                R.drawable.face_6,
                R.drawable.face_7,
                R.drawable.face_8

        };

        @Override
        public int getCallerDefaultPhoto() {
            return faces[new Random(3982).nextInt(faces.length)];
        }
    }
}

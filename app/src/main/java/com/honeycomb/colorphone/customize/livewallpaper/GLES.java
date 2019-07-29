package com.honeycomb.colorphone.customize.livewallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build;

import com.ihs.app.framework.HSApplication;

/**
 * Compatibility wrapper for {@link GLES20} and {@link GLES30}.
 */
public class GLES {

    private static boolean sSupportsGLES30;
    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sSupportsGLES30 = false;
        } else {
            ActivityManager am = (ActivityManager) HSApplication.getContext().
                    getSystemService(Context.ACTIVITY_SERVICE);
            ConfigurationInfo info = am.getDeviceConfigurationInfo();
            sSupportsGLES30 = (info.reqGlEsVersion >= 0x30000);
        }
    }

    public static boolean supportsGLES30() {
        return sSupportsGLES30;
    }
}

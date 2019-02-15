package com.honeycomb.colorphone;

import com.call.assistant.util.CommonUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Compats;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CrashFix {

    public static void fix() {
        fixFinalizerWatchdogDaemon();
        fixHuaWeiAnr();
    }
    
    /**
     * https://blog.csdn.net/ZLMrche/article/details/81365204
     *
     */
    public static void fixFinalizerWatchdogDaemon() {
        try {
            Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");

            Method method = clazz.getSuperclass().getDeclaredMethod("stop");
            method.setAccessible(true);

            Field field = clazz.getDeclaredField("INSTANCE");
            field.setAccessible(true);

            method.invoke(field.get(null));

        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void fixHuaWeiAnr() {
        if (Compats.IS_HUAWEI_DEVICE && CommonUtils.ATLEAST_N) {
            boolean showAd = HSConfig.optBoolean(false, "Application", "HuaWeiHighVersionAd");
//            AdmobBannerAdapter.limitHWloadAd(!showAd);
//            DfpBannerAdapter.limitHWloadAd(!showAd);
        }
    }

}

package com.acb.libwallpaper.live.util;

import android.content.ComponentName;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class ComponentNameUtils {

    public static boolean match(ComponentName c1, ComponentName c2) {
        if (c1 == null || c2 == null) {
            return false;
        }
        if ((c2.getClassName().equals("*") || c1.getClassName().equals("*"))) {
            return c2.getPackageName().equals(c1.getPackageName());
        } else {
            return c1.equals(c2);
        }
    }

    public static ComponentName newComponent(@NonNull String pkg, @Nullable String cls) {
        if (TextUtils.isEmpty(cls)) {
            return new ComponentName(pkg, "*");
        }
        if (cls.startsWith(".")) {
            cls = pkg + cls;
        }
        return new ComponentName(pkg, cls);
    }

    public static ComponentName unflattenFrom(@NonNull String str) {
        int index = str.indexOf('/');
        if (index < 0) {
            return newComponent(str, "*");
        }
        if (index + 1 >= str.length()) {
            return newComponent(str.substring(0, index), "*");
        }
        String pkg = str.substring(0, index);
        String cls = str.substring(index + 1);
        if (cls.length() > 0 && cls.charAt(0) == '.') {
            cls = pkg + cls;
        }
        return newComponent(pkg, cls);
    }

    /**
     * Component has package name, but no class name or invalid name.
     * @param componentName
     * @return
     */
    public static boolean isIncomplete(ComponentName componentName) {
        return componentName.getClassName().length() < componentName.getPackageName().length();
    }
}

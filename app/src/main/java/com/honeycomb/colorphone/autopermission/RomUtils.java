package com.honeycomb.colorphone.autopermission;

import com.ihs.permission.rom.RomInfoManager;

public class RomUtils {
    public static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    public static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    public static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    public static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    public static final String KEY_VERSION_VIVO = "ro.vivo.os.version";
    public static final String KEY_VERSION_GIONEE = "ro.gn.sv.version";
    public static final String KEY_VERSION_LENOVO = "ro.lenovo.lvp.version";
    public static final String KEY_VERSION_FLYME = "ro.build.display.id";

    public static String getRomVerison(String key) {
        String systemProperties = RomInfoManager.getSystemPropertiesByKey(key);
        if (key.contains("miui.ui.version.name")) {
            systemProperties = systemProperties.replaceAll("V", "");
        }
        if (key.contains("ro.build.version.emui")) {
            if (systemProperties.contains("EmotionUI_")) {
                systemProperties = systemProperties.replaceAll("EmotionUI_", "").trim();
            } else {
                systemProperties = systemProperties.replaceAll("EmotionUI", "").trim();
            }
            systemProperties = systemProperties.replaceAll("\\.", "");
        }
        return systemProperties;
    }
}

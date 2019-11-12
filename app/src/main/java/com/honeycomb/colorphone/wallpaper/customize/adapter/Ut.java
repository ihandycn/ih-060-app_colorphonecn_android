package com.honeycomb.colorphone.wallpaper.customize.adapter;


import com.honeycomb.colorphone.wallpaper.customize.CustomizeConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Ut {
    public static String getDefaultCategoryName(String name) {
        final List<Map<String, ?>> categoryConfigs = (List<Map<String, ?>>) CustomizeConfig.getList("Wallpapers");

        for (int i = 0; i < categoryConfigs.size(); i++) {
            Map<String, ?> map = categoryConfigs.get(i);
            String configString;
            Object objectFromKey = map.get("CategoryName");
            if (objectFromKey instanceof String) {
                configString = (String) objectFromKey;
                return configString;
            } else {
                Map<String, String> stringMap = (Map<String, String>) objectFromKey;
                if (stringMap == null) {
                    continue;
                }
                Collection<String> values = stringMap.values();
                if (values.contains(name)) {
                    return stringMap.get("Default");
                }
            }
        }
        return "";
    }
}

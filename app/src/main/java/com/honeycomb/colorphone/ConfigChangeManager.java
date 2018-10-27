package com.honeycomb.colorphone;

import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConfigChangeManager {
    public static final int AUTOPILOT = 0x1;
    public static final int REMOTE_CONFIG = 0x2;
    private static ConfigChangeManager sConfigChangeManager = new ConfigChangeManager();

    private Map<Callback, Integer> map = new HashMap<>();

    public static ConfigChangeManager getInstance() {
        return sConfigChangeManager;
    }

    public void registerCallbacks(int type, Callback callbacks) {
        map.put(callbacks, type);
    }

    public void removeCallback(Callback callbacks) {
        map.remove(callbacks);
    }

    public void onChange(int type) {
        HSLog.d("ConfigChangeManager", "ConfigChange: " + type);
        Set<Map.Entry<Callback, Integer>> entrySet = map.entrySet();

        for (Map.Entry<Callback, Integer> entry : entrySet) {
            if ((entry.getValue() & type) == type) {
                entry.getKey().onChange(type);
            }
        }

    }


    public interface Callback {
        void onChange(int type);
    }
}

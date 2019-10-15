package com.acb.libwallpaper.live.util;

import net.appcloudbox.common.utils.AcbMapUtils;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Utility to support "Regions" in configs.
 */
public class ConfigRegionsSupport {

    public static final String KEY_DATA = "Data";
    private static final String KEY_REGIONS = "Regions";

    public static void mergeRegions(Map<String, ?> data) {
        if (data != null) {
            Map<String, ?> mainData = AcbMapUtils.getMap(data, KEY_DATA);
            Map<String, ?> regionsData = AcbMapUtils.getMap(data, KEY_REGIONS);
            if (null != regionsData) {
                String countryCode = Locale.getDefault().getCountry().trim();
                Map<String, ?> matchedRegion = AcbMapUtils.getMap(regionsData, countryCode);
                if (null == matchedRegion) {
                    matchedRegion = AcbMapUtils.getMap(regionsData, Locale.getDefault().getCountry().toUpperCase());
                }

                if (null == matchedRegion) {
                    matchedRegion = AcbMapUtils.getMap(regionsData, Locale.getDefault().getCountry().toLowerCase());
                }

                if (null == matchedRegion) {
                    Iterator var5 = regionsData.keySet().iterator();

                    //noinspection WhileLoopReplaceableByForEach
                    while (var5.hasNext()) {
                        String region = (String) var5.next();
                        if (region.equalsIgnoreCase(countryCode)) {
                            matchedRegion = AcbMapUtils.getMap(regionsData, region);
                            break;
                        }
                    }
                }
                if (null != matchedRegion) {
                    Map<String, ?> matchedRegionData = AcbMapUtils.getMap(matchedRegion, KEY_DATA);
                    //noinspection unchecked
                    AcbMapUtils.mergeMaps((Map<String, Object>) mainData, matchedRegionData);
                }
            }
        }
    }
}

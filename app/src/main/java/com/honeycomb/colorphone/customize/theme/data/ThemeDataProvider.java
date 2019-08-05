package com.honeycomb.colorphone.customize.theme.data;

import android.support.v4.os.TraceCompat;

import com.honeycomb.colorphone.customize.CustomizeConfig;
import com.honeycomb.colorphone.customize.ThemeCategoryInfo;
import com.honeycomb.colorphone.customize.theme.ThemeInfo;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;

import net.appcloudbox.common.utils.AcbMapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class ThemeDataProvider {

    private static final String TAG = ThemeDataProvider.class.getSimpleName();

    private static Map<String, Integer> sTagWeights = new HashMap<>();

    public static void clearCache() {
        if (sTagWeights != null) {
            sTagWeights.clear();
        }
    }

    public static Map<String, ?> getThemes() {
        return CustomizeConfig.getMap("Themes");
    }

    public static List<ThemeInfo> getAllThemes(boolean sortByUpdateId) {
        TraceCompat.beginSection(TAG + "#getAllThemesFromConfig");
        try {
            Map<String, ?> onlineDescriptionsMap = getOnlineDescriptions();
            List<ThemeInfo> themeInfoList = new ArrayList<>();
            final Set<String> onlineThemes = onlineDescriptionsMap.keySet();
            for (String themePackage : onlineThemes) {
                if (!CustomizeUtils.isThemePackage(themePackage)) {
                    continue;
                }
                ThemeInfo theme = ThemeInfo.ofConfig(themePackage, onlineDescriptionsMap);
                if (theme.isOnlineDescriptionConfigured() && theme.updateId > 0) {
                    themeInfoList.add(theme);
                }
            }
            if (sortByUpdateId) {
                Collections.sort(themeInfoList, (o1, o2) -> o2.updateId - o1.updateId);
            }
            return themeInfoList;
        } finally {
            TraceCompat.endSection();
        }
    }

    public static Map<String, ?> getOnlineDescriptions() {
        return CustomizeConfig.getMap("Themes", "OnlineDescriptions");
    }

    public static Map<String, Map<String, ?>> getCategories() {
        return (Map<String, Map<String, ?>>) CustomizeConfig.getMap("Themes", "Categories");
    }

    public static Map<String, ?> getCategoriesByKey(String key) {
        return getCategories().get(key);
    }

    public static List<ThemeCategoryInfo> getCategoryInfoList() {
        return ThemeCategoryInfo.ofConfigList(getCategories());
    }

    public static Map<String, Map<String, ?>> getLauncherPromotions() {
        return (Map<String, Map<String, ?>>) CustomizeConfig.getMap("Themes", "LauncherPromotions");
    }

    public static Map<String, ?> getLauncherPromotionsByKey(String key) {
        Map<String, Map<String, ?>> map = getLauncherPromotions();
        if (map != null) {
            return map.get(key);
        } else {
            return null;
        }
    }

    public static Map<String, ?> getPush() {
        return CustomizeConfig.getMap("Themes", "Push");
    }

    public static int getOnceUpdateCount() {
        return AcbMapUtils.getInteger(getThemes(), "OnceUpdateCount");
    }

    public static int getTopThemeId() {
        return AcbMapUtils.getInteger(getThemes(), "TopThemeId");
    }

    public static List<String> getPackagePatterns() {
        return (List<String>) CustomizeConfig.getList("Themes", "PackagePatterns");
    }

    public static List<String> getRegionalPromotion() {
        return (List<String>) CustomizeConfig.getList("Themes", "RegionalPromotion");
    }

    public static List<String> getVariantPromotion() {
        return (List<String>) getThemes().get("VariantPromotion");
    }

    public static int getMaxPushTimes() {
        try {
            return AcbMapUtils.getInteger(getPush(), "Times");
        } catch (Exception e) {
            return -1;
        }
    }

    public static Map<String, Integer> getTagWeights() {
        if (!sTagWeights.isEmpty()) {
            return sTagWeights;
        }
        Map<String, ?> tagWeight = CustomizeConfig.getMap("Themes", "TagWeights");
        Map<String, Integer> special = (Map<String, Integer>) tagWeight.get("Special");
        Map<String, Integer> categories = (Map<String, Integer>) tagWeight.get("Categories");
        Map<String, Integer> color = (Map<String, Integer>) tagWeight.get("Colors");
        sTagWeights.putAll(special);
        sTagWeights.putAll(categories);
        sTagWeights.putAll(color);
        return sTagWeights;
    }

    private static int getTagWeight(String name) {
        Integer weight = getTagWeights().get(name);
        if (weight == null) {
            return 0;
        }
        return weight;
    }

    public static List<ThemeInfo> getRecommendThemes(ThemeInfo themeInfo) {
        List<String> tags = themeInfo.tags;
        List<ThemeInfo> resultList = new ArrayList<>();
        List<ThemeInfo> allThemes = getAllThemes(false);
        for (ThemeInfo theme : allThemes) {
            int weight = 0;
            for (String tag : tags) {
                for (String themeTag : theme.tags) {
                    if (tag.equals(themeTag)) {
                        weight += getTagWeight(tag);
                        break;
                    }
                }
            }
            if (weight > 0 && !theme.themeName.equals(themeInfo.themeName)) {
                theme.weight = weight;
                insetThemeByTagWeight(resultList, theme);
            }
        }
        return resultList;
    }

    private static void insetThemeByTagWeight(List<ThemeInfo> themeInfoList, ThemeInfo theme) {
        if (themeInfoList.isEmpty()) {
            themeInfoList.add(theme);
            return;
        }
        final int size = themeInfoList.size();
        boolean isAdded = false;
        for (int i = 0; i < size; i++) {
            if (theme.weight > themeInfoList.get(i).weight) {
                isAdded = true;
                themeInfoList.add(i, theme);
                break;
            }
        }
        if (!isAdded) {
            themeInfoList.add(theme);
        }
    }

    public static ThemeInfo getThemeInfo(String packageName) {
        return ThemeInfo.ofConfig(packageName, getOnlineDescriptions());
    }

}

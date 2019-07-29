package com.honeycomb.colorphone.customize;

import android.util.SparseArray;


import com.acb.utils.Utils;

import net.appcloudbox.common.utils.AcbMapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by lz on 4/21/17.
 */

public class ThemeCategoryInfo {

    public String identifier;
    public String bannerUrl;
    public String categoryName;
    public int index;
    public SparseArray<String> categoryThemes = new SparseArray<>();
    public List<String> mThemes;

    public static ThemeCategoryInfo ofConfig(Map<String, ?> category) {
        ThemeCategoryInfo info = new ThemeCategoryInfo();
        try {
            info.categoryName = Utils.getMultilingualString(category, "CategoryName");
            info.bannerUrl = (String) category.get("BannerUrl");
            info.index = AcbMapUtils.getInteger(category, "Index");

            Map<String, Integer> themes = (Map<String, Integer>) category.get("CategoryThemes");
            for (int i = 0; i < themes.size(); i++) {
                info.categoryThemes.put(AcbMapUtils.getInteger(themes, (String) themes.keySet().toArray()[i]),
                        (String) themes.keySet().toArray()[i]);
            }

            info.mThemes = (List<String>) category.get("Themes");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static List<ThemeCategoryInfo> ofConfigList(Map<String, Map<String, ?>> categories) {
        List<ThemeCategoryInfo> themeCategoryInfoList = new ArrayList<>();
        for (Map.Entry<String, Map<String, ?>> entry : categories.entrySet()) {
            ThemeCategoryInfo categoryInfo = ThemeCategoryInfo.ofConfig(entry.getValue());
            categoryInfo.identifier = entry.getKey();
            themeCategoryInfoList.add(categoryInfo);
        }

        Collections.sort(themeCategoryInfoList, (t1, t2) -> t1.index - t2.index);
        return themeCategoryInfoList;
    }

    public static String getCategoryByPkgName(Map<String, Map<String, ?>> categories, String pkgName) {
        if (pkgName == null) return null;

        List<ThemeCategoryInfo> themeCategoryInfoList = ofConfigList(categories);
        for (int i = 0; i < themeCategoryInfoList.size(); i++) {
            if (themeCategoryInfoList.get(i).categoryThemes.indexOfValue(pkgName) >= 0) {
                return themeCategoryInfoList.get(i).categoryName;
            }
        }

        return null;
    }
}

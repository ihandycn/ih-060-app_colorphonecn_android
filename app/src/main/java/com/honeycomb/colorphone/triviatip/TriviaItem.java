package com.honeycomb.colorphone.triviatip;

import com.acb.utils.Utils;

import com.ihs.commons.utils.HSMapUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TriviaItem implements Serializable {

    int id;
    String desc;
    String imgUrl;
    String title;
    String headLine;
    String button;

    static List<TriviaItem> fromConfig(Map<String, ?> configMap) {
        List<?> configItems = HSMapUtils.getList(configMap,  "ContentList");
        Map<String, ?> btn = HSMapUtils.getMap(configMap,  "Btn");
        Map<String, String> headLine = (Map<String, String>) HSMapUtils.getMap(configMap, "Headline");
        if (configItems != null) {
            List<TriviaItem> triviaItems = new ArrayList<>(configItems.size());
            for (Object item : configItems) {
                try {
                    Map<String, ?> configItem = (Map<String, ?>) item;
                    TriviaItem triviaItem = new TriviaItem();
                    triviaItem.id = HSMapUtils.optInteger(configItem, 0, "id");
                    triviaItem.desc = Utils.getMultilingualString(configItem, "desc");
                    triviaItem.title = Utils.getMultilingualString(configItem, "title");
                    triviaItem.imgUrl = HSMapUtils.optString(configItem, "", "image");
                    triviaItem.headLine = Utils.getStringForCurrentLanguage(headLine);
                    if (btn.size() == 1) {
                        triviaItem.button = Utils.getStringForCurrentLanguage((Map<String, String>) btn);
                    } else if (btn.containsKey("Btn5")) {
                        triviaItem.button = Utils.getMultilingualString(btn, "Btn5");
                    }
                    triviaItems.add(triviaItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return triviaItems;
        }
        return new ArrayList<>();
    }
}

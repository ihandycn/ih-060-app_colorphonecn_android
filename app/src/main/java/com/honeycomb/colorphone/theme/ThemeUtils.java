package com.honeycomb.colorphone.theme;

import com.honeycomb.colorphone.http.bean.AllCategoryBean;
import com.ihs.commons.config.HSConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThemeUtils {

    private final static String ICON_ACCEPT = "http://cdn.ihandysoft.cn/light2019/apps/apkcolorphone/resource/thumbnail/defaultbutton/acb_phone_call_answer.png";
    private final static String ICON_REJECT = "http://cdn.ihandysoft.cn/light2019/apps/apkcolorphone/resource/thumbnail/defaultbutton/acb_phone_call_refuse.png";

    @SuppressWarnings("unchecked")
    public static List<AllCategoryBean.CategoryItem> getCategoryItemsFromConfig() {
        List<Map<String, ?>> listMap = (List<Map<String, ?>>) HSConfig.getList("Application", "Theme", "Category");

        if (listMap == null) {
            return null;
        }

        List<AllCategoryBean.CategoryItem> result = new ArrayList<>(listMap.size());
        for (Map<String, ?> map : listMap) {
            AllCategoryBean.CategoryItem item = new AllCategoryBean.CategoryItem();
            item.setId((String) map.get("id"));
            item.setName((String) map.get("name"));
            result.add(item);
        }

        return result;
    }

    public static String getRejectIconFromConfig() {
        return HSConfig.optString(ICON_REJECT, "Application", "Theme", "Icon", "RejectIcon");
    }

    public static String getAcceptIconFromConfig() {
        return HSConfig.optString(ICON_ACCEPT, "Application", "Theme", "Icon", "AcceptIcon");
    }
}

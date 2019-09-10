package com.colorphone.ringtones.module;

import android.text.TextUtils;

import com.colorphone.ringtones.bean.ColumnBean;

import java.io.Serializable;

public class Banner implements Serializable {

    private static final String TYPE_AD = "2003002";
    /**
     * Column id
     */
    private String columnId;
    private String name;
    private String type;
    private String imgUrl;
    private String linkUrl;

    public static Banner valueOf(ColumnBean columnBean) {
        Banner banner = new Banner();
        banner.columnId = columnBean.getTargetid();
        banner.imgUrl = columnBean.getSimg();
        banner.name = columnBean.getName();
        banner.type = columnBean.getType();
        banner.linkUrl = columnBean.getLinkurl();
        return banner;
    }

    public String getName() {
        return name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    /**
     * For column type
     */
    public String getColumnId() {
        return columnId;
    }

    /**
     * For ad type
     */
    public String getLinkUrl() {
        return linkUrl;
    }

    public boolean isAdBannner() {
        return TextUtils.equals(TYPE_AD, type);
    }

}

package com.honeycomb.colorphone.news;

import java.util.List;

public class NewsResultBean {
    int totalItems;
    List<NewsBean> content;

    @Override public String toString() {
        return "NewsResultBean{" +
                "totalItems=" + totalItems +
                ", content=" + content +
                '}';
    }
}

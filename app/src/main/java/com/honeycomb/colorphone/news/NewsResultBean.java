package com.honeycomb.colorphone.news;

import java.util.ArrayList;
import java.util.List;

public class NewsResultBean {
    int totalItems;
    List<NewsBean> content;

    NewsResultBean() {
        content = new ArrayList<>();
    }

    @Override public String toString() {
        return "NewsResultBean{" +
                "totalItems=" + totalItems +
                ", content=" + content +
                '}';
    }
}

package com.honeycomb.colorphone.news;

import com.google.gson.JsonObject;

import java.util.List;

public class NewsContentData {
    List<NewsItem> items;
    JsonObject articles;

    @Override public String toString() {

        return "NewsContentData{" +
                "items=" + items +
//                ", articles=" + articles +
                '}';
    }
}

package com.honeycomb.colorphone.news;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

public class NewsResultBean {
    String msg;
    int res;
    UserInfo userinfo;
    NewsData data;
    NewsDataResult result;

    List<NewsArticle> articlesList = new ArrayList<>();

    NewsResultBean() {
    }

    void parseArticles() {
        Gson gson = new Gson();

        JsonObject json;
        NewsArticle article;
        if (data != null && data.data != null && data.data.items != null && data.data.items.size() > 0) {
            for (NewsItem item : data.data.items) {
                json = data.data.articles.getAsJsonObject(item.id);
                article = gson.fromJson(json, NewsArticle.class);
                articlesList.add(article);
            }
        } else {
            HSLog.w(NewsManager.TAG, "parseArticles items is null ! ");
        }
    }

    @Override public String toString() {
        return "NewsResultBean{" +
                "msg='" + msg + '\'' +
                ", res=" + res +
                ", userinfo=" + userinfo +
                ", data=" + data +
                ", result=" + result +
                ", articlesList=" + articlesList +
                '}';
    }
}

package com.honeycomb.colorphone.news;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

public class NewsResultBean {
    int adSize;
    int adOffset = 1;

    String msg;
    int res;
    UserInfo userinfo;
    NewsData data;

    List<NewsArticle> articlesList = new ArrayList<>();

    NewsResultBean() {
    }

    void parseArticles() {
        if (data != null && data.status >= 0) {
            Gson gson = new Gson();

            JsonObject json;
            NewsArticle article;
            if (data.data != null) {
                NewsContentData ncDate = gson.fromJson(data.data, NewsContentData.class);

                for (NewsItem item : ncDate.items) {
                    json = ncDate.articles.getAsJsonObject(item.id);
                    article = gson.fromJson(json, NewsArticle.class);
                    articlesList.add(article);
                }
            } else {
                HSLog.w(NewsManager.TAG, "parseArticles items is null ! ");
            }
        } else {
            HSLog.w(NewsManager.TAG, "result status: " + (data != null ? data.status : "null") + "  msg: " + (data != null ? data.message : "null"));
        }
    }

    @Override public String toString() {
        parseArticles();

        return "NewsResultBean{" +
                "msg='" + msg + '\'' +
                ", res=" + res +
                ", userinfo=" + userinfo +
                ", data=" + data +
                ", articlesList=" + articlesList +
                '}';
    }
}

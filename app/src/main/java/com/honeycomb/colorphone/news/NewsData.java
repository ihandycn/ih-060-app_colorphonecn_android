package com.honeycomb.colorphone.news;

import com.google.gson.JsonElement;

public class NewsData {
    JsonElement data;
    int status;
    String message;
    NewsDataResult result;

    @Override public String toString() {
        return "NewsData{" +
                "data=" + data +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}

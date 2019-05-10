package com.honeycomb.colorphone.news;

public class Thumbnail {
    private String url;
    int width;
    int height;
    String type;

    public String getUrl() {
        return url + "&width=" + width + "&height=" + height;
    }

    @Override public String toString() {
        return "Thumbnail{" +
                "url='" + url + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", type='" + type + '\'' +
                '}';
    }
}

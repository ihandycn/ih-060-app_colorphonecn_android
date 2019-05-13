package com.honeycomb.colorphone.news;

public class Thumbnail {
    private String url;
    int width;
    int height;
    String type;

    public String getUrl() {
        int h = width * 71 / 106;
        return url + "&width=" + Math.min(width, this.width) + "&height=" + Math.min(h, height);
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

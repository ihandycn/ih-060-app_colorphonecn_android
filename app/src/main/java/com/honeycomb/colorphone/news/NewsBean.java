package com.honeycomb.colorphone.news;

public class NewsBean {
    String contentId;
    String title;
    String summary;
    long publishedAt;
    String contentURL;
    String contentSourceDisplay;
    int views;

    ImagesBean images;

    @Override public String toString() {
        return "NewsBean{" +
                "contentId='" + contentId + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", contentSourceDisplay='" + contentSourceDisplay + '\'' +
                ", publishedAt=" + publishedAt +
                ", contentURL='" + contentURL + '\'' +
                ", images=" + images +
                ", views=" + views +
                '}';
    }
}

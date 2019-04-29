package com.honeycomb.colorphone.news;

import java.util.List;

public class NewsBean {
    String contentId;
    String title;
    String summary;
    List<String> categoriesEnglish;
    long publishedAt;
    String contentURL;
    String contentSourceDisplay;
    String contentSourceLogo;
    int views;

    ImagesBean images;

    String thumbnail;
    long length;
    int totalViews;

    VideoPreview videoPreview;


    @Override public String toString() {
        return "NewsBean{" +
                "contentId='" + contentId + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", categoriesEnglish='" + categoriesEnglish + '\'' +
                ", publishedAt=" + publishedAt +
                ", contentURL='" + contentURL + '\'' +
                ", contentSourceDisplay='" + contentSourceDisplay + '\'' +
                ", contentSourceLogo='" + contentSourceLogo + '\'' +
                ", views=" + views +
                ", images=" + images +
                ", thumbnail='" + thumbnail + '\'' +
                ", length=" + length +
                ", totalViews=" + totalViews +
                ", videoPreview=" + videoPreview +
                '}';
    }
}

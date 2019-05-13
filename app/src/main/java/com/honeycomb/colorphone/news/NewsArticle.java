package com.honeycomb.colorphone.news;

import java.util.List;

public class NewsArticle {
    String recoid;
    String id;
    String title;
    String url;
    String summary;
    List<Thumbnail> thumbnails;

    int item_type;
    int style_type;
    long publish_time;
    String source_name;

    List<DislikeInfo> dislike_infos;
    boolean enable_dislike;

    @Override public String toString() {
        return "NewsArticle{" +
                "recoid='" + recoid + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", summary='" + summary + '\'' +
                ", thumbnails=" + thumbnails +
                ", item_type=" + item_type +
                ", style_type=" + style_type +
                ", publish_time=" + publish_time +
                ", source_name='" + source_name + '\'' +
                ", dislike_infos=" + dislike_infos +
                ", enable_dislike=" + enable_dislike +
                '}';
    }
}

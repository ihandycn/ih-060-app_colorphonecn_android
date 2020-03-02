package com.colorphone.smartlocker.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TouTiaoFeedBean implements IFeedBean {

    public static class ImageUrlBean {
        public String url;
    }

    public String title; // 新闻的标题

    public String source; // 新闻的来源

    @SerializedName("is_stick")
    public boolean isStick; // 是否是置顶新闻

    @SerializedName("article_url")
    public String articleUrl; // 详情页地址,必须使用该字段访问文章详情页

    @SerializedName("publish_time")
    public long publishTime; // 新闻发布时间,时间戳

    @SerializedName("has_video")
    public boolean hasVideo; // 是否是视频,true：视频新闻，false：普通新闻。 当前新闻为视频时，客户端可以在封面图上加一个播放的图标【封面图本身不带有播放图标】，更容易辨认视频新闻，具体可参考今日头条app做法

    @SerializedName("comment_count")
    public long commentCount; // 评论的数量

    @SerializedName("cover_mode")
    public long coverMode; // 封面类型,0：无封面；1：大图；2：三图；3：右图

    @SerializedName("cover_image_list")
    public List<ImageUrlBean> coverImageList; // 封面图

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getBrief() {
        return "";
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public boolean isStick() {
        return isStick;
    }

    @Override
    public String getArticleUrl() {
        return articleUrl;
    }

    @Override
    public long getPublishTime() {
        return publishTime;
    }

    @Override
    public boolean hasVideo() {
        return hasVideo;
    }

    @Override
    public long getCommentCount() {
        return commentCount;
    }

    @Override
    public List<String> getCoverImageList() {
        List<String> imageUrls = new ArrayList<>();
        for (ImageUrlBean imageUrlBean : coverImageList) {
            imageUrls.add(imageUrlBean.url);
        }

        return imageUrls;
    }
}

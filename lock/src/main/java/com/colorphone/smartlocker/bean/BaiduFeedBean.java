package com.colorphone.smartlocker.bean;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.colorphone.smartlocker.utils.DateUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hao.li on 2019/9/16.
 */

@Keep
public class BaiduFeedBean implements IFeedBean {

    @Keep
    public class BaiduFeedBeanInner {
        public String type;
        private String data;
        private int commentCounts;
    }

    private static final String BAIDU_FEED_ITEM_TYPE_NEWS = "news";
    private static final String BAIDU_FEED_ITEM_TYPE_VIDEO = "video";

    private BaiduFeedBeanInner baiduFeedBeanInner;
    private BaiduNewsBean baiduNewsItemData;
    private BaiduVideoBean baiduVideoItemData;

    public BaiduFeedBean(String json) {
        baiduFeedBeanInner = new Gson().fromJson(json, new TypeToken<BaiduFeedBeanInner>() {
        }.getType());

        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                baiduNewsItemData = new Gson().fromJson(baiduFeedBeanInner.data, new TypeToken<BaiduNewsBean>() {
                }.getType());
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                baiduVideoItemData = new Gson().fromJson(baiduFeedBeanInner.data, new TypeToken<BaiduVideoBean>() {
                }.getType());
                break;
            default:
                break;
        }
    }

    @Override
    public String getTitle() {
        String title;
        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                title = baiduNewsItemData.title;
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                title = baiduVideoItemData.title;
                break;
            default:
                title = "";
                break;
        }

        return title;
    }

    @Override
    public String getBrief() {
        String brief;
        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                brief = baiduNewsItemData.brief;
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                brief = baiduVideoItemData.brief;
                break;
            default:
                brief = "";
                break;
        }

        return brief;
    }

    @Override
    public String getSource() {
        String source;
        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                source = baiduNewsItemData.source;
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                source = baiduVideoItemData.source.name;
                break;
            default:
                source = "";
                break;
        }
        return source;
    }

    @Override
    public boolean isStick() {
        return false;
    }

    @TouTiaoFeedUtils.NewsType
    public int getNewsType() {
        int newsType;
        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                List<String> images = baiduNewsItemData.images;
                if (images != null && images.size() >= 3) {
                    int randomStyle = (int) (Math.random() * 10);
                    if (randomStyle >= 7 && randomStyle <= 10) {
                        newsType = TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE;
                    } else {
                        newsType = TouTiaoFeedUtils.COVER_MODE_THREE_IMAGE;
                    }
                } else if (images != null && images.size() == 1) {
                    newsType = TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE;
                } else {
                    newsType = TouTiaoFeedUtils.COVER_MODE_NO_IMAGE;
                }
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                newsType = TouTiaoFeedUtils.COVER_MODE_BIG_IMAGE;
                break;
            default:
                newsType = TouTiaoFeedUtils.COVER_MODE_NO_IMAGE;
                break;
        }

        return newsType;
    }


    @Override
    public String getArticleUrl() {
        String detailUrl;
        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                detailUrl = baiduNewsItemData.detailUrl;
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                detailUrl = baiduVideoItemData.detailUrl;
                break;
            default:
                detailUrl = "";
                break;
        }
        return detailUrl;
    }

    @Override
    public long getPublishTime() {
        String time = baiduNewsItemData != null ? baiduNewsItemData.updateTime : baiduVideoItemData.updateTime;
        return DateUtils.dateToTimeStamp(time, "yyyy-MM-dd HH:mm:ss") / 1000;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }

    @Override
    public long getCommentCount() {
        return baiduFeedBeanInner.commentCounts;
    }

    @NonNull
    @Override
    public List<String> getCoverImageList() {
        List<String> imageList = new ArrayList<>();
        switch (baiduFeedBeanInner.type) {
            case BAIDU_FEED_ITEM_TYPE_NEWS:
                imageList = baiduNewsItemData.images;
                break;
            case BAIDU_FEED_ITEM_TYPE_VIDEO:
                String thumbUrl = baiduVideoItemData.thumbUrl;
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                imageList.add(thumbUrl);
                break;
        }
        return imageList;
    }
}

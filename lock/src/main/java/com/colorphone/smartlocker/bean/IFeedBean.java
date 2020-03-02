package com.colorphone.smartlocker.bean;

import java.util.List;

public interface IFeedBean {

    String getTitle();

    String getBrief();

    String getSource();

    boolean isStick();

    String getArticleUrl();

    long getPublishTime();

    boolean hasVideo();

    long getCommentCount();

    List<String> getCoverImageList();
}

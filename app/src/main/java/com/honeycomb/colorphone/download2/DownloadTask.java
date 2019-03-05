package com.honeycomb.colorphone.download2;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadTask {

    private AtomicInteger mFinishCount = new AtomicInteger();
    List<Downloader.DownloadItem> mDownloadItems;

    private String mDirectory;

    private boolean mCancelable;
    private boolean mLimitedByLru;
    private boolean mIsLifo;
    private boolean mIslimited;
    private int mLimitedCount;
    private String mTag;


    protected Downloader.DownloadTaskListener mListener;

    DownloadTask() {
    }

    public DownloadTask(@Nullable Downloader.DownloadTaskListener listener) {
        mListener = listener;
        mDownloadItems = new ArrayList<>();
    }

    public void add(Downloader.DownloadItem item) {
        mDownloadItems.add(item);
    }

    public boolean isEmpty() {
        return mDownloadItems.isEmpty();
    }

    public List<Downloader.DownloadItem> getDownloadItems() {
        return mDownloadItems;
    }

    Downloader.DownloadTaskListener getDownloadListener() {
        return mListener;
    }

    void finishOne() {
        mFinishCount.incrementAndGet();
    }

    int getFinishCount() {
        return mFinishCount.get();
    }

    public boolean isTaskFinish() {
        return mFinishCount.get() == mDownloadItems.size();
    }

    public void reset() {
        mFinishCount.set(0);
    }

    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
    }

    public boolean isCancelable() {
        return mCancelable;
    }

    public void setLimitedByLru(boolean limitedByLru) {
        mLimitedByLru = limitedByLru;
    }

    public boolean isLimitedByLru() {
        return mLimitedByLru;
    }

    public void setDirectory(String directory) {
        mDirectory = directory;
    }

    public String getDirectory() {
        return mDirectory;
    }

    public void setLifo(boolean lifo) {
        mIsLifo = lifo;
    }

    public boolean isLifo() {
        return mIsLifo;
    }

    public void setIsLimitedByCount(boolean limited) {
        mIslimited = limited;
    }

    public boolean isLimitedByCount() {
        return mIslimited;
    }

    public void setLimitedCount(int limitedCount) {
        mLimitedCount = limitedCount;
    }

    public int getLimitedCount() {
        return mLimitedCount;
    }

    public void setLimitedGroupTag(String tag) {
        mTag = tag;
    }

    public String getLimitedGroupTag() {
        return mTag;
    }
}

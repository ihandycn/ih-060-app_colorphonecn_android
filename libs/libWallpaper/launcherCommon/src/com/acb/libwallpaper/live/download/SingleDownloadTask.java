package com.acb.libwallpaper.live.download;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SingleDownloadTask extends DownloadTask {

    public SingleDownloadTask(Downloader.DownloadItem downloadItem) {
        this(downloadItem, null);
    }

    public SingleDownloadTask(Downloader.DownloadItem downloadItem, @Nullable SingleTaskListener listener) {
        super();
        mDownloadItems = new ArrayList<>(1);
        mDownloadItems.add(downloadItem);
        mListener = new Downloader.DownloadTaskListener() {

            @Override
            public void onStart() {
                if (listener != null) {
                    listener.onStart();
                }
            }

            @Override
            public void onProgress(int total, int current, boolean currentSuccess, Downloader.DownloadItem item) {
                if (currentSuccess && listener != null) {
                    listener.onSuccess(item);
                } else if (listener != null) {
                    listener.onFailed(item);
                }
            }

            @Override
            public void onComplete(int total) {

            }
        };
    }

    @Override
    public void add(Downloader.DownloadItem item) {
        throw new RuntimeException("SingleDownloadTask can not add DownloadItem");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<Downloader.DownloadItem> getDownloadItems() {
        return mDownloadItems;
    }

    public interface SingleTaskListener {

        void onStart();

        void onSuccess(Downloader.DownloadItem item);

        void onFailed(Downloader.DownloadItem item);
    }
}

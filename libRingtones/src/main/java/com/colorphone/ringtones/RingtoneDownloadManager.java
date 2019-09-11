package com.colorphone.ringtones;

import android.text.TextUtils;

import com.colorphone.ringtones.download2.DownloadTask;
import com.colorphone.ringtones.download2.Downloader;
import com.colorphone.ringtones.module.Ringtone;
import com.colorphone.ringtones.view.BaseRingtoneListAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sundxing
 */
public class RingtoneDownloadManager {
    private static RingtoneDownloadManager sRingtoneDownloadManager;

    public static RingtoneDownloadManager getInstance() {
        if (sRingtoneDownloadManager == null) {
            sRingtoneDownloadManager = new RingtoneDownloadManager();
        }
        return sRingtoneDownloadManager;
    }

    protected Map<String, DownloadTask> mDownloadTaskMap = new HashMap<>();

    public void download(Ringtone ringtone, int pos) {
        DownloadTask downloadTask = new DownloadTask(null);
        downloadTask.add(new Downloader.DownloadItem(ringtone.getRingtoneUrl(), ringtone.getFilePath(),
                new RingtoneDownloadListener() {
            @Override
            public void onStart(Downloader.DownloadItem item) {
                if (mRingtoneDownloadListener != null) {
                    mRingtoneDownloadListener.onStart(item);
                }
            }

            @Override
            public void onProgress(Downloader.DownloadItem item, float progress) {
                if (mRingtoneDownloadListener != null) {
                    mRingtoneDownloadListener.onProgress(item, progress);
                }
            }

            @Override
            public void onComplete(Downloader.DownloadItem item) {
                mDownloadTaskMap.remove(item.getUrl());
                if (mRingtoneDownloadListener != null) {
                    mRingtoneDownloadListener.onComplete(item);
                }
            }

            @Override
            public void onCancel(Downloader.DownloadItem item, CancelReason reason) {
                mDownloadTaskMap.remove(item.getUrl());
                if (mRingtoneDownloadListener != null) {
                    mRingtoneDownloadListener.onCancel(item, reason);
                }
            }

            @Override
            public void onFailed(Downloader.DownloadItem item, String errorMsg) {
                mDownloadTaskMap.remove(item.getUrl());
                if (mRingtoneDownloadListener != null) {
                    mRingtoneDownloadListener.onFailed(item, errorMsg);
                }

            }
        }));
        mDownloadTaskMap.put(ringtone.getRingtoneUrl(), downloadTask);
        Downloader.getInstance().download(downloadTask, null);
    }

    private boolean isSameRingtoneSource(Downloader.DownloadItem item, BaseRingtoneListAdapter.ViewHolder viewHolder) {
        String url = item.getUrl();
        Ringtone ringtoneVH = viewHolder.getRingtone();
        if (ringtoneVH != null) {
            return TextUtils.equals(ringtoneVH.getRingtoneUrl(), url);
        }
        return false;
    }

    public boolean isDownloaded(Ringtone ringtone) {
        String filePath = ringtone.getFilePath();
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
    }

    public boolean isDownloading(Ringtone ringtone) {
        return mDownloadTaskMap.containsKey(ringtone.getRingtoneUrl());
    }

    private RingtoneDownloadListener mRingtoneDownloadListener;
    public void listen(RingtoneDownloadListener downloadListener) {
        mRingtoneDownloadListener = downloadListener;
    }

    public static abstract class RingtoneDownloadListener implements Downloader.DownloadItem.DownloadItemListener {

    }

}

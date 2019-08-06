package com.honeycomb.colorphone.customize.livewallpaper;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;

import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download2.DownloadTask;
import com.honeycomb.colorphone.download2.Downloader;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Threads;

import java.io.File;

public class VideoWallpaperLoader extends WallpaperLoader {

    public static final String TAG = VideoWallpaperLoader.class.getSimpleName();

    private String mWallpaperName;
    private String url;
    private Callbacks mCallbacks;
    private DownloadTask mDownloadTask;

    public VideoWallpaperLoader() {
    }

    @Override
    public void setWallpaperName(String wallpaperName) {
        mWallpaperName = wallpaperName;
        url = mWallpaperName;
    }

    @Override
    public String getWallpaperName() {
        return mWallpaperName;
    }


    @Override
    @MainThread
    public void load(Callbacks callbacks) {
        mCallbacks = callbacks;
        if (LiveWallpaperConsts.DEBUG_PARTICLE_FLOW_WALLPAPER) {
            fireCallbackForSuccess();
        } else {
            loadVideoWallpaper();
        }
    }

    private void loadVideoWallpaper() {
        String path = TasksManager.getVideoWallpaperPath(url);
        boolean loadNeeded = auditLocalFilesForVideoWallpaper(path);
        if (loadNeeded) {
            // Download resource pack from server
            downloadAndDecryptVideoWallpaper(url, path);
        } else {
            fireCallbackForSuccess();
        }
    }

    private boolean auditLocalFilesForVideoWallpaper(String path) {
        File videoFile = new File(path);
        if (videoFile.exists()) {
            HSLog.d(TAG, videoFile + " already exists");
            return false;
        } else {
            HSLog.d(TAG, videoFile + " does NOT exist");
            return true;
        }
    }

    private void downloadAndDecryptVideoWallpaper(String url, String path) {
        DownloadTask downloadTask = new DownloadTask(null);
        mDownloadTask = downloadTask;
        Downloader.DownloadItem.DownloadItemListener itemListener = new Downloader.DownloadItem.DownloadItemListener() {
            @Override
            public void onStart(Downloader.DownloadItem item) {
                HSLog.d(TAG, "Download started");
            }

            @Override
            public void onProgress(Downloader.DownloadItem item, float progress) {
                if (mCallbacks != null) {
                    mCallbacks.onProgress(progress);
                }
            }

            @Override
            public void onComplete(Downloader.DownloadItem item) {
                HSLog.d(TAG, "Download completed: " + item);
                Threads.postOnThreadPoolExecutor(() -> {
                    try {
                        fireCallbackForSuccess();
                    } catch (Exception e) {
                        fireCallbackForFailure("Error decrypting video: " + e);
                    }
                });
            }

            @Override
            public void onCancel(Downloader.DownloadItem item, CancelReason reason) {
                HSLog.d(TAG, "Download cancelled: " + reason);
            }

            @Override
            public void onFailed(Downloader.DownloadItem item, String errorMsg) {
                fireCallbackForFailure("Error downloading res pack: " + errorMsg);
            }
        };
        downloadTask.add(new Downloader.DownloadItem(url,
                path, itemListener, null));
        if (!Networks.isNetworkAvailable(-1)) {
            fireCallbackForFailure("No network");
            return;
        }
        Downloader.getInstance().download(downloadTask, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void cancel() {
        mCallbacks = null;
        if (mDownloadTask != null) {
            DownloadTask taskToCancel = mDownloadTask;
            mDownloadTask = null;
            Downloader.getInstance().cancel(taskToCancel);
        }
    }

    private void fireCallbackForSuccess() {
        Threads.runOnMainThread(() -> {
            HSLog.w(TAG, "Successfully loaded wallpaper resources");
            if (mCallbacks != null) {
                mCallbacks.onLiveWallpaperLoaded(null);
            }
        });
    }

    private void fireCallbackForFailure(String message) {
        Threads.runOnMainThread(() -> {
            HSLog.w(TAG, "Failed to load wallpaper resources: " + message);
            if (mCallbacks != null) {
                mCallbacks.onLiveWallpaperLoadFailed(message);
            }
        });
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

}

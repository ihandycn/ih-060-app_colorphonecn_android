package com.honeycomb.colorphone.download;

import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.NetUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.superapps.util.Preferences;

public class FileDownloadMultiListener extends FileDownloadSampleListener {

    private static FileDownloadMultiListener instance;

    public static FileDownloadMultiListener getDefault() {
        if (instance == null) {
            instance = new FileDownloadMultiListener();
        }
        return instance;
    }

    private SparseArray<DownloadStateListener> mDownloadStateListeners = new SparseArray<>();
    private SparseBooleanArray mTaskProgressList = new SparseBooleanArray();

    public void addStateListener(int taskId, DownloadStateListener listener) {
        mDownloadStateListeners.put(taskId, listener);
    }

    public void removeStateListener(int taskId) {
        mDownloadStateListeners.remove(taskId);
    }

    private DownloadHolder checkCurrentHolder(final BaseDownloadTask task) {
        final DownloadHolder tag = (DownloadHolder) task.getTag();
        if (tag == null) {
            return null;
        }
        if (tag.getId() != task.getId()) {
            return null;
        }

        return tag;
    }

    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.pending(task, soFarBytes, totalBytes);

        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateDownloading(FileDownloadStatus.pending, soFarBytes, totalBytes);
        }

        final DownloadHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateDownloading(FileDownloadStatus.pending, task.getLargeFileSoFarBytes()
                , task.getLargeFileTotalBytes());
    }

    @Override
    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateDownloading(FileDownloadStatus.pending, soFarBytes, totalBytes);
        }

        final DownloadHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateDownloading(FileDownloadStatus.connected, task.getLargeFileSoFarBytes()
                , task.getLargeFileTotalBytes());
    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.progress(task, soFarBytes, totalBytes);
        mTaskProgressList.put(task.getId(), true);
        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateDownloading(FileDownloadStatus.pending, soFarBytes, totalBytes);
        }

        final DownloadHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateDownloading(FileDownloadStatus.progress, task.getLargeFileSoFarBytes()
                , task.getLargeFileTotalBytes());
    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        super.error(task, e);
        HSLog.e("BaseDownloadTask", "fail to download: " + task.getUrl());
        if (BuildConfig.DEBUG && e != null) {
            e.printStackTrace();
            Toast.makeText(HSApplication.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Preferences.get(Constants.PREF_FILE_DEFAULT).doLimitedTimes(new Runnable() {
            @Override
            public void run() {
                Analytics.logEvent("ColorPhone_Download_Error",
                        "Reason", e != null ? e.getMessage() : "Unknown",
                        "Network", NetUtils.getNetWorkStateName());
            }
        }, "event_download_error", 3);

        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateNotDownloaded(FileDownloadStatus.pending, task.getLargeFileSoFarBytes()
                    , task.getLargeFileTotalBytes());
        }

        final DownloadHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes()
                , task.getLargeFileTotalBytes());

    }

    @Override
    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.paused(task, soFarBytes, totalBytes);

        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateNotDownloaded(FileDownloadStatus.pending, soFarBytes, totalBytes);
        }

        final DownloadHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
        TasksManager.getImpl().removeTaskForViewHolder(task.getId());
    }

    @Override
    protected void completed(BaseDownloadTask task) {
        super.completed(task);
        boolean progressFlag = mTaskProgressList.get(task.getId());
        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());

        HSLog.d("ThemeSelectorAdapter", "complete download task");
        if (stateListener != null) {
            stateListener.updateDownloaded(progressFlag);
        }
        final TasksManagerModel model = TasksManager.getImpl().getById(task.getId());
        if (model != null) {
            model.setTaskStatus(FileDownloadStatus.completed);
            ColorPhoneApplication.getConfigLog().getEvent().onThemeDownloadFinish(model.getName().toLowerCase());
        }

        final DownloadHolder tag = checkCurrentHolder(task);
        if (tag != null) {
            tag.updateDownloaded(progressFlag);
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }
        mTaskProgressList.delete(task.getId());
    }
}

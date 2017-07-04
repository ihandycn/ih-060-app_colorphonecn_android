package com.honeycomb.colorphone.download;

import android.util.SparseArray;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

public class FileDownloadMultiListener extends FileDownloadSampleListener {

    private static FileDownloadMultiListener instance;

    public static FileDownloadMultiListener getDefault() {
        if (instance == null) {
            instance = new FileDownloadMultiListener();
        }
        return instance;
    }

    public boolean progressFlag;
    private SparseArray<DownloadStateListener> mDownloadStateListeners = new SparseArray<>();

    public void addStateListener(int taskId, DownloadStateListener listener) {
        mDownloadStateListeners.put(taskId, listener);
    }

    public void removeStateListener(int taskId) {
        mDownloadStateListeners.remove(taskId);
    }

    private DownloadViewHolder checkCurrentHolder(final BaseDownloadTask task) {
        final DownloadViewHolder tag = (DownloadViewHolder) task.getTag();
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

        final DownloadViewHolder tag = checkCurrentHolder(task);
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

        final DownloadViewHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateDownloading(FileDownloadStatus.connected, task.getLargeFileSoFarBytes()
                , task.getLargeFileTotalBytes());
    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.progress(task, soFarBytes, totalBytes);
        progressFlag = true;
        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateDownloading(FileDownloadStatus.pending, soFarBytes, totalBytes);
        }

        final DownloadViewHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateDownloading(FileDownloadStatus.progress, task.getLargeFileSoFarBytes()
                , task.getLargeFileTotalBytes());
    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        super.error(task, e);
        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateNotDownloaded(FileDownloadStatus.pending, task.getLargeFileSoFarBytes()
                    , task.getLargeFileTotalBytes());
        }

        final DownloadViewHolder tag = checkCurrentHolder(task);
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

        final DownloadViewHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
        TasksManager.getImpl().removeTaskForViewHolder(task.getId());
    }

    @Override
    protected void completed(BaseDownloadTask task) {
        super.completed(task);
        DownloadStateListener stateListener = mDownloadStateListeners.get(task.getId());
        if (stateListener != null) {
            stateListener.updateDownloaded(progressFlag);
        }

        final DownloadViewHolder tag = checkCurrentHolder(task);
        if (tag == null) {
            return;
        }
        tag.updateDownloaded(progressFlag);
        TasksManager.getImpl().removeTaskForViewHolder(task.getId());
    }
}

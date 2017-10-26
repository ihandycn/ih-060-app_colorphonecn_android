package com.honeycomb.colorphone.download;

import android.text.TextUtils;
import android.util.SparseArray;

import com.acb.call.themes.Type;
import com.acb.call.utils.FileUtils;
import com.acb.call.utils.Utils;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class TasksManager {

    private static final java.lang.String TAG = TasksManager.class.getSimpleName();


    private final static class HolderClass {
        private final static TasksManager INSTANCE
                = new TasksManager();
    }

    public static TasksManager getImpl() {
        return HolderClass.INSTANCE;
    }

    private TasksManagerDBController dbController;
    private List<TasksManagerModel> modelList;

    private TasksManager() {
        dbController = new TasksManagerDBController();
        modelList = dbController.getAllTasks();
    }

    private SparseArray<BaseDownloadTask> taskSparseArray = new SparseArray<>();

    public BaseDownloadTask getTask(int id) {
        return taskSparseArray.get(id);
    }

    public void addTaskForViewHolder(final BaseDownloadTask task) {
        taskSparseArray.put(task.getId(), task);
    }

    public void removeTaskForViewHolder(final int id) {
        taskSparseArray.remove(id);
    }


    public void releaseTask() {
        int size = taskSparseArray.size();
        for (int i = 0; i < size; i++) {
            BaseDownloadTask task = taskSparseArray.valueAt(i);
            if (task != null) {
                task.setTag(null);
            }
        }
    }

    private FileDownloadConnectListener listener;

    private void registerServiceConnectionListener(final WeakReference<Runnable> taskWeakReference) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener);
        }

        listener = new FileDownloadConnectListener() {

            @Override
            public void connected() {
                Runnable runnable  = taskWeakReference.get();
                if (runnable != null) {
                    runnable.run();
                }
            }

            @Override
            public void disconnected() {
                Runnable runnable  = taskWeakReference.get();
                if (runnable != null) {
                    runnable.run();
                }
            }
        };

        FileDownloader.getImpl().addServiceConnectListener(listener);
    }

    private void unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener);
        listener = null;
    }

    public void onCreate(final WeakReference<Runnable> taskWeakReference) {
        if (!FileDownloader.getImpl().isServiceConnected()) {
            FileDownloader.getImpl().bindService();
            registerServiceConnectionListener(taskWeakReference);
        }
    }

    public void onDestroy() {
        unregisterServiceConnectionListener();
        releaseTask();
    }

    public boolean isReady() {
        return FileDownloader.getImpl().isServiceConnected();
    }


    public TasksManagerModel getByThemeId(int themeId) {
        Type type = Utils.getTypeByThemeId(themeId);
        for (TasksManagerModel model : modelList) {
            if (TextUtils.equals(model.getName(), type.getIdName())) {
                return model;
            }
        }
        return null;
    }

    /**
     *
     * @param id taskId, generate by theme url & path.
     * @return
     */
    public TasksManagerModel getById(final int id) {
        for (TasksManagerModel model : modelList) {
            if (model.getId() == id) {
                return model;
            }
        }

        return null;
    }

    /**
     * @param status Download Status
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    public boolean isDownloaded(final int status) {
        return status == FileDownloadStatus.completed;
    }

    public boolean isDownloading(final int status) {
        return status == FileDownloadStatus.progress || status == FileDownloadStatus.started ||
                status == FileDownloadStatus.connected ;
    }

    public int getStatus(final int id, String path) {
        return FileDownloader.getImpl().getStatus(id, path);
    }

    public long getTotal(final int id) {
        return FileDownloader.getImpl().getTotal(id);
    }

    public long getSoFar(final int id) {
        return FileDownloader.getImpl().getSoFar(id);
    }

    public float getDownloadProgress(final int id) {
        final float percent = getSoFar(id)
                / (float) getTotal(id);
        return percent;
    }

    public int getTaskCounts() {
        return modelList.size();
    }

    public void addTask(Type type) {
        String url = type.getSuggestMediaUrl();
        if (TextUtils.isEmpty(url)) {
            throw new IllegalStateException("Theme type : [ " + type.getIdName() + " ] has not gif url!");
        }
        File file = FileUtils.getMediaDirectory();
        if (file != null) {
            String path = FileDownloadUtils.generateFilePath(file.getAbsolutePath(), type.getSuggestMediaUrl());
            addTask(url, path,  type.getIdName());
        }
    }

    public TasksManagerModel addTask(final String url, String token) {
        return addTask(url, createPath(url), token);
    }

    public TasksManagerModel addTask(final String url, final String path, String token) {
        HSLog.d(TAG, "## Add new task ##:" + url);

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null;
        }

        final int id = FileDownloadUtils.generateId(url, path);
        TasksManagerModel model = getById(id);
        if (model != null) {
            return model;
        }
        final TasksManagerModel newModel = dbController.addTask(url, path, token);
        if (newModel != null) {
            modelList.add(newModel);
        }

        return newModel;
    }

    public String createPath(final String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        return FileDownloadUtils.getDefaultSaveFilePath(url);
    }
}


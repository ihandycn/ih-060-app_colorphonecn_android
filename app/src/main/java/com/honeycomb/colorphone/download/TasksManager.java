package com.honeycomb.colorphone.download;

import android.text.TextUtils;
import android.util.SparseArray;

import com.acb.call.themes.Type;
import com.acb.call.utils.FileUtils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;


public class TasksManager {

    public static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & false;
    private static final java.lang.String TAG = TasksManager.class.getSimpleName();

    private static final String TOKEN_EXTRA_RINGTONE = "ringtone";

    public static void doDownload(TasksManagerModel model, Object tag) {
        if (model != null) {
            FileDownloadListener listener;
            listener = FileDownloadMultiListener.getDefault();

            if (getImpl().getTask(model.getId()) != null) {
                if (DEBUG_PROGRESS) {
                    HSLog.d("SUNDXING", "Task Exist, taskId = " + model.getId());
                }
                return;
            }
            final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                    .setPath(model.getPath())
                    .setCallbackProgressTimes(100)
                    .setListener(listener);
            getImpl().addTaskForViewHolder(task);
            if (DEBUG_PROGRESS) {
                HSLog.d("SUNDXING", "Add Task Id : " + task.getId() + ", tag = " + (tag != null ? tag.toString() : "null"));
            }

            if (tag != null) {
                task.setTag(tag);
            }

            task.start();

        } else {
            throw new IllegalStateException("Has no pending task to download!");
        }
    }

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


    private TasksManagerModel getByThemeId(int themeId, String extraToken) {
        Type theme = com.acb.utils.Utils.getTypeByThemeId(themeId);
        for (TasksManagerModel model : modelList) {
            if (TextUtils.equals(model.getName(), theme.getIdName() + extraToken)) {
                return model;
            }
        }
        return null;
    }

    public TasksManagerModel getByThemeId(int themeId) {
        return getByThemeId(themeId, "");
    }

    public TasksManagerModel getRingtoneTaskByThemeId(int themeId) {
        return getByThemeId(themeId, TOKEN_EXTRA_RINGTONE);
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

    /**
     * @param model Download task model
     * @return has already downloaded
     * @see FileDownloadStatus
     */
    public boolean isDownloaded(final TasksManagerModel model) {
        final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
        return status == FileDownloadStatus.completed;
    }

    public boolean isDownloading(final int status) {
        return status == FileDownloadStatus.progress || status == FileDownloadStatus.started ||
                status == FileDownloadStatus.connected || status == FileDownloadStatus.pending;
    }

    public int getStatus(final int id, String path) {
        if (id == Constants.DEFUALT_THEME_ID && new File(path).exists()) {
            return FileDownloadStatus.completed;
        }
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
        File ringtoneFile = null;
        if (type instanceof Theme && ((Theme) type).hasRingtone()) {
            String url = ((Theme) type).getRingtoneUrl();
            if (!TextUtils.isEmpty(url)) {
                ringtoneFile = Utils.getRingtoneFile();
                String fileName = Utils.getFileNameFromUrl(url);
                String path = FileDownloadUtils.generateFilePath(ringtoneFile.getAbsolutePath(), fileName);
                addTask(url, path, type.getIdName() + TOKEN_EXTRA_RINGTONE);
            }
        }


        File file = FileUtils.getMediaDirectory();
        if (file != null) {
            String url = type.getSuggestMediaUrl();
            String path = FileDownloadUtils.generateFilePath(file.getAbsolutePath(), type.getFileName());
            addTask(url, path, type.getIdName());
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


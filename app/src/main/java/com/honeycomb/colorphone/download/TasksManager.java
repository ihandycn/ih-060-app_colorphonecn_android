package com.honeycomb.colorphone.download;

import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.SparseArray;

import com.acb.call.themes.Type;
import com.acb.call.utils.FileUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.superapps.util.Threads;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class TasksManager {

    public static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;
    private static final java.lang.String TAG = TasksManager.class.getSimpleName();

    private static final String TOKEN_EXTRA_RINGTONE = "ringtone";
    private volatile boolean isRestoreTasks;
    private Runnable taskReadyCallback = null;

    public void downloadTheme(@NotNull Theme theme, @Nullable Object tag) {
        TasksManagerModel mediaTask = getMediaTaskByThemeId(theme.getId());
        if (mediaTask == null) {
            mediaTask = addMediaTask(theme);
        }
        downloadThemeThreadSafely(mediaTask, tag);


        if (theme.hasRingtone()) {
            TasksManagerModel ringtoneTask = getRingtoneTaskByThemeId(theme.getId());
            if (ringtoneTask == null) {
                ringtoneTask =  addRingtoneTask(theme);
            }
            downloadThemeThreadSafely(ringtoneTask, tag);
        }
    }

    private void downloadThemeThreadSafely(@NotNull TasksManagerModel model, @Nullable Object tag) {
        Threads.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                doDownload(model, tag);
            }
        });
    }

    @MainThread
    public static boolean doDownload(TasksManagerModel model, Object tag) {
        HSLog.d(TAG, "doDownload : " + model.getName());
        if (model != null) {
            BaseDownloadTask oldTask = getImpl().getTask(model.getId());
            if (oldTask != null && oldTask.isRunning()) {
                if (DEBUG_PROGRESS) {
                    HSLog.d(TAG, "Task Exist, taskId = " + model.getId());
                }
                return false;
            }

            FileDownloadListener listener;
            listener = FileDownloadMultiListener.getDefault();
            final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                    .setPath(model.getPath())
                    .setAutoRetryTimes(3)
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
            return true;
        } else {
            throw new IllegalStateException("Has no pending task to download!");
        }
    }

    private final static class HolderClass {
        private final static TasksManager INSTANCE
                = new TasksManager();
    }

    private TasksManagerDBController dbController;
    private List<TasksManagerModel> modelList = new ArrayList<>();

    private TasksManager() {

    }

    public static TasksManager getImpl() {
        return HolderClass.INSTANCE;
    }

    public void init() {
        dbController = new TasksManagerDBController();
        loadTasksFromDB();
    }

    private void loadTasksFromDB() {
        isRestoreTasks = true;
        HSLog.d(TAG, "restore tasks from local");
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                synchronized (TasksManager.this) {
                    HSLog.d(TAG, "restore tasks from local start.");
                    List<TasksManagerModel> temp = dbController.getAllTasks();
                    modelList.clear();
                    modelList.addAll(temp);
                }
                isRestoreTasks = false;
                HSLog.d(TAG, "restore tasks from local finished. Total tasks : " + modelList.size());
                if (taskReadyCallback != null) {
                    Threads.postOnMainThread(taskReadyCallback);
                }
            }
        });
    }

    public void setTaskReadyCallback(Runnable taskReadyCallback) {
        this.taskReadyCallback = taskReadyCallback;
    }

    public boolean isRestoringTasks() {
        return isRestoreTasks;
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


    public void unbindAllTasks() {
        int size = taskSparseArray.size();
        for (int i = 0; i < size; i++) {
            BaseDownloadTask task = taskSparseArray.valueAt(i);
            if (task != null) {
                task.setTag(null);
            }
        }
    }

    public void stopAllTasks() {
        int size = taskSparseArray.size();
        for (int i = 0; i < size; i++) {
            BaseDownloadTask task = taskSparseArray.valueAt(i);
            if (task != null) {
                if (task.isRunning()) {
                    task.pause();
                }
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
                Runnable runnable = taskWeakReference.get();
                if (runnable != null) {
                    runnable.run();
                }
            }

            @Override
            public void disconnected() {
                Runnable runnable = taskWeakReference.get();
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
        unbindAllTasks();
    }

    private TasksManagerModel getByThemeId(int themeId, String extraToken) {
        Type theme = com.acb.utils.Utils.getTypeByThemeId(themeId);
        if (theme != null) {
            final String name = theme.getIdName() + extraToken;
            synchronized (this) {
                for (TasksManagerModel model : modelList) {
                    if (TextUtils.equals(model.getName(), name)) {
                        return model;
                    }
                }
            }
        }
        return null;
    }

    private TasksManagerModel getMediaTaskByThemeId(int themeId) {
        return getByThemeId(themeId, "");
    }

    private TasksManagerModel getRingtoneTaskByThemeId(int themeId) {
        return getByThemeId(themeId, TOKEN_EXTRA_RINGTONE);
    }

    public TasksManagerModel requestMediaTask(Theme theme) {
        TasksManagerModel mediaTask = getMediaTaskByThemeId(theme.getId());
        if (mediaTask == null) {
            mediaTask = addMediaTask(theme);
        }
        return mediaTask;
    }

    public TasksManagerModel requestRingtoneTask(Theme theme) {
        TasksManagerModel mediaTask = getRingtoneTaskByThemeId(theme.getId());
        if (mediaTask == null) {
            mediaTask = addRingtoneTask(theme);
        }
        return mediaTask;
    }

    /**
     * @param id taskId, generate by theme url & path.
     * @return
     */
    public synchronized TasksManagerModel getById(final int id) {
        for (TasksManagerModel model : modelList) {
            if (model.getId() == id) {
                return model;
            }
        }

        return null;
    }

    /**
     * @param model Download task model
     * @return has already downloaded
     * @see FileDownloadStatus
     * TODO as private
     */
    public boolean isDownloaded(final TasksManagerModel model) {
        final int status = TasksManager.getImpl().getStatus(model);
        return status == FileDownloadStatus.completed;
    }

    public boolean isThemeDownloaded(int id) {
        TasksManagerModel model = getMediaTaskByThemeId(id);
        if (model == null) {
            return false;
        }
        return isDownloaded(model);
    }

    public boolean isDownloading(final int status) {
        return status == FileDownloadStatus.progress || status == FileDownloadStatus.started ||
                status == FileDownloadStatus.connected || status == FileDownloadStatus.pending;
    }

    private int getStatus(final TasksManagerModel model) {
        // If download cache it.
        if (model.getTaskStatus() == FileDownloadStatus.completed) {
            return FileDownloadStatus.completed;
        }

        if (FileDownloadUtils.isFilenameConverted(FileDownloadHelper.getAppContext())
                && new File(model.getPath()).exists()) {
            model.setTaskStatus(FileDownloadStatus.completed);
            return FileDownloadStatus.completed;
        }

        if (!FileDownloader.getImpl().isServiceConnected()) {
            return FileDownloadStatus.INVALID_STATUS;
        }
        return FileDownloader.getImpl().getStatus(model.getId(), model.getPath());
    }

    private long getTotal(final int id) {
        return FileDownloader.getImpl().getTotal(id);
    }

    private long getSoFar(final int id) {
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

    /**
     * Create task model if not exist.
     * @param theme
     */
    public void ensureThemeDownloadTaskModels(Theme theme) {
        if (theme.hasRingtone()) {
            TasksManagerModel ringtoneTask = getRingtoneTaskByThemeId(theme.getId());
            if (ringtoneTask == null) {
                addRingtoneTask(theme);
            }
        }

        TasksManagerModel mediaTask = getMediaTaskByThemeId(theme.getId());
        if (mediaTask != null) {
            addMediaTask(theme);
        }
    }

    private TasksManagerModel addRingtoneTask(Theme theme) {
        String url = ((Theme) theme).getRingtoneUrl();
        File ringtoneFile = null;
        if (!TextUtils.isEmpty(url)) {
            ringtoneFile = Utils.getRingtoneFile();
            String fileName = Utils.getFileNameFromUrl(url);
            String path = FileDownloadUtils.generateFilePath(ringtoneFile.getAbsolutePath(), fileName);
            ((Theme) theme).setRingtonePath(path);
            return addTask(url, path, theme.getIdName() + TOKEN_EXTRA_RINGTONE);
        }
        return null;
    }

    private TasksManagerModel addMediaTask(Theme theme) {
        File file = FileUtils.getMediaDirectory();
        if (file != null) {
            String url = theme.getSuggestMediaUrl();
            String path = FileDownloadUtils.generateFilePath(file.getAbsolutePath(), theme.getFileName());
            return addTask(url, path, theme.getIdName());
        }
        return null;
    }

    public static String getVideoWallpaperPath(String url) {
        List<Theme> themes = ThemeList.themes();
        Theme targetTheme = null;
        for (Theme theme : themes) {
            if (TextUtils.equals(theme.getMp4Url(),url)) {
                targetTheme = theme;
            }
        }
        File file = FileUtils.getMediaDirectory();
        if (file != null && targetTheme != null) {
            String path = FileDownloadUtils.generateFilePath(file.getAbsolutePath(), targetTheme.getFileName());
            return path;
        } else {
            return FileDownloadUtils.getDefaultSaveFilePath(url) + ".mp4";
        }
    }

    private synchronized boolean getTaskExist(Theme theme) {
        for (TasksManagerModel model : modelList) {
            if (TextUtils.equals(model.getName(), theme.getIdName())) {
                HSLog.d(TAG, "checkTaskExist true : " + theme);
                return true;
            }
        }
        HSLog.d(TAG, "checkTaskExist false" + theme);
        return false;
    }

    private synchronized TasksManagerModel addTask(final String url, final String path, String token) {
        HSLog.d(TAG, "## Add new task ##:" + url);

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return null;
        }

        final int id = FileDownloadUtils.generateId(url, path);
        TasksManagerModel model = getById(id);
        if (model != null) {
            HSLog.d(TAG, "## Add new task  ##, exist already");
            return model;
        }
        final TasksManagerModel newModel = dbController.addTask(url, path, token);
        if (newModel != null) {
            modelList.add(newModel);
        }

        return newModel;
    }
}


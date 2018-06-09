package colorphone.acb.com.libscreencard.gif;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Downloader {

    private static final String TAG = Downloader.class.getSimpleName();
    private static final boolean LOG_VERBOSE = false;

    private static final String THREAD_TAG = "security-download-thread-";
    private static final String CACHE_DIR_NAME = "downloader";
    private static final String DISK_LRU_CACHE_DIR = "download";
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50 MB

    private static final int NUMBER_OF_ALIVE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    public interface DownloadTaskListener {
        void onStart();

        void onProgress(int total, int current, boolean currentSuccess, DownloadItem item);

        void onComplete(int total);
    }

    public static class DownloadItem {
        private String mUrl;
        private String mPath;
        private Object mTag;
        private DownloadItemListener mDownloadItemListener;

        public DownloadItem(String url, String path) {
            this(url, path, null);
        }

        public DownloadItem(String url, String path, DownloadItemListener downloadItemListener) {
            this(url, path, downloadItemListener, null);
        }

        public DownloadItem(String url, String path, Object tag) {
            this(url, path, null, tag);
        }

        public DownloadItem(String url, String path, @Nullable DownloadItemListener downloadItemListener, @Nullable Object tag) {
            mUrl = url;
            mPath = path;
            mDownloadItemListener = downloadItemListener;
            mTag = tag;
        }

        public String getPath() {
            return mPath;
        }

        public String getUrl() {
            return mUrl;
        }

        public DownloadItemListener getListener() {
            return mDownloadItemListener;
        }

        public Object getTag() {
            return mTag;
        }

        private void setPath(String path) {
            mPath = path;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DownloadItem) {
                return TextUtils.equals(mUrl, ((DownloadItem) obj).mUrl)
                        && TextUtils.equals(mPath, ((DownloadItem) obj).mPath);
            }
            return false;
        }

        @Override
        public String toString() {
            return "DownloadItem " + mUrl + ", path: " + mPath + ", tag: " + mTag;
        }

        public interface DownloadItemListener {

            enum CancelReason {
                DUPLICATE_REQUEST,
                INVALID_PARAMETERS,
                MANUAL,
            }

            void onStart(DownloadItem item);

            void onProgress(DownloadItem item, float progress);

            void onComplete(DownloadItem item);

            void onCancel(DownloadItem item, CancelReason reason);

            void onFailed(DownloadItem item, String errorMsg);
        }
    }

    private final ThreadPoolExecutor mExecutor;
    private final ThreadFactory mDefaultThreadFactory = Executors.defaultThreadFactory();
    private final DownloadBlockingDeque mTaskDeque = new DownloadBlockingDeque();

    private File mCacheDir;

    private final List<DownloadingRecord> mDownloadingItems = new ArrayList<>();

    private volatile static Downloader sInstance;

    public static Downloader getInstance() {
        if (sInstance == null) {
            synchronized (Downloader.class) {
                if (sInstance == null) {
                    sInstance = new Downloader();
                }
            }
        }
        return sInstance;
    }

    private Downloader() {
        int poolSize = Math.min(NUMBER_OF_ALIVE_CORES * 2, 3);
        HSLog.d(TAG, "Pool size = " + poolSize);
        mExecutor = new ThreadPoolExecutor(
                poolSize, // Initial pool size
                poolSize, // Max pool size, not used as we are providing an unbounded queue to the executor
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mTaskDeque,
                new ThreadFactory() {
                    private AtomicInteger mThreadCount = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = mDefaultThreadFactory.newThread(r);
                        thread.setName(THREAD_TAG + mThreadCount.getAndIncrement());
                        thread.setPriority(Thread.MIN_PRIORITY);
                        return thread;
                    }
                }
        );
        mExecutor.allowCoreThreadTimeOut(true);
        mCacheDir = Utils.getCacheDirectory(CACHE_DIR_NAME, true);
    }

    public synchronized void download(final DownloadTask task, @Nullable Handler handler) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        final DownloadTaskListener downloadTaskListener = task.getDownloadListener();
        if (task.isEmpty() && downloadTaskListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    downloadTaskListener.onStart();
                    downloadTaskListener.onComplete(0);
                }
            });
            return;
        }
        HSLog.d(TAG, "Execute task " + task.hashCode());
        final Handler finalHandler = handler;
        final List<DownloadItem> downloadItems = task.getDownloadItems();
        for (final DownloadItem downloadItem : downloadItems) {
            final String url = downloadItem.getUrl();
            final String path = downloadItem.getPath();
            DownloadItem.DownloadItemListener downloadItemListener = downloadItem.getListener();

            if (TextUtils.isEmpty(url)) {
                if (HSLog.isDebugging()) {
                    throw new IllegalArgumentException("Invalid download item " + downloadItem);
                } else if (downloadItemListener != null) {
                    finalHandler.post(() -> {
                        downloadItemListener.onCancel(downloadItem,
                                DownloadItem.DownloadItemListener.CancelReason.INVALID_PARAMETERS);
                    });
                }
                continue;
            }

            DownloadRunnable taskRunnable = new DownloadRunnable() {
                private boolean mSuccess;

                @Override
                public void run() {
                    HSHttpConnection connection;
                    synchronized (mDownloadingItems) {
                        for (DownloadingRecord downloadingItem : mDownloadingItems) {
                            if (downloadingItem.item.equals(downloadItem)) {
                                HSLog.d(TAG, "Item " + downloadItem + " already downloading, return");
                                if (downloadItemListener != null) {
                                    finalHandler.post(() -> downloadItemListener.onCancel(downloadItem, DownloadItem
                                            .DownloadItemListener.CancelReason.DUPLICATE_REQUEST));
                                }
                                return;
                            }
                        }
                        HSLog.d(TAG, "Start downloading " + downloadItem);
                        connection = new HSHttpConnection(url, HttpRequest.Method.GET);
                        mDownloadingItems.add(new DownloadingRecord(downloadItem, connection));
                    }

                    if (mCacheDir.exists()) {
                        final File output = new File(mCacheDir, getFileName(url));
                        HSLog.d(TAG, "Temp cache file path: " + output.getAbsolutePath());
                        connection.setDownloadFile(output);
                        connection.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
                            @Override
                            public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long l, long l1) {
                                if (LOG_VERBOSE) {
                                    HSLog.d(TAG, "bytes.length = " + bytes.length + " l = " + l + " l1 = " + l1);
                                }
                                if (downloadItemListener != null && l1 > 0) {
                                    finalHandler.post(() -> downloadItemListener.onProgress(downloadItem, l * 1.0f / l1));
                                }
                            }
                        });
                        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
                            @Override
                            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                                if (revokeDownloadingStatus()) {
                                    boolean connectionSucceeded = hsHttpConnection.isSucceeded();
                                    if (connectionSucceeded) {
                                        if (task.isLimitedByLru()) {
                                            DiskCache diskCache = DiskLruCacheWrapperFixed.get(new File(Utils.getCacheDirectory(DISK_LRU_CACHE_DIR), task
                                                    .getDirectory()), DISK_CACHE_SIZE);
                                            Key key = new GlideUrl(url);
                                            diskCache.put(key, new DiskCache.Writer() {
                                                @Override
                                                public boolean write(File file) {
                                                    HSLog.d(TAG, "Dest path: " + file.getAbsolutePath());
                                                    try {
                                                        Utils.copyFile(output, file);
                                                        output.delete();
                                                    } catch (IOException e) {
                                                        HSLog.d(TAG, "File moved failed e: " + e.getMessage());
                                                        return false;
                                                    }
                                                    return true;
                                                }
                                            });
                                            File file = diskCache.get(key);
                                            if (Utils.checkFileValid(file)) {
                                                downloadItem.setPath(file.getAbsolutePath());
                                            } else {
                                                HSLog.d(TAG, "Url: " + url + " downloaded success but cached failure");
                                            }
                                        } else if (!output.renameTo(new File(path))) {
                                            HSLog.d(TAG, "File download failed");
                                            return;
                                        }
                                        if (downloadItemListener != null) {
                                            finalHandler.post(() -> downloadItemListener.onComplete(downloadItem));
                                        }
                                        HSLog.d(TAG, "File download succeeded");
                                        mSuccess = true;
                                    } else {
                                        if (downloadItemListener != null) {
                                            finalHandler.post(() -> {
                                                if (connectionSucceeded) {
                                                    downloadItemListener.onFailed(downloadItem,
                                                            "Rename failed");
                                                } else {
                                                    downloadItemListener.onFailed(downloadItem,
                                                            "Connection failed");
                                                }
                                            });
                                        }
                                        HSLog.d(TAG, "File download failed");
                                    }
                                } else {
                                    HSLog.d(TAG, "Task cancelled, Remove temp file");
                                    output.delete();
                                }
                            }

                            @Override
                            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                                HSLog.d(TAG, "File download failed error = " + hsError.getMessage());
                                if (revokeDownloadingStatus() && downloadItemListener != null) {
                                    finalHandler.post(() -> downloadItemListener.onFailed(downloadItem, hsError.getMessage()));
                                }
                            }

                            private boolean revokeDownloadingStatus() {
                                boolean notCancelled = false;
                                synchronized (mDownloadingItems) {
                                    for (Iterator<DownloadingRecord> iterator = mDownloadingItems.iterator();
                                         iterator.hasNext(); ) {
                                        DownloadingRecord downloadingItem = iterator.next();
                                        if (downloadingItem.item.equals(downloadItem)) {
                                            iterator.remove();
                                            notCancelled = true;
                                            break;
                                        }
                                    }
                                }
                                if (!notCancelled && downloadItemListener != null) {
                                    finalHandler.post(() -> downloadItemListener.onCancel(downloadItem, DownloadItem
                                            .DownloadItemListener.CancelReason.MANUAL));
                                }
                                HSLog.d(TAG, "revokeDownloadingStatus, task not cancelled: " + notCancelled);
                                return notCancelled;
                            }
                        });
                        if (downloadItemListener != null) {
                            finalHandler.post(() -> downloadItemListener.onStart(downloadItem));
                        }
                        connection.startSync();
                    }

                    synchronized (Downloader.this) {
                        task.finishOne();
                        if (downloadTaskListener != null) {
                            finalHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadTaskListener.onProgress(downloadItems.size(), task.getFinishCount(),
                                            mSuccess, downloadItem);
                                }
                            });
                        }
                        if (task.isTaskFinish()) {
                            HSLog.d(TAG, "Task finished");
                            if (downloadTaskListener != null) {
                                finalHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        downloadTaskListener.onComplete(downloadItems.size());
                                    }
                                });
                            }
                        }
                    }
                }
            };
            taskRunnable.setCancelable(task.isCancelable());
            taskRunnable.setIsLimitedByCount(task.isLimitedByCount());
            taskRunnable.setLimitedCount(task.getLimitedCount());
            taskRunnable.setLimitedGroupTag(task.getLimitedGroupTag());
            taskRunnable.setLifo(task.isLifo());
            if (downloadTaskListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadTaskListener.onStart();
                    }
                });
            }
            mExecutor.execute(taskRunnable);
        }
    }

    /**
     * @param downloadTask null for all
     */
    public void cancel(@Nullable DownloadTask downloadTask) {
        synchronized (mDownloadingItems) {
            for (Iterator<DownloadingRecord> iterator = mDownloadingItems.iterator();
                 iterator.hasNext(); ) {
                DownloadingRecord downloadingItem = iterator.next();
                if (downloadTask == null) {
                    downloadingItem.connection.cancel();
                    iterator.remove();
                } else {
                    for (DownloadItem downloadItem : downloadTask.getDownloadItems()) {
                        if (downloadingItem.item.equals(downloadItem)) {
                            downloadingItem.connection.cancel();
                            iterator.remove();
                            break;
                        }
                    }
                }
            }
        }
    }

    public void cancel(boolean onlyCancelable) {
        if (onlyCancelable) {
            Iterator iterator = mTaskDeque.iterator();
            while (iterator.hasNext()) {
                DownloadRunnable next = (DownloadRunnable) iterator.next();
                if (next.isCancelable()) {
                    HSLog.d(TAG, "Cancel download runnable " + next.hashCode());
                    iterator.remove();
                }
            }
        } else {
            mTaskDeque.clear();
        }
        cancel(null);
    }

    public void pause() {
        mTaskDeque.pause();
    }

    public void resume() {
        mTaskDeque.resume();
    }

    private String getFileName(String url) {
        return Utils.md5(url);
    }

    public static @Nullable
    File getCachedFile(String directory, String url) {
        DiskCache diskCache = DiskLruCacheWrapperFixed.get(new File(Utils.getCacheDirectory(DISK_LRU_CACHE_DIR), directory), DISK_CACHE_SIZE);
        return diskCache.get(new GlideUrl(url));
    }

    public boolean isDownloading(String url) {
        if (mCacheDir != null) {
            File output = new File(mCacheDir, getFileName(url));
            return output.exists();
        }
        return false;
    }

    public static String getDownloadPath(String directory, String url) {
        return new File(Utils.getDirectory(directory),
                Utils.md5(url) + "." + Utils.getRemoteFileExtension(url)).getAbsolutePath();
    }

    public static File getDownloadFile(String directory, String url) {
        return new File(Utils.getDirectory(directory),
                Utils.md5(url) + "." + Utils.getRemoteFileExtension(url));
    }

    public static boolean isCachedSuccess(String directory, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        File file = getDownloadFile(directory, url);
        if (file.exists() && file.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private static class DownloadingRecord {
        DownloadItem item;
        HSHttpConnection connection;

        DownloadingRecord(DownloadItem item, HSHttpConnection connection) {
            this.item = item;
            this.connection = connection;
        }
    }

    public static abstract class DownloadRunnable implements Runnable {

        private boolean mCancelable;
        private boolean mIsLifo;
        private boolean mIslimited;
        private int mLimitedCount;
        private String mTag;

        public void setCancelable(boolean cancelable) {
            mCancelable = cancelable;
        }

        public boolean isCancelable() {
            return mCancelable;
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
}

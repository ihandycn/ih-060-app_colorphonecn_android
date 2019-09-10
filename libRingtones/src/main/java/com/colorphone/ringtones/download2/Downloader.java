package com.colorphone.ringtones.download2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.colorphone.ringtones.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private static final String THREAD_TAG = "launcher-download-thread-";
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
        mCacheDir = getCacheDirectory(CACHE_DIR_NAME, true);
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
            final DownloadItem.DownloadItemListener downloadItemListener = downloadItem.getListener();

            if (TextUtils.isEmpty(url)) {
                if (BuildConfig.DEBUG) {
                    throw new IllegalArgumentException("Invalid download item " + downloadItem);
                } else if (downloadItemListener != null) {
                    finalHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadItemListener.onCancel(downloadItem,
                                    DownloadItem.DownloadItemListener.CancelReason.INVALID_PARAMETERS);
                        }
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
                                    finalHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            downloadItemListener.onCancel(downloadItem, DownloadItem
                                                    .DownloadItemListener.CancelReason.DUPLICATE_REQUEST);
                                        }
                                    });
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
                                            DiskCache diskCache = DiskLruCacheWrapperFixed.get(new File(getCacheDirectory(DISK_LRU_CACHE_DIR), task
                                                    .getDirectory()), DISK_CACHE_SIZE);
                                            Key key = new GlideUrl(url);
                                            diskCache.put(key, new DiskCache.Writer() {
                                                @Override
                                                public boolean write(File file) {
                                                    HSLog.d(TAG, "Dest path: " + file.getAbsolutePath());
                                                    try {
                                                        copyFile(output, file);
                                                        output.delete();
                                                    } catch (IOException e) {
                                                        HSLog.d(TAG, "File moved failed e: " + e.getMessage());
                                                        return false;
                                                    }
                                                    return true;
                                                }
                                            });
                                            File file = diskCache.get(key);
                                            if (checkFileValid(file)) {
                                                downloadItem.setPath(file.getAbsolutePath());
                                            } else {
                                                HSLog.d(TAG, "Url: " + url + " downloaded success but cached failure");
                                            }
                                        } else {
                                            try {
                                                copyFile(output, new File(path));
                                            } catch (IOException e) {
                                                HSLog.d(TAG, "File download failed: " + e.getMessage());
                                                if (downloadItemListener != null) {
                                                    finalHandler.post(() -> downloadItemListener.onFailed(downloadItem,"Rename failed"));
                                                }
                                                return;
                                            }
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
        return md5(url);
    }

    public static @Nullable
    File getCachedFile(String directory, String url) {
        DiskCache diskCache = DiskLruCacheWrapperFixed.get(new File(getCacheDirectory(DISK_LRU_CACHE_DIR), directory), DISK_CACHE_SIZE);
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
        return new File(directory,
                md5(url) + "." + getRemoteFileExtension(url)).getAbsolutePath();
    }

    public static File getDownloadFile(String directory, String url) {
        return new File(directory,
                md5(url) + "." + getRemoteFileExtension(url));
    }

    public static boolean isCachedSuccess(String directory, String url) {
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

    /**
     * Retrieve, creating if needed, a new directory of given name in which we
     * can place our own custom data files.
     */
    public static @Nullable File getDirectory(String dirPath) {
        File file = HSApplication.getContext().getFilesDir();
        String[] path = dirPath.split(File.separator);
        for (String dir : path) {
            file = new File(file, dir);
            if (!file.exists() && !file.mkdir()) {
                HSLog.w(TAG, "Error making directory");
                return null;
            }
        }
        return file;
    }

    /**
     * Retrieve, creating if needed, a new sub-directory in cache directory.
     * Internal cache directory is used if external cache directory is not available.
     */
    private static File getCacheDirectory(String subDirectory) {
        return getCacheDirectory(subDirectory, false);
    }

    /**
     * @param useInternal Only uses internal cache directory when {@code true}.
     */
    private static File getCacheDirectory(String subDirectory, boolean useInternal) {
        Context context = HSApplication.getContext();
        String cacheDirPath;
        File externalCache = null;
        if (!useInternal) {
            try {
                externalCache = context.getExternalCacheDir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (externalCache != null) {
            cacheDirPath = externalCache.getAbsolutePath() + File.separator + subDirectory + File.separator;
        } else {
            cacheDirPath = context.getCacheDir().getAbsolutePath() + File.separator + subDirectory + File.separator;
        }
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                HSLog.d("Utils.Cache", "Created cache directory: " + cacheDir.getAbsolutePath());
            } else {
                HSLog.e("Utils.Cache", "Failed to create cache directory: " + cacheDir.getAbsolutePath());
            }
        }
        return cacheDir;
    }


    private static void copyFile(File src, File dst) throws IOException {
        if (!src.exists()) {
            return;
        }
        if (dst.exists()) {
            boolean removed = dst.delete();
            if (removed) {
                HSLog.d(TAG, "Replacing file " + dst);
            }
        }
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            outChannel.close();
        }
    }

    private static String getRemoteFileExtension(String url) {
        String extension = "";
        if (url != null) {
            int i = url.lastIndexOf('.');
            int p = Math.max(url.lastIndexOf('/'), url.lastIndexOf('\\'));
            if (i > p) {
                extension = url.substring(i + 1);
            }
        }
        return extension;
    }

    private static boolean checkFileValid(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }

    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}

package com.honeycomb.colorphone.wallpaper.livewallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.AnyThread;

import com.honeycomb.colorphone.wallpaper.util.CommonUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Bitmaps;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BitmapDecoder {
    private static final String FILE_SUFFIX = ".png";
    private static final String TAG = "BitmapDecoder";
    private static ExecutorService sPoolExecutor = Executors.newFixedThreadPool(3);
    private String wallpaperName;

    /**
     * Key is format
     */
    private final Map<String, Bitmap> mBitmaps = new ConcurrentHashMap<>();
    private final List<File> mPendingFileList = new ArrayList<>();

    /**
     * If false, all bitmaps are ready.
     */
    private volatile boolean isDecoding;

    public BitmapDecoder(String wallpaperName) {
        this.wallpaperName = wallpaperName;
    }

    public String getWallpaperName() {
        return wallpaperName;
    }

    @AnyThread
    protected Bitmap getBitmap(String textureName) {
        final String keyName = textureName + FILE_SUFFIX;
        Bitmap bitmap = mBitmaps.remove(keyName);
        if (bitmap == null) {
            // is decoding
            // waiting for ready.
            while (isDecoding && mBitmaps.get(keyName) == null) {
                try {
                    synchronized (this) {
                        HSLog.d(TAG, "wait" + keyName);
                        wait(2000);
                        HSLog.d(TAG, "wait end ");
                    }
                } catch (InterruptedException ignore) {
                }
            }
            bitmap = mBitmaps.remove(keyName);
        }
        return bitmap;
    }

    private void prepare() {
        File baseDirectory = CommonUtils.getDirectory(LiveWallpaperConsts.Files.LIVE_DIRECTORY);
        File storedDirectory = new File(baseDirectory, wallpaperName);
        if (storedDirectory.exists() && storedDirectory.isDirectory()) {
            for (File file : storedDirectory.listFiles()) {
                if (file.getName().equals(LiveWallpaperConsts.LIVE_WALLPAPER_BACKGROUND_DIRECTORY) && file.isDirectory()) {
                    for (File wallpaper : file.listFiles()) {
                        if (wallpaper != null && wallpaper.getPath().endsWith(".png")) {
                            mPendingFileList.add(wallpaper);
                        }
                    }
                }
            }
        }
    }

    @AnyThread
    public synchronized void startDecode() {
        HSLog.d(TAG, "doDecode");
        if (isDecoding) {
            return;
        }
        isDecoding = true;
        doDecode();
    }

    private void doDecode() {
        if (mPendingFileList.isEmpty()) {
            prepare();
        }

        for (File file : mPendingFileList) {
            DecoderTask task = new DecoderTask(this, file);
            sPoolExecutor.execute(task);
        }
        HSLog.d(TAG, " to checkStatus doDecode");
        checkStatus();
    }

    private synchronized void queueBitmap(File file, Bitmap bitmap) {
        mBitmaps.put(file.getName(), bitmap);
        mPendingFileList.remove(file);
        HSLog.d(TAG, " to checkStatus queueBitmap : " + file.getName());
        checkStatus();
    }

    private synchronized void checkStatus() {
        if (mPendingFileList.isEmpty()) {
            // All task exc
            isDecoding = false;
        }
        HSLog.d(TAG, "notifyAll" + mBitmaps.size());
        notifyAll();
    }

    private synchronized void release() {
        mBitmaps.clear();
        mPendingFileList.clear();
        isDecoding = false;
        HSLog.d(TAG, " to checkStatus release");
        checkStatus();
    }

    public boolean isOver() {
        return !isDecoding && mBitmaps.isEmpty();
    }

    private static class DecoderTask implements Runnable {
        private static final String TAG = DecoderTask.class.getName();

        private final WeakReference<BitmapDecoder> mBitmapDecoderReference;
        private final File mFile;
        private volatile boolean mCancelled;

        private volatile boolean mRetried;

        DecoderTask(BitmapDecoder bitmapDecoder, File file) {
            mBitmapDecoderReference = new WeakReference<>(bitmapDecoder);
            mFile = file;
        }

        public void cancel() {
            mCancelled = true;
        }

        private void onPostExecute(Bitmap bitmap) {
            BitmapDecoder bitmapDecoder = mBitmapDecoderReference.get();
            if (bitmapDecoder != null) {
                bitmapDecoder.queueBitmap(mFile, bitmap);
            }
        }

        private void onCancelled() {
            BitmapDecoder bitmapDecoder = mBitmapDecoderReference.get();
            if (bitmapDecoder != null) {
                bitmapDecoder.release();
            }
        }

        private Bitmap decodeBitmap(String wallpaperPath) {
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(wallpaperPath);
            } catch (Exception ignored) {
                HSLog.e(TAG, "picture decode failed: file path = " + wallpaperPath);

            }
            return bitmap;
        }

        @Override
        public void run() {
            if (mCancelled) {
                onCancelled();
            } else {
                Bitmap bitmap = decodeBitmap(mFile.getPath());
                if (bitmap != null) {
                    onPostExecute(bitmap);
                } else {
                    if (!mRetried) {
                        // Retry if the first decode operation fails
                        mRetried = true;
                        sPoolExecutor.execute(this);
                    } else {
                        // Callback with a 1 x 1 empty bitmap if we fail the second time
                        onPostExecute(Bitmaps.createFallbackBitmap());
                    }
                }
            }
        }
    }

}
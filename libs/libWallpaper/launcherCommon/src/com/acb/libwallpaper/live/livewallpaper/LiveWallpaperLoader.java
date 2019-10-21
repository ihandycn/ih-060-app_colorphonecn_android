package com.acb.libwallpaper.live.livewallpaper;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.Base64;

import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.live.util.Thunk;
import com.acb.libwallpaper.live.download.DownloadTask;
import com.acb.libwallpaper.live.download.Downloader;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Threads;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import hugo.weaving.DebugLog;

public class LiveWallpaperLoader extends WallpaperLoader {

    public static final String TAG = LiveWallpaperLoader.class.getSimpleName();

    private String mWallpaperName;

    private Callbacks mCallbacks;
    private DownloadTask mDownloadTask;

    public LiveWallpaperLoader() {
    }

    @Override
    public void setWallpaperName(String wallpaperName) {
        mWallpaperName = wallpaperName;
    }

    @Override
    public String getWallpaperName() {
        return mWallpaperName;
    }

    @MainThread
    public void load(Callbacks callbacks) {
        mCallbacks = callbacks;
        if (LiveWallpaperConsts.DEBUG_PARTICLE_FLOW_WALLPAPER) {
            fireCallbackForSuccess(null);
        } else {
            LiveWallpaperManager wallpaperManager = LiveWallpaperManager.getInstance();
            String wallpaperName = getWallpaperName();

            int type = wallpaperManager.getType(wallpaperName);
            switch (type) {
                case LiveWallpaperConsts.TYPE_SHADER_AND_CONFETTI:
                    loadShaderAndConfettiWallpaper();
                    break;
                case LiveWallpaperConsts.TYPE_VIDEO:
                    loadVideoWallpaper();
                    break;
                case LiveWallpaperConsts.TYPE_PARTICLE_FLOW:
                    fireCallbackForSuccess(null);
                    break;
                default:
                    throw new IllegalStateException("Illegal type. Wallpaper XML may be corrupt.");
            }
        }
    }

    private void loadShaderAndConfettiWallpaper() {
        LiveWallpaperManager wallpaperManager = LiveWallpaperManager.getInstance();
        String wallpaperName = getWallpaperName();

        ShaderAndConfettiFileSet fileSet = new ShaderAndConfettiFileSet();
        boolean loadNeeded = auditLocalFilesForShaderAndConfettiWallpaper(
                fileSet, wallpaperManager, wallpaperName);

        if (loadNeeded) {
            if (wallpaperManager.isLocal(wallpaperName)) {
                // Copy resource pack in assets to internal storage
                copyAndUnpackShaderAndConfettiWallpaper(wallpaperName, fileSet);
            } else {
                // Download resource pack from server
                downloadAndUnpackShaderAndConfettiWallpaper(wallpaperManager, wallpaperName, fileSet);
            }
        } else {
            fireCallbackForSuccess(fileSet);
        }
    }

    private void loadVideoWallpaper() {
        LiveWallpaperManager wallpaperManager = LiveWallpaperManager.getInstance();
        String wallpaperName = getWallpaperName();

        VideoFileSet fileSet = new VideoFileSet();
        boolean loadNeeded = auditLocalFilesForVideoWallpaper(fileSet, wallpaperName);

        if (loadNeeded) {
            if (wallpaperManager.isLocal(wallpaperName)) {
                // Copy resource pack in assets to internal storage
                copyAndDecryptVideoWallpaper(wallpaperName, fileSet);
            } else {
                // Download resource pack from server
                downloadAndDecryptVideoWallpaper(wallpaperManager, wallpaperName, fileSet);
            }
        } else {
            fireCallbackForSuccess(null);
        }
    }

    /**
     * Audit local files to see whether we need to download resources (if any resource is missing
     * from local storage).
     *
     * @return Whether downloading of ZIP resource package is needed for current wallpaper.
     */
    private boolean auditLocalFilesForShaderAndConfettiWallpaper(
            ShaderAndConfettiFileSet fileSet, LiveWallpaperManager wallpaperManager, String wallpaperName) {
        if (!fileSet.build(wallpaperManager, wallpaperName)) {
            HSLog.d(TAG, "No local meta-data for wallpaper " + wallpaperName);
            return true;
        }
        boolean downloadNeeded = false;
        for (File storedFile : fileSet.imageFiles) {
            if (storedFile.exists()) {
                HSLog.d(TAG, storedFile + " already exists");
            } else {
                HSLog.d(TAG, storedFile + " does NOT exist");
                downloadNeeded = true;
            }
        }
        return downloadNeeded;
    }

    private void copyAndUnpackShaderAndConfettiWallpaper(String wallpaperName,
                                                         ShaderAndConfettiFileSet fileSet) {
        Threads.postOnThreadPoolExecutor(() -> {
            AssetManager assetManager = HSApplication.getContext().getAssets();
            try {
                InputStream is = assetManager.open("livewallpapers" + File.separator
                        + wallpaperName + File.separator + wallpaperName + ".zip");
                OutputStream os = new FileOutputStream(fileSet.zipFile);
                Utils.copy(is, os);
            } catch (IOException e) {
                fireCallbackForFailure("Error copying res pack from assets to internal directory: " + e);
                return;
            }
            try {
                unpackResources(fileSet);
                fireCallbackForSuccess(fileSet);
            } catch (IOException | ResourceUnzipError e) {
                fireCallbackForFailure("Error unzipping res pack: " + e);
            }
        });
    }

    private void downloadAndUnpackShaderAndConfettiWallpaper(LiveWallpaperManager wallpaperManager,
                                                             String wallpaperName,
                                                             ShaderAndConfettiFileSet fileSet) {
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
                        unpackResources(fileSet, wallpaperName);
                        fireCallbackForSuccess(fileSet);
                    } catch (IOException | ResourceUnzipError e) {
                        fireCallbackForFailure("Error unzipping res pack: " + e);
                    }
                });
            }

            @Override
            public void onCancel(Downloader.DownloadItem item, CancelReason reason) {
            }

            @Override
            public void onFailed(Downloader.DownloadItem item, String errorMsg) {
                fireCallbackForFailure("Error downloading res pack: " + errorMsg);
            }
        };
        String zipUrl = wallpaperManager.getBaseUrl(wallpaperName)
                + wallpaperName + File.separator + wallpaperName + ".zip";
        downloadTask.add(new Downloader.DownloadItem(zipUrl,
                fileSet.zipFile.getAbsolutePath(), itemListener, null));
        if (!Networks.isNetworkAvailable(-1)) {
            fireCallbackForFailure("No network");
            return;
        }
        Downloader.getInstance().download(downloadTask, new Handler(Looper.getMainLooper()));
    }

    private boolean auditLocalFilesForVideoWallpaper(VideoFileSet fileSet, String wallpaperName) {
        File baseDirectory = CommonUtils.getDirectory(
                LiveWallpaperConsts.Files.LIVE_DIRECTORY + File.separator + wallpaperName);
        File videoFile = new File(baseDirectory, "video.mp4");
        fileSet.videoFile = videoFile;
        if (videoFile.exists()) {
            HSLog.d(TAG, videoFile + " already exists");
            return false;
        } else {
            HSLog.d(TAG, videoFile + " does NOT exist");
            return true;
        }
    }

    private void copyAndDecryptVideoWallpaper(String wallpaperName, VideoFileSet fileSet) {
        Threads.postOnThreadPoolExecutor(() -> {
            AssetManager assetManager = HSApplication.getContext().getAssets();
            String hashedName = getHashFileName(wallpaperName);
            try {
                InputStream is = assetManager.open("livewallpapers" + File.separator
                        + wallpaperName + File.separator + hashedName);
                OutputStream os = new FileOutputStream(fileSet.videoFile);
                Utils.copy(is, os);
            } catch (IOException e) {
                fireCallbackForFailure("Error copying video from assets to internal directory: " + e);
                return;
            }
            try {
                decryptVideo(fileSet, wallpaperName);
                fireCallbackForSuccess(null);
            } catch (Exception e) {
                fireCallbackForFailure("Error decrypting video: " + e);
            }
        });
    }

    private void downloadAndDecryptVideoWallpaper(LiveWallpaperManager wallpaperManager,
                                                  String wallpaperName, VideoFileSet fileSet) {
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
                        decryptVideo(fileSet, wallpaperName);
                        fireCallbackForSuccess(null);
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
        String hashedName = getHashFileName(wallpaperName);
        String videoUrl = wallpaperManager.getBaseUrl(wallpaperName)
                + wallpaperName + File.separator + hashedName;
        downloadTask.add(new Downloader.DownloadItem(videoUrl,
                fileSet.videoFile.getAbsolutePath(), itemListener, null));
        if (!Networks.isNetworkAvailable(-1)) {
            fireCallbackForFailure("No network");
            return;
        }
        Downloader.getInstance().download(downloadTask, new Handler(Looper.getMainLooper()));
    }

    @SuppressWarnings({"WeakerAccess", "TryFinallyCanBeTryWithResources"})
    @DebugLog
    @Thunk
    void unpackResources(ShaderAndConfettiFileSet fileSet, String wallpaperName)
            throws IOException, ResourceUnzipError {
        HSLog.d(TAG, "Unzipping res pack " + fileSet.zipFile);
        if (!fileSet.isBuilt(wallpaperName)) {
            FileInputStream fis = new FileInputStream(fileSet.zipFile);
            ZipInputStream zis = new ZipInputStream(fis);
            try {
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    String fileName = zipEntry.getName();
                    if (zipEntry.isDirectory() || !fileName.equals(wallpaperName + ".xml")) {
                        continue;
                    }
                    unpackMetadata(wallpaperName, zis, fileName);
                    break;
                }
            } finally {
                try {
                    fis.close();
                    zis.close();
                } catch (IOException ignored) {
                }
            }

            if (!fileSet.build(LiveWallpaperManager.getInstance(), wallpaperName)) {
                String desc = "Metadata is neither placed in assets directory, " +
                        "nor successfully unpacked from remote res package, report failure";
                HSLog.w(TAG, desc);
                throw new ResourceUnzipError(desc);
            }
        }
        unpackResources(fileSet);
        boolean deleted = fileSet.zipFile.delete();
        HSLog.d(TAG, "Delete res pack " + fileSet.zipFile + ", success: " + deleted);
    }

    private void unpackMetadata(String wallpaperName, ZipInputStream zis, String fileName) throws IOException, ResourceUnzipError {
        File baseDirectory = CommonUtils.getDirectory(
                LiveWallpaperConsts.Files.LIVE_DIRECTORY + File.separator + wallpaperName);
        File dstFile = new File(baseDirectory, wallpaperName + ".xml");
        File tempDstFile = new File(dstFile.getAbsolutePath() + ".tmp");
        boolean created = tempDstFile.createNewFile();
        HSLog.d(TAG, "Unzipping metadata " + fileName + " from res pack to " + tempDstFile + ", created: " + created);
        FileOutputStream fos = new FileOutputStream(tempDstFile);
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                fos.flush();
            }
        } finally {
            try {
                fos.close();
            } catch (IOException ignored) {
            }
        }
        boolean deleted = dstFile.delete();
        if (deleted) {
            HSLog.d(TAG, "Deleted old file " + dstFile);
        }
        boolean renamed = tempDstFile.renameTo(dstFile);
        HSLog.d(TAG, "Rename unzipped temp file " + tempDstFile + " to " + dstFile + ", success: " + renamed);
        if (!renamed) {
            throw new ResourceUnzipError("Failed to rename meta-data file.");
        }
    }

    private void unpackResources(ShaderAndConfettiFileSet fileSet) throws IOException, ResourceUnzipError {
        FileInputStream fis = new FileInputStream(fileSet.zipFile);
        ZipInputStream zis = new ZipInputStream(fis);
        try {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    File dstFile = fileSet.fileMap.get(fileName);
                    if (dstFile == null) {
                        HSLog.w(TAG, "Check your XML config for file " + fileName + ". Is the name correct?");
                        continue;
                    }
                    File tempDstFile = new File(dstFile.getAbsolutePath() + ".tmp");
                    boolean created = tempDstFile.createNewFile();
                    HSLog.d(TAG, "Unzipping " + fileName + " from res pack to " + tempDstFile + ", created: " + created);
                    FileOutputStream fos = new FileOutputStream(tempDstFile);
                    try {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            fos.flush();
                        }
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException ignored) {
                        }
                    }
                    boolean deleted = dstFile.delete();
                    if (deleted) {
                        HSLog.d(TAG, "Deleted old file " + dstFile);
                    }
                    boolean renamed = tempDstFile.renameTo(dstFile);
                    HSLog.d(TAG, "Rename unzipped temp file " + tempDstFile + " to " + dstFile + ", success: " + renamed);
                    if (!renamed) {
                        throw new ResourceUnzipError("Failed to rename unzipped file.");
                    }
                } else {
                    HSLog.d(TAG, "Directory in res pack ignored: " + zipEntry);
                }
            }
        } finally {
            try {
                fis.close();
                zis.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void decryptVideo(VideoFileSet fileSet, String wallpaperName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileSet.videoFile);
            byte[] decrypted = decrypt(fis, wallpaperName);
            Utils.writeToFile(fileSet.videoFile, decrypted);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static byte[] decrypt(InputStream input, String wallpaperName) {
        if (null == input || TextUtils.isEmpty(wallpaperName)) {
            return null;
        }

        // Use encrypted file name as decryption key
        String keyName = getHashFileName(wallpaperName);
        String stringKey = encryptString(keyName, getEncryptKey());

        if (TextUtils.isEmpty(stringKey)) {
            return null;
        }

        byte buffer[] = new byte[1024];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count;
            while ((count = input.read(buffer)) >= 0) {
                baos.write(buffer, 0, count);
            }
            byte[] raw = stringKey.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decryptedAES = cipher.doFinal(baos.toByteArray());
            baos.close();
            return decryptedAES;
        } catch (Exception e) {
            HSLog.w(TAG, "Decryption failed: " + e.getMessage());
        }

        return null;
    }

    private static String getHashFileName(String fileName) {
        String salt = "_superapps";
        String nameToHash = fileName + salt;
        return Utils.md5(nameToHash).toUpperCase();
    }

    private static String getEncryptKey() {
        char key[] = {
                0x31, 0x77, 0x75, 0x57,
                0x36, 0x5c, 0x47, 0x46,
                0x31, 0x78, 0x44, 0x34,
                0xe4, 0xd9, 0xb3, 0xc0,
                0x31, 0x52, 0x3c, 0x5d,
                0xa1, 0x8a, 0xd0, 0x24,
                0x76, 0x56, 0x9d, 0xae,
                0x76, 0x2a, 0xea, 0xa6,
                0x43
        };
        for (int i = 0; i < 32; i++) {
            key[i] ^= i * i % 255;
            if (i == 16) {
                key[16] = 0;
            }
        }
        return String.valueOf(key, 0, 16);
    }

    private static String encryptString(String sSrc, String stringKey) {
        String result = "";
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(stringKey.getBytes("UTF-8"),
                    "AES");
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("UTF-8"));
            result = Base64.encodeToString(encrypted, Base64.NO_PADDING | Base64.NO_WRAP);

            int length = result.length();
            if (!TextUtils.isEmpty(result) && length > 16) {
                int startIndex = (length - 16) / 2;
                result = result.substring(startIndex, startIndex + 16);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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

    private void fireCallbackForSuccess(ShaderAndConfettiFileSet fileSet) {
        Threads.runOnMainThread(() -> {
            HSLog.w(TAG, "Successfully loaded wallpaper resources");
            if (mCallbacks != null) {
                if (fileSet != null) {
                    File[] loadedFiles = fileSet.imageFiles;
                    HSLog.w(TAG, "Success");
                    int count = loadedFiles.length;
                    Uri[] uris = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        uris[i] = Uri.fromFile(loadedFiles[i]);
                    }
                    mCallbacks.onLiveWallpaperLoaded(uris);
                } else {
                    mCallbacks.onLiveWallpaperLoaded(null);
                }
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

    private static class ShaderAndConfettiFileSet {
        File zipFile;
        File[] imageFiles;

        Map<String, File> fileMap = new HashMap<>(); // File name in ZIP -> local file

        private String mBuiltWallpaperName;

        private boolean isBuilt(String wallpaperName) {
            return TextUtils.equals(mBuiltWallpaperName, wallpaperName);
        }

        private boolean build(LiveWallpaperManager wallpaperManager, String wallpaperName) {
            if (isBuilt(wallpaperName)) {
                return true;
            }
            ArrayList<String> bg = wallpaperManager.getShaderTextureUrls(wallpaperName);
            ArrayList<String> confetti = wallpaperManager.getConfettiTextureUrls(wallpaperName);
            ArrayList<String> images = new ArrayList<>();
            images.addAll(bg);
            images.addAll(confetti);

            File baseDirectory = CommonUtils.getDirectory(
                    LiveWallpaperConsts.Files.LIVE_DIRECTORY + File.separator + wallpaperName);
            zipFile = new File(baseDirectory, "packed.zip");

            if (images.isEmpty()) {
                return false;
            }

            imageFiles = new File[images.size()];
            for (int i = 0; i < images.size(); i++) {
                String url = images.get(i);
                String remoteFileName = getFileNameFromUrl(url);
                String storedFileName = i < bg.size() ?
                        "image" + String.valueOf(i + 1) + ".png" : remoteFileName;
                File storedDirectory = new File(baseDirectory, i < bg.size() ?
                        LiveWallpaperConsts.LIVE_WALLPAPER_BACKGROUND_DIRECTORY :
                        LiveWallpaperConsts.LIVE_WALLPAPER_CONFETTI_DIRECTORY);
                if (!storedDirectory.exists()) {
                    boolean success = storedDirectory.mkdirs();
                    HSLog.d(TAG, "Create directory: " + success);
                }
                File storedFile = new File(storedDirectory, storedFileName);
                imageFiles[i] = storedFile;
                fileMap.put(remoteFileName, storedFile);
            }
            mBuiltWallpaperName = wallpaperName;
            return true;
        }
    }

    private static class VideoFileSet {
        File videoFile;
    }

    private static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private static class ResourceUnzipError extends Exception {
        ResourceUnzipError(String message) {
            super(message);
        }
    }
}

package com.acb.libwallpaper.live.debug;

import android.os.Bundle;
import android.os.Environment;

import com.acb.libwallpaper.live.base.BaseActivity;
 import com.honeycomb.colorphone.BuildConfig;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity trigger debug actions through ADB commands.
 */
public class DebugActivity extends BaseActivity {

    private static final String TAG = DebugActivity.class.getSimpleName();

    private static final String SEPARATOR_ESCAPED = "//";
    private static final String TEMP_COPY_DIRECTORY = SEPARATOR_ESCAPED + "airlauncher_db_files";

    private static final String ACTION_GC = "com.huandong.wallpaper.live.debug.ACTION_GC";
    private static final String ACTION_COPY_DBS_TO_EXTERNAL = "com.huandong.wallpaper.live.debug.ACTION_COPY_DBS_TO_EXTERNAL";
    private static final String ACTION_REMOVE_EXTERNAL_DBS = "com.huandong.wallpaper.live.debug.ACTION_REMOVE_EXTERNAL_DBS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        switch (action) {
            case ACTION_GC:
                System.gc();
                break;
            case ACTION_COPY_DBS_TO_EXTERNAL:
                copyPersistentStorageFilesToExternalStorage();
                break;
            case ACTION_REMOVE_EXTERNAL_DBS:
                removePersistentStorageFiles();
                break;
        }

        finish();
    }

    /**
     * Copy all database files to external storage for debugging on non-root devices. Use this with p
     */
    static void copyPersistentStorageFilesToExternalStorage() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        HSLog.i(TAG, "Start copying persistent storage files to external directory");
        File external = Environment.getExternalStorageDirectory();
        if (!external.canWrite()) {
            HSLog.w(TAG, "Cannot write to external directory, skip");
            return;
        }
        File tempCopyDir = new File(external, TEMP_COPY_DIRECTORY);
        if (!tempCopyDir.exists()) {
            boolean created = tempCopyDir.mkdir();
            HSLog.d(TAG, "Create: " + tempCopyDir + ", success: " + created);
        }
        List<String> transferredFiles = new ArrayList<>(16);
        try {
            transfer(external, "databases", transferredFiles);
            transfer(external, "shared_prefs", transferredFiles);
        } catch (Exception ignored) {
        }
        HSLog.i(TAG, "Done copying persistent storage files to external directory");
    }

    private static void transfer(File external, String fromDirectory, List<String> outTransferredFiles) throws IOException {
        File data = Environment.getDataDirectory();

        String currentPath = "//data//" + HSApplication.getContext().getPackageName() + "//" + fromDirectory;
        File currentDir = new File(data, currentPath);

        if (!currentDir.exists()) {
            HSLog.i(TAG, "Directory " + fromDirectory + " does NOT exist in app data directory, skip");
            return;
        }

        for (File file : currentDir.listFiles()) {
            String fileName = file.getName();
            outTransferredFiles.add(fileName);
            FileChannel src = new FileInputStream(file).getChannel();
            File backupFile = new File(external, TEMP_COPY_DIRECTORY + SEPARATOR_ESCAPED + fileName);
            FileChannel dst = new FileOutputStream(backupFile).getChannel();
            //noinspection TryFinallyCanBeTryWithResources
            try {
                dst.transferFrom(src, 0, src.size());
            } finally {
                try {
                    src.close();
                    dst.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void removePersistentStorageFiles() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        HSLog.i(TAG, "Start deleting persistent storage files from external directory");
        try {
            File external = Environment.getExternalStorageDirectory();
            File tempDir = new File(external, TEMP_COPY_DIRECTORY);

            if (external.canWrite()) {
                Utils.deleteRecursive(tempDir);
            }
        } catch (Exception ignored) {
        }
        HSLog.i(TAG, "Done deleting persistent storage files from external directory");
    }
}

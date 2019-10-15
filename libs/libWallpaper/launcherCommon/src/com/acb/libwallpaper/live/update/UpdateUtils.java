package com.acb.libwallpaper.live.update;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Networks;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.Locale;
import java.util.Map;

public class UpdateUtils {

    public static final long MARKET_OK = 0;

    public static void startInstall(Context context, Uri uri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }

    public static PackageInfo getApkInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        return pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    static boolean isLocalApkReadyAndLatest(String apkPath, Context context) {
        if (apkPath == null)
            return false;
        PackageInfo apkInfo = getApkInfo(context, apkPath);
        if (apkInfo == null) {
            return false;
        }
        String localPackage = context.getPackageName();
        if (apkInfo.packageName.equals(localPackage)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackage, 0);
                // 1 Apk file is newer than current.
                // 2 Apk file has same version code with config load from server.
                if (apkInfo.versionCode > packageInfo.versionCode && apkInfo.versionCode >= Utils.getLatestVersionCode()) {
                    return false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static File createLocalFile(Context context, String fileName) {
        try {
            File file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            return new File(file, fileName);
        } catch (Exception e) {
            // ignore
        }
        return new File(context.getFilesDir(), ApkDownloadConfig.getUpdateConfig().getLocalFileName());
    }

    private static File getDefaultLocalFile(Context context) {
        return createLocalFile(context, ApkDownloadConfig.getUpdateConfig().getLocalFileName());
    }

    public static boolean update(final Activity context) {
        if (Utils.hasUpdate()) {
            File file = getDefaultLocalFile(context);
            if (isLocalApkReadyAndLatest(file.getPath(), context)) {
                startInstall(context, Uri.fromFile(file));
                return false;
            }

            if (Networks.isNetworkAvailable(-1)){
                ApkUpdateConfirmDialog.show(context);
            } else {
                Toasts.showToast(R.string.update_toast_no_network);
            }
            return true;
        } else {
            Toasts.showToast(R.string.update_toast_already_latest);
            return false;
        }
    }

    private static boolean environmentIsPermitForDownload() {
        boolean configEnabled =  HSConfig.optBoolean(false, "Application", "Update", "AutoDownloadEnabled");
        HSLog.d("Background update:" + configEnabled);

        // Historically, download is only permitted for launcherSP build variant
        //noinspection PointlessBooleanExpression
        return false && configEnabled;
    }

    /**
     * If current App not from Market, download directly.
     */
    public static void doUpdate() {
        HSMarketUtils.browseAPP();
    }

    /**
     * Indicates whether DownloadManager is enabled.
     */
    static boolean isDownloadManagerReady(Context context) {
        try {
            int state = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");

            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Get download file status by id.
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING
     * @see DownloadManager#STATUS_PAUSED
     * @see DownloadManager#STATUS_RUNNING
     * @see DownloadManager#STATUS_SUCCESSFUL
     * @see DownloadManager#STATUS_FAILED
     */
    public static int getDownloadStatus(DownloadManager manager, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = manager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));

                }
            } finally {
                c.close();
            }
        }
        return -1;
    }

    /**
     * Get download file path by id.
     */
    public static String getDownloadFilePath(DownloadManager downloadManager, long id) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
        Cursor cursor = downloadManager.query(query);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public static String getStringForCurrentLanguage(Map<String, String> stringMap) {
        String key = getLanguageString();
        String localeString = stringMap.get(key);
        if (localeString == null) {
            localeString = stringMap.get("Default");
        }
        return localeString;
    }

    public static String getLanguageString() {
        Locale locale = Dimensions.getLocale(HSApplication.getContext());
        String localeString = locale.getLanguage();
        if ("zh".equals(localeString)) {
            String country = locale.getCountry();
            if ("CN".equals(country)) {
                // Simplified Chinese
                localeString = "zh-rCN";
            }
        }
        return localeString;
    }
}

package colorphone.acb.com.libscreencard.gif;

import android.text.TextUtils;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.Map;

public class GifCacheUtils {

    private static final String PREF_KEY_CURRENT_GIF_KEY = "current_gif_key";
    private static final String PREF_KEY_CACHED_GIF_VIEWED = "cached_gif_viewed";
    private static final String DOWNLOAD_DIRECTORY = "security_protection_gif";

    private static Map<String, String> sGifMap = getGif();

    public static boolean haveValidCached() {
        int currentViewedGifKey = getCurrentViewedGifKey();
        String url = sGifMap.get(String.valueOf(currentViewedGifKey));
        HSLog.d("CustomizeContentContainer", "haveValidCached: " + currentViewedGifKey);
        return Downloader.isCachedSuccess(DOWNLOAD_DIRECTORY, url);
    }

    public static boolean haveCached(String url) {
        return Downloader.isCachedSuccess(DOWNLOAD_DIRECTORY, url);
    }

    public static String getCachedGifPath() {
        int key = getCurrentViewedGifKey();
        String url = sGifMap.get(String.valueOf(key));
        return Downloader.getDownloadPath(DOWNLOAD_DIRECTORY, url);
    }

    public static void markCachedGifViewedState(boolean state) {
        Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).putBoolean(PREF_KEY_CACHED_GIF_VIEWED, state);
    }

    private static boolean isCachedGifViewed() {
        return Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).getBoolean(PREF_KEY_CACHED_GIF_VIEWED, false);
    }

    public static String getCachedGifPath(String url) {
        return Downloader.getDownloadPath(DOWNLOAD_DIRECTORY, url);
    }

    public static void updateGif() {
        sGifMap = getGif();
    }

    public static void cacheGif() {
        int key = getCurrentViewedGifKey();
        String url = sGifMap.get(String.valueOf(key));
        if (!TextUtils.isEmpty(url) && !Downloader.isCachedSuccess(DOWNLOAD_DIRECTORY, url)) {
            HSLog.d("CustomizeContentContainer", "cacheGif: " + key);
            Downloader.DownloadItem downloadItem = new Downloader.DownloadItem(url, Downloader.getDownloadPath(DOWNLOAD_DIRECTORY, url));
            SingleDownloadTask downloadTask = new SingleDownloadTask(downloadItem);
            Downloader.getInstance().download(downloadTask, null);
        }
    }

    public static void increaseCurrentViewedGifKey() {
        if (!isCachedGifViewed()) {
            return;
        }
        int newKey = getCurrentViewedGifKey() + 1;
        HSLog.d("CustomizeContentContainer", "increaseCurrentViewedGifKey: " + newKey);
        Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).putInt(PREF_KEY_CURRENT_GIF_KEY, newKey);
        markCachedGifViewedState(false);
    }

    public static void setCurrentViewedGifKey(int key) {
        Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).putInt(PREF_KEY_CURRENT_GIF_KEY, key);
    }

    public static int getCurrentViewedGifKey() {
        return Preferences.get(SecurityFiles.SECURITY_PROTECTION_PREFS).getInt(PREF_KEY_CURRENT_GIF_KEY, 1);
    }

    public static Map<String, String> getGif() {
        return (Map<String, String>) HSConfig.getMap("Application", "GIF");
    }
}

package com.colorphone.ringtones;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.colorphone.ringtones.download2.Downloader;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;

import java.io.File;

public class RingtoneConfig {

    private static RingtoneConfig INSTANCE = new RingtoneConfig();

    public static RingtoneConfig getInstance() {
        return INSTANCE;
    }

    private RingtoneImageLoader mRingtoneImageLoader;
    private WebLauncher mWebLauncher;
    private String mRingtoneDir;
    private RingtoneSetter mRingtoneSetter;
    private RemoteLogger mRemoteLogger = new RemoteLogger() {
        @Override
        public void logEvent(String eventID) {

        }

        @Override
        public void logEvent(String eventID, String... vars) {

        }
    };


    public RemoteLogger getRemoteLogger() {
        return mRemoteLogger;
    }

    public void setRemoteLogger(RemoteLogger remoteLogger) {
        mRemoteLogger = remoteLogger;
    }


    public RingtoneSetter getRingtoneSetter() {
        return mRingtoneSetter;
    }

    public void setRingtoneSetter(RingtoneSetter ringtoneSetter) {
        mRingtoneSetter = ringtoneSetter;
    }


    public RingtoneImageLoader getRingtoneImageLoader() {
        return mRingtoneImageLoader;
    }

    public void setRingtoneImageLoader(RingtoneImageLoader ringtoneImageLoader) {
        mRingtoneImageLoader = ringtoneImageLoader;
    }

    public void startWeb(String url) {
        boolean handleUrl = false;
        if (mWebLauncher != null) {
           handleUrl = mWebLauncher.handleUrl(url);
        }
        if (!handleUrl) {
            Navigations.openBrowser(HSApplication.getContext(), url);
        }
    }

    public void setWebLauncher(WebLauncher webLauncher) {
        mWebLauncher = webLauncher;
    }

    public String getRingtoneFileDir() {
        if (TextUtils.isEmpty(mRingtoneDir)) {
            return getRingtoneFile().getAbsolutePath();
        }
        return mRingtoneDir;
    }

    public void setRingtoneDir(String ringtoneDir) {
        mRingtoneDir = ringtoneDir;
    }

    public static File getRingtoneFile() {
        if (isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_RINGTONES), "download");
            if (!file.exists() && !file.mkdirs()) {
                Log.e("Ringtone File", "Directory not created");
            }
            return file;
        } else {
            File file = new File(Downloader.getDirectory("download"), "ringtone");
            if (!file.exists() && !file.mkdirs()) {
                Log.e("Ringtone File", "Directory not created");
            }
            return file;
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public interface RemoteLogger {
        void logEvent(String eventID);
        void logEvent(String eventID, String... vars);
    }

    public static class NetworkErrHit {
        public String retryButtonText;
        public String statusHit;

        public int statusImageRes;
    }

}

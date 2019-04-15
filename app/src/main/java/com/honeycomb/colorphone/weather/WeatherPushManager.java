package com.honeycomb.colorphone.weather;

import android.content.Context;

import com.acb.call.MediaDownloadManager;
import com.honeycomb.colorphone.Ap;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import java.util.Calendar;

/**
 * Created by zqs on 2019/4/9.
 */
public class WeatherPushManager {

    private static final String SHOW_LEGAL_INTERVAL = "show_legal_interval";


    private WeatherPushManager() {
    }

    public static WeatherPushManager getInstance() {
        return Inner.mInstance;
    }

    static class Inner {
        static WeatherPushManager mInstance = new WeatherPushManager();
    }

    public void push(Context context) {
        if (Ap.WeatherPush.showPush() && inValidTime() && showOncePerValidTime() && isWeatherInfoAvailable()) {
            MediaDownloadManager mediaDownloadManager = new MediaDownloadManager();
            if (Ap.WeatherPush.isSinleVideoType()) {
                if (mediaDownloadManager.isDownloaded(WeatherVideoActivity.REAL)) {
                    WeatherVideoActivity.start(context, WeatherVideoActivity.REAL);
                } else {
                    mediaDownloadManager.downloadMedia(HSConfig.optString("http://cdn.appcloudbox.net/colorphoneapps/weathervideo/real.mp4","Application", "weathervideo",
                            WeatherVideoActivity.REAL), WeatherVideoActivity.REAL, new MediaDownloadManager.DownloadCallback() {
                        @Override
                        public void onUpdate(long progress) {

                        }

                        @Override
                        public void onFail(MediaDownloadManager.MediaDownLoadTask task, String msg) {

                        }

                        @Override
                        public void onSuccess(MediaDownloadManager.MediaDownLoadTask task) {
                            WeatherVideoActivity.start(context, WeatherVideoActivity.REAL);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }
            } else {
                // TODO: 2019/4/15  multi type


            }
        }
    }

    private boolean isWeatherInfoAvailable() {
        // TODO: 2019/4/15
        return true;
    }

    public boolean inValidTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return (6 <= hourOfDay && hourOfDay < 9 && Ap.WeatherPush.showInMor()) || (18 <= hourOfDay && hourOfDay < 21) && Ap.WeatherPush.showAtNight();
    }

    public boolean showOncePerValidTime() {
        //早上6-9点之间可以弹出，晚上6-9点之间可以弹出，早晚各一次
        long lastShowTime = Preferences.getDefault().getLong(SHOW_LEGAL_INTERVAL, 0);
        return System.currentTimeMillis() - lastShowTime > 3 * 60 * 60 * 1000;
    }

    public static boolean isAdReady() {
        // TODO: 2019/4/13 add  ad ready logic
        return true;
    }

}

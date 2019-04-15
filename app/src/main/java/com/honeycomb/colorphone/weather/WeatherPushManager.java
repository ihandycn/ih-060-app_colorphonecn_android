package com.honeycomb.colorphone.weather;

import android.content.Context;
import android.text.format.DateUtils;

import com.acb.call.MediaDownloadManager;
import com.honeycomb.colorphone.Ap;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.Preferences;

import java.util.Calendar;

import colorphone.acb.com.libweather.WeatherClockManager;

/**
 * Created by zqs on 2019/4/9.
 */
public class WeatherPushManager {

    private static final String SHOW_LEGAL_INTERVAL = "show_legal_interval";
    private final MediaDownloadManager mediaDownloadManager;

    private WeatherPushManager() {
        mediaDownloadManager = new MediaDownloadManager();
    }

    public static WeatherPushManager getInstance() {
        return Inner.mInstance;
    }

    static class Inner {
        static WeatherPushManager mInstance = new WeatherPushManager();
    }

    public void push(Context context) {
        if (Ap.WeatherPush.showPush() && inValidTime() && showOncePerValidTime() && isWeatherInfoAvailable()) {

            String videoTypeName = getCurrentVideoType();
            if (videoTypeName == null) {
                HSLog.d("Weather.Push", "Weather data null");
                updateWeatherIfNeeded();
                return;
            }
            if (mediaDownloadManager.isDownloaded(videoTypeName)) {
                WeatherVideoActivity.start(context, videoTypeName);
            } else {
                HSLog.d("Weather.Push", videoTypeName + " has not downloaded");
                mediaDownloadManager.downloadMedia(HSConfig.optString("","Application", "WeatherVideo",
                        videoTypeName), videoTypeName, null);
            }
        }
    }


    public void downloadAllWeatherVideosIfNeeded() {
        if (Ap.WeatherPush.isSinleVideoType()) {
            if (!mediaDownloadManager.isDownloaded(WeatherVideoActivity.REAL)) {
                mediaDownloadManager.downloadMedia(HSConfig.optString("http://cdn.appcloudbox.net/colorphoneapps/weathervideo/real.mp4", "Application", "weathervideo",
                        WeatherVideoActivity.REAL), WeatherVideoActivity.REAL, null);
            }
        } else {
            for (String category : WeatherVideoActivity.allVideoCategory) {
                if (!mediaDownloadManager.isDownloaded(category)) {
                    mediaDownloadManager.downloadMedia(HSConfig.optString("", "Application", "WeatherVideo",
                            category), category, null);
                }
            }
        }
    }

    private String getCurrentVideoType() {
        if (Ap.WeatherPush.isSinleVideoType()) {
            return WeatherVideoActivity.REAL;
        } else {
            HSWeatherQueryResult result = WeatherClockManager.getInstance().getWeather();
            return getWeatherType(result);
        }
    }

    private boolean isWeatherInfoAvailable() {
        return !WeatherClockManager.getInstance().isWeatherUnknown();
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

    public boolean updateWeatherIfNeeded() {
        if (inValidTime()) {
            HSLog.d("Weather.Update", "current time not valid!");
            return false;
        }

        if (!WeatherClockManager.getInstance().isWeatherUnknown()
                && WeatherClockManager.getInstance().hasValidWeatherInTime(3 * DateUtils.HOUR_IN_MILLIS)) {
            HSLog.d("Weather.Update", "current weather data is fresh! ");
            return false;
        }
        HSLog.d("Weather.Update", "updateWeatherIfNeeded!");
        WeatherClockManager.getInstance().updateWeatherIfNeeded();
        return true;
    }

    public String getWeatherType(HSWeatherQueryResult result) {
        if (result == null || result.getCurrentCondition() == null
                || result.getCurrentCondition().getCondition() == null) {
            return null;
        }

        HSWeatherQueryResult.Condition condition = result.getCurrentCondition().getCondition();
        String type;
        switch (condition) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case WARM:
                type = WeatherVideoActivity.SUNNY;
                break;
//            case OVERCAST:
//                id = colorphone.acb.com.libweather.R.string.overcast;
//                break;
            case FAIR:
            case CLEAR:
//                id = R.string.clear;
                type = WeatherVideoActivity.SUNNY;
                break;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                type = WeatherVideoActivity.CLOUDY;
                break;
            case RAIN:
            case CHANCE_OF_RAIN:
                type = WeatherVideoActivity.RAIN;
                break;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                type = WeatherVideoActivity.RAIN;
                break;
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                type = WeatherVideoActivity.RAIN;
                break;
            case SNOW:
            case CHANCE_OF_SNOW:
                type = WeatherVideoActivity.SNOW;
                break;
            case SNOW_SHOWER:
                type = WeatherVideoActivity.SNOW;
                break;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                type = WeatherVideoActivity.RAIN;
                break;
//            case HAZY:
//            case SMOKE:
//            case FOG:
//            case MIST:
//                id = colorphone.acb.com.libweather.R.string.hazy;
//                break;
//            case DUST:
//                id = colorphone.acb.com.libweather.R.string.dust;
//                break;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                type = WeatherVideoActivity.RAIN;
                break;
//            case COLD:
//            case ICY:
//            case FROZEN_MIX:
//            case CHANCE_OF_FROZEN_MIX:
//                id = colorphone.acb.com.libweather.R.string.cold;
//                break;
//            case WINDY:
//                id = colorphone.acb.com.libweather.R.string.windy;
//                break;
//            case HOT:
//                id = colorphone.acb.com.libweather.R.string.hot;
//                break;
            default:
                type = WeatherVideoActivity.REAL;
        }
        return type;
    }


}

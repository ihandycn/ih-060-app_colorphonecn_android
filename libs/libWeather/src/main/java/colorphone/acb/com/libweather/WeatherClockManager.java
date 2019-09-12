package colorphone.acb.com.libweather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.TraceCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.location.HSLocationManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.weather.CurrentCondition;
import com.ihs.weather.DailyForecast;
import com.ihs.weather.HSWeatherQuery;
import com.ihs.weather.HSWeatherQueryListener;
import com.ihs.weather.HSWeatherQueryResult;
import com.ihs.weather.HourlyForecast;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import colorphone.acb.com.libweather.model.LauncherFiles;
import hugo.weaving.DebugLog;

/**
 * Singleton manager class for Weather & Clock widget.
 */
public class WeatherClockManager {

    public static final String TAG = "WeatherClockManager";
    private HSWeatherQueryListener mWeatherQueryListener;

    public enum UpdateStatus {
        INIT,
        UPDATING,
        FAILED,
        SUCCEEDED
    }

    public interface WeatherUpdateListener {
        void onWeatherUpdateFinished();
    }

    public interface WeatherRequestListener {
        void onWeatherRequestStart();
        void onWeatherRequestFinish(boolean success);
    }

    private WeatherRequestListener mWeatherRequestListener;

    public WeatherRequestListener getWeatherRequestListener() {
        return mWeatherRequestListener;
    }

    public void setWeatherRequestListener(WeatherRequestListener weatherRequestListener) {
        mWeatherRequestListener = weatherRequestListener;
    }

    private static WeatherClockManager sInstance;


    private UpdateStatus mStatus;
    private long mLastUpdateTime = Long.MIN_VALUE;

    private CurrentCondition mWeatherCondition;
    private HSWeatherQueryResult mWeatherQueryResult;
    private DailyForecast mTodayForecast;
    private DailyForecast mTomorrowForecast;
    private List<HourlyForecast> mHourlyForecasts;

    public static final String NOTIFICATION_WEATHER_CONDITION_CHANGED = "weather_condition_changed";
    public static final String NOTIFICATION_CLOCK_TIME_CHANGED = "clock_time_changed";
    public static final String NOTIFICATION_THEME_CHANGED = "theme_changed_for_weather_clock";
    public static final String NOTIFICATION_HINT_ANIMATION_START = "command_start_hint_animation";
    public static final String NOTIFICATION_HINT_ANIMATION_STOP = "command_stop_hint_animation";

    private static final String WEATHER_INFO_SHARE_PREF_FILE_NAME = "weather_clock_shared_pref";
    public static final String PREF_KEY_WEATHER_CLOCK_WIDGET_ADDED = "PREF_KEY_WEATHER_CLOCK_WIDGET_HAS_BEEN_ADDED";
    static final String IS_WEATHER_CLOCK_IN_CURRENT_PAGE = "is_weather_clock_in_current_page";

    public static WeatherClockManager getInstance() {
        if (sInstance == null) {
            sInstance = new WeatherClockManager();
        }
        return sInstance;
    }

    public List<HourlyForecast> getHourlyForecasts() {
        return mHourlyForecasts;
    }

    public void setHourlyForecasts(List<HourlyForecast> hourlyForecasts) {
        mHourlyForecasts = hourlyForecasts;
    }


    private WeatherClockManager() {
        Context context = HSApplication.getContext();
        mStatus = UpdateStatus.INIT;
        loadWeather(context, false);
    }

    public HSWeatherQueryResult getWeather() {
        if (mWeatherCondition == null) {
            return null;
        }
        return mWeatherQueryResult;
    }

    public boolean hasValidWeatherInTime(long expiredTimeMills) {
        return mStatus == UpdateStatus.SUCCEEDED
                && (System.currentTimeMillis() - mLastUpdateTime) < expiredTimeMills;
    }

    public void loadWeather(final Context context, final boolean notifyChange) {
        Threads.postOnThreadPoolExecutor(() -> {
            TraceCompat.beginSection(TAG + "#loadWeather");
            try {
                performLoadWeather(context, notifyChange);
            } finally {
                TraceCompat.endSection();
            }
        });
    }

    private void performLoadWeather(final Context context, final boolean notifyChange) {
        Cursor cursor = context.getContentResolver().query(WeatherDataProvider.CONTENT_URI,
                new String[]{WeatherDataProvider.COLUMN_WEATHER},
                null, null, WeatherDataProvider.COLUMN_RANK + " ASC LIMIT 1");
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    HSLog.d(TAG, "Initialize with weather data from DB");
                    String jsonString = cursor.getString(cursor.getColumnIndex(WeatherDataProvider.COLUMN_WEATHER));
                    if (TextUtils.isEmpty(jsonString)) {
                        HSLog.d(TAG, "No weather data in DB");
                        mWeatherCondition = null;
                    } else {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        HSWeatherQueryResult queryResult = null;
                        try {
                            // It's necessary to parse the whole result (not only current condition) so that data
                            // consistency check & fix can be performed with hourly forecasts.
                            queryResult = new HSWeatherQueryResult(jsonObject);
                        } catch (HSWeatherQueryResult.ResponseParseException e) {
                            HSLog.w(TAG, "Error parsing weather data from DB");
                            mWeatherCondition = null;
                            e.printStackTrace();
                        }
                        if (queryResult != null) {
                            mWeatherCondition = queryResult.getCurrentCondition();
                            mWeatherQueryResult = queryResult;
                        }
                    }
                    if (notifyChange) {
                        notifyWeatherConditionChanged();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
    }

    public void updateWeather(final WeatherUpdateListener listener) {
        HSLog.d(TAG, "Update weather");
        if (mStatus == UpdateStatus.UPDATING) {
            HSLog.i(TAG, "Abort local weather update: on-going update");
            return;
        }
        mStatus = UpdateStatus.UPDATING;

        notifyWeatherRequestStart();
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                updateLocalWeather(listener);
            }
        });
    }

    public void notifyWeatherRequestStart() {
        if (mWeatherRequestListener != null) {
            mWeatherRequestListener.onWeatherRequestStart();
        }
    }

    private @Nullable
    Pair<String, Boolean> getFirstItemInfo() {
        Context context = HSApplication.getContext();
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(WeatherDataProvider.CONTENT_URI,
                    new String[]{WeatherDataProvider.COLUMN_QUERY_ID, WeatherDataProvider.COLUMN_IS_LOCAL},
                    null, null, WeatherDataProvider.COLUMN_RANK + " ASC LIMIT 1");
        } catch (SQLiteException e) {
            return null;
        }
        String queryId = null;
        Boolean isLocal = null;
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    queryId = cursor.getString(cursor.getColumnIndex(WeatherDataProvider.COLUMN_QUERY_ID));
                    isLocal = cursor.getInt(cursor.getColumnIndex(WeatherDataProvider.COLUMN_IS_LOCAL)) != 0;
                }
            } finally {
                cursor.close();
            }
        }
        return Pair.create(queryId, isLocal);
    }

    public void setLocalWeather(HSWeatherQueryResult result) {
        mWeatherCondition = result.getCurrentCondition();
        mWeatherQueryResult = result;
        mTodayForecast = getForecastByDay(result, 0);
        mTomorrowForecast = getForecastByDay(result, 1);
        mHourlyForecasts = result.getHourlyForecasts();
        mLastUpdateTime = System.currentTimeMillis();
    }

    private void updateLocalWeather(final WeatherUpdateListener listener) {
        fetchWeather(new HSWeatherQueryListener() {
            @Override
            public void onQueryFinished(boolean success, final HSWeatherQueryResult result) {
//                LauncherAnalytics.logEvent("Weather_Load", "Result", success ? "Succeeded" : "Failed");
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyWeatherRequestFinish(success);
                    }
                });
                if (success) {
                    HSLog.i(TAG, "Local weather update succeeded");
                    Threads.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Preferences.get(LauncherFiles.WEATHER_PREFS).putBoolean(WeatherActivity.PREF_KEY_FIRST_WEATHER_DATA_LOADED, true);
                            HSGlobalNotificationCenter.sendNotification(WeatherActivity.NOTIFICATION_FIRST_WEATHER_DATA_LOADED);
                        }
                    });
                    CurrentCondition newCondition = result.getCurrentCondition();
                    if (newCondition.getCondition() != HSWeatherQueryResult.Condition.UNKNOWN) {
                        setLocalWeather(result);
                        HSLog.d(TAG, "" + mWeatherCondition);
                    }
                    mWeatherCondition = newCondition;
                    mStatus = UpdateStatus.SUCCEEDED;
                    saveWeatherToDatabase(result, true);

                    notifyWeatherUpdateFinished(listener);
                } else {
                    HSLog.w(TAG, "Local weather update failed");
                    mStatus = UpdateStatus.FAILED;
                    notifyWeatherUpdateFinished(listener);
                }
                notifyWeatherConditionChanged();
            }
        });
    }

    public void notifyWeatherRequestFinish(boolean success) {
        if (mWeatherRequestListener != null) {
            mWeatherRequestListener.onWeatherRequestFinish(success);
        }
    }

    public void fetchWeather(final HSWeatherQueryListener listener) {
        mWeatherQueryListener = listener;
        boolean granted = false;
        try {
            granted = RuntimePermissions.checkSelfPermission(HSApplication.getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == RuntimePermissions.PERMISSION_GRANTED;
        } catch (RuntimeException ignored) {
        }
        if (!granted || WeatherUtils.onlyIpLocation()) {
            doFetchWeather(HSLocationManager.LocationSource.IP);
        } else {
            doFetchWeather(HSLocationManager.LocationSource.DEVICE);
        }
    }


    public void doFetchWeather(final HSLocationManager.LocationSource source) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            // Ensure running on main thread as HSLocationManager needs a thread with looper
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    doFetchWeather(source);
                }
            });
            return;
        }
        FrequencyCapLocationFetcher.fetchLocation(source, new FrequencyCapLocationFetcher.LocationListener() {
            @Override
            public void onLocationFetched(boolean success, double lat, double lon) {
                if (success) {
                    fetchWeatherWithLocation(lat, lon);
                } else if (source == HSLocationManager.LocationSource.DEVICE) {
                    FrequencyCapLocationFetcher.fetchLocation(
                            HSLocationManager.LocationSource.IP,
                            new FrequencyCapLocationFetcher.LocationListener() {
                                @Override
                                public void onLocationFetched(boolean success, double lat, double lon) {
                                    if (success) {
                                        fetchWeatherWithLocation(lat, lon);
                                    } else {
                                        mWeatherQueryListener.onQueryFinished(false, null);
                                    }
                                }

                                @Override
                                public void onCountryAndRegionCodeFetched(String countryAndRegion) {
                                }
                            });
                } else {
                    mWeatherQueryListener.onQueryFinished(false, null);
                }
            }

            @Override
            public void onCountryAndRegionCodeFetched(String countryAndRegion) {
            }
        });
    }


    private void fetchWeatherWithLocation(double lat, double lon) {
        HSWeatherQuery weatherQuery = new HSWeatherQuery(lat, lon, new HSWeatherQueryListener() {
            @Override
            public void onQueryFinished(final boolean success, HSWeatherQueryResult result) {
                if (success) {
                    mWeatherQueryListener.onQueryFinished(true, result);
                } else {
                    mWeatherQueryListener.onQueryFinished(false, null);
                }
            }
        });
        weatherQuery.start();
    }


    private void notifyWeatherUpdateFinished(final WeatherUpdateListener listener) {
        if (listener != null) {
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onWeatherUpdateFinished();
                }
            });
        }
    }

    private void updateCityWeatherSync(final String queryId) {
        new HSWeatherQuery(queryId, new HSWeatherQueryListener() {
            @Override
            public void onQueryFinished(boolean success, HSWeatherQueryResult result) {
                if (success) {
                    HSLog.i(TAG, "Weather update succeeded for city: " + queryId);
                    mWeatherCondition = result.getCurrentCondition();
                    mWeatherQueryResult = result;
                    mTodayForecast = getForecastByDay(result, 0);
                    mTomorrowForecast = getForecastByDay(result, 1);
                    mHourlyForecasts = result.getHourlyForecasts();
                    mStatus = UpdateStatus.SUCCEEDED;
                    saveWeatherToDatabaseSync(result, false);
                } else {
                    HSLog.w(TAG, "Weather update failed for city: " + queryId);
                    mStatus = UpdateStatus.FAILED;
                }
                notifyWeatherConditionChanged();
            }
        }).startSync();
    }

    private DailyForecast getForecastByDay(HSWeatherQueryResult result, int offset) {
        List<DailyForecast> dailyForecasts = result.getDailyForecasts();
        if (dailyForecasts.isEmpty() || dailyForecasts.size() <= offset) {
            return null;
        }
        return dailyForecasts.get(offset);
    }

    private static void saveWeatherToDatabase(final HSWeatherQueryResult result, final boolean isLocal) {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                saveWeatherToDatabaseSync(result, isLocal);
            }
        });
    }

    @DebugLog
    private static void saveWeatherToDatabaseSync(HSWeatherQueryResult result, boolean isLocal) {
        ContentResolver cr = HSApplication.getContext().getContentResolver();
        CityData cityData = new CityData(result, isLocal);
        ContentValues values = cityData.getContentValues();

        int updateCount;
        try {
            if (isLocal) {
                updateCount = cr.update(WeatherDataProvider.CONTENT_URI, values, WeatherDataProvider.COLUMN_IS_LOCAL + " = ?",
                        new String[]{"1"});
            } else {
                updateCount = cr.update(WeatherDataProvider.CONTENT_URI, values,
                        WeatherDataProvider.COLUMN_QUERY_ID + " = ?", new String[]{cityData.getQueryId()});
            }
            if (updateCount <= 0) {
                WeatherDataProvider.insertToDatabaseSync(values);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public void updateWeatherIfNeeded() {
        HSLog.d(TAG, "Check if local weather should be updated, status = " + mStatus);

        if (mWeatherCondition == null || mStatus == UpdateStatus.FAILED
                || mWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN) {
            HSLog.d(TAG, "No weather data, local weather update needed");
            updateWeather(null);
        } else if (mStatus == UpdateStatus.SUCCEEDED) {
            long interval = System.currentTimeMillis() / 1000 - mLastUpdateTime / 1000;
            HSLog.d(TAG, interval + " seconds since previous update");
            if (interval > HSConfig.optInteger(3600, "Application", "WeatherUpdateInterval")) {
                updateWeather(null);
            } else if (interval > 1800) {
                // Update weather icon during day and night switch.
                notifyWeatherConditionChanged();
            }
        }
    }

    private void notifyWeatherConditionChanged() {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_WEATHER_CONDITION_CHANGED);
            }
        });
    }

    private String getTemperatureDescription() {
        if (mWeatherCondition == null) {
            return "";
        }
        if (WeatherSettings.shouldDisplayFahrenheit()) {
            return mWeatherCondition.getFahrenheit() + "°F";
        } else {
            return mWeatherCondition.getCelsius() + "°C";
        }
    }

    public @NonNull
    String getLowTemperatureDescription(boolean isToday) {
        DailyForecast forecast;
        if (isToday) {
            if (mTodayForecast == null) {
                return "";
            } else {
                forecast = mTodayForecast;
            }
        } else {
            if (mTomorrowForecast == null) {
                return "";
            } else {
                forecast = mTomorrowForecast;
            }
        }

        int temp;
        if (WeatherSettings.shouldDisplayFahrenheit()) {
            temp = forecast.getLowFahrenheit();
            return temp == HSWeatherQueryResult.UNKNOWN_VALUE ? "" : temp + "°";
        } else {
            temp = forecast.getLowCelsius();
            return temp == HSWeatherQueryResult.UNKNOWN_VALUE ? "" : temp + "°";
        }
    }

    public @NonNull
    String getHighTemperatureDescription(boolean isToday) {
        DailyForecast forecast;
        if (isToday) {
            if (mTodayForecast == null) {
                return "";
            } else {
                forecast = mTodayForecast;
            }
        } else {
            if (mTomorrowForecast == null) {
                return "";
            } else {
                forecast = mTomorrowForecast;
            }
        }

        int temp;
        if (WeatherSettings.shouldDisplayFahrenheit()) {
            temp = forecast.getHighFahrenheit();
            return temp == HSWeatherQueryResult.UNKNOWN_VALUE ? "" : temp + "°";
        } else {
            temp = forecast.getHighCelsius();
            return temp == HSWeatherQueryResult.UNKNOWN_VALUE ? "" : temp + "°";
        }
    }

    public @NonNull
    String getTemperatureDescription(HourlyForecast forecast) {
        if (forecast == null) {
            return "";
        }
        int temp;
        if (WeatherSettings.shouldDisplayFahrenheit()) {
            temp = forecast.getFahrenheit();
        } else {
            temp = forecast.getCelsius();
        }
        return temp == HSWeatherQueryResult.UNKNOWN_VALUE ? "" : temp + "°";
    }

    public @NonNull
    String getSimpleConditionDescription(boolean isToday) {
        DailyForecast forecast;
        if (isToday) {
            if (mTodayForecast == null) {
                return "";
            } else {
                forecast = mTodayForecast;
            }
        } else {
            if (mTomorrowForecast == null) {
                return "";
            } else {
                forecast = mTomorrowForecast;
            }
        }
        return getSimpleConditionDescription(forecast.getCondition());
    }

    public @NonNull
    boolean shouldConditionDescriptionBeBlack(boolean isToday) {
        DailyForecast forecast;
        if (isToday) {
            if (mTodayForecast == null) {
                return false;
            } else {
                forecast = mTodayForecast;
            }
        } else {
            if (mTomorrowForecast == null) {
                return false;
            } else {
                forecast = mTomorrowForecast;
            }
        }
        return TextUtils.equals(getSimpleConditionDescription(forecast.getCondition()),
                HSApplication.getContext().getString(R.string.cloudy));
    }

    public @NonNull
    String getTemperatureDescriptionWithoutUnit(DailyForecast forecast, boolean isHighTemp) {
        if (forecast == null) {
            return "";
        }
        int temp;
        if (WeatherSettings.shouldDisplayFahrenheit()) {
            if (isHighTemp) {
                temp = forecast.getHighFahrenheit();
            } else {
                temp = forecast.getLowFahrenheit();
            }
        } else {
            if (isHighTemp) {
                temp = forecast.getHighCelsius();
            } else {
                temp = forecast.getLowCelsius();
            }
        }
        return temp == HSWeatherQueryResult.UNKNOWN_VALUE ? "" : temp + "°";
    }

    public String getCurrentSimpleConditionDescription() {
        Context context = HSApplication.getContext();
        if (isWeatherUnknown()) {
            return context.getString(R.string.weather_clock_unknown_weather_message);
        }
        String conditionDescription = getSimpleConditionDescription(mWeatherCondition.getCondition());
        String description = getTemperatureDescription();
        if (!TextUtils.isEmpty(conditionDescription)) {
            description += " " + conditionDescription;
        }
        return description;
    }

    public boolean isWeatherUnknown() {
        return mWeatherCondition == null || mWeatherCondition.getCondition() == null;
    }

    /**
     * By "simple" description, we merge original {@link com.ihs.weather.HSWeatherQueryResult.Condition} enum into fewer
     * types and provide localized translation for them.
     *
     * @return Localized simple description string
     */
    public String getSimpleConditionDescription(HSWeatherQueryResult.Condition condition) {
        if (condition == null) {
            return HSApplication.getContext().getString(R.string.weather_clock_unknown_weather_message);
        }
        int id;
        switch (condition) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case WARM:
                id = R.string.sunny;
                break;
            case OVERCAST:
                id = R.string.overcast;
                break;
            case FAIR:
            case CLEAR:
//                id = R.string.clear;
                id = R.string.sunny;
                break;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                id = R.string.cloudy;
                break;
            case RAIN:
            case CHANCE_OF_RAIN:
                id = R.string.rain;
                break;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                id = R.string.drizzle;
                break;
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                id = R.string.rain_shower;
                break;
            case SNOW:
            case CHANCE_OF_SNOW:
                id = R.string.snow;
                break;
            case SNOW_SHOWER:
                id = R.string.snow_shower;
                break;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                id = R.string.sleet;
                break;
            case HAZY:
            case SMOKE:
            case FOG:
            case MIST:
                id = R.string.hazy;
                break;
            case DUST:
                id = R.string.dust;
                break;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                id = R.string.thunderstorm;
                break;
            case COLD:
            case ICY:
            case FROZEN_MIX:
            case CHANCE_OF_FROZEN_MIX:
                id = R.string.cold;
                break;
            case WINDY:
                id = R.string.windy;
                break;
            case HOT:
                id = R.string.hot;
                break;
            default:
                id = R.string.weather_clock_unknown_weather_message;
        }
        return HSApplication.getContext().getString(id);
    }

    public int getWeatherConditionIconResourceID() {
        return WeatherUtils.getWeatherConditionIconResourceId(mWeatherQueryResult);
    }

    public int getWeatherConditionSmallIconResourceID() {
        if (mWeatherCondition == null || mWeatherCondition.getCondition() == null
                || mWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN) {
            return R.drawable.weather_unknown_s;
        }
        return WeatherUtils.getWeatherConditionSmallIconResourceId(mWeatherCondition.getCondition()
                , WeatherUtils.isNight(new CityData.AstronomyInfo(mWeatherCondition)));
    }
}

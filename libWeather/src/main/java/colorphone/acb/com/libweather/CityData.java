package colorphone.acb.com.libweather;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;


import com.ihs.weather.CurrentCondition;
import com.ihs.weather.HSWeatherQueryResult;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model object of a city's basic information and it's last cached weather data.
 */
public class CityData implements Comparable {

    private static final long EXPIRE_AFTER_MILLIS = 2 * 24 * 60 * 60 * 1000;

    /**
     * row id
     */
    private long mId;

    /**
     * Handle of a city that can be used to make a query, eg. "/q/zmw:00000.1.54511" or “/q/China/Beijing”.
     */
    private String mQueryId;

    /**
     * Display name of city.
     */
    private String mDisplayName;

    /**
     * Weather data (in JSON string format) obtained from previous query.
     */
    private String mWeatherJsonString;

    /**
     * Weather data obtained from previous query, constructed from {@link #mWeatherJsonString}.
     */
    private HSWeatherQueryResult mWeatherData;

    /**
     * Epoch of last successful query.
     */
    private long mLastQueryTime = -1;

    /**
     * If an immediate update is necessary.
     */
    private boolean mNeedsUpdate;

    /**
     * Whether this city is a local city (hence should not be removable on UI).
     */
    private boolean mIsLocal;

    /**
     * The preferred rank of this entry when displayed in a list.
     */
    private int mRank = -1;

    public long getId() {
        return mId;
    }

    public String getQueryId() {
        return mQueryId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public HSWeatherQueryResult getWeatherData() {
        if (mWeatherData == null && mWeatherJsonString != null && !mWeatherJsonString.isEmpty()) {
            JSONObject jsonData;
            try {
                jsonData = new JSONObject(mWeatherJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            try {
                mWeatherData = new HSWeatherQueryResult(jsonData);
            } catch (HSWeatherQueryResult.ResponseParseException e) {
                e.printStackTrace();
            }
        }
        return mWeatherData;
    }

    public long getLastQueryTime() {
        return mLastQueryTime;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        mNeedsUpdate = needsUpdate;
    }

    public boolean needsUpdate() {
        return mNeedsUpdate;
    }

    public boolean isLocal() {
        return mIsLocal;
    }

    public int getRank() {
        return mRank;
    }

    public CityData(Cursor c) {
        int idColumnIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_ID);
        int queryIdColumnIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_QUERY_ID);
        int displayNameColumnIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_DISPLAY_NAME);
        int weatherColumnIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_WEATHER);
        int lastQueryTimeIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_LAST_QUERY_TIME);
        int needsUpdateIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_NEEDS_UPDATE);
        int isLocalIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_IS_LOCAL);
        int rankIndex = c.getColumnIndex(WeatherDataProvider.COLUMN_RANK);

        if (idColumnIndex != -1) {
            mId = c.getLong(idColumnIndex);
        }
        if (queryIdColumnIndex != -1) {
            mQueryId = c.getString(queryIdColumnIndex);
        }
        if (displayNameColumnIndex != -1) {
            mDisplayName = c.getString(displayNameColumnIndex);
        }
        if (weatherColumnIndex != -1) {
            mWeatherJsonString = c.getString(weatherColumnIndex);
        }
        if (lastQueryTimeIndex != -1) {
            mLastQueryTime = c.getLong(lastQueryTimeIndex);
        }
        if (needsUpdateIndex != -1) {
            mNeedsUpdate = c.getInt(needsUpdateIndex) != 0;
        }
        if (isLocalIndex != -1) {
            mIsLocal = c.getInt(isLocalIndex) != 0;
        }
        if (rankIndex != -1) {
            mRank = c.getInt(rankIndex);
        }
    }

    public CityData(HSWeatherQueryResult result, boolean isLocal) {
        mQueryId = result.getCityId();
        mDisplayName = result.getCurrentCondition().getCityName();
        mWeatherJsonString = result.getJSONInfoString();
        mLastQueryTime = System.currentTimeMillis();
        mNeedsUpdate = false;
        mIsLocal = isLocal;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        if (mQueryId != null) {
            values.put(WeatherDataProvider.COLUMN_QUERY_ID, mQueryId);
        }
        if (mDisplayName != null) {
            values.put(WeatherDataProvider.COLUMN_DISPLAY_NAME, mDisplayName);
        }
        if (mWeatherJsonString != null) {
            values.put(WeatherDataProvider.COLUMN_WEATHER, mWeatherJsonString);
        }
        if (mLastQueryTime != -1) {
            values.put(WeatherDataProvider.COLUMN_LAST_QUERY_TIME, mLastQueryTime);
        }
        values.put(WeatherDataProvider.COLUMN_NEEDS_UPDATE, mNeedsUpdate);
        values.put(WeatherDataProvider.COLUMN_IS_LOCAL, mIsLocal);
        if (mRank != -1) {
            values.put(WeatherDataProvider.COLUMN_RANK, mRank);
        }
        return values;
    }

    public CityData expireIfNeeded() {
        if (System.currentTimeMillis() - mLastQueryTime > EXPIRE_AFTER_MILLIS) {
            mWeatherJsonString = null;
            mWeatherData = null;
        }
        return this;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if (another instanceof CityData) {
            return mRank - ((CityData) another).mRank;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CityData) && (mId == ((CityData) o).mId || (mIsLocal && ((CityData) o).isLocal()));
    }

    public static class AstronomyInfo {
        public int sunriseHour;
        public int sunriseMinute;
        public int sunsetHour;
        public int sunsetMinute;

        public AstronomyInfo(CurrentCondition current) {
            sunriseHour = current.getSunriseHours();
            sunriseMinute = current.getSunriseMinutes();
            sunsetHour = current.getSunsetHours();
            sunsetMinute = current.getSunsetMinutes();
        }
    }
}

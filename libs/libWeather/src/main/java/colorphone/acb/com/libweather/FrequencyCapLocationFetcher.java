package colorphone.acb.com.libweather;

import android.content.SharedPreferences;
import android.location.Location;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.location.HSLocationManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import colorphone.acb.com.libweather.model.LauncherFiles;

/**
 * Wraps calls to {@link HSLocationManager#fetchLocation(HSLocationManager.LocationSource,
 * HSLocationManager.HSLocationListener)} to provide frequency capping by caching
 * known locations.
 */
public class FrequencyCapLocationFetcher {

    private static final String TAG = FrequencyCapLocationFetcher.class.getSimpleName();

    @SuppressWarnings("WeakerAccess")
    public static final int FETCH_MODE_NORMAL = 0;

    @SuppressWarnings("WeakerAccess")
    public static final int FETCH_MODE_ACTIVE = 1;

    public static final int FETCH_MODE_COUNTRY_AND_REGION = 2;

    public interface LocationListener {
        void onLocationFetched(boolean success, double lat, double lon);

        void onCountryAndRegionCodeFetched(String countryAndRegion);
    }

    public static void fetchLocation(HSLocationManager.LocationSource locationSource,
                              LocationListener listener) {
        fetchLocation(FETCH_MODE_NORMAL, locationSource, listener);
    }

    @SuppressWarnings("WeakerAccess")
    public static void fetchLocation(int fetchMode,
                              HSLocationManager.LocationSource locationSource,
                              LocationListener listener) {
        if (checkFrequencyCapping(fetchMode, locationSource)) {
            performFetch(locationSource, listener);
        } else {
            callbackWithCachedLocation(listener);
        }
    }

    private static boolean checkFrequencyCapping(int fetchMode,
                                          HSLocationManager.LocationSource locationSource) {
        if (fetchMode == FETCH_MODE_COUNTRY_AND_REGION) {
            HSLog.d(TAG, "Request country and region info, fetch needed");
            return true;
        }
        LocationInfo cachedLocation = LocationInfo.fromCache();
        if (locationSource.value() > cachedLocation.mSource.value()) {
            HSLog.d(TAG, "Request " + locationSource + " location but cached "
                    + cachedLocation.mSource + " location, fetch needed");
            return true;
        }
        long minFetchInterval = getMinimumFetchInterval(fetchMode);
        long expiresAt = cachedLocation.mTime + minFetchInterval;
        long now = System.currentTimeMillis();
        HSLog.d(TAG, "Cached location expired: " + (now > expiresAt));
        return now > expiresAt;
    }

    private static long getMinimumFetchInterval(int fetchMode) {
        switch (fetchMode) {
            case FETCH_MODE_NORMAL:
                return 1000 * HSConfig.optInteger((int) (DateUtils.WEEK_IN_MILLIS / 1000),
                        "Application", "Location", "NormalMinFetchIntervalSeconds");
            case FETCH_MODE_ACTIVE:
                return 1000 * HSConfig.optInteger((int) (DateUtils.HOUR_IN_MILLIS / 1000),
                        "Application", "Location", "ActiveMinFetchIntervalSeconds");
        }
        return Long.MAX_VALUE;
    }

    private static void performFetch(HSLocationManager.LocationSource locationSource,
                              LocationListener listener) {
        HSLocationManager.HSLocationListener listenerWrapper = new HSLocationManager.HSLocationListener() {
            @Override
            public void onLocationFetched(boolean success, HSLocationManager hsLocationManager) {
                if (success) {
                    Location location = hsLocationManager.getLocation();
                    listener.onLocationFetched(true,
                            location.getLatitude(), location.getLongitude());
                    cacheLocation(locationSource, hsLocationManager);
                } else {
                    listener.onLocationFetched(false, 0.0, 0.0);
                }
            }

            @Override
            public void onGeographyInfoFetched(boolean success, HSLocationManager hsLocationManager) {
                if (success) {
                    listener.onCountryAndRegionCodeFetched("86");
                }
            }
        };
        HSLocationManager locationManager = new HSLocationManager(HSApplication.getContext());
        locationManager.setDeviceLocationTimeout(5000);
        locationManager.fetchLocation(locationSource, listenerWrapper);
    }

    private static void cacheLocation(HSLocationManager.LocationSource locationSource,
                               HSLocationManager hsLocationManager) {
        LocationInfo fetched = LocationInfo.fromFetchResult(locationSource, hsLocationManager);
        LocationInfo cached = LocationInfo.fromCache();
        if (fetched.wins(cached)) {
            HSLog.d(TAG, "Cache fetched location");
            fetched.writeToCache();
        } else {
            HSLog.d(TAG, "Fetched location is not as good as cached location");
        }
    }

    private static void callbackWithCachedLocation(LocationListener listener) {
        LocationInfo cachedLocation = LocationInfo.fromCache();
        if (cachedLocation != null) {
            HSLog.d(TAG, "Skip fetch, callback with cached location");
            listener.onLocationFetched(true,
                    cachedLocation.mLatitude, cachedLocation.mLongitude);
        } else {
            HSLog.w(TAG, "Detected inconsistency with cache, should NOT happen");
            listener.onLocationFetched(false, 0.0, 0.0);
        }
    }

    private static class LocationInfo {
        private static final String PREF_KEY_TIMESTAMP = "cached_location_timestamp";
        private static final String PREF_KEY_SOURCE = "cached_location_source";
        private static final String PREF_KEY_CATEGORY = "cached_location_category";
        private static final String PREF_KEY_LATITUDE = "cached_location_latitude";
        private static final String PREF_KEY_LONGITUDE = "cached_location_longitude";

        private long mTime;
        private HSLocationManager.LocationSource mSource;
        private HSLocationManager.LocationCategory mCategory = HSLocationManager.LocationCategory.NO_VALUE;
        private double mLatitude;
        private double mLongitude;

        private static LocationInfo fromFetchResult(HSLocationManager.LocationSource locationSource,
                                                    HSLocationManager hsLocationManager) {
            LocationInfo info = new LocationInfo();
            info.mTime = System.currentTimeMillis();
            info.mSource = locationSource;
            if (info.mSource == null) {
                info.mSource = HSLocationManager.LocationSource.NO_VALUE;
            }
            info.mCategory = hsLocationManager.getCategory();
            if (info.mCategory == null) {
                info.mCategory = HSLocationManager.LocationCategory.NO_VALUE;
            }
            Location locationData = hsLocationManager.getLocation();
            info.mLatitude = locationData.getLatitude();
            info.mLongitude = locationData.getLongitude();
            return info;
        }

        private static LocationInfo fromCache() {
            LocationInfo info = new LocationInfo();
            Preferences prefs = Preferences.get(LauncherFiles.COMMON_PREFS);
            info.mTime = prefs.getLong(PREF_KEY_TIMESTAMP, 0L);
            info.mSource = HSLocationManager.LocationSource.valueOf(prefs.getInt(
                    PREF_KEY_SOURCE, HSLocationManager.LocationSource.NO_VALUE.value()));
            info.mCategory = HSLocationManager.LocationCategory.valueOf(prefs.getInt(
                    PREF_KEY_CATEGORY, HSLocationManager.LocationCategory.NO_VALUE.value()));
            info.mLatitude = prefs.getFloat(PREF_KEY_LATITUDE, 0.0f);
            info.mLongitude = prefs.getFloat(PREF_KEY_LONGITUDE, 0.0f);
            return info;
        }

        private void writeToCache() {
            SharedPreferences.Editor prefsEditor = Preferences.get(LauncherFiles.COMMON_PREFS).edit();
            prefsEditor.putLong(PREF_KEY_TIMESTAMP, mTime);
            prefsEditor.putInt(PREF_KEY_SOURCE, mSource.value());
            prefsEditor.putInt(PREF_KEY_CATEGORY, mCategory.value());
            prefsEditor.putFloat(PREF_KEY_LATITUDE, (float) mLatitude);
            prefsEditor.putFloat(PREF_KEY_LONGITUDE, (float) mLongitude);
            prefsEditor.apply();
        }

        private boolean wins(LocationInfo rival) {
            boolean sourceWins = mSource.value() >= rival.mSource.value();
            boolean categoryWins = rival.mCategory == HSLocationManager.LocationCategory.NO_VALUE
                    || mCategory.value() <= rival.mCategory.value();
            return sourceWins && categoryWins;
        }
    }
}

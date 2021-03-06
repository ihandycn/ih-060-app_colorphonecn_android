package colorphone.acb.com.libweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.location.HSLocationManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.weather.HSWeatherQuery;
import com.ihs.weather.HSWeatherQueryListener;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import colorphone.acb.com.libweather.background.BaseWeatherAnimBackground;
import colorphone.acb.com.libweather.base.BaseAppCompatActivity;
import colorphone.acb.com.libweather.model.DeferredHandler;
import colorphone.acb.com.libweather.model.LauncherFiles;
import colorphone.acb.com.libweather.util.CommonUtils;
import colorphone.acb.com.libweather.util.PermissionUtils;
import colorphone.acb.com.libweather.util.Thunk;
import colorphone.acb.com.libweather.util.Utils;
import colorphone.acb.com.libweather.util.ViewUtils;
import colorphone.acb.com.libweather.view.PageIndicator;
import colorphone.acb.com.libweather.view.RecyclerPagerAdapter;
import colorphone.acb.com.libweather.view.StoppableProgressBar;
import hugo.weaving.DebugLog;

/**
 * Weather detail page.
 */
public class WeatherActivity extends BaseAppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, ViewPager.OnPageChangeListener,
        HourlyForecastCurve.CurveAnimationCoordinator, INotificationObserver {

    static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 0;
    public static final String PREF_KEY_WEATHER_DETAIL_VISITED = "weather.detail.visited";

    // NOTICE: This shared preferences value affects CityAdapter's item count. Be sure to call
    // CityAdapter#notifyDataSetChanged() when you write this value.
    public static final String PREF_KEY_FIRST_WEATHER_DATA_LOADED = "weather.first.data.loaded";

    public static final String NOTIFICATION_FIRST_WEATHER_DATA_LOADED = "first_weather_data_loaded";

    public static final String PREF_KEY_WEATHER_LAST_OPEN_TIME = "weather_last_open_time";

    public static final String PREF_KEY_LOCATION_PERMISSION_RATIONALE_SHOWN = "location_permission_rationale_shown";

    private static final int URL_LOADER = 0;
    public static final String[] PROJECTION = {
            WeatherDataProvider.COLUMN_ID,
            WeatherDataProvider.COLUMN_QUERY_ID,
            WeatherDataProvider.COLUMN_DISPLAY_NAME,
            WeatherDataProvider.COLUMN_WEATHER,
            WeatherDataProvider.COLUMN_LAST_QUERY_TIME,
            WeatherDataProvider.COLUMN_NEEDS_UPDATE,
            WeatherDataProvider.COLUMN_IS_LOCAL,
    };

    private TextView mTitleText;
    @Thunk
    PageIndicator mPageIndicator;
    private View mBottomBar;
    private TextView mLastUpdateTimeText;
    @Thunk
    ViewPager mCityPager;
    private StoppableProgressBar mRefreshIndicator;
    private View mRefreshClickable;
    private View mBackground;
    @Thunk WeatherAnimView mWeatherAnimView;
    private LayerDrawable mBackgroundLayer;
    private int mCurPosition;
    private int mVerticalRange = Dimensions.pxFromDp(100f);
    @Thunk int mMaxCityCount;

    @Thunk CityAdapter mAdapter;
    @Thunk WeatherDetailPage mLocalPage;

    @Thunk boolean mFirstLoad;
    private boolean mCurveAnimationPlayed;
    private boolean mSuppressToastOnFailure;

    @Thunk boolean mStopped;
    private Runnable mPendingActionOnStart;

    private PageEventLogger mEventLogger;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mBottomBar = ViewUtils.findViewById(this, R.id.weather_bottom_bar);
        mLastUpdateTimeText = ViewUtils.findViewById(this, R.id.weather_last_update_time);
        mCityPager = ViewUtils.findViewById(this, R.id.weather_city_pager);
        mRefreshIndicator = ViewUtils.findViewById(this, R.id.weather_refresh_indicator);
        mRefreshClickable = ViewUtils.findViewById(this, R.id.weather_refresh_clickable);
        mBackground = ViewUtils.findViewById(this, R.id.weather_background);
        mWeatherAnimView = ViewUtils.findViewById(mBackground, R.id.weather_anim);

        configAppBar();
        ViewUtils.findViewById(this, android.R.id.content).setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mAdapter = new CityAdapter(this);
        mCityPager.setAdapter(mAdapter);
        mCityPager.addOnPageChangeListener(this);
        mRefreshClickable.setOnClickListener(this);

        mFirstLoad = true;
        mCurveAnimationPlayed = false;

        updateWeatherIfNeeded(0);
        bindNonScrollCityData(0, false);

        HSGlobalNotificationCenter.addObserver(WeatherSettingsActivity.NOTIFICATION_DISPLAY_UNIT_CHANGED, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_FIRST_WEATHER_DATA_LOADED, this);

        // Initializes the CursorLoader to load city data
        getLoaderManager().initLoader(URL_LOADER, null, this);

        mMaxCityCount = getResources().getInteger(R.integer.config_weatherCityMaxCount);

        Preferences.get(LauncherFiles.DESKTOP_PREFS).putLong(PREF_KEY_WEATHER_LAST_OPEN_TIME, System.currentTimeMillis());
    }

    private void configAppBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);

        // Title & page indicator
        toolbar.setTitle("");
        ViewGroup toolbarContent = (ViewGroup) getLayoutInflater().inflate(R.layout.weather_toolbar_content, toolbar, false);
        mTitleText = ViewUtils.findViewById(toolbarContent, R.id.weather_toolbar_title);
        mPageIndicator = ViewUtils.findViewById(toolbarContent, R.id.weather_toolbar_page_indicator);
        Toolbar.LayoutParams toolbarParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        toolbarContent.setLayoutParams(toolbarParams);
        toolbar.addView(toolbarContent);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.app_bar_back_white);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStopped = false;
        if (mPendingActionOnStart != null) {
            Runnable onStartAction = mPendingActionOnStart;
            mPendingActionOnStart = null;
            onStartAction.run();
        }
        mWeatherAnimView.bindView(mCityPager);
    }

    private void waitUntilStart(Runnable action) {
        if (mStopped) {
            mPendingActionOnStart = action;
        } else {
            action.run();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Preferences.get(LauncherFiles.WEATHER_PREFS).putBoolean(PREF_KEY_WEATHER_DETAIL_VISITED, true);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.anim.no_anim, R.anim.task_close_exit);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStopped = true;
        mWeatherAnimView.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
        WeatherUtils.clearAnimBitmapCache();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        } else if (i == R.id.action_settings) {
            startActivity(new Intent(this, WeatherSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                return new CursorLoader(this,                    // Context
                        WeatherDataProvider.CONTENT_URI,         // Table to query
                        PROJECTION,                              // Projection to return
                        null,                                    // No selection clause
                        null,                                    // No selection arguments
                        WeatherDataProvider.COLUMN_RANK + " ASC" // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        mAdapter.postNewData(data, new OnDataBoundListener() {
            @Override
            public void onCityDataBound(int localCityIndex) {
                int currentPage = mCityPager.getCurrentItem();

                mPageIndicator.removeAllMarkers(true);
                ArrayList<PageIndicator.PageMarkerResources> markers = new ArrayList<>();
                int count = mAdapter.getCount();
                for (int i = 0; i < count; i++) {
                    if (i == localCityIndex) {
                        // Local
                        markers.add(new PageIndicator.PageMarkerResources(R.drawable.weather_detail_city_local_active,
                                R.drawable.weather_detail_city_local_inactive));
                    } else {
                        markers.add(new PageIndicator.PageMarkerResources());
                    }
                }
                mPageIndicator.addMarkers(markers, true);
                mPageIndicator.setVisibility(View.VISIBLE);

                bindNonScrollCityData(currentPage, false);

                if (mFirstLoad) {
                    mFirstLoad = false;
                    mCityPager.setCurrentItem(Dimensions.isRtl() ? count - 1 : 0);
                    mPageIndicator.setActiveMarker(0);
                }

                mEventLogger = new PageEventLogger(count);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onClick(View v) {
        if (v == mRefreshClickable) {
        }
        if (v.getId() == R.id.weather_city_refresh_btn) {
            mSuppressToastOnFailure = true;
        }
        if (v == mRefreshClickable || v.getId() == R.id.weather_city_refresh_btn) {
            updateWeather(mCityPager.getCurrentItem(), false);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mBackgroundLayer != null && positionOffset != 0) {
            float realPosition = position + positionOffset;
            Drawable inDrawable = null;
            if (realPosition > mCurPosition) {
                inDrawable = mBackgroundLayer.getDrawable(2);
            }
            if (realPosition < mCurPosition) {
                inDrawable = mBackgroundLayer.getDrawable(1);
            }
            if (inDrawable != null) {
                inDrawable.mutate().setAlpha((int) (0xFF * positionOffset));
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        updateWeatherIfNeeded(position);
        bindNonScrollCityData(position, true);
        if (mEventLogger != null) {
            mEventLogger.tryLogPageChangeEvent(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (mCurPosition != mCityPager.getCurrentItem()) {
                mCurPosition = mCityPager.getCurrentItem();
                resetBackgroundLayer();
                resetPageVerticalPosition();
            }
        }
    }

    private void resetPageVerticalPosition() {
        for (int i = 0; i < mCityPager.getChildCount(); i++) {
            View child = mCityPager.getChildAt(i);
            if (child != null && i != mCurPosition && child instanceof WeatherDetailPage) {
                child.scrollTo(0, 0);
            }
        }
    }

    @Thunk void resetBackgroundLayer() {
        Drawable[] drawables = new Drawable[3];
        CityData centerData = mAdapter.getData(mCurPosition);
        if (centerData != null) {
            drawables[1] = ContextCompat.getDrawable(this, WeatherUtils.getWeatherBackgroundResourceId(centerData));
        } else {
            drawables[1] = ContextCompat.getDrawable(this, WeatherUtils.getWeatherBackgroundResourceId(null));
        }
        if (mCurPosition != 0) {
            CityData preData = mAdapter.getData(mCurPosition - 1);
            drawables[0] = ContextCompat.getDrawable(this, WeatherUtils.getWeatherBackgroundResourceId(preData));
        } else {
            drawables[0] = new ColorDrawable(Color.TRANSPARENT);
        }
        if (mCurPosition != mAdapter.getCount() - 1) {
            CityData nextData = mAdapter.getData(mCurPosition + 1);
            if (nextData == null) {
                drawables[2] = ContextCompat.getDrawable(this, WeatherUtils.getWeatherBackgroundResourceId(null));
            } else {
                drawables[2] = ContextCompat.getDrawable(this, WeatherUtils.getWeatherBackgroundResourceId(nextData));
            }
            drawables[2].setAlpha(0);
        } else {
            drawables[2] = new ColorDrawable(Color.TRANSPARENT);
        }
        mBackgroundLayer = new LayerDrawable(drawables);
        mBackground.setBackground(mBackgroundLayer);
    }

    private void updateWeatherIfNeeded(int position) {
        CityData cityData = mAdapter.getData(position);
        if (cityData != null) {
            if (cityData.needsUpdate()) {
                updateWeather(position, false);
                return;
            }
            long timeSinceLastQuerySeconds = (System.currentTimeMillis() - cityData.getLastQueryTime()) / 1000;
            HSLog.d("Weather.Update", cityData.getDisplayName() + ", time since last query: " + timeSinceLastQuerySeconds + " s");
            if (timeSinceLastQuerySeconds > HSConfig.optInteger(3600, "Application", "WeatherUpdateInterval")) {
                updateWeather(position, false);
            }
        }
    }

    private void updateWeather(int position, boolean ipOnly) {
        final CityData cityData = mAdapter.getData(position);
        if (cityData == null) {
            WeatherClockManager.getInstance().updateWeatherIfNeeded();
            return;
        }
        final String queryId = cityData.getQueryId();
        final boolean isLocal = cityData.isLocal();

        mRefreshIndicator.start();
        mLastUpdateTimeText.setVisibility(View.INVISIBLE);
        cityData.setNeedsUpdate(false);
        mAdapter.notifyDataSetChanged();

        HSLog.d("Weather.Update", "Update weather: " + queryId + ", isLocal: " + isLocal);
        if (isLocal) {
            fetchLocalWeather(cityData, ipOnly);
        } else {
            fetchWeather(cityData, queryId);
        }
    }

    private void fetchLocalWeather(final CityData oldData, boolean ipOnly) {
        final HSLocationManager.LocationSource locationSource;

        if (WeatherUtils.onlyIpLocation() || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationSource = HSLocationManager.LocationSource.IP;
        } else {
            HSLog.d("Weather.Update", "Fine location permission OK");
            locationSource = HSLocationManager.LocationSource.DEVICE;
        }
        FrequencyCapLocationFetcher.fetchLocation(
                FrequencyCapLocationFetcher.FETCH_MODE_ACTIVE,
                locationSource,
                new FrequencyCapLocationFetcher.LocationListener() {
                    @Override
                    public void onLocationFetched(boolean success, double lat, double lon) {
                        if (success) {
                            fetchWeather(oldData, lat, lon);
                        } else if (locationSource == HSLocationManager.LocationSource.DEVICE) {
                            FrequencyCapLocationFetcher.fetchLocation(
                                    FrequencyCapLocationFetcher.FETCH_MODE_ACTIVE,
                                    HSLocationManager.LocationSource.IP,
                                    new FrequencyCapLocationFetcher.LocationListener() {
                                        @Override
                                        public void onLocationFetched(boolean success, double lat, double lon) {
                                            if (success) {
                                                fetchWeather(oldData, lat, lon);
                                            } else {
                                                handleWeatherFetchResult(oldData, null);
                                            }
                                        }

                                        @Override
                                        public void onCountryAndRegionCodeFetched(String countryAndRegion) {
                                        }
                                    });

                        } else {
                            handleWeatherFetchResult(oldData, null);
                        }
                    }

                    @Override
                    public void onCountryAndRegionCodeFetched(String countryAndRegion) {
                    }
                });
    }

    private void fetchWeather(final CityData oldData, double lat, double lon) {
        HSWeatherQuery query = new HSWeatherQuery(lat, lon, new HSWeatherQueryListener() {
            @Override
            public void onQueryFinished(boolean success, HSWeatherQueryResult result) {
                handleWeatherFetchResult(oldData, result);
            }
        });
        query.start();
    }

    private void fetchWeather(final CityData oldData, String cityId) {
        HSWeatherQuery query = new HSWeatherQuery(cityId, new HSWeatherQueryListener() {
            @Override
            public void onQueryFinished(boolean success, HSWeatherQueryResult result) {
                handleWeatherFetchResult(oldData, result);
            }
        });
        query.start();
    }

    @Thunk void handleWeatherFetchResult(final CityData oldData, final HSWeatherQueryResult result) {
        final boolean success = result != null;
        mRefreshIndicator.requestStop();
        if (success) {
            WeatherClockManager.getInstance().setLocalWeather(result);
            HSGlobalNotificationCenter.sendNotification(WeatherClockManager.NOTIFICATION_WEATHER_CONDITION_CHANGED, null);
        } else {
            mLastUpdateTimeText.setVisibility(View.VISIBLE);
            if (!mSuppressToastOnFailure) {
                Toasts.showToast(R.string.weather_city_load_failed_message);
            }
        }
        mSuppressToastOnFailure = false;

        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    CityData wrappedData = new CityData(result, oldData.isLocal());
                    ContentValues values = wrappedData.getContentValues();
                    getContentResolver().update(WeatherDataProvider.CONTENT_URI, values,
                            WeatherDataProvider.COLUMN_ID + "=?", new String[]{String.valueOf(oldData.getId())});
                } else {
                    ContentValues values = oldData.getContentValues();
                    values.put(WeatherDataProvider.COLUMN_NEEDS_UPDATE, 1);
                    getContentResolver().update(WeatherDataProvider.CONTENT_URI, values,
                            WeatherDataProvider.COLUMN_ID + "=?", new String[]{String.valueOf(oldData.getId())});
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void bindNonScrollCityData(int positionAbsolute, boolean animated) {
        CityData cityData = mAdapter.getData(positionAbsolute);
        int count = mAdapter.getCount();
        if (count == 0) {
            return;
        }

        int position = Utils.mirrorIndexIfRtl(Dimensions.isRtl(), count, positionAbsolute);
        mPageIndicator.setActiveMarker(position);

        if (cityData != null) { // !hideBottomBar
            // Title
            mTitleText.setText(cityData.getDisplayName());
            mPageIndicator.setVisibility(View.VISIBLE);

            // Updated time
            long lastQueryTime = cityData.getLastQueryTime();
            if (lastQueryTime != -1) {
                SimpleDateFormat format;
                boolean today = WeatherUtils.isToday(lastQueryTime);
                if (today) {
                    format = new SimpleDateFormat("HH:mm");
                } else {
                    format = new SimpleDateFormat("MM-dd");
                }
                String displayTime = format.format(new Date(lastQueryTime));
                mLastUpdateTimeText.setVisibility(View.VISIBLE);
                if (today) {
                    mLastUpdateTimeText.setText(getString(R.string.weather_last_update_today, displayTime));
                } else {
                    mLastUpdateTimeText.setText(getString(R.string.weather_last_update_before_today, displayTime));
                }
            }
        } else {
            if (position == count - 1) {
                mTitleText.setText(getString(R.string.weather_title));
                mPageIndicator.setVisibility(View.VISIBLE);
            } else {
                mTitleText.setText("");
                mPageIndicator.setVisibility(View.INVISIBLE);
            }
        }

        boolean hideBottomBar = (cityData == null);
        if (!hideBottomBar && Float.compare(mBottomBar.getAlpha(), 1f) != 0) {
            if (animated)
                mBottomBar.animate().alpha(1f).setDuration(200).start();
            else
                mBottomBar.setAlpha(1f);
        }
        if (hideBottomBar && Float.compare(mBottomBar.getAlpha(), 0f) != 0) {
            if (animated)
                mBottomBar.animate().alpha(0f).setDuration(200).start();
            else
                mBottomBar.setAlpha(0f);
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (WeatherSettingsActivity.NOTIFICATION_DISPLAY_UNIT_CHANGED.equals(s)) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (int i = 0, count = mCityPager.getChildCount(); i < count; i++) {
                        View page = mCityPager.getChildAt(i);
                        if (page instanceof WeatherDetailPage) {
                            ((WeatherDetailPage) page).applyWeather();
                        }
                    }
                }
            };
            new DeferredHandler().postIdle(r);
        } else if (NOTIFICATION_FIRST_WEATHER_DATA_LOADED.equals(s)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Thunk boolean checkLocationPermission() {
        return RuntimePermissions.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == RuntimePermissions.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateWeather(0, false);
                    if (mLocalPage != null && mLocalPage.isPermissionRequestPending()) {
                    }
                } else {
                    updateWeather(0, true);
                }
                if (mLocalPage != null) {
                    mLocalPage.setLocationPermissionRationaleVisibility(false);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PermissionUtils.REQUEST_PERMISSION:
                if (RuntimePermissions.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == RuntimePermissions.PERMISSION_GRANTED) {
                    updateWeather(0, false);
                    if (mLocalPage != null && mLocalPage.isPermissionRequestPending()) {
                    }
                }
                if (mLocalPage != null) {
                    mLocalPage.setLocationPermissionRationaleVisibility(false);
                }
                break;
        }
    }

    @Override
    public boolean shouldStartCurveAnimation() {
        if (mCurveAnimationPlayed) {
            return false;
        }
        mCurveAnimationPlayed = true;
        return true;
    }

    interface OnDataBoundListener {
        void onCityDataBound(int localCityIndex);
    }

    private class CityAdapter extends RecyclerPagerAdapter<WeatherDetailPage> {
        private Context mContext;
        private final List<CityData> mData = new ArrayList<>();
        private LayoutInflater mInflater;

        CityAdapter(Activity activity) {
            mContext = activity;
            mInflater = activity.getLayoutInflater();
        }

        CityData getData(int positionAbsolute) {
            int position = Utils.mirrorIndexIfRtl(Dimensions.isRtl(), getCount(), positionAbsolute);
            if (position < 0 || position >= mData.size()) {
                return null;
            }
            return mData.get(position);
        }

        @Override
        public int getCount() {
            if (mData.isEmpty()) {
                if (!Preferences.get(LauncherFiles.WEATHER_PREFS).getBoolean(PREF_KEY_FIRST_WEATHER_DATA_LOADED, false)) {
                    return 2; // "Loading" / "Failed" & "Add new city" page
                } else {
                    return 0;
                }
            }
            return mData.size() + 1; // Actual city pages & "Add new city" page
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int positionAbsolute) {
            View itemView = null;
            boolean noValidData = true;
            boolean lastUpdateFailed = false;
            int position = Utils.mirrorIndexIfRtl(Dimensions.isRtl(), getCount(), positionAbsolute);
            if (position < mData.size()) {
                CityData wrappedData = mData.get(position);
                HSWeatherQueryResult weather = wrappedData.getWeatherData();
                boolean isLocal = wrappedData.isLocal();
                if (weather != null) {
                    // Page with weather content
                    noValidData = false;
                    WeatherDetailPage page;
                    if ((page = mPagePool.poll()) == null) {
                        page = (WeatherDetailPage) mInflater.inflate(R.layout.weather_city_detail, container, false);
                    }
                    page.setWeather(weather);

                    Preferences prefs = Preferences.get(LauncherFiles.WEATHER_PREFS);
                    boolean locationPermissionRationaleShown = prefs
                            .getBoolean(PREF_KEY_LOCATION_PERMISSION_RATIONALE_SHOWN, false);
                    boolean shouldShow = isLocal &&
                            !locationPermissionRationaleShown
                            && !checkLocationPermission();
                    if (shouldShow) {
                        prefs.putBoolean(PREF_KEY_LOCATION_PERMISSION_RATIONALE_SHOWN, true);
                    }
                    page.setLocationPermissionRationaleVisibility(shouldShow);

                    page.setTag(wrappedData.getQueryId());
                    page.setOnScrollChangeListener(new WeatherDetailPage.OnScrollChangeListener() {
                        @Override
                        public void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
                            float ratio = 1;
                            if (top > 0 && top <= mVerticalRange) {
                                ratio = 1 - top * 1.0f / mVerticalRange;
                            } else if (top > mVerticalRange) {
                                ratio = 0;
                            }
                            mWeatherAnimView.setVerticalAlpha(ratio);
                            mWeatherAnimView.invalidate();
                        }
                    });
                    if (isLocal) {
                        mLocalPage = page;
                    }
                    itemView = page;
                } else {
                    lastUpdateFailed = wrappedData.needsUpdate();
                }
            }
            if (position == getCount() - 1) {
                // "Add new city" page
                View addNewPage = mInflater.inflate(R.layout.weather_city_add_new, container, false);
                View addNewBtn = ViewUtils.findViewById(addNewPage, R.id.weather_city_add_new_btn);
                View addNewBtnCard = ViewUtils.findViewById(addNewPage, R.id.weather_city_add_new_btn_card);
                View.OnClickListener onClickListener = v -> {
                    if (getCount() - 1 >= mMaxCityCount) {
                        Toasts.showToast(R.string.weather_city_more_than_limit);
                    } else {
                        ActivityOptions opts = ActivityOptions.makeCustomAnimation(WeatherActivity.this,
                                R.anim.grow_fade_in_center, R.anim.no_anim);
                        Bundle optsBundle = opts != null ? opts.toBundle() : null;
                        mContext.startActivity(new Intent(mContext, CitySearchActivity.class), optsBundle);
                    }
                };
                addNewBtn.setOnClickListener(onClickListener);
                addNewBtnCard.setOnClickListener(onClickListener);
                itemView = addNewPage;
            } else if (noValidData) {
                if (!lastUpdateFailed
                        && Preferences.get(LauncherFiles.WEATHER_PREFS).getBoolean(PREF_KEY_FIRST_WEATHER_DATA_LOADED, false)) {
                    // "Loading" page
                    View loadingPage = mInflater.inflate(R.layout.weather_city_loading, container, false);
                    ImageView progressBar = ViewUtils.findViewById(loadingPage, R.id.weather_city_loading_image_view);
                    Animation rotatingAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
                    progressBar.startAnimation(rotatingAnimation);
                    itemView = loadingPage;
                } else {
                    // "Failed to load" page
                    View failurePage = mInflater.inflate(R.layout.weather_city_load_failure, container, false);
                    View refreshBtn = ViewUtils.findViewById(failurePage, R.id.weather_city_refresh_btn);
                    refreshBtn.setOnClickListener((View.OnClickListener) mContext);
                    itemView = failurePage;
                }
            }
            container.addView(itemView);
            return itemView;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        void postNewData(Cursor cursor, final OnDataBoundListener onDataBoundListener) {
            final List<CityData> newData = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                newData.add(new CityData(cursor).expireIfNeeded());
            }
            //noinspection unchecked
            mWeatherAnimView.clearAnimationList();

            for (final CityData cityData : newData) {
                if (cityData == null) {
                    continue;
                }
                Threads.postOnThreadPoolExecutor(() -> {
                    final List<BaseWeatherAnimBackground> anims =
                            WeatherUtils.getWeatherBackgroundAnims(cityData, mWeatherAnimView);
                    int dataIndex = newData.indexOf(cityData);
                    if (Dimensions.isRtl()) {
                        dataIndex = newData.size() - dataIndex;
                    }

                    final int finalDataIndex = dataIndex;
                    Threads.postOnMainThread(() -> waitUntilStart(() -> {
                        mWeatherAnimView.setAnimationList(finalDataIndex, anims);
                        mData.clear();
                        mData.addAll(newData);
                        if (!newData.isEmpty()) {
                            Preferences.get(LauncherFiles.WEATHER_PREFS).putBoolean(PREF_KEY_FIRST_WEATHER_DATA_LOADED, true);
                        }
                        resetBackgroundLayer();
                        notifyDataSetChanged();
                        mWeatherAnimView.refresh();
                        int localIndex = -1;
                        for (int i = 0, count = newData.size(); i < count; i++) {
                            if (newData.get(i).isLocal()) {
                                localIndex = i;
                                break;
                            }
                        }
                        if (onDataBoundListener != null) {
                            onDataBoundListener.onCityDataBound(localIndex);
                        }
                    }));
                });
            }
        }
    }

    private static class PageEventLogger {
        private int mPageCount;

        private int mCurrentPage;

        PageEventLogger(int pageCount) {
            mPageCount = pageCount;
        }

        void tryLogPageChangeEvent(int newPage) {
            if (newPage < mPageCount - 1) {
            } else {
            }
            mCurrentPage = newPage;
        }
    }
}

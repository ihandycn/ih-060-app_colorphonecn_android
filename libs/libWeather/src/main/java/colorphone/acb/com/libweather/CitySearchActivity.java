package colorphone.acb.com.libweather;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.weather.CityInfo;
import com.ihs.weather.HSCitySearchListener;
import com.ihs.weather.HSCitySearchQuery;
import com.ihs.weather.HSCitySearchResult;
import com.ihs.weather.HSWeatherQuery;
import com.ihs.weather.HSWeatherQueryListener;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import colorphone.acb.com.libweather.base.BaseAppCompatActivity;
import colorphone.acb.com.libweather.launcher.LauncherTextWatcher;
import colorphone.acb.com.libweather.launcher.PreventLeakTextWatcher;
import colorphone.acb.com.libweather.util.Alarm;
import colorphone.acb.com.libweather.util.CommonUtils;
import colorphone.acb.com.libweather.util.OnAlarmListener;
import colorphone.acb.com.libweather.util.Thunk;
import colorphone.acb.com.libweather.util.Utils;
import colorphone.acb.com.libweather.util.ViewUtils;
import colorphone.acb.com.libweather.view.recyclerview.SafeLinearLayoutManager;
import hugo.weaving.DebugLog;

/**
 * Weather city search page.
 */
public class CitySearchActivity extends BaseAppCompatActivity implements View.OnClickListener, LauncherTextWatcher {

    /**
     * Interval (in milliseconds) to prevent making search query too often.
     */
    private static final long SEARCH_DEBOUNCE_INTERVAL = 300;
    private static final long SEARCH_TIMEOUT = 6000;

    private EditText mSearchBar;
    private View mSearchClearBtn;
    @Thunk
    RecyclerView mResultView;
    private CitySearchAdapter mResultAdapter;

    private Timer mKeyboardTimer;
    private Alarm mDebounceAlarm = new Alarm();
    private Alarm mTimeoutAlarm = new Alarm();
    private HSCitySearchQuery mSearchQuery;
    private PreventLeakTextWatcher mTextWatcher;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);

        mResultView = ViewUtils.findViewById(this, R.id.weather_city_search_result);
        mResultView.setLayoutManager(new SafeLinearLayoutManager(this));
        mResultAdapter = new CitySearchAdapter(this);
        mResultView.setAdapter(mResultAdapter);

        configAppBar();
        mTextWatcher = new PreventLeakTextWatcher(this);
        mSearchBar.addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKeyboardTimer = new Timer();
        mKeyboardTimer.schedule(new TimerTask() {
            public void run() {
                Utils.showKeyboard(CitySearchActivity.this);
            }
        }, 800);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mKeyboardTimer.cancel();
        Utils.hideKeyboard(this);
    }

    @SuppressLint("InlinedApi")
    private void configAppBar() {
        Toolbar toolbar = (Toolbar) ViewUtils.findViewById(this, R.id.action_bar).findViewById(R.id.inner_tool_bar);

        // Search bar
        toolbar.setTitle("");
        ViewGroup searchBarWrapper = (ViewGroup) getLayoutInflater().inflate(R.layout.weather_city_search_bar,
                toolbar, false);
        mSearchBar = ViewUtils.findViewById(searchBarWrapper, R.id.weather_city_search_edit_text);
        final Typeface typeface = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR);
        mSearchBar.setTypeface(typeface);
        mSearchClearBtn = ViewUtils.findViewById(searchBarWrapper, R.id.weather_city_search_clear_btn);
        mSearchClearBtn.setOnClickListener(this);
        toolbar.addView(searchBarWrapper);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        final String queryUntrimmed = s.toString();
        if (TextUtils.isEmpty(queryUntrimmed)) {
            mSearchClearBtn.setVisibility(View.INVISIBLE);
        } else {
            mSearchClearBtn.setVisibility(View.VISIBLE);
        }
        final String query = Utils.trim(queryUntrimmed);
        if (mDebounceAlarm.alarmPending()) {
            mDebounceAlarm.cancelAlarm();
        }
        mDebounceAlarm.setOnAlarmListener(new OnAlarmListener() {
            @Override
            public void onAlarm(Alarm alarm) {
                performSearch(query);
            }
        });
        mDebounceAlarm.setAlarm(SEARCH_DEBOUNCE_INTERVAL);
    }

    @Thunk void performSearch(String query) {
        if (!TextUtils.isEmpty(query)) {
            mResultAdapter.startSearch();
        }
        if (mSearchQuery != null) {
            HSLog.i("Weather.CitySearch", "Cancel old query");
            mSearchQuery.cancel();
        }
        HSCitySearchQuery search = new HSCitySearchQuery(query, new HSCitySearchListener() {
            @Override
            public void onQueryFinished(boolean success, HSCitySearchResult result) {
                if (mTimeoutAlarm.alarmPending()) {
                    mTimeoutAlarm.cancelAlarm();
                }
                if (success) {
                    HSLog.i("Weather.CitySearch", "Search returned: " + result);
                    mResultAdapter.setSearchResult(result);
                } else {
                    HSLog.w("Weather.CitySearch", "Error in search: " + result);
                    mResultAdapter.searchError();
                }
                mSearchQuery = null;
            }
        });
        HSLog.i("Weather.CitySearch", "Make search: " + query);

        if (mTimeoutAlarm.alarmPending()) {
            mTimeoutAlarm.cancelAlarm();
        }
        mTimeoutAlarm.setOnAlarmListener(new OnAlarmListener() {
            @Override
            public void onAlarm(Alarm alarm) {
                if (mSearchQuery != null) {
                    HSLog.i("Weather.CitySearch", "Cancel timed out query");
                    mSearchQuery.cancel();
                    mResultAdapter.searchError();
                }
            }
        });
        mTimeoutAlarm.setAlarm(SEARCH_TIMEOUT);

        search.start();
        mSearchQuery = search;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == mSearchClearBtn) {
            HSLog.i("Weather.CitySearch", "Clear search query");
            mResultAdapter.setSearchResult(new HSCitySearchResult()); // Clear
            mSearchBar.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchBar.removeTextChangedListener(mTextWatcher);
        mTextWatcher.release();
        cancelSearchQuery();
    }

    private void cancelSearchQuery() {
        if (mSearchQuery != null) {
            mSearchQuery.cancel();
        }
    }

    private class CitySearchAdapter extends RecyclerView.Adapter<CityItemHolder> implements View.OnClickListener {
        private static final int STATE_CITY_LIST = 0;
        private static final int STATE_SEARCHING = 1;
        private static final int STATE_ERROR = 3;

        Context mContext;
        LayoutInflater mLayoutInflater;

        // Data returned in search result
        String mQuery;
        List<CityInfo> mCities;

        private int mState = STATE_CITY_LIST;

        CitySearchAdapter(Context context) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        void startSearch() {
            // Show "searching" prompt only when current result view is empty
            if (getItemCount() == 0) {
                mState = STATE_SEARCHING;
                notifyDataSetChanged();
            }
        }

        void setSearchResult(final HSCitySearchResult result) {
            Threads.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    final List<CityInfo> filteredCities = filterCities(result.getCandidateCities());

                    Threads.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mState = STATE_CITY_LIST;
                            mQuery = result.getQuery();
                            mCities = filteredCities;
                            mResultView.smoothScrollToPosition(0);
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        void searchError() {
            mState = STATE_ERROR;
            notifyDataSetChanged();
        }

        private List<CityInfo> filterCities(@Nullable List<CityInfo> allCities) {
            if (allCities == null) {
                return new ArrayList<>();
            }
            List<CityInfo> filtered = new ArrayList<>(allCities.size());
            Cursor cursor;
            try {
                cursor = getContentResolver().query(WeatherDataProvider.CONTENT_URI,
                        new String[]{WeatherDataProvider.COLUMN_QUERY_ID},
                        null, null, WeatherDataProvider.COLUMN_RANK + " ASC");
            } catch (SQLiteException e) {
                return new ArrayList<>();
            }

            try {
                for (CityInfo city : allCities) {
                    String cityName = city.getCityName();
                    boolean alreadyExisted = false;
                    if (cursor != null && cursor.moveToFirst()) {
                        String newCityId = city.getIdentifier();
                        String queryId = cursor.getString(cursor.getColumnIndex(WeatherDataProvider.COLUMN_QUERY_ID));
                        alreadyExisted = TextUtils.equals(newCityId, queryId);
                        while (cursor.moveToNext() && !alreadyExisted) {
                            queryId = cursor.getString(cursor.getColumnIndex(WeatherDataProvider.COLUMN_QUERY_ID));
                            alreadyExisted = TextUtils.equals(newCityId, queryId);
                        }
                    }

                    if (!alreadyExisted && !TextUtils.isEmpty(cityName) && cityName.matches(".*,.*")) {
                        // Only take cities whose display name contains one and only one ","
                        filtered.add(city);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
            return filtered;
        }

        @Override
        public int getItemCount() {
            if (mState == STATE_SEARCHING || mState == STATE_ERROR) {
                return 1;
            }
            if (mCities == null || TextUtils.isEmpty(mQuery)) {
                return 0;
            }
            if (mCities.isEmpty()) {
                return 1;
            }
            return mCities.size();
        }

        @Override
        public CityItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView item = (TextView) mLayoutInflater.inflate(R.layout.weather_city_search_item, mResultView, false);
            return new CityItemHolder(item);
        }

        @Override
        public void onBindViewHolder(CityItemHolder holder, int position) {
            TextView item = (TextView) holder.itemView;
            if (mState == STATE_SEARCHING) {
                // Searching
                item.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_LIGHT));
                item.setText(getString(R.string.weather_city_searching_prompt));
                clearTagAndOnClickListener(item);
            } else if (mState == STATE_ERROR) {
                item.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_LIGHT));
                item.setText(getString(R.string.weather_city_search_error_prompt));
                clearTagAndOnClickListener(item);
            } else if (!mCities.isEmpty()) {
                DisplayedCityInfo city = new DisplayedCityInfo(mCities.get(position));
                item.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR));
                item.setText(city.getCityName());
                item.setTag(city);
                item.setOnClickListener(this);
            } else {
                // Empty result
                item.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_LIGHT));
                item.setText(getString(R.string.weather_city_empty_result_prompt));
                clearTagAndOnClickListener(item);
            }
        }

        private void clearTagAndOnClickListener(View item) {
            item.setTag(null);
            item.setOnClickListener(null);
        }

        @Override
        public void onClick(View v) {
            DisplayedCityInfo city = (DisplayedCityInfo) v.getTag();
            final String cityName = ((DisplayedCityInfo) v.getTag()).getFilteredDisplayName();
            final String cityId = city.getIdentifier();
            HSLog.i("Weather.Settings", "Add city " + city + " to list");

            Threads.postOnSingleThreadExecutor(new Runnable() {
                @Override
                public void run() {
                    final ContentValues values = new ContentValues();
                    values.put(WeatherDataProvider.COLUMN_DISPLAY_NAME, cityName);
                    values.put(WeatherDataProvider.COLUMN_QUERY_ID, cityId);
                    WeatherDataProvider.insertToDatabaseSync(values);
                }
            });
            HSWeatherQuery query = new HSWeatherQuery(cityId, new HSWeatherQueryListener() {
                @Override
                public void onQueryFinished(final boolean success, final HSWeatherQueryResult result) {
                    Threads.postOnSingleThreadExecutor(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                CityData wrappedData = new CityData(result, false);
                                ContentValues values = wrappedData.getContentValues();
                                getContentResolver().update(WeatherDataProvider.CONTENT_URI, values,
                                        WeatherDataProvider.COLUMN_QUERY_ID + "=?", new String[]{cityId});
                            } else {
                                Cursor cursor;
                                int id = -1;
                                cursor = mContext.getContentResolver().query(WeatherDataProvider.CONTENT_URI,
                                        new String[]{WeatherDataProvider.COLUMN_ID},
                                        WeatherDataProvider.COLUMN_QUERY_ID + "=?", new String[]{cityId},
                                        WeatherDataProvider.COLUMN_ID + " DESC LIMIT 1");
                                if (cursor == null) {
                                    return;
                                }
                                try {
                                    if (cursor.moveToNext()) {
                                        id = cursor.getInt(cursor.getColumnIndex(WeatherDataProvider.COLUMN_ID));
                                    }
                                } finally {
                                    cursor.close();
                                }
                                if (id != -1) {
                                    ContentValues values = new ContentValues();
                                    values.put(WeatherDataProvider.COLUMN_NEEDS_UPDATE, 1);
                                    getContentResolver().update(WeatherDataProvider.CONTENT_URI, values,
                                            WeatherDataProvider.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                                }
                            }
                        }
                    });
                }
            });
            query.start();

            // Back to previous activity
            finish();
        }
    }

    private static class CityItemHolder extends RecyclerView.ViewHolder {
        CityItemHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DisplayedCityInfo {
        private CityInfo mRawCityInfo;

        DisplayedCityInfo(CityInfo rawCityInfo) {
            mRawCityInfo = rawCityInfo;
        }

        String getCityName() {
            return mRawCityInfo.getCityName();
        }

        String getFilteredDisplayName() {
            return mRawCityInfo.getCityName().split(",")[0];
        }

        public String getIdentifier() {
            return mRawCityInfo.getIdentifier();
        }
    }
}

package colorphone.acb.com.libweather;

import android.animation.LayoutTransition;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Fonts;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libweather.settings.BaseSettingsActivity;
import colorphone.acb.com.libweather.util.Thunk;
import colorphone.acb.com.libweather.util.ViewUtils;
import colorphone.acb.com.libweather.view.DragLinearLayout;
import colorphone.acb.com.libweather.view.SwipeRevealLayout;
import hugo.weaving.DebugLog;

/**
 * Weather settings page.
 */
public class WeatherSettingsActivity extends BaseSettingsActivity
        implements View.OnClickListener, SwipeRevealLayout.OnSwipeChangeListener, LoaderManager.LoaderCallbacks<Cursor>,
        CityListItem.OnClickListener, DragLinearLayout.DragListener {

    public static final String NOTIFICATION_DISPLAY_UNIT_CHANGED = "weather.display.unit.changed";

    @Thunk
    ScrollView mScrollable;
    private View mScrollableContent;
    private View mTemperatureUnitBtn;
    private ToggleSwitch mTemperatureToggle;
    private View mDistanceUnitBtn;
    private ToggleSwitch mDistanceToggle;
    @Thunk DragLinearLayout mCityList;
    private View mAddCityView;
    private View mBottomArea;

    private int mMaxCityCount;

    private List<SwipeRevealLayout> mCityItems = new ArrayList<>();

    private static final int URL_LOADER = 0;
    public static final String[] PROJECTION = {
            WeatherDataProvider.COLUMN_ID,
            WeatherDataProvider.COLUMN_DISPLAY_NAME,
            WeatherDataProvider.COLUMN_IS_LOCAL,
            WeatherDataProvider.COLUMN_RANK,
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_weather_settings;
    }

    @Override
    protected int getTitleId() {
        return R.string.weather_settings_title;
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int activeTextColor = ContextCompat.getColor(this, R.color.material_text_black_primary);
        int inactiveTextColor = ContextCompat.getColor(this, R.color.weather_settings_inactive_text);

        Toolbar toolbar = (Toolbar) ViewUtils.findViewById(this, R.id.action_bar).findViewById(R.id.inner_tool_bar);
        mScrollable = ViewUtils.findViewById(this, R.id.weather_settings_scrollable);
        mScrollableContent = ViewUtils.findViewById(this, R.id.weather_settings_scrollable_content);
        mTemperatureUnitBtn = ViewUtils.findViewById(mScrollable, R.id.weather_settings_temperature_unit_clickable);
        TextView celsiusText = ViewUtils.findViewById(mTemperatureUnitBtn, R.id.weather_settings_celsius);
        TextView fahrenheitText = ViewUtils.findViewById(mTemperatureUnitBtn, R.id.weather_settings_fahrenheit);
        mTemperatureToggle = new ToggleSwitch(fahrenheitText, celsiusText, activeTextColor, inactiveTextColor);
        mDistanceUnitBtn = ViewUtils.findViewById(mScrollable, R.id.weather_settings_distance_unit_clickable);
        TextView kilometerText = ViewUtils.findViewById(mDistanceUnitBtn, R.id.weather_settings_kilometer);
        TextView mileText = ViewUtils.findViewById(mDistanceUnitBtn, R.id.weather_settings_mile);
        mDistanceToggle = new ToggleSwitch(mileText, kilometerText, activeTextColor, inactiveTextColor);
        mCityList = ViewUtils.findViewById(mScrollable, R.id.weather_settings_city_list);
        mAddCityView = ViewUtils.findViewById(mCityList, R.id.weather_settings_add_city);
        mBottomArea = ViewUtils.findViewById(this, R.id.weather_settings_bottom_area);

        configUnitSwitches();

        toolbar.setOnClickListener(this);
        final GestureDetector gestureDetector = new GestureDetector(this, new ToolbarGestureListener());
        toolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        mScrollableContent.setOnClickListener(this);
        mAddCityView.setOnClickListener(this);
        mTemperatureUnitBtn.setOnClickListener(this);
        mDistanceUnitBtn.setOnClickListener(this);
        mBottomArea.setOnClickListener(this);

        mCityList.setDragListener(this);
        mCityList.setContainerScrollView(mScrollable);
        mCityList.setLayoutTransition(new LayoutTransition());

        // Initializes a CursorLoader to keep city data in sync
        getLoaderManager().initLoader(URL_LOADER, null, this);

        mMaxCityCount = getResources().getInteger(R.integer.config_weatherCityMaxCount);
    }

    private void configUnitSwitches() {
        mTemperatureToggle.switchTo(WeatherSettings.shouldDisplayFahrenheit());
        mDistanceToggle.switchTo(WeatherSettings.shouldDisplayEnglishMetric());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!closeOpeningSwipeRevealItem(null)) {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onClick(View v) {
        if (closeOpeningSwipeRevealItem(null)) {
            return;
        }
        if (v == mAddCityView) {
            if(mCityList.getChildCount() - 1 >= mMaxCityCount) {
                Toasts.showToast(R.string.weather_city_more_than_limit);
            } else {
                startActivity(new Intent(this, CitySearchActivity.class));
            }
        } else if (v == mTemperatureUnitBtn) {
            boolean newState = !WeatherSettings.shouldDisplayFahrenheit();
            WeatherSettings.setDisplayFahrenheit(newState);
            mTemperatureToggle.switchTo(newState);
//            NotificationManager.getInstance().updateCpuCooler();
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_DISPLAY_UNIT_CHANGED);
        } else if (v == mDistanceUnitBtn) {
            boolean newState = !WeatherSettings.shouldDisplayEnglishMetric();
            WeatherSettings.setDisplayEnglishMetric(newState);
            mDistanceToggle.switchTo(newState);
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_DISPLAY_UNIT_CHANGED);
        } else if (v == mScrollableContent) {
            // No particular operation
        } else if (v == mBottomArea) {
            // No particular operation
        }
    }

    /**
     * Implementation of {@link LoaderManager.LoaderCallbacks}.
     */
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

    /**
     * Implementation of {@link LoaderManager.LoaderCallbacks}.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }

        int totalCityCount = data.getCount();
        List<CityData> cities = new ArrayList<>(totalCityCount);
        while (data.moveToNext()) {
            CityData city = new CityData(data);
            boolean childFound = false;
            for (int i = 0, count = mCityList.getChildCount(); i < count; ++i) {
                View child = mCityList.getChildAt(i);
                if (city.equals(child.getTag())) {
                    childFound = true;
                }
            }
            if (!childFound) {
                cities.add(city);
            }
        }
        //noinspection unchecked
        boolean isFirstLoad = cities.size() == totalCityCount;
        for (int i = 0, count = cities.size(); i < count; i++) {
            CityData city = cities.get(i);
            CityListItem cityView = (CityListItem) getLayoutInflater().inflate(R.layout.weather_settings_city_item,
                    mCityList, false);
            cityView.bind(city);
            cityView.setOnViewClickListener(this);
            cityView.setOnSwipeChangeListener(this);
            mCityItems.add(cityView);
            mCityList.addDragView(cityView, cityView.getDragHandle(), mCityList.getChildCount() - 1);
        }
    }

    /**
     * Implementation of {@link LoaderManager.LoaderCallbacks}.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // No operation
    }

    /**
     * Implementation of {@link SwipeRevealLayout.OnSwipeChangeListener}.
     */
    @Override
    public void onOpen(SwipeRevealLayout layout) {
        closeOpeningSwipeRevealItem(layout);
    }

    /**
     * Implementation of {@link SwipeRevealLayout.OnSwipeChangeListener}.
     */
    @Override
    public void onClose(SwipeRevealLayout layout) {
    }

    /**
     * Implementation of {@link SwipeRevealLayout.OnSwipeChangeListener}.
     */
    @Override
    public void onDragging(SwipeRevealLayout layout) {
    }

    /**
     * Implementation of {@link SwipeRevealLayout.OnSwipeChangeListener}.
     */
    @Override
    public void onStartOpen(SwipeRevealLayout layout) {
        closeOpeningSwipeRevealItem(layout);
    }

    /**
     * Implementation of {@link SwipeRevealLayout.OnSwipeChangeListener}.
     */
    @Override
    public void onStartClose(SwipeRevealLayout layout) {
    }

    @Override
    public void onBackPressed() {
        if (!closeOpeningSwipeRevealItem(null)) {
            super.onBackPressed();
        }
    }

    private boolean closeOpeningSwipeRevealItem(SwipeRevealLayout exceptionLayout) {
        boolean needClose = false;
        if (exceptionLayout == null) {
            // Close all
            for (SwipeRevealLayout item : mCityItems) {
                if (item.getStatus() == SwipeRevealLayout.Status.OPEN) {
                    needClose = true;
                    item.close();
                }
            }
        } else {
            // Close other item
            CityData cityData = (CityData) exceptionLayout.getTag();
            for (SwipeRevealLayout item : mCityItems) {
                if (item.getStatus() == SwipeRevealLayout.Status.OPEN && !cityData.equals(item.getTag())) {
                    needClose = true;
                    item.close();
                }
            }
        }
        return needClose;
    }

    /**
     * Implementation of {@link CityListItem.OnClickListener}.
     */
    @Override
    public void onClickFrontView(SwipeRevealLayout layout) {
        closeOpeningSwipeRevealItem(null);
    }

    /**
     * Implementation of {@link CityListItem.OnClickListener}.
     */
    @Override
    public void onClickBackView(SwipeRevealLayout layout) {
        mCityList.removeDragView(layout);
        mCityItems.remove(layout);

        refreshRanks();
    }

    /**
     * Implementation of {@link CityListItem.OnClickListener}.
     */
    @Override
    public void onClickActionButton(SwipeRevealLayout layout) {
    }

    /**
     * Implementation of {@link DragLinearLayout.DragListener}.
     */
    @Override
    public void onDragStart() {
    }

    /**
     * Implementation of {@link DragLinearLayout.DragListener}.
     */
    @Override
    public void onDragStop() {
        refreshRanks();
    }

    /**
     * Implementation of {@link DragLinearLayout.DragListener}.
     */
    @Override
    public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
        HSLog.d("WeatherSettings.Drag", "Swap view at " + firstPosition + " with view at " + secondPosition);
    }

    private void refreshRanks() {
        final int count = mCityList.getChildCount() - 1;
        final List<Long> cityIdsInNewOrder = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CityListItem cityView = (CityListItem) mCityList.getChildAt(i);
            if (i == 0) {
                cityView.setIsFirstItem();
            } else {
                cityView.setIsFirstItem();
            }
            CityData cityData = (CityData) cityView.getTag();
            cityIdsInNewOrder.add(cityData.getId());
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                ArrayList<ContentProviderOperation> ops = new ArrayList<>(count);
                for (int rank = 0; rank < count; rank++) {
                    long id = cityIdsInNewOrder.get(rank);
                    ContentValues values = new ContentValues();
                    values.put(WeatherDataProvider.COLUMN_RANK, rank);
                    ops.add(ContentProviderOperation.newUpdate(WeatherDataProvider.CONTENT_URI)
                            .withValues(values)
                            .withSelection(WeatherDataProvider.COLUMN_ID + " = ?", new String[]{String.valueOf(id)})
                            .build());
                }
                try {
                    getContentResolver().applyBatch(WeatherDataProvider.AUTHORITY, ops);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }
                WeatherClockManager.getInstance().loadWeather(WeatherSettingsActivity.this, true);
            }
        });
    }

    private class ToolbarGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            mScrollable.smoothScrollTo(0, 0);
            return true;
        }
    }

    private static class ToggleSwitch {
        private static final Fonts.Font NORMAL_TYPEFACE = Fonts.Font.CUSTOM_FONT_REGULAR;
        private static final Fonts.Font HIGHLIGHT_TYPEFACE = Fonts.Font.CUSTOM_FONT_SEMIBOLD;

        private TextView mOnActiveView;
        private TextView mOffActiveView;
        private int mActiveColor;
        private int mInActiveColor;

        ToggleSwitch(TextView onActiveView, TextView offActiveView, int activeColor, int inActiveColor) {
            mOnActiveView = onActiveView;
            mOffActiveView = offActiveView;
            mActiveColor = activeColor;
            mInActiveColor = inActiveColor;
        }

        void switchTo(boolean on) {
            if (on) {
                mOnActiveView.setTextColor(mActiveColor);
                mOnActiveView.setTypeface(Fonts.getTypeface(HIGHLIGHT_TYPEFACE));
                mOffActiveView.setTextColor(mInActiveColor);
                mOffActiveView.setTypeface(Fonts.getTypeface(NORMAL_TYPEFACE));
            } else {
                mOnActiveView.setTextColor(mInActiveColor);
                mOnActiveView.setTypeface(Fonts.getTypeface(NORMAL_TYPEFACE));
                mOffActiveView.setTextColor(mActiveColor);
                mOffActiveView.setTypeface(Fonts.getTypeface(HIGHLIGHT_TYPEFACE));
            }
        }
    }
}

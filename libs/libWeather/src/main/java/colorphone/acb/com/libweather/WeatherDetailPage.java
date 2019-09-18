package colorphone.acb.com.libweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ihs.weather.CurrentCondition;
import com.ihs.weather.DailyForecast;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.RuntimePermissions;

import java.util.List;
import java.util.Locale;

import colorphone.acb.com.libweather.util.CommonUtils;
import colorphone.acb.com.libweather.util.PermissionUtils;
import colorphone.acb.com.libweather.util.ViewUtils;

/**
 * View for weather detail page.
 */
public class WeatherDetailPage extends ScrollView implements Comparable {

    private Activity mActivity;

    private View mScrollContent;
    private TextView mCurrentTempText;
    private TextView mHighLowTempText;
    private TextView mCurrentConditionText;
    private HourlyForecastScrollView mHourlyView;
    private DailyForecastListView mDailyView;
    private TextView mWindSpeedText;
    private TextView mWindDirecText;
    private TextView mHumidityText;
    private TextView mSunriseTimeText;
    private TextView mSunsetTimeText;
    private View mPermissionRationale;

    private HSWeatherQueryResult mWeather;

    private OnScrollChangeListener mOnScrollChangeListener;
    private ScrollEventLogger mEventLogger;

    private boolean mPermissionRequestPending;

    public interface OnScrollChangeListener {
        void onScrollChanged(int left, int top, int oldLeft, int oldTop);
    }

    public WeatherDetailPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mActivity = CommonUtils.getActivity(getContext());

        mScrollContent = ViewUtils.findViewById(this, R.id.weather_scrollable);
        mCurrentTempText = ViewUtils.findViewById(this, R.id.weather_current_temp);
        mHighLowTempText = ViewUtils.findViewById(this, R.id.weather_high_low_temp);
        mCurrentConditionText = ViewUtils.findViewById(this, R.id.weather_current_condition);
        mHourlyView = ViewUtils.findViewById(this, R.id.weather_hourly_forecast);
        mDailyView = ViewUtils.findViewById(this, R.id.weather_daily_forecast);
        mWindSpeedText = ViewUtils.findViewById(this, R.id.weather_wind_speed);
        mWindDirecText = ViewUtils.findViewById(this, R.id.weather_wind_direction);
        mHumidityText = ViewUtils.findViewById(this, R.id.weather_humidity);
        mSunriseTimeText = ViewUtils.findViewById(this, R.id.weather_sunrise_time);
        mSunsetTimeText = ViewUtils.findViewById(this, R.id.weather_sunset_time);

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mEventLogger = new ScrollEventLogger(WeatherDetailPage.this, mScrollContent, mDailyView);
                }
            });
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        mEventLogger.tryLogScrollUpEvent(t, oldt);
        mEventLogger.tryLogScrollDownEvent(t, oldt);
        mEventLogger.tryLogScrollToDailyForecastEvent(t);
        mEventLogger.tryLogScrollToBottomEvent(t);

        mOnScrollChangeListener.onScrollChanged(l, t, oldl, oldt);
    }

    public void setOnScrollChangeListener(OnScrollChangeListener listener) {
        this.mOnScrollChangeListener = listener;
    }

    public void setWeather(HSWeatherQueryResult weather) {
        mWeather = weather;
        if (mEventLogger != null) {
            mEventLogger.reset();
        }
        applyWeather();
    }

    public void setLocationPermissionRationaleVisibility(boolean visible) {
        if (mPermissionRationale != null) {
            if (visible) {
                mPermissionRationale.setVisibility(VISIBLE);
            } else {
                mPermissionRationale.setVisibility(GONE);
                mPermissionRequestPending = false;
            }
        }
    }

    boolean isPermissionRequestPending() {
        return mPermissionRequestPending;
    }

    @SuppressLint("SetTextI18n")
    public void applyWeather() {
        boolean fahrenheit = WeatherSettings.shouldDisplayFahrenheit();
        boolean englishMetric = WeatherSettings.shouldDisplayEnglishMetric();

        CurrentCondition currentCondition = mWeather.getCurrentCondition();
        if (currentCondition != null) {
            int temperature = fahrenheit ? currentCondition.getFahrenheit() : currentCondition.getCelsius();
            mCurrentTempText.setText(WeatherUtils.filterInt(temperature));
            WeatherClockManager manager = WeatherClockManager.getInstance();
            String simpleConditionDesc = manager.getSimpleConditionDescription(currentCondition.getCondition());
            mCurrentConditionText.setText(simpleConditionDesc);
        }
        List<DailyForecast> dailyForecasts = mWeather.getDailyForecasts();
        if (dailyForecasts.size() > 0) {
            DailyForecast todayForecast = dailyForecasts.get(0);
            if (fahrenheit) {
                mHighLowTempText.setText(getContext().getString(R.string.weather_high_low_temperature,
                        WeatherUtils.filterInt(todayForecast.getLowFahrenheit()),
                        WeatherUtils.filterInt(todayForecast.getHighFahrenheit())));
            } else {
                mHighLowTempText.setText(getContext().getString(R.string.weather_high_low_temperature,
                        WeatherUtils.filterInt(todayForecast.getLowCelsius()),
                        WeatherUtils.filterInt(todayForecast.getHighCelsius())));
            }
        }

        mHourlyView.bindHourlyForecasts(mWeather.getHourlyForecasts(), new CityData.AstronomyInfo(currentCondition));
        mDailyView.bindDailyForecasts(dailyForecasts);

        String placeholder = getContext().getString(R.string.weather_no_info_placeholder);
        if (currentCondition != null) {
            if (englishMetric) {
                String windSpeedStr = WeatherUtils.filterInt((int) currentCondition.getWindSpeedMph());
                if (windSpeedStr.equals(placeholder)) {
                    mWindSpeedText.setText(placeholder);
                } else {
                    mWindSpeedText.setText(String.format(Locale.getDefault(), "%s %s", windSpeedStr,
                            getContext().getString(R.string.weather_mph)));
                }
            } else {
                String windSpeedStr = WeatherUtils.filterInt((int) currentCondition.getWindSpeedKph());
                if (windSpeedStr.equals(placeholder)) {
                    mWindSpeedText.setText(placeholder);
                } else {
                    mWindSpeedText.setText(String.format(Locale.getDefault(), "%s %s", windSpeedStr,
                            getContext().getString(R.string.weather_kph)));
                }
            }
            HSWeatherQueryResult.Direction windDirection = currentCondition.getWindDirection();
            if (windDirection != null) {
                mWindDirecText.setText(windDirection.getDescription());
            } else {
                mWindDirecText.setText(getContext().getString(R.string.weather_no_info_placeholder));
            }

            String humidityStr = WeatherUtils.filterDouble(currentCondition.getHumidity(), 0);
            if (humidityStr.equals(placeholder)) {
                mHumidityText.setText(placeholder);
            } else {
                mHumidityText.setText(humidityStr + "%");
            }
            mSunriseTimeText.setText(getContext().getString(R.string.weather_time,
                    WeatherUtils.filterInt(currentCondition.getSunriseHours(), true),
                    WeatherUtils.filterInt(currentCondition.getSunriseMinutes(), true)));
            mSunsetTimeText.setText(getContext().getString(R.string.weather_time,
                    WeatherUtils.filterInt(currentCondition.getSunsetHours(), true),
                    WeatherUtils.filterInt(currentCondition.getSunsetMinutes(), true)));
        } else {
            mWindSpeedText.setText(placeholder);
            mWindDirecText.setText(placeholder);
            mHumidityText.setText(placeholder);
            mSunriseTimeText.setText(getContext().getString(R.string.weather_time,
                    placeholder, placeholder));
            mSunsetTimeText.setText(getContext().getString(R.string.weather_time,
                    placeholder, placeholder));
        }
    }

    @Override
    public int compareTo(@NonNull Object another) {
        // For recycled by RecyclerPagerAdapter
        return 0;
    }

    private static class ScrollEventLogger {
        private boolean mScrollUpEventLogged;
        private boolean mScrollDownEventLogged;
        private boolean mScrollToDailyForecastEventLogged;
        private boolean mScrollToBottomEventLogged;

        private int mDailyForecastRevealTop;
        private int mMaxTop;

        ScrollEventLogger(ScrollView scrollView, View scrollContent, View dailyForecastView) {
            mDailyForecastRevealTop = dailyForecastView.getBottom() - scrollView.getHeight();
            mMaxTop = scrollContent.getHeight() - scrollView.getHeight();
        }

        public void reset() {
            mScrollUpEventLogged = false;
            mScrollDownEventLogged = false;
            mScrollToDailyForecastEventLogged = false;
            mScrollToBottomEventLogged = false;
        }

        void tryLogScrollUpEvent(int top, int oldTop) {
            if (!mScrollUpEventLogged && top > oldTop) {
                mScrollUpEventLogged = true;
            }
        }

        void tryLogScrollDownEvent(int top, int oldTop) {
            if (!mScrollDownEventLogged && top < oldTop) {
                mScrollDownEventLogged = true;
            }
        }

        void tryLogScrollToDailyForecastEvent(int top) {
            if (!mScrollToDailyForecastEventLogged && top >= mDailyForecastRevealTop) {
                mScrollToDailyForecastEventLogged = true;
//                LauncherAnalytics.logEvent("Weather_Detail_TenDaysForecast_Viewed");
            }
        }

        void tryLogScrollToBottomEvent(int top) {
            if (!mScrollToBottomEventLogged && top >= mMaxTop) {
                mScrollToBottomEventLogged = true;
//                LauncherAnalytics.logEvent("Weather_Detail_Bottom_Viewed");
            }
        }
    }
}

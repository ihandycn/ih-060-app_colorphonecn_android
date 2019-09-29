package colorphone.acb.com.libweather;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

import com.ihs.weather.HourlyForecast;

import java.util.List;

import colorphone.acb.com.libweather.util.ViewUtils;

/**
 * View for displaying hourly weather forecast that can scroll horizontally.
 */
public class HourlyForecastScrollView extends HorizontalScrollView {

    private HourlyForecastCurve mHourlyForecastCurve;
    private HourlyForecastIcons mHourlyForecastIcons;

    private boolean mScrollLeftEventLogged;
    private boolean mScrollRightEventLogged;

    public HourlyForecastScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        setHorizontalScrollBarEnabled(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHourlyForecastCurve = ViewUtils.findViewById(this, R.id.weather_hourly_forecast_curve);
        mHourlyForecastIcons = ViewUtils.findViewById(this, R.id.weather_hourly_forecast_icons);
    }

    public void bindHourlyForecasts(List<HourlyForecast> hourlyForecasts, CityData.AstronomyInfo astronomyInfo) {
        if (hourlyForecasts == null || hourlyForecasts.isEmpty()) {
            setVisibility(GONE);
            return;
        } else {
            setVisibility(VISIBLE);
        }

        mScrollLeftEventLogged = false;
        mScrollRightEventLogged = false;

        mHourlyForecastCurve.bindHourlyForecasts(hourlyForecasts);
        mHourlyForecastIcons.bindHourlyForecasts(hourlyForecasts, astronomyInfo);
        scrollTo(0, 0);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (!mScrollLeftEventLogged && l > oldl) {
            mScrollLeftEventLogged = true;
//            LauncherAnalytics.logEvent("Weather_Detail_Hours_HorizontalSlide", "type", "Left");
        }
        if (!mScrollRightEventLogged && l < oldl) {
            mScrollRightEventLogged = true;
//            LauncherAnalytics.logEvent("Weather_Detail_Hours_HorizontalSlide", "type", "Right");
        }
    }
}

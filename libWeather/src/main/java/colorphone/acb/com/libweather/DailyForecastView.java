package colorphone.acb.com.libweather;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.weather.DailyForecast;

import colorphone.acb.com.libweather.util.ViewUtils;

/**
 * View for displaying one daily forecast row.
 */
public class DailyForecastView extends RelativeLayout {

    private TextView mWeekdayText;
    private ImageView mConditionIcon;
    private TextView mTempText;

    public DailyForecastView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWeekdayText = ViewUtils.findViewById(this, R.id.weather_daily_forecast_weekday);
        mConditionIcon = ViewUtils.findViewById(this, R.id.weather_daily_forecast_condition_icon);
        mTempText = ViewUtils.findViewById(this, R.id.weather_daily_forecast_temperature);
    }

    public void bindDailyForecast(DailyForecast dailyForecast, boolean today) {
        if (today) {
            mWeekdayText.setText(getContext().getString(R.string.weather_today));
        } else {
            mWeekdayText.setText(WeatherUtils.getWeekDayString(dailyForecast.getWeekday()));
        }
        mConditionIcon.setImageResource(
                WeatherUtils.getWeatherConditionSmallIconResourceId(dailyForecast.getCondition(), false));
        if (WeatherSettings.shouldDisplayFahrenheit()) {
            mTempText.setText(getContext().getString(R.string.weather_high_low_temperature,
                    WeatherUtils.filterInt(dailyForecast.getLowFahrenheit()),
                    WeatherUtils.filterInt(dailyForecast.getHighFahrenheit())));
        } else {
            mTempText.setText(getContext().getString(R.string.weather_high_low_temperature,
                    WeatherUtils.filterInt(dailyForecast.getLowCelsius()),
                    WeatherUtils.filterInt(dailyForecast.getHighCelsius())));
        }
    }
}

package colorphone.acb.com.libweather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ihs.weather.DailyForecast;

import java.util.ArrayList;
import java.util.List;

/**
 * View for displaying daily weather forecasts of following several days.
 */
public class DailyForecastListView extends LinearLayout {

    private LayoutInflater mLayoutInflater;
    private List<DailyForecastView> mDailyViewPool = new ArrayList<>(10);

    public DailyForecastListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }

        mLayoutInflater = LayoutInflater.from(context);
    }

    public void bindDailyForecasts(List<DailyForecast> dailyForecasts) {
        removeAllViews();

        if (dailyForecasts == null) {
            return;
        }
        for (int i = 0, count = dailyForecasts.size(); i < count; i++) {
            DailyForecastView dailyView;
            if (mDailyViewPool.size() <= i) {
                // Inflate new view and add to pool
                dailyView = (DailyForecastView)
                        mLayoutInflater.inflate(R.layout.weather_daily_forecast_item, this, false);
                mDailyViewPool.add(dailyView);
            } else {
                // Reused pooled view
                dailyView = mDailyViewPool.get(i);
            }
            dailyView.bindDailyForecast(dailyForecasts.get(i), i == 0);
            addView(dailyView);
        }
        requestLayout();
    }
}

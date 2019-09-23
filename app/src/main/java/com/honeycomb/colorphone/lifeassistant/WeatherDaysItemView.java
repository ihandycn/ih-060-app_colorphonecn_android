package com.honeycomb.colorphone.lifeassistant;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.ihs.weather.DailyForecast;
import com.superapps.util.Fonts;
import com.superapps.view.TypefacedTextView;

import colorphone.acb.com.libweather.WeatherSettings;
import colorphone.acb.com.libweather.WeatherUtils;

public class WeatherDaysItemView extends LinearLayout {
    private TypefacedTextView mWeekdayText;
    private ImageView mConditionIcon;
    private TextView mTemperatureLowHigh;

    public WeatherDaysItemView(Context context) {
        super(context);
    }

    public WeatherDaysItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WeatherDaysItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWeekdayText = findViewById(R.id.minus_one_weather_days_item_date);
        mConditionIcon = findViewById(R.id.minus_one_weather_days_item_weather_icon);
        mTemperatureLowHigh = findViewById(R.id.minus_one_weather_days_item_temperature_low_high);
    }

    public void bindDailyForecast(DailyForecast forecast, boolean today, boolean isTomorrow, boolean isNight) {
        if (forecast == null) {
            return;
        }
        Typeface boldFace = Fonts.getTypeface(Fonts.Font.ofFontResId(R.string.custom_font_semibold), 0);
        Typeface regularFace = Fonts.getTypeface(Fonts.Font.ofFontResId(R.string.custom_font_regular), 0);

        if (today) {
            mTemperatureLowHigh.setTypeface(boldFace);
            mWeekdayText.setTypeface(boldFace);
            mWeekdayText.setText(getContext().getString(R.string.weather_today));
        } else {
            mTemperatureLowHigh.setTypeface(regularFace);
            mWeekdayText.setTypeface(regularFace);

            if (isTomorrow) {
                mWeekdayText.setText(getContext().getString(R.string.weather_tomorrow));
            } else {
                mWeekdayText.setText(getWeekDayAbbreviation(forecast.getWeekday()));
            }
        }
        mConditionIcon.setImageResource(
                WeatherUtils.getWeatherConditionSmallIconResourceId(forecast.getCondition(), isNight));

        if (false && WeatherSettings.shouldDisplayFahrenheit()) { // 国内直接采用摄氏度
            mTemperatureLowHigh.setText(getContext().getString(R.string.weather_high_low_temperature,
                    WeatherUtils.filterInt(forecast.getLowFahrenheit()),
                    WeatherUtils.filterInt(forecast.getHighFahrenheit())));
        } else {
            mTemperatureLowHigh.setText(getContext().getString(R.string.weather_high_low_temperature,
                    WeatherUtils.filterInt(forecast.getLowCelsius()),
                    WeatherUtils.filterInt(forecast.getHighCelsius())));
        }
    }

    private String getWeekDayAbbreviation(String rawString) {
        if (TextUtils.isEmpty(rawString)) {
            return "";
        }
        Resources resources = getContext().getResources();
        switch (rawString) {
            case "Sunday":
                return resources.getString(R.string.sunday);
            case "Monday":
                return resources.getString(R.string.monday);
            case "Tuesday":
                return resources.getString(R.string.tuesday);
            case "Wednesday":
                return resources.getString(R.string.wednesday);
            case "Thursday":
                return resources.getString(R.string.thursday);
            case "Friday":
                return resources.getString(R.string.friday);
            case "Saturday":
                return resources.getString(R.string.saturday);
            default:
                return "";
        }
    }
}

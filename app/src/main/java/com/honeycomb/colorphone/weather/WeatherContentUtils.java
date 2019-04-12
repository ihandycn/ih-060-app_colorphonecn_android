package com.honeycomb.colorphone.weather;

import android.app.Activity;
import android.graphics.Rect;
import android.view.Window;

import com.honeycomb.colorphone.Constants;
import com.superapps.util.Preferences;

/**
 * @author sundxing
 */
public class WeatherContentUtils {

    public static final String KEY_WEATHER_DISPLAY_WINDOW_HEIGHT = "weather_display_window_height";

    public static void saveWeatherTransHeight(Activity activity, int toolBarHeight) {
        int weatherHeight = Preferences.get(Constants.DESKTOP_PREFS).getInt(KEY_WEATHER_DISPLAY_WINDOW_HEIGHT, 0);
        if (weatherHeight == 0) {
            Rect rect = new Rect();
            activity.findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(rect);
            weatherHeight = rect.height() - toolBarHeight;
            Preferences.get(Constants.DESKTOP_PREFS).putInt(KEY_WEATHER_DISPLAY_WINDOW_HEIGHT, weatherHeight);
        }
    }

    public static int getWeatherWindowHeight() {
        return Preferences.get(Constants.DESKTOP_PREFS).getInt(KEY_WEATHER_DISPLAY_WINDOW_HEIGHT, 0);
    }
}

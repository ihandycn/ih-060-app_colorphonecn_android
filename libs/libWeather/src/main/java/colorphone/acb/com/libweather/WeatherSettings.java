package colorphone.acb.com.libweather;

import android.text.TextUtils;

import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;

import java.util.List;
import java.util.Locale;

import colorphone.acb.com.libweather.model.LauncherFiles;

/**
 * User settings for weather module.
 */
public class WeatherSettings {

    public static final String PREF_KEY_DISPLAY_FAHRENHEIT = "display.fahrenheit";
    public static final String PREF_KEY_DISPLAY_ENGLISH_METRIC = "display.english.metric";

    public static void setDisplayFahrenheit(boolean fahrenheit) {
        Preferences.get(LauncherFiles.WEATHER_PREFS).putBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, fahrenheit);
    }

    public static boolean shouldDisplayFahrenheit() {
        Preferences prefs = Preferences.get(LauncherFiles.WEATHER_PREFS);
        if (!prefs.contains(PREF_KEY_DISPLAY_FAHRENHEIT)) {
            String currentCountry = Locale.getDefault().getCountry().toUpperCase();
            @SuppressWarnings("unchecked") List<String> fahrenheitCountries =
                    (List<String>) HSConfig.getList("Application", "Units", "FahrenheitDisplayCountries");
            boolean fahrenheit = false;
            for (String fahrenheitCountry : fahrenheitCountries) {
                if (TextUtils.equals(currentCountry, fahrenheitCountry)) {
                    fahrenheit = true;
                    prefs.putBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, true);
                    break;
                }
            }
            if (!fahrenheit) {
                prefs.putBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, false);
            }
        }
        return prefs.getBoolean(PREF_KEY_DISPLAY_FAHRENHEIT, false);
    }

    public static void setDisplayEnglishMetric(boolean englishMetric) {
        Preferences.get(LauncherFiles.WEATHER_PREFS).putBoolean(PREF_KEY_DISPLAY_ENGLISH_METRIC, englishMetric);
    }

    public static boolean shouldDisplayEnglishMetric() {
        Preferences prefs = Preferences.get(LauncherFiles.WEATHER_PREFS);
        if (!prefs.contains(PREF_KEY_DISPLAY_ENGLISH_METRIC)) {
            String currentCountry = Locale.getDefault().getCountry().toUpperCase();
            @SuppressWarnings("unchecked") List<String> englishCountries =
                    (List<String>) HSConfig.getList("Application", "Units", "EnglishMetricDisplayCountries");
            boolean english = false;
            for (String fahrenheitCountry : englishCountries) {
                if (TextUtils.equals(currentCountry, fahrenheitCountry)) {
                    english = true;
                    prefs.putBoolean(PREF_KEY_DISPLAY_ENGLISH_METRIC, true);
                    break;
                }
            }
            if (!english) {
                prefs.putBoolean(PREF_KEY_DISPLAY_ENGLISH_METRIC, false);
            }
        }
        return prefs.getBoolean(PREF_KEY_DISPLAY_ENGLISH_METRIC, false);
    }
}

package colorphone.acb.com.libweather;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;

import com.ihs.app.framework.HSApplication;
import com.ihs.weather.CurrentCondition;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.Bitmaps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import colorphone.acb.com.libweather.background.BaseWeatherAnimBackground;
import colorphone.acb.com.libweather.background.BottomShaderBackground;
import colorphone.acb.com.libweather.background.CloudyBackground;
import colorphone.acb.com.libweather.background.DustBackground;
import colorphone.acb.com.libweather.background.HazeBackground;
import colorphone.acb.com.libweather.background.HotBackground;
import colorphone.acb.com.libweather.background.MeteorBackground;
import colorphone.acb.com.libweather.background.OvercastBackground;
import colorphone.acb.com.libweather.background.RainyBackground;
import colorphone.acb.com.libweather.background.SnowBackground;
import colorphone.acb.com.libweather.background.StarsBackground;
import colorphone.acb.com.libweather.background.SunnyBackground;
import colorphone.acb.com.libweather.background.WindBackground;

public class WeatherUtils {

    private static SparseArray<Bitmap> sAnimBitmapCache = new SparseArray<>();

    public static String filterInt(int figure) {
        return filterInt(figure, false);
    }

    static String filterInt(int figure, boolean leadingZero) {
        if (figure == HSWeatherQueryResult.UNKNOWN_VALUE) {
            return HSApplication.getContext().getString(R.string.weather_no_info_placeholder);
        }
        return String.format(Locale.getDefault(), leadingZero ? "%02d" : "%d", figure);
    }

    public static String filterDouble(double figure, int digit) {
        if (Double.compare(figure, HSWeatherQueryResult.UNKNOWN_VALUE) == 0) {
            return HSApplication.getContext().getString(R.string.weather_no_info_placeholder);
        }
        return String.format(Locale.getDefault(), "%." + digit + "f", figure);
    }

    public static int getWeatherConditionIconResourceId(HSWeatherQueryResult result) {
        if (result == null || result.getCurrentCondition() == null
                || result.getCurrentCondition().getCondition() == null) {
            return R.drawable.weather_unknown;
        }

        CurrentCondition currentCondition = result.getCurrentCondition();
        TimeZone timeZone = getTimeZone(result);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(timeZone);
        Date date = new Date();
        String[] formatTime = formatter.format(date).split(":");
        int hour = Integer.parseInt(formatTime[0]);
        int minute = Integer.parseInt(formatTime[1]);
        //noinspection UnusedAssignment
        boolean isNight = isNight(new CityData.AstronomyInfo(currentCondition), hour, minute);
        switch (result.getCurrentCondition().getCondition()) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case FAIR:
            case CLEAR:
                return isNight ? R.drawable.weather_clear : R.drawable.weather_sunny;
            case OVERCAST:
                return R.drawable.weather_overcast;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                return isNight ? R.drawable.weather_cloudy_night : R.drawable.weather_cloudy;
            case RAIN:
            case CHANCE_OF_RAIN:
                return R.drawable.weather_rain;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                return R.drawable.weather_drizzle;
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                return R.drawable.weather_rainshower;
            case SNOW:
            case CHANCE_OF_SNOW:
                return R.drawable.weather_snow;
            case SNOW_SHOWER:
                return R.drawable.weather_snowshower;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                return R.drawable.weather_sleet;
            case HAZY:
            case SMOKE:
            case FOG:
            case MIST:
                return R.drawable.weather_hazy;
            case DUST:
                return R.drawable.weather_dust;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                return R.drawable.weather_thunderstorm;
            case COLD:
            case ICY:
            case FROZEN_MIX:
            case CHANCE_OF_FROZEN_MIX:
                return R.drawable.weather_cold;
            case WINDY:
                return R.drawable.weather_windy;
            case HOT:
                return R.drawable.weather_hot;
            default:
                return R.drawable.weather_unknown;
        }
    }

    public static int getWeatherConditionSmallIconResourceId(HSWeatherQueryResult.Condition condition, boolean isNight) {
        if (condition == null) {
            return R.drawable.weather_unknown_s;
        }
        switch (condition) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case FAIR:
            case CLEAR:
                return isNight ? R.drawable.weather_clear_s : R.drawable.weather_sunny_s;
            case OVERCAST:
                return R.drawable.weather_overcast_s;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                return isNight ? R.drawable.weather_cloudy_night_s : R.drawable.weather_cloudy_s;
            case RAIN:
            case CHANCE_OF_RAIN:
                return R.drawable.weather_rain_s;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                return R.drawable.weather_drizzle_s;
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                return R.drawable.weather_rainshower_s;
            case SNOW:
            case CHANCE_OF_SNOW:
                return R.drawable.weather_snow_s;
            case SNOW_SHOWER:
                return R.drawable.weather_snowshower_s;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                return R.drawable.weather_sleet_s;
            case HAZY:
            case SMOKE:
            case FOG:
            case MIST:
                return R.drawable.weather_hazy_s;
            case DUST:
                return R.drawable.weather_dust_s;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                return R.drawable.weather_thunderstorm_s;
            case COLD:
            case ICY:
            case FROZEN_MIX:
            case CHANCE_OF_FROZEN_MIX:
                return R.drawable.weather_cold_s;
            case WINDY:
                return R.drawable.weather_windy_s;
            case HOT:
                return R.drawable.weather_hot_s;
            default:
                return R.drawable.weather_unknown_s;
        }
    }

    public static Bitmap getWeatherBackgroundAnimBitmap(int resId) {
        Bitmap bitmap = sAnimBitmapCache.get(resId);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = Bitmaps.decodeResourceWithFallback(HSApplication.getContext().getResources(), resId);
            sAnimBitmapCache.put(resId, bitmap);
        }
        return bitmap;
    }

    public static void clearAnimBitmapCache() {
        sAnimBitmapCache.clear();
    }

    /**
     * @return A list containing background animation strategy objects for weather in {@code data}. Empty list if
     * no valid weather data is provided.
     */
    static @NonNull
    public List<BaseWeatherAnimBackground> getWeatherBackgroundAnims(CityData data,
                                                              WeatherAnimView view) {
        HSWeatherQueryResult weatherData = data.getWeatherData();
        if (weatherData == null) {
            return new ArrayList<>();
        }
        CurrentCondition currentCondition = weatherData.getCurrentCondition();
        TimeZone timeZone = getTimeZone(weatherData);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
        formater.setTimeZone(timeZone);
        Date date = new Date(data.getLastQueryTime());
        String[] formatTime = formater.format(date).split(":");
        int hour = Integer.parseInt(formatTime[0]);
        int minute = Integer.parseInt(formatTime[1]);
        //noinspection UnusedAssignment
        boolean isNight = isNight(new CityData.AstronomyInfo(currentCondition), hour, minute);
        List<BaseWeatherAnimBackground> anims = new ArrayList<>();

        if (currentCondition == null || currentCondition.getCondition() == null) {
            return new ArrayList<>();
        }
        switch (currentCondition.getCondition()) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case FAIR:
            case CLEAR:
                anims.add(new BottomShaderBackground(view));
                if (isNight) {
                    anims.add(new StarsBackground(view));
                    anims.add(new MeteorBackground(view));
                } else {
                    anims.add(new SunnyBackground(view));
                }
                return anims;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                anims.add(new BottomShaderBackground(view));
                if (isNight) {
                    anims.add(new StarsBackground(view));
                    anims.add(new CloudyBackground(view));
                } else {
                    anims.add(new CloudyBackground(view));
                }
                return anims;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                anims.add(new RainyBackground(view, RainyBackground.Intensity.DRIZZLE));
                return anims;
            case RAIN:
            case CHANCE_OF_RAIN:
                anims.add(new RainyBackground(view, RainyBackground.Intensity.NORMAL));
                return anims;
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                anims.add(new RainyBackground(view, RainyBackground.Intensity.SHOWER));
                return anims;
            case DUST:
                anims.add(new DustBackground(view));
                return anims;
            case HAZY:
                anims.add(new HazeBackground(view));
                return anims;
            case HOT:
                anims.add(new HotBackground(view));
                return anims;
            case OVERCAST:
                anims.add(new OvercastBackground(view));
                return anims;
            case WINDY:
                anims.add(new BottomShaderBackground(view));
                anims.add(new WindBackground(view));
                return anims;
            case SNOW:
            case SNOW_SHOWER:
            case CHANCE_OF_SNOW:
                anims.add(new BottomShaderBackground(view));
                anims.add(new SnowBackground(view));
                return anims;
        }
        return new ArrayList<>();
    }

    public static int getWeatherBackgroundResourceId(CityData data) {
        if (data == null) {
            return R.drawable.weather_sunny_day_bg;
        }
        HSWeatherQueryResult result = data.getWeatherData();
        if (result == null) {
            return R.drawable.weather_sunny_day_bg;
        }
        CurrentCondition current = result.getCurrentCondition();
        if (current == null) {
            return R.drawable.weather_sunny_day_bg;
        }
        HSWeatherQueryResult.Condition condition = current.getCondition();
        if (condition == null) {
            return R.drawable.weather_sunny_day_bg;
        }
        TimeZone timeZone = getTimeZone(data.getWeatherData());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        formatter.setTimeZone(timeZone);
        Date date = new Date(data.getLastQueryTime());
        String[] formatTime = formatter.format(date).split(":");
        int hour = Integer.parseInt(formatTime[0]);
        int minute = Integer.parseInt(formatTime[1]);
        boolean isNight = isNight(new CityData.AstronomyInfo(current), hour, minute);
        switch (condition) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case FAIR:
            case CLEAR:
            case WARM:
                return isNight ? R.drawable.weather_sunny_night_bg : R.drawable.weather_sunny_day_bg;
            case OVERCAST:
                return R.drawable.weather_overcast_bg;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                return isNight ? R.drawable.weather_cloudy_night_bg : R.drawable.weather_cloudy_day_bg;
            case RAIN:
            case CHANCE_OF_RAIN:
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                return R.drawable.weather_rainy_bg;
            case SNOW:
            case CHANCE_OF_SNOW:
            case SNOW_SHOWER:
                return R.drawable.weather_snow_bg;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                return R.drawable.weather_sleet_bg;
            case HAZY:
            case SMOKE:
            case FOG:
            case MIST:
                return R.drawable.weather_haze_bg;
            case DUST:
                return R.drawable.weather_dust_bg;
            case COLD:
            case ICY:
            case FROZEN_MIX:
            case CHANCE_OF_FROZEN_MIX:
                return R.drawable.weather_cold_bg;
            case WINDY:
                return R.drawable.weather_wind_bg;
            case HOT:
                return R.drawable.weather_hot_bg;
            default:
                return isNight ? R.drawable.weather_sunny_night_bg : R.drawable.weather_sunny_day_bg;
        }
    }

    public static int getWeatherConditionNotificationResId(HSWeatherQueryResult.Condition condition) {
        if (condition == null) {
            return R.drawable.weather_notification_sunny_img;
        }
        switch (condition) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case FAIR:
            case CLEAR:
                return R.drawable.weather_notification_sunny_img;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                return R.drawable.weather_notification_cloudy_img;
            case RAIN:
            case CHANCE_OF_RAIN:
                return R.drawable.weather_notification_rain_img;
            case OVERCAST:
                return R.drawable.weather_notification_overcast_img;
            case RAIN_SHOWER:
            case STORM:
            case CHANCE_OF_STORM:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                return R.drawable.weather_notification_rain_img;
            case SNOW:
            case CHANCE_OF_SNOW:
                return R.drawable.weather_notification_snow_img;
            case SNOW_SHOWER:
                return R.drawable.weather_notification_snow_img;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                return R.drawable.weather_notification_thunderstorm_img;
            default:
                return R.drawable.weather_notification_sunny_img;
        }
    }


    public static String getWeekDayString(String rawString) {
        if (TextUtils.isEmpty(rawString)) {
            return "";
        }
        Resources res = HSApplication.getContext().getResources();
        switch (rawString) {
            case "Sunday":
                return res.getString(R.string.sunday);
            case "Monday":
                return res.getString(R.string.monday);
            case "Tuesday":
                return res.getString(R.string.tuesday);
            case "Wednesday":
                return res.getString(R.string.wednesday);
            case "Thursday":
                return res.getString(R.string.thursday);
            case "Friday":
                return res.getString(R.string.friday);
            case "Saturday":
                return res.getString(R.string.saturday);
            default:
                return "";
        }
    }

    public static TimeZone getTimeZone(HSWeatherQueryResult weather) {
        if (weather != null) {
            String id = weather.getTimeZone();
            if (id != null) {
                return TimeZone.getTimeZone(id);
            }
        }
        return TimeZone.getDefault();
    }

    public static boolean isNight(CityData.AstronomyInfo astronomyInfo) {
        Calendar calendar = Calendar.getInstance();

        // Time of current timezone
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return isNight(astronomyInfo, hour, minute);
    }

    public static boolean isNight(CityData.AstronomyInfo astronomyInfo, int hour, int minute) {
        int sunriseHour = astronomyInfo.sunriseHour;
        int sunriseMinute = astronomyInfo.sunriseMinute;
        int sunsetHour = astronomyInfo.sunsetHour;
        int sunsetMinute = astronomyInfo.sunsetMinute;

        if (sunriseHour < 0 || sunriseMinute < 0 || sunsetHour < 0 || sunsetMinute < 0) {
            // No valid astronomy info, use default
            return hour < 6 || hour >= 19;
        }
        // Depending on location and season, nautical dusk or dimmer twilight would be considered night
        return compare(hour, minute, sunriseHour - 1, sunriseMinute) < 0 || compare(sunsetHour + 1, sunsetMinute, hour, minute) < 0;
    }

    public static boolean isToday(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        Date queriedTime = calendar.getTime();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date startOfDay = calendar.getTime();
        return queriedTime.compareTo(startOfDay) > 0;
    }

    private static int compare(int hourA, int minuteA, int hourB, int minuteB) {
        if (hourA != hourB) {
            return hourA - hourB;
        }
        if (minuteA != minuteB) {
            return minuteA - minuteB;
        }
        return 0;
    }

    /**
     * We not register ACCESS_FINE_LOCATION  or ACCESS_COARSE_LOCATION
     * so force ip location.
     * Related crash : https://www.fabric.io/flashlightandroid/android/apps/com.app.phone.call.flash.screen/issues/5cbf22f5f8b88c2963ffd00b?time=last-seven-days
     * @return
     */
    public static boolean onlyIpLocation() {
        return true;
    }
}

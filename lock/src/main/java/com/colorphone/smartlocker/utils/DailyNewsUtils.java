package com.colorphone.smartlocker.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.smartlocker.NewsDetailActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DailyNewsUtils {
    @IntDef({NEWS_SOURCE_BAIDU, NEWS_SOURCE_TOUTIAO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NewsSource {
    }

    public static final String BUNDLE_EXTRA_KEY_CATEGORY_PARAM = "BUNDLE_EXTRA_KEY_CATEGORY_PARAM";
    public static final String BUNDLE_EXTRA_KEY_CITY = "BUNDLE_EXTRA_KEY_CITY";

    private static final String PREF_FILE_DAILY_NEWS = "optimizer_daily_news_utils";
    private static final String PREF_KEY_DAILY_NEWS_PREFIX = "PREF_KEY_DAILY_NEWS_PREFIX";
    private static final String PREF_KEY_LOCAL_CATEGORY_ENABLE = "PREF_KEY_LOCAL_CATEGORY_ENABLE";
    private static final String PREF_KEY_LOCAL_CATEGORY_CITY = "PREF_KEY_LOCAL_CATEGORY_CITY";
    private static final String PREF_KEY_LOCAL_CATEGORY_ENABLE_CHECK_TIME = "PREF_KEY_LOCAL_CATEGORY_ENABLE_CHECK_TIME";

    private static final String PREF_FILE_DAILY_NEWS_EXTERNAL_CONTENT = "optimizer_daily_news_external_content";
    private static final String PREF_KEY_LATEST_SAVE_NEWS_TIME = "PREF_KEY_LATEST_SAVE_NEWS_TIME";

    public static final int NEWS_SOURCE_BAIDU = 0;
    public static final int NEWS_SOURCE_TOUTIAO = 1;

    public static void jumpToNewsDetail(Context context, String articleUr) {
        Intent intent = new Intent(context, NewsDetailActivity.class)
                .putExtra(NewsDetailActivity.INTENT_EXTRA_URL, articleUr);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDailyNewsEnable() {
        if (getNewsSource() == NEWS_SOURCE_TOUTIAO) {
            return Build.VERSION.SDK_INT <= 28
                    && ActivityCompat.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @NewsSource
    public static int getNewsSource() {
        return NEWS_SOURCE_BAIDU;
    }

    public static void removeViewFromParent(View view) {
        if (view == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
    }

    public static int computeScrollVerticalDuration(int dy, int height) {
        final int duration;
        float absDelta = (float) Math.abs(dy);
        duration = (int) (((absDelta / height) + 1) * 200);
        return dy == 0 ? 0 : Math.min(duration, 500);
    }

    public static long getLatestSaveNewsTime() {
        return HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS_EXTERNAL_CONTENT)
                .getLongInterProcess(PREF_KEY_LATEST_SAVE_NEWS_TIME, 0L);
    }

    public static boolean isSaveNewsExpired(long validPeriodInMillis) {
        return System.currentTimeMillis() - getLatestSaveNewsTime() > validPeriodInMillis;
    }

    public static void saveNews(String category, String newsJsonObject) {
        HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
                .putString(PREF_KEY_DAILY_NEWS_PREFIX + category, newsJsonObject);
        HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS_EXTERNAL_CONTENT)
                .putLongInterProcess(PREF_KEY_LATEST_SAVE_NEWS_TIME, System.currentTimeMillis());
    }

    @Nullable
    public static JSONObject getLastNews(String category) {
        String newsJsonObject = HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
                .getString(PREF_KEY_DAILY_NEWS_PREFIX + category, null);

        if (TextUtils.isEmpty(newsJsonObject)) {
            return null;
        }

        try {
            return new JSONObject(newsJsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static void checkLocalCategoryEnable(Context context) {
//
//        if (!isDailyNewsEnable()) {
//            return;
//        }
//
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        long checkTime = HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                .getLong(PREF_KEY_LOCAL_CATEGORY_ENABLE_CHECK_TIME, 0);
//
//        if (DateUtils.isSameDay(checkTime)) {
//            return;
//        }
//
//        HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                .putLong(PREF_KEY_LOCAL_CATEGORY_ENABLE_CHECK_TIME, System.currentTimeMillis());
//
//        try {
//            final LocationManager locationManager = (LocationManager) HSApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
//
//            if (locationManager == null) {
//                return;
//            }
//
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//                    double latitude = location.getLatitude();
//                    double longitude = location.getLongitude();
//                    String getCityNameUrl = "http://api.map.baidu.com/geocoder?output=json&location=" + latitude + "," + longitude;
//                    HSHttpConnection connection = new HSHttpConnection(getCityNameUrl, HttpRequest.Method.GET);
//                    connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
//                        @Override
//                        public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
//                            try {
//                                JSONObject response = hsHttpConnection.getBodyJSON();
//
//                                if (response == null || response.getJSONObject("result") == null) {
//                                    return;
//                                }
//
//                                JSONObject jsonObject = response.getJSONObject("result").getJSONObject("addressComponent");
//                                String cityName = jsonObject.getString("city");
//                                cityName = cityName.replace("市", "");
//
//                                final String finalCityName = cityName;
//
//                                if (ActivityCompat.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                                    return;
//                                }
//
//                                TouTiaoFeedUtils.loadNews(TouTiaoFeedUtils.CATEGORY_NEWS_LOCAL, cityName, false, TelephonyUtils.getDeviceId(HSApplication.getContext()),
//                                        new TouTiaoFeedUtils.DataBackListener() {
//                                            @Override
//                                            public void onDataBack(JSONObject response, boolean hasMore) {
//                                                if (!hasMore || response == null) {
//                                                    HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                                                            .putBoolean(PREF_KEY_LOCAL_CATEGORY_ENABLE, false);
//                                                } else {
//                                                    HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                                                            .putBoolean(PREF_KEY_LOCAL_CATEGORY_ENABLE, true);
//
//                                                    HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                                                            .putString(PREF_KEY_LOCAL_CATEGORY_CITY, finalCityName);
//                                                }
//                                            }
//                                        });
//
//                            } catch (JSONException e) {
//                                HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                                        .putBoolean(PREF_KEY_LOCAL_CATEGORY_ENABLE, false);
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
//                        }
//                    });
//                    connection.startAsync();
//                    locationManager.removeUpdates(this);
//                }
//
//                @Override
//                public void onStatusChanged(String s, int i, Bundle bundle) {
//                }
//
//                @Override
//                public void onProviderEnabled(String s) {
//                }
//
//                @Override
//                public void onProviderDisabled(String s) {
//                }
//            });
//        } catch (Exception e) {
//            HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
//                    .putBoolean(PREF_KEY_LOCAL_CATEGORY_ENABLE, false);
//        }
//    }

    public static boolean isLocalCategoryEnable() {
        if (getNewsSource() == NEWS_SOURCE_TOUTIAO) {
            return HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
                    .getBoolean(PREF_KEY_LOCAL_CATEGORY_ENABLE, false);
        } else {
            return true;
        }
    }

    public static String getCityName() {
        return HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_DAILY_NEWS)
                .getString(PREF_KEY_LOCAL_CATEGORY_CITY, "");
    }

}

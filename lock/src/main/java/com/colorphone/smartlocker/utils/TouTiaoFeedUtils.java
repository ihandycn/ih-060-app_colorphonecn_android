package com.colorphone.smartlocker.utils;

import android.os.Build;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TouTiaoFeedUtils {

    public interface DataBackListener {
        void onDataBack(JSONObject response, boolean hasMore);
    }

    private static final String TAG = "TouTiaoFeedUtils";

    public static final String PREF_FILE_NAME = "optimizer_toutiaofeed";
    private static final String PREF_KEY_NAME_ACCESS_TOKEN = "PREF_KEY_NAME_ACCESS_TOKEN";
    public static final String PREF_KEY_NAME_ANIM_COUNT = "PREF_KEY_NAME_ANIM_COUNT";

    private static final String nonce = String.valueOf(new Random().nextInt(1000000));
    private static final String secureKey = HSConfig.getString("Application", "ToutiaoFeed", "Secure_key");
    private static final long timestamp = System.currentTimeMillis() / 1000;
    private static final String OPENUDID = Settings.Secure.getString(HSApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

    public static final int COVER_MODE_NO_IMAGE = 0;
    public static final int COVER_MODE_BIG_IMAGE = 1;
    public static final int COVER_MODE_THREE_IMAGE = 2;
    public static final int COVER_MODE_RIGHT_IMAGE = 3;

    @IntDef({
            COVER_MODE_NO_IMAGE,
            COVER_MODE_BIG_IMAGE,
            COVER_MODE_THREE_IMAGE,
            COVER_MODE_RIGHT_IMAGE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface NewsType {
    }

    @StringDef({
            CATEGORY_ALL,
            CATEGORY_NEWS_HOT,
            CATEGORY_NEWS_LOCAL,
            CATEGORY_NEWS_ENTERTAINMENT,
            CATEGORY_NEWS_TECH,
            CATEGORY_NEWS_CAR,
            CATEGORY_NEWS_FINANCE,
            CATEGORY_NEWS_MILITARY,
            CATEGORY_NEWS_SPORTS,
            CATEGORY_NEWS_WORLD,
            CATEGORY_NEWS_HEALTH,
            CATEGORY_NEWS_HOUSE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface TouTiaoCategory {
    }

    public static final String CATEGORY_ALL = "__all__"; // 推荐
    public static final String CATEGORY_NEWS_HOT = "news_hot"; // 热点
    public static final String CATEGORY_NEWS_LOCAL = "news_local"; // 本地
    public static final String CATEGORY_NEWS_SOCIETY = "news_society"; // 社会
    public static final String CATEGORY_NEWS_ENTERTAINMENT = "news_entertainment"; // 娱乐
    public static final String CATEGORY_NEWS_TECH = "news_tech"; // 科技
    public static final String CATEGORY_NEWS_CAR = "news_car"; // 懂车帝
    public static final String CATEGORY_NEWS_FINANCE = "news_finance"; // 财经
    public static final String CATEGORY_NEWS_MILITARY = "news_military"; // 军事
    public static final String CATEGORY_NEWS_SPORTS = "news_sports"; // 体育
    public static final String CATEGORY_NEWS_PET = "news_pet"; // 宠物
    public static final String CATEGORY_NEWS_CULTURE = "news_culture"; // 人文
    public static final String CATEGORY_NEWS_WORLD = "news_world"; // 国际
    public static final String CATEGORY_NEWS_FASHION = "news_fashion"; // 时尚
    public static final String CATEGORY_NEWS_GAME = "news_game"; // 游戏
    public static final String CATEGORY_NEWS_TRAVEL = "news_travel"; // 旅游
    public static final String CATEGORY_NEWS_HISTORY = "news_history"; // 历史
    public static final String CATEGORY_NEWS_DISCOVERY = "news_discovery"; // 探索
    public static final String CATEGORY_NEWS_FOOD = "news_food"; // 美食
    public static final String CATEGORY_NEWS_REGIMEN = "news_regimen"; // 养生
    public static final String CATEGORY_NEWS_HEALTH = "news_health"; // 健康
    public static final String CATEGORY_NEWS_BABY = "news_baby"; // 育儿
    public static final String CATEGORY_NEWS_STORY = "news_story"; // 故事
    public static final String CATEGORY_NEWS_ESSAY = "news_essay"; // 美文
    public static final String CATEGORY_NEWS_EDU = "news_edu"; // 教育
    public static final String CATEGORY_NEWS_HOUSE = "news_house"; // 房产
    public static final String CATEGORY_NEWS_CAREER = "news_career"; // 职场
    public static final String CATEGORY_NEWS_PHOTOGRAPHY = "news_photography"; // 摄影
    public static final String CATEGORY_NEWS_COMIC = "news_comic"; // 动漫
    public static final String CATEGORY_NEWS_ASTROLOGY = "news_astrology"; // 星座
    public static final String CATEGORY_WEITOUTIAO = "weitoutiao"; // 微头条
    public static final String CATEGORY_VIDEO = "video"; // 视频 本频道接入需要与bd确认，否则不能使用

    private static StringBuilder getCommonParamsUrl(String url, String deviceId) {

        StringBuilder comParamsUrl = new StringBuilder(url);

        comParamsUrl.append("&timestamp=").append(timestamp);

        comParamsUrl.append("&nonce=").append(nonce);

        comParamsUrl.append("&partner=").append(HSConfig.getString("Application", "ToutiaoFeed", "Partener"));

        comParamsUrl.append("&os_version=").append(Build.VERSION.RELEASE);

        comParamsUrl.append("&os=Android");

        comParamsUrl.append("&udid=").append(deviceId);

        return comParamsUrl;
    }

    public static void loadNews(@TouTiaoCategory final String category, String deviceId, final DataBackListener dataBackListener) {
        loadNews(category, "", false, deviceId, dataBackListener);
    }

    public static void loadNews(@TouTiaoCategory final String category, String city, boolean isStick, String deviceId, final DataBackListener dataBackListener) {
        if (!HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_NAME).contains(PREF_KEY_NAME_ACCESS_TOKEN)) {
            getAccessToken(category, city, isStick, deviceId, dataBackListener);
        } else {
            toLoadNews(category, city, isStick, deviceId, dataBackListener);
        }
    }

    private static void toLoadNews(final String category, @Nullable final String city, final boolean isStick, final String deviceId, final DataBackListener dataBackListener) {

        final StringBuilder newsUrl = getCommonParamsUrl("http://open.snssdk.com/data/stream/v3/?", deviceId);

        newsUrl.append("&access_token=").append(HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_NAME)
                .getString(PREF_KEY_NAME_ACCESS_TOKEN, ""));

        newsUrl.append("&category=").append(category);

        newsUrl.append("&dt=").append(Build.MODEL.replace(" ", "+")); // 设备型号

        newsUrl.append("&type=").append("1"); // 设备类型： Unknown=0；Phone/手机=1；Tablet/平板=2；TV/智能电视=3

        newsUrl.append("&device_brand=").append(Build.BRAND);

        newsUrl.append("&signature=").append(getSignature(false, deviceId));

        newsUrl.append("&https=").append("1"); // 是否输出https详情页: https=1, 则输出https详情页

        if (isStick) {
            newsUrl.append("&allow_stick=").append("1"); // 是否需要置顶文章，置顶文章每次刷新都会出
        }

        if (!TextUtils.isEmpty(city) && TextUtils.equals(CATEGORY_NEWS_LOCAL, category)) {
            newsUrl.append("&city=").append(city);
        }

        HSLog.i(TAG, "newsUrl = " + newsUrl);

        HSHttpConnection connection = new HSHttpConnection(newsUrl.toString(), HttpRequest.Method.GET);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                try {
                    JSONObject response = hsHttpConnection.getBodyJSON();
                    int ret = response.getInt("ret");

                    if (ret == 1) {
                        getAccessToken(category, city, isStick, deviceId, dataBackListener);

                    } else if (ret == 0) {
                        dataBackListener.onDataBack(response, response.getBoolean("has_more"));

                    } else {
                        dataBackListener.onDataBack(null, false);
                    }
                } catch (Exception e) {
                    dataBackListener.onDataBack(null, false);
                    e.printStackTrace();
                    HSLog.i(TAG, "onConnectionFinished Exception e = " + e.getMessage());
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                dataBackListener.onDataBack(null, false);
                HSLog.i(TAG, "onConnectionFailed hsError " + hsError.getMessage());
            }
        });
        connection.startAsync();
    }

    private static void getAccessToken(final String category, final String city, final boolean isStick, final String deviceId, final DataBackListener dataBackListener) {

        final StringBuilder registerUrl = getCommonParamsUrl("http://open.snssdk.com/access_token/register/device/v4/?", deviceId);

        registerUrl.append("&signature=").append(getSignature(true, deviceId));

        registerUrl.append("&device_model=").append(Build.MODEL.replace(" ", "+"));

        registerUrl.append("&openudid=").append(OPENUDID);

        HSLog.i(TAG, "registerUrl = " + registerUrl);

        HSHttpConnection connection = new HSHttpConnection(registerUrl.toString(), HttpRequest.Method.GET);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                try {
                    JSONObject response = hsHttpConnection.getBodyJSON();

                    int ret = response.getInt("ret");

                    if (ret == 0) {
                        JSONObject data = response.getJSONObject("data");
                        String token = data.getString("access_token");

                        HSPreferenceHelper.create(HSApplication.getContext(), PREF_FILE_NAME)
                                .putString(PREF_KEY_NAME_ACCESS_TOKEN, token);

                        toLoadNews(category, city, isStick, deviceId, dataBackListener);
                    } else {// 获取token失败
                        dataBackListener.onDataBack(null, false);
                    }

                } catch (Exception e) {
                    dataBackListener.onDataBack(null, false);
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                dataBackListener.onDataBack(null, false);
            }
        });
        connection.startAsync();
    }

    private static String getSignature(boolean isGetAccessToken, String deviceId) {

        List<String> strings = new ArrayList<>();
        if (!TextUtils.isEmpty(nonce)) {
            strings.add(nonce);
        }
        if (!TextUtils.isEmpty(secureKey)) {
            strings.add(secureKey);
        }
        if (!TextUtils.isEmpty(String.valueOf(timestamp))) {
            strings.add(String.valueOf(timestamp));
        }

        if (isGetAccessToken) {
            strings.add("Android");
            if (!TextUtils.isEmpty(deviceId)) {
                strings.add(deviceId);
            }
            if (!TextUtils.isEmpty(OPENUDID)) {
                strings.add(OPENUDID);
            }
        }

        Collections.sort(strings);
        StringBuilder key = new StringBuilder();
        for (String string : strings) {
            key.append(string);
        }

        String signatureKey = null;
        try {
            signatureKey = sha1(key.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signatureKey;
    }

    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public static String getTime(long timestamp) {
        String time;

        long distance = (System.currentTimeMillis() / 1000 - timestamp) / 60;
        if (distance < 60) {
            time = HSApplication.getContext().getResources().getString(R.string.before_minutes, distance);
        } else if (distance < 60 * 24) {
            time = HSApplication.getContext().getResources().getString(R.string.before_hour, distance / 60);
        } else {
            time = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp * 1000).substring(5, 10);
        }
        return time;
    }

    public static String getToutiaoCategoryParam(int position) {
        switch (position) {
            case 0:
                return CATEGORY_ALL;
            case 1:
                return CATEGORY_NEWS_HOT;
            case 2:
                return CATEGORY_NEWS_LOCAL;
            case 3:
                return CATEGORY_NEWS_ENTERTAINMENT;
            case 4:
                return CATEGORY_NEWS_TECH;
            case 5:
                return CATEGORY_NEWS_CAR;
            case 6:
                return CATEGORY_NEWS_FINANCE;
            case 7:
                return CATEGORY_NEWS_MILITARY;
            case 8:
                return CATEGORY_NEWS_SPORTS;
            case 9:
                return CATEGORY_NEWS_WORLD;
            case 10:
                return CATEGORY_NEWS_HEALTH;
            case 11:
                return CATEGORY_NEWS_HOUSE;
            default:
                break;
        }

        if (BuildConfig.DEBUG) {
            throw new RuntimeException("参数错误 position = " + position);
        }

        return TouTiaoFeedUtils.CATEGORY_ALL;
    }
}

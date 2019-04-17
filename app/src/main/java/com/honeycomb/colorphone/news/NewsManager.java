package com.honeycomb.colorphone.news;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NewsManager {
    private static class NewsManagerHolder {
        private static final NewsManager instance = new NewsManager();
    }

    private NewsManager() {
    }

    public static NewsManager getInstance() {
        return NewsManager.NewsManagerHolder.instance;
    }

    static String TAG = NewsManager.class.getSimpleName();
    private static UUID userID = java.util.UUID.randomUUID();
    private static int LIMIT_SIZE = 10;

    public interface NewsLoadListener {
        void onNewsLoaded(NewsResultBean bean);
    }
    private NewsLoadListener loadListener;

    private NewsResultBean resultBean;

    public void fetchNews() {
        HSLog.i(NewsManager.TAG, "fetchNews");
        HSHttpConnection news = new HSHttpConnection(getURL(String.valueOf(LIMIT_SIZE), "0"));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    resultBean = gson.fromJson(jsonBody, NewsResultBean.class);
                    HSLog.i(TAG, "result size == " + (resultBean != null ? resultBean.totalItems : null));
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(resultBean);
                    }
                } else {
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(null);
                    }
                    HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                HSLog.i(TAG, "HSError: " + hsError);
                if (loadListener != null) {
                    loadListener.onNewsLoaded(null);
                }
            }
        });
        news.startAsync();
    }

    public void fetchLaterNews() {
        int offset = resultBean != null ? resultBean.totalItems : 0;
        HSLog.i(NewsManager.TAG, "fetchLaterNews offset == " + offset);

        HSHttpConnection news = new HSHttpConnection(getURL(String.valueOf(LIMIT_SIZE), String.valueOf(offset)));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    NewsResultBean bean = gson.fromJson(jsonBody, NewsResultBean.class);
                    HSLog.i(TAG, "result: size == " + (bean != null ? bean.totalItems : null));
                    if (resultBean == null) {
                        resultBean = bean;
                    } else {
                        if (resultBean.content == null) {
                            resultBean.content = new ArrayList<>();
                        }
                        
                        resultBean.content.addAll(bean.content);
                        resultBean.totalItems += bean.totalItems;
                    }
                    HSLog.i(TAG, "result: add size == " + (resultBean != null ? resultBean.totalItems : null));
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(resultBean);
                    }
                } else {
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(null);
                    }
                    HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                HSLog.i(TAG, "HSError: " + hsError);
                if (loadListener != null) {
                    loadListener.onNewsLoaded(null);
                }
            }
        });
        news.startAsync();
    }

    public void setNewsLoadListener(NewsLoadListener listener) {
        loadListener = listener;
    }

    private static String getURL(String limit, String offset) {
        final StringBuffer url = new StringBuffer(HSConfig.optString("",
                "Application", "News", "Url"));
        
        url.append("?userId=").append(userID.toString());

        List keys = HSConfig.getList("Application", "News", "PublisherKey");
        Random random = new Random();
        if (keys != null && keys.size() > 0) {
            Map map = (Map) keys.get(random.nextInt(keys.size()));
            String key = HSMapUtils.getString(map, "Key");
            String id = HSMapUtils.getString(map, "PublisherId");

            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(id)) {
                url.append("&publisherId=").append(id);
                url.append("&key=").append(key);
            }
        }

        Locale locale = Locale.getDefault();

        url.append("&countryCode=").append(locale.getCountry());
        url.append("&language=").append(locale.getLanguage());
        url.append("&limit=").append(limit);
        url.append("&offset=").append(offset);
        String category = HSConfig.optString("", "Application", "News", "Category");
        if (!TextUtils.isEmpty(category)) {
            url.append("&category=").append(category);
        }

        HSLog.i(TAG, "getUrl: " + url.toString());


        return url.toString();
    }

}

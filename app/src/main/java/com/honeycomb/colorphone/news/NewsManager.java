package com.honeycomb.colorphone.news;

import com.google.gson.Gson;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

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
    private static String URL ="https://contentapi.celltick.com/mediaApi/v1.0/content?publisherId=%1$s&key=%2$s&userId=%3$s&countryCode=US&language=en&limit=%4$s&offset=%5$s";
    private static int LIMIT_SIZE = 10;
//      iHandy-Web
//      SHI4TMTLx4z256WRU0Hy3hEByAu3jDS0
//      iHandy02-Web
//      1RsC40Mo73o37eEnbIZIUidCjiM8KoPr
//      iHandy03-Web
//      tBoejdzeNvnUq5svJUCSFGdE5yhnaAe9

    public interface NewsLoadListener {
        void onNewsLoaded(NewsResultBean bean);
    }
    private NewsLoadListener loadListener;

    private NewsResultBean resultBean;

    public void fetchNews() {
        HSLog.i(NewsManager.TAG, "fetchNews");
        HSHttpConnection news = new HSHttpConnection(getURL("iHandy-Web", "SHI4TMTLx4z256WRU0Hy3hEByAu3jDS0", userID.toString(), String.valueOf(LIMIT_SIZE), "0"));
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

        HSHttpConnection news = new HSHttpConnection(getURL("iHandy-Web", "SHI4TMTLx4z256WRU0Hy3hEByAu3jDS0", userID.toString(), String.valueOf(LIMIT_SIZE), String.valueOf(offset)));
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

    private static String getURL(String publishID, String key, String userID, String limit, String offset) {
        return String.format(URL, publishID, key, userID, limit, offset);
    }

}

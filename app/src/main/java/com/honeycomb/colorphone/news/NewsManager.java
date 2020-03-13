package com.honeycomb.colorphone.news;

import android.app.Activity;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.lifeassistant.LifeAssistantConfig;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import net.appcloudbox.AcbAds;
import net.appcloudbox.UnreleasedAdWatcher;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;
import java.util.UUID;

public class NewsManager {

    public static final String NOTIFY_KEY_NEWS_LOADED = "NOTIFY_KEY_NEWS_LOADED";

    private static class NewsManagerHolder {
        private static final NewsManager instance = new NewsManager();
    }

    private NewsManager() {
        userID = Preferences.get(PERF_FILE).getString(PERF_KEY_USERID, "");
        if (TextUtils.isEmpty(userID)) {
            userID = UUID.randomUUID().toString();
            Preferences.get(PERF_FILE).putString(PERF_KEY_USERID, userID);
        }
        UnreleasedAdWatcher.getInstance().setMaxSize(50);
//        AcbInterstitialAdManager.getInstance().activePlacementInProcess(NEWS_WIRE);
    }

    public static NewsManager getInstance() {
        return NewsManager.NewsManagerHolder.instance;
    }

    public static String TAG = NewsManager.class.getSimpleName();
    private static String PERF_KEY_USERID = "perf_key_userid";
    private static String PERF_FILE = "news";
    private static String userID;

    private AcbNativeAd mAd;

    public interface NewsLoadListener {
        void onNewsLoaded(NewsResultBean bean, int size);

        default boolean isLoadNewsAd() {
            return true;
        }

        default int getNewsAdOffset() {
            return 1;
        }
    }

    private NewsResultBean lifeAssistantBean;
    private NewsResultBean exitNewsBean;

    public NewsResultBean getLifeAssistantBean() {
        return lifeAssistantBean;
    }

    public NewsResultBean getExitNewsBean() {
        return exitNewsBean;
    }

    void fetchNews(NewsLoadListener loadListener, boolean isVideo) {

        HSLog.i(NewsManager.TAG, "fetchNews");

        HSHttpConnection news = new HSHttpConnection(getURL(false));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    try {
                        NewsResultBean bean = gson.fromJson(jsonBody, NewsResultBean.class);
                        if (bean != null && loadListener != null) {
                            bean.parseArticles();
                            int size = bean.articlesList.size();
                            if (loadListener.isLoadNewsAd()) {
                                bean.adOffset = loadListener.getNewsAdOffset();
                            }
                            loadListener.onNewsLoaded(bean, size);
                            HSLog.i(NewsManager.TAG, "onNewsLoaded sendNotification");
                            HSGlobalNotificationCenter.sendNotification(NOTIFY_KEY_NEWS_LOADED);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (loadListener != null) {
                    loadListener.onNewsLoaded(null, 0);
                }

                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                HSLog.i(TAG, "HSError: " + hsError);
                if (loadListener != null) {
                    loadListener.onNewsLoaded(null, 0);
                }
            }
        });
        news.startAsync();
    }

    void fetchLaterNews(final NewsResultBean resultBean, NewsLoadListener loadListener, boolean isVideo) {

        HSHttpConnection news = new HSHttpConnection(getURL(false));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    try {
                        NewsResultBean bean = gson.fromJson(jsonBody, NewsResultBean.class);
                        bean.parseArticles();

                        int size = resultBean.articlesList.size();
                        int newSize = bean.articlesList.size();
                        resultBean.articlesList.addAll(bean.articlesList);
                        if (loadListener != null) {
                            if (loadListener.isLoadNewsAd()) {
                                resultBean.adOffset = loadListener.getNewsAdOffset();
                            }
                            loadListener.onNewsLoaded(resultBean, newSize);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(null, 0);
                    }
                    HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                HSLog.i(TAG, "HSError: " + hsError);
                if (loadListener != null) {
                    loadListener.onNewsLoaded(null, 0);
                }
            }
        });
        news.startAsync();
    }

    private String getURL(boolean isVideo) {
        final StringBuilder url = new StringBuilder();

//        if (isVideo) {
//            url.append(HSConfig.optString("", "Application", "News", "VideoUrl"));
//        } else {
//            url.append(HSConfig.optString("", "Application", "News", "Url"));
//        }
        url.append(HSConfig.optString("", "Application", "News", "Url"));

        url.append("?dn=").append(userID);
        url.append("&app_key=").append(HSConfig.optString("", "Application", "News", "Key"));
        url.append("&fn=").append("android");
        url.append("&imei=").append(NewsUtils.getImei(HSApplication.getContext()));
        url.append("&ve=").append(BuildConfig.VERSION_NAME);
        String nt = "99";
        if (Networks.isNetworkAvailable(1)) {
            nt = "2";
        } else if (Networks.isNetworkAvailable(0)) {
            nt = "1";
        }
        url.append("&nt=").append(nt);

        String category;
        if (isVideo) {
            category = HSConfig.optString("", "Application", "News", "VideoType");
        } else {
            category = HSConfig.optString("", "Application", "News", "Category");
        }

        if (!TextUtils.isEmpty(category)) {
            url.append("&cid=").append(category);
        } else {
            url.append("&cid=").append("100");
        }

        HSLog.i(TAG, "getUrl: " + url.toString());
        return url.toString();
    }

    public static void logNewsListShow(String from) {
        if (!TextUtils.isEmpty(from)) {
            Analytics.logEvent("News_List_Show", "Source", from);
        }
    }

    public void preloadForExitNews(Activity activity) {
        HSLog.d(TAG, "preloadForExitNews");
        exitNewsBean = null;
        if (activity != null) {
            AcbAds.getInstance().setActivity(activity);
        }
        AcbNativeAdManager.getInstance().activePlacementInProcess(getNativeAdPlacementName());
        AcbNativeAdManager.getInstance().preload(1, getNativeAdPlacementName());

        fetchNews((bean, size) -> {
            HSLog.d(TAG, "preloadForExitNews  onNewsLoaded ");
            exitNewsBean = bean;
        }, false);
    }

    public void preloadForLifeAssistant(Activity activity) {
        HSLog.d(TAG, "preloadForLifeAssistant");
        lifeAssistantBean = null;

        NewsLoadListener loadListener = new NewsLoadListener() {
            @Override public void onNewsLoaded(NewsResultBean bean, int size) {
                HSLog.d(TAG, "preloadForLifeAssistant  onNewsLoaded ");
                lifeAssistantBean = bean;
            }

            @Override public boolean isLoadNewsAd() {
                return LifeAssistantConfig.isLifeAssistantAdEnable();
            }

            @Override public int getNewsAdOffset() {
                return 0;
            }
        };
        fetchNews(loadListener, false);
    }

    public void releaseLifeNews() {
        lifeAssistantBean = null;
    }

    public void releaseExitNews() {
        exitNewsBean = null;
    }

    private static String getNativeAdPlacementName() {
        return Placements.getAdPlacement(Placements.AD_EXIT_WIRE_NEW);
    }

    public AcbNativeAd getNativeAd() {
        if (mAd == null) {
            List<AcbNativeAd> ads = AcbNativeAdManager.getInstance().fetch(getNativeAdPlacementName(), 1);
            if (ads != null && ads.size() > 0) {
                mAd = ads.get(0);
                HSLog.i("ThemeFullAd", "new native ad");
            }
        }
        return mAd;
    }

    public void releaseNativeAd() {
        if (mAd != null) {
            mAd.release();
            mAd = null;
        }

        releaseExitNews();
    }
}

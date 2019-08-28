package com.honeycomb.colorphone.news;

import android.app.Activity;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import net.appcloudbox.AcbAds;
import net.appcloudbox.UnreleasedAdWatcher;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewsManager {
    private static class NewsManagerHolder {
        private static final NewsManager instance = new NewsManager();
    }

    private NewsManager() {
        userID = Preferences.get(PERF_FILE).getString(PERF_KEY_USERID, "");
        if (TextUtils.isEmpty(userID)) {
            userID = UUID.randomUUID().toString();
            Preferences.get(PERF_FILE).putString(PERF_KEY_USERID, userID);
        }
        AcbNativeAdManager.getInstance().activePlacementInProcess(NEWS_LIST_BANNER);

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
    private static String NEWS_LIST_BANNER = Placements.AD_NEWS;
    public static int AD_INTERVAL = 4;
    public static int NATIVE_AD_SIZE = 4;
    private int adSize = 0;

    private AcbNativeAd mAd;

    public interface NewsLoadListener {
        void onNewsLoaded(NewsResultBean bean, int size);
    }
//    private NewsLoadListener loadListener;

//    private NewsResultBean resultBean;
    private NewsResultBean pushBean;
    private boolean showNativeAD;

    public NewsResultBean getPushBean() {
        return pushBean;
    }

    void fetchNews(NewsResultBean resultBean, NewsLoadListener loadListener, boolean isVideo) {
        showNativeAD = HSConfig.optBoolean(true, "Application", "News", "IsNewsTabAdEnable");
        if (showNativeAD) {
            AcbNativeAdManager.getInstance().preload(NATIVE_AD_SIZE, NEWS_LIST_BANNER);
        }

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
                            replaceADs(bean, 0);
                            loadListener.onNewsLoaded(bean, size);
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

    private void replaceADs(NewsResultBean resultBean, int size) {
        List<Integer> adIndexes = new ArrayList<>();
        if (resultBean == null || resultBean.articlesList == null) {
            HSLog.i(TAG, "replaceADs resultBean or articlesList is NULL ");
            return;
        }

        if (resultBean.articlesList.size() > 0) {
            int index = 0;
            for (NewsArticle article : resultBean.articlesList) {
                if (article.item_type == 8 || article.item_type == 30 || article.item_type == 100) {
                    adIndexes.add(0, index);
                }
                index++;
            }

            if (adIndexes.size() > 0) {
                for (int i : adIndexes) {
                    resultBean.articlesList.remove(i);
                }
            }
            HSLog.i(TAG, "replaceADs index: " + adIndexes + "  AfterSize: " + resultBean.articlesList.size());
        }

        if (!showNativeAD) {
            return;
        }

        int index = 1;
        if (size > 0) {
            index = size + AD_INTERVAL - (size - adSize - 1) % AD_INTERVAL;
        }

        size = (resultBean.articlesList != null ? resultBean.articlesList.size() : size);

        adIndexes.clear();
        for (; index < size; ) {
            adIndexes.add(index);
            index += AD_INTERVAL + 1;
            size++;

            Analytics.logEvent("News_List_Ad_Should_Show");
        }

        List<AcbNativeAd> ads = AcbNativeAdManager.getInstance().fetch(NEWS_LIST_BANNER, adIndexes.size());
        if (ads != null && ads.size() > 0) {
            NewsNativeAdBean bean;

            HSLog.i(TAG, "replaceADs addIndex: " + adIndexes + "  AdSize: " + ads.size());

            for (AcbNativeAd ad : ads) {
                bean = new NewsNativeAdBean();
                bean.acbNativeAd = ad;

                if (adIndexes.size() > 0) {
                    index = adIndexes.remove(0);
                    if (index < resultBean.articlesList.size()) {
                        resultBean.articlesList.add(index, bean);
                        Analytics.logEvent("New_List_Ad_Fetch_Success");
                    }
                } else {
                    ad.release();
                    break;
                }
            }

            HSLog.i(TAG, "replaceADs AfterSize: " + resultBean.articlesList.size());
            adSize += ads.size();
        }

        showNativeAD = HSConfig.optBoolean(true, "Application", "News", "IsNewsTabAdEnable");
        if (showNativeAD) {
            AcbNativeAdManager.getInstance().preload(NATIVE_AD_SIZE, NEWS_LIST_BANNER);
        }
    }

    void fetchLaterNews(final NewsResultBean resultBean, NewsLoadListener loadListener, boolean isVideo) {
        showNativeAD = HSConfig.optBoolean(true, "Application", "News", "IsNewsTabAdEnable");
        if (showNativeAD) {
            AcbNativeAdManager.getInstance().preload(NATIVE_AD_SIZE, NEWS_LIST_BANNER);
        }

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
                            if (resultBean != null) {
                                replaceADs(resultBean, size);
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

    public void preload(Activity activity) {
        HSLog.d(TAG, "preload");

        if (activity != null) {
            AcbAds.getInstance().setActivity(activity);
        }
        AcbNativeAdManager.getInstance().activePlacementInProcess(getNativeAdPlacementName());
        AcbNativeAdManager.getInstance().preload(1, getNativeAdPlacementName());
    }

    private static String getNativeAdPlacementName() {
        return Placements.AD_EXIT_WIRE_NEW;
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
    }
}

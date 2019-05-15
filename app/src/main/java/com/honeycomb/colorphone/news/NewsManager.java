package com.honeycomb.colorphone.news;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

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

        UnreleasedAdWatcher.getInstance().setMaxSize(20);
//        AcbInterstitialAdManager.getInstance().activePlacementInProcess(NEWS_WIRE);
    }

    public static NewsManager getInstance() {
        return NewsManager.NewsManagerHolder.instance;
    }

    public static String TAG = NewsManager.class.getSimpleName();
    private static String PERF_KEY_USERID = "perf_key_userid";
    private static String PERF_FILE = "news";
    private static String userID;
    private static String NEWS_LIST_BANNER = "News";
//    private static String NEWS_WIRE = "NewsWire";

//    private static int LIMIT_SIZE = 10;
//    private static int LIMIT_PUSH_SIZE = 5;
    public static int AD_INTERVAL = 4;
    public static int NATIVE_AD_SIZE = 4;
    private int adSize = 0;
//    private AcbInterstitialAd mInterstitialAd;

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
            AcbNativeAdManager.preload(NATIVE_AD_SIZE, NEWS_LIST_BANNER);
        }
//        AcbInterstitialAdManager.preload(1, NEWS_WIRE);

        HSLog.i(NewsManager.TAG, "fetchNews");
//        newOffset = 0;

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
                if (article.item_type == 8) {
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

        List<AcbNativeAd> ads = AcbNativeAdManager.fetch(NEWS_LIST_BANNER, adIndexes.size());
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
            AcbNativeAdManager.preload(NATIVE_AD_SIZE, NEWS_LIST_BANNER);
        }
    }

    void fetchLaterNews(final NewsResultBean resultBean, NewsLoadListener loadListener, boolean isVideo) {
        showNativeAD = HSConfig.optBoolean(true, "Application", "News", "IsNewsTabAdEnable");
        if (showNativeAD) {
            AcbNativeAdManager.preload(NATIVE_AD_SIZE, NEWS_LIST_BANNER);
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
//
//    public void fetchPushNews(NewsLoadListener loadListener) {
//        int offset = 0;
//        HSLog.i(NewsManager.TAG, "fetchPushNews offset == " + offset);
//        pushBean = null;
//
//        HSHttpConnection news = new HSHttpConnection(getURL(false));
//        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
//            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
//                if (hsHttpConnection.isSucceeded()) {
//                    String jsonBody = hsHttpConnection.getBodyString();
//                    Gson gson = new Gson();
//                    pushBean = gson.fromJson(jsonBody, NewsResultBean.class);
//                    HSLog.i(TAG, "result: size == " + (pushBean != null ? pushBean.msg : null));
//                    if (loadListener != null) {
//                        loadListener.onNewsLoaded(pushBean);
//                    }
//                } else {
//                    HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
//                    if (loadListener != null) {
//                        loadListener.onNewsLoaded(null);
//                    }
//                }
//            }
//
//            @Override
//            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
//                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
//                HSLog.i(TAG, "HSError: " + hsError);
//                if (loadListener != null) {
//                    loadListener.onNewsLoaded(null);
//                }
//            }
//        });
//        news.startAsync();
//    }

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

    public void preloadAD() {
//        AcbInterstitialAdManager.getInstance().activePlacementInProcess(NEWS_WIRE);
//        AcbInterstitialAdManager.preload(1, NEWS_WIRE);
    }

//    AcbInterstitialAd getInterstitialAd() {
//        if (mInterstitialAd == null) {
//            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(NEWS_WIRE, 1);
//            if (ads != null && ads.size() > 0) {
//                mInterstitialAd = ads.get(0);
//            }
//        }
//        return mInterstitialAd;
//    }
//
//    private void releaseInterstitialAd() {
//        if (mInterstitialAd != null) {
//            mInterstitialAd.release();
//            mInterstitialAd = null;
//        }
//    }
//
//    boolean showInterstitialAd(String from) {
//        if (!NewsTest.canShowNewsWireAD()) {
//            return false;
//        }
//
//        AcbInterstitialAd ad = getInterstitialAd();
//        if (ad != null) {
//            ad.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
//                @Override
//                public void onAdDisplayed() {
//                    if (TextUtils.equals(from, WebViewActivity.FROM_ALERT)) {
//                        NewsTest.logNewsEvent("news_detail_page_wire_show_from_alert");
//                    } else if (TextUtils.equals(from, WebViewActivity.FROM_LIST)) {
//                        NewsTest.logNewsEvent("news_detail_page_wire_show_from_list");
//                    }
//                }
//
//                @Override
//                public void onAdClicked() {
//
//                }
//
//                @Override
//                public void onAdClosed() {
//                    releaseInterstitialAd();
////                    AcbInterstitialAdManager.preload(1, NEWS_WIRE);
//                    NewsTest.recordShowNewsWireAdTime();
//                }
//
//                @Override
//                public void onAdDisplayFailed(AcbError acbError) {
//
//                }
//            });
//            ad.show();
//
//            return true;
//        }
//        return false;
//    }

    public static void logNewsListShow(String from) {
        if (!TextUtils.isEmpty(from)) {
            Analytics.logEvent("News_List_Show", "Source", from);
        }
    }
}

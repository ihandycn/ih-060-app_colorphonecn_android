package com.honeycomb.colorphone.news;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NewsManager {
    private static class NewsManagerHolder {
        private static final NewsManager instance = new NewsManager();
    }

    private NewsManager() {
        AcbNativeAdManager.getInstance().activePlacementInProcess(NEWS_LIST_BANNER);
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(NEWS_WIRE);
    }

    public static NewsManager getInstance() {
        return NewsManager.NewsManagerHolder.instance;
    }

    public static String TAG = NewsManager.class.getSimpleName();
    private static UUID userID = java.util.UUID.randomUUID();
    private static String NEWS_LIST_BANNER = "NewsNative";
    private static String NEWS_WIRE = "NewsWire";

    private static int LIMIT_SIZE = 10;
    private static int LIMIT_PUSH_SIZE = 5;
    public static int BIG_IMAGE_INTERVAL = 5;
    private int newOffset = 0;
    private AcbInterstitialAd mInterstitialAd;

    public interface NewsLoadListener {
        void onNewsLoaded(NewsResultBean bean);
    }
    private NewsLoadListener loadListener;

    private NewsResultBean resultBean;
    private NewsResultBean pushBean;

    public NewsResultBean getPushBean() {
        return pushBean;
    }

    public void fetchNews() {
        AcbNativeAdManager.preload(2, NEWS_LIST_BANNER);
        AcbInterstitialAdManager.preload(1, NEWS_WIRE);

        HSLog.i(NewsManager.TAG, "fetchNews");
        newOffset += resultBean == null ? 0 : resultBean.totalItems;

        HSHttpConnection news = new HSHttpConnection(getURL(String.valueOf(LIMIT_SIZE), String.valueOf(newOffset)));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    resultBean = gson.fromJson(jsonBody, NewsResultBean.class);
                    HSLog.i(TAG, "result size == " + (resultBean != null ? resultBean.totalItems : null));
                    if (loadListener != null) {
                        addNativeADs(resultBean);
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

    private void addNativeADs(NewsResultBean resultBean) {
        List<AcbNativeAd> ads = AcbNativeAdManager.fetch(NEWS_LIST_BANNER, 2);
        if (ads != null && ads.size() > 0) {
            NewsNativeAdBean bean;
            int index = -1;
            if (resultBean != null && resultBean.totalItems > 0) {
                int tail = resultBean.totalItems % BIG_IMAGE_INTERVAL;
                if (tail == 0) {
                    index = resultBean.totalItems - BIG_IMAGE_INTERVAL * 2;
                } else {
                    index = resultBean.totalItems - BIG_IMAGE_INTERVAL - tail;
                }
            }
            for (AcbNativeAd ad : ads) {
                bean = new NewsNativeAdBean();
                bean.acbNativeAd = ad;

                if (index != -1 && index < resultBean.totalItems) {
                    HSLog.i(NewsManager.TAG, "addNativeADs index == " + index);
                    resultBean.content.add(index, bean);
                    resultBean.totalItems++;
                    index += 5;
                }
            }
        }
    }

    public void fetchLaterNews() {
        AcbNativeAdManager.preload(2, NEWS_LIST_BANNER);

        int offset = resultBean != null ? newOffset + resultBean.totalItems : 0;
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

    public void fetchPushNews(NewsLoadListener loadListener) {
        int offset = 0;
        HSLog.i(NewsManager.TAG, "fetchPushNews offset == " + offset);
        pushBean = null;

        HSHttpConnection news = new HSHttpConnection(
                getURL(String.valueOf(LIMIT_PUSH_SIZE),
                        String.valueOf(offset),
                        NewsTest.getLastShowNewsAlertTime()));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    pushBean = gson.fromJson(jsonBody, NewsResultBean.class);
                    HSLog.i(TAG, "result: size == " + (pushBean != null ? pushBean.totalItems : null));
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(pushBean);
                    }
                } else {
                    HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                    if (loadListener != null) {
                        loadListener.onNewsLoaded(null);
                    }
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
        return getURL(limit, offset, 0);
    }

    private static String getURL(String limit, String offset, long time) {
        final StringBuffer url = new StringBuffer(HSConfig.optString("",
                "Application", "News", "Url"));
        
        url.append("?userId=").append(userID.toString());

        url.append("&publisherId=").append(HSConfig.optString("", "Application", "News", "PublisherId"));
        url.append("&key=").append(HSConfig.optString("", "Application", "News", "Key"));

        Locale locale = Locale.getDefault();
        url.append("&countryCode=").append(locale.getCountry());
        url.append("&language=").append(locale.getLanguage());

        url.append("&limit=").append(limit);
        url.append("&offset=").append(offset);
//        if (time > 0) {
//
//        }

        String category = HSConfig.optString("", "Application", "News", "Category");
        if (!TextUtils.isEmpty(category)) {
            url.append("&category=").append(category);
        }

        HSLog.i(TAG, "getUrl: " + url.toString());

        return url.toString();
    }

    public void preloadAD() {
        AcbInterstitialAdManager.preload(1, NEWS_WIRE);
    }

    public AcbInterstitialAd getInterstitialAd() {
        if (mInterstitialAd == null) {
            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(NEWS_WIRE, 1);
            if (ads != null && ads.size() > 0) {
                mInterstitialAd = ads.get(0);
            }
        }
        return mInterstitialAd;
    }

    public void releaseInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
            mInterstitialAd = null;
        }
    }

    public boolean showInterstitialAd() {
        if (!NewsTest.canShowNewsWireAD()) {
            return false;
        }

        AcbInterstitialAd ad = getInterstitialAd();
        if (ad != null) {
            ad.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                @Override
                public void onAdDisplayed() {

                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdClosed() {
                    releaseInterstitialAd();
                    AcbInterstitialAdManager.preload(1, NEWS_WIRE);
                    NewsTest.recordShowNewsWireAdTime();
                }

                @Override
                public void onAdDisplayFailed(AcbError acbError) {

                }
            });
            ad.show();

            return true;
        }
        return false;
    }

}

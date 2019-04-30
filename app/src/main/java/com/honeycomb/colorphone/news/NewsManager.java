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
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NewsManager {
    private static class NewsManagerHolder {
        private static final NewsManager instance = new NewsManager();
    }

    private NewsManager() {
        AcbNativeAdManager.getInstance().activePlacementInProcess(NEWS_LIST_BANNER);
//        AcbInterstitialAdManager.getInstance().activePlacementInProcess(NEWS_WIRE);
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
//    private NewsLoadListener loadListener;

//    private NewsResultBean resultBean;
    private NewsResultBean pushBean;
    private boolean showNativeAD;

    public NewsResultBean getPushBean() {
        return pushBean;
    }

    void fetchNews(NewsResultBean resultBean, NewsLoadListener loadListener, boolean isVideo) {
        showNativeAD = HSConfig.optBoolean(false, "Application", "News", "NewsTabShowNativeAd");
        if (showNativeAD) {
            AcbNativeAdManager.preload(2, NEWS_LIST_BANNER);
        }
//        AcbInterstitialAdManager.preload(1, NEWS_WIRE);

        HSLog.i(NewsManager.TAG, "fetchNews");
        newOffset = 0;

        HSHttpConnection news = new HSHttpConnection(getURL(String.valueOf(LIMIT_SIZE), String.valueOf(newOffset), isVideo));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    NewsResultBean bean = gson.fromJson(jsonBody, NewsResultBean.class);
                    HSLog.i(TAG, "result size == " + (bean != null ? bean.totalItems : null));
                    if (bean != null && loadListener != null) {
                        addNativeADs(bean);
                        resultBean.totalItems = bean.totalItems;
                        resultBean.content.clear();
                        resultBean.content.addAll(bean.content);
                        loadListener.onNewsLoaded(resultBean);
                        return;
                    }
                }

                if (loadListener != null) {
                    loadListener.onNewsLoaded(null);
                }
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
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
        if (!showNativeAD) {
            return;
        }

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

    void fetchLaterNews(final NewsResultBean resultBean, NewsLoadListener loadListener, boolean isVideo) {
        showNativeAD = HSConfig.optBoolean(false, "Application", "News", "NewsTabShowNativeAd");
        if (showNativeAD) {
            AcbNativeAdManager.preload(2, NEWS_LIST_BANNER);
        }

        int offset = resultBean != null ? newOffset + resultBean.totalItems : 0;
        HSLog.i(NewsManager.TAG, "fetchLaterNews offset == " + offset);

        HSHttpConnection news = new HSHttpConnection(getURL(String.valueOf(LIMIT_SIZE), String.valueOf(offset), isVideo));
        news.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    Gson gson = new Gson();
                    NewsResultBean bean = gson.fromJson(jsonBody, NewsResultBean.class);
                    HSLog.i(TAG, "result: size == " + (bean != null ? bean.totalItems : null));
                    NewsResultBean ret;
                    if (resultBean == null) {
                        ret = bean;
                    } else if (bean == null) {
                        ret = resultBean;
                    } else {
                        ret = new NewsResultBean();
                        ret.totalItems = resultBean.totalItems + bean.totalItems;
                        ret.content.addAll(resultBean.content);
                        ret.content.addAll(bean.content);
                    }

                    HSLog.i(TAG, "result: add size == " + (ret != null ? ret.totalItems : null));
                    if (loadListener != null) {
                        if (ret != null) {
                            addNativeADs(ret);
                        }
                        loadListener.onNewsLoaded(ret);
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
                        false,
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

    private static String getURL(String limit, String offset, boolean isVideo) {
        return getURL(limit, offset, isVideo, 0);
    }

    private static String getURL(String limit, String offset, boolean isVideo, long time) {
        final StringBuilder url = new StringBuilder();

        if (isVideo) {
            url.append(HSConfig.optString("", "Application", "News", "VideoUrl"));
        } else {
            url.append(HSConfig.optString("", "Application", "News", "Url"));
        }
        
        url.append("?userId=").append(userID.toString());

        url.append("&publisherId=").append(HSConfig.optString("", "Application", "News", "PublisherId"));
        url.append("&key=").append(HSConfig.optString("", "Application", "News", "Key"));

        Locale locale = Locale.getDefault();
        url.append("&countryCode=").append(locale.getCountry());
        url.append("&language=").append(locale.getLanguage());

        url.append("&limit=").append(limit);
        url.append("&offset=").append(offset);
        if (time > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy%20hh:mm:ss");
            String date = sdf.format(new Date(time));
//            url.append("&publishedAfter=").append(Uri.encode(date, "UTF-8"));
            url.append("&publishedAfter=").append(date);
        }

        String category;
        if (isVideo) {
            category = HSConfig.optString("", "Application", "News", "VideoType");
        } else {
            category = HSConfig.optString("", "Application", "News", "Category");
        }

        if (!TextUtils.isEmpty(category)) {
            url.append("&category=").append(category);
        }

        HSLog.i(TAG, "getUrl: " + url.toString());
        return url.toString();
    }

    public void preloadAD() {
//        AcbInterstitialAdManager.getInstance().activePlacementInProcess(NEWS_WIRE);
//        AcbInterstitialAdManager.preload(1, NEWS_WIRE);
    }

    AcbInterstitialAd getInterstitialAd() {
        if (mInterstitialAd == null) {
//            List<AcbInterstitialAd> ads = AcbInterstitialAdManager.fetch(NEWS_WIRE, 1);
//            if (ads != null && ads.size() > 0) {
//                mInterstitialAd = ads.get(0);
//            }
        }
        return mInterstitialAd;
    }

    private void releaseInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.release();
            mInterstitialAd = null;
        }
    }

    boolean showInterstitialAd(String from) {
        if (!NewsTest.canShowNewsWireAD()) {
            return false;
        }

        AcbInterstitialAd ad = getInterstitialAd();
        if (ad != null) {
            ad.setInterstitialAdListener(new AcbInterstitialAd.IAcbInterstitialAdListener() {
                @Override
                public void onAdDisplayed() {
                    if (TextUtils.equals(from, WebViewActivity.FROM_ALERT)) {
                        NewsTest.logNewsEvent("news_detail_page_wire_show_from_alert");
                    } else if (TextUtils.equals(from, WebViewActivity.FROM_LIST)) {
                        NewsTest.logNewsEvent("news_detail_page_wire_show_from_list");
                    }
                }

                @Override
                public void onAdClicked() {

                }

                @Override
                public void onAdClosed() {
                    releaseInterstitialAd();
//                    AcbInterstitialAdManager.preload(1, NEWS_WIRE);
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

package com.colorphone.smartlocker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.smartlocker.NewsDetailActivity;
import com.colorphone.smartlocker.bean.BaiduFeedBean;
import com.colorphone.smartlocker.bean.BaiduFeedItemsBean;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NewsUtils {

    private static final String PREF_FILE_DAILY_NEWS = "optimizer_daily_news_utils";
    private static final String PREF_KEY_DAILY_NEWS_PREFIX = "PREF_KEY_DAILY_NEWS_PREFIX";

    private static final String PREF_FILE_DAILY_NEWS_EXTERNAL_CONTENT = "optimizer_daily_news_external_content";
    private static final String PREF_KEY_LATEST_SAVE_NEWS_TIME = "PREF_KEY_LATEST_SAVE_NEWS_TIME";

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

    public static int getCountOfResponse(String response) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonObject = null;
        }

        BaiduFeedItemsBean baiduFeedItemsBean = new BaiduFeedItemsBean(jsonObject);
        List<BaiduFeedBean> baiduFeedBeanList = baiduFeedItemsBean.getBaiduFeedBeans();
        int newsCount = 0;
        for (BaiduFeedBean baiduNewsItemData : baiduFeedBeanList) {
            if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_THREE_IMAGE
                    || baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE) {
                newsCount++;
            }
        }

        return newsCount;
    }
}

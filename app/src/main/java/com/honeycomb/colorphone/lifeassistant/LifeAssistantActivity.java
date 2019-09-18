package com.honeycomb.colorphone.lifeassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

import java.util.Calendar;

public class LifeAssistantActivity extends HSAppCompatActivity implements INotificationObserver {
    private LifeAssistantNewsPage newsPage;
    private String closeReason = "Other";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.life_assistant_page);

        newsPage = findViewById(R.id.news_swipe_layout);
        if (NewsManager.getInstance().getLifeAssistantBean() != null) {
            newsPage.onNewsLoaded(NewsManager.getInstance().getLifeAssistantBean(), 0);
        } else {
            HSGlobalNotificationCenter.addObserver(NewsManager.NOTIFY_KEY_NEWS_LOADED, this);
        }

        ImageView close = findViewById(R.id.close_view);
        close.setOnClickListener(v -> {
            closeReason = "Close";
            finish();
        });
        newsPage.setCloseView(close);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String source = "";
        if (hour >= 5 && hour < 9) {
            source = "Morning";
        } else if ((hour >= 17 && hour < 23)) {
            source = "Evening";
        }

        LifeAssistantConfig.recordLifeAssistantShow();
        Analytics.logEvent("Life_Assistant_Show", "Source", source);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        closeReason = "Back";
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Analytics.logEvent("Life_Assistant_Close", "Type", closeReason);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, NewsManager.NOTIFY_KEY_NEWS_LOADED)) {
            HSLog.d(NewsManager.TAG, "onReceive onNewsLoaded");
            HSGlobalNotificationCenter.removeObserver(this);
            newsPage.onNewsLoaded(NewsManager.getInstance().getLifeAssistantBean(), 0);
        }
    }
}

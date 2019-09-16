package com.honeycomb.colorphone.lifeassistant;

import android.os.Bundle;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.news.NewsManager;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class LifeAssistantActivity extends HSAppCompatActivity {
    private LifeAssistantNewsPage newsPage;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.life_assistant_page);

        newsPage = findViewById(R.id.news_swipe_layout);
        newsPage.onNewsLoaded(NewsManager.getInstance().getExitNewsBean(), 0);

        View close = findViewById(R.id.close_view);
        close.setOnClickListener(v -> {
            finish();
        });

//        View setting = findViewById(R.id.life_assistant_setting);
//        setting.setOnClickListener(v -> {
//            Navigations.startActivitySafely(this, LifeAssistantSettingActivity.class);
//        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        NewsManager.getInstance().releaseNativeAd();
    }
}

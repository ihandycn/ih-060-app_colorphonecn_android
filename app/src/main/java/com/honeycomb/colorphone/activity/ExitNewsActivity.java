package com.honeycomb.colorphone.activity;

import android.os.Bundle;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.news.ExitNewsPage;
import com.honeycomb.colorphone.news.NewsManager;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class ExitNewsActivity extends HSAppCompatActivity {
    private ExitNewsPage newsPage;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_exit_page);

        newsPage = findViewById(R.id.news_swipe_layout);
        newsPage.onNewsLoaded(NewsManager.getInstance().getExitNewsBean(), 0);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        NewsManager.getInstance().releaseNativeAd();
    }
}

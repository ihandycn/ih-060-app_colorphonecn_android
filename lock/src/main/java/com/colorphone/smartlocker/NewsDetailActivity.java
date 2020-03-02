package com.colorphone.smartlocker;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.StatusBarUtils;
import com.colorphone.smartlocker.view.NewsWebView;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class NewsDetailActivity extends HSAppCompatActivity {

    public static final String INTENT_EXTRA_URL = "INTENT_EXTRA_URL";
    public static final String INTENT_EXTRA_TITLE = "INTENT_EXTRA_TITLE";
    public static final String INTENT_EXTRA_SHOW_WHEN_LOCKED = "INTENT_EXTRA_SHOW_WHEN_LOCKED";

    private NewsWebView webView;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private FrameLayout videoShowLayout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(INTENT_EXTRA_SHOW_WHEN_LOCKED, false)) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        setContentView(R.layout.activity_toutiao_detail);
        setStatusBarColor();
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.daily_news_title_text));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final RelativeLayout emptyView = findViewById(R.id.empty_view);

        webView = findViewById(R.id.web_view);
        videoShowLayout = findViewById(R.id.video_show_layout);

        webView.initWebView();
        webView.setWebViewStatusChangedListener(new NewsWebView.WebViewStatusChangedListener() {
            @Override
            public void onWebUrlChange(boolean canGoForward, String currentUrl) {
            }

            @Override
            public void onWebViewMove(int moveType) {
            }

            @Override
            public void onPageStart(String websiteUrl) {
            }

            @Override
            public void onPageFinish(boolean isReload) {
                findViewById(R.id.loading_layout).setVisibility(View.GONE);
            }

            @Override
            public void onVideoShow(View view, WebChromeClient.CustomViewCallback callback) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;
                videoShowLayout.setVisibility(View.VISIBLE);
                videoShowLayout.addView(customView);
                webView.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
            }

            @Override
            public void onVideoHidden() {
                if (customView == null) {
                    return;
                }

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                webView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                customView.setVisibility(View.GONE);
                videoShowLayout.removeView(customView);
                customView = null;
                videoShowLayout.setVisibility(View.GONE);
                customViewCallback.onCustomViewHidden();
                getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            }
        });

        try {
            webView.loadUrl(getIntent().getStringExtra(INTENT_EXTRA_URL));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!NetworkStatusUtils.isNetworkConnected(this)) {
            emptyView.setVisibility(View.VISIBLE);
        }

        View connectAgain = findViewById(R.id.connect_again);
        connectAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkStatusUtils.isNetworkConnected(NewsDetailActivity.this)) {
                    Toast.makeText(NewsDetailActivity.this, getString(R.string.no_network_now), Toast.LENGTH_SHORT).show();
                    return;
                }

                emptyView.setVisibility(View.GONE);
                findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);

                try {
                    webView.loadUrl(getIntent().getStringExtra(INTENT_EXTRA_URL));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (videoShowLayout.isShown()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            customView.setVisibility(View.GONE);
            videoShowLayout.removeView(customView);
            customView = null;
            videoShowLayout.setVisibility(View.GONE);
            customViewCallback.onCustomViewHidden();

            webView.setVisibility(View.VISIBLE);
            webView.onResume();
        }
    }

    private void setStatusBarColor() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null && toolbar.getBackground() instanceof ColorDrawable) {
            StatusBarUtils.setStatusBarColor(this, ((ColorDrawable) toolbar.getBackground()).getColor());
        }
    }
}

package com.colorphone.smartlocker.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.StatusBarUtils;

public class NewsDetailView extends RelativeLayout {
    private Activity activity;

    private Toolbar toolbar;
    private View customView;
    private NewsWebView newsWebView;
    private RelativeLayout emptyView;
    private FrameLayout videoShowLayout;

    private WebChromeClient.CustomViewCallback customViewCallback;

    private String newsUrl;

    public NewsDetailView(Context context) {
        super(context);
        init(context);
    }

    public NewsDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NewsDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        activity = (Activity) context;
        View.inflate(context, R.layout.layout_news_detail, this);
        setBackgroundColor(ContextCompat.getColor(context, R.color.clean_app_green));

        setPadding(0, StatusBarUtils.getStatusBarHeight(context), 0, 0);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(context.getString(R.string.daily_news_title_text));
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeNewsDetailPage();
            }
        });

        newsWebView = findViewById(R.id.web_view);
        emptyView = findViewById(R.id.empty_view);
        videoShowLayout = findViewById(R.id.video_show_layout);

        newsWebView.setWebViewStatusChangedListener(new NewsWebView.WebViewStatusChangedListener() {
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
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;
                videoShowLayout.setVisibility(View.VISIBLE);
                videoShowLayout.addView(customView);
                newsWebView.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                activity.getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
            }

            @Override
            public void onVideoHidden() {
                if (customView == null) {
                    return;
                }

                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                newsWebView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                customView.setVisibility(View.GONE);
                videoShowLayout.removeView(customView);
                videoShowLayout.setVisibility(View.GONE);
                customViewCallback.onCustomViewHidden();
                customView = null;
                activity.getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            }
        });

        View connectAgain = findViewById(R.id.connect_again);
        connectAgain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkStatusUtils.isNetworkConnected(context)) {
                    Toast.makeText(context, context.getString(R.string.no_network_now), Toast.LENGTH_SHORT).show();
                    return;
                }

                emptyView.setVisibility(View.GONE);
                findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);

                try {
                    newsWebView.loadUrl(newsUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void loadUrl(String url) {
        this.newsUrl = url;

        newsWebView.initWebView();

        if (!NetworkStatusUtils.isNetworkConnected(activity)) {
            emptyView.setVisibility(View.VISIBLE);
        }

        try {
            newsWebView.loadUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeNewsDetailPage() {
        setVisibility(GONE);
        newsWebView.onDestroy();
    }

    public boolean onBackClicked() {
        if (videoShowLayout.isShown()) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            customView.setVisibility(View.GONE);
            videoShowLayout.removeView(customView);
            customView = null;
            videoShowLayout.setVisibility(View.GONE);
            customViewCallback.onCustomViewHidden();

            newsWebView.setVisibility(View.VISIBLE);
            newsWebView.onResume();
            return true;
        }
        return false;
    }
}

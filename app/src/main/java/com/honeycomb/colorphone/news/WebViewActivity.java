package com.honeycomb.colorphone.news;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Thunk;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Networks;
import com.superapps.util.Threads;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Activity that contains a {@link WebView}, a loading progress bar, a reload button and a back button.
 */
public class WebViewActivity extends HSAppCompatActivity implements View.OnClickListener {

    private static final String NEWS_DETAIL_URL = "news_detail_url";
    private static final String NEWS_DETAIL_SHOULD_SHOW_TITLE = "news_detail_should_show_title";
    private static final String USE_WIDE_VIEWPORT_AND_LOAD_WITH_OVERVIEW_MODE = "use_wide_viewport_and_load_with_overview_mode";
    private static final String KEY_FROM = "key_from";

    public static final String FROM_ALERT = "alert";
    public static final String FROM_LIST = "list";

    // Instantiate WebView after activity launch for better experience
    private ViewStub mWebViewStub;
    private WebView mWebView;

    private Toolbar toolbar;
    private ViewStub mErrorPageStub;
    private View mErrorPage;
    private SmoothProgressBar mLoadingProgressBar;
    private View mReloadBtn;
    private ValueAnimator mProgressAnimator;
    private float mDelta;
    private float mOldProgress;
    private float mTargetProgress;
    private boolean mOnPageStart;
    private boolean mReceiveError;
    private String mUrl;
    private String mLinkUrl = "";
    private float mAnimatedValue;
    private List<String> mHistory = new ArrayList<>();
    private boolean mShouldShowTitle;
    private String from;

    public static Intent newIntent(String url, boolean shouldShowTitle, String from) {
        return newIntent(url, shouldShowTitle, from,  true);
    }

    public static Intent newIntent(String url, boolean shouldShowTitle, String from, boolean wideViewport) {
        Intent intent = new Intent(HSApplication.getContext(), WebViewActivity.class);
        intent.putExtra(NEWS_DETAIL_URL, url);
        intent.putExtra(NEWS_DETAIL_SHOULD_SHOW_TITLE, shouldShowTitle);
        intent.putExtra(USE_WIDE_VIEWPORT_AND_LOAD_WITH_OVERVIEW_MODE, wideViewport);
        intent.putExtra(KEY_FROM, from);
        return intent;
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mShouldShowTitle = getIntent().getBooleanExtra(NEWS_DETAIL_SHOULD_SHOW_TITLE, false);
        from = getIntent().getStringExtra(KEY_FROM);
        ViewUtils.findViewById(this, R.id.news_detail_root).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mWebViewStub = ViewUtils.findViewById(this, R.id.web_view_stub);
        mLoadingProgressBar = ViewUtils.findViewById(this, R.id.loading_progress_bar);
        mErrorPageStub = ViewUtils.findViewById(this, R.id.error_page_stub);

        mProgressAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isFinishing()) {
                    return;
                }
                mAnimatedValue = (float) animation.getAnimatedValue();
                float newProgress = mOldProgress + (mAnimatedValue * mDelta);
                if (Float.compare(newProgress, 100f) == 0 && Float.compare(mTargetProgress, 100f) == 0) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                }
                if (newProgress <= 90 || newProgress <= mTargetProgress)
                    mLoadingProgressBar.setProgress(newProgress);
            }
        });
        mProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                float curProgress = mLoadingProgressBar.getProgress();
                if (Float.compare(mAnimatedValue, 1.0f) == 0 && curProgress <= 90) {
                    mOldProgress = mLoadingProgressBar.getProgress();
                    mDelta = 90 - mOldProgress;
                    mProgressAnimator.setDuration((long) (50 * mDelta));
                    mProgressAnimator.start();
                }
            }
        });

        NewsTest.logNewsEvent("news_detail_page_show");

        Threads.postOnMainThreadDelayed(() -> {
//            NewsManager.getInstance().showInterstitialAd(from);
        }, 1000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mWebView == null) {
            inflateAndConfigWebView();
        }
    }

    private void inflateErrorPageIfNeeded() {
        if (mErrorPage == null) {
            mErrorPage = mErrorPageStub.inflate();
            mReloadBtn = ViewUtils.findViewById(mErrorPage, R.id.reload_button);
            mReloadBtn.setOnClickListener(this);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @DebugLog
    private void inflateAndConfigWebView() {
        try {
            mWebView = (WebView) mWebViewStub.inflate();
        } catch (Exception e) {
            // Some crashes caused by WebView constructor are found on XOLO BLACK and Oppo F1f (#375) devices
            e.printStackTrace();
            finish();
            return;
        }
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (mLoadingProgressBar.getVisibility() == View.GONE && mOnPageStart && !mReceiveError) {
                    mLoadingProgressBar.setVisibility(View.VISIBLE);
                    mLoadingProgressBar.setProgress(0);
                }
                mTargetProgress = newProgress;
                float curProgress = mLoadingProgressBar.getProgress();
                if (mTargetProgress > curProgress) {
                    mOldProgress = curProgress;
                    mDelta = mTargetProgress - mOldProgress;
                    if (mProgressAnimator.isRunning()) {
                        mProgressAnimator.cancel();
                    }
                    int perTime = 40;
                    if (mTargetProgress <= 10) {
                        perTime = 200;
                    }
                    mProgressAnimator.setDuration((long) (perTime * mDelta));
                    mProgressAnimator.start();
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (mShouldShowTitle) {
                    toolbar.setTitle(title);
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (mLinkUrl.equals(url)) {
                    // Load a new url from click event
                    if (mProgressAnimator.isRunning()) {
                        mProgressAnimator.cancel();
                    }
                    HSLog.d("News.overrideurl", "add " + mLinkUrl + "to history");
                    mHistory.add(mLinkUrl);
                }
                mLoadingProgressBar.setProgress(0);
                mUrl = url;
                if (mShouldShowTitle) {
                    toolbar.setTitle(url);
                }
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mOnPageStart = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mOnPageStart = false;
            }

            /**
             * For Marshmallow.
             */
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                handleError(request, error);
            }

            /**
             * Pre-Marshmallow.
             */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                handleError(null, null);
            }

            private void handleError(WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (TextUtils.equals(request.getUrl().toString(), mUrl)) {
                        onFatalError();
                    }
                } else {
                    onFatalError();
                }
            }

            private void onFatalError() {
                mReceiveError = true;
                showErrorPage();
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        // Enable JS
        try {
            webSettings.setJavaScriptEnabled(true);
        } catch (NullPointerException e) {
            // See https://code.google.com/p/android/issues/detail?id=40944
            e.printStackTrace();
        }
        // Enable page adaptation
        if (getIntent().getBooleanExtra(USE_WIDE_VIEWPORT_AND_LOAD_WITH_OVERVIEW_MODE, true)) {
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
        }

        // Enable zoom
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        mWebView.setBackgroundColor(ContextCompat.getColor(WebViewActivity.this, R.color.app_bar_activities_content_bg));

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mWebView == null) {
                    // Activity destroyed
                    return true;
                }
                Message msg = Message.obtain();
                msg.setTarget(new HrefHandler(WebViewActivity.this));
                mWebView.requestFocusNodeHref(msg);
                return false;
            }
        });

        mUrl = getIntent().getStringExtra(NEWS_DETAIL_URL);
        if (TextUtils.isEmpty(mUrl)) {
            showErrorPage();
        } else {
            mWebView.loadUrl(mUrl);
            mHistory.add(mUrl);
        }
    }

    private void showErrorPage() {
        if (mWebView == null) {
            // Activity destroyed
            return;
        }
        // Clear default error page
        mWebView.loadDataWithBaseURL(null, "", null, "utf-8", mUrl);
        mWebView.setVisibility(View.GONE);
        inflateErrorPageIfNeeded();
        mErrorPage.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.GONE);
    }

    private void reload() {
        if (mWebView == null) {
            return;
        }
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        mLoadingProgressBar.setProgress(0);
        // Clear default error page HTML content
        mReceiveError = false;
        mWebView.loadUrl(mUrl);
        if (mHistory.isEmpty() || !mHistory.get(mHistory.size() - 1).equals(mUrl)) {
            mHistory.add(mUrl);
        }
        mWebView.setVisibility(View.VISIBLE);
        if (mErrorPage != null) {
            mErrorPage.setVisibility(View.GONE);
        }
    }

    private void configAppBar() {
        toolbar = findViewById(R.id.action_bar).findViewById(R.id.inner_tool_bar);
        ActivityUtils.configSimpleAppBar(this, "", ContextCompat.getColor(this, R.color.material_text_black_primary), Color.WHITE, true);
        ActivityUtils.configStatusBarColor(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar_action_news_refresh, menu);
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        configAppBar();
        mLoadingProgressBar.startAnimator();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLoadingProgressBar.stopAnimator();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Analytics.logEvent("news_detail_page_back_click");
                finish();
                return true;
            case R.id.action_bar_refresh:
//                onClickRefresh(toolbar.findViewById(R.id.action_bar_refresh));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mHistory.size() > 1 && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mProgressAnimator.isRunning()) {
                mProgressAnimator.cancel();
            }
            mLoadingProgressBar.setProgress(0);
            if (Networks.isNetworkAvailable(-1)) {
                mHistory.remove(mHistory.size() - 1);
                mWebView.loadUrl(mHistory.get(mHistory.size() - 1));
            } else {
                Analytics.logEvent("news_detail_page_back_click");
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override public void onBackPressed() {
        super.onBackPressed();
        Analytics.logEvent("news_detail_page_back_click");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (mShouldShowTitle) {
                Analytics.logEvent("Search_WebPage_CloseIcon_Clicked");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }
        // mWebView might be null in case activity is destroyed before web view gets inflated
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mReloadBtn) {
            reload();
        }
    }

    private void onClickRefresh(View v) {
        if (mShouldShowTitle) {
            Analytics.logEvent("Search_WebPage_RefreshIcon_Clicked");
        }
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.search_trending_word_refresh);
        v.clearAnimation();
        v.startAnimation(animation);
        reload();
    }

    @Thunk void setLinkUrl(String url) {
        this.mLinkUrl = url;
    }

    private static class HrefHandler extends Handler {

        private WeakReference<WebViewActivity> mReference;

        public HrefHandler(WebViewActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            String linkUrl = msg.getData().getString("url");
            WebViewActivity webViewActivity = mReference.get();
            if (webViewActivity != null && linkUrl != null) {
                webViewActivity.setLinkUrl(linkUrl);
            }
        }
    }
}

package com.colorphone.smartlocker.h5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.DisplayUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

public class ProgressWebView extends RelativeLayout {

    public abstract static class WebViewStatusChangedListener {
        public void onWebUrlChange(boolean canGoForward, String currentUrl) {
        }

        public void onWebViewMove(@WebMoveType int moveType) {
        }

        public void onPageStart(String websiteUrl) {
        }

        public void onPageFinish(boolean isReload) {
        }

        public void onReceivedError() {
        }

        public void onVideoShow(View view, WebChromeClient.CustomViewCallback callback) {
        }

        public void onVideoHidden() {
        }
    }

    @IntDef({WEB_MOVE_UP, WEB_MOVE_DOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WebMoveType {
    }

    public static final int WEB_MOVE_UP = 1;
    public static final int WEB_MOVE_DOWN = 2;

    private static final String TAG = "ProgressWebView";

    private ProgressBar progressBar;
    private WebView webView;
    private Context context;

    @Nullable
    private WebViewStatusChangedListener statusChangedListener;

    private boolean isReload;
    private float positionY, currentPosY;

    private String websiteTitle;
    private String avatarUrl;

    public ProgressWebView(Context context) {
        super(context);
        this.context = context;
    }

    public ProgressWebView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ProgressWebView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public void initWebView() {
        if (webView != null) {
            HSLog.i(TAG, "initWebView() webView != null");
            onResume();
            return;
        }

        webView = new WebView(context);
        ViewGroup.LayoutParams vl2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(vl2);
        addView(webView);

        progressBar = new ProgressBar(context, null, android.R.style.Widget_Holo_Light_ProgressBar_Horizontal);
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.progress_bar_height));
        progressBar.setLayoutParams(vl);
        progressBar.setBackgroundColor(Color.WHITE);
        ClipDrawable drawable = new ClipDrawable(new ColorDrawable(getResources().getColor(R.color.progress_bar_color))
                , Gravity.LEFT, ClipDrawable.HORIZONTAL);
        progressBar.setProgressDrawable(drawable);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        addView(progressBar);

        init();
        initWebViewListener();
        initWebSetting();
    }

    private void init() {
        setSaveEnabled(true);
        webView.setAlwaysDrawnWithCacheEnabled(true);
        webView.setAnimationCacheEnabled(true);
        webView.setDrawingCacheBackgroundColor(0);
        webView.setDrawingCacheEnabled(true);
        webView.setWillNotCacheDrawing(false);
        webView.setSaveEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.setBackground(null);
            webView.getRootView().setBackground(null);
        }

        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setScrollbarFadingEnabled(true);
    }

    private void initWebSetting() {
        WebSettings webSettings = webView.getSettings();

        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getContext().getCacheDir().getAbsolutePath());

        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationDatabasePath(getContext().getFilesDir().toString());
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.setLoadsImagesAutomatically(false);
        } else {
            webSettings.setLoadsImagesAutomatically(true);
        }

        webSettings.setBlockNetworkImage(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setSaveFormData(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initWebViewListener() {
        webView.setWebViewClient(new WebViewClient() {

//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                HSLog.d(TAG, "url: " + url);
//                if (url.startsWith("tel:")) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW,
//                            Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    HSApplication.getContext().startActivity(intent);
//                    return true;
//                }
//                view.loadUrl(url);
//                return super.shouldOverrideUrlLoading(view, url);
//            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                websiteTitle = "";

                if (progressBar != null) {
                    progressBar.setVisibility(VISIBLE);
                    progressBar.setProgress(0);
                }

                if (statusChangedListener != null && webView != null) {
                    statusChangedListener.onPageStart(url);
                    statusChangedListener.onWebUrlChange(webView.canGoForward(), url);
                }
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }

                        if (statusChangedListener != null) {
                            statusChangedListener.onPageFinish(isReload);
                            isReload = false;
                            if (webView != null) {
                                if (webView.getSettings() != null && !webView.getSettings().getLoadsImagesAutomatically()) {
                                    webView.getSettings().setLoadsImagesAutomatically(true);
                                }
                                statusChangedListener.onWebUrlChange(webView.canGoForward(), url);
                            }
                        }
                    }
                }, 200);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (statusChangedListener != null) {
                    statusChangedListener.onReceivedError();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            private Bitmap defaultVideoPoster;

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                websiteTitle = title.split(" ")[0];
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (statusChangedListener != null) {
                    statusChangedListener.onVideoShow(view, callback);
                }
            }

            @Override
            public void onHideCustomView() {
                if (statusChangedListener != null) {
                    statusChangedListener.onVideoHidden();
                }
            }

            @Override
            public Bitmap getDefaultVideoPoster() {

                if (defaultVideoPoster == null) {
                    defaultVideoPoster = DisplayUtils.drawable2Bitmap(VectorDrawableCompat.create(HSApplication.getContext().getResources(),
                            R.drawable.ic_safe_browsing_video_default_poster, null));
                }
                return super.getDefaultVideoPoster();
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                super.onReceivedTouchIconUrl(view, url, precomposed);

                HSLog.i(TAG, "onReceivedTouchIconUrl() icon url is " + url);
                if (!TextUtils.isEmpty(url)) {
                    avatarUrl = url;
                }
            }
        });

        webView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        positionY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        currentPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (currentPosY - positionY > 0 && (Math.abs(currentPosY - positionY) > 100)) {
                            if (statusChangedListener != null) {
                                statusChangedListener.onWebViewMove(WEB_MOVE_UP);
                            }
                        } else if (currentPosY - positionY < 0 && (Math.abs(currentPosY - positionY) > 100)) {
                            if (statusChangedListener != null) {
                                statusChangedListener.onWebViewMove(WEB_MOVE_DOWN);
                            }
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    public void loadUrl(String url) {
        webView.loadUrl(url);
    }

    public void setWebViewStatusChangedListener(WebViewStatusChangedListener listener) {
        statusChangedListener = listener;
    }

    public void stopLoading() {
        webView.stopLoading();
    }

    public void goBack() {
        if (webView.canGoBack()) {
            webView.reload();
            webView.stopLoading();
            webView.goBack();
        }
    }

    public void goForward() {
        if (webView.canGoForward()) {
            webView.stopLoading();
            webView.goForward();
        }
    }

    public void reload() {
        webView.stopLoading();
        webView.reload();
        isReload = true;
    }

    public void onPause() {
        if (webView != null) {
            webView.onPause();
        }
    }

    public void onResume() {
        if (webView != null) {
            webView.onResume();
        }
    }

    public void onDestroy(boolean clearCookies) {
        if (webView != null) {
            removeView(webView);

            webView.stopLoading();
            webView.onPause();

            webView.clearHistory();
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearSslPreferences();
            WebStorage.getInstance().deleteAllData();
            webView.destroyDrawingCache();
            webView.removeAllViews();

            webView.destroy();
            webView = null;
        }

        if (clearCookies) {
            CookieSyncManager.createInstance(HSApplication.getContext());
            CookieSyncManager.getInstance().startSync();
            CookieManager.getInstance().removeSessionCookie();
        }

        releaseAllWebViewCallback();

        if (progressBar != null) {
            removeView(progressBar);
        }
    }

    public void onDestroy() {
        onDestroy(true);
    }

    private void releaseAllWebViewCallback() {
        try {
            Field configCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
            if (configCallback != null) {
                configCallback.setAccessible(true);
                configCallback.set(null, null);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    public String getWebsiteUrl() {
        if (webView == null) {
            return "";
        }
        return webView.getUrl();
    }

    public String getWebsiteTitle() {
        return websiteTitle;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}

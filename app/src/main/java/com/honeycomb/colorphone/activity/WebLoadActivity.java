package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.concurrent.atomic.AtomicBoolean;

public class WebLoadActivity extends HSAppCompatActivity {


    public static final String WEB_LOAD_URL = "WEB_LOAD_URL";

    private AtomicBoolean errorState = new AtomicBoolean(false);

    private WebView webView;

    private ProgressBar webProgressBar;


    private TextView webErrorView;

    private String originalUrl;

    public static void start(Context context, String url) {
        Intent starter = new Intent(context, WebLoadActivity.class);
        starter.putExtra(WEB_LOAD_URL, url);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_load);
        webView = this.findViewById(R.id.web_content_view);
        webProgressBar = this.findViewById(R.id.web_loading_view);
        webErrorView = this.findViewById(R.id.web_error_view);
        initWebView();
        startLoad();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startLoad() {
        showWebLoading();
        try {
            originalUrl = getIntent().getStringExtra(WEB_LOAD_URL);
            webView.loadUrl(originalUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showWebLoading() {
        webView.setVisibility(View.GONE);
        webProgressBar.setVisibility(View.VISIBLE);
        webErrorView.setVisibility(View.GONE);
    }

    private void showWebContent() {
        webView.setVisibility(View.VISIBLE);
        webProgressBar.setVisibility(View.GONE);
        webErrorView.setVisibility(View.GONE);
    }


    private void showWebError() {
        webView.setVisibility(View.GONE);
        webProgressBar.setVisibility(View.GONE);
        webErrorView.setVisibility(View.VISIBLE);
    }


    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSavePassword(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                errorState.compareAndSet(false, true);
                showWebError();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                errorState.compareAndSet(false, false);
                showWebLoading();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (errorState.get()) {
                    showWebError();
                    return;
                }
                showWebContent();
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                errorState.compareAndSet(false, true);
                try {
                    if (handler != null)
                        handler.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                return super.shouldOverrideUrlLoading(view, url);

            }
        });

    }


}

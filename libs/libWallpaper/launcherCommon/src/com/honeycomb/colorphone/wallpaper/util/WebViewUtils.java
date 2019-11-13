package com.honeycomb.colorphone.wallpaper.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import static android.view.View.SCROLLBARS_INSIDE_OVERLAY;

public final class WebViewUtils {

    @SuppressLint("SetJavaScriptEnabled")
    public static void configWebViewSettings(WebView webView, Context context, boolean supportMultiWindow) {
        WebSettings settings = webView.getSettings();

        // Enable JS
        try {
            settings.setJavaScriptEnabled(true);
        } catch (NullPointerException e) {
            // See https://code.google.com/p/android/issues/detail?id=40944
            e.printStackTrace();
        }
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setSaveFormData(true);
        settings.setDatabaseEnabled(true);

        // Enable page adaptation
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        PackageManager packageManager = context.getPackageManager();
        boolean supportMultiTouch = packageManager.hasSystemFeature("android.hardware.touchscreen.multitouch")
                || packageManager.hasSystemFeature("android.hardware.faketouch.multitouch.distinct");
        settings.setDisplayZoomControls(!supportMultiTouch);
        webView.setVerticalScrollBarEnabled(false);
        webView.setVerticalScrollbarOverlay(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setHorizontalScrollbarOverlay(false);
        webView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(true);
        settings.setAllowContentAccess(true);
        settings.setDefaultTextEncodingName("utf-8");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(true);
        }
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        settings.setSupportMultipleWindows(supportMultiWindow);
    }

    public static void destroy(WebView webView) {
        if (webView != null) {
            try {
                ViewGroup viewGroup = (ViewGroup) webView.getParent();
                if (viewGroup != null) {
                    viewGroup.removeView(webView);
                }
                webView.stopLoading();
                webView.destroy();
            } catch (Throwable th) {
            }
        }
    }

    public static void resume(WebView webView) {
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    public static void pause(WebView webView) {
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }
}

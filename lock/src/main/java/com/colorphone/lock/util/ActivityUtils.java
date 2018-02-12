package com.colorphone.lock.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colorphone.lock.R;

public class ActivityUtils {

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor) {
        configSimpleAppBar(activity, title, bgColor, true);
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, Typeface tf, int bgColor) {
        configSimpleAppBar(activity, title, tf, Color.WHITE, bgColor, true);
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor,
                                          boolean containsBackButton) {
        configSimpleAppBar(activity, title, Color.WHITE, bgColor, containsBackButton);
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int titleColor, int bgColor,
                                          boolean containsBackButton) {
        configSimpleAppBar(activity, title, null, titleColor, bgColor, containsBackButton);
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, Typeface tf, int titleColor, int bgColor,
                                          boolean containsBackButton) {
        View container = activity.findViewById(R.id.action_bar);

        assert container != null;
        Toolbar toolbar;
        if (container instanceof LinearLayout) {
            toolbar = (Toolbar) container.findViewById(R.id.inner_tool_bar);
        } else {
            toolbar = (Toolbar) container;
        }
        assert toolbar != null;

        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        ViewStyleUtils.setToolBarTitle(titleTextView, !containsBackButton);
        titleTextView.setTextColor(titleColor);
        titleTextView.setText(title);
        titleTextView.setMaxLines(1);
        if (tf != null) {
            titleTextView.setTypeface(tf);
        }
        toolbar.addView(titleTextView);

        toolbar.setBackgroundColor(bgColor);
        activity.setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection ConstantConditions
            container.setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        } else {
            View line = activity.findViewById(R.id.toolbar_separate_line);
            if (line != null) {
                line.setVisibility(View.VISIBLE);
            }
        }
        if (containsBackButton) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public static void configStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setWhiteStatusBar(activity);
            View decor = activity.getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setCustomColorStatusBar(activity, Color.BLACK);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setWhiteStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.white));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setCustomColorStatusBar(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

}

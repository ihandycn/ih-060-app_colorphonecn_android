package com.acb.libwallpaper.live.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

 import com.honeycomb.colorphone.R;

public class LauncherActivityUtils {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentSystemBarsForLmpNoNavigation(Activity activityContext) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                    .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static Toolbar configSimpleAppBar(AppCompatActivity activity, @StringRes int titleResId) {
        return configSimpleAppBar(activity, activity.getString(titleResId));
    }

    public static Toolbar configSimpleAppBar(AppCompatActivity activity, String title) {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.action_bar);

        // Title
        assert toolbar != null;
        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        ViewStyleUtils.setToolBarTitle(titleTextView);
        titleTextView.setText(title);
        toolbar.addView(titleTextView);

        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_action_bar_bg));
        activity.setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            activity.getSupportActionBar().setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        return toolbar;
    }

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
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            container.setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        } else {
            View line = activity.findViewById(R.id.tab_separate_line_top);
            if (line != null) {
                line.setVisibility(View.VISIBLE);
            }
        }
        if (containsBackButton) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor, int logoResource) {
        View container = activity.findViewById(R.id.action_bar);
        assert container != null;

        Toolbar toolbar = null;
        if (container instanceof LinearLayout) {
            toolbar = (Toolbar) container.findViewById(R.id.inner_tool_bar);
        } else {
            toolbar = (Toolbar) container;
        }
        assert toolbar != null;

        // Title
        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        ViewStyleUtils.setToolBarTitle(titleTextView);
        titleTextView.setText(title);
        toolbar.addView(titleTextView);
        if (logoResource > 0) {
            toolbar.setLogo(logoResource);
        }
        toolbar.setBackgroundColor(bgColor);
        activity.setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            activity.getSupportActionBar().setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}

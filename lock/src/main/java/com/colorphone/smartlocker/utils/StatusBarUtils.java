package com.colorphone.smartlocker.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class StatusBarUtils {

    public static final int DEFAULT_STATUS_BAR_COLOR = 0x44000000;
    public static final int DEFAULT_STATUS_BAR_ALPHA = 44;

    private static int statusBarHeight = -1;

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        setStatusBarColor(activity, color, DEFAULT_STATUS_BAR_ALPHA);
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color, int fakeStatusBarViewID) {
        setStatusBarColor(activity, color, DEFAULT_STATUS_BAR_ALPHA, fakeStatusBarViewID);
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color, int statusBarAlpha, int fakeStatusBarViewID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(calculateStatusColor(color, statusBarAlpha));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View fakeStatusBarView = decorView.findViewById(fakeStatusBarViewID);
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView.getVisibility() == View.GONE) {
                    fakeStatusBarView.setVisibility(View.VISIBLE);
                }
                fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
            } else {
                decorView.addView(createStatusBarView(activity, color, statusBarAlpha));
            }
            setRootView(activity);
        }
    }

    public static void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static void addTranslucentView(Activity activity, int statusBarAlpha) {
        addTranslucentView(activity, statusBarAlpha, 0);
    }

    public static void addTranslucentView(Activity activity, int statusBarAlpha, int fakeTranslucentViewID) {
        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View fakeTranslucentView = contentView.findViewById(fakeTranslucentViewID);
        if (fakeTranslucentView != null) {
            if (fakeTranslucentView.getVisibility() == View.GONE) {
                fakeTranslucentView.setVisibility(View.VISIBLE);
            }
            fakeTranslucentView.setBackgroundColor(Color.argb(statusBarAlpha, 0, 0, 0));
        } else {
            contentView.addView(createTranslucentStatusBarView(activity, statusBarAlpha, fakeTranslucentViewID));
        }
    }

    private static View createStatusBarView(Activity activity, @ColorInt int color, int alpha, int fakeStatusBarViewID) {
        // 绘制一个和状态栏一样高的矩形
        return createStatusBarView(activity, calculateStatusColor(color, alpha), fakeStatusBarViewID);
    }

    public static View createStatusBarView(Activity activity, @ColorInt int color) {
        return createStatusBarView(activity, color, 0);
    }

    public static View createStatusBarView(Activity activity, @ColorInt int color, int fakeStatusBarViewID) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(color);
        statusBarView.setId(fakeStatusBarViewID);
        return statusBarView;
    }


    private static View createTranslucentStatusBarView(Activity activity, int alpha, int fakeTranslucentViewID) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(activity);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        statusBarView.setId(fakeTranslucentViewID);
        return statusBarView;
    }

    private static void setRootView(Activity activity) {
        ViewGroup parent = (ViewGroup) activity.findViewById(android.R.id.content);
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View childView = parent.getChildAt(i);
            if (childView instanceof ViewGroup) {
                childView.setFitsSystemWindows(true);
                ((ViewGroup) childView).setClipToPadding(true);
            }
        }
    }

    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight < 0) {
            // 获得状态栏高度
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        return statusBarHeight;
    }

    public static int calculateStatusColor(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }
}

package com.honeycomb.colorphone.wallpaper.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;

public class ActivityUtils {

    private static final int DEFAULT_NAVIGATION_BAR_COLOR = Color.BLACK;

    public static void configStatusBarColor(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setWhiteStatusBar(activity);
            View decor = activity.getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setCustomColorStatusBar(activity, activity.getResources().getColor(R.color.status_bar));
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    public static void setStatusBarAlpha(Activity activity, float progress) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            int alpha = (int) (0Xff * progress);
            activity.getWindow().setStatusBarColor(Color.argb(alpha, 0xff, 0xff, 0xff));
        }
    }

    public static void hideStatusBar(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void showStatusBar(Activity activity) {
        final Window window = activity.getWindow();
        if ((window.getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) ==
                WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarAlpha(Activity activity, float alpha) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            int alphaInt = (int) (0xff * alpha);
            activity.getWindow().setNavigationBarColor(Color.argb(alphaInt, 0x00, 0x00, 0x00));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColor(Activity activity, int color) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarDefaultColor(Activity activity) {
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            setNavigationBarColor(activity, DEFAULT_NAVIGATION_BAR_COLOR);
        }
    }

    public static void hideSystemUi(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static Activity contextToActivitySafely(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return (Activity) (((ContextThemeWrapper) context).getBaseContext());
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            return (Activity) (((android.support.v7.view.ContextThemeWrapper) context).getBaseContext());
        } else if (context instanceof android.support.v7.widget.TintContextWrapper) {
            return (Activity) (((android.support.v7.widget.TintContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public static boolean isDestroyed(Activity activity) {
        if (activity == null) {
            return false;
        }

        if (activity.isFinishing()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        }
        return false;
    }
}

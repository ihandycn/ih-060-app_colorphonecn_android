package com.honeycomb.colorphone.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.InsettableFrameLayout;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ActivityUtils {
    private static final int DEFAULT_NAVIGATION_BAR_COLOR = Color.BLACK;

    public static void configStatusBarColor(Activity activity) {
        setCustomColorStatusBar(activity, activity.getResources().getColor(android.R.color.white));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setCustomColorStatusBar(Activity activity, int color) {
        boolean isLightColor = isLightColor(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    && isLightColor) {
                window.setStatusBarColor(Color.LTGRAY);
            } else {
                window.setStatusBarColor(color);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = activity.getWindow().getDecorView();
            if (isLightColor) {
                if (Compats.IS_XIAOMI_DEVICE) {
                    setStatusBarDarkModeXiaomi(true, activity.getWindow());
                }
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                if (Compats.IS_XIAOMI_DEVICE) {
                    setStatusBarDarkModeXiaomi(false, activity.getWindow());
                }
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    private static boolean isLightColor(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) >= 0.5;
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
    public static void setNavigationBarDefaultColor(Activity activity) {
        if (Utils.ATLEAST_LOLLIPOP) {
            setNavigationBarColor(activity, DEFAULT_NAVIGATION_BAR_COLOR);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColor(Activity activity, int color) {
        if (Utils.ATLEAST_LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(color);
        } else {
            setNavigationBarColorNative(activity, color);
        }
    }

    public static void setNavigationBarColorNative(Activity activity, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            View navigationBarView = ViewUtils.findViewById(activity, R.id.navigation_bar_bg_v);
            if (null != navigationBarView) {
                if (color == Color.TRANSPARENT) {
                    navigationBarView.setVisibility(View.GONE);
                } else {
                    int navigationBarHeight = Dimensions.getNavigationBarHeight(activity);
                    if (navigationBarHeight == 0) {
                        navigationBarView.setVisibility(View.GONE);
                    } else {
                        InsettableFrameLayout.LayoutParams layoutParams = new InsettableFrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.mInsetWay = InsettableFrameLayout.LayoutParams.InsetWay.NONE;
                        layoutParams.height = navigationBarHeight;
                        navigationBarView.setLayoutParams(layoutParams);
                        layoutParams.gravity = Gravity.BOTTOM;
                        navigationBarView.setBackgroundColor(color);
                        navigationBarView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    /**
     * Sets up transparent navigation and status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentSystemBarsForLmp(Activity activityContext) {
        if (Utils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                    .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * Sets up transparent status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentStatusBarsForLmp(Activity activityContext) {
        if (Utils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentSystemBarsForLmpNoNavigation(Activity activityContext) {
        if (Utils.ATLEAST_LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View
                    .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * @return Status bar (top bar) height. Note that this height remains fixed even when status bar is hidden.
     */
    public static int getStatusBarHeight(Context context) {
        if (null == context) {
            return 0;
        }
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }
        return height;
    }

    public static void hideSystemUi(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
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
            toolbar = container.findViewById(com.colorphone.lock.R.id.inner_tool_bar);
        } else {
            toolbar = (Toolbar) container;
        }
        assert toolbar != null;

        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        titleTextView.setId(R.id.title_text);
        if (containsBackButton) {
            setToolBarTitle(titleTextView, false);
        } else {
            setToolbarTitleWithoutLayoutParams(titleTextView);
        }
        titleTextView.setTextColor(titleColor);
        titleTextView.setText(title);
        if (tf != null) {
            titleTextView.setTypeface(tf);
        }
        toolbar.addView(titleTextView);

        toolbar.setBackgroundColor(bgColor);
        activity.setSupportActionBar(toolbar);
        if (Utils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            container.setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        if (containsBackButton) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        } else {
            // Disable app logo
//            activity.getSupportActionBar().setLogo(R.drawable.app_icon);
        }
    }

    public static void configSimpleAppBar(AppCompatActivity activity, String title, int bgColor, int logoResource) {
        View container = activity.findViewById(R.id.action_bar);
        assert container != null;

        Toolbar toolbar = null;

        toolbar = (Toolbar) container;

        assert toolbar != null;

        // Title
        toolbar.setTitle("");
        TextView titleTextView = new TextView(activity);
        setToolBarTitle(titleTextView);
        titleTextView.setText(title);
        toolbar.addView(titleTextView);
        if (logoResource > 0) {
            toolbar.setLogo(logoResource);
        }
        toolbar.setBackgroundColor(bgColor);
        activity.setSupportActionBar(toolbar);
        if (Utils.ATLEAST_LOLLIPOP) {
            //noinspection ConstantConditions
            activity.getSupportActionBar().setElevation(
                    activity.getResources().getDimensionPixelSize(R.dimen.app_bar_elevation));
        }
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public static void setToolBarTitle(TextView titleTextView) {
        setToolBarTitle(titleTextView, false);
    }

    @SuppressWarnings("deprecation")
    public static void setToolBarTitle(TextView titleTextView, boolean largeMargin) {
        setToolbarTitleWithoutLayoutParams(titleTextView);

        Toolbar.LayoutParams toolbarTitleParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.START);
        boolean isRtl = Utils.isRtl();
        int margin = largeMargin ? Dimensions.pxFromDp(20) : Dimensions.pxFromDp(16);
        //noinspection ResourceType
//        toolbarTitleParams.setMargins(isRtl ? 0 : margin, 0, isRtl ? margin : 0, 0);
        titleTextView.setPadding( margin, 0,  margin, 0);
        titleTextView.setLayoutParams(toolbarTitleParams);
    }

    public static void setToolbarTitleWithoutLayoutParams(TextView titleTextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleTextView.setTextAppearance(R.style.ToolbarTextAppearance);
        } else {
            titleTextView.setTextAppearance(HSApplication.getContext(), R.style.ToolbarTextAppearance);
        }
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setTextSize(20);
//        final Typeface typeface = FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD);
//        titleTextView.setTypeface(typeface);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return activity.isDestroyed();
        }
        return false;
    }

    public static boolean setStatusBarDarkModeXiaomi(boolean darkmode, Window window) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static WindowManager.LayoutParams getFullScreenFloatWindowParams(WindowManager mWm) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.systemUiVisibility = 1;
        layoutParams.type = getFloatWindowType();
        layoutParams.screenOrientation = 1;
        layoutParams.gravity = 48;
        layoutParams.format = 1;
        layoutParams.flags = 264072;
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.y = 0;

        boolean needFix = false;
        int l = 0;
        if (Build.VERSION.SDK_INT < 19) {
            needFix = false;
        } else {
            Display defaultDisplay = mWm.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            defaultDisplay.getRealMetrics(displayMetrics);
            int i = displayMetrics.heightPixels;
            int i2 = displayMetrics.widthPixels;
            DisplayMetrics displayMetrics2 = new DisplayMetrics();
            defaultDisplay.getMetrics(displayMetrics2);
            int i3 = displayMetrics2.heightPixels;
            int i4 = displayMetrics2.widthPixels;
            l = i - i3;
            if (i2 - i4 > 0 || i - i3 > 0) {
                needFix = true;
            }
        }

        if (needFix) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mWm.getDefaultDisplay().getMetrics(displayMetrics);
            layoutParams.y = 0;
            layoutParams.width = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            layoutParams.height = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels) + l;
            if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
                layoutParams.width += l;
                layoutParams.height -= l;
            }
            layoutParams.gravity = 48;
            layoutParams.flags = 155714048;
            layoutParams.systemUiVisibility = 5890;
        }

        // Add extra
        int systemUiVisibility = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        layoutParams.systemUiVisibility = layoutParams.systemUiVisibility | systemUiVisibility;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        return layoutParams;
    }

    protected static int getFloatWindowType() {
        int floatWindowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            floatWindowType = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            floatWindowType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            floatWindowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(HSApplication.getContext())) {
            floatWindowType = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        return floatWindowType;
    }

}

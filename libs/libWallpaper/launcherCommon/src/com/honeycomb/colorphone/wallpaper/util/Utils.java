/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.honeycomb.colorphone.wallpaper.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.honeycomb.colorphone.LauncherConstants;
import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSInstallationUtils;
import com.ihs.app.utils.HSMarketUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Commons;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Packages;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hugo.weaving.DebugLog;

/**
 * Various utilities shared amongst the Launcher's classes.
 * <p>
 * Use {@link NavUtils}, {@link WallpaperUtils}, {@link CustomizeUtils}, etc.
 * for module-specific utilities.
 */
public final class Utils {

    private static final String TAG = "Launcher.Utils";

    public static final String PREF_KEY_ICON_DECORATION_DONE = "icon_decoration_done";

    /**
     * Threshold for float decimal equality test.
     */
    public static final float EPSILON = 0.0005f;

    private static final int STREAM_OP_BUFFER_SIZE = 4096;

    private static final Canvas sCanvas = new Canvas();

    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final long DIALOG_IGNORE_BACK_KEY_DURATION = 1000;

    public static final int RETRY_FAIL_ACTION_ABORT = 0;
    public static final int RETRY_FAIL_ACTION_IGNORE = 1;

    /**
     * Defines the duration in milliseconds between the first click event and
     * the second click event for an interaction to be considered a double-click.
     */
    private static final int DOUBLE_CLICK_TIMEOUT = 500;

    private static final String PREF_KEY_UNINSTALLED_APPS = "uninstalled_apps";

    private static List<String> sUninstalledAppsCache;

    private static final Rect sOldBounds = new Rect();
    private static final DrawFilter sIconDrawFilter = new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG);

    private static final Pattern sTrimPattern = Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    private static int sColors[] = {0xffff0000, 0xff00ff00, 0xff0000ff};
    private static int sColorIndex = 0;

    private static final int[] sLoc0 = new int[2];
    private static final int[] sLoc1 = new int[2];

    public static final int LDPI_DEVICE_SCREEN_HEIGHT = 320;
    private static final long USE_DND_DURATION = 2 * DateUtils.HOUR_IN_MILLIS; // 2 hour don not disturb

    private static long sLastClickTimeForDoubleClickCheck;

    private static long sInstallTime;

    private static int sStreamVolume = -1;

    private static HashMap<String, Boolean> sLaunchAbleAppMap = new HashMap<>();

    private static Random sRandom = new Random();

    public static final boolean ATLEAST_LOLLIPOP =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static boolean isNycOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isNycMR1OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    public static boolean isLollipopOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean equals(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }

    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static long getPackageLastModifiedTime(String packageName) {
        ApplicationInfo appInfo;
        try {
            appInfo = HSApplication.getContext().getPackageManager().getApplicationInfo(packageName, 0);
        } catch (Exception e) {
            return -1;
        }
        String appFile = appInfo.sourceDir;
        return new File(appFile).lastModified();
    }

    public static int validateIndex(List<? extends Object> sizeLimit, int rawIndex) {
        return Math.max(0, Math.min(rawIndex, sizeLimit.size() - 1));
    }

    public static final int FLASHLIGHT_STATUS_FAIL = -1;
    public static final int FLASHLIGHT_STATUS_OFF = 0;
    public static final int FLASHLIGHT_STATUS_ON = 1;

    public static float celsiusToFahrenheit(float celsius) {
        return celsius * 1.8f + 32f;
    }

    public static float celsiusCoolerByToFahrenheit(float celsius) {
        return celsius * 1.8f;
    }

    public static boolean setMobileDataStatus(Context context, boolean enabled) {
        if (Compats.IS_HUAWEI_DEVICE && isWifiEnabled()) {
            return false;
        }
        ConnectivityManager connectivityManager;
        Class connectivityManagerClz;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManagerClz = connectivityManager.getClass();
            Method method = ReflectionHelper.getMethod(connectivityManagerClz, "setMobileDataEnabled", boolean.class);
            // Asynchronous invocation
            method.invoke(connectivityManager, enabled);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean getMobileDataStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        String methodName = "getMobileDataEnabled";
        Class cmClass = connectivityManager.getClass();
        Boolean isOpen;

        try {
            @SuppressWarnings("unchecked")
            Method method = ReflectionHelper.getMethod(cmClass, methodName);
            isOpen = (Boolean) method.invoke(connectivityManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return isOpen;
    }

    public static boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) HSApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean hasUpdate() {
        int latestVersionCode = getLatestVersionCode();
        return HSApplication.getCurrentLaunchInfo().appVersionCode < latestVersionCode;
    }

    public static int getLatestVersionCode() {
        return HSConfig.optInteger(10000000, "Application", "Update", "LatestVersionCode");
    }

    /**
     * Whether this launcher is set as default home screen. Defaults to {@code true} if error occurs.
     */
    @DebugLog
    public static boolean isDefaultLauncher() {
        String defaultLauncherPackage = getDefaultLauncher();
        return TextUtils.equals(HSApplication.getContext().getPackageName(), defaultLauncherPackage);
    }

    /**
     * @return Package name of current default launcher.
     */
    public @NonNull
    static String getDefaultLauncher() {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo;
        try {
            resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        } catch (Exception e) {
            return "";
        }
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            String packageName = resolveInfo.activityInfo.packageName;
            return packageName == null ? "" : packageName;
        }
        return "";
    }

    public static boolean isSpecialApp(String[] keywords, String packageName) {
        for (String keyword : keywords) {
            if (packageName.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBrowserApp(String packageName) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
        List<ResolveInfo> resolveInfos;
        try {
            resolveInfos = HSApplication.getContext().getPackageManager()
                    .queryIntentActivities(browserIntent, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
            return false;
        }
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (TextUtils.equals(packageName, resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether we are in new user "Do Not Disturb" status.
     */
    public static boolean isNewUserInDNDStatus() {
        long currentTimeMills = System.currentTimeMillis();
        return currentTimeMills - getAppInstallTimeMillis() < USE_DND_DURATION;
    }

    public interface DefaultLauncherQueryCallback {
        void onDefaultLauncherQueryResult(boolean isDefaultLauncher);
    }

    public static void isDefaultLauncher(final DefaultLauncherQueryCallback callback) {
        if (callback == null) {
            return;
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final boolean isDefaultLauncher = isDefaultLauncher();
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDefaultLauncherQueryResult(isDefaultLauncher);
                    }
                });
            }
        });
    }

    public static List<String> getInstalledLaunchers() {
        List<String> launcherNames = new ArrayList<>();
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo info : list) {
            if (!validateLauncher(info)) {
                continue;
            }
            launcherNames.add(info.activityInfo.packageName);
        }
        return launcherNames;
    }

    private static boolean validateLauncher(ResolveInfo resolveInfo) {
        // Exclude AOSP Settings app FallbackHome activity since 7.0
        return !"com.android.settings.FallbackHome".equals(resolveInfo.activityInfo.name);
    }

    public static void configTabLayoutText(final TabLayout tabLayout, final Typeface typeface, final float textSize) {
        setTypefaceRecursive(tabLayout, typeface);

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildrenCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildrenCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTextSize(textSize);
                }
            }
        }
    }

    public static void setTypefaceRecursive(View root, Typeface typeface) {
        if (!(root instanceof ViewGroup)) {
            if (root instanceof TextView) {
                ((TextView) root).setTypeface(typeface);
            }
            return;
        }
        int childCount = ((ViewGroup) root).getChildCount();
        for (int i = 0; i < childCount; i++) {
            setTypefaceRecursive(((ViewGroup) root).getChildAt(i), typeface);
        }
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Get epoch time when user installed us. You should always call this method rather than reading
     * LauncherConstants.PREF_KEY_INSTALLED_TIME yourself to get better estimation for upgraded users.
     */
    public static long getAppInstallTimeMillis() {
        if (sInstallTime <= 0) {
            Preferences prefs = Preferences.getDefault();
            if ((sInstallTime = prefs.getLong(LauncherConstants.PREF_KEY_INSTALLED_TIME, 0)) == 0) {
                sInstallTime = System.currentTimeMillis();
                prefs.putLong(LauncherConstants.PREF_KEY_INSTALLED_TIME, sInstallTime);
            }
        }
        return sInstallTime;
    }

    public static boolean isPackageEverInstalled(String pkgName) {
        if (isPackageInstalled(pkgName)) {
            return true;
        }
        List<String> uninstalledApps;
        if (sUninstalledAppsCache != null) {
            uninstalledApps = sUninstalledAppsCache;
        } else {
            uninstalledApps = Preferences.getDefault().getStringList(PREF_KEY_UNINSTALLED_APPS);
            sUninstalledAppsCache = uninstalledApps;
        }
        return uninstalledApps.contains(pkgName);
    }

    public static boolean isPackageInstalled(String pkgName) {

        // Normal path, makes an actual call to system server
        return Packages.isPackageInstalled(pkgName);
    }

    public static void recordPackageUninstall(String pkgName) {
        Preferences.getDefault().addStringToList(PREF_KEY_UNINSTALLED_APPS, pkgName);
        if (sUninstalledAppsCache != null) {
            sUninstalledAppsCache.add(pkgName);
        }
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant        The descendant to which the passed coordinate is relative.
     * @param root              The root view to make the coordinates relative to.
     * @param outCoord          The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *                          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     * this scale factor is assumed to be equal in X and Y, and so if at any point this
     * assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToParent(
            View descendant, View root, int[] outCoord, boolean includeRootScroll) {
        ArrayList<View> ancestorChain = new ArrayList<>();

        float[] pt = {outCoord[0], outCoord[1]};

        View v = descendant;
        while (v != root && v != null) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        int count = ancestorChain.size();
        for (int i = 0; i < count; i++) {
            View v0 = ancestorChain.get(i);
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v0 != descendant || includeRootScroll) {
                pt[0] -= v0.getScrollX();
                pt[1] -= v0.getScrollY();
            }

            v0.getMatrix().mapPoints(pt);
            pt[0] += v0.getLeft();
            pt[1] += v0.getTop();
            scale *= v0.getScaleX();
        }

        outCoord[0] = Math.round(pt[0]);
        outCoord[1] = Math.round(pt[1]);
        return scale;
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    public static void scaleRect(Rect r, float scale) {
        if (scale != 1.0f) {
            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);
        }
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        int cx = r.centerX();
        int cy = r.centerY();
        r.offset(-cx, -cy);
        Utils.scaleRect(r, scale);
        r.offset(cx, cy);
    }

    public static int mirrorIndexIfRtl(boolean isRtl, int total, int ltrIndex) {
        if (isRtl) {
            return total - ltrIndex - 1;
        } else {
            return ltrIndex;
        }
    }

    public static boolean isSystemApp(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = intent.getComponent();
        String packageName = null;
        if (cn == null) {
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if ((info != null) && (info.activityInfo != null)) {
                packageName = info.activityInfo.packageName;
            }
        } else {
            packageName = cn.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isSystemApp(ApplicationInfo appInfo) {
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    /**
     * @return Default to {@code false} on error.
     */
    public static boolean isSystemApp(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        if (packageName.contains("com.google") || packageName.contains("com.android") || packageName.contains("android.process")) {
            return true;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
            return null != applicationInfo && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isLaunchAbleApp(Context context, String packageName) {
        if (sLaunchAbleAppMap.containsKey(packageName)) {
            return sLaunchAbleAppMap.get(packageName);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        boolean result = null != context.getPackageManager().resolveActivity(intent, 0);
        sLaunchAbleAppMap.put(packageName, result);
        return result;
    }

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     *
     * @param bitmap  The bitmap to scan
     * @param samples The approximate max number of samples to use.
     */
    public static int findDominantColorByHue(Bitmap bitmap, int samples) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int sampleStride = (int) Math.sqrt((height * width) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }

        // This is an out-param, for getting the hsv values for an rgb
        float[] hsv = new float[3];

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        float[] hueScoreHistogram = new float[360];
        float highScore = -1;
        int bestHue = -1;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & (argb >> 24);
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue;
                }
                // Remove the alpha channel.
                int rgb = argb | 0xFF000000;
                Color.colorToHSV(rgb, hsv);
                // Bucket colors by the 360 integer hues.
                int hue = (int) hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) {
                    // Defensively avoid array bounds violations.
                    continue;
                }
                float score = hsv[1] * hsv[2];
                hueScoreHistogram[hue] += score;
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue];
                    bestHue = hue;
                }
            }
        }

        SparseArray<Float> rgbScores = new SparseArray<>();
        int bestColor = 0xff000000;
        highScore = -1;
        // Go back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int rgb = bitmap.getPixel(x, y) | 0xff000000;
                Color.colorToHSV(rgb, hsv);
                int hue = (int) hsv[0];
                if (hue == bestHue) {
                    float s = hsv[1];
                    float v = hsv[2];
                    int bucket = (int) (s * 100) + (int) (v * 10000);
                    // Score by cumulative saturation * value.
                    float score = s * v;
                    Float oldTotal = rgbScores.get(bucket);
                    float newTotal = oldTotal == null ? score : oldTotal + score;
                    rgbScores.put(bucket, newTotal);
                    if (newTotal > highScore) {
                        highScore = newTotal;
                        // All the colors in the winning bucket are very similar. Last in wins.
                        bestColor = rgb;
                    }
                }
            }
        }
        return bestColor;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isViewAttachedToWindow(View v) {
        if (CommonUtils.ATLEAST_KITKAT) {
            return v.isAttachedToWindow();
        } else {
            // A proxy call which returns null, if the view is not attached to the window.
            return v.getKeyDispatcherState() != null;
        }
    }

    /**
     * Compresses the bitmap to a byte array for serialization.
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            // ArrayIndexOutOfBoundsException may be thrown during byte array copy
            HSLog.w(TAG, "Could not write bitmap");
            return null;
        }
    }

    /*
     * Finds a system apk which had a broadcast receiver listening to a particular action.
     * @param action intent action used to find the apk
     * @return a pair of apk package name and the resources.
     */
    public static Pair<String, Resources> findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null &&
                    (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                final String packageName = info.activityInfo.packageName;
                try {
                    final Resources res = pm.getResourcesForApplication(packageName);
                    return Pair.create(packageName, res);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Failed to find resources for " + packageName);
                }
            }
        }
        return null;
    }


    /**
     * Find the first vacant cell, if there is one.
     *
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX  Horizontal cell span.
     * @param spanY  Vertical cell span.
     * @return true if a vacant cell was found
     */
    public static boolean findVacantCell(int[] vacant, int spanX, int spanY, int xCount, int yCount, boolean[][] occupied) {
        for (int y = 0; (y + spanY) <= yCount; y++) {
            for (int x = 0; (x + spanX) <= xCount; x++) {
                boolean available = !occupied[x][y];
                out:
                for (int i = x; i < x + spanX; i++) {
                    for (int j = y; j < y + spanY; j++) {
                        available = available && !occupied[i][j];
                        if (!available)
                            break out;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }

        // Just strip any sequence of whitespace or java space characters from the beginning and end
        Matcher m;
        try {
            m = sTrimPattern.matcher(s);
        } catch (IllegalArgumentException e) {
            return s.toString();
        }
        return m.replaceAll("$1");
    }

    /**
     * Convenience println with multiple args.
     */
    public static void println(String key, Object... args) {
        StringBuilder b = new StringBuilder();
        b.append(key);
        b.append(": ");
        boolean isFirstArgument = true;
        for (Object arg : args) {
            if (isFirstArgument) {
                isFirstArgument = false;
            } else {
                b.append(", ");
            }
            b.append(arg);
        }
        System.out.println(b.toString());
    }

    public static float dpiFromPx(int size) {
        return (size / Dimensions.getDensityRatio());
    }

    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, metrics));
    }

    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, metrics));
    }

    public static String createDbSelectionQuery(String columnName, Iterable<?> values) {
        return String.format(Locale.ENGLISH, "%s IN (%s)", columnName, TextUtils.join(", ", values));
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Height with status bar but not navigation bar
     */
    public static Point getScreenSize(Activity launcher) {
        Display display = launcher.getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        return screenSize;
    }

    public static float getPhysicalScreenHeight(Activity launcher) {
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            launcher.getWindowManager().getDefaultDisplay().getRealSize(point);
        } else {
            launcher.getWindowManager().getDefaultDisplay().getSize(point);
        }

        DisplayMetrics dm = launcher.getResources().getDisplayMetrics();
        return point.y / dm.ydpi;
    }

    public static int getPhysicalScreenHeight(Context context) {
        Point point = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }

//        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return point.y;
    }

    public static void showNavigationBarMenuButton(Activity activity) {
        if (!Dimensions.hasNavBar(activity))
            return;
        int menuFlag;
        try {
            Field f = ReflectionHelper.getField(WindowManager.LayoutParams.class, "FLAG_NEEDS_MENU_KEY");
            menuFlag = f.getInt(null);
            Window window = activity.getWindow();
            window.addFlags(menuFlag);
            return;
        } catch (Exception ignored) {
        }

        try {
            Field menuFlagField = ReflectionHelper.getField(WindowManager.LayoutParams.class, "NEEDS_MENU_SET_TRUE");
            menuFlag = menuFlagField.getInt(null);
            Method method = ReflectionHelper.getDeclaredMethod(Window.class, "setNeedsMenuKey", int.class);
            method.setAccessible(true);
            method.invoke(activity.getWindow(), menuFlag);
        } catch (Exception ignored) {
        }
    }

    public static void hideNavigationBarMenuButton(Activity activity) {
        if (!Dimensions.hasNavBar(activity))
            return;
        int menuFlag;
        try {
            Field f = ReflectionHelper.getField(WindowManager.LayoutParams.class, "FLAG_NEEDS_MENU_KEY");
            menuFlag = f.getInt(null);
            Window window = activity.getWindow();
            window.clearFlags(menuFlag);
            return;
        } catch (Exception ignored) {
        }
        try {
            Field menuFlagField = WindowManager.LayoutParams.class.getField("NEEDS_MENU_SET_FALSE");
            menuFlag = menuFlagField.getInt(null);
            Method method = ReflectionHelper.getDeclaredMethod(Window.class, "setNeedsMenuKey", int.class);
            method.setAccessible(true);
            method.invoke(activity.getWindow(), menuFlag);
        } catch (Exception ignored) {
        }
    }

    public static byte[] readFile(File file) {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[STREAM_OP_BUFFER_SIZE];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } catch (IOException e) {
            return new byte[0];
        } finally {
            try {
                if (ous != null) {
                    ous.close();
                }
            } catch (IOException ignored) {
            }
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException ignored) {
            }
        }
        return ous.toByteArray();
    }

    public static void writeToFile(File file, byte[] data) {
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    HSLog.d(TAG, "Create file " + file.getAbsolutePath());
                }
            }
            fos = new FileOutputStream(file);
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static void saveBitmapToFile(Bitmap bitmap, String fileOutPath, int quality) {
        try {
            saveBitmapToFileInternal(bitmap, new FileOutputStream(fileOutPath), quality);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmapToFile(Bitmap bitmap, File fileOutPath, int quality) {
        try {
            saveBitmapToFileInternal(bitmap, new FileOutputStream(fileOutPath), quality);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveBitmapToFileInternal(Bitmap bitmap, FileOutputStream fos, int quality) {
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean saveInputStreamToFile(byte[] preData, InputStream is, File fileOut) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(fileOut);
            if (null != preData) {
                output.write(preData);
            }

            byte[] buffer = new byte[STREAM_OP_BUFFER_SIZE];
            int read;

            while ((read = is.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ignored) {
            }
        }
        return true;
    }

    /**
     * Retrieve, creating if needed, a new sub-directory in cache directory.
     * Internal cache directory is used if external cache directory is not available.
     */
    public static File getCacheDirectory(String subDirectory) {
        return getCacheDirectory(subDirectory, false);
    }

    /**
     * @param useInternal Only uses internal cache directory when {@code true}.
     */
    public static File getCacheDirectory(String subDirectory, boolean useInternal) {
        Context context = HSApplication.getContext();
        String cacheDirPath;
        File externalCache = null;
        if (!useInternal) {
            try {
                externalCache = context.getExternalCacheDir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (externalCache != null) {
            cacheDirPath = externalCache.getAbsolutePath() + File.separator + subDirectory + File.separator;
        } else {
            cacheDirPath = context.getCacheDir().getAbsolutePath() + File.separator + subDirectory + File.separator;
        }
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                HSLog.d("Utils.Cache", "Created cache directory: " + cacheDir.getAbsolutePath());
            } else {
                HSLog.e("Utils.Cache", "Failed to create cache directory: " + cacheDir.getAbsolutePath());
            }
        }
        return cacheDir;
    }

    public static void copyFile(File src, File dst) throws IOException {
        if (!src.exists()) {
            return;
        }
        if (dst.exists()) {
            boolean removed = dst.delete();
            if (removed) HSLog.d(TAG, "Replacing file " + dst);
        }
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            outChannel.close();
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[STREAM_OP_BUFFER_SIZE];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } finally {
            in.close();
            out.close();
        }
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        boolean success = fileOrDirectory.delete();
        HSLog.v("Launcher.Files", "Delete " + (fileOrDirectory.isDirectory() ? "directory " : "file ")
                + fileOrDirectory.getName() + ", success: " + success);
    }

    private static long getSelfMemoryUsed() {
        long memSize = 0;
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAPP : runningApps) {
            if (HSApplication.getContext().getPackageName().equals(runningAPP.processName)) {
                int[] pids = new int[]{runningAPP.pid};
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);
                memSize = memoryInfo[0].getTotalPss() * 1024;
                break;
            }
        }
        return memSize;
    }

    public static String getRemoteFileExtension(String url) {
        String extension = "";
        if (url != null) {
            int i = url.lastIndexOf('.');
            int p = Math.max(url.lastIndexOf('/'), url.lastIndexOf('\\'));
            if (i > p) {
                extension = url.substring(i + 1);
            }
        }
        return extension;
    }

    /**
     * @return {@code n} unique integers in range [start, end). Or {@code null} when end - start < n
     * or end <= start. Note that this implementation is for "dense" params, where end - start is no
     * larger than a reasonable size of array list allocation, and where n takes a substantial
     * portion of the range.
     */
    public static int[] getUniqueRandomInts(int start, int end, int n) {
        if (n > end - start || end <= start) {
            return null;
        }
        List<Integer> numberList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            numberList.add(i);
        }
        Collections.shuffle(numberList);
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = numberList.get(i);
        }
        return result;
    }

    public static float formatNumberOneDigit(double number) {
        return (float) (Math.round(number * 10)) / 10;
    }

    public static double formatNumberTwoDigit(double number) {
        BigDecimal bg = new BigDecimal(number);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @MainThread
    public static boolean checkDoubleClickGlobal() {
        long time = SystemClock.elapsedRealtime();
        long timeD = time - sLastClickTimeForDoubleClickCheck;
        if (0 < timeD && timeD < DOUBLE_CLICK_TIMEOUT) {
            return true;
        }
        sLastClickTimeForDoubleClickCheck = time;
        return false;
    }

    public static String getAppLabel(String packageName) {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (Exception ignored) {
        }
        return (String) ((null != applicationInfo) ? packageManager.getApplicationLabel(applicationInfo) : "");
    }

    public static boolean isNewUser() {
        return HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode;
    }

    public static boolean isNewUserByVersionName() {
        return TextUtils.equals(HSApplication.getCurrentLaunchInfo().appVersionName,
                HSApplication.getFirstLaunchInfo().appVersionName);
    }

    public static boolean checkFileValid(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }

    public static boolean isTouchInView(View view, MotionEvent event) {
        if (null == view) {
            return false;
        }

        Rect rect = new Rect();
        view.getDrawingRect(rect);

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        RectF viewRectF = new RectF(rect);
        viewRectF.offset(location[0], location[1]);
        return viewRectF.contains(event.getRawX(), event.getRawY());
    }

    public static float formatNumberToOneDigit(double number) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.0");
        double result = number;
        try {
            result = Double.parseDouble(df.format(number));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return (float) result;
    }

    public static boolean isStringNumber(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57)
                return false;
        }
        return true;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public static boolean isKeyguardLocked(Context context, boolean defaultValue) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        try {
            Method declaredMethod = ReflectionHelper.getDeclaredMethod(KeyguardManager.class, "isKeyguardLocked");
            declaredMethod.setAccessible(true);
            defaultValue = (Boolean) declaredMethod.invoke(keyguardManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        return defaultValue;
    }

    public static Notification buildNotificationSafely(NotificationCompat.Builder builder) {
        try {
            return builder.build();
        } catch (Exception e) {
            HSLog.e(TAG, "Error building notification: " + builder + ", exception: " + e);
            return null;
        }
    }

    public static void handleBinderSizeError(Exception e) {
        if (Commons.isBinderSizeError(e)) {
            e.printStackTrace();
        } else {
            throw new RuntimeException("Unexpected exception that is not a binder size error", e);
        }
    }

    public static Object callWithRetry(Callable<Object> action, int totalTryCount, long interval, int failAction) {
        for (int i = 0; i < totalTryCount; i++) {
            try {
                return action.call();
            } catch (Throwable e) {
                HSLog.d(TAG, "Failed, try #" + i);
            }
            if (interval > 0) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        switch (failAction) {
            case RETRY_FAIL_ACTION_ABORT:
                HSLog.e(TAG, "Failed after " + totalTryCount + " retries, abort");
                System.exit(-1);
                break;
            case RETRY_FAIL_ACTION_IGNORE:
                HSLog.w(TAG, "Failed after " + totalTryCount + " retries, ignore");
                break;
        }
        return null;
    }

    public static boolean isEn() {
        String lg = Locale.getDefault().getLanguage();
        return lg.startsWith("en");
    }

    public static boolean isRu() {
        String lg = Locale.getDefault().getLanguage();
        return lg.startsWith("ru");
    }

    public static boolean isIntentExist(Context context, Intent intent) {
        if (intent == null) {
            return false;
        }
        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
            return false;
        }
        return true;
    }

    public static boolean hasPermission(String permission) {
        boolean granted = false;
        if (!TextUtils.isEmpty(permission)) {
            try {
                granted = ContextCompat.checkSelfPermission(HSApplication.getContext(), permission)
                        == PackageManager.PERMISSION_GRANTED;
            } catch (RuntimeException e) {
            }
        }
        return granted;
    }

    public static int getPixelInsetTop(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    /**
     * It's guaranteed that exactly one of {@link ResolveInfo#activityInfo},
     * {@link ResolveInfo#serviceInfo}, or {@link ResolveInfo#providerInfo} will be non-null.
     *
     * @return The only non-null {@link ComponentInfo} in the {@link ResolveInfo}. Or {@code null}.
     */
    public static ComponentInfo getComponentInfo(ResolveInfo resolveInfo) {
        if (resolveInfo.activityInfo != null) {
            return resolveInfo.activityInfo;
        }
        if (resolveInfo.serviceInfo != null) {
            return resolveInfo.serviceInfo;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && resolveInfo.providerInfo != null) {
            return resolveInfo.providerInfo;
        }
        return null;
    }

    public static void setRingerModeOrShowErrorToast(int ringerMode) {
        Context context = HSApplication.getContext();
        try {
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                try {
                    audioManager.setRingerMode(ringerMode);
                } catch (SecurityException e) {
                    Toasts.showToast(R.string.setting_device_not_support_message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mute() {
        try {
            AudioManager audioManager = (AudioManager) HSApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (streamVolume != 0) {
                sStreamVolume = streamVolume;
            }
            audioManager.setStreamVolume(3, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resumeVolume() {
        try {
            if (sStreamVolume != -1) {
                ((AudioManager) HSApplication.getContext().getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(3, sStreamVolume, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getRandomInt(int bound) {
        return sRandom.nextInt(bound);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl() {
        Resources res = HSApplication.getContext().getResources();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    private static String getMarketAppUrl(String marketName) {
        String url = null;
        url = HSConfig.getString("libCommons", "Market", "Markets", marketName, "AppUrl");
        return url;
    }

    public static boolean shouldShowFiveStarTip() {
        try {
            if (!HSInstallationUtils.isGooglePlayInstalled()) {
                return false;
            }
            Intent intent = null;
            Uri uri;
            String defaultMarket = HSMarketUtils.getDefaultMarket();
            uri = Uri.parse(getMarketAppUrl(defaultMarket) + HSApplication.getContext().getPackageName());
            intent = new Intent("android.intent.action.VIEW");
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PackageManager packageManager = HSApplication.getContext().getPackageManager();
            ResolveInfo resolveInfo;
            try {
                resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            } catch (Exception e) {
                return false;
            }
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                String packageName = resolveInfo.activityInfo.packageName;
                return "com.android.vending".equals(packageName);
            }
            return false;
        } catch (Exception e) {
        }
        return false;
    }
}

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

package com.honeycomb.colorphone;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.acb.call.themes.Type;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final String TAG = "Utils";

    public static final boolean ATLEAST_LOLLIPOP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    public static final boolean ATLEAST_JELLY_BEAN = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public static final int DEFAULT_DEVICE_SCREEN_WIDTH = 1080;
    public static final int DEFAULT_DEVICE_SCREEN_HEIGHT = 1920;
    private static final Pattern sTrimPattern = Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");


    private static float sDensityRatio;

    public static Type getTypeByThemeId(int id) {
        for (Type t : Type.values()) {
            if (t.getValue() == id) {
                return t;
            }
        }
        return null;
    }

    public static float getDensityRatio() {
        if (sDensityRatio > 0f) {
            return sDensityRatio;
        }
        Resources resources = HSApplication.getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        sDensityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return sDensityRatio;
    }

    public static int pxFromDp(float dp) {
        return Math.round(dp * getDensityRatio());
    }

    public static boolean mayDisturbUserAtThisTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return 6 <= hourOfDay && hourOfDay < 23;
    }

    public static boolean getMobileDataStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        String methodName = "getMobileDataEnabled";
        Class cmClass = connectivityManager.getClass();
        Boolean isOpen;

        try {
            @SuppressWarnings("unchecked")
            Method method = cmClass.getMethod(methodName);
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

    public static boolean setMobileDataStatus(Context context, boolean enabled) {
        if (isHuaweiDevice() && isWifiEnabled()) {
            return false;
        }
        ConnectivityManager connectivityManager;
        Class connectivityManagerClz;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManagerClz = connectivityManager.getClass();
            @SuppressWarnings("unchecked")
            Method method = connectivityManagerClz.getMethod("setMobileDataEnabled", boolean.class);
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

    public static boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) HSApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean isHuaweiDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase("Huawei")
                && !Build.BRAND.equalsIgnoreCase("google"); // Exclude Nexus 6P
    }

    /**
     * 返回手机屏幕高度
     */
    public static int getPhoneHeight(Context context) {
        if (null == context) {
            return DEFAULT_DEVICE_SCREEN_HEIGHT;
        }
        int height = context.getResources().getDisplayMetrics().heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            Point localPoint = new Point();
            windowManager.getDefaultDisplay().getRealSize(localPoint);
            HSLog.v(TAG, "height == " + height + ", w == " + localPoint.x + ", h == " + localPoint.y);
            if (localPoint.y > height) {
                height = localPoint.y;
            }
        } else {
            int navigationBarHeight = getNavigationBarHeight(context);
            HSLog.v(TAG, "Layout h == " + height + ", navigationBarHeight == " + navigationBarHeight);
            if (navigationBarHeight != 0 && height % 10 != 0) {
                if ((height + navigationBarHeight) % 10 == 0) {
                    height = (height + navigationBarHeight);
                }
            }
            HSLog.v(TAG, "height == " + height + ", navigationBarHeight == " + navigationBarHeight);
        }

        return height;
    }

    public static int getPhoneWidth(Context context) {
        if (null == context) {
            return DEFAULT_DEVICE_SCREEN_WIDTH;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return Math.min(dm.widthPixels, dm.heightPixels);
    }

    public static int getNavigationBarHeight(Context context) {
        if (null == context) {
            return 0;
        }
        if (context instanceof Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Activity activityContext = (Activity) context;
            DisplayMetrics metrics = new DisplayMetrics();
            activityContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activityContext.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight) {
                return realHeight - usableHeight;
            } else {
                return 0;
            }
        }
        Resources localResources = context.getResources();
        if (!hasNavBar(context)) {
            HSLog.i("no navbar");
            return 0;
        }
        int i = localResources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (i > 0) {
            return localResources.getDimensionPixelSize(i);
        }
        i = localResources.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        if (i > 0) {
            return localResources.getDimensionPixelSize(i);
        }
        return 0;
    }

    public static boolean hasNavBar(Context paramContext) {
        boolean bool = true;
        String sNavBarOverride;
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                Object localObject = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String.class);
                ((Method) localObject).setAccessible(true);
                sNavBarOverride = (String) ((Method) localObject).invoke(null, "qemu.hw.mainkeys");
                localObject = paramContext.getResources();
                int i = ((Resources) localObject).getIdentifier("config_showNavigationBar", "bool", "android");
                if (i != 0) {
                    bool = ((Resources) localObject).getBoolean(i);
                    if ("1".equals(sNavBarOverride)) {
                        bool = false;
                        return bool;
                    }
                }
            } catch (Throwable localThrowable) {
            }
            if (!ViewConfiguration.get(paramContext).hasPermanentMenuKey()) {
                HSLog.e("hasPermanentMenuKey true");
                return bool;
            }
        }
        bool = false;
        return bool;
    }

    public static Drawable getAppIcon(String packageName) {
        Drawable icon = HSApplication.getContext().getResources().getDrawable(R.mipmap.ic_launcher);
        try {
            icon = HSApplication.getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (Exception ignored) {
        }
        return icon;
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

    @SuppressLint("NewApi")
    public static boolean isFloatWindowAllowed(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    private static volatile long lastClickTime;

    // 通过设定时间间隔来避免某些按钮的重复点击
    public static boolean isFastDoubleClick() {
        long time = SystemClock.elapsedRealtime();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void showWhenLocked(Activity context) {
        boolean keyguardFlag;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            keyguardFlag = false;
        } else {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager == null) {
                keyguardFlag = false;
            } else {
                keyguardFlag = keyguardManager.isKeyguardSecure();
                HSLog.i("isKeyguardSecure: " + keyguardManager.isKeyguardSecure()
                        + " isKeyguardLocked: " + keyguardManager.isKeyguardLocked());
            }
        }

        Window window = context.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (!keyguardFlag) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
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
        ArrayList<View> ancestorChain = new ArrayList<View>();

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

    public static
    @NonNull
    String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : getRunningProcesses(activityManager)) {
            if (appProcess.pid == pid) {
                return appProcess.processName == null ? "" : appProcess.processName;
            }
        }
        return "";
    }

    private static List<ActivityManager.RunningAppProcessInfo> getRunningProcesses(ActivityManager am) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            runningAppProcesses = new ArrayList<>(0);
        }
        return runningAppProcesses;
    }

    public static boolean doLimitedTimes(@NonNull Runnable action, String token, int limitedTimes) {
        int haveDone = HSPreferenceHelper.getDefault().getInt(token, 0);
        if (haveDone < limitedTimes) {
            HSPreferenceHelper.getDefault().putInt(token, ++haveDone);
            action.run();
            return true;
        }
        return false;
    }

    public static boolean isFlavors(String flavor) {
        return TextUtils.equals(BuildConfig.FLAVOR, flavor);
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
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
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

    /**
     * Check if network of given type is currently available.
     *
     * @param type one of {@link ConnectivityManager#TYPE_MOBILE}, {@link ConnectivityManager#TYPE_WIFI},
     *             {@link ConnectivityManager#TYPE_WIMAX}, {@link ConnectivityManager#TYPE_ETHERNET},
     *             {@link ConnectivityManager#TYPE_BLUETOOTH}, or other types defined by {@link ConnectivityManager}.
     *             Pass -1 for ANY type
     */
    public static boolean isNetworkAvailable(int type) {
        Context context = HSApplication.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return isNetworkAvailableLollipop(cm, type);
        } else {
            return isNetworkAvailableJellyBean(cm, type);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isNetworkAvailableLollipop(ConnectivityManager cm, int type) {
        try {
            Network[] networks = cm.getAllNetworks();
            if (networks != null) {
                for (Network network : networks) {
                    NetworkInfo networkInfo = cm.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.getState() != null && isTypeMatchAndConnected(networkInfo, type)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static boolean isNetworkAvailableJellyBean(ConnectivityManager cm, int type) {
        try {
            NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
            if (networkInfos != null) {
                for (NetworkInfo networkInfo : networkInfos) {
                    if (networkInfo.getState() != null && isTypeMatchAndConnected(networkInfo, type)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isTypeMatchAndConnected(@NonNull NetworkInfo networkInfo, int type) {
        return (type == -1 || networkInfo.getType() == type) && networkInfo.isConnected();
    }


    /**
     * Retrieve, creating if needed, a new directory of given name in which we
     * can place our own custom data files.
     */
    public static @Nullable File getDirectory(String dirPath) {
        File file = HSApplication.getContext().getFilesDir();
        String[] path = dirPath.split(File.separator);
        for (String dir : path) {
            file = new File(file, dir);
            if (!file.exists() && !file.mkdir()) {
                HSLog.w(TAG, "Error making directory");
                return null;
            }
        }
        return file;
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

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
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

    public static void configActivityStatusBar(AppCompatActivity activity, Toolbar toolbar) {
        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

        activity.setSupportActionBar(toolbar);
        final Drawable upArrow = ContextCompat.getDrawable(activity, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        activity.getSupportActionBar().setHomeAsUpIndicator(upArrow);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        applyFontForToolbarTitle(activity, toolbar);
    }

    public static void applyFontForToolbarTitle(Activity context, Toolbar toolbar){
        for(int i = 0; i < toolbar.getChildCount(); i++){
            View view = toolbar.getChildAt(i);
            if(view instanceof TextView){
                TextView tv = (TextView) view;
                Typeface typeface = FontUtils.getTypeface(FontUtils.Font.ofFontResId(R.string.proxima_nova_bold), 0);
                if(tv.getText().equals(toolbar.getTitle())){
                    tv.setTypeface(typeface);
                    break;
                }
            }
        }
    }

}

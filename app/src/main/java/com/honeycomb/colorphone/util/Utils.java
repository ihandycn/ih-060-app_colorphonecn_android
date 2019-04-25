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

package com.honeycomb.colorphone.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.colorphone.lock.ReflectionHelper;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.Navigations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
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
    public static final boolean ATLEAST_JB_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final int DEFAULT_DEVICE_SCREEN_WIDTH = 1080;
    public static final int DEFAULT_DEVICE_SCREEN_HEIGHT = 1920;
    private static final Pattern sTrimPattern = Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");
    private static final float THUMBNAIL_RATIO = 0.4f;
    private static final long USE_DND_DURATION = 2 * DateUtils.HOUR_IN_MILLIS; // 2 hour don not disturb

    private static float sDensityRatio;


    private static int THUMBNAIL_HEIGHT = 0;
    private static int THUMBNAIL_WIDTH = 0;

    public static int localThemeId = 8;
    private static int sPhoneWidth;

    public static void startActivitySafely(Context context, Intent intent) {
        try {
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException | NullPointerException e) {
            HSLog.e("StartActivity", "Cannot start activity: " + intent);
        }
    }

    public static int[] getThumbnailImageSize() {
        if (THUMBNAIL_HEIGHT == 0) {
            THUMBNAIL_WIDTH = (int) (Dimensions.getPhoneWidth(HSApplication.getContext()) * THUMBNAIL_RATIO);
            THUMBNAIL_HEIGHT = THUMBNAIL_WIDTH * 1920 / 1080;
        }
        return new int[]{THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT};
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


    public static void sentEmail(Context mContext, @NonNull String[] addresses, String subject, String body) {
        if (addresses.length == 0 || TextUtils.isEmpty(addresses[0])) {
            return;
        }
        try {
            Intent sendIntentGmail = new Intent(Intent.ACTION_VIEW);
            sendIntentGmail.setType("plain/text");
            sendIntentGmail.setData(Uri.parse(TextUtils.join(",", addresses)));
            sendIntentGmail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, addresses);
            if (subject != null) sendIntentGmail.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (body != null) sendIntentGmail.putExtra(Intent.EXTRA_TEXT, body);
            mContext.startActivity(sendIntentGmail);
        } catch (Exception e) {
            //When Gmail App is not installed or disable
            Intent sendIntentIfGmailFail = new Intent(Intent.ACTION_SENDTO);
            sendIntentIfGmailFail.setData(Uri.parse("mailto:")); // only email apps should handle this
            sendIntentIfGmailFail.putExtra(Intent.EXTRA_EMAIL, addresses);
            if (subject != null) sendIntentIfGmailFail.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (body != null) sendIntentIfGmailFail.putExtra(Intent.EXTRA_TEXT, body);
            if (sendIntentIfGmailFail.resolveActivity(mContext.getPackageManager()) != null) {
                Navigations.startActivitySafely(mContext, sendIntentIfGmailFail);
            }
        }
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

        if (sPhoneWidth <= 0) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            sPhoneWidth = Math.min(dm.widthPixels, dm.heightPixels);
        }
        return sPhoneWidth;
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

    public static Drawable getAppIcon(String packageName) {
        Drawable icon = HSApplication.getContext().getResources().getDrawable(R.drawable.ic_launcher);
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

    public static File getRingtoneFile() {
        if (isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_RINGTONES), "color-phone");
            if (!file.exists() && !file.mkdirs()) {
                Log.e("Ringtone File", "Directory not created");
            }
            return file;
        } else {
            return new File(getDirectory("color-phone"), "ringtone");
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
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

    public static void copyAssetFileTo(Context context, String assetFileName, File targetFile) throws IOException {
        if(context != null) {
            try (InputStream myInput = context.getAssets().open(assetFileName);
                 FileOutputStream myOutput = new FileOutputStream(targetFile)) {
                byte[] ignore = new byte[1024];

                for (int length = myInput.read(ignore); length > 0; length = myInput.read(ignore)) {
                    myOutput.write(ignore, 0, length);
                }
            }
        }
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

    public static boolean checkFileValid(File file) {
        if (file != null && file.exists()) {
            return true;
        }
        return false;
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
        configActivityStatusBar(activity, toolbar, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
    }

    public static void configActivityStatusBar(AppCompatActivity activity, Toolbar toolbar, int back_dark) {
        configActivityStatusBar(activity, toolbar, back_dark, 24);
    }

    public static void configActivityStatusBar(AppCompatActivity activity, Toolbar toolbar, int upDrawable, int textSize) {
        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));

        activity.setSupportActionBar(toolbar);
        if (upDrawable != 0) {
            final Drawable upArrow = ContextCompat.getDrawable(activity, upDrawable);
            upArrow.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
            activity.getSupportActionBar().setHomeAsUpIndicator(upArrow);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        applyFontForToolbarTitle(activity, toolbar, textSize);
    }

    public static void applyFontForToolbarTitle(Activity context, Toolbar toolbar, int textSize){
        for(int i = 0; i < toolbar.getChildCount(); i++){
            View view = toolbar.getChildAt(i);
            if(view instanceof TextView){
                TextView tv = (TextView) view;
                tv.setTextSize(textSize);
                if (Utils.ATLEAST_LOLLIPOP) {
                    tv.setLetterSpacing(-0.03f);
                }
                Typeface typeface = Fonts.getTypeface(Fonts.Font.ofFontResId(R.string.custom_font_bold), 0);
                if(tv.getText().equals(toolbar.getTitle())){
                    tv.setTypeface(typeface);
                    break;
                }
            }
        }
    }

    public static Bitmap getBitmapFromLocalFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void showToast(String hint) {
       Toast toast = new Toast(HSApplication.getContext().getApplicationContext());
        final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.toast_theme_apply, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentView.setElevation(Dimensions.pxFromDp(8));
        }
        TextView textView = contentView.findViewById(R.id.text_toast);
        textView.setText(hint);
        int yOffset = (int) (0.6f * Dimensions.getPhoneHeight(HSApplication.getContext()));
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yOffset);
        toast.setView(contentView);
        toast.show();
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
    }

    public static boolean isAnyLockerAppInstalled() {

        List<?> lockers = HSConfig.getList("Application", "Promote", "LockerList");
        for (Object item : lockers) {
            try {
                HSApplication.getContext().getPackageManager().getPackageInfo((String)item, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf(File.separator) + 1);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl() {
        Resources res = HSApplication.getContext().getResources();
        return ATLEAST_JB_MR1 && (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    /**
     * Sets up transparent status bars in LMP.
     * This method is a no-op for other platform versions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setupTransparentStatusBarsForLmp(Activity activityContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activityContext.getWindow();
            window.getAttributes().systemUiVisibility |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
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

    /**
     * Whether we are in new user "Do Not Disturb" status.
     */
    public static boolean isNewUserInDNDStatus() {
        long currentTimeMills = System.currentTimeMillis();
        return currentTimeMills - HSSessionMgr.getFirstSessionStartTime() < USE_DND_DURATION;
    }

    public static Notification buildNotificationSafely(NotificationCompat.Builder builder) {
        try {
            return builder.build();
        } catch (Exception e) {
            HSLog.e(TAG, "Error building notification: " + builder + ", exception: " + e);
            return null;
        }
    }

    public static float celsiusCoolerByToFahrenheit(float celsius) {
        return celsius * 1.8f;
    }

    public static
    @NonNull
    Bitmap decodeResourceWithFallback(Resources res, int id) {
        Bitmap decoded = BitmapFactory.decodeResource(res, id);
        if (decoded == null) {
            decoded = createFallbackBitmap();
        }
        return decoded;
    }

    public static
    @NonNull
    Bitmap createFallbackBitmap() {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

    public static boolean unregisterReceiver(Context context, BroadcastReceiver receiver) {
        if (receiver == null) {
            return true;
        }
        try {
            context.unregisterReceiver(receiver);
            return true;
        } catch (Exception e) {
            HSLog.e(TAG, "Error unregistering broadcast receiver: " + receiver + " at ");
            e.printStackTrace();
            return false;
        }
    }

    public static double formatNumberTwoDigit(double number) {
        BigDecimal bg = new BigDecimal(number);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static String getAppTitle(String packageName) {
        String title = "";
        try {
            PackageManager pm = HSApplication.getContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            if (info != null) {
                title = info.loadLabel(pm).toString();
                if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(info.name)) {
                    title = info.name;
                }

                if (TextUtils.isEmpty(title)) {
                    title = packageName;
                }
            }
        } catch (Exception ignored) {
        }
        return title;
    }

    public static boolean isNewUser() {
        return HSApplication.getFirstLaunchInfo().appVersionCode == HSApplication.getCurrentLaunchInfo().appVersionCode;
    }


    private static long sInstallTime;

    public static long getAppInstallTimeMillis() {
        if (sInstallTime <= 0) {
            sInstallTime = getInstallTime();
        }
        return sInstallTime;
    }

    public static long getInstallTime() {
        long firstSessionTime = HSSessionMgr.getFirstSessionStartTime();
        return firstSessionTime > 0 ? firstSessionTime : System.currentTimeMillis();
    }

    public static boolean installVersionAfter(int versionCode) {
        return HSApplication.getFirstLaunchInfo().appVersionCode >= versionCode;
    }

    public static int getDefaultThemeId() {
        String defaultThemeId = HSConfig.optString("DeepLove", "Application", "Theme", "DefaultThemeID");
        for (Theme theme : Theme.themes()) {
            if (TextUtils.equals(theme.getIdName(), defaultThemeId)) {
                return theme.getId();
            }
        }
        return Theme.NEON;
    }

    public static String getNewDate(long time) {
        return DateUtils.getRelativeTimeSpanString(time * DateUtils.SECOND_IN_MILLIS).toString();
    }
}

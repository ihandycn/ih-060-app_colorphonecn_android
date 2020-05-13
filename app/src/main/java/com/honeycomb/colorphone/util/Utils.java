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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.lock.ReflectionHelper;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.RateAlertActivity;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.preview.transition.TransitionView;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;
import com.umeng.commonsdk.statistics.common.DeviceConfig;

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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.VISIBLE;

public final class Utils {

    private static final String TAG = "Utils";
    private static final int ALPHA_TEXT = 1;

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
            if (removed) {
                HSLog.d(TAG, "Replacing file " + dst);
            }
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

    public static void configActivityStatusBar(AppCompatActivity activity, Toolbar toolbar, int upDrawable) {
        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.colorPrimaryReverse));

        activity.setSupportActionBar(toolbar);
        final Drawable upArrow = ContextCompat.getDrawable(activity, upDrawable);
        upArrow.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimaryReverse), PorterDuff.Mode.SRC_ATOP);
        activity.getSupportActionBar().setHomeAsUpIndicator(upArrow);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        applyFontForToolbarTitle(activity, toolbar);
    }

    public static TextView getTitleView( Toolbar toolbar) {
        for(int i = 0; i < toolbar.getChildCount(); i++){
            View view = toolbar.getChildAt(i);
            if(view instanceof TextView){
                TextView tv = (TextView) view;
                if(tv.getText().equals(toolbar.getTitle())){
                    return tv;
                }
            }
    }
        return null;
    }

    public static void applyFontForToolbarTitle(Activity context, Toolbar toolbar){
        TextView tv = getTitleView(toolbar);
        if (tv != null) {
            Typeface typeface = FontUtils.getTypeface(FontUtils.Font.ofFontResId(R.string.proxima_nova_bold), 0);
            tv.setTypeface(typeface);
            if (Locale.getDefault().getLanguage().equalsIgnoreCase(Locale.CHINA.getLanguage())) {
                tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                tv.invalidate();
            }
        }
    }

    public static float dpiFromPx(int size) {
        return (size / getDensityRatio());
    }

    public static Bitmap getBitmapFromLocalFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static void showApplySuccessToastView(PercentRelativeLayout rootView, TransitionView backTransition) {
        final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.lottie_theme_apply, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentView.setElevation(Dimensions.pxFromDp(8));
        }

        ViewGroup viewGroup = (ViewGroup) rootView;
        viewGroup.addView(contentView);

        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        RelativeLayout themeApply = contentView.findViewById(R.id.theme_apply_view);
        RelativeLayout themeChange = contentView.findViewById(R.id.theme_apply_view_change);
        LottieAnimationView lottieThemeApply = contentView.findViewById(R.id.lottie_theme_apply);
        TextView applySuccessText = contentView.findViewById(R.id.apply_success_text);
        TextView applyText = contentView.findViewById(R.id.apply_text);

        themeChange.animate().alpha(1f)
                .setDuration(166)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        themeChange.setVisibility(VISIBLE);

                    }
                })
                .start();

        if (backTransition != null) {
            backTransition.hide(true);
        }

        applySuccessText.setTranslationY(Dimensions.pxFromDp(40));
        applySuccessText.setAlpha(0);
        ObjectAnimator moveUp = ObjectAnimator.ofFloat(applySuccessText, "translationY", Dimensions.pxFromDp(40), 0f);
        moveUp.setInterpolator(PathInterpolatorCompat.create(0.4f, 0.61f, 1f, 1f));
        ObjectAnimator fadeInOut = ObjectAnimator.ofFloat(applySuccessText, "alpha", 0f, 0.8f);
        fadeInOut.setInterpolator(PathInterpolatorCompat.create(0.4f, 0.57f, 0.74f, 1f));
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(250);
        animatorSet.setStartDelay(116);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                applySuccessText.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.play(moveUp).with(fadeInOut);
        animatorSet.start();

        applyText.setTranslationY(Dimensions.pxFromDp(50));
        applyText.setAlpha(0);
        ObjectAnimator moveUp1 = ObjectAnimator.ofFloat(applyText, "translationY", Dimensions.pxFromDp(50), 0f);
        moveUp1.setInterpolator(PathInterpolatorCompat.create(0.4f, 0.35f, 1f, 1f));
        ObjectAnimator fadeInOut1 = ObjectAnimator.ofFloat(applyText, "alpha", 0f, 0.5f);
        fadeInOut1.setInterpolator(PathInterpolatorCompat.create(0.4f, 0.6f, 0.74f, 1f));
        AnimatorSet animatorSet1 = new AnimatorSet();
        animatorSet1.setDuration(250);
        animatorSet1.setStartDelay(116);
        animatorSet1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                applyText.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet1.play(moveUp1).with(fadeInOut1);
        animatorSet1.start();

        lottieThemeApply.animate().setStartDelay(166)
                .setDuration(716)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        lottieThemeApply.setVisibility(VISIBLE);
                        lottieThemeApply.playAnimation();
                    }
                }).start();

        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                themeChange.animate().alpha(0)
                        .setDuration(166)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                themeChange.setVisibility(View.GONE);
                            }
                        })
                        .start();

                themeApply.animate().alpha(0)
                        .setDuration(166)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                themeApply.setVisibility(View.GONE);
                                viewGroup.removeView(contentView);
                            }
                        })
                        .start();
                backTransition.show(true);
            }
        }, 1382);
        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (FiveStarRateTip.canShowWhenApplyTheme()) {
                    RateAlertActivity.showRateFrom(rootView.getContext(), FiveStarRateTip.From.SET_THEME);
                }
            }
        }, 1548);
    }



    public static void showDefaultFailToast() {
        Toast toast = new Toast(HSApplication.getContext().getApplicationContext());
        final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.toast_set_default_fail, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentView.setElevation(Dimensions.pxFromDp(8));
        }
        int yOffset = (int) (0.6f * Dimensions.getPhoneHeight(HSApplication.getContext()));
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yOffset);
        toast.setView(contentView);
        toast.setDuration(Toast.LENGTH_LONG);
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

    private static long getInstallTime() {
        long firstSessionTime = HSSessionMgr.getFirstSessionStartTime();
        return firstSessionTime > 0 ? firstSessionTime : System.currentTimeMillis();
    }

    public static boolean installVersionAfter(int versionCode) {
        return HSApplication.getFirstLaunchInfo().appVersionCode >= versionCode;
    }

    public static int getDefaultThemeId() {
        return -1;
    }

    public static String[] getTestDeviceInfo(Context context){
        String[] deviceInfo = new String[2];
        try {
            if(context != null){
                deviceInfo[0] = DeviceConfig.getDeviceIdForGeneral(context);
                deviceInfo[1] = DeviceConfig.getMac(context);
            }
        } catch (Exception e){
        }
        return deviceInfo;
    }

    public static String getDeviceInfo() {
        return String.valueOf(Build.VERSION.SDK_INT);
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

    public static double getEmuiVersion() {
        try {
            String emuiVersion = RomUtils.getSystemProperty("ro.build.version.emui");
            String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            if (version.length() > 0) {
                String[] vers = version.split("\\.");
                if (vers.length > 0) {
                    return Double.parseDouble(vers[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4.0;
    }
}

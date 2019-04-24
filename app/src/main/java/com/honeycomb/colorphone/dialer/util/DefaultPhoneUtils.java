package com.honeycomb.colorphone.dialer.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.TelecomManager;
import android.text.TextUtils;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

import java.util.List;

public class DefaultPhoneUtils {

    public static final String PREFS_DEFAULT_PHONE_PKG = "default_phone_package";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void saveSystemDefaultPhone() {
        TelecomManager telecomManager = (TelecomManager) HSApplication.getContext().getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            Preferences.get(Constants.DESKTOP_PREFS).putString(PREFS_DEFAULT_PHONE_PKG, telecomManager.getDefaultDialerPackage());
        }
    }

    public static boolean isDefaultPhone() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) HSApplication.getContext().getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                return HSApplication.getContext().getPackageName().equals(telecomManager.getDefaultDialerPackage());
            }
        }
        return false;
    }

    public static boolean checkGuideResult() {
        boolean needCheckDefaultSetResult =
                Preferences.get(Constants.DESKTOP_PREFS)
                        .getBoolean(Constants.PREFS_CHECK_DEFAULT_PHONE, false);
        if (needCheckDefaultSetResult) {
            Preferences.get(Constants.DESKTOP_PREFS)
                    .putBoolean(Constants.PREFS_CHECK_DEFAULT_PHONE, false);
            boolean defaultPhone = isDefaultPhone();
            HSLog.d("DefaultPhoneUtils", "default phone now : " + defaultPhone);
            if (defaultPhone) {
                ConfigEvent.successSetAsDefault();
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void checkDefaultPhoneSettings() {
        Analytics.logEvent("Dialer_Set_Default_Show");
        ConfigEvent.monitorResult();

        Preferences.get(Constants.DESKTOP_PREFS).putBoolean(Constants.PREFS_CHECK_DEFAULT_PHONE, true);
        checkDefaultWithoutEvent();
    }

    public static void checkDefaultWithoutEvent() {
        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, HSApplication.getContext().getPackageName());
        Navigations.startActivitySafely(HSApplication.getContext(), intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void resetDefaultPhone() {
        String systemPhone = Preferences.get(Constants.DESKTOP_PREFS)
                .getString(PREFS_DEFAULT_PHONE_PKG, "");
        if (TextUtils.isEmpty(systemPhone)) {
            systemPhone = findDefaultDialerPkg();
            HSLog.d("findDefaultDialerPkg", "pkg=" + systemPhone);
        }
        if (!TextUtils.isEmpty(systemPhone)) {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, systemPhone);
            Navigations.startActivitySafely(HSApplication.getContext(), intent);
        }

    }

    public static String findDefaultDialerPkg() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + "911");
        intent.setData(data);

        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        final String myPkg = HSApplication.getContext().getPackageName();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (!TextUtils.equals(resolveInfo.activityInfo.packageName, myPkg)) {
                return resolveInfo.activityInfo.packageName;
            }
        }
        return null;
    }
}

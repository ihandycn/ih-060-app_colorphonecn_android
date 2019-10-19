package com.acb.libwallpaper.live.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.permission.NormalPermissionGuide;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Permissions;

import java.util.Locale;

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    public static final int REQUEST_PERMISSION = 1234;

    private static final int PERMISSION_NOTIFICATION = 2;

    private static String[] getSettingNames(int permission) {
        switch (permission) {
            case PERMISSION_NOTIFICATION:
                return getNotificationSettingNames();
        }
        throw new IllegalArgumentException("Permission not define");
    }

    private static String[] getNotificationSettingNames() {
        if (CommonUtils.ATLEAST_JB_MR2) {
            String[] names = new String[1];
            names[0] = ENABLED_NOTIFICATION_LISTENERS;
            return names;
        } else {
            return null;
        }
    }

    public static boolean shouldEnforceUsageAccessPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1
                || Permissions.isUsageAccessGranted()) {
            return false;
        }
        return true;
    }

    public static boolean checkContentObservePermissionForUri(Uri uri) {
        final Context context = HSApplication.getContext();

        Cursor cursor = null;
        boolean result = false;
        try {
            cursor = context.getContentResolver().query(uri,
                    new String[]{BaseColumns._ID}, null, null, null);

            result = true;
        } catch (Exception e) {
            HSLog.d(TAG, "permission denied for " + uri);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            } else {
                result = false;
            }
        }
        HSLog.d(TAG, "permission access for " + uri);
        return result;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void startUsageAccessPermissionSetting(Context context) {
        if (context instanceof Activity) {
            try {
                context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } catch (SecurityException | ActivityNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            try {
                context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
            } catch (SecurityException | ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tryRequestPermissionFromSystemSettings(Activity activity, boolean needGuide) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, REQUEST_PERMISSION);
        if (needGuide){
            Intent i = new Intent(activity, NormalPermissionGuide.class);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(activity, R.anim.fade_in_long, R.anim.app_lock_fade_out_long);
            new Handler().postDelayed(() -> {
                try {
                    activity.startActivity(i, options.toBundle());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 1000);
        }
    }

    public static void showPermissionRequestFailedToast() {
        @SuppressLint("InflateParams")
        View toastView = LayoutInflater.from(HSApplication.getContext()).inflate(
                R.layout.permission_request_failed_toast, null);
        Toast toast = new Toast(HSApplication.getContext());
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showUnreadMessagePermissionRequestSucceedToast() {
        Context context = HSApplication.getContext();

        @SuppressLint("InflateParams")
        View toastView = LayoutInflater.from(context).inflate(
                R.layout.permission_request_succeed_toast, null);

        Resources resources = context.getResources();
        String format = resources.getString(R.string.permission_request_succeed_message);
        String args = resources.getString(R.string.result_page_unreadmessage_promote_notification_access_guide_title);
        if (Locale.getDefault().getLanguage().equals("en")) {
            args = resources.getString(R.string.permission_request_unread_message);
        }
        String formattedString = String.format(format, args);
        ((TextView) toastView.findViewById(R.id.content)).setText(formattedString);

        Toast toast = new Toast(HSApplication.getContext());
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}

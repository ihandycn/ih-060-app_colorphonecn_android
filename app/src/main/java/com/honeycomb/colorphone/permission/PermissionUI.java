package com.honeycomb.colorphone.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;

public class PermissionUI {
    public static boolean requestDrawOverlayIfNeeded() {
        final Context context = HSApplication.getContext();
        boolean hasPermission = FloatWindowManager.getInstance().checkPermission(context);
        boolean request = !hasPermission;

        if (request) {
            // TODO
            boolean needShowTip  = true;
            if (needShowTip) {
                Threads.postOnMainThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Navigations.startActivitySafely(context, new Intent(context, OverlayGuideActivity.class));

                    }
                }, 200);
                LauncherAnalytics.logEvent("Flashlight_Permission_Alert_FloatWindow_View_Showed");
            }
            FloatWindowManager.getInstance().applyPermission(context);
        }

        return request;
    }

//    public static void tryRequestPermissionFromSystemSettings(Activity activity, boolean needGuide) {
//        Intent intent = new Intent();
//        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
//        intent.setData(uri);
//        activity.startActivity(intent);
//        if (needGuide){
//            Intent i = new Intent(activity, SettingPermissionsGuideActivity.class);
//            new Handler().postDelayed(() -> {
//                try {
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                        ActivityOptions options = ActivityOptions.makeCustomAnimation(activity, android.R.anim.fade_in, android.R.anim.fade_out);
//                        activity.startActivity(i, options.toBundle());
//                    } else {
//                        activity.startActivity(i);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }, 1000);
//        }
//    }

    public static void showPermissionRequestToast(boolean success) {
        if (success) {
            showPermissionRequestSucceedToast();
        } else {
            showPermissionRequestFailedToast();
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

    public static void showPermissionRequestSucceedToast() {
        Context context = HSApplication.getContext();

        @SuppressLint("InflateParams")
        View toastView = LayoutInflater.from(context).inflate(
                R.layout.permission_request_succeed_toast, null);

        Resources resources = context.getResources();
        String msg = resources.getString(R.string.permission_request_succeed_message);
        ((TextView) toastView.findViewById(R.id.content)).setText(msg);

        Toast toast = new Toast(HSApplication.getContext());
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}

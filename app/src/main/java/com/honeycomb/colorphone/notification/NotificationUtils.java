package com.honeycomb.colorphone.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.Nullable;

import com.acb.call.MediaDownloadManager;
import com.acb.call.customize.AcbCallManager;
import com.acb.call.themes.Type;
import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.acb.utils.PermissionUtils;
import com.acb.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.app.framework.HSSessionObserver;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.utils.HSNetworkUtils;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NotificationUtils {

    public static final String PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED = "PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED";
    
    public static boolean isShowNotificationGuideAlertInFirstSession(Context context) {
        if (!isInsideAppAccessAlertEnabled(context)) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, false)) {
            return false;
        }
        return true;
    }

    public static boolean isShowNotificationGuideAlertWhenApplyTheme(Context context) {
        if (!isInsideAppAccessAlertEnabled(context)) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getInt(GuideApplyThemeActivity.PREFS_GUIDE_APPLY_ALERT_SHOW_SESSION_ID, 0)
                == SessionMgr.getInstance().getCurrentSessionId()) {
            return false;
        }

       if(HSPreferenceHelper.getDefault().getInt(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, 0)
                >= NotificationConfig.getInsideAppAccessAlertShowMaxTime()) {
            return false;
       }

        if (HSPreferenceHelper.getDefault().getLong(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_TIME, 0)
                + NotificationConfig.getInsideAppAccessAlertInterval() > System.currentTimeMillis()) {
            return false;
        }

        if (SessionMgr.getInstance().getCurrentSessionId() <= 1 && HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, false)) {
            return false;
        }
        return true;
    }

    private static boolean isInsideAppAccessAlertEnabled(Context context) {
        if (!NotificationConfig.isInsideAppAccessAlertOpen()) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || PermissionUtils.isNotificationAccessGranted(context)) {
            return false;
        }
        return true;
    }


    /**
     *
     *  theme notification
     *
     */

    private void sendNotificationIfProper() {
        if (isShowNewThemeNotification()) {

        } else if (isShowOldThemeNotification()) {

        }
    }

    private boolean isShowNewThemeNotification() {
        return true;
    }

    private boolean isShowOldThemeNotification() {
        return false;
    }

    private MediaDownloadManager mediaDownloadManager = new MediaDownloadManager();

    private void downLoadPreviewImage(final Type type) {
        registerWifiReceiver();
        GlideApp.with(ColorPhoneApplication.getContext()).download(type.getFileName()).listener(new RequestListener<File>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                unregisterWifiReceiver();
                return false;
            }

            @Override
            public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                if (wifiConnected) {
                    downLoadMp4(type);
                }
                unregisterWifiReceiver();
                return false;
            }
        });
    }

    private void downLoadMp4(final Type type) {
        mediaDownloadManager.downloadMedia(type.getMp4Url(), type.getFileName(), new MediaDownloadManager.DownloadCallback() {
            @Override
            public void onUpdate(long l) {

            }

            @Override
            public void onFail(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask, String s) {

            }

            @Override
            public void onSuccess(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask) {
                doShowNotification(type);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void doShowNotification(Type type) {

    }

    private BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)){
                    wifiConnected = true;
                } else {
                    wifiConnected = false;
                }
            }
        }
    };

    private boolean wifiConnected;

    private void registerWifiReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        ColorPhoneApplication.getContext().registerReceiver(wifiBroadcastReceiver, intentFilter);
    }

    private void unregisterWifiReceiver() {
        if (wifiBroadcastReceiver != null) {
            ColorPhoneApplication.getContext().unregisterReceiver(wifiBroadcastReceiver);
            wifiBroadcastReceiver = null;
        }
    }

}

package com.honeycomb.colorphone.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.acb.call.MediaDownloadManager;
import com.acb.call.themes.Type;
import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.acb.utils.PermissionUtils;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;

import static com.honeycomb.colorphone.notification.NotificationConstants.PREFS_NOTIFICATION_THEMES_SENT;

public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

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

        if (HSPreferenceHelper.getDefault().getInt(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, 0)
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
     * theme notification
     */

    private interface ShowNewThemeNotificationListener {
        void onFailed();

        void onSuccess();
    }

    protected static void sendNotificationIfProper() {
        showNewThemeNotification(new ShowNewThemeNotificationListener() {
            @Override
            public void onFailed() {
                showOldThemeNotification();
            }

            @Override
            public void onSuccess() {

            }
        });
    }

    private static void showNewThemeNotification(ShowNewThemeNotificationListener listener) {
        Type notificationType = getNewestType();
        if (notificationType == null) {
            if (listener != null) {
                listener.onFailed();
            }
            Log.d(TAG, "new Type notificationType = null");
            return;
        }
        Log.d(TAG, "startLoad new type Notification" + " id = " + notificationType.getId() + "name = " + notificationType.getName());
        downLoadPreviewImage(notificationType, listener);
    }

    private static Type getNewestType() {
        int maxId = HSPreferenceHelper.getDefault().getInt(NotificationConstants.PREFS_NOTIFICATION_OLD_MAX_ID, -1);
        Type notificationType = null;
        int tempId = -1;
        for (Type theme : Type.values()) {

            if (theme.getId() > maxId && theme.isHot()) {
                if (tempId < theme.getId()) {
                    tempId = theme.getId();
                    notificationType = theme;
                }
            }
        }
        return notificationType;
    }

    private static MediaDownloadManager mediaDownloadManager = new MediaDownloadManager();

    private static void downLoadPreviewImage(final Type type, final ShowNewThemeNotificationListener listener) {
        GlideApp.with(ColorPhoneApplication.getContext())
                .downloadOnly().load(type.getPreviewImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        listener.onFailed();

                        Log.d(TAG, "load Preview Image failed");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {

                        Log.d(TAG, "wifiConnected = " + Utils.isWifiEnabled());
                        if (Utils.isWifiEnabled()) {
                            downloadMedia(type, listener, true);
                        }
                        return false;
                    }
                }).preload();
    }

    public static void downLoadMedia(Type type) {
        downloadMedia(type, null, false);
    }

    public static void downloadMedia(final Type type, final ShowNewThemeNotificationListener listener, final boolean showNotification) {
        Log.d(TAG, "start download Mp4");
        if (mediaDownloadManager.isDownloaded(type.getFileName())) {
            if (listener != null) {
                listener.onFailed();
            }
            Log.d(TAG, "already downLoaded");
            return;
        }

        mediaDownloadManager.downloadMedia(type.getMp4Url(), type.getFileName(), new MediaDownloadManager.DownloadCallback() {
            @Override
            public void onUpdate(long l) {

            }

            @Override
            public void onFail(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask, String s) {
                if (listener != null) {
                    listener.onFailed();
                }
                Log.d(TAG, "download media failed");
            }

            @Override
            public void onSuccess(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask) {
                Log.d(TAG, "download media success " + "app foreGround = " + ColorPhoneApplication.isAppForeground());
                HSGlobalNotificationCenter.sendNotification(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME);

                if (!showNotification) {
                    return;
                }
                if (!ColorPhoneApplication.isAppForeground()) {
                    if (!isThemeNotificationSentEver(type)) {
                        doShowNotification(type, HSApplication.getContext());

                        if (listener != null) {
                            listener.onSuccess();
                        }
                        return;
                    }
                }

                if (listener != null) {
                    listener.onFailed();
                }

            }

            @Override
            public void onCancel() {
                if (listener != null) {
                    listener.onFailed();
                }
            }
        });
    }

    public static void saveThemeNotificationSent(Type theme) {
        StringBuilder sb = new StringBuilder(4);
        String pre = HSPreferenceHelper.getDefault().getString(PREFS_NOTIFICATION_THEMES_SENT, "");
        sb.append(pre).append(theme.getId()).append(",");
        HSPreferenceHelper.getDefault().putString(PREFS_NOTIFICATION_THEMES_SENT, sb.toString());
    }

    public static boolean isThemeNotificationSentEver(Type theme) {

        String[] themes = HSPreferenceHelper.getDefault().getString(PREFS_NOTIFICATION_THEMES_SENT, "").split(",");
        for (String themeId : themes) {
            if (TextUtils.isEmpty(themeId)) {
                continue;
            }
            if (theme.getId() == Integer.parseInt(themeId)) {
                return true;
            }
        }
        return false;
    }

    private static void doShowNotification(Type type, Context context) {
        Intent intentClick = new Intent(context, NotificationActionReceiver.class);
        intentClick.setAction(NotificationConstants.THEME_NOTIFICATION_CLICK_ACTION);
        intentClick.putExtra(NotificationConstants.THEME_NOTIFICATION_KEY, NotificationConstants.THEME_NOTIFICATION_ID);
        PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_ONE_SHOT);

        Intent intentDelete = new Intent(context, NotificationActionReceiver.class);
        intentDelete.setAction(NotificationConstants.THEME_NOTIFICATION_DELETE_ACTION);
        intentDelete.putExtra(NotificationConstants.THEME_NOTIFICATION_KEY, NotificationConstants.THEME_NOTIFICATION_ID);
        PendingIntent pendingIntentDelete = PendingIntent.getBroadcast(context, 0, intentDelete, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.drawer_icon)
                        .setContentTitle(type.getName())
                        .setContentText("content test")
                        .setDeleteIntent(pendingIntentDelete)
                        .setContentIntent(pendingIntentClick);
        android.app.NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationConstants.THEME_NOTIFICATION_ID, builder.build());

        saveThemeNotificationSent(type);
        HSPreferenceHelper.getDefault().putLong(NotificationConstants.PREFS_NOTIFICATION_SHOWED_LAST_TIME, System.currentTimeMillis());
    }

    public static void showOldThemeNotification() {
        if (!isShowOldThemeNotificationAtValidInterval()) {
            return;
        }
        if (isAppOpenedInLastSeveralDays()) {
            return;
        }
        final Type notificationType = getOldThemeType();

        Log.d(TAG, notificationType == null ? "notification null" : "old theme start load");
        if (notificationType != null) {
            downLoadPreviewImage(notificationType, new ShowNewThemeNotificationListener() {
                @Override
                public void onFailed() {
                    Log.d(TAG, "load old preview image failed");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "start download preview image");
                    downloadMedia(notificationType, null, true);
                }
            });
        }
    }

    private static boolean isShowOldThemeNotificationAtValidInterval() {
        Log.d(TAG, "showOldThemeAtValidInterval");

        long interval = HSConfig.optInteger(0, "Application", "OldThemePush", "MinShowIntervalByPush")
                            * DateUtils.DAY_IN_MILLIS;
        if (System.currentTimeMillis() -
                HSPreferenceHelper.getDefault().getLong(NotificationConstants.PREFS_NOTIFICATION_SHOWED_LAST_TIME, 0)
                > interval) {

            Log.d(TAG, "showOldThemeAtValidInterval  valid");
            return true;
        }
        Log.d(TAG, "showOldThemeAtValidInterval  invalid");
        return false;
    }

    private static boolean isAppOpenedInLastSeveralDays() {
        long interval = HSConfig.optInteger(0, "Application", "OldThemePush", "MinShowIntervalByOpenApp")
                            * DateUtils.DAY_IN_MILLIS;
        if (System.currentTimeMillis() -
                HSPreferenceHelper.getDefault().getLong(NotificationConstants.PREFS_APP_OPENED_TIME, 0)
                > interval) {
            Log.d(TAG, "app not opened  should show notification");
            return false;
        }
        Log.d(TAG, "should not show notification");
        return true;
    }

    private static Type getOldThemeType() {
        int maxId = HSPreferenceHelper.getDefault().getInt(NotificationConstants.PREFS_NOTIFICATION_OLD_MAX_ID, 0);
        Type notificationType = null;
        int tempIndex = Integer.MAX_VALUE;
        for (Type theme : Type.values()) {
            if (theme.getId() < maxId
                    && !theme.isHot()
                    && theme.isMedia()
                    && !ThemePreviewView.isThemeAppliedEver(theme)) {
                if (tempIndex > theme.getIndex()) {
                    tempIndex = theme.getIndex();
                }
            }
        }
        return notificationType;
    }

}

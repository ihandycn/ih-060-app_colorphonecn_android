package com.honeycomb.colorphone.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.acb.call.MediaDownloadManager;
import com.acb.call.themes.Type;
import com.acb.call.utils.FileUtils;
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
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.util.ArrayList;

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

    private interface ThemeNotificationListener {
        void onFailed();

        void onSuccess(Theme type);
    }

    public static void logThemeAppliedFlurry(Theme theme) {
        if (SessionMgr.getInstance().getCurrentSessionId() ==
                HSPreferenceHelper.getDefault().getInt(NotificationConstants.THEME_NOTIFICATION_SESSION_ID, 0)) {
            boolean isNewTheme = HSPreferenceHelper.getDefault().getBoolean(NotificationConstants.THEME_NOTIFICATION_IS_NEW_THEME, false);
            if (isNewTheme) {
                HSAnalytics.logEvent("Colorphone_LocalPush_NewTheme_ThemeApply", "ThemeName", theme.getName());
                NotificationAutoPilotUtils.logNewThemeNotificationApply();
            } else {
                HSAnalytics.logEvent("Colorphone_LocalPush_OldTheme_ThemeApply", "ThemeName", theme.getName());
                NotificationAutoPilotUtils.logOldThemeNotificationApply();
            }
        }
    }

    public static void sendNotificationIfProper() {
        showNewThemeNotificationIfProper(new ThemeNotificationListener() {
            @Override
            public void onFailed() {
                showOldThemeNotificationIfProper();
            }

            @Override
            public void onSuccess(Theme type) {
                doShowNotification(type, true);
            }
        });
    }

    private static void showNewThemeNotificationIfProper(ThemeNotificationListener listener) {
        if (!NotificationAutoPilotUtils.isNewThemeNotificationEnabled()) {
            listener.onFailed();
            return;
        }
        Theme notificationType = getNewestType();
        if (notificationType == null) {
            if (listener != null) {
                listener.onFailed();
            }
            HSLog.d(TAG, "new Theme notificationType = null");
            return;
        }
        HSLog.d(TAG, "startLoad new type Notification" + " id = " + notificationType.getId() + "name = " + notificationType.getName());
        downLoadPreviewImage(notificationType, listener);
    }

    private static Theme getNewestType() {
        int maxId = HSPreferenceHelper.getDefault().getInt(NotificationConstants.PREFS_NOTIFICATION_OLD_MAX_ID, -1);
        Theme notificationType = null;
        int tempId = -1;

        ArrayList<Type> themeTypes = Type.values();
        for (Type type : themeTypes) {
            if (type.getValue() == Type.NONE) {
                continue;
            }
            Theme theme = (Theme) type;
            if (theme.getId() > maxId && theme.isHot() && theme.isNotificationEnabled()) {
                if (tempId < theme.getId() && !isThemeNotificationSentEver(theme)) {
                    tempId = theme.getId();
                    notificationType = theme;
                }
            }
        }
        return notificationType;
    }

    private static MediaDownloadManager mediaDownloadManager = new MediaDownloadManager();

    private static void downLoadPreviewImage(final Theme type, @Nullable final ThemeNotificationListener listener) {
        GlideApp.with(ColorPhoneApplication.getContext())
                .downloadOnly()
                .load(type.getPreviewImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        HSLog.d(TAG, "load Preview Image failed");
                        listener.onFailed();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        downloadNotificationPreviewImages(type, listener);
                        return false;
                    }
                }).preload();
    }

    private static void downloadNotificationPreviewImages(final Theme theme, final ThemeNotificationListener listener) {
        mediaDownloadManager.downloadMedia(theme.getNotificationBigPictureUrl(), theme.getNotificationBigPictureFileName(), new MediaDownloadManager.DownloadCallback() {
            @Override
            public void onUpdate(long l) {
                HSLog.d(TAG, "previewImages " + l);
            }

            @Override
            public void onFail(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask, String s) {
                listener.onFailed();
            }

            @Override
            public void onSuccess(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask) {
                downloadNotificationLargeIcon(theme, listener);
            }

            @Override
            public void onCancel() {
                listener.onFailed();
            }
        });
    }

    private static void downloadNotificationLargeIcon(final Theme theme, final ThemeNotificationListener listener) {
        mediaDownloadManager.downloadMedia(theme.getNotificationLargeIconUrl(), theme.getNotificationLargeIconFileName(), new MediaDownloadManager.DownloadCallback() {
            @Override
            public void onUpdate(long l) {
                HSLog.d(TAG, "largeIcon " + l);
            }

            @Override
            public void onFail(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask, String s) {
                listener.onFailed();
            }

            @Override
            public void onSuccess(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask) {
                if (Utils.isWifiEnabled()) {
                    downloadMedia(theme, listener);
                } else if (!ColorPhoneApplication.isAppForeground()) {
                    listener.onSuccess(theme);
                }
            }

            @Override
            public void onCancel() {
                listener.onFailed();
            }
        });
    }

    public static void downloadMedia(Theme type) {
        downloadMedia(type, null);
    }

    public static void downloadMedia(final Theme type, final ThemeNotificationListener listener) {
        HSLog.d(TAG, "start download Mp4");

        final boolean canShowNotification = !ColorPhoneApplication.isAppForeground();
        if (!canShowNotification) {
            if (listener != null) {
                listener.onFailed();
            }
        }

        if (mediaDownloadManager.isDownloaded(type.getFileName())) {
            if (canShowNotification) {
                if (listener != null) listener.onSuccess(type);
            }
            HSLog.d(TAG, "already downLoaded");
            return;
        }

        mediaDownloadManager.downloadMedia(type.getMp4Url(), type.getFileName(), new MediaDownloadManager.DownloadCallback() {
            @Override
            public void onUpdate(long l) {

            }

            @Override
            public void onFail(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask, String s) {
                if (canShowNotification) {
                    if (listener != null) listener.onSuccess(type);
                }
                HSLog.d(TAG, "download media failed");
            }

            @Override
            public void onSuccess(MediaDownloadManager.MediaDownLoadTask mediaDownLoadTask) {
                HSLog.d(TAG, "download media success " + "app foreGround = " + ColorPhoneApplication.isAppForeground());

                HSGlobalNotificationCenter.sendNotification(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME);

                if (canShowNotification) {
                    if (listener != null) listener.onSuccess(type);
                }
            }

            @Override
            public void onCancel() {
                if (canShowNotification) {
                    if (listener != null) listener.onSuccess(type);
                }
            }
        });
    }

    public static void saveThemeNotificationSent(Theme theme) {
        StringBuilder sb = new StringBuilder();
        String pre = HSPreferenceHelper.getDefault().getString(PREFS_NOTIFICATION_THEMES_SENT, "");
        sb.append(pre).append(theme.getId()).append(",");
        HSPreferenceHelper.getDefault().putString(PREFS_NOTIFICATION_THEMES_SENT, sb.toString());
    }

    public static boolean isThemeNotificationSentEver(Theme theme) {
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

    private static void doShowNotification(Theme type, boolean isNewTheme) {
        Context context = HSApplication.getContext();
        boolean isMp4Downloaded = mediaDownloadManager.isDownloaded(type.getFileName());

        Intent intentClick = new Intent(context, NotificationActionReceiver.class);
        intentClick.setAction(NotificationConstants.THEME_NOTIFICATION_CLICK_ACTION);
        fillNotificationActionIntent(intentClick, isNewTheme, isMp4Downloaded, type);
        PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_ONE_SHOT);

        Intent intentDelete = new Intent(context, NotificationActionReceiver.class);
        intentDelete.setAction(NotificationConstants.THEME_NOTIFICATION_DELETE_ACTION);
        fillNotificationActionIntent(intentDelete, isNewTheme, isMp4Downloaded, type);
        PendingIntent pendingIntentDelete = PendingIntent.getBroadcast(context, 0, intentDelete, PendingIntent.FLAG_ONE_SHOT);

        String contentText;
        String title;
        if (isNewTheme) {
            contentText = NotificationAutoPilotUtils.getNewThemeNotificationContent();
            title = NotificationAutoPilotUtils.getNewThemeNotificationTitle();
        } else {
            contentText = NotificationAutoPilotUtils.getOldThemeNotificationContent();
            title = NotificationAutoPilotUtils.getOldThemeNotificationTitle();
        }
        contentText = String.format(contentText, type.getName());

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getSmallIconRes())
                        .setLargeIcon(getLargeIconBitmap(context, type))
                        .setContentTitle(title)
                        .setContentText(contentText)
                        .setDeleteIntent(pendingIntentDelete)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setFullScreenIntent(pendingIntentClick, true)
                        .setContentIntent(pendingIntentClick);

        Notification notification = new NotificationCompat.BigPictureStyle(builder)
                .bigPicture(Utils.getBitmapFromLocalFile(FileUtils.getMediaDirectory() + "/" + type.getNotificationBigPictureFileName()))
                .build();

        android.app.NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationConstants.THEME_NOTIFICATION_ID,notification);
        HSPreferenceHelper.getDefault().putInt(NotificationConstants.THEME_NOTIFICATION_SESSION_ID, SessionMgr.getInstance().getCurrentSessionId() + 1);
        saveThemeNotificationSent(type);
        HSPreferenceHelper.getDefault().putBoolean(NotificationConstants.THEME_NOTIFICATION_IS_NEW_THEME, isNewTheme);
        if (isNewTheme) {
            HSAnalytics.logEvent("Colorphone_LocalPush_NewTheme_Show",
                    "ThemeName", type.getName(), "isDownloaded", String.valueOf(isMp4Downloaded));
            NotificationAutoPilotUtils.logNewThemeNotificationShow();
        } else {
            HSAnalytics.logEvent("Colorphone_LocalPush_OldTheme_Show",
                    "ThemeName", type.getName(), "isDownloaded", String.valueOf(isMp4Downloaded));
            NotificationAutoPilotUtils.logOldThemeNotificationShow();
        }
        HSPreferenceHelper.getDefault().putLong(NotificationConstants.PREFS_NOTIFICATION_SHOWED_LAST_TIME, System.currentTimeMillis());
    }

    private static void fillNotificationActionIntent(Intent intent, boolean isNewTheme, boolean isMp4Downloaded, Theme type) {
        intent.putExtra(NotificationConstants.THEME_NOTIFICATION_IS_NEW_THEME, isNewTheme);
        intent.putExtra(NotificationConstants.THEME_NOTIFICATION_KEY, NotificationConstants.THEME_NOTIFICATION_ID);
        intent.putExtra(NotificationConstants.THEME_NOTIFICATION_THEME_NAME, type.getName());
        intent.putExtra(NotificationConstants.THEME_NOTIFICATION_MP4_DOWNLOADED, isMp4Downloaded);
        intent.putExtra(NotificationConstants.THEME_NOTIFICATION_THEME_INDEX, type.getIndex());
    }
    private static Bitmap getLargeIconBitmap(Context context, Theme theme) {
        if (NotificationAutoPilotUtils.getPushIconType().equals("ThemeIcon")) {
            return Utils.getBitmapFromLocalFile(FileUtils.getMediaDirectory() + "/" + theme.getNotificationLargeIconFileName());
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_icon);
        }
    }

    @DrawableRes
    private static int getSmallIconRes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return R.drawable.notification_small_icon_v24;
        } else {
            return R.drawable.notification_small_icon;
        }
    }

    public static void showOldThemeNotificationIfProper() {
        if (!NotificationAutoPilotUtils.isOldThemeNotificationEnabled()) {
            return;
        }

        if (!isShowOldThemeNotificationAtValidInterval()) {
            return;
        }
        if (isAppOpenedInLastSeveralDays()) {
            return;
        }
        final Theme notificationType = getOldThemeType();

        HSLog.d(TAG, notificationType == null ? "notification null" : "old theme start load");
        if (notificationType != null) {

            HSLog.d(TAG, "notificationType old theme " + " id = "+notificationType.getId() + "name = " + notificationType.getName());
            downLoadPreviewImage(notificationType, new ThemeNotificationListener() {
                @Override
                public void onFailed() {
                }

                @Override
                public void onSuccess(Theme type) {
                    HSLog.d(TAG, "start download preview image");
                    onOldThemePreviewImageDownloaded(type);
                }
            });
        }
    }

    private static void onOldThemePreviewImageDownloaded(Theme type) {
        downloadMedia(type, new ThemeNotificationListener() {
            @Override
            public void onFailed() {

            }

            @Override
            public void onSuccess(Theme type) {
                doShowNotification(type, false);
            }
        });
    }

    private static boolean isShowOldThemeNotificationAtValidInterval() {
        HSLog.d(TAG, "showOldThemeAtValidInterval");

        long interval = ((int) NotificationAutoPilotUtils.getOldThemeNotificationShowInterval())
                            * DateUtils.DAY_IN_MILLIS;

        if (System.currentTimeMillis() -
                HSPreferenceHelper.getDefault().getLong(NotificationConstants.PREFS_NOTIFICATION_SHOWED_LAST_TIME, 0)
                > interval) {

            HSLog.d(TAG, "showOldThemeAtValidInterval  valid");
            return true;
        }
        HSLog.d(TAG, "showOldThemeAtValidInterval  invalid");
        return false;
    }

    private static boolean isAppOpenedInLastSeveralDays() {
        long interval = ((int) NotificationAutoPilotUtils.getOldThemeNotificationShowIntervalByOpenApp())
                            * DateUtils.DAY_IN_MILLIS;

        if (System.currentTimeMillis() -
                HSPreferenceHelper.getDefault().getLong(NotificationConstants.PREFS_APP_OPENED_TIME, 0)
                > interval) {
            HSLog.d(TAG, "app not opened  should show notification");
            return false;
        }
        HSLog.d(TAG, "should not show notification");
        return true;
    }

    private static Theme getOldThemeType() {
        int maxId = HSPreferenceHelper.getDefault().getInt(NotificationConstants.PREFS_NOTIFICATION_OLD_MAX_ID, 26);
        Theme notificationType = null;
        int tempIndex = Integer.MAX_VALUE;
        for (Theme theme : Theme.themes()) {
            if (theme.getId() < maxId
                    && !theme.isHot()
                    && theme.isMedia()
                    && theme.isNotificationEnabled()
                    && tempIndex > theme.getIndex()
                    && !isThemeNotificationSentEver(theme)
                    && !ThemePreviewView.isThemeAppliedEver(theme.getId())) {
                tempIndex = theme.getIndex();
                notificationType = theme;
            }
        }
        return notificationType;
    }

}

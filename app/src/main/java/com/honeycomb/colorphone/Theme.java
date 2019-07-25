package com.honeycomb.colorphone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.text.TextUtils;

import com.acb.call.themes.Type;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.Locale;

/**
 * Color phone theme.
 */

public class Theme extends Type {
    public static final String CONFIG_DOWNLOAD_NUM = "DownloadNum";
    public static final String CONFIG_RINGTONE = "Ringtone";
    public static final String CONFIG_UPLOADER = "Nickname";

    private static final String PREFS_FILE_THEME_LOCK_STATE = "prefs_theme_lock_state_file";
    private static final String PREFS_KEY_THEME_LOCK_ID_USER_UNLOCK_PREFIX ="prefs_theme_lock_id_prefix";

    private static final int LOCK_THEME_VERSION_CODE = 26;

    private long download;
    private boolean isSelected;
    private boolean isLike;
    private boolean isNotificationEnabled;
    private boolean isLocked;
    private boolean isSpecialTopic;
    private boolean canDownload;
    // User selected but file not ready
    // (e.g Theme file is downloading or download fail)
    private boolean pendingSelected;

    private String ringtoneUrl;
    private String ringtonePath;

    private int avatar;
    private String avatarName;
    private String notificationLargeIconUrl;
    private String notificationLargePictureUrl;

    private String uploaderName;

    public static int RANDOM_THEME = 10000;

    public long getDownload() {
        return download;
    }

    public void setDownload(long download) {
        this.download = download;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean enabled) {
        isNotificationEnabled = enabled;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public String getNotificationLargeIconUrl() {
        return notificationLargeIconUrl;
    }

    public void setNotificationLargeIconUrl(String notificationLargeIconUrl) {
        this.notificationLargeIconUrl = notificationLargeIconUrl;
    }

    public String getNotificationBigPictureUrl() {
        return notificationLargePictureUrl;
    }

    public void setNotificationBigPictureUrl(String notificationLargePictureUrl) {
        this.notificationLargePictureUrl = notificationLargePictureUrl;
    }

    public String getNotificationBigPictureFileName() {
        return getFileName() + "_BIG_IMAGE";
    }

    public String getNotificationLargeIconFileName() {
        return getFileName() + "_LARGE_ICON";
    }

    public Drawable getThemePreviewDrawable() {
        if (getValue() != LED && getValue() != TECH) {
            int colorIndex = getIndex() % COLORS.length;
            return new ColorDrawable(Color.parseColor(COLORS[colorIndex]));
        }
        return null;
    }

    @AnyRes
    private static int getIdentifier(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    public void configAvatar() {
        boolean isChinese = Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage());
        if (getValue() == Type.NONE) {
            setAvatarName(isChinese ? "系统来电" : "System");
            setAvatar(R.drawable.acb_phone_theme_none_default);
        } else if (getValue() == Type.TECH) {
            setAvatarName("Alexis");
            setAvatar(R.drawable.acb_phone_theme_default_technological_caller_avatar);
        } else {
            setAvatar(avatars[getIndex() % avatars.length]);
            setAvatarName(avatarNames[getIndex() % avatarNames.length]);
        }
    }

    private static int[] avatars = new int[]{
            R.drawable.theme_preview_avatar_default,
    };

    private static String[] avatarNames = new String[]{
            HSApplication.getContext().getString(R.string.app_name),
    };

    private static String[] COLORS = new String[]{
            "#ff9af6e1",
            "#fffae997",
            "#ffa4ffb1",
            "#ffffb7a4",
            "#ffa4efff",
            "#ffa4c0ff",
    };


    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getRingtoneUrl() {
        return ringtoneUrl;
    }

    public void setRingtoneUrl(String ringtoneUrl) {
        this.ringtoneUrl = ringtoneUrl;
    }

    public boolean hasRingtone() {
        return !TextUtils.isEmpty(ringtoneUrl) && Ap.Ringtone.isEnable();
    }

    public String getRingtonePath() {
        return ringtonePath;
    }

    public void setRingtonePath(String ringtonePath) {
        this.ringtonePath = ringtonePath;
    }

    public boolean isLocked() {
        if (!HSApplication.getContext().getPackageName().equals("com.colorphone.smooth.dialer")) {
            return false;
        }
        if (HSApplication.getFirstLaunchInfo().appVersionCode >= LOCK_THEME_VERSION_CODE) {
            return isLocked;
        }
        return false;
    }

    public void setLocked(boolean locked) {
        Preferences file = Preferences.get(PREFS_FILE_THEME_LOCK_STATE);
        boolean userUnLock = file.getBoolean(PREFS_KEY_THEME_LOCK_ID_USER_UNLOCK_PREFIX + getId(), false);
        if (userUnLock) {
            isLocked = false;
        } else if (!locked){
            isLocked = false;
            file.putBoolean(PREFS_KEY_THEME_LOCK_ID_USER_UNLOCK_PREFIX + getId(), true);
        } else {
            isLocked = true;
        }
    }

    public boolean canBeDownloaded() {
        return canDownload;
    }

    public void setCanDownload(boolean canDownload) {
        this.canDownload = canDownload;
    }


    public boolean isSpecialTopic() {
        return isSpecialTopic;
    }

    public void setSpecialTopic(boolean specialTopic) {
        isSpecialTopic = specialTopic;
    }

    public void setPendingSelected(boolean pendingSelected) {
        HSLog.d("AP-ScreenFlash", getIdName() + " setPendingSelected " + pendingSelected);
        this.pendingSelected = pendingSelected;
    }

    public boolean isPendingSelected() {
        return pendingSelected;
    }

    @Override
    public String toString() {
        return getId() + "-" + getName();
    }
}

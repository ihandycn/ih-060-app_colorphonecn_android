package com.honeycomb.colorphone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyRes;
import android.text.TextUtils;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Color phone theme.
 */

public class Theme extends Type {

    public static final String CONFIG_DOWNLOAD_NUM = "DownloadNum";
    public static final String CONFIG_RINGTONE = "Mp3";

    private static final boolean DEBUG_THEME_CHANGE = BuildConfig.DEBUG & false;

    private long download;
    private boolean isSelected;
    private boolean isLike;
    private boolean isNotificationEnabled;
    private String ringtoneUrl;

    private int avatar;
    private String avatarName;
    private String notificationLargeIconUrl;
    private String notificationLargePictureUrl;
    private static ArrayList<Theme> themes = new ArrayList<>(30);

    private static Handler mTestHandler = new Handler(Looper.getMainLooper());
    private static Runnable sTestRunnable = new Runnable() {
        @Override
        public void run() {
            Iterator<Theme> iter = themes.iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
                HSLog.d("THEME", "Test size --, current size = " + themes.size());
                HSGlobalNotificationCenter.sendNotification(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME);
                mTestHandler.postDelayed(this, 4000);
            }

        }
    };

    public static ArrayList<Theme> themes() {
        if (themes.isEmpty()) {
            updateThemes();
        }
        return themes;
    }

    public static void updateThemes() {
        updateTypes();
        themes.clear();
        for (Type type : Type.values()) {
            if (type.getValue() == NONE) {
                continue;
            }
            themes.add((Theme) type);
        }
        HSGlobalNotificationCenter.sendNotification(NotificationConstants.NOTIFICATION_REFRESH_MAIN_FRAME);

        if (DEBUG_THEME_CHANGE) {
            mTestHandler.postDelayed(sTestRunnable, 8000);
        }
    }

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
        if (getValue() == Type.TECH) {
            setAvatarName("Alexis");
            setAvatar(R.drawable.acb_phone_theme_default_technological_caller_avatar);
        } else {
            setAvatar(avatars[getIndex() % avatars.length]);
            setAvatarName(avatarNames[getIndex() % avatarNames.length]);
        }
    }

    private static int[] avatars = new int[]{
            R.drawable.female_3,
            R.drawable.female_5,
            R.drawable.male_1,
            R.drawable.female_2,
            R.drawable.female_4,
            R.drawable.male_2,
            R.drawable.female_1,
            R.drawable.male_3,
    };

    private static String[] avatarNames = new String[]{
            "Grace",
            "Ava",
            "Jackson",
            "Isabella",
            "Harper",
            "Noah",
            "Emma",
            "Oliver",
    };

    private static String[] COLORS = new String[]{
            "#ff9af6e1",
            "#fffae997",
            "#ffa4ffb1",
            "#ffffb7a4",
            "#ffa4efff",
            "#ffa4c0ff",
    };

    public String getRingtoneUrl() {
        return ringtoneUrl;
    }

    public void setRingtoneUrl(String ringtoneUrl) {
        this.ringtoneUrl = ringtoneUrl;
    }

    public boolean hasRingtone() {
        return !TextUtils.isEmpty(ringtoneUrl);
    }
}

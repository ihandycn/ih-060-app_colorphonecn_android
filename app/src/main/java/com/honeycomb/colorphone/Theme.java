package com.honeycomb.colorphone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;

import com.acb.call.themes.Type;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;

import static android.R.attr.type;

/**
 * Color phone theme.
 */

public class Theme extends Type {

    public static final String CONFIG_DOWNLOAD_NUM = "DownloadNum";

    private long download;
    private boolean isSelected;
    private boolean isLike;
    private boolean isNotificationEnabled;
    private int avatar;
    private String avatarName;
    private String notificationLargeIconUrl;
    private String notificationLargePictureUrl;

    public static ArrayList<Theme> themes() {
        ArrayList<Theme> themes = new ArrayList<>(30);
        for (Type type : Type.values()) {
            if (type.getValue()== NONE) {
                continue;
            }
            themes.add((Theme) type);
        }
        return themes;
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
}

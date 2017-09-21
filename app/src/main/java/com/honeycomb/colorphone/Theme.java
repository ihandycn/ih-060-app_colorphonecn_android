package com.honeycomb.colorphone;

import com.acb.call.themes.Type;

/**
 * Color phone theme.
 */

public class Theme extends Type {

    public static final String CONFIG_DOWNLOAD_NUM = "DownloadNum";

    private long download;
    private boolean isSelected;
    private boolean isLike;
    private int avatar;
    private String avatarName;

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
            R.drawable.female_4,
            R.drawable.male_1,
            R.drawable.female_2,
            R.drawable.female_3,
            R.drawable.male_2,
            R.drawable.female_1,
            R.drawable.male_3,
            R.drawable.male_4,
    };

    private static String[] avatarNames = new String[]{
            "Grace",
            "Jackson",
            "Isabella",
            "Harper",
            "Noah",
            "Emma",
            "Oliver",
            "Lucas",

    };
}

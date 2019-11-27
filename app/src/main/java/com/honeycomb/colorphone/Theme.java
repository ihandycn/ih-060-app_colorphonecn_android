package com.honeycomb.colorphone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.text.TextUtils;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.http.bean.AllThemeBean;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Color phone theme.
 */

public class Theme extends Type {
    public static final String CONFIG_DOWNLOAD_NUM = "DownloadNum";
    public static final String CONFIG_RINGTONE = "Ringtone";
    public static final String CONFIG_UPLOADER = "Nickname";

    private static final String PREFS_FILE_THEME_LOCK_STATE = "prefs_theme_lock_state_file";
    private static final String PREFS_KEY_THEME_LOCK_ID_USER_UNLOCK_PREFIX = "prefs_theme_lock_id_prefix";

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

    private boolean isDeleteSelect = false;
    private static boolean isSetDefaultTheme = true;

    private String ringtoneUrl;
    private String ringtonePath;

    private int avatar;
    private String avatarName;
    private String notificationLargeIconUrl;
    private String notificationLargePictureUrl;

    private String uploaderName;

    private static Theme sFirstTheme;

    public static int RANDOM_THEME = 10000;

    public static Theme getFirstTheme() {
        return sFirstTheme;
    }

    public long getDownload() {
        return download;
    }

    public void setDownload(long download) {
        this.download = download;
    }

    public boolean isDeleteSelected() {
        return isDeleteSelect;
    }

    public void setDeleteSelected(boolean selected) {
        isDeleteSelect = selected;
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
        } else if (!locked) {
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

    public static ArrayList<Type> transformData(int beforeDataSize, AllThemeBean data) {
        ArrayList<Type> dataList = new ArrayList<>();
        for (AllThemeBean.ShowListBean bean : data.getShow_list()) {
            Theme theme = new Theme();
            theme.setIndex(beforeDataSize + dataList.size());
            theme.setId(bean.getShow_id());
            theme.setIdName(bean.getId_name());
            theme.setResType(bean.getRes_type());
            theme.setItemIcon(bean.getIcon());
            theme.setName(bean.getName());
            theme.setAcceptIcon(bean.getIcon_accept());
            theme.setRejectIcon(bean.getIcon_reject());
            theme.setPreviewImage(bean.getPreview_image());
            theme.setThemeGuideImage(bean.getTheme_guide_preview_image());
            theme.setMp4Url(bean.getMp4());
            theme.setGifUrl(bean.getGif());
            theme.setHot(bean.isHot());
            theme.setSuggestMediaType(Type.MEDIA_MP4);
            theme.setNotificationBigPictureUrl(bean.getLocal_push() != null ? bean.getLocal_push().getLocalPushPreviewImage() : "");
            theme.setNotificationLargeIconUrl(bean.getLocal_push() != null ? bean.getLocal_push().getLocalPushIcon() : "");
            theme.setNotificationEnabled(bean.getLocal_push() != null && bean.getLocal_push().isEnable());
            theme.setDownload(bean.getDownload_num());
            theme.setRingtoneUrl(bean.getRingtone());
            theme.setUploaderName(bean.getUser_name());
            theme.setLocked(bean.getStatus() != null && bean.getStatus().isLock());
            theme.setCanDownload(bean.getStatus() != null && bean.getStatus().isStaticPreview());
            theme.setSpecialTopic(false);
            theme.setAvatar(R.drawable.theme_preview_avatar_default);
            theme.setAvatarName(HSApplication.getContext().getString(R.string.app_name));

            if (isSetDefaultTheme) {
                isSetDefaultTheme = false;
                sFirstTheme = theme;
            }

            dataList.add(theme);
        }

        return dataList;
    }

    public static ArrayList<Theme> transformCategoryData(int beforeDataSize, AllThemeBean data) {
        ArrayList<Theme> dataList = new ArrayList<>();
        for (AllThemeBean.ShowListBean bean : data.getShow_list()) {
            Theme theme = new Theme();
            theme.setIndex(beforeDataSize + dataList.size());
            theme.setId(bean.getShow_id());
            theme.setIdName(bean.getId_name());
            theme.setResType(bean.getRes_type());
            theme.setItemIcon(bean.getIcon());
            theme.setName(bean.getName());
            theme.setAcceptIcon(bean.getIcon_accept());
            theme.setRejectIcon(bean.getIcon_reject());
            theme.setPreviewImage(bean.getPreview_image());
            theme.setThemeGuideImage(bean.getTheme_guide_preview_image());
            theme.setMp4Url(bean.getMp4());
            theme.setGifUrl(bean.getGif());
            theme.setHot(bean.isHot());
            theme.setSuggestMediaType(Type.MEDIA_MP4);
            theme.setNotificationBigPictureUrl(bean.getLocal_push() != null ? bean.getLocal_push().getLocalPushPreviewImage() : "");
            theme.setNotificationLargeIconUrl(bean.getLocal_push() != null ? bean.getLocal_push().getLocalPushIcon() : "");
            theme.setNotificationEnabled(bean.getLocal_push() != null && bean.getLocal_push().isEnable());
            theme.setDownload(bean.getDownload_num());
            theme.setRingtoneUrl(bean.getRingtone());
            theme.setUploaderName(bean.getUser_name());
            theme.setLocked(bean.getStatus() != null && bean.getStatus().isLock());
            theme.setCanDownload(bean.getStatus() != null && bean.getStatus().isStaticPreview());
            theme.setSpecialTopic(false);
            theme.setAvatar(R.drawable.theme_preview_avatar_default);
            theme.setAvatarName(HSApplication.getContext().getString(R.string.app_name));

            if (isSetDefaultTheme) {
                isSetDefaultTheme = false;
                sFirstTheme = theme;
            }

            dataList.add(theme);
        }

        return dataList;
    }

    public static ArrayList<Theme> transformData(AllUserThemeBean bean) {
        ArrayList<Theme> dataList = new ArrayList<>();
        if (bean.getShow_list() != null && bean.getShow_list().size() > 0) {
            for (AllUserThemeBean.ShowListBean item : bean.getShow_list()) {
                Theme theme = new Theme();
                theme.setIndex(dataList.size());
                theme.setId(item.getCustomize_show_id());
                theme.setIdName(item.getFile_name());
                theme.setResType("url");
                theme.setItemIcon("");
                theme.setName(item.getFile_name());
                theme.setAcceptIcon("http://cdn.ihandysoft.cn/light2019/apps/apkcolorphone/resource/thumbnail/defaultbutton/acb_phone_call_answer.png");
                theme.setRejectIcon("http://cdn.ihandysoft.cn/light2019/apps/apkcolorphone/resource/thumbnail/defaultbutton/acb_phone_call_refuse.png");
                theme.setPreviewImage(item.getImage_url());
                theme.setThemeGuideImage("");
                theme.setMp4Url(item.getVideo_url());
                theme.setGifUrl("");
                theme.setHot(false);
                theme.setSuggestMediaType(Type.MEDIA_MP4);
                theme.setNotificationBigPictureUrl("");
                theme.setNotificationLargeIconUrl("");
                theme.setNotificationEnabled(false);
                theme.setDownload(0);
                theme.setRingtoneUrl(item.getAudio_url());
                theme.setUploaderName("");
                theme.setLocked(false);
                theme.setCanDownload(true);
                theme.setSpecialTopic(false);
                theme.setAvatar(R.drawable.theme_preview_avatar_default);
                theme.setAvatarName(HSApplication.getContext().getString(R.string.app_name));

                dataList.add(theme);
            }
        }
        return dataList;
    }

    public static final int THEME_DEFAULT_LENGTH = 25;

    public String toPrefString() {
        return getIndex() + SEPARATOR +
                getId() + SEPARATOR +
                getIdName() + SEPARATOR +
                getResType() + SEPARATOR +
                getItemIcon() + SEPARATOR +
                getName() + SEPARATOR +
                getAcceptIcon() + SEPARATOR +
                getRejectIcon() + SEPARATOR +
                getPreviewImage() + SEPARATOR +
                getThemeGuideImage() + SEPARATOR +
                getMp4Url() + SEPARATOR +
                getGifUrl() + SEPARATOR +
                (isHot() ? "true" : "false") + SEPARATOR +
                getSuggestMediaType() + SEPARATOR +
                getNotificationBigPictureUrl() + SEPARATOR +
                getNotificationLargeIconUrl() + SEPARATOR +
                (isNotificationEnabled() ? "true" : "false") + SEPARATOR +
                getDownload() + SEPARATOR +
                getRingtoneUrl() + SEPARATOR +
                getUploaderName() + SEPARATOR +
                (isLocked() ? "true" : "false") + SEPARATOR +
                (canBeDownloaded() ? "true" : "false") + SEPARATOR +
                (isSpecialTopic() ? "true" : "false") + SEPARATOR +
                getAvatar() + SEPARATOR +
                getAvatarName();
    }

    public static Theme valueOfPrefString(String prefString) {
        if (TextUtils.isEmpty(prefString)) {
            return null;
        }
        String[] array = prefString.split(SEPARATOR);
        if (array.length == THEME_DEFAULT_LENGTH) {
            return valueOfThemePrefString(array);
        } else if (array.length == TYPE_DEFAULT_LENGTH) {
            return valueOfTypePrefString(array);
        } else {
            return null;
        }
    }

    public static Theme valueOfThemePrefString(String[] array) {
        try {
            Theme theme = new Theme();
            theme.setIndex(Integer.valueOf(array[0]));
            theme.setId(Integer.valueOf(array[1]));
            theme.setIdName(array[2]);
            theme.setResType(array[3]);
            theme.setItemIcon(array[4]);
            theme.setName(array[5]);
            theme.setAcceptIcon(array[6]);
            theme.setRejectIcon(array[7]);
            theme.setPreviewImage(array[8]);
            theme.setThemeGuideImage(array[9]);
            theme.setMp4Url(array[10]);
            theme.setGifUrl(array[11]);
            theme.setHot(array[12].equals("true"));
            theme.setSuggestMediaType(Integer.valueOf(array[13]));
            theme.setNotificationBigPictureUrl(array[14]);
            theme.setNotificationLargeIconUrl(array[15]);
            theme.setNotificationEnabled(array[16].equals("true"));
            theme.setDownload(Integer.valueOf(array[17]));
            theme.setRingtoneUrl(array[18]);
            theme.setUploaderName(array[19]);
            theme.setLocked(array[20].equals("true"));
            theme.setCanDownload(array[21].equals("true"));
            theme.setSpecialTopic(array[22].equals("true"));
            theme.setAvatar(Integer.valueOf(array[23]));
            theme.setAvatarName(array[24]);
            return theme;
        } catch (Exception e) {
            return null;
        }
    }

    public static Theme valueOfTypePrefString(String[] array) {
        try {
            Theme theme = new Theme();
            theme.setIndex(Integer.valueOf(array[0]));
            theme.setId(Integer.valueOf(array[1]));
            theme.setIdName(array[2]);
            theme.setResType(array[3]);
            theme.setItemIcon(array[4]);
            theme.setName(array[5]);
            theme.setAcceptIcon(array[6]);
            theme.setRejectIcon(array[7]);
            theme.setPreviewImage(array[8]);
            theme.setThemeGuideImage(array[9]);
            theme.setMp4Url(array[10]);
            theme.setGifUrl(array[11]);
            theme.setHot(array[12].equals("true"));
            theme.setSuggestMediaType(Integer.valueOf(array[13]));
            return theme;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return getId() + "-" + getName();
    }
}

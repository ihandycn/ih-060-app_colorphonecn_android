package com.honeycomb.colorphone.customize;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;

import org.json.JSONObject;

import java.util.Map;

public class WallpaperInfo implements Parcelable {

    public static final int WALLPAPER_TYPE_NONE = -1;
    public static final int WALLPAPER_TYPE_BUILT_IN = 0;
    public static final int WALLPAPER_TYPE_ONLINE = 1;
    public static final int WALLPAPER_TYPE_GALLERY = 2;
    public static final int WALLPAPER_TYPE_LUCKY = 3;
    public static final int WALLPAPER_TYPE_3D = 4;
    public static final int WALLPAPER_TYPE_LIVE = 5;
    public static final int WALLPAPER_TYPE_VIDEO = 6;

    private static final String JSON_KEY_IS_BUILT_IN = "isBuiltIn"; // Added in 24 (v1.2.0)
    private static final String JSON_KEY_URL = "wallpaperUrl";
    private static final String JSON_KEY_THUMB = "thumbnailUrl";
    private static final String JSON_KEY_BUILT_IN_DRAWABLE_NAME = "builtInDrawableName"; // Added in 24 (v1.2.0)

    // Before 24 (v1.2.0)
    private static final String LEGACY_BUILT_IN_DRAWABLE_NAME = "wallpaper";

    private int mType = WALLPAPER_TYPE_NONE;

    /**
     * For online wallpapers mSource is hdUrl.
     * For gallery wallpapers mSource is original path.
     * For 3D / live wallpapers mSource is the wallpaper name (eg. "balloon").
     */
    private String mSource;
    private String mVideoUrl;

    private String mThumbnailUrl;
    private CategoryInfo mCategory;

    private int mBuiltInDrawableId = 0;

    private String mPath;

    private int mPopularity;
    /**
     * format as follow:
     * scaleX, scaleY, skewX, skewY, transX, transY
     */
    private String mEdit;

    private long mCreateTime = -1;
    private long mEditTime = -1;

    private boolean mIsApplied;
    private boolean mIsBoutique;
    private boolean mIsSpecialDay;
    private Boolean mIsTextLight;

    private WallpaperInfo() {
    }

    public static WallpaperInfo newWallpaper(int type, String path, String source) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = type;
        info.mPath = path;
        info.mSource = source;
        return info;
    }

    public static WallpaperInfo newBuiltInWallpaper(@DrawableRes int builtInWallpaperResId) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_BUILT_IN;
        info.mBuiltInDrawableId = builtInWallpaperResId;
        return info;
    }

    public static WallpaperInfo newOnlineWallpaper(String url, String thumb) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_ONLINE;
        info.mSource = url;
        info.mThumbnailUrl = thumb;
        return info;
    }

    public static WallpaperInfo newOnlineWallpaper(String url, String thumb, String path, int popularity) {
        WallpaperInfo info = newOnlineWallpaper(url, thumb);
        info.mPath = path;
        info.mPopularity = popularity;
        return info;
    }

    public static WallpaperInfo newGalleryWallpaper(String path) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_GALLERY;
        info.mPath = path;
        return info;
    }

    public static WallpaperInfo newLuckyWallpaper(String url, String thumb, String path) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_LUCKY;
        info.mSource = url;
        info.mThumbnailUrl = thumb;
        info.mPath = path;
        return info;
    }


    public static WallpaperInfo newVideoWallpaper(String url, String thumb) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_VIDEO;
        info.mSource = url;
        info.mThumbnailUrl = thumb;
        return info;
    }

    public static WallpaperInfo new3DWallpaper(String name) {
        return new3DWallpaper(name, "");
    }

    public static WallpaperInfo new3DWallpaper(String name, String videoUrl) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_3D;
        info.mSource = name;
        info.mVideoUrl = videoUrl;
        return info;
    }

    @SuppressWarnings("unchecked")
    public static WallpaperInfo newLiveWallpaper(String name) {
        return newLiveWallpaper(name, "");
    }

    public static WallpaperInfo newLiveWallpaper(String name, String videoUrl) {
        WallpaperInfo info = new WallpaperInfo();
        info.mType = WALLPAPER_TYPE_LIVE;
        info.mSource = name;
        info.mVideoUrl = videoUrl;
        return info;
    }

    private WallpaperInfo(Parcel in) {
        mType = in.readInt();
        mSource = in.readString();
        mThumbnailUrl = in.readString();
        mCategory = in.readParcelable(CategoryInfo.class.getClassLoader());
        mBuiltInDrawableId = in.readInt();
        mPath = in.readString();
        mEdit = in.readString();
        mCreateTime = in.readLong();
        mEditTime = in.readLong();
        mIsApplied = in.readByte() != 0;
        mPopularity = in.readInt();
    }

    public static final Creator<WallpaperInfo> CREATOR = new Creator<WallpaperInfo>() {
        @Override
        public WallpaperInfo createFromParcel(Parcel in) {
            return new WallpaperInfo(in);
        }

        @Override
        public WallpaperInfo[] newArray(int size) {
            return new WallpaperInfo[size];
        }
    };

    /**
     * Local wallpapers were serialized in JSON format in v1.2.8 (33) and earlier versions.
     */
    public static
    @Nullable
    WallpaperInfo valueOf(JSONObject jsonData) {
        if (jsonData == null) {
            return null;
        }
        WallpaperInfo info = null;

        boolean isBuiltIn = jsonData.optBoolean(JSON_KEY_IS_BUILT_IN, false);
        String url = jsonData.optString(JSON_KEY_URL);
        String thumbnailUrl = jsonData.optString(JSON_KEY_THUMB);
        String buildInDrawableName = jsonData.optString(JSON_KEY_BUILT_IN_DRAWABLE_NAME);

        // Maintain compatibility
        if (!isBuiltIn && url.isEmpty() && thumbnailUrl.isEmpty()) {
            isBuiltIn = true;
            buildInDrawableName = LEGACY_BUILT_IN_DRAWABLE_NAME;
        }

        if (isBuiltIn) {
            Context context = HSApplication.getContext();
            Resources res = context.getResources();
            @DrawableRes int drawableResId = res.getIdentifier(buildInDrawableName, "drawable", context.getPackageName());
            if (drawableResId > 0) {
                info = WallpaperInfo.newBuiltInWallpaper(drawableResId);
            }
        } else {
            info = WallpaperInfo.newOnlineWallpaper(url, thumbnailUrl);
        }
        return info;
    }

    void onAdd() {
        mCreateTime = System.currentTimeMillis();
    }

    public void onEdit() {
        mEditTime = System.currentTimeMillis();
    }

    public WallpaperInfo(@NonNull Cursor cursor) {
        int typeIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_TYPE);
        int drawableNameIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_DRAWABLE_NAME);
        int thumbnailUrlIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_THUMBNAIL_URL);
        int hdUrlIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_HD_URL);
        int pathIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_PATH);
        int editIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_EDIT);
        int createTimeIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_CREATE_TIME);
        int editTimeIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_EDIT_TIME);
        int isCurrentIndex = cursor.getColumnIndex(WallpaperProvider.COLUMN_IS_APPLIED);
        if (typeIndex != -1) {
            mType = cursor.getInt(typeIndex);
        }
        if (drawableNameIndex != -1) {
            Context context = HSApplication.getContext();
            Resources res = context.getResources();
            String drawableName = cursor.getString(drawableNameIndex);
            mBuiltInDrawableId = res.getIdentifier(drawableName, "drawable", context.getPackageName());
        }
        if (thumbnailUrlIndex != -1) {
            mThumbnailUrl = cursor.getString(thumbnailUrlIndex);
        }
        if (hdUrlIndex != -1) {
            mSource = cursor.getString(hdUrlIndex);
        }
        if (pathIndex != -1) {
            mPath = cursor.getString(pathIndex);
        }
        if (editIndex != -1) {
            mEdit = cursor.getString(editIndex);
        }
        if (createTimeIndex != -1) {
            mCreateTime = cursor.getLong(createTimeIndex);
        }
        if (editTimeIndex != -1) {
            mEditTime = cursor.getLong(editTimeIndex);
        }
        if (isCurrentIndex != -1) {
            mIsApplied = cursor.getInt(isCurrentIndex) > 0;
        }
    }

    void onAddToDatabase(ContentValues values) {
        if (mType != WALLPAPER_TYPE_NONE) {
            values.put(WallpaperProvider.COLUMN_TYPE, mType);
        }
        if (mBuiltInDrawableId != 0) {
            String drawableName = WallpaperMgr.DRAWABLE_NAME_MAP.get(mBuiltInDrawableId);
            if (!TextUtils.isEmpty(drawableName)) {
                values.put(WallpaperProvider.COLUMN_DRAWABLE_NAME, drawableName);
            }
        }
        if (!TextUtils.isEmpty(mThumbnailUrl)) {
            values.put(WallpaperProvider.COLUMN_THUMBNAIL_URL, mThumbnailUrl);
        }
        if (!TextUtils.isEmpty(mSource)) {
            values.put(WallpaperProvider.COLUMN_HD_URL, mSource);
        }
        if (!TextUtils.isEmpty(mPath)) {
            values.put(WallpaperProvider.COLUMN_PATH, mPath);
        }
        if (!TextUtils.isEmpty(mEdit)) {
            values.put(WallpaperProvider.COLUMN_EDIT, mEdit);
        }
        if (mCreateTime != -1) {
            values.put(WallpaperProvider.COLUMN_CREATE_TIME, mCreateTime);
        }
        if (mEditTime != -1) {
            values.put(WallpaperProvider.COLUMN_EDIT_TIME, mEditTime);
        }
        if (mIsApplied) {
            values.put(WallpaperProvider.COLUMN_IS_APPLIED, mIsApplied);
        }
    }

    public int getType() {
        return mType;
    }

    public String getSource() {
        return mSource;
    }

    public String getThumbnailUrl() {
        switch (mType) {
            case WALLPAPER_TYPE_3D:
                return CustomizeUtils.generate3DWallpaperThumbnailUrl(mSource);
            case WALLPAPER_TYPE_LIVE:
                return CustomizeUtils.generateLiveWallpaperThumbnailUrl(mSource);
            default:
                return mThumbnailUrl;
        }
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    /**
     * Get base download URL for 3D / live wallpapers.
     *
     * @return Empty string if feature is not enabled.
     */
    private static String getBaseUrl(String configName) {
        Map<String, ?> configMap = CustomizeConfig.getMap(configName);
        return HSMapUtils.optString(configMap, "", "BaseUrl");
    }

    public int getPopularity() {
        return mPopularity;
    }

    public boolean isApplied() {
        return mIsApplied;
    }

    public boolean isSpecialDay() {
        return mIsSpecialDay;
    }

    public boolean isBoutique() {
        return mIsBoutique;
    }

    public void setApplied(boolean isApplied) {
        mIsApplied = isApplied;
    }

    public String getEdit() {
        return mEdit;
    }

    public void setEdit(@NonNull String edit) {
        if (WallpaperInfo.WALLPAPER_TYPE_GALLERY == mType) {
            mEdit = edit;
        }
    }

    public
    @DrawableRes
    int getBuiltInDrawableId() {
        return mBuiltInDrawableId;
    }

    public String getPath() {
        return mPath;
    }

    public void setCategory(CategoryInfo category) {
        mCategory = category;
    }

    public CategoryInfo getCategory() {
        return mCategory;
    }

    private String getFileName() {
        return mSource.substring(mSource.lastIndexOf("/") + 1);
    }

    public String getName() {
        if (mType == WallpaperInfo.WALLPAPER_TYPE_BUILT_IN)
            return "default";

        if (mType == WALLPAPER_TYPE_GALLERY) {
            return mPath;
        }
        return getFileName();
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (o instanceof WallpaperInfo) {
            WallpaperInfo other = (WallpaperInfo) o;
            if (mType == WALLPAPER_TYPE_BUILT_IN && other.mType == WALLPAPER_TYPE_BUILT_IN
                    && mBuiltInDrawableId == other.mBuiltInDrawableId) {
                return true;
            }
            if (mType == WALLPAPER_TYPE_ONLINE && other.mType == WALLPAPER_TYPE_ONLINE
                    && TextUtils.equals(mThumbnailUrl, other.mThumbnailUrl)
                    && TextUtils.equals(mSource, other.mSource)) {
                return true;
            }
            if (mType == WALLPAPER_TYPE_GALLERY && other.mType == WALLPAPER_TYPE_GALLERY
                    && (TextUtils.equals(mPath, other.mPath) || TextUtils.equals(mSource, other.mSource))) {
                return true;
            }
            if (mType == WALLPAPER_TYPE_3D && other.mType == WALLPAPER_TYPE_3D
                    && TextUtils.equals(mSource, other.mSource)) {
                return true;
            }

            if (mType == WALLPAPER_TYPE_LIVE && other.mType == WALLPAPER_TYPE_LIVE
                    && TextUtils.equals(mSource, other.mSource)) {
                return true;
            }

            if (mType == WALLPAPER_TYPE_LUCKY && other.mType == WALLPAPER_TYPE_LUCKY
                    && TextUtils.equals(mThumbnailUrl, other.mThumbnailUrl)
                    && TextUtils.equals(mSource, other.mSource)
                    && TextUtils.equals(mPath, other.mPath)) {
                return true;
            }
        }
        return false;
    }

    String createDbSelectionQuery() {
        // Built in
        return "(" + WallpaperProvider.COLUMN_TYPE + " = " + WALLPAPER_TYPE_BUILT_IN + " AND " +
                WallpaperProvider.COLUMN_DRAWABLE_NAME + " = " + wrapString(WallpaperMgr.DRAWABLE_NAME_MAP.get(mBuiltInDrawableId)) + ")" +

                // Online
                " OR (" + WallpaperProvider.COLUMN_TYPE + " = " + WALLPAPER_TYPE_ONLINE + " AND " +
                WallpaperProvider.COLUMN_THUMBNAIL_URL + " = " + wrapString(mThumbnailUrl) +
                " AND " + WallpaperProvider.COLUMN_HD_URL + " = " + wrapString(mSource) + ")" +

                // Lucky
                " OR (" + WallpaperProvider.COLUMN_TYPE + " = " + WALLPAPER_TYPE_LUCKY + " AND " +
                WallpaperProvider.COLUMN_THUMBNAIL_URL + " = " + wrapString(mThumbnailUrl) +
                " AND " + WallpaperProvider.COLUMN_HD_URL + " = " + wrapString(mSource) + ")" +

                // Local
                " OR (" + WallpaperProvider.COLUMN_TYPE + " = " + WALLPAPER_TYPE_GALLERY + " AND " +
                WallpaperProvider.COLUMN_PATH + " = " + wrapString(mPath) + ")";
    }

    private static String wrapString(String str) {
        if (str == null) {
            str = "";
        }
        return "\'" + str + "\'";
    }

    @Override
    public String toString() {
        return "WallpaperInfo type " + mType + " mSource=" + mSource + ", mThumbnailUrl=" + mThumbnailUrl + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
        dest.writeString(mSource);
        dest.writeString(mThumbnailUrl);
        dest.writeParcelable(mCategory, flags);
        dest.writeInt(mBuiltInDrawableId);
        dest.writeString(mPath);
        dest.writeString(mEdit);
        dest.writeLong(mCreateTime);
        dest.writeLong(mEditTime);
        dest.writeByte((byte) (mIsApplied ? 1 : 0));
        dest.writeInt(mPopularity);
    }

    public boolean isTextLight() {
        return mIsTextLight == null ? false : mIsTextLight;
    }

    public void setTextLight(boolean light) {
        mIsTextLight = light;
    }

    public boolean textLightUnkown() {
        return mIsTextLight == null;
    }
}

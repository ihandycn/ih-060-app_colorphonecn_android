package com.honeycomb.colorphone.customize.theme;

import android.support.annotation.NonNull;

import com.acb.utils.Utils;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;

import net.appcloudbox.common.utils.AcbMapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Representative of a theme. Contains theme information.
 * <p>
 * This object is instantiated from a map obtained from configuration file with {@link #ofConfig(String, Map)}.
 * Use {@link #ofSystem()} to instantiate a system theme.
 * <p>
 * See under path "Customize", "Themes" of plist archive configuration (customize.la).
 */
public class ThemeInfo {

    /**
     * Package name of theme APK. Also used as unique identifier of a theme.
     * <p>
     * Special value of this field:
     * - {@link ThemeConstants#PRESET_THEME_IDENTIFIER} for launcher app preset theme.
     */
    public String packageName;

    /**
     * Displayed name of a theme.
     */
    public String themeName;

    /**
     * Short version theme description text. Displayed in the online theme gallery.
     */
    public String shortDescription;

    /**
     * Complete version theme description text. Displayed on theme detail page.
     */
    public String fullDescription;

    /**
     * URL for theme banner introductory image displayed in the online theme gallery.
     */
    public String bannerUrl;

    public String imageUrl;

    /**
     * URL for theme icon displayed in the online theme gallery.
     */
    public String iconUrl;

    /**
     * temporary variate
     */
    public int weight;

    public List<String> tags;

    /**
     * URLs for theme preview images displayed on theme detail page.
     */
    public List<String> previewImageUrls;

    /**
     * Last installed or modified time of theme package.
     */
    public long packageModifiedTime = 0;

    private boolean mOnlineDescriptionConfigured;

    /***
     * isBoutique
     */
    private boolean isBoutique;

    /**
     * Whether this theme is a 3D theme.
     */
    private boolean is3D;

    /**
     * Whether this theme is a live theme.
     */
    private boolean isLive;

    /**
     * iconProcessor way
     */
    public int iconProcessor;

    /**
     * hot
     */
    public int hot;

    /**
     * The unique update id of theme.( 37 is newer than 36)
     */
    public int updateId;

    /**
     * New or Hot badge.
     */
    public int badgeMode = BADGE_NONE;

    /**
     * Keys for configuration map.
     */
    public static final String CONFIG_KEY_NAME = "Name";
    public static final String CONFIG_KEY_SHORT_DESC = "ShortDescription";
    public static final String CONFIG_KEY_DESC = "Description";
    public static final String CONFIG_KEY_BANNER = "Banner";
    public static final String CONFIG_KEY_IMAGE = "Image";
    public static final String CONFIG_KEY_ICON = "Icon";
    public static final String CONFIG_KEY_PREVIEW_IMAGES = "PreviewImages";
    public static final String CONFIG_KEY_APK_URL = "ApkUrl";
    public static final String CONFIG_KEY_IS_BOUTIQUE = "Boutique";
    public static final String CONFIG_KEY_THEME_TYPE = "Type";
    public static final String CONFIG_KEY_ICON_PROCESSOR = "IconProcessor";
    public static final String CONFIG_KEY_UPDATE_ID = "UpdateId";
    public static final String CONFIG_KEY_HOT = "Hot";
    private static final String CONFIG_KEY_TAGS = "Tags";

    public static final int BADGE_NONE = 0;
    public static final int BADGE_HOT = 1;
    public static final int BADGE_NEW = 2;

    @SuppressWarnings("unchecked")
    public static @NonNull
    ThemeInfo ofConfig(String themePackage, Map<String, ?> onlineDescriptionConfigs) {
        ThemeInfo info = new ThemeInfo();
        info.packageName = themePackage;

        // Populate online descriptions if configured
        if (onlineDescriptionConfigs == null) {
            return info;
        }
        Map<String, ?> onlineDescriptionConfig = (Map<String, ?>) onlineDescriptionConfigs.get(themePackage);
        if (onlineDescriptionConfig != null && !onlineDescriptionConfig.isEmpty()) {
            info.mOnlineDescriptionConfigured = true;
            try {
                info.themeName = Utils.getMultilingualString(onlineDescriptionConfig, CONFIG_KEY_NAME);
                info.shortDescription = Utils.getMultilingualString(onlineDescriptionConfig, CONFIG_KEY_SHORT_DESC);
                info.fullDescription = Utils.getMultilingualString(onlineDescriptionConfig, CONFIG_KEY_DESC);
                info.bannerUrl = (String) onlineDescriptionConfig.get(CONFIG_KEY_BANNER);
                info.imageUrl = (String) onlineDescriptionConfig.get(CONFIG_KEY_IMAGE);
                info.iconUrl = (String) onlineDescriptionConfig.get(CONFIG_KEY_ICON);
                info.previewImageUrls = (List<String>) onlineDescriptionConfig.get(CONFIG_KEY_PREVIEW_IMAGES);
                info.isBoutique = (Boolean) onlineDescriptionConfig.get(CONFIG_KEY_IS_BOUTIQUE);
                String type= HSMapUtils.optString(onlineDescriptionConfig, "", CONFIG_KEY_THEME_TYPE);
                info.is3D = "3d".equalsIgnoreCase(type);
                info.isLive = "live".equalsIgnoreCase(type);
                info.updateId = AcbMapUtils.getInteger(onlineDescriptionConfig, CONFIG_KEY_UPDATE_ID);
                info.tags = (List<String>) onlineDescriptionConfig.get(CONFIG_KEY_TAGS);
                if (info.tags == null) {
                    info.tags = new ArrayList<>();
                }
                Object o = onlineDescriptionConfig.get(CONFIG_KEY_ICON_PROCESSOR);
                Object hotConfig = onlineDescriptionConfig.get(CONFIG_KEY_HOT);
                if (hotConfig != null) {
                    info.hot = AcbMapUtils.getInteger(onlineDescriptionConfig, CONFIG_KEY_HOT);
                }
                if (o != null) {
                    info.iconProcessor = AcbMapUtils.getInteger(onlineDescriptionConfig, CONFIG_KEY_ICON_PROCESSOR);
                }
            } catch (Exception e) {
                HSLog.w("Theme", "Error loading theme config, please check config format");
                e.printStackTrace();
                info.mOnlineDescriptionConfigured = false;
            }
        }
        return info;
    }

    public static ThemeInfo ofSystem() {
        ThemeInfo info = new ThemeInfo();
        info.packageName = ThemeConstants.PRESET_THEME_IDENTIFIER;
        return info;
    }

    /**
     * A package theme is a theme shipped with independent APK.
     */
    public boolean isPackageTheme() {
        return !isPresetTheme();
    }

    public boolean isBoutique() {
        return isBoutique;
    }

    public boolean is3D() {
        return is3D;
    }

    public boolean isLive() {
        return isLive;
    }

    /**
     * A system theme means no theme.
     */
    public boolean isPresetTheme() {
        return ThemeConstants.PRESET_THEME_IDENTIFIER.equals(packageName);
    }

    /**
     * If online description is configured in plist. Theme shall only be displayed in online gallery if this method
     * returns {@code true}.
     */
    public boolean isOnlineDescriptionConfigured() {
        return mOnlineDescriptionConfigured;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof String) {
            if (packageName != null) {
                return packageName.equals(o);
            }
        }
        if (o instanceof ThemeInfo) {
            if (packageName != null) {
                return packageName.equals(((ThemeInfo) o).packageName);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "Theme %s package: %s, description: %s (short %s), " +
                        "icon: %s, banner: %s, package modified time: %d",
                themeName, packageName, fullDescription, shortDescription,
                iconUrl, bannerUrl, packageModifiedTime);
    }
}

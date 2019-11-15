package com.honeycomb.colorphone.wallpaper.theme;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;

import com.honeycomb.colorphone.wallpaper.LauncherConstants;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.wallpaper.util.LocalConfig;
import com.ihs.commons.utils.HSLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource handle for a theme.
 * <p/>
 * Instantiated with theme package name (or launcher package name for preset theme) with {@link #ofTheme(Context, String)}.
 * <p/>
 * Use
 * {@link #getDrawable(String)}, {@link #getInteger(String)}, {@link #getString(String)},
 * {@link #getDimensionPixelSize(String)} to get resources of given name on theme package.
 * Use
 * {@link #getIcon(String)} to get themed app icon with given "icon identifier".
 */
public class ThemeResources implements Comparable {

    private static final String TAG = "ThemeResources";

    private Resources mRes;
    private String mThemePackage;

    /**
     * Maps [package name, short flatten component name, or feature name] -> [icon drawable resource name].
     */
    private static final Map<String, String> sIconMap = new HashMap<>();

    /**
     * Maps theme package name to its corresponding {@link ThemeResources} object.
     */
    private static Map<String, ThemeResources> sResMap = new HashMap<>();

    private ThemeResources(Resources res, String themePackage) {
        mRes = res;
        mThemePackage = themePackage;
    }

    public static ThemeResources ofTheme(Context context, String packageName) {
        ThemeResources cachedRes = sResMap.get(packageName);
        if (cachedRes != null && cachedRes.mRes != null) {
            return cachedRes;
        }
        Resources res = null;
        if (LauncherConstants.LAUNCHER_PACKAGE_NAME.equals(packageName)) {
            // Get resources from launcher app it self. Note that this branch is always exercised on the launcher app.
            res = context.getResources();
        } else {
            // Get resources from theme package
            try {
                res = context.getPackageManager().getResourcesForApplication(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                HSLog.w(TAG, "Cannot find required theme package, or is setting system theme");
                e.printStackTrace();
            }
        }
        HSLog.d("Themes.Profile", "Instantiate a theme resources object");
        ThemeResources themeRes = new ThemeResources(res, packageName);
        sResMap.put(packageName, themeRes);
        return themeRes;
    }

    public String getString(String name) throws Resources.NotFoundException {
        ensureResources();
        return mRes.getString(getIdentifier(name, "string"));
    }

    public String getString(String name, String defaultValue) {
        try {
            return getString(name);
        } catch (Resources.NotFoundException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        try {
            ensureResources();
            return mRes.getBoolean(getIdentifier(name, "bool"));
        } catch (Resources.NotFoundException e) {
            return defaultValue;
        }
    }

    public Drawable getDrawable(String name) throws Resources.NotFoundException {
        ensureResources();
        return ResourcesCompat.getDrawable(mRes, getIdentifier(name, "drawable"), null);
    }

    private XmlResourceParser getXml(String name) throws Resources.NotFoundException {
        ensureResources();
        return mRes.getXml(getIdentifier(name, "xml"));
    }

    public int getInteger(String name) throws Resources.NotFoundException {
        ensureResources();
        return mRes.getInteger(getIdentifier(name, "integer"));
    }

    public int getInteger(String name, int defaultValue) {
        try {
            return getInteger(name);
        } catch (Resources.NotFoundException e) {
            return defaultValue;
        }
    }

    public int getDimensionPixelSize(String name) throws Resources.NotFoundException {
        ensureResources();
        return mRes.getDimensionPixelSize(getIdentifier(name, "dimen"));
    }

    /**
     * Get themed icon for specified app.
     *
     * @param iconIdentifier Identifier to specify the icon. Can be in one of the following forms:
     *                       (1) Package name (com.android.vending).
     *                       (2) Short flatten component name (com.google.android.gm/.ConversationListActivityGmail).
     *                       (3) Feature name with "feature://" prefix (feature://settings).
     * @return {@code null} when this theme does not provide an icon for given identifier, or when theme package is not
     * found.
     */
    public @Nullable Drawable getIcon(String iconIdentifier) {
        HSLog.d(TAG, "Get icon for " + iconIdentifier);
        Drawable icon;
        try {
            parseIconMapXmlIfNeeded();
            String resourceName = getResourceName(iconIdentifier);
            icon = getDrawable(resourceName);
            HSLog.d("Themes", "Resource name for " + iconIdentifier + " is " + resourceName + ", icon: " + icon);
        } catch (Resources.NotFoundException expected) {
            HSLog.d("Themes", "Icon " + iconIdentifier + ", exception: " + expected);
            return null;
        }
        return icon;
    }

    /**
     * Get theme icon for flashlight custom feature.
     *
     * @param isOn If flashlight is turned on.
     * @return {@code null} when this theme does not provide an icon for flashlight, or when theme package is not
     * found.
     */
    public
    @Nullable
    Drawable getFlashlightIcon(boolean isOn) {
        HSLog.d(TAG, "Get icon for flashlight");
        Drawable icon;
        try {
            parseIconMapXmlIfNeeded();
            String resourceName = getResourceName(
                    LauncherConstants.CUSTOM_FEATURE_PREFIX + ThemeConstants.FEATURE_NAME_FLASHLIGHT);
            if (isOn) {
                resourceName += "_on";
            }
            icon = getDrawable(resourceName);
            HSLog.d("Themes", "Resource name for flashlight is " + resourceName + ", icon: " + icon);
        } catch (Resources.NotFoundException expected) {
            HSLog.d("Themes", "Exception: " + expected);
            return null;
        }
        return icon;
    }

    /**
     * Get theme icon for update custom feature.
     *
     * @return {@code null} when this theme does not provide an icon for update, or when theme package is not
     * found.
     */
    public
    @Nullable
    Drawable getUpdateIcon() {
        HSLog.d(TAG, "Get icon for update");
        Drawable icon;
        try {
            parseIconMapXmlIfNeeded();
            String resourceName = getResourceName(LauncherConstants.CUSTOM_FEATURE_PREFIX + ThemeConstants.FEATURE_NAME_UPDATE);
            icon = getDrawable(resourceName);
            HSLog.d("Themes", "Resource name for update is " + resourceName + ", icon: " + icon);
        } catch (Resources.NotFoundException expected) {
            HSLog.d("Themes", "Exception: " + expected);
            return null;
        }
        return icon;
    }

    private void parseIconMapXmlIfNeeded() {
        synchronized (sIconMap) {
            parseIconMapXmlIfNeededLocked();
        }
    }

    @SuppressWarnings("unchecked")
    private void parseIconMapXmlIfNeededLocked() {
        if (!sIconMap.isEmpty()) {
            // Already parsed. No need to parse again.
            return;
        }
        int targetVersion = getInteger(ThemeConstants.RES_NAME_TARGET_LAUNCHER_VERSION_CODE, 0);
        if (BuildConfig.FLAVOR.equals(LauncherConstants.LAUNCHER_PACKAGE_NAME) && targetVersion < 44) {
            sIconMap.put(LauncherConstants.CUSTOM_FEATURE_PREFIX + "all.apps", "all_apps_icon");
        }
        XmlResourceParser iconParser;
        try {
            iconParser = getXml(ThemeConstants.RES_NAME_ICON_MAP);
            int eventType = iconParser.getEventType();
            List<String> listKeys = new ArrayList<>();
            String iconIdentifier = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = iconParser.getName();
                    if (tagName.equals("icon")) {
                        String attrName = iconParser.getAttributeName(0);
                        switch (attrName) {
                            case "name":
                                iconIdentifier = iconParser.getAttributeValue(0);
                                break;
                            case "listName":
                                String listName = iconParser.getAttributeValue(0);
                                List<String> list = (List<String>) LocalConfig.getList("Application", "AppLists", listName);
                                listKeys.clear();
                                listKeys.addAll(list);
                                break;
                            case "featureName":
                                iconIdentifier = LauncherConstants.CUSTOM_FEATURE_PREFIX + iconParser.getAttributeValue(0);
                                break;
                        }
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String resourceName = iconParser.getText();
                    if (!iconIdentifier.isEmpty()) {
                        sIconMap.put(iconIdentifier, resourceName);
                        HSLog.d(TAG, "Single | " + iconIdentifier + " : " + resourceName);
                        iconIdentifier = "";
                    } else {
                        for (String listPackageOrComponent : listKeys) {
                            sIconMap.put(listPackageOrComponent, resourceName);
                            HSLog.d(TAG, "List | " + listPackageOrComponent + " : " + resourceName);
                        }
                    }
                }
                eventType = iconParser.next();
            }
            HSLog.d("Themes", "Icon map: " + sIconMap);
        } catch (Resources.NotFoundException | XmlPullParserException | IOException expected) {
            HSLog.w(TAG, "Parse exception: " + expected);
        }
    }

    public static void flushIconMap() {
        synchronized (sIconMap) {
            sIconMap.clear();
        }
    }

    private void ensureResources() throws Resources.NotFoundException {
        if (mRes == null) {
            throw new Resources.NotFoundException("Theme resources not valid. "
                    + "Cannot find required theme package: " + mThemePackage);
        }
    }

    /**
     * @param iconIdentifier Short flatten component name (don't give package name here) or feature identifier.
     * @return Resource drawable name
     */
    private String getResourceName(String iconIdentifier) {
        String resourceName;
        resourceName = sIconMap.get(iconIdentifier);
        if (resourceName != null) {
            return resourceName;
        }
        resourceName = sIconMap.get(iconIdentifier.split("/")[0]);
        return resourceName == null ? "" : resourceName;
    }

    public String getThemePackage() {
        return mThemePackage;
    }

    public @AnyRes int getIdentifier(String name, String type) {
        return mRes.getIdentifier(name, type, mThemePackage);
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if (another instanceof ThemeResources) {
            return mThemePackage.compareToIgnoreCase(((ThemeResources) another).mThemePackage);
        }
        return 0;
    }
}

package com.acb.libwallpaper.live.customize;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.RemoteException;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.customize.activity.CustomizeActivity;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallpaperMgr {

    private static final String TAG = WallpaperMgr.class.getSimpleName();

    /**
     * Full size built-in wallpapers.
     */
    public static final int[] BUILT_IN_WALLPAPER_IDS = new int[]{R.drawable.wallpaper_2};

    /**
     * Thumbnails of relatively large size. Used in welcome view wallpaper picker.
     */
    public static final int[] BUILT_IN_WALLPAPER_THUMBNAIL_IDS = new int[]{R.drawable.wallpaper_thumbnail_2};

    /**
     * Thumbnails of small size. Used in local wallpaper gallery.
     */
    public static final int[] BUILT_IN_WALLPAPER_LOCAL_THUMBNAIL_IDS = new int[]{R.drawable.local_wallpaper_thumb_2};

    // Let it be a hash map (instead of sparse array) as it's a constant map anyway
    @SuppressLint("UseSparseArrays")
    static Map<Integer, String> DRAWABLE_NAME_MAP = new HashMap<>(4);

    static {
        DRAWABLE_NAME_MAP.put(R.drawable.wallpaper_2, "wallpaper_2");
    }

    /**
     * The following preferences is designed to communicate inter processes.
     * Should be called with Preferences.getDefault().
     */
    public static final String PREF_KEY_PACKAGE_PREPARED = "pref_key_package_prepared";
    public static final String PREF_KEY_SHOULD_SHOW_PACKAGE_BADGE = "should_show_package_badge";
    public static final String PREF_KEY_USES_RECOMMEND_WALLPAPER = "uses_recommend_wallpaper";
    public static final String PREF_KEY_SET_RECOMMEND_WALLPAPER = "set_recommend_wallpaper";

    /**
     * Inner customize process preferences.
     */
    public static final String PREF_KEY_PACKAGE_GUIDE_SHOWN = "pref_key_package_guide_shown";

    private final Preferences mPrefs = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);

    /*
     * Files to store users' original wallpaper they were using before choosing an Air Launcher built-in wallpaper,
     * in case they want to change back to their original ones.
     *
     * These files are no longer used since v1.2.9 (34) and will be removed on upgrade.
     */
    public static final String ORIGINAL_WALLPAPER_FILE_NAME = "pre_set_wallpaper.png";
    private static final String PREF_KEY_WELCOME_WALLPAPER_SET = "PREF_KEY_WELCOME_WALLPAPER_SET";
    public static final String ORIGINAL_WALLPAPER_THUMBNAIL_FILE_NAME = "pre_set_wallpaper_thumbnail.png";

    // TODO: 16/11/2016 combine the next notification and broadcast
    public static final String NOTIFICATION_REFRESH_LOCAL_WALLPAPER = "notification_refresh_local_wallpaper";

    // File deprecated (and removed on upgrade) since v1.2.9 (34)
    private static final String PREVIEW_SCREENSHOT_FILE_NAME = "preview_background.png";

    private static WallpaperMgr sInstance = new WallpaperMgr();

    public static final String NOTIFICATION_WALLPAPER_GALLERY_SAVED = "NOTIFICATION_WALLPAPER_GALLERY_SAVED";

    // Cached lists
    private final List<WallpaperInfo> mLocalWallpapers = new ArrayList<>(16);
    private ContentResolver mCr = HSApplication.getContext().getContentResolver();
    private Bitmap mBanner;

    public synchronized static WallpaperMgr getInstance() {
        return sInstance;
    }

    public static String getScreenshotBitmapPath() {
        return HSApplication.getContext().getCacheDir().getAbsolutePath() + File.separator + PREVIEW_SCREENSHOT_FILE_NAME;
    }

    public Preferences getSharedPreferences() {
        return mPrefs;
    }

    public void initLocalWallpapers(final CustomizeService service, final WallpaperInitListener callback) {
        if (!mLocalWallpapers.isEmpty()) {
            // Already initialized
            doCallback(callback);
            return;
        }
        Threads.postOnSingleThreadExecutor(() -> {
            // Insert built-in wallpapers to DB
            insertBuildInWallpapers();

            // Migrate data from shared preferences for old versions <= v1.2.8 (33)
            mPrefs.doOnce(() -> {
                List<WallpaperInfo> upgraded = getUpgradedWallpapers(service);
                addLocalWallpapersToDatabaseSync(upgraded);
            }, Prefs.IS_LOCAL_WALLPAPERS_UPGRADED);

            // Load DB to memory
            loadLocalWallpapers();

            doCallback(callback);
        });
    }

    void initLocalWallpapers(final String legacyWallpapers, final WallpaperInitListener callback) {
        if (!mLocalWallpapers.isEmpty()) {
            // Already initialized
            doCallback(callback);
            return;
        }

        Threads.postOnSingleThreadExecutor(() -> {
            // Insert built-in wallpapers to DB
            insertBuildInWallpapers();

            // Migrate data from shared preferences for old versions <= v1.2.8 (33)
            mPrefs.doOnce(() -> {
                List<WallpaperInfo> upgraded = getUpgradedWallpapers(legacyWallpapers);
                addLocalWallpapersToDatabaseSync(upgraded);
            }, Prefs.IS_LOCAL_WALLPAPERS_UPGRADED);

            // Load DB to memory
            loadLocalWallpapers();

            doCallback(callback);
        });
    }

    private void doCallback(final WallpaperInitListener callback) {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onWallpaperInitialized();
                }
            }
        });
    }

    public static long getBadgeTriggerTime() {
        return CustomizeConfig.getInteger(0, "Package", "TriggerTime") * 24 * 60 * 60 * 1000;
    }

    public static long getBadgeCdTime() {
        return CustomizeConfig.getInteger(0, "Package", "CDTime") * 60 * 60 * 1000;
    }

    private void insertBuildInWallpapers() {
        mPrefs.doOnce(() -> {
            Cursor c = mCr.query(WallpaperProvider.CONTENT_URI, null,
                    WallpaperProvider.COLUMN_TYPE + "=?", new String[]{WallpaperInfo.WALLPAPER_TYPE_BUILT_IN + ""},
                    null);
            try {
                if (c == null) {
                    List<WallpaperInfo> builtIns = getBuiltInWallpapers();
                    addLocalWallpapersToDatabaseSync(builtIns);
                } else {
                    if (c.getCount() == 0) {
                        List<WallpaperInfo> builtIns = getBuiltInWallpapers();
                        addLocalWallpapersToDatabaseSync(builtIns);
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }, Prefs.IS_LOCAL_WALLPAPERS_INITIALIZED);
    }

    public List<WallpaperInfo> getBuiltInWallpapers() {
        List<WallpaperInfo> builtInWallpapers = new ArrayList<>(WallpaperMgr.BUILT_IN_WALLPAPER_IDS.length);
        int buildDrawableId = HSPreferenceHelper.getDefault().getInt(WallpaperMgr.PREF_KEY_WELCOME_WALLPAPER_SET, 0);
        for (int i = 0; i < WallpaperMgr.BUILT_IN_WALLPAPER_IDS.length; i++) {
            WallpaperInfo buildInWallpaper = WallpaperInfo.newBuiltInWallpaper(WallpaperMgr.BUILT_IN_WALLPAPER_IDS[i]);
            if (buildDrawableId == WallpaperMgr.BUILT_IN_WALLPAPER_IDS[i]) {
                buildInWallpaper.setApplied(true);
            }
            builtInWallpapers.add(buildInWallpaper);
        }
        return builtInWallpapers;
    }

    /**
     * This method is invoked if wallpaper manager is first initialized by
     * {@link CustomizeActivity}.
     *
     * @param service Service provided by main process to obtain legacy (v1.2.8 [33] or earlier)
     *                local wallpapers stored in shared preferences in JSON string.
     */
    private List<WallpaperInfo> getUpgradedWallpapers(CustomizeService service) {
        List<WallpaperInfo> upgraded = new ArrayList<>();
        String jsonArrayString = "";
        try {
            jsonArrayString = service.getDefaultSharedPreferenceString(Prefs.LOCAL_WALLPAPERS, "");
            service.putDefaultSharedPreferenceString(Prefs.LOCAL_WALLPAPERS, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        parseWallpapers(upgraded, jsonArrayString);
        return upgraded;
    }

    /**
     * This method is invoked if wallpaper manager is first initialized by {@link WallpaperProvider}.
     *
     * @param legacyWallpapers Legacy (v1.2.8 [33] or earlier) local wallpapers stored in shared
     *                         preferences in JSON string.
     */
    private List<WallpaperInfo> getUpgradedWallpapers(String legacyWallpapers) {
        List<WallpaperInfo> upgraded = new ArrayList<>();
        String jsonArrayString = legacyWallpapers == null ? "" : legacyWallpapers;
        parseWallpapers(upgraded, jsonArrayString);
        return upgraded;
    }

    private void parseWallpapers(List<WallpaperInfo> outList, String jsonArrayString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonArrayString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    WallpaperInfo info = WallpaperInfo.valueOf(jsonArray.getJSONObject(i));
                    if (info != null && info.getType() != WallpaperInfo.WALLPAPER_TYPE_BUILT_IN) {
                        outList.add(info);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadLocalWallpapers() {
        List<WallpaperInfo> localWallpapers = new ArrayList<>(16);
        Cursor c = mCr.query(WallpaperProvider.CONTENT_URI, null, null, null,
                WallpaperProvider.COLUMN_CREATE_TIME + " DESC");
        if (c == null) {
            return;
        }
        try {
            while (c.moveToNext()) {
                WallpaperInfo wallpaper = new WallpaperInfo(c);
                localWallpapers.add(wallpaper);
            }
        } finally {
            c.close();
        }
        synchronized (mLocalWallpapers) {
            mLocalWallpapers.clear();
            mLocalWallpapers.addAll(localWallpapers);
        }
    }

    public void addLocalWallpaperSync(WallpaperInfo wallpaper) {
        synchronized (mLocalWallpapers) {
            mLocalWallpapers.add(0, wallpaper);
        }

        List<WallpaperInfo> addedWallpaper = new ArrayList<>(1);
        addedWallpaper.add(wallpaper);
        addLocalWallpapersToDatabaseSync(addedWallpaper);
    }

    public void addLocalWallpapersToDatabaseSync(final List<WallpaperInfo> wallpapers) {
        for (WallpaperInfo wallpaper : wallpapers) {
            ContentValues values = new ContentValues();
            wallpaper.onAdd();
            wallpaper.onAddToDatabase(values);
            mCr.insert(WallpaperProvider.CONTENT_URI, values);
        }
    }

    public List<WallpaperInfo> getLocalWallpapers() {
        synchronized (mLocalWallpapers) {
            return new ArrayList<>(mLocalWallpapers);
        }
    }

    public void removeLocalWallpapers(final List<WallpaperInfo> wallpapers) {
        synchronized (mLocalWallpapers) {
            mLocalWallpapers.removeAll(wallpapers);
        }

        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                for (WallpaperInfo wallpaper : wallpapers) {
                    mCr.delete(WallpaperProvider.CONTENT_URI, wallpaper.createDbSelectionQuery(), null);
                }
            }
        });
    }

    public boolean isAppliedWallpaper(WallpaperInfo info) {
        synchronized (mLocalWallpapers) {
            for (WallpaperInfo local : mLocalWallpapers) {
                if (local.isApplied()) {
                    return local.equals(info);
                }
            }
        }
        return false;
    }

    public void saveCurrentWallpaper(final WallpaperInfo wallpaperInfo) {
        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                boolean isContains = false;
                for (WallpaperInfo local : mLocalWallpapers) {
                    if (local.equals(wallpaperInfo)) {
                        local.setApplied(true);
                        local.setEdit(wallpaperInfo.getEdit());
                        isContains = true;
                        break;
                    }
                }
                if (!isContains) {
                    addLocalWallpaperSync(wallpaperInfo);
                }

                ArrayList<ContentProviderOperation> updateOps = new ArrayList<>(2);
                ContentValues values = new ContentValues();
                values.put(WallpaperProvider.COLUMN_IS_APPLIED, 0);
                updateOps.add(ContentProviderOperation.newUpdate(WallpaperProvider.CONTENT_URI)
                        .withValues(values)
                        .build());
                values = new ContentValues();
                wallpaperInfo.setApplied(true);
                wallpaperInfo.onAddToDatabase(values);
                updateOps.add(ContentProviderOperation.newUpdate(WallpaperProvider.CONTENT_URI)
                        .withValues(values)
                        .withSelection(wallpaperInfo.createDbSelectionQuery(), null)
                        .build());
                try {
                    mCr.applyBatch(WallpaperProvider.AUTHORITY, updateOps);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void saveCurrentWallpaper(final WallpaperInfo wallpaperInfo, final Runnable callback) {
        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                if (!mLocalWallpapers.contains(wallpaperInfo)) {
                    addLocalWallpaperSync(wallpaperInfo);
                }

                ArrayList<ContentProviderOperation> updateOps = new ArrayList<>(2);
                ContentValues values = new ContentValues();
                values.put(WallpaperProvider.COLUMN_IS_APPLIED, 0);
                updateOps.add(ContentProviderOperation.newUpdate(WallpaperProvider.CONTENT_URI)
                        .withValues(values)
                        .build());
                values = new ContentValues();
                wallpaperInfo.setApplied(true);
                wallpaperInfo.onAddToDatabase(values);
                updateOps.add(ContentProviderOperation.newUpdate(WallpaperProvider.CONTENT_URI)
                        .withValues(values)
                        .withSelection(wallpaperInfo.createDbSelectionQuery(), null)
                        .build());
                try {
                    mCr.applyBatch(WallpaperProvider.AUTHORITY, updateOps);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                } finally {
                    if (callback != null) {
                        callback.run();
                    }
                }
            }
        });
    }

    public void cleanCurrentWallpaper() {
        if (!mLocalWallpapers.isEmpty()) {
            for (WallpaperInfo localWallpaper : mLocalWallpapers) {
                localWallpaper.setApplied(false);
            }
        }
        Threads.postOnSingleThreadExecutor(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(WallpaperProvider.COLUMN_IS_APPLIED, 0);
                try {
                    mCr.update(WallpaperProvider.CONTENT_URI, values, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public enum Scenario {
        ONLINE_NEW(0),
        ONLINE_CATEGORY(1),
        LOCAL(2),
        PACKAGE(4),
        ONLINE_HOT(5);

        int value;

        Scenario(int v) {
            this.value = v;
        }

        public static Scenario valueOfOrdinal(int ordinal) {
            Scenario scenarios[] = Scenario.values();
            return scenarios[ordinal];
        }
    }

    public interface WallpaperInitListener {
        void onWallpaperInitialized();

        void onWallpaperInitializationFailed();
    }

    public static class Prefs {
        /*
         * Preferences written in default SharedPreference file. Access this file through CustomizeService.
         */
        /**
         * Added in 24 (v1.2.0).
         */
        public static final String CURRENT_WALLPAPER = "current_wallpaper_info";

        /**
         * Value: JSON array string containing local wallpapers.
         */
        public static final String LOCAL_WALLPAPERS = "SHARE_PREF_KEY_LOCAL_WALLPAPERS";

        /*
         * Preferences written in SharedPreference file for :customize subprocess only.
         */
        /**
         * Time we reordered the display lists.
         */
        public static final String DISPLAY_LIST_REORDER_TIME = "display_list_reorder_time";
        /**
         * Value: an integer recording next index of category to be put at first in "latest wallpapers" page.
         */
        public static final String SHUFFLE_CATEGORY_INDEX = "shuffle_category_index";
        /**
         * Key prefix that should be joined with a category identifier (eg. shuffle_index_cartoon).
         * <p/>
         * Value: an integer recording next index we shall read the shuffled list. When the shuffled list is used up,
         * it's regenerated.
         */
        public static final String SHUFFLE_INDEX_PREFIX = "shuffle_index_";
        /**
         * Time we changed latest wallpapers.
         */
        public static final String LATEST_WALLPAPERS_CHANGE_TIME = "latest_wallpapers_change_time";
        /**
         * Whether local wallpapers DB data are initialized. Added in v1.2.9 (34).
         */
        private static final String IS_LOCAL_WALLPAPERS_INITIALIZED = "is_local_wallpapers_initialized";
        /**
         * Whether legacy local wallpapers are migrated. Added in v1.2.9 (34).
         */
        private static final String IS_LOCAL_WALLPAPERS_UPGRADED = "is_local_wallpapers_upgraded";
    }
}

package com.honeycomb.colorphone.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.acb.call.utils.FileUtils;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.video.VideoUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.superapps.util.Compats;
import com.superapps.util.Threads;
import com.umeng.commonsdk.debug.E;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import hugo.weaving.DebugLog;

/**
 * Created by sundxing on 2018/1/19.
 */

public class RingtoneHelper {
    private final static String PREFS_KEY_ACTIVE = "ringtone_key_active";
    private final static String PREFS_KEY_ANIM = "ringtone_key_anim";
    private final static String PREFS_KEY_FIRST_RINGTONE = "ringtone_key_ringtone_first";
    private final static String PREFS_KEY_SYSTEM_RINGTONE = "ringtone_key_system_ringtone";

    private final static String PREFS_KEY_TOAST_FLAG = "ringtone_key_toast";

    private final static String SPLIT = ",";
    private final static ConcurrentHashMap<String, String> mPathUriMaps = new ConcurrentHashMap<String, String>();

    private static Set<Integer> mAnimThemes;
    private static Set<Integer> mActiveThemes;


    public static boolean isAnimationFinish(int themeId) {
        ensureAnimThemeList();
        return mAnimThemes.contains(themeId);
    }

    public static boolean isActive(int themeId) {
        ensureActiveThemeList();
        return mActiveThemes.contains(themeId);
    }

    @Deprecated
    public static void ringtoneAnim(int themeId) {
        ensureAnimThemeList();
        boolean update = mAnimThemes.add(themeId);
        if (update) {
            savePrefs(PREFS_KEY_ANIM, mAnimThemes);
        }
    }

    public static void ringtoneActive(int themeId, boolean active) {
        ensureActiveThemeList();
        boolean update = false;
        if (active) {
            update = mActiveThemes.add(themeId);
        } else {
            update = mActiveThemes.remove(themeId);
        }
        if (update) {
            savePrefs(PREFS_KEY_ACTIVE, mActiveThemes);
        }

    }

    private static void ensureAnimThemeList() {
        synchronized (PREFS_KEY_ANIM) {
            if (mAnimThemes == null) {
                mAnimThemes = new HashSet<>();
                readPrefs(PREFS_KEY_ACTIVE, mAnimThemes);
            }
        }
    }

    private static void ensureActiveThemeList() {
        synchronized (PREFS_KEY_ACTIVE) {
            try {
                if (mActiveThemes == null) {
                    mActiveThemes = new HashSet<>();
                    readPrefs(PREFS_KEY_ANIM, mActiveThemes);
                }
            } catch (Exception e) {
                //
            }
        }
    }

    private static void readPrefs(String keyName, Set<Integer> results) {
        String list = HSPreferenceHelper.create(HSApplication.getContext(), "ringtone")
                .getString(keyName, "");
        String[] ids = list.split(SPLIT);

        for (String s : ids) {
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            results.add(Integer.parseInt(s));
        }
    }

    private static void savePrefs(String keyName, Set<Integer> results) {

        StringBuilder sb = new StringBuilder();
        if (results != null && results.size() > 0) {
            for (Integer id : results) {
                if (id != null) {
                    sb.append(id.toString());
                    sb.append(SPLIT);
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        HSPreferenceHelper.create(HSApplication.getContext(), "ringtone")
                .putString(keyName, sb.toString());
    }

    private static int getFirstRingtoneId() {
        int id = HSPreferenceHelper.create(HSApplication.getContext(), "ringtone")
                .getInt(PREFS_KEY_FIRST_RINGTONE, 0);
        return id;
    }

    private static void saveFirstRingtoneId(int id) {
        HSPreferenceHelper.create(HSApplication.getContext(), "ringtone")
                .putInt(PREFS_KEY_FIRST_RINGTONE, id);

    }

    public static String getSystemRingtoneUri() {
        String lastSystemRingtone = HSPreferenceHelper.create(HSApplication.getContext(), "ringtone")
                .getString(PREFS_KEY_SYSTEM_RINGTONE, "");
        return lastSystemRingtone;
    }

    private static void saveSystemRingtoneUri(String uri) {
        HSPreferenceHelper.create(HSApplication.getContext(), "ringtone")
                .putString(PREFS_KEY_SYSTEM_RINGTONE, uri);
    }

    public static void resetDefaultRingtone() {
        String sysRingtone = getSystemRingtoneUri();
        if (!TextUtils.isEmpty(sysRingtone)) {
            RingtoneManager.setActualDefaultRingtoneUri(HSApplication.getContext(), RingtoneManager.TYPE_RINGTONE,
                    Uri.parse(sysRingtone));
            HSLog.d("Ringtone", "Reset default ringtone: " + sysRingtone);

        } else {
            // Not set any ringtone before.
            HSLog.e("Ringtone", "Reset default ringtone fail, last system Ringtone: " + sysRingtone);
        }
    }

    public static void setDefaultRingtoneInBackground(final Theme theme) {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                setDefaultRingtone(theme);
            }
        });
    }

    public static void setDefaultRingtoneInBackground(String path, String title) {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                setDefaultRingtone(HSApplication.getContext(), path, title);
            }
        });
    }

    private static void setDefaultRingtone(Theme theme) {
        setDefaultRingtone(HSApplication.getContext(), getRingtonePath(theme), theme.getIdName());
    }

    /**
     * 设置铃声
     *
     * @param path  下载下来的mp3全路径
     * @param title 铃声的名字
     */
    @DebugLog
    private static void setDefaultRingtone(Context context, String path, String title) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        Uri oldRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE); //系统当前  通知铃声

        int lastRingtoneIdInteger = -1;

        if (oldRingtoneUri != null) {
            String lastRingtoneId = oldRingtoneUri.getLastPathSegment();
            try {
                // System ringtone uri may not contain media id.
                lastRingtoneIdInteger = Integer.parseInt(lastRingtoneId);
            } catch (Exception ignore) {
            }
        }

        int firstRingtoneId = getFirstRingtoneId();
        // Our first ringtone id is larger than system-embedded. Or no id ( lastRingtoneIdInteger = -1 )
        final boolean firstTimeRingtoneSet = firstRingtoneId == 0;
        final boolean isSystemRingtone = firstRingtoneId > 0 && lastRingtoneIdInteger < firstRingtoneId;
        if ((firstTimeRingtoneSet || isSystemRingtone) &&
                oldRingtoneUri != null) {
            saveSystemRingtoneUri(oldRingtoneUri.toString());
        }
        HSLog.d("Ringtone", "old uri = " + oldRingtoneUri);

        Uri newUri = getRingtoneUri(context, path, title);
        if (newUri == null) {
            return;
        }

        if (Compats.IS_HUAWEI_DEVICE) {
            try {
                Settings.System.putString(context.getContentResolver(), "ringtone2", newUri.toString());
                Settings.System.putString(context.getContentResolver(), "ringtone2_path", path);
            } catch (Exception e) {
                HSLog.e("ringtone sim2 set error" + e.getMessage());
            }
        }

        if (Compats.IS_OPPO_DEVICE) {
            try {
                android.content.ContentResolver r2 = context.getContentResolver();
                java.lang.String r4 = "ringtone_sim2"; // 权限不允许
                java.lang.String r3 = newUri.toString();
                android.provider.Settings.System.putString(r2, r4, r3);
            } catch (Exception e) {
                HSLog.e("ringtone sim2 set error by system" + e.getMessage());
                try {
                    b2(context, path, title);
                } catch (Exception e1) {
                    HSLog.e("ringtone sim2 set error by content provider" + e1.getMessage());
                }
            }
        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("?path=");
//        sb.append(path);
//        sb.append("&title=");
//        sb.append(title);
//        sb.append("&is_drm=0&is_cached=1");
//        boolean ringtone2Path =  Settings.System.putString(context.getContentResolver(), "ringtone_2_CONSTANT_PATH", sb.toString());

        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);

        // Write first ringtone id.
        String newRingtoneId = newUri.getLastPathSegment();
        if (!TextUtils.isEmpty(newRingtoneId) && firstTimeRingtoneSet && TextUtils.isDigitsOnly(newRingtoneId)) {
            saveFirstRingtoneId(Integer.parseInt(newRingtoneId));
        }

        if (BuildConfig.DEBUG) {
            Log.d("RingtoneHelper:", "Ringtone change to: " + title);
        }
    }

    private static void b2(Context context, String str, String str2) {
        if (context != null && !TextUtils.isEmpty(str)) {
            String ringtone_sim2 = Settings.System.getString(context.getContentResolver(), "ringtone_sim2");
            Uri a2 = Uri.parse(ringtone_sim2);
            if (a2 != null) {
                ContentResolver contentResolver = context.getContentResolver();
                //a(contentResolver, a2, str);
                ContentValues contentValues = new ContentValues();
                o.a(contentResolver, a2, contentValues, new o.a() {
                    public boolean a(String str, int i, Object obj, ContentValues contentValues) {
                        return "_id".equals(str) || str.equals("artist_key") || str.equals("album_key");
                    }
                });
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put("title", str2);
                boolean b2 = b(context, a2, str);
                if (!b2) {
                    contentValues2.put("_data", str);
                }
                int update = contentResolver.update(a2, contentValues2, null, null);
                if (!b2 && update >= 1) {
                    String a3 = o.a(a2);
                    String uri = a2.toString();
                    Uri insert = context.getContentResolver().insert(Uri.parse(uri.substring(0, uri.lastIndexOf("/" + a3))), contentValues);
                }
            }
        }
    }


    private static void a(ContentResolver contentResolver, Uri uri, String str) {
        if (contentResolver != null && uri != null && !TextUtils.isEmpty(str)) {
            String a2 = o.a(uri);
            int delete = contentResolver.delete(MediaStore.Files.getContentUri("internal"), "_id !=? and _data =?", new String[]{String.valueOf(a2), str});
            int delete2 = contentResolver.delete(MediaStore.Files.getContentUri("external"), "_id !=? and _data =?", new String[]{String.valueOf(a2), str});
        }
    }

    private static String[] f12050b = {"title", "_data", "_size"};

    private static boolean b(Context context, Uri uri, String str) {
        boolean z = false;
        if (context == null || uri == null || TextUtils.isEmpty(str)) {
            return false;
        }
        Cursor query = context.getContentResolver().query(uri, f12050b, null, null, null);
        if (query == null) {
            return false;
        }
        if (query.moveToFirst()) {
            z = str.equals(query.getString(query.getColumnIndex("_data")));
        }
        o.a(query);
        return z;
    }

    private static String getRingtonePath(Theme theme) {
        String path = theme.getRingtonePath();
        if (TextUtils.isEmpty(path)) {
            String videoFileName = theme.getFileName();
            File mediaDirectory = FileUtils.getMediaDirectory();
            if (mediaDirectory == null) {
                path = null;
            } else {
                String voiceFilePath = FileDownloadUtils.generateFilePath(mediaDirectory.getAbsolutePath(), videoFileName);

                File file = new File(voiceFilePath);
                if (file.exists() && file.length() > 0) {
                    path = voiceFilePath;
                } else {
                    path = VideoUtils.getVoiceFromVideo(voiceFilePath, videoFileName);
                }
                theme.setRingtonePath(path);
            }
        }

        if (TextUtils.isEmpty(path)) {
            Analytics.logEvent("CallFlash_Ringtone_File_Null", Analytics.FLAG_LOG_UMENG);
        }
        return path;
    }

    private static Uri getRingtoneUri(Context context, String path, String title) {

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        // Try hint cache.
        String cachedUriString = mPathUriMaps.get(path);
        if (cachedUriString != null) {
            return Uri.parse(cachedUriString);
        }

        // Try Obtain from provider
        File sdfile = new File(path);
        Uri mediaUri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mediaUri, null, MediaStore.MediaColumns.DATA + "=?", new String[]{sdfile.getAbsolutePath()}, null);
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                if (!TextUtils.isEmpty(id)) {
                    Uri existUri = Uri.withAppendedPath(mediaUri, id);
                    HSLog.d("Ringtone", "Path = " + path + " has exist.\n Uri = " + existUri.toString());
                    mPathUriMaps.put(path, existUri.toString());
                    return existUri;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Create new
        Uri newUri = null;
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, title);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        try {
            context.getContentResolver().delete(mediaUri,
                    MediaStore.MediaColumns.DATA + "=\"" + sdfile.getAbsolutePath() + "\"", null);
            newUri = context.getContentResolver().insert(mediaUri, values);
            mPathUriMaps.put(path, newUri.toString());

            HSLog.d("Ringtone", "new uri = " + newUri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newUri;
    }

    @DebugLog
    public static void setSingleRingtone(@Nullable Theme theme, String contactId) {
        if (theme != null) {
            setSingleRingtone(theme.getIdName(), getRingtonePath(theme), contactId);
        } else {
            // Clear
            setSingleRingtone(null, null, contactId);
        }
    }

    public static void setSingleRingtone(String name, @Nullable String path, String contactId) {
        String uri = null;
        if (!TextUtils.isEmpty(path) && name != null) {
            Uri ringtoneUri = getRingtoneUri(HSApplication.getContext(), path, name);
            if (ringtoneUri != null) {
                uri = ringtoneUri.toString();
            }
        }
        ContentValues values = new ContentValues();
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, uri);
        String Where = ContactsContract.Contacts._ID + " = ?";
        String[] WhereParams = new String[]{contactId};

        try {
            HSApplication.getContext().getContentResolver()
                    .update(ContactsContract.Contacts.CONTENT_URI, values, Where, WhereParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HSLog.d("Ringtone", "set contact id = " + contactId + ", ringtone = " + uri);

    }


    public static boolean isDefaultRingtone(Theme theme) {
        String path = getRingtonePath(theme);
        Uri sysUri = RingtoneManager.getActualDefaultRingtoneUri(HSApplication.getContext(), RingtoneManager.TYPE_RINGTONE);
        Uri themeUri = getRingtoneUri(HSApplication.getContext(), path, theme.getIdName());
        return (sysUri != null && themeUri != null && sysUri.equals(themeUri));
    }
}

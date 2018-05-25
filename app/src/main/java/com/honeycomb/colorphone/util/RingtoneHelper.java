package com.honeycomb.colorphone.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import hugo.weaving.DebugLog;

/**
 * Created by sundxing on 2018/1/19.
 */

public class RingtoneHelper {
    private static String PREFS_KEY_ACTIVE = "ringtone_key_active";
    private static String PREFS_KEY_ANIM = "ringtone_key_anim";
    private static String PREFS_KEY_FIRST_RINGTONE = "ringtone_key_ringtone_first";
    private static String PREFS_KEY_SYSTEM_RINGTONE = "ringtone_key_system_ringtone";

    private static String PREFS_KEY_TOAST_FLAG = "ringtone_key_toast";

    private static String SPLIT = ",";
    private static Set<Integer> mAnimThemes;
    private static Set<Integer> mActiveThemes;
    private static ConcurrentHashMap<String, String> mPathUriMaps = new ConcurrentHashMap<String, String>();


    public static boolean isAnimationFinish(int themeId) {
        ensureAnimThemeList();
        return mAnimThemes.contains(themeId);
    }

    public static boolean isActive(int themeId ) {
        ensureActiveThemeList();
        return mActiveThemes.contains(themeId);
    }

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
            if (mActiveThemes == null) {
                mActiveThemes = new HashSet<>();
                readPrefs(PREFS_KEY_ANIM, mActiveThemes);
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
        for (Integer id : results) {
            if (id != null) {
                sb.append(id.toString());
                sb.append(SPLIT);
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
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

    public static void setDefaultRingtone(Theme theme) {
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

        Uri oldRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE); //系统当前  通知铃声

        int lastRingtoneIdInteger = -1;

        if (oldRingtoneUri != null) {
            String lastRingtoneId = oldRingtoneUri.getLastPathSegment();
            try {
                // System ringtone uri may not contain media id.
                lastRingtoneIdInteger = Integer.parseInt(lastRingtoneId);
            } catch (Exception ignore) {
                ColorPhoneCrashlytics.getInstance().logException(
                        new IllegalStateException("Ringtone uri is not contain id segment : " + oldRingtoneUri)
                );
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
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);

        // Write first ringtone id.
        if (newUri != null) {
            String newRingtoneId = newUri.getLastPathSegment();
            if (firstTimeRingtoneSet && TextUtils.isDigitsOnly(newRingtoneId)) {
                saveFirstRingtoneId(Integer.parseInt(newRingtoneId));
            }
        }

        if (BuildConfig.DEBUG) {
            Toasts.showToast("Ringtone change to: " + title, Toast.LENGTH_SHORT);
        }
    }

    private static String getRingtonePath(Theme theme) {
        String path = theme.getRingtonePath();
        if (TextUtils.isEmpty(path)) {
            TasksManagerModel ringtoneModel = TasksManager.getImpl().getRingtoneTaskByThemeId(theme.getId());
            if (ringtoneModel != null) {
                path = ringtoneModel.getPath();
                theme.setRingtonePath(path);
            }
        }
        return path;
    }

    private static Uri getRingtoneUri(Context context, String path, String title) {

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
            cursor = context.getContentResolver().query(mediaUri, null, MediaStore.MediaColumns.DATA + "=?", new String[] { sdfile.getAbsolutePath() }, null);
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                if (!TextUtils.isEmpty(id)) {
                    Uri existUri =  Uri.withAppendedPath(mediaUri, id);
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
    public static void setSingleRingtone(Theme theme, String contactId) {
        String uri = null;
        if (theme != null) {
            Uri ringtoneUri = getRingtoneUri(HSApplication.getContext(), getRingtonePath(theme), theme.getIdName());
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
        } catch (Exception ignore) { }

        HSLog.d("Ringtone", "set contact id = " + contactId + ", ringtone = " + uri);

    }


    public static boolean isDefaultRingtone(Theme theme) {
        String path = getRingtonePath(theme);
        Uri sysUri = RingtoneManager.getActualDefaultRingtoneUri(HSApplication.getContext(), RingtoneManager.TYPE_RINGTONE);
        Uri themeUri = getRingtoneUri(HSApplication.getContext(), path, theme.getIdName());
        return (sysUri != null && themeUri != null && sysUri.equals(themeUri));
    }
}

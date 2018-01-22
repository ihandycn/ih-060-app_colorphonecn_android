package com.honeycomb.colorphone.util;

import android.content.ContentValues;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sundxing on 2018/1/19.
 */

public class RingtoneHelper {
    private static String PREFS_KEY_ACTIVE = "ringtone_key_active";
    private static String PREFS_KEY_ANIM = "ringtone_key_anim";
    private static String PREFS_KEY_FIRST_RINGTONE = "ringtone_key_ringtone_first";
    private static String PREFS_KEY_SYSTEM_RINGTONE = "ringtone_key_system_ringtone";

    private static String SPLIT = ",";
    private static Set<Integer> mAnimThemes;
    private static Set<Integer> mActiveThemes;


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
        if (mAnimThemes == null) {
            mAnimThemes = new HashSet<>();
            readPrefs(PREFS_KEY_ACTIVE, mAnimThemes);
        }
    }

    private static void ensureActiveThemeList() {
        if (mActiveThemes == null) {
            mActiveThemes = new HashSet<>();
            readPrefs(PREFS_KEY_ANIM, mActiveThemes);
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
        Uri oldRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(HSApplication.getContext(), RingtoneManager.TYPE_RINGTONE); //系统当前  通知铃声
        int lastRingtoneIdInteger = 0;
        if (oldRingtoneUri != null) {
            String lastRingtoneId = Utils.getFileNameFromUrl(oldRingtoneUri.toString());
            lastRingtoneIdInteger = Integer.parseInt(lastRingtoneId);
        }

        if (lastRingtoneIdInteger > 0 && lastRingtoneIdInteger < getFirstRingtoneId()) {
            // current ringtone used as system one.
        }
        RingtoneManager.setActualDefaultRingtoneUri(HSApplication.getContext(), RingtoneManager.TYPE_RINGTONE,
                Uri.parse(getSystemRingtoneUri()));

    }

    public static void setDefaultRingtone(Theme theme) {
        TasksManagerModel ringtoneModel = TasksManager.getImpl().getRingtoneTaskByThemeId(theme.getId());
        setDefaultRingtone(HSApplication.getContext(), ringtoneModel.getPath(), theme.getIdName());
    }

    /**
     * 设置铃声
     *
     * @param path  下载下来的mp3全路径
     * @param title 铃声的名字
     */
    public static void setDefaultRingtone(Context context, String path, String title) {

        Uri oldRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE); //系统当前  通知铃声

        String lastRingtoneId = Utils.getFileNameFromUrl(oldRingtoneUri.toString());
        int lastRingtoneIdInteger = Integer.parseInt(lastRingtoneId);
        if (lastRingtoneIdInteger == -1) {
            throw new IllegalStateException("Ringtone uri invalid:" + oldRingtoneUri);
        }

        int firstRingtoneId = getFirstRingtoneId();
        // Our first ringtone id is larger than system-embedded.
        final boolean firstTimeRingtoneSet = firstRingtoneId == 0;
        final boolean isSystemRingtone = firstRingtoneId > 0 && lastRingtoneIdInteger < firstRingtoneId;
        if (firstTimeRingtoneSet || isSystemRingtone) {
            saveSystemRingtoneUri(oldRingtoneUri.toString());
        }
        Log.d("Ringtone", "old uri = " + oldRingtoneUri);

        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, title);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        Uri newUri = null;
        context.getContentResolver().delete(uri,
                MediaStore.MediaColumns.DATA + "=\"" + sdfile.getAbsolutePath() + "\"", null);
        try {
            newUri = context.getContentResolver().insert(uri, values);
            Log.d("Ringtone", "new uri = " + newUri);
            RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Write first ringtone id.
        if (newUri != null) {
            String newRingtoneId = Utils.getFileNameFromUrl(newUri.toString());
            if (firstTimeRingtoneSet) {
                saveFirstRingtoneId(Integer.parseInt(newRingtoneId));
            }
        }

        if (BuildConfig.DEBUG) {
            Toast.makeText(context, "Ringtone change to: " + title, Toast.LENGTH_SHORT).show();
        }
    }

    public static void setSingleRingtone(Context context, String path, String contactId) {
        ContentValues values = new ContentValues();
        File file = new File(path);
        values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, Uri.fromFile(file).toString());
        String Where = ContactsContract.Contacts._ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] WhereParams = new String[]{contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, };
        context.getContentResolver().update(ContactsContract.Contacts.CONTENT_URI, values, null, null);
    }


}

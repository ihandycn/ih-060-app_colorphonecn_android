package com.honeycomb.colorphone.util;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sundxing on 2018/1/19.
 */

public class RingtoneHelper {
    private static String PREFS_KEY_ACTIVE = "ringtone_key_active";
    private static String PREFS_KEY_ANIM = "ringtone_key_anim";
    private static String SPLIT = ",";
    private static Set<Integer> mAnimThemes;
    private static Set<Integer> mActiveThemes;


    public static boolean isAnimationed(int themeId) {
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
            results.add(Integer.getInteger(s));
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
}

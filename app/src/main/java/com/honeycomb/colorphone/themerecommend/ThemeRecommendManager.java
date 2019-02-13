package com.honeycomb.colorphone.themerecommend;

import android.text.TextUtils;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.acb.utils.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.List;

public class ThemeRecommendManager {

    private static final String TAG = ThemeRecommendManager.class.getSimpleName();

    private PrefHelper mPrefHelper;

    private ThemeRecommendManager() {
        mPrefHelper = new PrefHelper();
    }

    public static ThemeRecommendManager getInstance() {
        return ClassHolder.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public String getRecommendThemeIdAndRecord(String number) {
        String result = "";
        List<String> guideThemeIdNameList = (List<String>) HSConfig.getList("Application", "ThemeGuide");
        if (guideThemeIdNameList.isEmpty()) {
            return result;
        }
        int size = guideThemeIdNameList.size();
        int lastIndex = getThemeRecommendIndex(number);
        int startIndex;
        if (lastIndex < 0 || lastIndex >= size - 1) {
            startIndex = 0;
        } else {
            startIndex = lastIndex + 1;
        }

        result = getThemeIdAndRecordIndex(guideThemeIdNameList, startIndex, size, number);

        if (TextUtils.isEmpty(result)) {
            result = getThemeIdAndRecordIndex(guideThemeIdNameList, 0, startIndex, number);
        }

        HSLog.d(TAG, "recommend theme: " + result);
        return result;
    }

    public void putAppliedTheme(String number, String idName) {
        mPrefHelper.putAppliedTheme(number, idName);
    }

    public void putAppliedThemeForAllUser(String idName) {
        mPrefHelper.putAppliedThemeForAllUser(idName);
    }

    private void putThemeRecommendIndex(String number, int index) {
        mPrefHelper.putThemeRecommendIndex(number, index);
    }

    private int getThemeRecommendIndex(String number) {
        return mPrefHelper.getThemeRecommendIndex(number);
    }

    private List<String> getAppliedThemeForAllUser() {
        return mPrefHelper.getAppliedThemeForAllUser();
    }

    private List<String> getAppliedTheme(String number) {
        return mPrefHelper.getAppliedTheme(number);
    }

    private boolean isLegal(String number, String idName) {
        int currentThemeId = ScreenFlashManager.getInstance().getAcbCallFactory().getIncomingReceiverConfig().getThemeIdByPhoneNumber(number);
        Type type = Utils.getTypeByThemeId(currentThemeId);
        return (type == null || !idName.equals(type.getIdName())) && !getAppliedThemeForAllUser().contains(idName) && !getAppliedTheme(number).contains(idName);
    }

    private String getThemeIdAndRecordIndex(List<String> guideThemeNameList, int startIndex, int endIndex, String number) {
        for (int k = startIndex; k < endIndex; k++) {
            String idName = guideThemeNameList.get(k);
            if (isLegal(number, idName)) {
                putThemeRecommendIndex(number, k);
                return idName;
            } else {
                HSLog.d(TAG, "theme: " + idName + "is illegal!!!!");
            }
        }
        return "";
    }

    private class PrefHelper {
        private static final String PREF_FILE = "applied_theme_file";
        private static final String APPLIED_THEME_USER_PREFIX = "applied_theme_user_";
        private static final String APPLIED_THEME_FOR_ALL_USER = "applied_theme_for_all_user";
        private static final String THEME_RECOMMEND_INDEX_USER_PREFIX = "theme_recommend_index_user_";

        private Preferences pref;

        PrefHelper() {
            pref = Preferences.get(PREF_FILE);
        }

        void putAppliedTheme(String number, String idName) {
            String key = APPLIED_THEME_USER_PREFIX + number;
            List<String> list = pref.getStringList(key);
            if (list.contains(idName)) {
                return;
            }
            list.add(idName);
            pref.putStringList(key, list);
        }

        List<String> getAppliedTheme(String number) {
            return pref.getStringList(APPLIED_THEME_USER_PREFIX + number);
        }

        void putAppliedThemeForAllUser(String idName) {
            List<String> list = pref.getStringList(APPLIED_THEME_FOR_ALL_USER);
            if (list.contains(idName)) {
                return;
            }
            list.add(idName);
            pref.putStringList(APPLIED_THEME_FOR_ALL_USER, list);
        }

        List<String> getAppliedThemeForAllUser() {
            return pref.getStringList(APPLIED_THEME_FOR_ALL_USER);
        }

        void putThemeRecommendIndex(String number, int index) {
            pref.putInt(THEME_RECOMMEND_INDEX_USER_PREFIX + number, index);
        }

        int getThemeRecommendIndex(String number) {
            return pref.getInt(THEME_RECOMMEND_INDEX_USER_PREFIX + number, -1);
        }
    }

    private static class ClassHolder {
        private final static ThemeRecommendManager INSTANCE = new ThemeRecommendManager();
    }
}

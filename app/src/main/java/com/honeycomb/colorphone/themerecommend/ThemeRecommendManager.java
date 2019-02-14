package com.honeycomb.colorphone.themerecommend;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.acb.utils.Utils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public boolean isShowRecommendTheme(String number) {
        boolean result = false;
        boolean isFirstThemeRecommendShowed = isFirstThemeRecommendShowed(number);
        HSLog.d(TAG, "isFirstThemeRecommendShowed = " + isFirstThemeRecommendShowed);
        int hour = getTimeIntervalHours();
        boolean timeAble = now() - getThemeRecommendLastShowedTimeForAllUser() > TimeUnit.HOURS.toMillis(hour);
        HSLog.d(TAG, "time > " + hour + "h: " + timeAble);
        int callTimes = getCallTimes(number);
        HSLog.d(TAG, "callTimes = " + callTimes);
        if (!isFirstThemeRecommendShowed) {
            if (callTimes >= getCallTimesAtFirstThemeRecommendShowed() && timeAble) {
                recordFirstThemeRecommendShowed(number);
                resetRecordCallTimes(number);
                result = true;
            }
        } else {
            boolean isCouldShowToday = getThemeRecommendShowTimes(number) < getMaxShowTimesEveryOne();
            HSLog.d(TAG, "isCouldShowToday = " + isCouldShowToday);
            boolean isAppliedThemeAndTimesEnable = !isAppliedThemeForUser(number) || callTimes > getMaxCallTimesAfterAppliedTheme();
            HSLog.d(TAG, "isAppliedThemeAndTimesEnable = " + isAppliedThemeAndTimesEnable);
            result = isCouldShowToday && timeAble && isAppliedThemeAndTimesEnable;
        }

        if (result){
            increaseThemeRecommendShowTimes(number);
        }

        return result;
    }

    public void increaseCallTimes(String number) {
        mPrefHelper.increaseCallTimes(number);
    }

    private int getCallTimes(String number) {
        return mPrefHelper.getCallTimes(number);
    }

    private void resetRecordCallTimes(String number) {
        mPrefHelper.resetRecordCallTimes(number);
    }

    private void recordFirstThemeRecommendShowed(String number) {
        mPrefHelper.recordFirstThemeRecommendShowed(number);
    }

    private boolean isFirstThemeRecommendShowed(String number) {
        return mPrefHelper.isFirstThemeRecommendShowed(number);
    }

    private void increaseThemeRecommendShowTimes(String number) {
        mPrefHelper.increaseThemeRecommendShowTimes(number);
    }

    private long getThemeRecommendShowTimes(String number) {
        return mPrefHelper.getThemeRecommendShowTimes(number);
    }

    private long getThemeRecommendLastShowedTimeForAllUser() {
        return mPrefHelper.getThemeRecommendLastShowedTimeForAllUser();
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

    private boolean isAppliedThemeForUser(String number) {
        return !getAppliedThemeForAllUser().isEmpty() || !getAppliedTheme(number).isEmpty();
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

    private int getMaxCallTimesAfterAppliedTheme() {
        return 5;
    }

    private int getTimeIntervalHours() {
        return 2;
    }

    private int getCallTimesAtFirstThemeRecommendShowed(){
        return 3;
    }

    private int getMaxShowTimesEveryOne(){
        return 1;
    }

    private long now() {
        return System.currentTimeMillis();
    }


    private class PrefHelper {
        private static final String PREF_FILE = "applied_theme_file";
        private static final String APPLIED_THEME_USER_PREFIX = "applied_theme_user_";
        private static final String APPLIED_THEME_FOR_ALL_USER = "applied_theme_for_all_user";
        private static final String THEME_RECOMMEND_INDEX_USER_PREFIX = "theme_recommend_index_user_";
        private static final String CALL_TIMES_FOR_USER_PREFIX = "call_times_for_user_";
        private static final String THEME_RECOMMEND_FIRST_SHOWED_PREFIX = "theme_recommend_first_showed_";
        private static final String THEME_RECOMMEND_SHOW_TIMES_PREFIX = "theme_recommend_show_times_";
        private static final String THEME_RECOMMEND_SHOW_TIME_PREFIX = "theme_recommend_show_time_";
        private static final String THEME_RECOMMEND_LAST_SHOWED_TIME_FOR_ALL_USER = "theme_recommend_last_showed_time_for_all_user";
        private static final String INCREASE_CALL_TIMES_FOR_USER_LAST_TIME_PREFIX = "increase_call_times_for_user_last_time_";
        private static final String APPLIED_THEME_FOR_ALL_USER_TIME = "applied_theme_for_all_user_time";
        private static final String APPLIED_THEME_USER_TIME_PREFIX = "applied_theme_user_time_";

        private Preferences pref;

        PrefHelper() {
            pref = Preferences.get(PREF_FILE);
        }

        void putAppliedTheme(String number, String idName) {
            pref.putLong(APPLIED_THEME_USER_TIME_PREFIX + number, now());
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
            pref.putLong(APPLIED_THEME_FOR_ALL_USER_TIME, now());
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

        void increaseCallTimes(String number) {
            if (isFirstThemeRecommendShowed(number) && getCallTimes(number) > getMaxCallTimesAfterAppliedTheme()) {
                return;
            }
            String key = CALL_TIMES_FOR_USER_PREFIX + number;
            int times = pref.getInt(key, 0);
            String lastIncreaseKey = INCREASE_CALL_TIMES_FOR_USER_LAST_TIME_PREFIX + number;
            long lastIncreaseTime = pref.getLong(lastIncreaseKey, 0);
            if (lastIncreaseTime < Math.max(getAppliedThemeForAllUserTime(), getAppliedThemeTime(number))) {
                times = 0;
            }
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(key, ++times);
            editor.putLong(lastIncreaseKey, now());
            editor.apply();
        }

        void resetRecordCallTimes(String number) {
            pref.remove(CALL_TIMES_FOR_USER_PREFIX + number);
        }

        int getCallTimes(String number) {
            return pref.getInt(CALL_TIMES_FOR_USER_PREFIX + number, 0);
        }

        void recordFirstThemeRecommendShowed(String number) {
            pref.putBoolean(THEME_RECOMMEND_FIRST_SHOWED_PREFIX + number, true);
        }

        boolean isFirstThemeRecommendShowed(String number) {
            return pref.getBoolean(THEME_RECOMMEND_FIRST_SHOWED_PREFIX + number, false);
        }

        void increaseThemeRecommendShowTimes(String number) {
            SharedPreferences.Editor editor = pref.edit();
            int times = getThemeRecommendShowTimes(number);
            long time = getThemeRecommendShowTime(number);
            if (Calendars.isSameDay(time, now())) {
                times++;
            } else {
                editor.putLong(THEME_RECOMMEND_SHOW_TIME_PREFIX + number, now());
                times = 1;
            }
            editor.putInt(THEME_RECOMMEND_SHOW_TIMES_PREFIX + number, times);
            editor.putLong(THEME_RECOMMEND_LAST_SHOWED_TIME_FOR_ALL_USER, now());
            editor.apply();
        }

        int getThemeRecommendShowTimes(String number) {
            if (Calendars.isSameDay(getThemeRecommendShowTime(number), System.currentTimeMillis())) {
                return pref.getInt(THEME_RECOMMEND_SHOW_TIMES_PREFIX + number, 0);
            } else {
                return 0;
            }
        }

        long getThemeRecommendShowTime(String number) {
            return pref.getLong(THEME_RECOMMEND_SHOW_TIME_PREFIX + number, 0);
        }

        long getThemeRecommendLastShowedTimeForAllUser() {
            return pref.getLong(THEME_RECOMMEND_LAST_SHOWED_TIME_FOR_ALL_USER, 0);
        }

        long getAppliedThemeTime(String number) {
            return pref.getLong(APPLIED_THEME_USER_TIME_PREFIX + number, 0);
        }

        long getAppliedThemeForAllUserTime() {
            return pref.getLong(APPLIED_THEME_FOR_ALL_USER_TIME, 0);
        }
    }

    private static class ClassHolder {
        private final static ThemeRecommendManager INSTANCE = new ThemeRecommendManager();
    }
}

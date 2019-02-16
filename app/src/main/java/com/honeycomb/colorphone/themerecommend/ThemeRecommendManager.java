package com.honeycomb.colorphone.themerecommend;

import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.acb.utils.Utils;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.theme.ThemeDownloadJobService;
import com.honeycomb.colorphone.util.ColorPhoneCrashlytics;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.NetUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Calendars;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ThemeRecommendManager {

    private static final String TAG = ThemeRecommendManager.class.getSimpleName();
    private static final String TOPIC_ID = "topic-70osaxxsn";
    private static final String PREF_FILE = "applied_theme_file";
    private static final String MORE_CLICK_SESSION = "more_click_session";
    private static final String PREPARE_THENE = "prepare_thene";

    private PrefHelper mPrefHelper;
    private String preparedThemeIdName;

    private ThemeRecommendManager() {
        mPrefHelper = new PrefHelper();
    }

    public static ThemeRecommendManager getInstance() {
        return ClassHolder.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public String getRecommendThemeIdAndRecord(String number) {
        if (!isThemeRecommendEnable()) {
            HSLog.d(TAG, "getRecommendThemeIdAndRecord, not enable");
            return "";
        }
        
        number = deleteWhiteSpace(number);
        String result = "";
        List<String> guideThemeIdNameList = (List<String>) HSConfig.getList("Application", "ThemeGuide");
        if (guideThemeIdNameList.isEmpty()) {
            HSLog.d(TAG, "getRecommendThemeIdAndRecord, guide is null");
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
        } else if (TextUtils.equals(PREPARE_THENE, result)) {
            result = "";
        }

        if (!TextUtils.isEmpty(result)) {
            preparedThemeIdName = result;
        }

        HSLog.d(TAG, "recommend theme: " + result);
        return result;
    }

    public String getPreparedThemeIdName() {
        return preparedThemeIdName;
    }

    public void clearPreparedThemeIdName() {
        preparedThemeIdName = "";
    }

    public void putAppliedTheme(String number, String idName) {
        mPrefHelper.putAppliedTheme(deleteWhiteSpace(number), idName);
        logThemeRecommendChooseFromResultPage();
    }

    public void putAppliedThemeForAllUser(String idName) {
        mPrefHelper.putAppliedThemeForAllUser(idName);
        logThemeRecommendChooseFromResultPage();
    }

    public boolean isShowRecommendTheme(String number) {
        if (!isThemeRecommendEnable()) {
            return false;
        }

        number = deleteWhiteSpace(number);
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

        if (result) {
            increaseThemeRecommendShowTimes(number);
        }

        return result;
    }

    public void increaseCallTimes(String number) {
        mPrefHelper.increaseCallTimes(deleteWhiteSpace(number));
    }

    private int getCallTimes(String number) {
        return mPrefHelper.getCallTimes(deleteWhiteSpace(number));
    }

    private void resetRecordCallTimes(String number) {
        mPrefHelper.resetRecordCallTimes(deleteWhiteSpace(number));
    }

    private void recordFirstThemeRecommendShowed(String number) {
        mPrefHelper.recordFirstThemeRecommendShowed(deleteWhiteSpace(number));
    }

    private boolean isFirstThemeRecommendShowed(String number) {
        return mPrefHelper.isFirstThemeRecommendShowed(deleteWhiteSpace(number));
    }

    private void increaseThemeRecommendShowTimes(String number) {
        mPrefHelper.increaseThemeRecommendShowTimes(deleteWhiteSpace(number));
    }

    private long getThemeRecommendShowTimes(String number) {
        return mPrefHelper.getThemeRecommendShowTimes(deleteWhiteSpace(number));
    }

    private long getThemeRecommendLastShowedTimeForAllUser() {
        return mPrefHelper.getThemeRecommendLastShowedTimeForAllUser();
    }

    private void putThemeRecommendIndex(String number, int index) {
        mPrefHelper.putThemeRecommendIndex(deleteWhiteSpace(number), index);
    }

    private int getThemeRecommendIndex(String number) {
        return mPrefHelper.getThemeRecommendIndex(deleteWhiteSpace(number));
    }

    private List<String> getAppliedThemeForAllUser() {
        return mPrefHelper.getAppliedThemeForAllUser();
    }

    private boolean isAppliedThemeForUser(String number) {
        return (!getAppliedThemeForAllUser().isEmpty() || !getAppliedTheme(deleteWhiteSpace(number)).isEmpty());
    }

    private List<String> getAppliedTheme(String number) {
        return mPrefHelper.getAppliedTheme(deleteWhiteSpace(number));
    }

    private boolean isLegal(String number, String idName) {
        number = deleteWhiteSpace(number);
        int currentThemeId = ScreenFlashManager.getInstance().getAcbCallFactory().getIncomingReceiverConfig().getThemeIdByPhoneNumber(number);
        Type type = Utils.getTypeByThemeId(currentThemeId);
        return (type == null || !idName.equals(type.getIdName())) && !getAppliedThemeForAllUser().contains(idName) && !getAppliedTheme(number).contains(idName);
    }

    private String getThemeIdAndRecordIndex(List<String> guideThemeNameList, int startIndex, int endIndex, String number) {
        number = deleteWhiteSpace(number);
        for (int k = startIndex; k < endIndex; k++) {
            String idName = guideThemeNameList.get(k);
            if (isLegal(number, idName)) {
                Type theme = Utils.getTypeByThemeIdName(idName);
                if (theme != null && isThemeReady(theme)) {
                    putThemeRecommendIndex(number, k);
                    return idName;
                } else {
                    prepareTheme(theme);
                    return PREPARE_THENE;
                }
            } else {
                HSLog.d(TAG, "theme: " + idName + "is illegal!!!!");
            }
        }
        return "";
    }

    public void recordThemeRecommendNotShow(String number) {
        int k = getThemeRecommendIndex(number);
        putThemeRecommendIndex(number, --k);
    }

    private boolean isThemeReady(Type theme) {
        return !theme.isMedia()
                || TasksManager.getImpl().isThemeDownloaded(theme.getId());
    }

    private void prepareTheme(Type theme) {
        HSLog.d(TAG, "Prepare theme start : " + (theme != null ? theme.getIdName() : "null"));

        if (theme != null
                && theme.isMedia()) {
            // Need download it first
            TasksManagerModel model = TasksManager.getImpl().getByThemeId(theme.getId());
            if (model == null) {
//                LauncherAnalytics.logEvent("Test_Theme_Model_NULL", "Index", String.valueOf(pendingThemeIndex));
                return;
            }
            if (TasksManager.getImpl().isDownloaded(model)) {
                HSLog.d(TAG, "prepareTheme success , file already downloaded : " + theme.getIdName());
                return;
            }

            // Check wifi state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                HSLog.d(TAG, "Start theme download job : index " + theme.getIdName());
                try {
                    ThemeDownloadJobService.scheduleDownloadJob(model.getId());
                } catch (Exception e) {
                    ColorPhoneCrashlytics.getInstance().logException(e);
                }
            } else if (NetUtils.isWifiConnected(HSApplication.getContext())) {
                downloadMediaTheme(theme.getIndex(), model);
            } else {
                HSLog.d(TAG, "prepareTheme not download , native theme : " + theme.getIdName());
            }
        } else {
            HSLog.d(TAG, "prepareTheme success , native theme : " + (theme != null ? theme.getIdName() : "null"));
        }
    }

    private void downloadMediaTheme(int pendingThemeIndex, TasksManagerModel model) {
        boolean downloadStart = TasksManager.doDownload(model, null);
        if (downloadStart) {
            Ap.RandomTheme.logEvent("random_theme_download_start");
            LauncherAnalytics.logEvent("clorphone_random_theme_download_start");
        }
        final int taskId = model.getId();
        FileDownloadMultiListener.getDefault().addStateListener(taskId, new DownloadStateListener() {

            @Override
            public void updateDownloaded(boolean progressFlag) {
                // In case method call more than once.
                FileDownloadMultiListener.getDefault().removeStateListener(taskId);
                Ap.RandomTheme.logEvent("random_theme_download_success");
                LauncherAnalytics.logEvent("colorphone_random_theme_download_success");

                HSLog.d(TAG, "prepareTheme next success , file downloaded : " + pendingThemeIndex);
            }

            @Override
            public void updateNotDownloaded(int status, long sofar, long total) {
                FileDownloadMultiListener.getDefault().removeStateListener(taskId);
                HSLog.d(TAG, "prepareTheme next fail , file not downloaded : " + pendingThemeIndex);
            }

            @Override
            public void updateDownloading(int status, long sofar, long total) {
            }
        });
    }

    private String deleteWhiteSpace(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        return number.replaceAll(" ", "");
    }

    private int getMaxCallTimesAfterAppliedTheme() {
        return getThemeRecommendApplyInterval();
    }

    private int getTimeIntervalHours() {
        return getThemeRecommendTimeInterval();
    }

    private int getCallTimesAtFirstThemeRecommendShowed() {
        return getThemeRecommendFirstInterval();
    }

    private int getMaxShowTimesEveryOne() {
        return getThemeRecommendMaxTimesForOne();
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private class PrefHelper {
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
            String lastIncreaseKey = INCREASE_CALL_TIMES_FOR_USER_LAST_TIME_PREFIX + number;
            long lastIncreaseTime = pref.getLong(lastIncreaseKey, 0);
            long lastAppliedTime = Math.max(getAppliedThemeForAllUserTime(), getAppliedThemeTime(number));
            if (isFirstThemeRecommendShowed(number) && getCallTimes(number) > getMaxCallTimesAfterAppliedTheme() && lastIncreaseTime > lastAppliedTime) {
                return;
            }
            String key = CALL_TIMES_FOR_USER_PREFIX + number;
            int times = pref.getInt(key, 0);
            if (lastIncreaseTime <= lastAppliedTime) {
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

        long getThemeRecommendLastShowedTimeForAllUser() {
            return pref.getLong(THEME_RECOMMEND_LAST_SHOWED_TIME_FOR_ALL_USER, 0);
        }

        private long getThemeRecommendShowTime(String number) {
            return pref.getLong(THEME_RECOMMEND_SHOW_TIME_PREFIX + number, 0);
        }

        private long getAppliedThemeTime(String number) {
            return pref.getLong(APPLIED_THEME_USER_TIME_PREFIX + number, 0);
        }

        private long getAppliedThemeForAllUserTime() {
            return pref.getLong(APPLIED_THEME_FOR_ALL_USER_TIME, 0);
        }
    }

    public static boolean isThemeRecommendEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "themerecommend_enable", false);
    }

    public static boolean isThemeRecommendAdShow() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "themerecommend_ad_show", false);
    }

    public static boolean isThemeRecommendAdShowBeforeRecommend() {
        return false;
//        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "ad_show_before_recommend", false);
    }

    private static int getThemeRecommendFirstInterval() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "themerecommend_first_show", 3);
    }

    private static int getThemeRecommendMaxTimesForOne() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "maxtime_one_contact", 1);
    }

    private static int getThemeRecommendApplyInterval() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "themerecommend_show_interval_when_applied", 5);
    }

    private static int getThemeRecommendTimeInterval() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "timeintervalhour", 2);
    }

    static void logThemeRecommendShow(String number) {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommend_show");
        LauncherAnalytics.logEvent("ColorPhone_ThemeRecommend_Show");

        String themeId = ThemeRecommendManager.getInstance().getRecommendThemeIdAndRecord(number);
        if (!TextUtils.isEmpty(themeId)) {
            ThemeRecommendManager.getInstance().clearPreparedThemeIdName();
            ThemeRecommendManager.getInstance().recordThemeRecommendNotShow(number);
        }
    }

    static void logThemeRecommendClick() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommend_click");
        LauncherAnalytics.logEvent("ColorPhone_ThemeRecommend_Click");
    }

    public static void logThemeRecommendWireShouldShow(boolean isBefore) {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommendwire_should_show");
        LauncherAnalytics.logEvent("ColorPhone_ThemeRecommendWire_Should_Show", "type", isBefore ? "Before" : "After");
    }

    public static void logThemeRecommendWireShow(boolean isBefore) {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommendwire_show");
        LauncherAnalytics.logEvent("ColorPhone_ThemeRecommendWire_Show", "type", isBefore ? "Before" : "After");
    }

    public static void logThemeRecommendDoneShouldShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommenddone_should_show");
        LauncherAnalytics.logEvent("ColorPhone_ThemeRecommendDone_Should_Show");
    }

    public static void logThemeRecommendDoneShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommenddone_show");
        LauncherAnalytics.logEvent("ColorPhone_ThemeRecommendDone_Show");
    }

    public static void logThemeRecommendResultPageShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_resultpage_show");
        LauncherAnalytics.logEvent("ColorPhone_ResultPage_Show");
    }

    public static void logThemeRecommendResultPageFindMoreClicked() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_resultpage_findmore_click");
        LauncherAnalytics.logEvent("ColorPhone_ResultPage_FindMore_Click");

        setThemeRecommendMoreClickSession();
    }

    private static boolean isThemeRecommendMoreClickSession() {
        return Preferences.get(PREF_FILE).getInt(MORE_CLICK_SESSION, -1) == SessionMgr.getInstance().getCurrentSessionId();
    }

    private static void setThemeRecommendMoreClickSession() {
        Preferences.get(PREF_FILE).putInt(MORE_CLICK_SESSION, SessionMgr.getInstance().getCurrentSessionId());
    }

    public static void logThemeRecommendThemeDetailFromResultPage() {
        if (isThemeRecommendMoreClickSession()) {
            isThemeRecommendEnable();
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themedetail_view_fromresultpage");
            LauncherAnalytics.logEvent("ColorPhone_ThemeDetail_View_FromResultPage");
        }
    }

    private static void logThemeRecommendChooseFromResultPage() {
        if (isThemeRecommendMoreClickSession()) {
            isThemeRecommendEnable();
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_choosetheme_fromresultpage");
            LauncherAnalytics.logEvent("ColorPhone_ChooseTheme_FromResultPage");
        }
    }

    public static void logThemeRecommendThemeWireShow() {
        if (isThemeRecommendMoreClickSession()) {
            isThemeRecommendEnable();
            AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_themerecommend_themewire_show");
            LauncherAnalytics.logEvent("ColorPhone_ThemeRecommend_ThemeWire_Show");
        }
    }

    private static class ClassHolder {
        private final static ThemeRecommendManager INSTANCE = new ThemeRecommendManager();
    }
}

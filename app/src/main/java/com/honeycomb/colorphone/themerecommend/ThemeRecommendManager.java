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
import com.honeycomb.colorphone.resultpage.ResultPageManager;
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
    private static final String TOPIC_ID = "topic-71fwky10t";
    private static final String PREF_FILE = "applied_theme_file";
    private static final String MORE_CLICK_SESSION = "more_click_session";
    private static final String PREPARE_THENE = "prepare_thene";

    private PrefHelper mPrefHelper;

    private ThemeRecommendManager() {
        mPrefHelper = new PrefHelper();
    }

    public static ThemeRecommendManager getInstance() {
        return ClassHolder.INSTANCE;
    }

    public String getRecommendThemeIdAndRecord(String number) {
        return getRecommendThemeIdAndRecord(number, false);
    }

    @SuppressWarnings("unchecked")
    public String getRecommendThemeIdAndRecord(String number, boolean isRecord) {
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

        result = getThemeIdAndRecordIndex(guideThemeIdNameList, startIndex, size, number, isRecord);

        if (TextUtils.equals(PREPARE_THENE, result)) {
            result = "";
        }

        HSLog.d(TAG, "recommend theme: " + result);
        return result;
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

        int minutes = getTimeIntervalMinutes();
        boolean timeAble = now() - getThemeRecommendLastShowedTimeForAllUser() > TimeUnit.MINUTES.toMillis(minutes);
        HSLog.d(TAG, "time > " + minutes + "h: " + timeAble);
        int callTimes = getCallTimes(number);
        HSLog.d(TAG, "callTimes = " + callTimes);
        if (callTimes >= getCallTimesAtFirstThemeRecommendShowed() && timeAble) {
            boolean isCouldShowToday = getThemeRecommendShowTimes(number) < getMaxShowTimesEveryOne();
            HSLog.d(TAG, "isCouldShowToday = " + isCouldShowToday);
            result = isCouldShowToday;
        }

        return result;
    }

    public boolean isCallValid(String number) {
        return getCallTimes(number) >= getCallTimesAtFirstThemeRecommendShowed();
    }

    public boolean isTimeValid(String number) {
        int minutes = getTimeIntervalMinutes();
        return now() - getThemeRecommendLastShowedTimeForAllUser() > TimeUnit.MINUTES.toMillis(minutes);
    }

    public void recordThemeRecommendShow (String number) {
        increaseThemeRecommendShowTimes(number);
        resetRecordCallTimes(number);
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

    private String getThemeIdAndRecordIndex(List<String> guideThemeNameList, int startIndex, int endIndex, String number, boolean isRecord) {
        number = deleteWhiteSpace(number);
        for (int k = startIndex; k < endIndex; k++) {
            String idName = guideThemeNameList.get(k);
            if (isLegal(number, idName)) {
                Type theme = Utils.getTypeByThemeIdName(idName);
                if (theme != null && isThemeReady(theme)) {
                    if (isRecord) {
                        putThemeRecommendIndex(number, k);
                    }
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
                    ThemeDownloadJobService.scheduleDownloadJobAnyNet(model.getId());
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

    private int getTimeIntervalMinutes() {
        return getThemeRecommendTimeInterval();
    }

    private int getCallTimesAtFirstThemeRecommendShowed() {
        return getThemeRecommendCallInterval();
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
        private static final String THEME_RECOMMEND_INDEX_USER = "theme_recommend_index_user";
        private static final String CALL_TIMES_FOR_USER = "call_times_for_user";
        private static final String THEME_RECOMMEND_FIRST_SHOWED_PREFIX = "theme_recommend_first_showed_";
        private static final String THEME_RECOMMEND_SHOW_TIMES_PREFIX = "theme_recommend_show_times_";
        private static final String THEME_RECOMMEND_SHOW_TIME_PREFIX = "theme_recommend_show_time_";
        private static final String THEME_RECOMMEND_LAST_SHOWED_TIME_FOR_ALL_USER = "theme_recommend_last_showed_time_for_all_user";
        private static final String INCREASE_CALL_TIMES_FOR_USER_LAST_TIME = "increase_call_times_for_user_last_time";
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
            pref.putInt(THEME_RECOMMEND_INDEX_USER, index);
        }

        int getThemeRecommendIndex(String number) {
            return pref.getInt(THEME_RECOMMEND_INDEX_USER, -1);
        }

        void increaseCallTimes(String number) {
            String lastIncreaseKey = INCREASE_CALL_TIMES_FOR_USER_LAST_TIME;
            long lastIncreaseTime = pref.getLong(lastIncreaseKey, 0);
            long lastAppliedTime = Math.max(getAppliedThemeForAllUserTime(), getAppliedThemeTime(number));
//            if (isFirstThemeRecommendShowed(number) && lastIncreaseTime > lastAppliedTime) {
//            if (lastIncreaseTime > lastAppliedTime) {
//                return;
//            }
            String key = CALL_TIMES_FOR_USER;
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
            pref.remove(CALL_TIMES_FOR_USER);
        }

        int getCallTimes(String number) {
            return pref.getInt(CALL_TIMES_FOR_USER, 0);
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
        return AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "ad_show_before_recommend", false);
    }

    private static int getThemeRecommendCallInterval() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "show_interval_calls", 3);
    }

    private static int getThemeRecommendMaxTimesForOne() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "maxtime_one_contact", 1);
    }

    private static int getThemeRecommendTimeInterval() {
        return (int) AutopilotConfig.getDoubleToTestNow(TOPIC_ID, "show_interval_minutes", 2);
    }

    static void logThemeRecommendShow(String number) {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_show");
        LauncherAnalytics.logEvent("recommend_show");

        String themeId = ThemeRecommendManager.getInstance().getRecommendThemeIdAndRecord(number);
//        if (!TextUtils.isEmpty(themeId)) {
//            ThemeRecommendManager.getInstance().clearPreparedThemeIdName();
//            ThemeRecommendManager.getInstance().recordThemeRecommendNotShow(number);
//        }
    }

    static void logThemeRecommendClick() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_btn_click");
        LauncherAnalytics.logEvent("recommend_btn_click");
    }

    public static void logThemeRecommendWireShouldShow() {
        if (isThemeRecommendAdShow()) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_detail_wiread_should_show");
            LauncherAnalytics.logEvent("recommend_detail_wiread_should_show");
        }
    }

    public static void logThemeRecommendWireShow() {
        if (isThemeRecommendAdShow()) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_detail_wiread_show");
            LauncherAnalytics.logEvent("recommend_detail_wiread_show", "From", ResultPageManager.getInstance().getInterstitialAdPlacement());
        }
    }

    public static void logThemeRecommendDoneShouldShow() {
        if (isThemeRecommendAdShow()) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_detail_donead_should_show");
            LauncherAnalytics.logEvent("recommend_detail_donead_should_show");
        }
    }

    public static void logThemeRecommendDoneShow() {
        if (isThemeRecommendAdShow()) {
            AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_detail_donead_show");
            LauncherAnalytics.logEvent("recommend_detail_donead_show", "From", ResultPageManager.getInstance().getExpressAdPlacement());
        }
    }

    public static void logThemeRecommendResultPageShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_detail_morethemes_show");
        LauncherAnalytics.logEvent("recommend_detail_morethemes_show");
    }

    public static void logThemeRecommendResultPageFindMoreClicked() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_detail_morethemes_findmore_click");
        LauncherAnalytics.logEvent("recommend_detail_morethemes_findmore_click");

        setThemeRecommendMoreClickSession();
    }

    public static void logThemeRecommendCallAssistantClose() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "call_assistant_close");
        LauncherAnalytics.logEvent("call_assistant_close");
    }

    public static void logThemeRecommendShouldShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_should_show");
        LauncherAnalytics.logEvent("recommend_should_show");
    }

    public static void logThemeRecommendWireOnRecommendShouldShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "wire_on_recommend_should_show");
        LauncherAnalytics.logEvent("wire_on_recommend_should_show");
    }

    public static void logThemeRecommendWireOnRecommendShow() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "wire_on_recommend_show");
        LauncherAnalytics.logEvent("wire_on_recommend_show", "From", ResultPageManager.getInstance().getExpressAdPlacement());
    }

    public static void logThemeRecommendThemeDownloadStart() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_theme_download_start");
        LauncherAnalytics.logEvent("recommend_theme_download_start");
    }

    public static void logThemeRecommendThemeDownloadSuccess() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_theme_download_success");
        LauncherAnalytics.logEvent("recommend_theme_download_success");
    }

    public static void logThemeRecommendThemeDownloadFail() {
        isThemeRecommendEnable();
        AutopilotEvent.logTopicEvent(TOPIC_ID, "recommend_theme_download_fail");
        LauncherAnalytics.logEvent("recommend_theme_download_fail",
                "Reason" , NetUtils.getNetworkState(HSApplication.getContext()) == NetUtils.NETWORK_NONE
                        ? "NoNetWork" : "TimeOut");
    }

    private static boolean isThemeRecommendMoreClickSession() {
        return Preferences.get(PREF_FILE).getInt(MORE_CLICK_SESSION, -1) == SessionMgr.getInstance().getCurrentSessionId();
    }

    private static void setThemeRecommendMoreClickSession() {
        Preferences.get(PREF_FILE).putInt(MORE_CLICK_SESSION, SessionMgr.getInstance().getCurrentSessionId() + 1);
    }

    public static void logThemeRecommendThemeDetailFromResultPage() {
        if (isThemeRecommendMoreClickSession()) {
            isThemeRecommendEnable();
            AutopilotEvent.logTopicEvent(TOPIC_ID, "themedetail_show_from_recommend");
            LauncherAnalytics.logEvent("themedetail_show_from_recommend");
        }
    }

    private static void logThemeRecommendChooseFromResultPage() {
        if (isThemeRecommendMoreClickSession()) {
            isThemeRecommendEnable();
            AutopilotEvent.logTopicEvent(TOPIC_ID, "choose_theme_from_recommend");
            LauncherAnalytics.logEvent("choose_theme_from_recommend");
        }
    }

    public static void logThemeRecommendThemeWireShow() {
        if (isThemeRecommendMoreClickSession()) {
            isThemeRecommendEnable();
            AutopilotEvent.logTopicEvent(TOPIC_ID, "themewire_show_from_recommend");
            LauncherAnalytics.logEvent("themewire_show_from_recommend");
        }
    }

    private static class ClassHolder {
        private final static ThemeRecommendManager INSTANCE = new ThemeRecommendManager();
    }
}

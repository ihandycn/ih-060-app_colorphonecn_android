package com.honeycomb.colorphone.notification;

import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.SparseArray;

import com.colorphone.lock.ScreenStatusReceiver;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class CleanGuideCondition implements INotificationObserver {
    public static final String TAG = "CleanGuideCondition";
    private static final String CLEAN_GUIDE_HISTORY = "CLEAN_GUIDE_HISTORY";

    private List<CleanGuideHolder> cleanGuideHolderList;
    private CleanGuideHolder lastHolder;

    private static final String PREF_KEY_BATTERY_LOW_SHOW_TIME = "pref_key_battery_low_show_time";
    private static final String PREF_KEY_CPU_HOT_SHOW_TIME = "pref_key_cpu_hot_show_time";
    private static final String PREF_KEY_BOOST_MEMORY_SHOW_TIME = "pref_key_boost_memory_show_time";
    private static final String PREF_KEY_BOOST_JUNK_SHOW_TIME = "pref_key_boost_junk_show_time";
    private static final String PREF_KEY_BOOST_APPS_SHOW_TIME = "pref_key_boost_apps_show_time";
    private static final String PREF_KEY_BATTERY_APPS_SHOW_TIME = "pref_key_battery_apps_show_time";
    private static final String PREF_KEY_CLEAN_GUIDE_LAST_SHOW_TIME = "pref_key_clean_guide_last_show_time";
    public static final String PREF_KEY_BATTERY_NORMAL = "pref_key_battery_normal";
    public static final String PREF_KEY_CPU_NORMAL = "pref_key_cpu_normal";

    public static final int CLEAN_GUIDE_TYPE_BATTERY_LOW = 1;
    public static final int CLEAN_GUIDE_TYPE_CPU_HOT = 2;
    public static final int CLEAN_GUIDE_TYPE_BOOST_MEMORY = 3;
    public static final int CLEAN_GUIDE_TYPE_BOOST_JUNK = 4;
    public static final int CLEAN_GUIDE_TYPE_BOOST_APPS = 5;
    public static final int CLEAN_GUIDE_TYPE_BATTERY_APPS = 6;

    private static final int CLEAN_GUIDE_TYPE_SPECIAL_COUNT = 2;

    @IntDef({ CLEAN_GUIDE_TYPE_BATTERY_LOW,
            CLEAN_GUIDE_TYPE_CPU_HOT,
            CLEAN_GUIDE_TYPE_BOOST_MEMORY,
            CLEAN_GUIDE_TYPE_BOOST_JUNK,
            CLEAN_GUIDE_TYPE_BOOST_APPS,
            CLEAN_GUIDE_TYPE_BATTERY_APPS })

    @Retention(RetentionPolicy.SOURCE)
    @interface CLEAN_GUIDE_TYPES {}

    private SparseArray<String> cleanGuidePrefKeys = new SparseArray<>();

    public static CleanGuideCondition getInstance() {
        return CleanGuideConditionHolder.instance;
    }

    private static class CleanGuideConditionHolder {
        private static final CleanGuideCondition instance = new CleanGuideCondition();
    }

    /* Public visibility for test */
    @SuppressWarnings("WeakerAccess")
    private CleanGuideCondition() {
        cleanGuidePrefKeys.append(CLEAN_GUIDE_TYPE_BATTERY_LOW, PREF_KEY_BATTERY_LOW_SHOW_TIME);
        cleanGuidePrefKeys.append(CLEAN_GUIDE_TYPE_CPU_HOT, PREF_KEY_CPU_HOT_SHOW_TIME);
        cleanGuidePrefKeys.append(CLEAN_GUIDE_TYPE_BOOST_MEMORY, PREF_KEY_BOOST_MEMORY_SHOW_TIME);
        cleanGuidePrefKeys.append(CLEAN_GUIDE_TYPE_BOOST_JUNK, PREF_KEY_BOOST_JUNK_SHOW_TIME);
        cleanGuidePrefKeys.append(CLEAN_GUIDE_TYPE_BOOST_APPS, PREF_KEY_BOOST_APPS_SHOW_TIME);
        cleanGuidePrefKeys.append(CLEAN_GUIDE_TYPE_BATTERY_APPS, PREF_KEY_BATTERY_APPS_SHOW_TIME);

        cleanGuideHolderList = new ArrayList<>(6);
        readFromPref();
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_PRESENT, this);
    }

    private boolean canShowCleanGuide(@CLEAN_GUIDE_TYPES int type) {
        if (type == CLEAN_GUIDE_TYPE_BATTERY_LOW || type == CLEAN_GUIDE_TYPE_CPU_HOT) {
            return false;
        }

        long lastShow = Preferences.get(Constants.NOTIFICATION_PREFS).getLong(cleanGuidePrefKeys.get(type), 0);
        boolean isToday = DateUtils.isToday(lastShow);
        return !isToday;
    }

    private void recordCleanGuideShow(@CLEAN_GUIDE_TYPES int type) {
        long now = System.currentTimeMillis();
        Preferences.get(Constants.NOTIFICATION_PREFS).putLong(cleanGuidePrefKeys.get(type), now);
        Preferences.get(Constants.NOTIFICATION_PREFS).putLong(PREF_KEY_CLEAN_GUIDE_LAST_SHOW_TIME, now);

        recordCleanGuideShown(type, type, now);
    }

    public void showCleanGuideIfNeeded() {
        boolean enable = HSConfig.optBoolean(true, "Application", "CleanGuide", "Enable");
        if (!enable) {
            HSLog.d(TAG, "没有开启功能");
        }

        long now = System.currentTimeMillis();
        int activeAfterInstallMinutes = HSConfig.optInteger(360, "Application", "CleanGuide", "ActiveAfterInstallMinutes");
        boolean newUserTimeInterval = now - HSSessionMgr.getFirstSessionStartTime()
                                    < DateUtils.MINUTE_IN_MILLIS * activeAfterInstallMinutes;
        if (newUserTimeInterval) {
            HSLog.d(TAG, String.format(Locale.getDefault(), "新用户 %d 分钟内不提示。", activeAfterInstallMinutes));
            return;
        }

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) <= 5) {
            HSLog.d(TAG, "0-6 点不提示");
            return;
        }

        if (showBatteryLowIfNeeded()) {
            HSLog.d(TAG, "show BatteryLow ");
            return;
        }

        if (showCpuHotIfNeeded()) {
            HSLog.d(TAG, "show CpuHot ");
            return;
        }

        checkHolders();

        boolean countMax = cleanGuideHolderList.size() >= HSConfig.optInteger(6, "Application", "CleanGuide", "MaxShowTime");
        if (countMax) {
            HSLog.d(TAG, "NOT show, max times");
            return;
        }

        boolean minTimeInterval = System.currentTimeMillis() - Preferences.get(Constants.NOTIFICATION_PREFS).getLong(PREF_KEY_CLEAN_GUIDE_LAST_SHOW_TIME, 0)
                                < DateUtils.MINUTE_IN_MILLIS * HSConfig.optInteger(120, "Application", "CleanGuide", "MinShowInterval");

        if (minTimeInterval) {
            HSLog.d(TAG, "NOT show, min time interval");
            return;
        }

        DeviceManager.getInstance().checkRunningApps(null);

        List<Integer> needToShowGuideTypes = new ArrayList<>(cleanGuidePrefKeys.size() - CLEAN_GUIDE_TYPE_SPECIAL_COUNT);
        @CLEAN_GUIDE_TYPES int type;
        for (int i = CLEAN_GUIDE_TYPE_SPECIAL_COUNT; i < cleanGuidePrefKeys.size(); i++) {
            type = cleanGuidePrefKeys.keyAt(i);
            if (canShowCleanGuide(type)) {
                needToShowGuideTypes.add(type);
            }
        }

        HSLog.d(TAG, "may show type: " + needToShowGuideTypes);

        int size = needToShowGuideTypes.size();

        if (size > 1) {
            Random random = new Random();
            type = needToShowGuideTypes.get(random.nextInt(size));
            showCleanGuideByType(type);
        } else if (size == 1) {
            showCleanGuideByType(needToShowGuideTypes.get(0));
        } else {
            HSLog.d(TAG, "NOT show, no guide can show");
        }
    }

    private boolean showBatteryLowIfNeeded() {
        int batteryLevel = DeviceManager.getInstance().getBatteryLevel();
        boolean changeToLow = Preferences.get(Constants.NOTIFICATION_PREFS).getBoolean(PREF_KEY_BATTERY_NORMAL, true);
        if (batteryLevel < 20) {
            if (changeToLow) {
                CleanGuideActivity.start(CLEAN_GUIDE_TYPE_BATTERY_LOW);
                recordCleanGuideShow(CLEAN_GUIDE_TYPE_BATTERY_LOW);
                Preferences.get(Constants.NOTIFICATION_PREFS).putBoolean(PREF_KEY_BATTERY_NORMAL, false);
                return true;
            }
        } else {
            Preferences.get(Constants.NOTIFICATION_PREFS).putBoolean(PREF_KEY_BATTERY_NORMAL, true);
        }
        HSLog.d(TAG, "NOT show BatteryLow");
        return false;
    }

    private boolean showCpuHotIfNeeded() {
        float cpuTemp = DeviceManager.getInstance().getCpuTemperatureCelsius();
        boolean changeToHigh = Preferences.get(Constants.NOTIFICATION_PREFS).getBoolean(PREF_KEY_CPU_NORMAL, true);
        if (cpuTemp >= 45) {
            if (changeToHigh) {
                HSLog.i(TAG, "show CpuHot");
                CleanGuideActivity.start(CLEAN_GUIDE_TYPE_CPU_HOT);
                Preferences.get(Constants.NOTIFICATION_PREFS).putBoolean(PREF_KEY_CPU_NORMAL, false);
                recordCleanGuideShow(CLEAN_GUIDE_TYPE_CPU_HOT);
                return true;
            }
        } else {
            Preferences.get(Constants.NOTIFICATION_PREFS).putBoolean(PREF_KEY_CPU_NORMAL, true);
        }
        HSLog.d(TAG, "NOT show CpuHot");
        return false;
    }

    private void showCleanGuideByType(@CLEAN_GUIDE_TYPES int showType) {
        HSLog.i(TAG, "show guide, type: " + showType);
        if (showType == CLEAN_GUIDE_TYPE_BOOST_APPS) {
            DeviceManager.getInstance().checkRunningApps(() -> {
                DeviceManager.getInstance().setRunningAppsRandom();

                CleanGuideActivity.start(showType);
                recordCleanGuideShow(showType);
            });
        } else {
            if (showType == CLEAN_GUIDE_TYPE_BOOST_JUNK
                    || showType == CLEAN_GUIDE_TYPE_BOOST_MEMORY) {
                DeviceManager.getInstance().setRunningAppsRandom();
            }
            CleanGuideActivity.start(showType);
            recordCleanGuideShow(showType);
        }
    }

    void sendCleanGuide(@CLEAN_GUIDE_TYPES int type) {
        showCleanGuideByType(type);
    }

    public void clearData() {
        for (int i = 0; i < cleanGuidePrefKeys.size(); i++) {
            Preferences.get(Constants.NOTIFICATION_PREFS).putLong(cleanGuidePrefKeys.valueAt(i), 0);
        }
        Preferences.get(Constants.NOTIFICATION_PREFS).putLong(PREF_KEY_CLEAN_GUIDE_LAST_SHOW_TIME, 0);
        cleanGuideHolderList.clear();
        saveToPref();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        HSLog.d(TAG, "onReceive s == " + s);
        switch (s) {
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                break;
            case ScreenStatusReceiver.NOTIFICATION_PRESENT:
                DeviceManager.getInstance().checkRunningApps(null);
                break;
            default:
                break;
        }
    }

    private void recordCleanGuideShown(int notifyId, int type, long time) {
        HSLog.d(TAG, "recordCleanGuideShown  id == " + notifyId + "  curType == " + type);
        lastHolder = new CleanGuideHolder(notifyId, type, time);

        cleanGuideHolderList.add(lastHolder);

        checkHolders();
    }

    private void checkHolders() {
        int size = cleanGuideHolderList.size();
        if (size > 0) {
            CleanGuideHolder holder;
            for (int i = size - 1; i >= 0; i--) {
                holder = cleanGuideHolderList.get(i);
                if (!holder.isValid()) {
                    cleanGuideHolderList.remove(holder);
                }
            }
            saveToPref();
        }
        HSLog.d(TAG, "checkHolders size == " + cleanGuideHolderList.size());
    }

    private void readFromPref() {
        String history = Preferences.get(Constants.NOTIFICATION_PREFS).getString(CLEAN_GUIDE_HISTORY, "");
        HSLog.d(TAG, "readFromPref history == " + history);
        if (!TextUtils.isEmpty(history)) {
            cleanGuideHolderList.clear();
            try {
                JSONArray jArray = new JSONArray(history);
                HSLog.d(TAG, "readFromPref jArray == " + jArray);
                if (jArray.length() > 0) {
                    for (int i = 0; i < jArray.length(); i++) {
                        HSLog.d(TAG, "readFromPref nStr == " + jArray.get(i));
                        lastHolder = new CleanGuideHolder();
                        lastHolder.fromJSON((JSONObject) jArray.get(i));
                        HSLog.d(TAG, "readFromPref holder == " + lastHolder);
                        if (lastHolder.isValid()) {
                            cleanGuideHolderList.add(lastHolder);
                        } else {
                            lastHolder = null;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HSLog.d(TAG, "readFromPref size == " + cleanGuideHolderList.size());
        }
    }

    private void saveToPref() {
        if (cleanGuideHolderList.size() > 0) {
            JSONArray jArray = new JSONArray();
            for (CleanGuideHolder holder : cleanGuideHolderList) {
                jArray.put(holder.toJSON());
            }
            Preferences.get(Constants.NOTIFICATION_PREFS).putString(CLEAN_GUIDE_HISTORY, jArray.toString());
        } else {
            Preferences.get(Constants.NOTIFICATION_PREFS).putString(CLEAN_GUIDE_HISTORY, "");
        }
    }

    static class CleanGuideHolder {
        private static final String ID = "id";
        private static final String TYPE = "type";
        private static final String TIME = "time";

        int nId;
        int nType;
        long sendTime;

        CleanGuideHolder(){}

        CleanGuideHolder(int id, int type, long time) {
            nId = id;
            nType = type;
            sendTime = time;
        }

        JSONObject toJSON() {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put(ID, nId);
                jObj.put(TYPE, nType);
                jObj.put(TIME, sendTime);
            } catch (JSONException e) {
            }
            HSLog.d(TAG, "toJSON == " + jObj.toString());
            return jObj;
        }

        CleanGuideHolder fromJSON(JSONObject jObj) {
            if (jObj == null) {
                return this;
            }

            try {
                nId = jObj.getInt(ID);
                nType = jObj.getInt(TYPE);
                sendTime = jObj.getLong(TIME);
            } catch (JSONException e) {
            }
            HSLog.d(TAG, "fromJSON == " + toString());
            return this;
        }

        public boolean isValid() {
            long time = System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS;
            return nType >= 0 && sendTime > time;
        }

        @Override
        public String toString() {
            return "CleanGuideHolder{" +
                    "nId=" + nId +
                    ", nType=" + nType +
                    ", sendTime=" + sendTime +
                    '}';
        }
    }
}

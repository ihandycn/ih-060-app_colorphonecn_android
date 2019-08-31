package com.honeycomb.colorphone.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import com.colorphone.lock.ScreenStatusReceiver;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.battery.BatteryCleanActivity;
import com.honeycomb.colorphone.boost.BoostActivity;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.cpucooler.CpuCoolDownActivity;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.toolbar.NotificationActivity;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.rom.RomUtils;

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

    static final String EXTRA_AUTO_COLLAPSE = "auto_collapse";
    static final String EXTRA_NOTIFICATION_ID = "notification_id";
    static final String EXTRA_NOTIFICATION_TYPE = "notification_type";

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
            return;
        }

        long now = System.currentTimeMillis();
        long lastShowTime = Preferences.get(Constants.NOTIFICATION_PREFS).getLong(PREF_KEY_CLEAN_GUIDE_LAST_SHOW_TIME, 0);

        if (now - lastShowTime > DateUtils.MINUTE_IN_MILLIS * 30) {
            NotificationManager.cancelSafely(NotificationManager.NOTIFICATION_ID_CLEAN_GUIDE);
            Analytics.logEvent("Clean_Guide_Close", "Type", "OverTime");
        }

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

        boolean minTimeInterval = now - lastShowTime
                                < DateUtils.MINUTE_IN_MILLIS * HSConfig.optInteger(120, "Application", "CleanGuide", "MinShowInterval");

        minTimeInterval = !BuildConfig.DEBUG && minTimeInterval;

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
                showCleanGuideInner(CLEAN_GUIDE_TYPE_BATTERY_LOW);
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
                showCleanGuideInner(CLEAN_GUIDE_TYPE_CPU_HOT);
                Preferences.get(Constants.NOTIFICATION_PREFS).putBoolean(PREF_KEY_CPU_NORMAL, false);
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
                showCleanGuideInner(showType);

            });
        } else {
            if (showType == CLEAN_GUIDE_TYPE_BOOST_JUNK
                    || showType == CLEAN_GUIDE_TYPE_BOOST_MEMORY) {
                DeviceManager.getInstance().setRunningAppsRandom();
            }
            showCleanGuideInner(showType);
        }
    }

    private void showCleanGuideInner(@CLEAN_GUIDE_TYPES int showType) {
        //            CleanGuideActivity.start(showType);
        sendNotification(new CleanGuideInfo(HSApplication.getContext(), showType));
        recordCleanGuideShow(showType);
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
                CleanGuideCondition.getInstance().showCleanGuideIfNeeded();
//                DeviceManager.getInstance().checkRunningApps(null);
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

    static class CleanGuideInfo {
        @CLEAN_GUIDE_TYPES
        int cleanGuideType;

        int imageRes;
        int actionColor;
        int actionBgId;
        final Runnable actionRunnable;
        String actionStr;
        String intentAction;
        String descriptionStr;
        SpannableString titleText;

        CleanGuideInfo(Context context, @CLEAN_GUIDE_TYPES int type) {
            Runnable boostRunnable = () ->
                    BoostActivity.start(context, ResultConstants.RESULT_TYPE_BOOST_CLEAN_GUIDE);

            cleanGuideType = type;
            int actionRes;
            int descriptionRes;
            String highlight;
            String titleStr;
            int index;

            switch (type) {
                case CleanGuideCondition.CLEAN_GUIDE_TYPE_BATTERY_APPS:
                    imageRes = R.drawable.clean_guide_battery_apps;
                    descriptionRes = R.string.clean_guide_description_battery_apps;
                    actionColor = 0xff5abc6e;
                    actionBgId = R.drawable.clean_guide_action_battery_bg;
                    actionRes = R.string.clean_guide_battery_action_optimize;
                    intentAction = NotificationManager.CLEAN_GUIDE_TYPE_BATTERY_APPS_ACTION;

                    highlight = context.getString(R.string.clean_guide_title_battery_apps_highlight);
                    titleStr = context.getString(R.string.clean_guide_title_battery_apps);

                    index = titleStr.indexOf(highlight);
                    titleText = new SpannableString(titleStr);

                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    actionRunnable = () -> {
                        Intent intent = new Intent(context, BatteryCleanActivity.class);
                        intent.putExtra(BatteryCleanActivity.EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_BATTERY_CLEAN_GUIDE);
                        Navigations.startActivitySafely(context, intent);
                    };
                    break;
                case CleanGuideCondition.CLEAN_GUIDE_TYPE_BATTERY_LOW:
                    imageRes = R.drawable.clean_guide_battery_low;
                    descriptionRes = R.string.clean_guide_description_battery_low;
                    actionColor = 0xff5abc6e;
                    actionBgId = R.drawable.clean_guide_action_battery_bg;
                    actionRes = R.string.clean_guide_battery_action_optimize_now;
                    intentAction = NotificationManager.CLEAN_GUIDE_TYPE_BATTERY_LOW_ACTION;

                    highlight = DeviceManager.getInstance().getBatteryLevel() + "%";
                    titleStr = String.format(context.getString(R.string.clean_guide_title_battery_low), highlight);
                    index = titleStr.indexOf(highlight);
                    titleText = new SpannableString(titleStr);

                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    actionRunnable = () -> {
                        Intent intent = new Intent(context, BatteryCleanActivity.class);
                        intent.putExtra(BatteryCleanActivity.EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_BATTERY_CLEAN_GUIDE);
                        Navigations.startActivitySafely(context, intent);
                    };
                    break;
                case CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_APPS:
                    imageRes = R.drawable.clean_guide_boost_apps;
                    descriptionRes = R.string.clean_guide_description_boost_apps;
                    actionColor = 0xff007ef5;
                    actionBgId = R.drawable.clean_guide_action_battery_bg;
                    actionRes = R.string.clean_guide_boost_action_fast;
                    intentAction = NotificationManager.CLEAN_GUIDE_TYPE_BOOST_APPS_ACTION;

                    highlight = String.valueOf(DeviceManager.getInstance().getRunningApps());
                    titleStr = String.format(context.getString(R.string.clean_guide_title_boost_apps), highlight);
                    index = titleStr.indexOf(highlight);
                    titleText = new SpannableString(titleStr);

                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    highlight = context.getString(R.string.clean_guide_title_boost_apps_highlight);
                    index = titleStr.indexOf(highlight);
                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    actionRunnable = boostRunnable;
                    break;
                case CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_JUNK:
                    imageRes = R.drawable.clean_guide_boost_junk;
                    descriptionRes = R.string.clean_guide_description_boost_junk;
                    actionColor = 0xff007ef5;
                    actionBgId = R.drawable.clean_guide_action_battery_bg;
                    actionRes = R.string.clean_guide_boost_action_boost;
                    intentAction = NotificationManager.CLEAN_GUIDE_TYPE_BOOST_JUNK_ACTION;

                    highlight = DeviceManager.getInstance().getJunkSize();
                    titleStr = String.format(context.getString(R.string.clean_guide_title_boost_junk), highlight);
                    index = titleStr.indexOf(highlight);
                    titleText = new SpannableString(titleStr);

                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    actionRunnable = boostRunnable;
                    break;
                case CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_MEMORY:
                    imageRes = R.drawable.clean_guide_boost_memory;
                    descriptionRes = R.string.clean_guide_description_boost_memory;
                    actionColor = 0xff007ef5;
                    actionBgId = R.drawable.clean_guide_action_battery_bg;
                    actionRes = R.string.clean_guide_boost_action_fast;
                    intentAction = NotificationManager.CLEAN_GUIDE_TYPE_BOOST_MEMORY_ACTION;

                    highlight = DeviceManager.getInstance().getRamUsage() + "%";
                    titleStr = String.format(context.getString(R.string.clean_guide_title_boost_memory), highlight);
                    index = titleStr.indexOf(highlight);
                    titleText = new SpannableString(titleStr);

                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    actionRunnable = boostRunnable;
                    break;
                default:
                case CleanGuideCondition.CLEAN_GUIDE_TYPE_CPU_HOT:
                    imageRes = R.drawable.clean_guide_cpu_hot;
                    descriptionRes = R.string.clean_guide_description_cpu_hot;
                    actionColor = 0xff58b8ff;
                    actionBgId = R.drawable.clean_guide_action_cpu_bg;
                    actionRes = R.string.clean_guide_cpu_action;
                    intentAction = NotificationManager.CLEAN_GUIDE_TYPE_CPU_HOT_ACTION;

                    highlight = context.getString(R.string.clean_guide_title_cpu_hot_highlight);
                    titleStr = context.getString(R.string.clean_guide_title_cpu_hot);
                    index = titleStr.indexOf(highlight);
                    titleText = new SpannableString(titleStr);

                    titleText.setSpan(
                            new ForegroundColorSpan(0xffd43d3d),
                            index, index + highlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    actionRunnable = () -> {
                        Intent intent = new Intent(context, CpuCoolDownActivity.class);
                        intent.putExtra(CpuCoolDownActivity.EXTRA_KEY_RESULT_PAGE_TYPE, ResultConstants.RESULT_TYPE_CPU_CLEAN_GUIDE);
                        Navigations.startActivitySafely(context, intent);
                    };
                    break;
            }

            descriptionStr = context.getString(descriptionRes);
            actionStr = context.getString(actionRes);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void sendNotification(CleanGuideInfo info) {
        HSLog.d(TAG, "sendNotification");
        LocalNotification localNotification = new LocalNotification();

        localNotification.title = info.titleText;
        localNotification.description = info.descriptionStr;
        localNotification.buttonText = info.actionStr;
        localNotification.buttonBgDrawableId = info.actionBgId;
        localNotification.iconDrawableIdRealStyle = info.imageRes;
        localNotification.smallIconDrawableId = info.imageRes;
        localNotification.isHeadsUp = HSConfig.optBoolean(false, "Application", "Boost", "BoostSuspension");
        localNotification.notificationId = NotificationManager.NOTIFICATION_ID_CLEAN_GUIDE;

        final int notificationId = localNotification.notificationId;
        localNotification.pendingIntent = getPendingIntent(
                info.intentAction, false,
                new NotificationCondition.ExtraProvider() {
                    @Override
                    public void onAddExtras(Intent intent) {
                        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
                        intent.putExtra(EXTRA_NOTIFICATION_TYPE, info.cleanGuideType);
                    }
                });

        localNotification.deletePendingIntent = getPendingIntent(
                NotificationManager.CLEAN_GUIDE_DISMISS_ACTION, false
        );
        showNotification(localNotification);

        Analytics.logEvent("Clean_Guide_Show", "Type", "Guide" + info.cleanGuideType);
    }

    private static void showNotification(LocalNotification notificationModel) {
        if (null == notificationModel) {
            return;
        }

        RemoteViews remoteViews = createRealStyleNotification(notificationModel);

        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(HSApplication.getContext())
                .setSmallIcon(notificationModel.smallIconDrawableId)
                .setContent(remoteViews)
                .setContentIntent(notificationModel.pendingIntent)
                .setDeleteIntent(notificationModel.deletePendingIntent)
                .setTicker(notificationModel.title)
                .setAutoCancel(true);

        if (notificationModel.isHeadsUp) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND
                    | NotificationCompat.DEFAULT_VIBRATE
                    | NotificationCompat.DEFAULT_LIGHTS);

            // 测试中存在高版本出现 crash, notified from MAX team
            try {
                builder.setPriority(NotificationCompat.PRIORITY_MAX);
            } catch (Exception e) {
                HSLog.i(TAG, "builder.setPriority(NotificationCompat.PRIORITY_MAX) EXCEPTION");
            }
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        NotificationManager.getInstance().hideNotificationToolbar();
        NotificationManager.notifySafely(notificationModel.notificationId, Utils.buildNotificationSafely(builder));
    }

    private static RemoteViews createRealStyleNotification(LocalNotification notificationModel) {
        int layoutId = R.layout.notification_clean_guide_layout;

        RemoteViews remoteViews = new RemoteViews(HSApplication.getContext().getPackageName(), layoutId);
        remoteViews.setImageViewResource(R.id.protect_image, notificationModel.iconDrawableIdRealStyle);
        remoteViews.setTextViewText(R.id.action_btn, notificationModel.buttonText);
        remoteViews.setTextViewText(R.id.clean_guide_title, notificationModel.title);
        if (RomUtils.checkIsHuaweiRom() && Utils.getEmuiVersion() < 5) {
            remoteViews.setViewVisibility(R.id.clean_guide_content, View.GONE);
        } else {
            remoteViews.setTextViewText(R.id.clean_guide_content, notificationModel.description);
        }
        remoteViews.setImageViewResource(R.id.action_btn_bg, notificationModel.buttonBgDrawableId);

        return remoteViews;
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse) {
        return getPendingIntent(action, autoCollapse, null);
    }

    public interface ExtraProvider {
        void onAddExtras(Intent intent);
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse, NotificationCondition.ExtraProvider extras) {
        Context context = HSApplication.getContext();
        int requestCode = (int) System.currentTimeMillis();
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra(EXTRA_AUTO_COLLAPSE, autoCollapse);
        if (extras != null) {
            extras.onAddExtras(intent);
        }
        intent.setAction(action);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

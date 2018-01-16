package com.honeycomb.colorphone.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import com.acb.utils.ConcurrentUtils;
import com.colorphone.lock.ScreenStatusReceiver;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.activity.NotificationSettingsActivity;
import com.honeycomb.colorphone.boost.BoostAnimationManager;
import com.colorphone.lock.util.PreferenceHelper;
import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.BoostAutoPilotUtils;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.receiver.UserPresentReceiver;
import com.honeycomb.colorphone.util.DeviceUtils;
import com.honeycomb.colorphone.util.Thunk;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class NotificationCondition implements INotificationObserver {
    public static final String TAG = "NotificationCondition";

    static final String EXTRA_AUTO_COLLAPSE = "auto_collapse";
    static final String EXTRA_NOTIFICATION_ID = "notification_id";
    static final String EXTRA_NOTIFICATION_TYPE = "notification_type";

    private static final String NOTIFICATION_HISTORY = "NOTIFICATION_HISTORY";
    private static final String BOOST_PLUS = "BoostPlus";

    private static final int[] ICON_CONTAINER_RES_ID = {
            R.id.recentest_notification_icon_0,
            R.id.recentest_notification_icon_1,
            R.id.recentest_notification_icon_2,
            R.id.recentest_notification_icon_3,
//            R.id.recentest_notification_icon_4
    };

    private static final String PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_A_TIME = "boost_plus_last_notification_time";
    private static final String PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_B_TIME = "boost_plus_last_notification_b_time";

    public static final String NOTIFICATION_CHECK_DONE = "notification_check_done";
    public static final String KEY_NOTIFICATION_TYPE = "key_notification_type";

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_BOOST_PLUS_NOTIFICATION = false && BuildConfig.DEBUG;

    private static final int NOTIFICATION_ID_BOOST_PLUS = 10005;

    private static final int CHECK_STATE_START = -1;
    public static final int NOTIFICATION_TYPE_BOOST_PLUS = NOTIFICATION_ID_BOOST_PLUS;
    private static final int CHECK_STATE_DONE = -2;

    private static final int EVENT_CHECK_NEXT_NOTIFICATION = 100;

    // 等待通知检查条件时间，超时认为失败检查下一条。
    // 比如 boost+ 或者 junk clean 扫描用时。这里目前没做处理，所以最好是时间足够长，保证能完成扫描。
    private static final long CHECK_NOTIFICATION_TIMEOUT     = DateUtils.MINUTE_IN_MILLIS;
//    // 没有打开相应功能模块的时间
//    public static final long NOT_OPEN_FEATURE_INTERVAL       = HSConfig.optInteger(30, "Application", "NotificationSystem", "CleanTimeDelay") * DateUtils.MINUTE_IN_MILLIS;
//    // 两条消息之间的时间间隔
      // 替换为 BoostAutoPilotUtils.getBoostPushInterval()
    private static final long CHECK_NOTIFICATION_INTERVAL    = HSConfig.optInteger(60, "Application", "NotificationSystem", "PushDelayTime") * DateUtils.MINUTE_IN_MILLIS;
    // 亮屏之后到检查通知的时间
    private static final long AFTER_UNLOCK_TIME = 3 * DateUtils.SECOND_IN_MILLIS;
    // 同一类型的消息的时间间隔 (需求为 0 小时)
    // 替换为 BoostAutoPilotUtils.getBoostPushInterval()
    private static final long SAME_NOTIFICATION_INTERVAL     = HSConfig.optInteger(0, "Application", "NotificationSystem", "DelayTimePerPush") * DateUtils.HOUR_IN_MILLIS;
    // 每天最多通知条数 (需求为 24 小时 6 条)
    // 替换为 BoostAutoPilotUtils.getBoostPushMaxCount()
//    private static final int NOTIFICATION_LIMIT_IN_DAY = HSConfig.optInteger(6, "Application", "NotificationSystem", "MaxTimesNotification");
//    // 每天同类最多通知条数 (需求为 24 小时 2 条)
//    private static final int SAME_NOTIFICATION_LIMIT_IN_DAY = HSConfig.optInteger(2, "Application", "NotificationSystem", "MaxTimesPerNotification");
//    // 通知消息自动消失时间
//    private static final long AUTO_CANCEL_NOTIFICATION_TIME = HSConfig.optInteger(1, "Application", "NotificationSystem", "NotificationDisplay") * DateUtils.HOUR_IN_MILLIS;

//    private static int CPU_ALERT_TEMPERATURE = HSConfig.optInteger(40, "Application", "NotificationSystem", "CPUTemp");
//    private static int LOW_BATTERY = HSConfig.optInteger(30, "Application", "NotificationSystem", "BatteryAlarmA");
//    private static int HIGH_BATTERY = HSConfig.optInteger(50, "Application", "NotificationSystem", "BatteryAlarmB");
//    private static int BATTERY_APPS = HSConfig.optInteger(3, "Application", "NotificationSystem", "BatteryApp");
//    private static int JUNK_CLEAN_NOTIFICATION_SIZE = HSConfig.optInteger(80, "Application", "NotificationSystem", "JunkAlarm") * 1024 * 1024;
//    private static int BOOST_RAM = HSConfig.optInteger(60, "Application", "NotificationSystem", "BoostAlarmA");
    private static int BOOST_RAM = 60; //HSConfig.optInteger(60, "Application", "NotificationSystem", "BoostAlarmA");
    private static int BOOST_APPS = HSConfig.optInteger(3, "Application", "NotificationSystem", "BoostAalarmB");

    private Context context;
    private static NotificationCondition sInstance;
    private List<NotificationHolder> notificationHolderList;
    private int checkState = CHECK_STATE_DONE;
    @SuppressWarnings("WeakerAccess") public int runningApps = -1; // Public visibility for test
    private long lastScreenOnTime;
    private NotificationHolder lastHolder;
//    private boolean isUnlock = false;
    private static long lastSendFeatureNotificationTime = 0;

    @SuppressLint("HandlerLeak") // NotificationCondition instance has process-wide life cycle, leak is not a concern here
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EVENT_CHECK_NEXT_NOTIFICATION:
                    checkNextNotification();
                    break;
                default:
                    break;

            }
        }
    };

    public synchronized static void init() {
        if (sInstance == null) {
            sInstance = new NotificationCondition(HSApplication.getContext());
        }
    }

    public static NotificationCondition getsInstance() {
        if (sInstance == null) {
            init();
        }
        return sInstance;
    }

    /* Public visibility for test */
    @SuppressWarnings("WeakerAccess")
    public NotificationCondition(Context context) {
        this.context = context;
        notificationHolderList = new ArrayList<>(BoostAutoPilotUtils.getBoostPushMaxCount());
        readFromPref();
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_CHECK_DONE, this);
        HSGlobalNotificationCenter.addObserver(UserPresentReceiver.USER_PRESENT, this);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        HSLog.d(TAG, "onReceive s == " + s);
        switch (s) {
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                break;
            case NOTIFICATION_CHECK_DONE:
                if (hsBundle != null) {
                    int type = hsBundle.getInt(KEY_NOTIFICATION_TYPE);
                    if (checkState != CHECK_STATE_DONE && checkState == type) {
                        mHandler.removeMessages(EVENT_CHECK_NEXT_NOTIFICATION);
                        checkNextNotification();
                    }
                }
                break;
            case UserPresentReceiver.USER_PRESENT:
                mHandler.postDelayed(new Runnable() {
                    @Override public void run() {
                        NotificationCondition.this.sendNotificationIfNeeded();
                    }
                }, AFTER_UNLOCK_TIME);
                break;
            default:
                break;
        }
    }

    public void sendNotificationIfNeeded() {
        if (Utils.isKeyguardLocked(context, true)) {
            HSLog.d(TAG, "没有解锁。");
            return;
        }

        if (Utils.isNewUserInDNDStatus()) {
            HSLog.d(TAG, "新用户 2 小时内不提示。");
        }

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) <= 5) {
            HSLog.d(TAG, "0-6 点不提示");
            return;
        }

        long now = System.currentTimeMillis();
        long screenOnTime = ScreenStatusReceiver.getScreenOnTime();
        long keepOnTime = now - screenOnTime;
        HSLog.d(TAG, "sendNotificationIfNeeded keepOnTime == " + keepOnTime);
        if (keepOnTime > AFTER_UNLOCK_TIME && lastScreenOnTime != screenOnTime) {
            if (lastHolder == null || now - lastHolder.sendTime > BoostAutoPilotUtils.getBoostPushInterval()) {
                lastScreenOnTime = screenOnTime;
                checkHolders();
                if (notificationHolderList.size() >= BoostAutoPilotUtils.getBoostPushMaxCount()) {
                    HSLog.d(TAG, String.format(Locale.getDefault(), "24 小时，超过 %d 个", BoostAutoPilotUtils.getBoostPushMaxCount()));
                    return;
                }
                trySendNotificationInOrder();
            } else {
                HSLog.d(TAG, String.format("%s 小时内发送过消息", String.valueOf(BoostAutoPilotUtils.getBoostPushInterval())));
            }
        } else {
            HSLog.d(TAG, "亮屏不超过 1 分钟 或者 本次亮屏已经判断过通知");
        }
    }

    private void trySendNotificationInOrder() {
        if (checkState != CHECK_STATE_DONE) {
            return;
        }
        runningApps = -1;
        DeviceUtils.getRunningPackageListFromMemory(false, new DeviceUtils.RunningAppsListener() {
            @Override
            public void onScanFinished(List<String> list, long l) {
                HSLog.d(TAG, "onScanFinished appSize == " + list.size());
                runningApps = list.size();
                if (mHandler.hasMessages(EVENT_CHECK_NEXT_NOTIFICATION)) {
                    if (checkState == NOTIFICATION_TYPE_BOOST_PLUS) {
                        mHandler.removeMessages(EVENT_CHECK_NEXT_NOTIFICATION);
                        trySendNotification();
                    }
                }
            }
        });

        checkState = CHECK_STATE_START;
        checkNextNotification();
    }

    @Thunk void checkNextNotification() {
        switch (checkState) {
            case CHECK_STATE_START:
                checkState = NOTIFICATION_TYPE_BOOST_PLUS;
                break;
            case NOTIFICATION_TYPE_BOOST_PLUS:
            default:
                HSLog.d(TAG, "checkNextNotification Done");
                checkState = CHECK_STATE_DONE;
                return;
        }

        HSLog.d(TAG, "checkNextNotification checkState == " + checkState);

        trySendNotification();
    }

    private void trySendNotification() {
        if (trySendNotification(checkState)) {
            if (checkState != CHECK_STATE_DONE) {
                mHandler.sendEmptyMessageDelayed(EVENT_CHECK_NEXT_NOTIFICATION, CHECK_NOTIFICATION_TIMEOUT);
            }
        } else {
            checkNextNotification();
        }
    }

    private boolean trySendNotification(int type) {
        boolean ret = true;
        HSLog.d(TAG, "trySendNotification type == " + type);
        switch (type) {
            case NOTIFICATION_TYPE_BOOST_PLUS:
                ret = sendBoostPlusNotificationIfNeeded();
                break;
            default:
                break;
        }
        return ret;
    }

    void recordNotification(int notifyId, int type, long time) {
        HSLog.d(TAG, "recordNotification  id == " + notifyId + "  curType == " + checkState);
        checkState = CHECK_STATE_DONE;
        mHandler.removeMessages(EVENT_CHECK_NEXT_NOTIFICATION);

        lastHolder = new NotificationHolder();
        lastHolder.nId = notifyId;
        lastHolder.nType = type;
        lastHolder.sendTime = System.currentTimeMillis();

        notificationHolderList.add(lastHolder);

        saveToPref();
    }

    private void checkHolders() {
        int size = notificationHolderList.size();
        if (size > 0) {
            NotificationHolder holder;
            for (int i = size - 1; i > 0; i--) {
                holder = notificationHolderList.get(i);
                if (!holder.isValid()) {
                    notificationHolderList.remove(holder);
                }
            }
            saveToPref();
        }
        HSLog.d(TAG, "checkHolders size == " + notificationHolderList.size());
    }

    private void readFromPref() {
//        PreferenceHelper.get(LauncherFiles.NOTIFICATION_PREFS).putString(NOTIFICATION_HISTORY, "");
        String history = PreferenceHelper.get(Constants.NOTIFICATION_PREFS).getString(NOTIFICATION_HISTORY, "");
        HSLog.d(TAG, "readFromPref history == " + history);
        if (!TextUtils.isEmpty(history)) {
            notificationHolderList.clear();
            try {
                JSONArray jArray = new JSONArray(history);
                HSLog.d(TAG, "readFromPref jArray == " + jArray);
                if (jArray.length() > 0) {
                    for (int i = 0; i < jArray.length(); i++) {
                        HSLog.d(TAG, "readFromPref nStr == " + jArray.get(i));
                        lastHolder = new NotificationHolder();
                        lastHolder.fromJSON((JSONObject) jArray.get(i));
                        HSLog.d(TAG, "readFromPref holder == " + lastHolder);
                        if (lastHolder.isValid()) {
                            notificationHolderList.add(lastHolder);
                        } else {
                            lastHolder = null;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HSLog.d(TAG, "readFromPref size == " + notificationHolderList.size());
        }
    }

    private void saveToPref() {
        if (notificationHolderList.size() > 0) {
//            List<String> saveStr = new ArrayList<>(notificationHolderList.size());
            JSONArray jArray = new JSONArray();
            for (NotificationHolder holder : notificationHolderList) {
                jArray.put(holder.toJSON());
            }
            PreferenceHelper.get(Constants.NOTIFICATION_PREFS).putString(NOTIFICATION_HISTORY, jArray.toString());
        } else {
            PreferenceHelper.get(Constants.NOTIFICATION_PREFS).putString(NOTIFICATION_HISTORY, "");
        }
    }

    private static void setLastNotifyBoostPlusTime(String prefKey) {
        PreferenceHelper.get(Constants.NOTIFICATION_PREFS).putLong(prefKey, System.currentTimeMillis());
    }

    private static long getLastNotifyBoostPlusTime(String prefKey) {
        return PreferenceHelper.get(Constants.NOTIFICATION_PREFS).getLong(prefKey, 0);
    }

    private boolean shouldNotifyBoostPlus() {
        if (DEBUG_BOOST_PLUS_NOTIFICATION) {
            HSLog.d(TAG, "shouldNotifyBoostPlus  无视自身条件发送");
            return true;
        }

        return NotificationSettingsActivity.isNotificationBoostOn() && BoostAutoPilotUtils.isBoostPushEnable();
    }

    private boolean sendBoostPlusNotificationIfNeeded() {
        // Boost+ notification
        if (shouldNotifyBoostPlus()) {

            if (runningApps == -1) {
                return true;
            }

            int ram = DeviceManager.getInstance().getRamUsage();
            if (ram > BOOST_RAM && checkLastNotificationInterval(getLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_A_TIME))) {
                sendBoostPlusNotification(true);
                return true;
            } else {
                if (ram > BOOST_RAM) {
                    HSLog.d(TAG, "BoostPlus_A 间隔时间少于 1 天");
                } else {
                    HSLog.d(TAG, "BoostPlus_A RAM 少于 60%");
                }
            }
            if (runningApps >= BOOST_APPS) {
                if (checkLastNotificationInterval(getLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_B_TIME))) {
                    sendBoostPlusNotification(false);
                    return true;
                } else {
                    HSLog.d(TAG, "BoostPlus_B 间隔时间少于 1 天");
                }

            } else {
                HSLog.d(TAG, "sendBoostPlusNotificationIfNeeded 可清理应用数太少：" + runningApps);
            }
        }
        return false;
    }

    /* Public visibility for test */
    @SuppressWarnings("WeakerAccess")
    public void sendBoostPlusNotification(boolean typeA) {
        HSLog.d(TAG, "Show boost+ notification when screen on. is typeA? " + typeA);
        LocalNotification localNotification = new LocalNotification();
        localNotification.notificationId = NOTIFICATION_ID_BOOST_PLUS;
        String title;
        final String type;
        String number;
        if (typeA) {
            number = String.valueOf(DeviceManager.getInstance().getRamUsage());
            title = context.getString(R.string.notification_boost_plus_title_ram, number);
            type = BOOST_PLUS + "_A";
            setLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_A_TIME);
            localNotification.pictorialContentType = LocalNotification.PICTORIAL_CONTENT_TYPE_BAR;
        } else {
            number = String.valueOf(runningApps);
            title = context.getString(R.string.notification_boost_plus_title, number);
            type = BOOST_PLUS + "_B";
            setLastNotifyBoostPlusTime(PREF_KEY_BOOST_PLUS_LAST_NOTIFICATION_B_TIME);
            localNotification.pictorialContentType = LocalNotification.PICTORIAL_CONTENT_TYPE_ICONS;
        }
        SpannableString titleSpannableString = new SpannableString(title);
        int start = title.indexOf(number);
        int end = start + number.length() + (typeA ? 1 : 0) /* For % */;
        titleSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleSpannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        localNotification.title = titleSpannableString;

        String descriptionSentence = context.getString(R.string.notification_boost_plus_description);
        String descriptionBoldWord = context.getString(R.string.notification_boost_plus_description_bold_word);
        int startPos = descriptionSentence.indexOf(descriptionBoldWord);
        if (startPos != -1) {
            int endPos = startPos + descriptionBoldWord.length();
            SpannableString descriptionSpannableString = new SpannableString(descriptionSentence);
            descriptionSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_description_bold)),
                    startPos, endPos, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            descriptionSpannableString.setSpan(new StyleSpan(Typeface.BOLD), startPos, endPos, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            localNotification.description = descriptionSpannableString;
        } else {
            localNotification.description = descriptionSentence;
        }

        localNotification.buttonText = context.getString(R.string.boost_notification_low_ram_btn);
        localNotification.buttonBgDrawableId = R.drawable.notification_inset_and_real_style_boost_btn_bg;
        localNotification.primaryColor = ContextCompat.getColor(context, R.color.notification_boost_primary);
        localNotification.smallIconDrawableId = R.drawable.notification_boost_plus_small_icon;
        localNotification.isHeadsUp = HSConfig.optBoolean(false, "Application", "NotificationSystem", "BoostSuspension");
        localNotification.iconDrawableIdRealStyle = R.drawable.push_icon_boost_real_style;

        final int notificationId = localNotification.notificationId;
        localNotification.pendingIntent = getPendingIntent(
                NotificationConstants.ACTION_BOOST_PLUS, true,
                new ExtraProvider() {
                    @Override
                    public void onAddExtras(Intent intent) {
                        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
                        intent.putExtra(EXTRA_NOTIFICATION_TYPE, type);
                    }
                });
        showNotification(localNotification);
//        logNotificationPushed(type);
    }

    private static void showNotification(LocalNotification notificationModel) {
        if (null == notificationModel) {
            return;
        }
        switch (notificationModel.notificationId) {
            case NOTIFICATION_ID_BOOST_PLUS:
                // Pass
                break;
            default:
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

        lastSendFeatureNotificationTime = System.currentTimeMillis();
        notify(notificationModel.notificationId, Utils.buildNotificationSafely(builder));
        BoostAutoPilotUtils.logBoostPushShow();
    }

    private static RemoteViews createRealStyleNotification(LocalNotification notificationModel) {
        int layoutId;
        if (notificationModel.pictorialContentType == LocalNotification.PICTORIAL_CONTENT_TYPE_NONE) {
            layoutId = R.layout.notification_real_style_two_lines;
        } else {
            layoutId = R.layout.notification_real_style;
        }
        RemoteViews remoteViews = new RemoteViews(HSApplication.getContext().getPackageName(), layoutId);
        remoteViews.setImageViewResource(R.id.protect_image, notificationModel.iconDrawableIdRealStyle);
        TitleSplit titleSplit = TitleSplit.createSplit(notificationModel.title);
        remoteViews.setTextViewText(R.id.notification_quantitative_figure_text, titleSplit.quantitative);
        remoteViews.setTextColor(R.id.notification_quantitative_figure_text, notificationModel.primaryColor);
        remoteViews.setTextViewText(R.id.notification_quantitative_unit_text, titleSplit.unit);
        remoteViews.setTextColor(R.id.notification_quantitative_unit_text, notificationModel.primaryColor);
        remoteViews.setTextViewText(R.id.block_title_text, titleSplit.descriptive);
        remoteViews.setTextViewText(R.id.notification_btn_text, notificationModel.buttonText);
        remoteViews.setImageViewResource(R.id.notification_btn_bg, notificationModel.buttonBgDrawableId);

        configurePictorialContent(notificationModel, remoteViews);

        return remoteViews;
    }

    private static void configurePictorialContent(
            LocalNotification notificationModel, RemoteViews remoteViews) {
        switch (notificationModel.pictorialContentType) {
            case LocalNotification.PICTORIAL_CONTENT_TYPE_NONE:
                remoteViews.setViewVisibility(R.id.notification_pictorial_layout, View.GONE);
                break;
            case LocalNotification.PICTORIAL_CONTENT_TYPE_ICONS:
                configureIconsPictorialContent(remoteViews);
                break;
            case LocalNotification.PICTORIAL_CONTENT_TYPE_BAR:
                configureBarPictorialContent(notificationModel, remoteViews);
                break;
        }
    }

    private static void configureIconsPictorialContent(RemoteViews remoteViews) {
        remoteViews.setViewVisibility(R.id.notification_pictorial_layout, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.notification_icons_layout, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.notification_quantitative_bar, View.GONE);
        for (int id : ICON_CONTAINER_RES_ID) {
            remoteViews.setViewVisibility(id, View.GONE);
        }
        BoostAnimationManager boostAnimationManager = new BoostAnimationManager(0, 0);
        Bitmap[] bitmaps = boostAnimationManager.getBoostAppIconBitmaps(HSApplication.getContext());
        for (int i = 0; i < bitmaps.length && i < ICON_CONTAINER_RES_ID.length; i++) {
            remoteViews.setViewVisibility(ICON_CONTAINER_RES_ID[i], View.VISIBLE);
            if (i != 4) {
                remoteViews.setImageViewBitmap(ICON_CONTAINER_RES_ID[i], bitmaps[i]);
            }
        }
    }

    private static void configureBarPictorialContent(
            LocalNotification notificationModel, RemoteViews remoteViews) {
        remoteViews.setViewVisibility(R.id.notification_pictorial_layout, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.notification_icons_layout, View.GONE);
        remoteViews.setViewVisibility(R.id.notification_quantitative_bar, View.VISIBLE);
        TitleSplit titleSplit = TitleSplit.createSplit(notificationModel.title);
        try {
            remoteViews.setImageViewBitmap(R.id.notification_quantitative_bar,
                    getQuantitativeBarBitmap(
                            Integer.parseInt(titleSplit.quantitative),
                            notificationModel.primaryColor));
        } catch (NumberFormatException | OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    private static Bitmap getQuantitativeBarBitmap(int quantity, int colorPrimary) {
        float width = Utils.pxFromDp(120);
        float halfHeight = Utils.pxFromDp(7.5f);
        float strokeWidth = Utils.pxFromDp(3.3f);
        float left = strokeWidth / 2;
        float right = width - strokeWidth / 2;

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) (halfHeight * 2), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(0xffdbdbdb);
        paint.setStrokeWidth(strokeWidth);

        Canvas c = new Canvas(bitmap);
        c.drawLine(left, halfHeight, right, halfHeight, paint);
        paint.setColor(colorPrimary);
        c.drawLine(left, halfHeight, left + (quantity / 100f) * (right - left), halfHeight, paint);

        return bitmap;
    }

    /**
     * We split the whole title ("42 MB Junk Files Found!") into quantitative, unit and descriptive
     * parts in order to bind notification UI of "inset" and "real" styles. In such way we don't
     * have to split all the string resources (and their translations).
     */
    private static class TitleSplit {
        private static final String[] UNITS = {"%", "°C", "°F", "MB"};

        private static final int SCAN_MODE_UNTIL_FIGURE_CHAR = 0;
        private static final int SCAN_MODE_WHILE_FIGURE_CHAR = 1;

        private String quantitative = ""; // eg. 42
        private String unit = "";         // eg. % / °C / MB
        private String descriptive;       // eg. Junk Files Found!

        private static TitleSplit createSplit(CharSequence whole) {
            TitleSplit split = new TitleSplit();
            String wholeString = whole.toString();

            int figureEnd = scanForFigure(wholeString, 0, SCAN_MODE_WHILE_FIGURE_CHAR);
            split.quantitative = wholeString.substring(0, figureEnd);

            boolean hasQuantityAtBeginning = (figureEnd > 0);
            if (hasQuantityAtBeginning) {
                String withoutQuantitative = wholeString.substring(figureEnd);
                split.unit = findUnitAtBeginning(withoutQuantitative);
                split.descriptive = withoutQuantitative.replace(split.unit, "").trim();
            } else {
                int start = scanForFigure(wholeString, 0, SCAN_MODE_UNTIL_FIGURE_CHAR);
                int end = scanForFigure(wholeString, start, SCAN_MODE_WHILE_FIGURE_CHAR);
                if (start < whole.length()) {
                    split.quantitative = wholeString.substring(start, end);
                }
                String afterQuantitative = wholeString.substring(end);
                split.unit = findUnitAtBeginning(afterQuantitative);
                split.descriptive = wholeString;
            }

            return split;
        }

        @SuppressWarnings("StatementWithEmptyBody")
        private static int scanForFigure(String text, int start, int mode) {
            int current = start;
            int length = text.length();
            while ((mode == SCAN_MODE_WHILE_FIGURE_CHAR ?
                    isFigureCharacter(text.charAt(current++)) : (!isFigureCharacter(text.charAt(current++))) )
                    && current < length);
            if (current < length) current--;
            return current;
        }

        private static boolean isFigureCharacter(char ch) {
            return Character.isDigit(ch) || ch == '.' || ch == '-';
        }

        private static String findUnitAtBeginning(String text) {
            for (String unit : UNITS) {
                if (text.startsWith(unit)) {
                    return unit;
                }
            }
            return "";
        }
    }

    private boolean checkNotificationCountByType(int type, int limitSize) {
        int count = 0;
        for (NotificationHolder holder : notificationHolderList) {
            if (holder.nType == type) {
                count++;
            }
        }
        HSLog.d(TAG, "checkCountByType type == " + type + "  count == " + count + "  limit == " + limitSize);
        return limitSize > count;
    }

    private static boolean checkLastNotificationInterval(long last) {
        return (System.currentTimeMillis() - last) > BoostAutoPilotUtils.getBoostPushInterval();
    }

    static class NotificationHolder {
        private static final String ID = "id";
        private static final String TYPE = "type";
        private static final String TIME = "time";

        int nId;
        int nType;
        long sendTime;

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

        NotificationHolder fromJSON(JSONObject jObj) {
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

        @Override public String toString() {
            return "NotificationHolder{" +
                    "nId=" + nId +
                    ", nType=" + nType +
                    ", sendTime=" + sendTime +
                    '}';
        }
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse) {
        return getPendingIntent(action, autoCollapse, null);
    }

    public interface ExtraProvider {
        void onAddExtras(Intent intent);
    }

    public PendingIntent getPendingIntent(String action, boolean autoCollapse, ExtraProvider extras) {
        Context context = HSApplication.getContext();
        int requestCode = (int) System.currentTimeMillis();
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.putExtra(EXTRA_AUTO_COLLAPSE, autoCollapse);
        if (extras != null) {
            extras.onAddExtras(intent);
        }
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 所有的 notification 通知都要调用这个方法来发送
     *
     * @param id
     * @param notification
     */
    public static void notify(final int id, final Notification notification) {
        notify(id, id, notification);
    }

    public static void notify(final int id, final int type, final Notification notification) {
        if (notification == null) {
            return;
        }

        if (sInstance != null) {
            sInstance.recordNotification(id, type, notification.when);
        }

        Runnable notifyRunnable = new Runnable() {
            @Override
            public void run() {
                android.app.NotificationManager notifyMgr = (android.app.NotificationManager)
                        HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                try {
                    HSLog.d(TAG, "notify()");
                    notifyMgr.notify(id, notification);
                } catch (Exception e) {
                    CrashlyticsCore.getInstance().logException(e);
                }
            }
        };

        ConcurrentUtils.postOnSingleThreadExecutor(notifyRunnable); // Keep notifications in original order
    }
}

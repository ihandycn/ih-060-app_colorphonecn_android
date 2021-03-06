package com.colorphone.lock.lockscreen.locker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.R;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.AppNotificationInfo;
import com.colorphone.lock.lockscreen.LockNotificationManager;
import com.colorphone.lock.lockscreen.NotificationObserver;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.colorphone.lock.ScreenStatusReceiver.NOTIFICATION_SCREEN_ON;

public class NotificationWindowHolder implements NotificationObserver, INotificationObserver {

    public static final String NOTIFY_KEY_REMOVE_MESSAGE = "notify_key_remove_message";
    public static final String BUNDLE_KEY_PACKAGE_NAME = "bundle_key_package_name";
    public static final int SOURCE_LOCKER = 1;
    public static final int SOURCE_CHARGING = 2;

    private View mContainerRoot;
    private LinearLayout mNotificationContainer;
    private RelativeLayout mNotificationWindow;
    private SlidingNotificationLayout mSlidingWindow;

    private ImageView mSourceAppAvatar;
    private TextView mAppNameAndSendTime;
    private TextView mSenderName;
    private ImageView mSenderAvatar;
    private TextView mNotificationContent;

    private int receiveNumberCount;
    private String receiveNumber;
    private int showNumber;
    private int yCoordinateOfAboveNotification;

    private boolean isObtainDistanceToTop = false;
    private AppNotificationInfo mAppNotificationInfo;
    private final NotificationClickCallback mNotificationClickCallback;

    private final int NO_NOTIFICATION_SHOWING = 0;
    private final int ONE_NOTIFICATION_SHOWING = 1;
    private final int TWO_NOTIFICATION_SHOWING = 2;
    private final int SAME_AS_EXISTED_NOTIFICATION = 0;
    private final int LOWER_PRIORITY_THAN_NOTIFICATION_AT_POSITION_0 = 1;
    private final int SIMPLE_SHOW = 2;

    private final int mSource;

    public NotificationWindowHolder(ViewGroup rootView, int source, @Nullable NotificationClickCallback callback) {
        mSource = source;
        mContainerRoot = rootView;
        mNotificationClickCallback = callback;
        showNumber = 0;
        receiveNumberCount = 0;

        mNotificationContainer = findViewById(R.id.test_view);
        isObtainDistanceToTop = true;
    }

    private void bindNotification(final View view, AppNotificationInfo info) {
        mSlidingWindow = view.findViewById(R.id.lock_sliding_window);
        mSlidingWindow.setClickable(true);
        mNotificationWindow = view.findViewById(R.id.lock_notification_window);
        mNotificationWindow.setAlpha(0.9f);

        mSourceAppAvatar = view.findViewById(R.id.source_app_avatar);
        mAppNameAndSendTime = view.findViewById(R.id.source_app_name);
        mSenderAvatar = view.findViewById(R.id.sender_avatar);
        mSenderName = view.findViewById(R.id.sender_name);
        mNotificationContent = view.findViewById(R.id.notification_content);
        mSlidingWindow.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationWindow.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
            mSenderAvatar.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(3.3f), false));
        }
        bindViewAndInfo(view, info);

        mNotificationWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickForNotification(view);
            }
        });

        mSlidingWindow.setOnViewDismissCallback(new SlidingNotificationLayout.OnViewDismissCallback() {
            @Override
            public void onDismiss(View v) {
                removeDismissingNotification(v);
                notifyForLockerTimeSizeChange();
                notifyForChargingNumberSizeChange();
            }
        });
        changeNotificaitonWindow(info, view);
    }


    private int getNotificationCount() {
        showNumber = mNotificationContainer.getChildCount();
        return showNumber;
    }

    private void addNewNotification(int index) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_layout, mNotificationContainer, false);
        view.setVisibility(View.GONE);
        mNotificationContainer.addView(view, index);
    }

    private void removeDismissingNotification(View view) {

        mNotificationContainer.removeView(view);
        view.setVisibility(View.GONE);

    }

    private <T extends View> T findViewById(int id) {
        return mContainerRoot.findViewById(id);
    }

    private Context getContext() {
        return mContainerRoot.getContext();
    }

    private void onClickForNotification(View v) {
        if (getInfo(v) == null) {
            return;
        }

        LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Click",
                getInfo(v).packageName);

        LockNotificationManager.getInstance().setClickedNotification(getInfo(v));

        if (mNotificationClickCallback != null) {
            mNotificationClickCallback.onNotificationClick();
        }
    }

    private static void startNotificationIntent(AppNotificationInfo appNotificationInfo) {
        boolean userNotClicked = appNotificationInfo != null;
        if (userNotClicked) {
            PendingIntent pendingIntent = appNotificationInfo.notification.contentIntent;
            if (pendingIntent != null) {
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
            LockNotificationManager.getInstance().setClickedNotification(null);
        }
    }

    private AppNotificationInfo getInfo(View v) {
        if (v.getTag() != null) {
            return (AppNotificationInfo) v.getTag();
        }
        return null;
    }

    private void bindViewAndInfo(View view, AppNotificationInfo info) {
        view.setTag(info);
    }

    private void changeNotificaitonWindow(AppNotificationInfo info, View view) {

        updateNotificationView(info, view);
        mSlidingWindow.setVisibility(View.VISIBLE);
        view.setVisibility(View.VISIBLE);
    }

    private void updateNotificationView(AppNotificationInfo info, View view) {
        if (ScreenStatusReceiver.isScreenOn()) {
            if (receiveNumberCount <= 5) {
                receiveNumber = String.valueOf(receiveNumberCount);
            } else {
                receiveNumber = "above 5";
            }
            LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Show",
                    info.packageName, receiveNumber, String.valueOf(getNotificationCount()));
        }
        mSenderName.setText(info.title);
        mNotificationContent.setText(info.content);
        if (info.notification.largeIcon != null) {
            mSenderAvatar.setVisibility(View.VISIBLE);
            mSenderAvatar.setImageBitmap(info.notification.largeIcon);
        } else {
            mSenderAvatar.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mSourceAppAvatar.setBackground(LockNotificationManager.getAppIcon(info.packageName));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("HH:mm");
        mAppNameAndSendTime.setText(LockNotificationManager.getAppName(info.packageName) + " " + "·" + " " + sdf.format(info.when));
    }

    @Override
    public void onReceive(AppNotificationInfo info) {
        LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Receive", info.packageName);
        boolean userEnabled = mSource == SOURCE_LOCKER ?
                LockerSettings.needShowNotificationLocker()
                : LockerSettings.needShowNotificationCharging();
        List<String> whiteList = (List<String>) HSConfig.getList("Application", "Locker", "Notification", "WhiteList");
        if (whiteList.contains(info.packageName)) {
            receiveNumberCount ++;
        }
        if (userEnabled) {
            mAppNotificationInfo = info;
            showNotificaiton(info);
        }
    }

    private void showNotificaiton(AppNotificationInfo info) {
        if (HSConfig.optBoolean(false,"Application", "Locker", "Notification", "ShowMultiple")) {
            showNotificaitonMultiple(info);
        } else {
            showNotificaitonSingle(info);
        }
    }

    private void showNotificaitonSingle(AppNotificationInfo info) {
        if (getNotificationCount() == NO_NOTIFICATION_SHOWING) {
            addNewNotification(0);
        }
        bindNotification(mNotificationContainer.getChildAt(0), info);
    }

    private void showNotificaitonMultiple(AppNotificationInfo info) {
        switch (judgeNotificationShowMode(info)) {
            case SAME_AS_EXISTED_NOTIFICATION:
                bindNotification(mNotificationContainer.getChildAt(getPositionOfSameSource(info)), info);
                break;
            case LOWER_PRIORITY_THAN_NOTIFICATION_AT_POSITION_0:
                if (getNotificationCount() == ONE_NOTIFICATION_SHOWING) {
                    addNewNotification(1);
                }
                bindNotification(mNotificationContainer.getChildAt(1), info);
                break;
            case SIMPLE_SHOW:
                if (getNotificationCount() == TWO_NOTIFICATION_SHOWING) {
                    removeDismissingNotification(mNotificationContainer.getChildAt(1));
                }
                addNewNotification(0);
                bindNotification(mNotificationContainer.getChildAt(0), info);
                break;
            default:
                break;
        }

        notifyForLockerTimeSizeChange();
        notifyForChargingNumberSizeChange();
    }

    private int getPositionOfSameSource(AppNotificationInfo info) {
        int getPositionOfSameSource = -1;
        for (int i = 0; i < getNotificationCount(); i++) {
            AppNotificationInfo existInfo = (AppNotificationInfo) mNotificationContainer.getChildAt(i).getTag();
            getPositionOfSameSource = (existInfo.packageName.equalsIgnoreCase(info.packageName) ) ? i : -1;
            if (getPositionOfSameSource != -1) {
                break;
            }
        }
        return getPositionOfSameSource;
    }

    private boolean isHigherPriorityPackage(AppNotificationInfo info) {
        return "com.tencent.mobileqq".equalsIgnoreCase(info.packageName)
                || "com.tencent.mm".equalsIgnoreCase(info.packageName)
                || "com.android.mms".equalsIgnoreCase(info.packageName)
                || "com.eg.android.AlipayGphone".equalsIgnoreCase(info.packageName);
    }

    private boolean isLowerPriorityThanNotificationAtPosition0(AppNotificationInfo info) {
        AppNotificationInfo existInfo = (AppNotificationInfo) mNotificationContainer.getChildAt(0).getTag();
        return isHigherPriorityPackage(existInfo) && !isHigherPriorityPackage(info);
    }

    private int judgeNotificationShowMode(AppNotificationInfo info) {
        if (getNotificationCount() != 0 && getPositionOfSameSource(info) != -1) {
            return SAME_AS_EXISTED_NOTIFICATION;
        }else if (getNotificationCount() != 0 && isLowerPriorityThanNotificationAtPosition0(info)) {
            return SAME_AS_EXISTED_NOTIFICATION;
        } else {
            return SIMPLE_SHOW;
        }
    }

    private void notifyForLockerTimeSizeChange() {
       if (getNotificationCount() == TWO_NOTIFICATION_SHOWING && isObtainDistanceToTop) {
           obtainDistanceToTop();
        }
        LockNotificationManager.getInstance().notifyForUpdatingLockerTimeSize(getNotificationCount(), yCoordinateOfAboveNotification);
    }

    private void notifyForChargingNumberSizeChange() {
        LockNotificationManager.getInstance().notifyForUpdatingChargingNumberSize(getNotificationCount());
    }

    private void obtainDistanceToTop() {
        int[] position = new int[2];
        mNotificationContainer.getChildAt(0).getLocationOnScreen(position);
        yCoordinateOfAboveNotification = position[1] - Dimensions.pxFromDp(3) - Dimensions.pxFromDp(mNotificationContainer.getChildAt(1).getHeight());
        isObtainDistanceToTop = false;
    }

    private String getSourceName(int source) {
        return source == SOURCE_CHARGING ? " ChargingScreen" : "LockScreen";
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_SCREEN_ON.equalsIgnoreCase(s)) {
            if (mAppNotificationInfo != null) {
                if (receiveNumberCount <= 5) {
                   receiveNumber = String.valueOf(receiveNumberCount);
                } else {
                    receiveNumber = "above 5";
                }
                LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Show",
                        mAppNotificationInfo.packageName, receiveNumber, String.valueOf(getNotificationCount()));
            }
        }
    }

    public interface NotificationClickCallback {
        void onNotificationClick();
    }
}
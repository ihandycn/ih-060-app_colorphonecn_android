package com.colorphone.lock.lockscreen.locker;

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

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.R;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.AppNotificationInfo;
import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.colorphone.lock.lockscreen.LockNotificationManager;
import com.colorphone.lock.lockscreen.NotificationObserver;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
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
    private AppNotificationInfo mAboveInfo;
    private AppNotificationInfo mBelowInfo;
    private final NotificationClickCallback mNotificationClickCallback;

    private final int NO_NOTIFICATION_SHOWING = 0;
    private final int ONE_NOTIFICATION_SHOWING = 1;
    private final int TWO_NOTIFICATION_SHOWING = 2;

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

    private void createNewNotificationWindow(final View view, AppNotificationInfo info) {

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

        mNotificationWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickForNotification(view);
            }
        });

        mSlidingWindow.setOnViewDismissCallback(new SlidingNotificationLayout.OnViewDismissCallback() {
            @Override
            public void onDismiss(View v) {
                unbindDismissingNotification(v);
                notifyForTwoScreen();
            }
        });
        changeNotificaitonWindow(info, view);
    }

    private int getShowNumber() {
        showNumber = mNotificationContainer.getChildCount();
        return showNumber;
    }

    private void bindNewNotification(int index) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_layout, mNotificationContainer, false);
        view.setVisibility(View.GONE);
        mNotificationContainer.addView(view, index);

    }

    private void unbindDismissingNotification(View view) {
        if (mNotificationContainer.indexOfChild(view) == 1) {
            mBelowInfo = mAboveInfo;
        }
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

        if (getContext() instanceof BaseKeyguardActivity) {
            ((BaseKeyguardActivity) getContext()).tryDismissKeyguard(true);
        }
    }

    private AppNotificationInfo getInfo(View v) {
        AppNotificationInfo info = null;
        if (getShowNumber() == TWO_NOTIFICATION_SHOWING) {
            if (mNotificationContainer.indexOfChild(v) == 0) {
                info = mAboveInfo;
            } else if (mNotificationContainer.indexOfChild(v) == 1) {
                info = mBelowInfo;
            }
        } else if (getShowNumber() == ONE_NOTIFICATION_SHOWING) {
            info = mBelowInfo;
        }
        return info;
    }

    private void changeNotificaitonWindow(AppNotificationInfo info, View view) {

        optimizeNotification(info, view);
        mSlidingWindow.setVisibility(View.VISIBLE);
        view.setVisibility(View.VISIBLE);
    }

    private void optimizeNotification(AppNotificationInfo info, View view) {
        if (ScreenStatusReceiver.isScreenOn()) {
            if (receiveNumberCount <= 5) {
                receiveNumber = String.valueOf(receiveNumberCount);
            } else {
                receiveNumber = "above 5";
            }
            LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Show",
                    info.packageName, receiveNumber, String.valueOf(getShowNumber()));
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
        switch (getShowNumber()) {
            case NO_NOTIFICATION_SHOWING:
                bindNewNotification(0);
                createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                break;
            case ONE_NOTIFICATION_SHOWING:
                createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                break;
            default:
                break;
        }
    }

    private void showNotificaitonMultiple(AppNotificationInfo info) {
        switch (getShowNumber()) {
            case NO_NOTIFICATION_SHOWING:
                bindNewNotification(0);
                createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                mBelowInfo = info;
                break;

            case ONE_NOTIFICATION_SHOWING:
                if (isSamePackageAsBelowOne(info)) {
                    createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                    mBelowInfo = info;
                } else if (isHigherPriorityPackage(mBelowInfo) && !isHigherPriorityPackage(info)) {
                    bindNewNotification(0);
                    createNewNotificationWindow(mNotificationContainer.getChildAt(0), mBelowInfo);
                    createNewNotificationWindow(mNotificationContainer.getChildAt(1), info);
                    mAboveInfo = mBelowInfo;
                    mBelowInfo = info;
                } else {
                    bindNewNotification(0);
                    createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                    mAboveInfo = info;
                }
                break;

            case TWO_NOTIFICATION_SHOWING:
                if (BuildConfig.DEBUG) {
                    HSLog.e("LockNotification", "New notification " + mAboveInfo.content + " " + mBelowInfo.content );
                }
                boolean isOptimizeBelowOne = (isHigherPriorityPackage(mAboveInfo) && !isHigherPriorityPackage(info)) || isSamePackageAsBelowOne(info);
                if (isOptimizeBelowOne) {
                    createNewNotificationWindow(mNotificationContainer.getChildAt(1), info);
                    mBelowInfo = info;
                } else if (isSamePackageAsAboveOne(info)) {
                    createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                    mAboveInfo = info;
                } else {
                    createNewNotificationWindow(mNotificationContainer.getChildAt(0), info);
                    createNewNotificationWindow(mNotificationContainer.getChildAt(1), mAboveInfo);
                    mBelowInfo = mAboveInfo;
                    mAboveInfo = info;
                }
                break;

            default:
                break;
        }
        notifyForTwoScreen();
    }

    private boolean isSamePackageAsBelowOne(AppNotificationInfo info) {
        return mBelowInfo.packageName.equalsIgnoreCase(info.packageName);
    }

    private boolean isSamePackageAsAboveOne(AppNotificationInfo info) {
        return mAboveInfo.packageName.equalsIgnoreCase(info.packageName);
    }

    private boolean isHigherPriorityPackage(AppNotificationInfo info) {
        return "com.tencent.mobileqq".equalsIgnoreCase(info.packageName)
                || "com.tencent.mm".equalsIgnoreCase(info.packageName)
                || "com.android.mms".equalsIgnoreCase(info.packageName)
                || "com.eg.android.AlipayGphone".equalsIgnoreCase(info.packageName);
    }

    private void notifyForTwoScreen() {
       if (getShowNumber() == TWO_NOTIFICATION_SHOWING && isObtainDistanceToTop) {
           obtainDistanceToTop();
        }
        LockNotificationManager.getInstance().notifyForUpdatingLockerTimeSize(getShowNumber(), yCoordinateOfAboveNotification);
        LockNotificationManager.getInstance().notifyForUpdatingChargingNumberSize(getShowNumber());
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
                        mAppNotificationInfo.packageName, receiveNumber, String.valueOf(getShowNumber()));
            }
        }
    }

    public interface NotificationClickCallback {
        void onNotificationClick();
    }
}
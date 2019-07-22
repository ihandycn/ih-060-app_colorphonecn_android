package com.colorphone.lock.lockscreen.locker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.R;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.AppNotificationInfo;
import com.colorphone.lock.lockscreen.DismissKeyguradActivity;
import com.colorphone.lock.lockscreen.LockNotificationManager;
import com.colorphone.lock.lockscreen.NotificationObserver;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.colorphone.lock.ScreenStatusReceiver.NOTIFICATION_SCREEN_ON;

public class NotificationWindowHolder implements NotificationObserver, INotificationObserver {

    public static final int SOURCE_LOCKER = 1;
    public static final int SOURCE_CHARGING = 2;

    private View mContainerRoot;
    private RelativeLayout mNotificationWindow;
    private SlidingNotificationLayout mSlidingWindow;

    private ImageView mSourceAppAvatar;
    private TextView mAppNameAndSendTime;
    private TextView mSenderName;
    private ImageView mSenderAvatar;
    private TextView mNotificationContent;

    private RelativeLayout mNotificationWindowAbove;
    private SlidingNotificationLayout mSlidingWindowAbove;

    private ImageView mSourceAppAvatarAbove;
    private TextView mAppNameAndSendTimeAbove;
    private TextView mSenderNameAbove;
    private ImageView mSenderAvatarAbove;
    private TextView mNotificationContentAbove;
    private int receiveNumberCount;
    private String receiveNumber;
    private int showNumber;
    private int yOfmSlidingWindowAbove;
    private List<SlidingNotificationLayout> list;

    public int displayPosition = 0;
    private AppNotificationInfo mAppNotificationInfo;
    private AppNotificationInfo mLastInfo;
    private final NotificationClickCallback mNotificationClickCallback;

    private final int mSource;

    public NotificationWindowHolder(ViewGroup rootView, int source, @Nullable NotificationClickCallback callback) {
        mSource = source;
        mContainerRoot = rootView;
        mNotificationClickCallback = callback;
        showNumber = 0;
        receiveNumberCount = 0;

        mSlidingWindow = findViewById(R.id.lock_sliding_window);
        mSlidingWindow.setClickable(true);
        mNotificationWindow = findViewById(R.id.lock_notification_window);
        mNotificationWindow.setAlpha(0.9f);

        mSlidingWindowAbove = findViewById(R.id.lock_sliding_window_above);
        mSlidingWindow.setClickable(true);
        mNotificationWindowAbove = findViewById(R.id.lock_notification_window_above);
        list = new ArrayList<>();
        list.add(mSlidingWindow);
        list.add(mSlidingWindowAbove);
        mNotificationWindowAbove.setAlpha(0.9f);

        mSourceAppAvatar = findViewById(R.id.source_app_avatar);
        mAppNameAndSendTime = findViewById(R.id.source_app_name);
        mSenderAvatar = findViewById(R.id.sender_avatar);
        mSenderName = findViewById(R.id.sender_name);
        mNotificationContent = findViewById(R.id.notification_content);
        mSlidingWindow.setVisibility(View.INVISIBLE);


        mSourceAppAvatarAbove = findViewById(R.id.source_app_avatar_above);
        mAppNameAndSendTimeAbove = findViewById(R.id.source_app_name_above);
        mSenderAvatarAbove = findViewById(R.id.sender_avatar_above);
        mSenderNameAbove = findViewById(R.id.sender_name_above);
        mNotificationContentAbove = findViewById(R.id.notification_content_above);
        mSlidingWindowAbove.setVisibility(View.INVISIBLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationWindow.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
            mSenderAvatar.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(3.3f), false));
            mNotificationWindowAbove.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
            mSenderAvatarAbove.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(3.3f), false));
        }

        mNotificationWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSLog.d("NotificationWindow clicked");
                onClickNotification();
            }
        });

        mSlidingWindow.setOnViewDismissCallback(new SlidingNotificationLayout.OnViewDismissCallback() {
            @Override
            public void onDismiss(View v) {
                mSlidingWindow.setVisibility(View.GONE);
                if (mSlidingWindowAbove.getVisibility() == View.VISIBLE) {
                    if (mSource == SOURCE_LOCKER) {
                        list.remove(mSlidingWindow);
                        list.add(mSlidingWindow);
                        setMarginForNotification();
                        displayPosition = 4;
                    } else {
                        displayPosition = 1;
                    }
                } else {
                    displayPosition = 0;
                }

                if (showNumber > 0) {
                    showNumber --;
                }
                notifyForTwoScreen();

            }
        });

        mNotificationWindowAbove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSLog.d("NotificationWindowAbove clicked");
                onClickNotification();
            }
        });

        mSlidingWindowAbove.setOnViewDismissCallback(new SlidingNotificationLayout.OnViewDismissCallback() {
            @Override
            public void onDismiss(View v) {
                mSlidingWindowAbove.setVisibility(View.GONE);
                if (showNumber > 0) {
                    showNumber --;
                }
                displayPosition = 3;
                notifyForTwoScreen();
            }
        });
    }

    private <T extends View> T findViewById(int id) {
        return mContainerRoot.findViewById(id);
    }

    private void setMarginForNotification() {
        if (mSource == SOURCE_LOCKER) {
            LockerMainFrame.LayoutParams layoutParams = (LockerMainFrame.LayoutParams)list.get(0).getLayoutParams();
            layoutParams.addRule(LockerMainFrame.ABOVE, R.id.rl_ad_container);
            layoutParams.bottomMargin = Dimensions.pxFromDp(6);
            list.get(0).setLayoutParams(layoutParams);

            LockerMainFrame.LayoutParams layoutParams1 = (LockerMainFrame.LayoutParams)list.get(1).getLayoutParams();
            layoutParams1.addRule(LockerMainFrame.ABOVE, list.get(0).getId());
            layoutParams1.bottomMargin = Dimensions.pxFromDp(6);
            list.get(1).setLayoutParams(layoutParams1);
        }
    }

    private Context getContext() {
        return HSApplication.getContext();
    }

    private void onClickNotification() {
        if (mAppNotificationInfo == null) {
            return;
        }
        LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Click",
                getInfo().packageName);

        DismissKeyguradActivity.startSelfIfKeyguardSecure(getContext());
        PendingIntent pendingIntent = getInfo().notification.contentIntent;
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
                NotificationManager noMan = (NotificationManager)
                        getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                noMan.cancel(getInfo().tag, getInfo().notificationId);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

        if (mNotificationClickCallback != null) {
            mNotificationClickCallback.onNotificationClick();
        }
    }

    private void changeNotificationWindowBelow(AppNotificationInfo info) {
        if (ScreenStatusReceiver.isScreenOn()) {
            if (receiveNumberCount <= 5) {
                receiveNumber = String.valueOf(receiveNumberCount);
            } else {
                receiveNumber = "above 5";
            }
            LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Show",
                    info.packageName, receiveNumber, String.valueOf(showNumber));
        }
        if (mAppNotificationInfo == info) {
            return;
        }
        mAppNotificationInfo = info;
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

    private void changeNotificationWindowAbove(AppNotificationInfo info) {
        if (ScreenStatusReceiver.isScreenOn()) {
            if (receiveNumberCount <= 5) {
                receiveNumber = String.valueOf(receiveNumberCount);
            } else {
                receiveNumber = "above 5";
            }
            LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Show",
                    info.packageName, receiveNumber, String.valueOf(showNumber));
        }
        if (mAppNotificationInfo == info) {
            return;
        }
        mAppNotificationInfo = info;
        mSenderNameAbove.setText(info.title);
        mNotificationContentAbove.setText(info.content);
        if (info.notification.largeIcon != null) {
            mSenderAvatarAbove.setVisibility(View.VISIBLE);
            mSenderAvatarAbove.setImageBitmap(info.notification.largeIcon);
        } else {
            mSenderAvatarAbove.setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mSourceAppAvatarAbove.setBackground(LockNotificationManager.getAppIcon(info.packageName));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("HH:mm");
        mAppNameAndSendTimeAbove.setText(LockNotificationManager.getAppName(info.packageName) + " " + "·" + " " + sdf.format(info.when));
    }

    private AppNotificationInfo getInfo() {
        return mAppNotificationInfo;
    }

    private boolean judgePackageNamePriority(AppNotificationInfo info) {
        return info.packageName.equalsIgnoreCase("com.tencent.mobileqq")
                || info.packageName.equalsIgnoreCase("com.tencent.mm")
                || info.packageName.equalsIgnoreCase("com.android.mms")
                || info.packageName.equalsIgnoreCase("com.eg.android.AlipayGphone");
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
            HSLog.e(displayPosition + " judgePackageNamePriority" );
            changeNotificationWindow(info);
        }
    }

    private void changeNotificationWindow(AppNotificationInfo info) {
        if (HSConfig.optBoolean(false,"Application", "Locker", "Notification", "ShowMultiple")) {
            if (list.size() == 2) {
                setMarginForNotification();
            }
            if (displayPosition == 1 && !judgePackageNamePriority(info) && judgePackageNamePriority(mLastInfo)) {
                displayPosition = 2;
            }
            if (displayPosition == 0) {
                mSlidingWindow.setVisibility(View.VISIBLE);
                changeNotificationWindowBelow(info);
                mLastInfo = info;
                showNumber = 1;
                displayPosition = 1;
            } else if (displayPosition == 1) {
                    if (mLastInfo.packageName.equalsIgnoreCase(info.packageName)) {
                        if (showNumber == 1) {
                            mSlidingWindow.setVisibility(View.VISIBLE);
                            changeNotificationWindowBelow(info);
                        } else if (showNumber == 2) {
                            mSlidingWindowAbove.setVisibility(View.VISIBLE);
                            changeNotificationWindowAbove(info);
                        }
                        mLastInfo = info;
                } else {
                    mSlidingWindowAbove.setVisibility(View.VISIBLE);
                    changeNotificationWindowAbove(info);
                    if (mLastInfo != null) {
                        mSlidingWindow.setVisibility(View.VISIBLE);
                        changeNotificationWindowBelow(mLastInfo);
                    }
                    mLastInfo = info;

                    showNumber = 2;
                }

            } else if (displayPosition == 2) {
                    mSlidingWindow.setVisibility(View.VISIBLE);
                    changeNotificationWindowBelow(info);
                    mLastInfo = info;
                    displayPosition = 1;
                    showNumber = 2;
            } else if (displayPosition == 3) {
                mSlidingWindowAbove.setVisibility(View.VISIBLE);
                changeNotificationWindowAbove(info);
                mLastInfo = info;
                displayPosition = 1;
                showNumber = 2;
            } else if (displayPosition == 4) {
                if (mLastInfo.packageName.equalsIgnoreCase(info.packageName)) {
                    mSlidingWindowAbove.setVisibility(View.VISIBLE);
                    changeNotificationWindowAbove(info);
                    displayPosition = 4;
                    showNumber = 1;
                } else {
                    mSlidingWindow.setVisibility(View.VISIBLE);
                    changeNotificationWindowBelow(info);
                    displayPosition = 1;
                    showNumber = 2;
                }
                mLastInfo = info;

                list.remove(mSlidingWindowAbove);
                list.add(mSlidingWindowAbove);
            }
        } else {
            mSlidingWindow.setVisibility(View.VISIBLE);
            changeNotificationWindowBelow(info);
            showNumber = 1;
        }

        notifyForTwoScreen();
    }

    private void notifyForTwoScreen() {
        LockNotificationManager.getInstance().notifyForUpdateTimeSize(showNumber);
        LockNotificationManager.getInstance().sendNotificationForChargingScreen(showNumber);
    }

    public SlidingNotificationLayout getmSlidingWindowAbove() {
        return mSlidingWindowAbove;
    }

    public SlidingNotificationLayout getmSlidingWindow() {
        return mSlidingWindow;
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
                        mAppNotificationInfo.packageName, receiveNumber, String.valueOf(showNumber));

            }
        }
    }

    public interface NotificationClickCallback {
        void onNotificationClick();
    }
}

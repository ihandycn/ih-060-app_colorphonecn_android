package com.colorphone.lock.lockscreen.locker;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.R;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.AppNotificationInfo;
import com.colorphone.lock.lockscreen.DismissKeyguradActivity;
import com.colorphone.lock.lockscreen.LockNotificationManager;
import com.colorphone.lock.lockscreen.NotificationObserver;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationWindowHolder implements NotificationObserver {

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
    private AppNotificationInfo mAppNotificationInfo;

    private final int mSource;

    public NotificationWindowHolder(ViewGroup rootView, int source) {
        mSource = source;
        mContainerRoot = rootView;
        mSlidingWindow = findViewById(R.id.lock_sliding_window);
        mSlidingWindow.setClickable(true);
        mNotificationWindow = findViewById(R.id.lock_notification_window);

        mSourceAppAvatar = findViewById(R.id.source_app_avatar);
        mAppNameAndSendTime = findViewById(R.id.source_app_name);
        mSenderAvatar = findViewById(R.id.sender_avatar);
        mSenderName = findViewById(R.id.sender_name);
        mNotificationContent = findViewById(R.id.notification_content);
        mSlidingWindow.setVisibility(View.INVISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationWindow.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
            mSenderAvatar.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(3.3f), false));
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
                mSlidingWindow.setVisibility(View.INVISIBLE);
            }
        });
    }

    private <T extends View> T findViewById(int id) {
        return mContainerRoot.findViewById(id);
    }

    private Context getContext() {
        return HSApplication.getContext();
    }

    private void onClickNotification() {
        if (mAppNotificationInfo == null) {
            return;
        }
        LockNotificationManager.getInstance().logEvent("ColorPhone_" + getSourceName(mSource) + "_Notification_Click",
                getInfo().packageName);

        DismissKeyguradActivity.startSelfIfKeyguardSecure(getContext());
        PendingIntent pendingIntent = getInfo().notification.contentIntent;
        if (pendingIntent != null) {
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeNotificationWindow(AppNotificationInfo info) {
        if (ScreenStatusReceiver.isScreenOn()) {
            LockNotificationManager.getInstance().logEvent("ColorPhone_" + getSourceName(mSource) + "_Notification_Show",
                    info.packageName);
        }
        mAppNotificationInfo = info;
        mSenderName.setText(info.title);
        mNotificationContent.setText(info.content);
        mSenderAvatar.setImageBitmap(info.notification.largeIcon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mSourceAppAvatar.setBackground(LockNotificationManager.getAppIcon(info.packageName));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("HH:mm");
        mAppNameAndSendTime.setText(LockNotificationManager.getAppName(info.packageName) + " " + "·" + " " + sdf.format(info.when));
    }

    private AppNotificationInfo getInfo() {
        return mAppNotificationInfo;
    }

    @Override
    public void onReceive(AppNotificationInfo info) {
        LockNotificationManager.getInstance().logEvent("ColorPhone_" + getSourceName(mSource) + "_Notification_Receive", info.packageName);
        boolean userEnabled = mSource == SOURCE_LOCKER ?
                LockerSettings.needShowNotificationLocker()
                : LockerSettings.needShowNotificationCharging();
        if (userEnabled) {
            mSlidingWindow.setVisibility(View.VISIBLE);
            changeNotificationWindow(info);
        }
    }

    private String getSourceName(int source) {
        return source == SOURCE_CHARGING ? " ChargingScreen" : "LockScreen";
    }
}

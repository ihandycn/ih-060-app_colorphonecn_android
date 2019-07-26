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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.colorphone.lock.ScreenStatusReceiver.NOTIFICATION_SCREEN_ON;

public class NotificationWindowHolder implements NotificationObserver, INotificationObserver {

    public static final String NOTIFY_KEY_REMOVE_MESSAGE = "notify_key_remove_message";
    public static final String BUNDLE_KEY_PACKAGE_NAME = "bundle_key_package_name";
    public static final int SOURCE_LOCKER = 1;
    public static final int SOURCE_CHARGING = 2;

    private View mContainerRoot;
    private LinearLayout mTestView;
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
    private AppNotificationInfo mAppNotificationInfo0;
    private AppNotificationInfo mAppNotificationInfo1;
    private AppNotificationInfo mLastInfo;
    private final NotificationClickCallback mNotificationClickCallback;

    private final int mSource;

    public NotificationWindowHolder(ViewGroup rootView, int source, @Nullable NotificationClickCallback callback) {
        mSource = source;
        mContainerRoot = rootView;
        mNotificationClickCallback = callback;
        showNumber = 0;
        receiveNumberCount = 0;

        mTestView = findViewById(R.id.test_view);
    }

    private void initNotificationWindow(final View view, AppNotificationInfo info) {

        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", "New notification " + "initNotificationWindow" );
        }
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
                if (BuildConfig.DEBUG) {
                    HSLog.e("LockNotification", " " + "click" );
                }
                onClickTest(view);
            }
        });

        mSlidingWindow.setOnViewDismissCallback(new SlidingNotificationLayout.OnViewDismissCallback() {
            @Override
            public void onDismiss(View v) {
                unbindNotificationWindow(v);
                notifyForTwoScreen();
            }
        });
        changeNotificaitonWindowTest(info, view);
    }

    private int getShowNumber() {
        showNumber = mTestView.getChildCount();
        return showNumber;
    }
    private void bindNotificationWindow(int index) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_layout, mTestView, false);
        view.setVisibility(View.GONE);
        mTestView.addView(view, index);
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", "New notification " + "bindNotificationWindow" );

        }
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", " " + "bindNotificationWindow " + mTestView.indexOfChild(view));
        }

    }

    private void unbindNotificationWindow(View view) {
        mTestView.removeView(view);
        view.setVisibility(View.GONE);
        if (mTestView.indexOfChild(view) == 0) {
            setInfo(mLastInfo);
        } else {
            setInfo(mAppNotificationInfo);
        }

    }


    private void initNotificationWindowBelow() {
        mSlidingWindow = findViewById(R.id.lock_sliding_window);
        mSlidingWindow.setClickable(true);
        mNotificationWindow = mSlidingWindow.findViewById(R.id.lock_notification_window);
        mNotificationWindow.setAlpha(0.9f);

        mSourceAppAvatar = mSlidingWindow.findViewById(R.id.source_app_avatar);
        mAppNameAndSendTime = mSlidingWindow.findViewById(R.id.source_app_name);
        mSenderAvatar = mSlidingWindow.findViewById(R.id.sender_avatar);
        mSenderName = mSlidingWindow.findViewById(R.id.sender_name);
        mNotificationContent = mSlidingWindow.findViewById(R.id.notification_content);
        mSlidingWindow.setVisibility(View.INVISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationWindow.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
            mSenderAvatar.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(3.3f), false));
        }

        mNotificationWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSLog.d("NotificationWindow clicked");
                //onClickNotificationBelow();
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
    }

    private void initNotificationWindowAbove() {
        //mSlidingWindowAbove = findViewById(R.id.lock_notification_layout_above);
        mSlidingWindowAbove.setClickable(true);
        mNotificationWindowAbove = mSlidingWindowAbove.findViewById(R.id.lock_notification_window);
        list = new ArrayList<>();
        list.add(mSlidingWindow);
        list.add(mSlidingWindowAbove);
        mNotificationWindowAbove.setAlpha(0.9f);

        mSourceAppAvatarAbove = mSlidingWindowAbove.findViewById(R.id.source_app_avatar);
        mAppNameAndSendTimeAbove = mSlidingWindowAbove.findViewById(R.id.source_app_name);
        mSenderAvatarAbove = mSlidingWindowAbove.findViewById(R.id.sender_avatar);
        mSenderNameAbove = mSlidingWindowAbove.findViewById(R.id.sender_name);
        mNotificationContentAbove = mSlidingWindowAbove.findViewById(R.id.notification_content);
        mSlidingWindowAbove.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mNotificationWindowAbove.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
            mSenderAvatarAbove.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(3.3f), false));
        }

        mNotificationWindowAbove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSLog.d("NotificationWindowAbove clicked");
                onClickNotificationAbove();
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
        return mContainerRoot.getContext();
    }

    /*private void onClickNotificationBelow() {
        if (mAppNotificationInfo0 == null) {
            return;
        }
        LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Click",
                getInfoBelow().packageName);

        LockNotificationManager.getInstance().setClickedNotification(getInfoBelow());

        if (getContext() instanceof BaseKeyguardActivity) {
            ((BaseKeyguardActivity) getContext()).tryDismissKeyguard(true);
        }

    }*/

    private void onClickTest(View v) {

        if (getInfo(v) == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", " " + "onClickTest" );
        }

        LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Click",
                getInfo(v).packageName);

        LockNotificationManager.getInstance().setClickedNotification(getInfo(v));

        if (getContext() instanceof BaseKeyguardActivity) {
            ((BaseKeyguardActivity) getContext()).tryDismissKeyguard(true);
        }
    }

    private void onClickNotificationAbove() {
        if (mAppNotificationInfo1 == null) {
            return;
        }
        LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Click",
                getInfoAbove().packageName);

        LockNotificationManager.getInstance().setClickedNotification(getInfoAbove());

        if (getContext() instanceof BaseKeyguardActivity) {
            ((BaseKeyguardActivity) getContext()).tryDismissKeyguard(true);
        }

    }

    private void changeNotificaitonWindowTest(AppNotificationInfo info, View view) {
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", "New notification " + "changeNotificaitonWindowTest" );
        }
        changeNotificationWindowForTest(info, view);
        mSlidingWindow.setVisibility(View.VISIBLE);
        view.setVisibility(View.VISIBLE);
    }

    private void changeNotificationWindowForTest(AppNotificationInfo info, View view) {
        if (ScreenStatusReceiver.isScreenOn()) {
            if (receiveNumberCount <= 5) {
                receiveNumber = String.valueOf(receiveNumberCount);
            } else {
                receiveNumber = "above 5";
            }
            LockNotificationManager.getInstance().logEvent(getSourceName(mSource) + "_Notification_Show",
                    info.packageName, receiveNumber, String.valueOf(getShowNumber()));
        }

        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", " " + info.title + " " + info.content);
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
        if (mAppNotificationInfo0 == info) {
            return;
        }

        mAppNotificationInfo0 = info;
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

        if (mAppNotificationInfo1 == info) {
            return;
        }

        mAppNotificationInfo1 = info;
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

    private AppNotificationInfo getInfo(View v) {
        AppNotificationInfo info = null;
        if (mTestView.indexOfChild(v) == 0) {
            info = mAppNotificationInfo0;
            HSLog.e("LockNotification", " " + "onClickTest0 " + (info == null) + mAppNotificationInfo0.content);
        } else if (mTestView.indexOfChild(v) == 1) {
            info = mAppNotificationInfo1;
            HSLog.e("LockNotification", " " + "onClickTest1 " + (info == null)  + mAppNotificationInfo1.content);
        }
        return info;
    }

    private void setInfo( AppNotificationInfo info) {
        mAppNotificationInfo0 = info;
        if (getShowNumber() == 2) {
            mAppNotificationInfo1 = mLastInfo;
        }

    }

    private AppNotificationInfo getInfoAbove() {
        return mAppNotificationInfo1;
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
            HSLog.e(displayPosition + " judgePackageNamePriority" );
            ShowNotificaitonMultiple(info);
        }
    }

    private void ShowNotificaiton(AppNotificationInfo info) {
        if (HSConfig.optBoolean(false,"Application", "Locker", "Notification", "ShowMultiple")) {
            ShowNotificaitonMultiple(info);
        } else {
            ShowNotificaitonSingle(info);
        }
    }


    private void ShowNotificaitonSingle(AppNotificationInfo info) {
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", "New notification " + "onBindNotificaiton" );
        }
        switch (getShowNumber()) {
            case 0:
                bindNotificationWindow(0);
                initNotificationWindow(mTestView.getChildAt(0), info);
                break;
            case 1:
                initNotificationWindow(mTestView.getChildAt(0), info);
                break;
            default:
                break;
        }
    }

    private void ShowNotificaitonMultiple(AppNotificationInfo info) {
        if (BuildConfig.DEBUG) {
            HSLog.e("LockNotification", "New notification " + "onBindNotificaiton" );
        }
        switch (getShowNumber()) {
            case 0:
                if (BuildConfig.DEBUG) {
                    HSLog.e("LockNotification", "New notification " + "case 0" );
                }
                bindNotificationWindow(0);
                initNotificationWindow(mTestView.getChildAt(0), info);

                break;
            case 1:
                if (BuildConfig.DEBUG) {
                    HSLog.e("LockNotification", "New notification " + "case 1" );
                }
                bindNotificationWindow(0);
                initNotificationWindow(mTestView.getChildAt(0), info);
                break;
            case 2:
                if (BuildConfig.DEBUG) {
                    HSLog.e("LockNotification", "New notification " + "case 2" );
                    HSLog.e("LockNotification", "New notification " + info.content + " " + mLastInfo.content );
                }

                initNotificationWindow(mTestView.getChildAt(0), info);
                initNotificationWindow(mTestView.getChildAt(1), mLastInfo);
                break;
            default:
                break;
        }
        setInfo(info);
        mLastInfo = info;
    }

    private void onBindNotification() {
        switch (getShowNumber()) {
            case 0:
                bindNotificationWindow(0);
                break;
            case 1:

                break;
            default:
                break;
        }
    }


    private boolean isSamePackageAsLastOne(AppNotificationInfo info) {
        return mLastInfo.packageName.equalsIgnoreCase(info.packageName);
    }

    private boolean isHigherPriorityPackage(AppNotificationInfo info) {
        boolean isHigherPriorityPackage = info.packageName.equalsIgnoreCase("com.tencent.mobileqq")
                || info.packageName.equalsIgnoreCase("com.tencent.mm")
                || info.packageName.equalsIgnoreCase("com.android.mms")
                || info.packageName.equalsIgnoreCase("com.eg.android.AlipayGphone");
        //1和0和2和3

        return true;
    }


    private void changeNotificationWindow(AppNotificationInfo info) {
        if (HSConfig.optBoolean(false,"Application", "Locker", "Notification", "ShowMultiple")) {
            if (list.size() == 2) {
                setMarginForNotification();
            }
            if (displayPosition == 1 && !isHigherPriorityPackage(info) && isHigherPriorityPackage(mLastInfo)) {
                displayPosition = 2;
            }
            if (displayPosition == 0) {
                mSlidingWindow.setVisibility(View.VISIBLE);
                changeNotificationWindowBelow(info);
                mLastInfo = info;
                showNumber = 1;
                displayPosition = 1;
            } else if (displayPosition == 1) {
                    /*if (mLastInfo.packageName.equalsIgnoreCase(info.packageName)) {
                        if (showNumber == 1) {
                            mSlidingWindow.setVisibility(View.VISIBLE);
                            changeNotificationWindowBelow(info);
                        } else if (showNumber == 2) {
                            mSlidingWindowAbove.setVisibility(View.VISIBLE);
                            changeNotificationWindowAbove(info);
                        }
                        mLastInfo = info;
                } else {*/
                    mSlidingWindowAbove.setVisibility(View.VISIBLE);
                    changeNotificationWindowAbove(info);
                    if (mLastInfo != null) {
                        mSlidingWindow.setVisibility(View.VISIBLE);
                        changeNotificationWindowBelow(mLastInfo);
                    }
                    mLastInfo = info;

                    showNumber = 2;
                //}

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
                /*if (mLastInfo.packageName.equalsIgnoreCase(info.packageName)) {
                    mSlidingWindowAbove.setVisibility(View.VISIBLE);
                    changeNotificationWindowAbove(info);
                    displayPosition = 4;
                    showNumber = 1;
                } else {*/
                    mSlidingWindow.setVisibility(View.VISIBLE);
                    changeNotificationWindowBelow(info);
                    displayPosition = 1;
                    showNumber = 2;
               // }
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
        //LockNotificationManager.getInstance().notifyForUpdateTimeSize(getShowNumber());
        //LockNotificationManager.getInstance().sendNotificationForChargingScreen(getShowNumber());
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
                        mAppNotificationInfo.packageName, receiveNumber, String.valueOf(getShowNumber()));
            }
        }
    }

    public interface NotificationClickCallback {
        void onNotificationClick();
    }
}

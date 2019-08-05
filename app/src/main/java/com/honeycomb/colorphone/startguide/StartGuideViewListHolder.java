package com.honeycomb.colorphone.startguide;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.HSPermissionRequestMgr;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

import static com.honeycomb.colorphone.startguide.StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_BG_POP;
import static com.honeycomb.colorphone.startguide.StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_PHONE;
import static com.honeycomb.colorphone.startguide.StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_NOTIFICATION;
import static com.honeycomb.colorphone.startguide.StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_ON_LOCK;
import static com.honeycomb.colorphone.startguide.StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_SCREEN_FLASH;
import static com.honeycomb.colorphone.startguide.StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_WRITE_SETTINGS;

public class StartGuideViewListHolder implements INotificationObserver {
    private static final String AUTO_PERMISSION_FAILED = "auto_permission_failed";

    private static final String TAG = "AutoPermission";
    private static final int CIRCLE_SPEED = 360;
    private static final int UPGRADE_MIN_INTERVAL = 25;
    private static final int UPGRADE_MAX_INTERVAL = UPGRADE_MIN_INTERVAL * 20;
    private static final int PROGRESS_MAX_VALUE = 100;
    private static final int EVENT_UPGRADE = 5000;

    private boolean isConfirmPage;
    private View container;
    private View circleAnimView;
    private ValueAnimator circleAnimator;

    private ViewGroup permissionLayout;

    private TextView progress;
    private int progressNum;
    private int goalNum = 0;
    private int progressInterval = UPGRADE_MIN_INTERVAL * 2;

    private SparseArray<StartGuideItemHolder> permissionList = new SparseArray<>();

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EVENT_UPGRADE:
                    if (!isConfirmPage) {
                        progressNum++;
                        long interval = getNextUpgradeDelay();

                        if (goalNum == PROGRESS_MAX_VALUE || progressNum >= PROGRESS_MAX_VALUE) {
                            interval = UPGRADE_MIN_INTERVAL;
                        }

                        if (progressNum >= PROGRESS_MAX_VALUE) {
                            if (goalNum == PROGRESS_MAX_VALUE) {
                                progressNum = PROGRESS_MAX_VALUE;
                                finish(1000);
                                interval = 0;
                            } else {
                                progressNum = PROGRESS_MAX_VALUE - 1;
                            }
                        }

                        if (interval != 0) {
                            handler.sendEmptyMessageDelayed(EVENT_UPGRADE, interval);
                        }
                        progress.setText(String.valueOf(progressNum + "%"));
                    }
                    break;
            }
        }
    };

    public StartGuideViewListHolder(View root, boolean isConfirmPage) {
        container = root;
        this.isConfirmPage = isConfirmPage;

        List<Integer> permissions = new ArrayList<>();

        permissions.add(TYPE_PERMISSION_TYPE_SCREEN_FLASH);
        permissions.add(TYPE_PERMISSION_TYPE_ON_LOCK);
        permissions.add(TYPE_PERMISSION_TYPE_NOTIFICATION);
        permissions.add(TYPE_PERMISSION_TYPE_BG_POP);
        permissions.add(TYPE_PERMISSION_TYPE_PHONE);
        permissions.add(TYPE_PERMISSION_TYPE_WRITE_SETTINGS);

        permissionLayout = container.findViewById(R.id.start_guide_permission_list);

        View item;
        StartGuideItemHolder itemHolder;
        for (int i = 0; i < permissions.size(); i++) {
            item = LayoutInflater.from(container.getContext()).inflate(R.layout.start_guide_premission_item, null, false);
            int pType = permissions.get(i);
            itemHolder = new StartGuideItemHolder(item, pType, isConfirmPage);
            if (!itemHolder.checkGrantStatus()) {
                permissionList.put(pType, itemHolder);
                permissionLayout.addView(item);
            }
        }

        if (isConfirmPage) {
            Preferences.getDefault().getBoolean(AUTO_PERMISSION_FAILED, false);

            refreshConfirmPage();
        } else {
            progress = container.findViewById(R.id.start_guide_request_progress);
        }

        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFICATION_PERMISSION_RESULT, this);
        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH, this);
    }

    public boolean refreshHolder(@StartGuidePermissionFactory.PERMISSION_TYPES int pType) {
        return permissionList.get(pType).checkGrantStatus();
    }

    public int refreshConfirmPage() {
        int confirmPermission = 0;
        if (!isConfirmPage) {
            return confirmPermission;
        }

        int notGrant = 0;
        StartGuideItemHolder holder;
        for (int i = 0; i < permissionList.size(); i++) {
            holder = permissionList.valueAt(i);
            HSLog.i("Permission", "refreshConfirmPage hP: " + holder.permissionType + "  grant: " + holder.checkGrantStatus() + "  fix: " + holder.clickToFix);
            if (!holder.checkGrantStatus()) {
                notGrant++;
            }
            if (holder.clickToFix) {
                holder.clickToFix = false;
                confirmPermission = holder.permissionType;
            }
        }

        if (notGrant == 0) {
            finish(1000);
        }

        return confirmPermission;
    }

    public boolean isManualFix() {
        StartGuideItemHolder holder;
        boolean isManualFix = false;
        for (int i = 0; i < permissionList.size(); i++) {
            holder = permissionList.valueAt(i);
            isManualFix |= holder.checkGrantStatus();
        }
        return isManualFix;
    }

    void setCircleAnimView(@IdRes int viewID) {
        circleAnimView = container.findViewById(viewID);
    }

    void startCircleAnimation() {
        if (circleAnimView != null) {
            ViewGroup.LayoutParams params = circleAnimView.getLayoutParams();
            if (params instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams clParams = ((ConstraintLayout.LayoutParams) params);
                final float oriAngle = clParams.circleAngle;
                cancelCircleAnimation();

                if (isConfirmPage) {
                    circleAnimator = ValueAnimator.ofFloat(-0.125f, 0f);
                } else {
                    circleAnimator = ValueAnimator.ofFloat(0, 1);
                    circleAnimator.setInterpolator(new LinearInterpolator());
                    circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    circleAnimator.setRepeatMode(ValueAnimator.RESTART);
                }

                circleAnimator.setDuration(360 * 1000 / CIRCLE_SPEED);
                circleAnimator.addUpdateListener(animation -> {
                    clParams.circleAngle = (oriAngle + animation.getAnimatedFraction() * 360) % 360;
                    circleAnimView.setLayoutParams(clParams);
                    circleAnimView.requestLayout();
                });
                circleAnimator.start();
                return;
            }
            HSLog.i(TAG, "not ConstraintLayout.LayoutParams ");
        }
        HSLog.i(TAG, "circleAnimView is null ");
    }

    private void cancelCircleAnimation() {
        if (circleAnimator != null) {
            circleAnimator.cancel();
            circleAnimator = null;
        }
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, AutoRequestManager.NOTIFICATION_PERMISSION_RESULT)) {
            String pType = hsBundle.getString(AutoRequestManager.BUNDLE_PERMISSION_TYPE);
            boolean result = hsBundle.getBoolean(AutoRequestManager.BUNDLE_PERMISSION_RESULT);
            int status = result ? StartGuideItemHolder.PERMISSION_STATUS_OK : isConfirmPage ? StartGuideItemHolder.PERMISSION_STATUS_FIX : StartGuideItemHolder.PERMISSION_STATUS_FAILED;
            if (!result) {
                Preferences.getDefault().putBoolean(AUTO_PERMISSION_FAILED, true);
            }

            switch (pType) {
                case HSPermissionRequestMgr.TYPE_AUTO_START:
                    updateProgress(TYPE_PERMISSION_TYPE_SCREEN_FLASH, status);
                    HSLog.w(TAG, "cast time 11 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                    updateProgress(TYPE_PERMISSION_TYPE_ON_LOCK, status);
                    HSLog.w(TAG, "cast time 22 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING:
                    updateProgress(TYPE_PERMISSION_TYPE_NOTIFICATION, status);
                    HSLog.w(TAG, "cast time 33 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case AutoRequestManager.TYPE_CUSTOM_BACKGROUND_POPUP:
                    updateProgress(TYPE_PERMISSION_TYPE_BG_POP, status);
                    HSLog.w(TAG, "cast time 44 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;

                default:
                    break;
            }

            refreshConfirmPage();
        } else if (TextUtils.equals(s, AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH)) {
            if (!isConfirmPage) {
                goalNum = PROGRESS_MAX_VALUE;
            }
        }
    }

    private void updateProgress(int pType, int status) {
        goalNum += 25;
        progressInterval = (1500 / (PROGRESS_MAX_VALUE - progressNum));
        setPermissionStatus(pType, status);
    }

    private void finish(long delay) {
        HSLog.w(TAG, "finish num == " + progressNum);

        if (handler != null) {
            handler.removeMessages(EVENT_UPGRADE);
            handler = null;
        }

        if (!isConfirmPage) {
            Threads.postOnMainThreadDelayed(() -> {
                HSLog.w(TAG, "dismiss float window num == " + progressNum);
                HSGlobalNotificationCenter.sendNotification(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW);
                AutoRequestManager.getInstance().dismissCoverWindow();
            }, delay);
        }
        HSGlobalNotificationCenter.removeObserver(this);
    }

    private long startAutoRequestAnimation;
    void startAutoRequestAnimation() {
        if (isConfirmPage) {
            return;
        }

        HSLog.i(TAG, "startAutoRequestAnimation ");
        startAutoRequestAnimation = System.currentTimeMillis();

        handler.sendEmptyMessage(EVENT_UPGRADE);
//        setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, StartGuideItemHolder.PERMISSION_STATUS_LOADING);
    }

    private long getNextUpgradeDelay() {
        if (goalNum == PROGRESS_MAX_VALUE) {
            progressInterval = UPGRADE_MIN_INTERVAL;
        } else if (goalNum - progressNum > 15) {
            progressInterval *= 0.8;
        } else if (progressNum - goalNum > 20) {
            progressInterval *= 1.3;
        }
        return Math.min(Math.max(progressInterval, UPGRADE_MIN_INTERVAL), UPGRADE_MAX_INTERVAL);
    }

    private void setPermissionStatus(@StartGuidePermissionFactory.PERMISSION_TYPES int pType, @StartGuideItemHolder.PERMISSION_STATUS int pStatus) {
        StartGuideItemHolder holder = permissionList.get(pType);
        if (holder != null) {
            holder.setStatus(pStatus);
        }
    }

    public void requestNextPermission() {
        StartGuideItemHolder holder;
        for (int i = 0; i < permissionList.size(); i++) {
            holder = permissionList.valueAt(i);
            if (!holder.checkGrantStatus()) {
                holder.fix.performClick();
                return;
            }
        }
    }
}

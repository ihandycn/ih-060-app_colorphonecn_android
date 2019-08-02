package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.RuntimePermissions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;

public class RuntimePermissionViewListHolder {
    private static final String AUTO_PERMISSION_FAILED = "auto_permission_failed";

    @StringDef({Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE })

    @Retention(RetentionPolicy.SOURCE)
    @interface PERMISSION_TYPES {}

    private static final String TAG = "AutoPermission";

    private View container;

    private ViewGroup permissionLayout;

    private HashMap<String, RuntimePermissionItemHolder> permissionList = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper());

    public RuntimePermissionViewListHolder(View root, List<String> permissions) {
        container = root;

        permissionLayout = container.findViewById(R.id.permission_list);

        View item;
        RuntimePermissionItemHolder itemHolder;
        for (int i = 0; i < permissions.size(); i++) {
            item = LayoutInflater.from(container.getContext()).inflate(R.layout.runtime_premission_item, null, false);
            String pType = permissions.get(i);
            itemHolder = new RuntimePermissionItemHolder(item, pType);
            permissionList.put(pType, itemHolder);
            permissionLayout.addView(item);
        }

//        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFICATION_PERMISSION_RESULT, this);
//        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH, this);
    }

    public boolean refreshHolder(@PERMISSION_TYPES String pType) {
        return permissionList.get(pType).checkGrantStatus();
    }

    public int refreshConfirmPage() {

        int notGrant = 0;
        for (RuntimePermissionItemHolder holder : permissionList.values()) {
            if (!holder.checkGrantStatus()) {
                notGrant++;
            }
        }

        if (notGrant == 0) {
            finish(1000);
        }

        return 0;
    }

    public boolean isAllGrant() {
        boolean isAllGrant = true;
        for (RuntimePermissionItemHolder holder : permissionList.values()) {
            isAllGrant &= holder.checkGrantStatus();
        }
        return isAllGrant;
    }

//    @Override public void onReceive(String s, HSBundle hsBundle) {
//        if (TextUtils.equals(s, AutoRequestManager.NOTIFICATION_PERMISSION_RESULT)) {
//            String pType = hsBundle.getString(AutoRequestManager.BUNDLE_PERMISSION_TYPE);
//            boolean result = hsBundle.getBoolean(AutoRequestManager.BUNDLE_PERMISSION_RESULT);
//            int status = result ? RuntimePermissionItemHolder.PERMISSION_STATUS_OK : RuntimePermissionItemHolder.PERMISSION_STATUS_FAILED;
//            if (!result) {
//                Preferences.getDefault().putBoolean(AUTO_PERMISSION_FAILED, true);
//            }
//
//            switch (pType) {
//                case HSPermissionRequestMgr.TYPE_AUTO_START:
//                    updateProgress(TYPE_PERMISSION_TYPE_SCREEN_FLASH, status);
//                    HSLog.w(TAG, "cast time 11 " + (System.currentTimeMillis() - startAutoRequestAnimation));
//                    break;
//                case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
//                    updateProgress(TYPE_PERMISSION_TYPE_ON_LOCK, status);
//                    HSLog.w(TAG, "cast time 22 " + (System.currentTimeMillis() - startAutoRequestAnimation));
//                    break;
//                case HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING:
//                    updateProgress(TYPE_PERMISSION_TYPE_NOTIFICATION, status);
//                    HSLog.w(TAG, "cast time 33 " + (System.currentTimeMillis() - startAutoRequestAnimation));
//                    break;
//                case AutoRequestManager.TYPE_CUSTOM_BACKGROUND_POPUP:
//                    updateProgress(TYPE_PERMISSION_TYPE_BG_POP, status);
//                    HSLog.w(TAG, "cast time 44 " + (System.currentTimeMillis() - startAutoRequestAnimation));
//                    break;
//
//                default:
//                    break;
//            }
//
//            refreshConfirmPage();
//        } else if (TextUtils.equals(s, AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH)) {
//        }
//    }

    private void updateProgress(String pType, int status) {
        setPermissionStatus(pType, status);
    }

    private void finish(long delay) {
        HSLog.w(TAG, "finish");

//        Threads.postOnMainThreadDelayed(() -> {
//            HSLog.w(TAG, "dismiss float window num == " + progressNum);
//            HSGlobalNotificationCenter.sendNotification(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW);
//            AutoRequestManager.getInstance().dismissCoverWindow();
//        }, delay);
//        HSGlobalNotificationCenter.removeObserver(this);
    }

    private void setPermissionStatus(@PERMISSION_TYPES String pType, @RuntimePermissionItemHolder.PERMISSION_STATUS int pStatus) {
        RuntimePermissionItemHolder holder = permissionList.get(pType);
        if (holder != null) {
            holder.setStatus(pStatus);
        }
    }

    public static int getItemTitle(@PERMISSION_TYPES String type) {
        switch (type) {
            case Manifest.permission.READ_CONTACTS:
                return R.string.acb_phone_permission_read_contact;
            case Manifest.permission.WRITE_CONTACTS:
                return R.string.acb_phone_permission_write_contact;
            case Manifest.permission.READ_CALL_LOG:
                return R.string.acb_phone_permission_read_call_log;
            default:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.string.acb_phone_permission_read_storage;
        }
    }

    public static int getItemDrawable(@PERMISSION_TYPES String type) {
        switch (type) {
            case Manifest.permission.READ_CONTACTS:
                return R.drawable.acb_phone_permission_read_contact;
            case Manifest.permission.WRITE_CONTACTS:
                return R.drawable.acb_phone_permission_write_contact;
            case Manifest.permission.READ_CALL_LOG:
                return R.drawable.acb_phone_permission_read_call_log;
            default:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.drawable.acb_phone_permission_read_storage;
        }
    }

    public static boolean getItemGrant(@PERMISSION_TYPES String type) {
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), type) == RuntimePermissions.PERMISSION_GRANTED;
    }
}

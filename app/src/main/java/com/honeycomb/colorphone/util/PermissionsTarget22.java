package com.honeycomb.colorphone.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.support.annotation.RequiresApi;

import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class PermissionsTarget22 {

    public static final String READ_CONTACT = "OP_READ_CONTACTS";
    public static final String WRITE_CONTACT = "OP_WRITE_CONTACTS";
    public static final String READ_PHONE_STATE = "OP_READ_PHONE_STATE";
    public static final String SYSTEM_ALERT_WINDOW = "OP_SYSTEM_ALERT_WINDOW";

    /**
     * For xiaomi
     */
    public static final String AUTO_START = "OP_AUTO_START";
    /**
     * For xiaomi
     */
    public static final String SHOW_WHEN_LOCKED = "OP_SHOW_WHEN_LOCKED";
    /**
     * For xiaomi
     */
    public static final String BACKGROUND_START_ACTIVITY = "OP_BACKGROUND_START_ACTIVITY";

    public static final int NOT_GRANTED = 1;
    public static final int GRANTED = 0;

    /**
     * If return ERROR, we should discard the result and try other methods.
     */
    public static final int ERROR = -1;

    private static PermissionsTarget22 INSTANCE = new PermissionsTarget22();

    private AppOpsManager appOpsManager;
    private Method opMthod;
    private int permAllow;
    private int permAsk;

    public static PermissionsTarget22 getInstance() {
        return INSTANCE;
    }

    private PermissionsTarget22() {
        try {
            if (appOpsManager == null) {
                appOpsManager = (AppOpsManager) HSApplication.getContext().getSystemService(Context.APP_OPS_SERVICE);
                opMthod = AppOpsManager.class.getMethod("checkOp", int.class, int.class, String.class);
                Field mode = AppOpsManager.class.getField("MODE_ALLOWED");
                mode.setAccessible(true);
                permAllow = mode.getInt(appOpsManager);
//                if (Compats.IS_XIAOMI_DEVICE) {
//                    Field modeAsk = AppOpsManager.class.getField("MODE_ASK");
//                    modeAsk.setAccessible(true);
//                    permAsk = modeAsk.getInt(appOpsManager);
//                }
                HSLog.d("PermissionCheck", "Check allow = " + permAllow + ", ask = " + permAsk);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Analytics.logEvent("Permission_OP_Init_Error", true,
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Msg", e.getMessage());
        }
    }

    public int checkPerm(String permName) {
        try {
            if (opMthod != null) {
                Field filedDefine = AppOpsManager.class.getField(permName);
                if (filedDefine != null) {
                    filedDefine.setAccessible(true);
                    int valueDefine = filedDefine.getInt(appOpsManager);
                    int checkResult = (int) opMthod.invoke(appOpsManager, valueDefine, Process.myUid(), HSApplication.getContext().getPackageName());
                    HSLog.d("PermissionCheck", permName
                            + ", Check = " + checkResult
                            + ", Given = " + (checkResult == permAllow));
                    return (checkResult == permAllow) ? GRANTED : NOT_GRANTED;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Analytics.logEvent("Permission_OP_Check_Error", true,
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Msg", e.getMessage());
        }
        return ERROR;
    }

}

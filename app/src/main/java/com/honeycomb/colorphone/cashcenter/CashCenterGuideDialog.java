package com.honeycomb.colorphone.cashcenter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.boost.FloatWindowDialog;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.messagecenter.notification.FloatWindow;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import java.util.Calendar;

public class CashCenterGuideDialog extends FloatWindowDialog {
    private final static String PREF_KEY_LAST_SHOW_CASH_GUIDE = "pref_key_last_show_cash_guide";
    private final int TOUCH_IGNORE = 10;
    protected ViewGroup mContentView;

    private static int statusBarHeight;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    private float yDownInScreen;
    private float xInView;
    private float yInView;
    public static int viewX;
    public static int viewY;
//    private final int viewOriginalX;
    public static int viewViewWidth;
    public static int viewViewHeight;
    private boolean isStop = true;

    public static void showCashCenterGuideDialog(Context context) {
        FloatWindowDialog dialog = new CashCenterGuideDialog(context);
        FloatWindowManager.getInstance().showDialog(dialog);

        Analytics.logEvent("CashCenter_FloatingGuide_Show");

        recordShow();
    }

    public CashCenterGuideDialog(Context context) {
        this(context, null);
    }

    public CashCenterGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CashCenterGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContentView = (ViewGroup) View.inflate(getContext(), R.layout.cash_center_guide, this);
    }

    @Override public void dismiss() {
        FloatWindowManager.getInstance().removeDialog(this);
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = getFloatWindowType();
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        lp.gravity = Gravity.END | Gravity.TOP;
        lp.y = Dimensions.pxFromDp(200);
        lp.width = Dimensions.pxFromDp(130);
        lp.height = Dimensions.pxFromDp(100);

        this.setLayoutParams(lp);
        return lp;
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public void onAddedToWindow(SafeWindowManager windowManager) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isStop) {
            HSLog.d(FloatWindow.TAG, "not onTouch");
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                isCancelAllAnimator = true;

                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - statusBarHeight;
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - statusBarHeight;
                HSLog.d(FloatWindow.TAG, "ACTION_DOWN x == " + xDownInScreen + "  y == " + yDownInScreen);

//                actionDownStatus();
                break;
            case MotionEvent.ACTION_MOVE:
//                checkActionDownFinalStatus();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - statusBarHeight;
//                updateViewPosition();

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                FloatWindow.getInstance().hideFloatingBackground();
//                if (isRemoveviewView) {
//                    isRemoveviewView = false;
//                    FloatWindow.getInstance().hideFloatingview();
//                    return false;
//                }

//                isStop = false;
                if (isMisOperation()) {
                    ColorPhoneActivity.startColorPhone(getContext(), ColorPhoneActivity.CASH_POSITION);
                    Analytics.logEvent("CashCenter_FloatingGuide_Click", "type", getPeriod());
                    dismiss();
                }
                HSLog.d(FloatWindow.TAG, "ACTION_UP " + "x == " + xInScreen + "  y == " + yInScreen);
                break;
            default:
                break;
        }
        return false;
    }

    private static void recordShow() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (hour >= 5 && hour < 8) {
            hour = 8;
        }

        if (hour >= 11 && hour < 14) {
            hour = 14;
        }

        if (hour >= 18 && hour < 21) {
            hour = 21;
        }

        Preferences.getDefault().putInt(PREF_KEY_LAST_SHOW_CASH_GUIDE, (day * 100 + hour));
    }

    public static boolean isPeriod() {
        if (!HSConfig.optBoolean(false, "Application", "CashCenter", "FloatingGuide")) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int lastShow = Preferences.getDefault().getInt(PREF_KEY_LAST_SHOW_CASH_GUIDE, 0);
        int lastDay = lastShow / 100;
        int lastHour = lastShow % 100;
        if (lastDay < day) {
            lastHour = 0;
        }

        if (hour >= 5 && hour < 8 && hour > lastHour) {
            return true;
        }

        if (hour >= 11 && hour < 14 && hour > lastHour) {
            return true;
        }

        if (hour >= 18 && hour < 21 && hour > lastHour) {
            return true;
        }
        return false;
    }

    private String getPeriod() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 8) {
            return "5:00-8:00";
        }

        if (hour >= 11 && hour < 14) {
            return "11:00-14:00";
        }

        if (hour >= 18 && hour < 21) {
            return "18:00-21:00";
        }
        return "";
    }

    private boolean isMisOperation(Point p1, Point p2) {
        return (Math.abs(p1.x - p2.x) < TOUCH_IGNORE && Math.abs(p1.y - p2.y) < TOUCH_IGNORE);
    }

    private boolean isMisOperation() {
        return (Math.abs(xInScreen - xDownInScreen) < TOUCH_IGNORE && Math.abs(yInScreen - yDownInScreen) < TOUCH_IGNORE);
    }
}

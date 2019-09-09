package com.acb.colorphone.permissions;

import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

/**
 * @author sundxing
 */
public class StableToast {

    private static Toast toast;
    private static Handler sHandler = new Handler();

    public static long timeMills;
    public static String logEvent;

    public static void showStableToast(@StringRes int stringId) {
        showStableToast(R.layout.toast_one_line_text, stringId, 0, null);
    }

    public static void showStableToast(@LayoutRes int layoutId, @StringRes int stringId, int yOffset, String eventId) {
        logEvent = eventId;
        timeMills = System.currentTimeMillis();
        showToastInner(layoutId, stringId, yOffset);

        long duration = HSConfig.optInteger(18, "Application", "AutoPermission", "ToastDurationSeconds")
                * DateUtils.SECOND_IN_MILLIS;
        sHandler.postDelayed(StableToast::cancelToastInner, duration);
    }

    private static void showToastInner(@LayoutRes int layoutId, @StringRes int stringId, int yOffset) {
        toast = new Toast(HSApplication.getContext().getApplicationContext());
        final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(layoutId, null);
        if (stringId != 0) {
            TextView tv = contentView.findViewById(R.id.toast_tv);
            tv.setText(stringId);
        }
        contentView.setAlpha(0.9f);
        contentView.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#000000"),
                Dimensions.pxFromDp(6), false));

        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yOffset);
        toast.setView(contentView);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
        sHandler.postDelayed(() -> showToastInner(layoutId, stringId, yOffset), 5000);

    }

    public static boolean cancelToast() {
        if (toast != null) {
            cancelToastInner();
            return true;
        }
        return false;
    }

    private static void cancelToastInner() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        sHandler.removeCallbacksAndMessages(null);
    }

}

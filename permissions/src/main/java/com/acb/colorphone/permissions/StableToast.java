package com.acb.colorphone.permissions;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ImageSpan;
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
            String string = HSApplication.getContext().getResources().getString(stringId);
            if (string.contains("icon")) {
                int identifier = HSApplication.getContext().getResources().getIdentifier("ic_launcher", "mipmap", HSApplication.getContext().getPackageName());
                Drawable appIcon = ContextCompat.getDrawable(HSApplication.getContext(), identifier);
                int appIconIndex = string.indexOf("icon");
                if (appIconIndex >= 0) {
                    SpannableString highlighted = new SpannableString(string);

                    int size = Dimensions.pxFromDp(40);
                    appIcon.setBounds(0, 0, size, size);
                    ImageSpan span = new ImageSpan(appIcon, ImageSpan.ALIGN_BOTTOM);
                    highlighted.setSpan(span, appIconIndex, appIconIndex + 4, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    tv.setText(highlighted);
                }
            } else {
                tv.setText(string);
            }
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

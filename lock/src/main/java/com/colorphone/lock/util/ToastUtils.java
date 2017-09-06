package com.colorphone.lock.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;

/**
 * This toast utility is better than directly calling {@link Toast#makeText(Context, CharSequence, int)} in two ways:
 *
 * (1) Consecutive calls would result in updated toast text (not new toasts).
 * (2) This utility forces usage of application context to avoid memory leak with activity context caused by an internal
 * bug of {@link Toast}.
 *
 * @see https://groups.google.com/forum/#!topic/android-developers/3i8M6-wAIwM
 */
public class ToastUtils {

    private static Toast sToast;

    public static void showToast(@StringRes int msgResId) {
        showToast(msgResId, Toast.LENGTH_SHORT);
    }

    public static void showToast(@StringRes int msgResId, int length) {
        showToast(HSApplication.getContext().getString(msgResId), length);
    }

    public static void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(String msg, int length) {
        if (sToast == null) {
            sToast = Toast.makeText(HSApplication.getContext(), msg, length);
            TextView v = (TextView) sToast.getView().findViewById(android.R.id.message);
            if( v != null) {
                v.setGravity(Gravity.CENTER);
            }
        }
        sToast.setText(msg);
        sToast.show();
    }
}

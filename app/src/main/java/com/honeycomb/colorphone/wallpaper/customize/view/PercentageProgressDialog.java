package com.honeycomb.colorphone.wallpaper.customize.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import com.honeycomb.colorphone.util.ActivityUtils;
 import com.honeycomb.colorphone.R;

public class PercentageProgressDialog extends Dialog {

    private PercentageProgressView mPercentageView;

    private PercentageProgressDialog(Context context) {
        super(context, R.style.WallpaperLoadingDialogTheme);
    }

    public static PercentageProgressDialog createDialog(Context context, String text) {
        PercentageProgressDialog dialog = new PercentageProgressDialog(context);

        dialog.setContentView(R.layout.percentage_progress_layout);

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView textView = dialog.findViewById(R.id.progress_dialog_message_text);
        textView.setText(text);

        Window window = dialog.getWindow();
        if (window != null) {
            window.getAttributes().gravity = Gravity.CENTER;
        }
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPercentageView = findViewById(R.id.progress_dialog_container);
        findViewById(R.id.progress_dialog_close_btn).setOnClickListener(v -> cancel());
    }

    public void show() {
        Activity activity = ActivityUtils.contextToActivitySafely(getContext());
        if (activity == null || activity.isFinishing() || ActivityUtils.isDestroyed(activity)) {
            return;
        }
        super.show();
    }

    public void setProgress(float progress) {
        if (mPercentageView != null) {
            mPercentageView.setProgress(progress);
        }
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
        } catch (IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: View=DecorView@780008[CustomizeActivity] not attached to window manager
            //   	at android.view.WindowManagerGlobal.findViewLocked(WindowManagerGlobal.java:503)
            e.printStackTrace();
        }
    }
}

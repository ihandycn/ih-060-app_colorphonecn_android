package com.honeycomb.colorphone.wallpaper.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.R;
import com.superapps.util.Toasts;

/**
 * Progress dialog with rotating progress bar and configurable text.
 */
public class ProgressDialog extends Dialog {

    private static ProgressDialog customProgressDialog = null;
    private static Animation rotatingAnimation = null;

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static ProgressDialog createDialog(Context context, String text) {
        customProgressDialog = new ProgressDialog(context, R.style.WallpaperLoadingDialogTheme);
        customProgressDialog.setContentView(R.layout.progress_dialog);
        customProgressDialog.setCancelable(true);
        customProgressDialog.setOnCancelListener(new OnDialogCancelListener(context));
        TextView textView = (TextView) customProgressDialog.findViewById(R.id.dialog_loading_text_view);
        textView.setText(text);
        customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        rotatingAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        return customProgressDialog;
    }

    public void show() {
        Context context = getContext();
        if(context instanceof Activity &&
                (((Activity) context).isFinishing() || ActivityUtils.isDestroyed((Activity) context))) {
            return;
        }
        ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.dialog_loading_image_view);
        try {
            // Temp to fix https://fabric.io/launcher5/android/apps/com.honeycomb.launcher/issues/5b7ff44b6007d59fcdb0dc4a?time=last-seven-days
            // But why?
            super.show();
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
        imageView.startAnimation(rotatingAnimation);
    }

    public void dismiss() {
        super.dismiss();
        customProgressDialog = null;
        rotatingAnimation = null;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        // No operation
    }

    public ProgressDialog setMessage(String strMessage) {
        TextView tvMsg = (TextView) customProgressDialog.findViewById(R.id.dialog_loading_text_view);
        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }
        return customProgressDialog;
    }

    private static class OnDialogCancelListener implements OnCancelListener {
        public OnDialogCancelListener(Context context) {
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            Toasts.showToast(R.string.progress_bar_toast_action_continuing);
        }
    }
}

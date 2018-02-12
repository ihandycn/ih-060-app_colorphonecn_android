package com.honeycomb.colorphone.boost;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.superapps.util.Dimensions;


public abstract class FloatWindowDialog extends FrameLayout implements FloatWindowListener {

    public FloatWindowDialog(Context context) {
        super(context);
    }

    public FloatWindowDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatWindowDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @return {@code true} to indicate that this dialog has no need to show and the control flow should continue as if
     * the dialog is never requested to show.
     */
    public boolean hasNoNeedToShow() {
        return false;
    }

    public boolean onBackPressed() {
        return false;
    }

    /**
     * Indicates if this dialog is modal.
     * Some dialog may requires show solidly (Like boost-plus-clean dialog), in that case, set return value be false.
     * @return
     */
    public boolean isModalTip() { return true; }

    public abstract void dismiss();

    //TODO Duplicated impl
    public abstract WindowManager.LayoutParams getLayoutParams();

    public abstract boolean shouldDismissOnLauncherStop();

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            return onBackPressed();
        }
        return super.dispatchKeyEvent(event);
    }

    private WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
    protected  WindowManager.LayoutParams getDefaultWindowLayoutParams() {
        if (mLayoutParams == null) {
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mLayoutParams.format = PixelFormat.TRANSLUCENT;
            mLayoutParams.gravity = Gravity.TOP;
            mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            mLayoutParams.height = Dimensions.getPhoneHeight(getContext());
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            }
        }
        return mLayoutParams;
    }
}

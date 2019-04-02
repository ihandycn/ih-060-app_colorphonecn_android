package com.honeycomb.colorphone.startguide;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Compats;

public class RequestPermissionDialog extends FullScreenDialog {
    private StartGuideViewHolder holder;
    public RequestPermissionDialog(Context context) {
        super(context);
    }

    public RequestPermissionDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RequestPermissionDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        HSLog.i("AutoPermission", "RequestPermissionDialog onFinishInflate");
    }

    @Override protected int getLayoutResId() {
        return R.layout.start_guide_request;
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // In HuaWei System Settings - Notification Center - Dropzones, Default block app float window but TYPE_TOAST
        // TYPE_TOAST float window will dismiss above api 25
        lp.flags |=
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (Compats.IS_HUAWEI_DEVICE) {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        return lp;
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public void onAddedToWindow(SafeWindowManager windowManager) {
        HSLog.i("AutoPermission", "RequestPermissionDialog onAddedToWindow" + holder);
        if (holder == null) {
            holder = new StartGuideViewHolder(this, false);
            holder.setCircleAnimView(R.id.start_guide_request_ball);
            holder.startCircleAnimation();
        }
    }
}

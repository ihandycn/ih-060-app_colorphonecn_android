package com.honeycomb.colorphone.startguide;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.ihs.commons.utils.HSLog;

public class RequestPermissionDialog extends FullScreenDialog {
    private StartGuideViewHolder holder;
    public RequestPermissionDialog(Context context) {
        this(context, null);
    }

    public RequestPermissionDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RequestPermissionDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPage();
    }

    private void initPage() {
        HSLog.i("AutoPermission", "RequestPermissionDialog init");
        holder = new StartGuideViewHolder(this, false);
        holder.setCircleAnimView(R.id.start_guide_request_ball);
    }

    @Override protected int getLayoutResId() {
        return R.layout.start_guide_request;
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.format = PixelFormat.TRANSLUCENT;

        lp.type = getFloatWindowType();

        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // In HuaWei System Settings - Notification Center - Dropzones, Default block app float window but TYPE_TOAST
        // TYPE_TOAST float window will dismiss above api 25
        lp.flags |=
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        return lp;
    }

    @Override public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override public void onAddedToWindow(SafeWindowManager windowManager) {
        HSLog.i("AutoPermission", "RequestPermissionDialog onAddedToWindow" + holder);
        if (holder != null) {
            holder.startCircleAnimation();
            holder.startAutoRequestAnimation();
        }
    }
}

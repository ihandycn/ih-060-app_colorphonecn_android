package com.honeycomb.colorphone.startguide;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.ihs.commons.utils.HSLog;

public class RequestPermissionDialog extends FullScreenDialog {
    private final WindowManager mWindowManager;
    private StartGuideViewListHolder holder;
    public RequestPermissionDialog(Context context) {
        this(context, null);
    }

    public RequestPermissionDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RequestPermissionDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initPage();
    }

    private void initPage() {
        HSLog.i("AutoPermission", "RequestPermissionDialog init");
        holder = new StartGuideViewListHolder(this, false);
        holder.setCircleAnimView(R.id.start_guide_request_ball);
    }

    @Override protected int getLayoutResId() {
        return R.layout.start_guide_request;
    }

    @Override public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = ActivityUtils.getFullScreenFloatWindowParams(mWindowManager);
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

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}

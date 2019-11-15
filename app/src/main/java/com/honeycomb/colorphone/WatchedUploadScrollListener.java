package com.honeycomb.colorphone;


import android.support.v7.widget.RecyclerView;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;

public class WatchedUploadScrollListener extends RecyclerView.OnScrollListener {
    private HSBundle scrollData = new HSBundle();

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        scrollData.putInt("state", newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        scrollData.putInt("dx", dx);
        scrollData.putInt("dy", dy);
        HSGlobalNotificationCenter.sendNotification(Constants.NOTIFY_KEY_LIST_UPLOAD_SCROLLED, scrollData);
        if (!recyclerView.canScrollVertically(-1)) {
            HSGlobalNotificationCenter.sendNotification(Constants.NOTIFY_KEY_LIST_UPLOAD_SCROLLED_TOP);
        }
    }
}

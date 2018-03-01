package com.honeycomb.colorphone.recentapp;

import android.os.Bundle;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;


public class SmartAssistantActivity extends HSActivity implements INotificationObserver {

    private SmartAssistantView mRecentAppGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AcbAdsManager.activePlacementInProcess(AdPlacements.SMART_ASSISTANT_PLACEMENT_NAME);
        HSGlobalNotificationCenter.addObserver(SmartAssistantView.NOTIFICATION_FINISH, this);

        mRecentAppGuide = new SmartAssistantView(this);
        setContentView(mRecentAppGuide);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case SmartAssistantView.NOTIFICATION_FINISH:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();

        if (mRecentAppGuide != null) {
            mRecentAppGuide.dismiss(true, true);
            mRecentAppGuide = null;
        }

        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRecentAppGuide != null) {
            mRecentAppGuide.dismiss(true, true);
            mRecentAppGuide = null;
        }
        try {
            HSGlobalNotificationCenter.removeObserver(this);
        } catch (Exception ignored) {
        }
    }
}

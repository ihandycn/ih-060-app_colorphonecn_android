package com.honeycomb.colorphone;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

public class ColorPhoneApplication extends HSApplication {
    private INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                HSLog.d("Session Start.");
            }

            if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("Session End.");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, sessionEventObserver);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
    }

    @Override
    protected String getConfigFileName() {
        if (HSLog.isDebugging()) {
            return "config-d.ya";
        } else {
            return "config-r.ya";
        }
    }

}

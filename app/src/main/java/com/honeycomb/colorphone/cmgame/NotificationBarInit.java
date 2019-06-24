package com.honeycomb.colorphone.cmgame;

import com.honeycomb.colorphone.AppMainInit;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.UserSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSVersionControlUtils;

public class NotificationBarInit extends AppMainInit {

    boolean isInit = false;
    @Override
    public void onInit(HSApplication application) {
        if (!isInit) {
            isInit = true;
            initNotificationToolbar();
        }
    }

    private void initNotificationToolbar() {
        if (HSVersionControlUtils.isFirstLaunchSinceInstallation() || HSVersionControlUtils.isFirstLaunchSinceUpgrade()) {
            UserSettings.checkNotificationToolbarToggleClicked();
        }

        if (!UserSettings.isNotificationToolbarToggleClicked()) {
            UserSettings.setNotificationToolbarEnabled(ModuleUtils.isNotificationToolBarEnabled());
        }

        NotificationManager.getInstance().showNotificationToolbarIfEnabled();
    }

    @Override
    public boolean afterAppFullyDisplay() {
        return true;
    }
}

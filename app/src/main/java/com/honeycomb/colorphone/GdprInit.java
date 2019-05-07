package com.honeycomb.colorphone;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSGdprConsent;

public class GdprInit extends AppMainInit {
    @Override
    public void onInit(HSApplication application) {
        if (GdprUtils.isNeedToAccessDataUsage()) {
            GdprUtils.setDataUsageUserEnabled(true);
            PushManager.getInstance().onGdprGranted();
            FirebaseAnalytics.getInstance(application).setAnalyticsCollectionEnabled(true);
        } else {
            FirebaseAnalytics.getInstance(application).setAnalyticsCollectionEnabled(false);
        }

        HSGdprConsent.addListener(new HSGdprConsent.GDPRConsentListener() {
            @Override
            public void onGDPRStateChanged(HSGdprConsent.ConsentState oldState, HSGdprConsent.ConsentState newState) {
                if (GdprUtils.isNeedToAccessDataUsage()) {
                    ((ColorPhoneApplication)application).initFabric();
                    PushManager.getInstance().onGdprGranted();
                }
                if (!HSApplication.isMainProcess()) {
                    if (oldState == HSGdprConsent.ConsentState.ACCEPTED && newState != oldState) {
                        System.exit(0);
                    }
                }
            }
        });
    }

}

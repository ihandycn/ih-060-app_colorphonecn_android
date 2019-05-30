package com.honeycomb.colorphone;

import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSGdprConsent;

public class GdprInit extends AppMainInit {
    @Override
    public void onInit(HSApplication application) {
        if (!GdprUtils.isGdprNewUser() && HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.TO_BE_CONFIRMED) {
            GdprUtils.setDataUsageUserEnabled(true);
        }

        HSGdprConsent.addListener(new HSGdprConsent.GDPRConsentListener() {
            @Override
            public void onGDPRStateChanged(HSGdprConsent.ConsentState oldState, HSGdprConsent.ConsentState newState) {
                if (GdprUtils.isNeedToAccessDataUsage()) {
                    ((ColorPhoneApplication)application).onGdprGranted();
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

package com.honeycomb.colorphone.dialer;

import android.content.Context;

import com.honeycomb.colorphone.dialer.contact.ContactInfoCache;

public class ReturnToCallController {
    public ReturnToCallController(InCallServiceImpl inCallService, ContactInfoCache instance) {

    }

    public static boolean isEnabled(Context context) {
        return false;
    }

    public void tearDown() {

    }
}

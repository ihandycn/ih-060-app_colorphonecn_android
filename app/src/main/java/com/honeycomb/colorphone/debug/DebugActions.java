package com.honeycomb.colorphone.debug;

import android.app.Activity;
import android.content.Intent;

public class DebugActions {

    private static final String TAG = "DebugActions";

    public static void onVolumeDown(Activity activity) {
        Intent intent = new Intent(activity, DebugActivity.class);
        activity.startActivity(intent);
    }

    public static void onVolumeUp(Activity activity) {
    }
}

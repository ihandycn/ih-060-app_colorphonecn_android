package com.honeycomb.colorphone.boost;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.Analytics;
import com.superapps.util.Preferences;

public class BoostStarterActivity extends Activity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BoostActivity.start(this, ResultConstants.RESULT_TYPE_BOOST_SHORTCUT);
        Analytics.logEvent("Shortcut_Boost_Clicked");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }


    public static void createShortCut(Context context) {
        if (!HSconfig.optBoolean(false, "Application", "Boost", "ShortcutEnable")) {
            return;
        }
        boolean hasCreated = Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean("shortcut_boost_exist", false);
        if (hasCreated) {
            return;
        }
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sIntent.setClass(context, BoostStarterActivity.class );
        String name = context.getString(R.string.boost_title);


        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, "shortcut_boost");

        ShortcutInfoCompat shortcutInfoCompat = builder
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_boost_plus))
                .setShortLabel(name)
                .setIntent(sIntent)
                .build();

        ShortcutManagerCompat.requestPinShortcut(context.getApplicationContext(),shortcutInfoCompat, null);

        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean("shortcut_boost_exist", true);
        Analytics.logEvent("Shortcut_Boost_Created");
    }

}

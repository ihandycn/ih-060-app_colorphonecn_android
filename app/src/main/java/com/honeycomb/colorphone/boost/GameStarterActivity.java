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
import com.honeycomb.colorphone.cmgame.CmGameUtil;
import com.honeycomb.colorphone.util.Analytics;
import com.superapps.util.Preferences;

/**
 * @author sundxing
 */
public class GameStarterActivity extends Activity {

    private static final String KEY_SHORTCUT_FLAG = "shortcut_cmgame_create";
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CmGameUtil.startCmGameActivity(this, "ShortCut");
        Analytics.logEvent("Shortcut_GameCenter_Clicked");
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


    public static boolean createShortCut(Context context) {

        boolean hasCreated = Preferences.get(Constants.PREF_FILE_DEFAULT).getBoolean(KEY_SHORTCUT_FLAG, false);
        if (hasCreated) {
            return false;
        }
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sIntent.setClass(context, GameStarterActivity.class );
        String name = context.getString(R.string.title_cmgame);


        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(context, "shortcut_game");

        ShortcutInfoCompat shortcutInfoCompat = builder
                .setIcon(IconCompat.createWithResource(context, R.drawable.game_icon_launch))
                .setShortLabel(name)
                .setIntent(sIntent)
                .build();

        ShortcutManagerCompat.requestPinShortcut(context.getApplicationContext(),shortcutInfoCompat, null);

        Preferences.get(Constants.PREF_FILE_DEFAULT).putBoolean(KEY_SHORTCUT_FLAG, true);
        Analytics.logEvent("Shortcut_GameCenter_Created");
        return true;
    }

}

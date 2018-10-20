package com.honeycomb.colorphone.cashcenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.HashMap;


public class ShortcutGuideActivity extends AppCompatActivity {

    public static final String EXTRA_GAME = "EXTRA_GAME";

    private TextView tvAction;
    private ImageView ivIcon;
    private Bitmap icon;
    private HashMap<String, String> params;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);

        String gameId = null;
        if (getIntent().getExtras() != null) {
            gameId = getIntent().getStringExtra(EXTRA_GAME);
        }
        if (gameId == null) {
            gameId = "1";
        }

        setContentView(R.layout.cashcenter_shortcut_guide);


        LauncherAnalytics.logEvent("Flashlight_Game_Shortcut_Alert_Show", params);

        ivIcon = findViewById(R.id.basketball_shortcut_guide_icon);


        tvAction = findViewById(R.id.basketball_shortcut_guide_btn);
        tvAction.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xff028dff, Dimensions.pxFromDp(21), true));
        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                createShortCut();
                LauncherAnalytics.logEvent("Flashlight_Game_Shortcut_Alert_Ok_Clicked", params);

                finish();
            }
        });

        findViewById(R.id.basketball_shortcut_guide_close).setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(14), true));
        findViewById(R.id.basketball_shortcut_guide_close).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                finish();
                LauncherAnalytics.logEvent("Flashlight_Game_Shortcut_Alert_Close_Clicked", params);
            }
        });
    }

    public void createShortCut() {
        Intent sIntent = new Intent(Intent.ACTION_MAIN);
        sIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sIntent.setClass(this, GameStarterActivity.class );

        String name = getString(R.string.cashcenter_title);
//        Intent installer = new Intent();
//        installer.putExtra("duplicate", false);
//        installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT, sIntent);
//        installer.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
//        if (isBasketball) {
//            installer.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon_basketball_small));
//        } else {
//            installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
//        }
//        installer.setAction( "com.android.launcher.action.INSTALL_SHORTCUT");
//        sendBroadcast(installer);

        ShortcutInfoCompat.Builder builder = new ShortcutInfoCompat.Builder(getApplicationContext(), "cash_center");

        ShortcutInfoCompat shortcutInfoCompat = builder
                .setIcon(IconCompat.createWithResource(this, R.drawable.cashcenter_icon))
                .setShortLabel(name)
                .setIntent(sIntent)
                .build();

        ShortcutManagerCompat.requestPinShortcut(this.getApplicationContext(),shortcutInfoCompat, null);
    }

    @Override public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}

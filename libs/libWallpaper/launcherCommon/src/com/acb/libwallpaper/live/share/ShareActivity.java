package com.acb.libwallpaper.live.share;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.ihs.app.analytics.HSAnalytics;
import com.superapps.util.Preferences;

import hugo.weaving.DebugLog;

public class ShareActivity extends Activity {

    public static final String INTENT_KEY_SHARE_PIC_URL = "intent_key_share_pic_url";
    public static final String PREF_KEY_SHARE_PIC_URL = "pref_key_share_pic_url";
    @Deprecated
    public static final String PREF_KEY_SHARE_WALLPAPER_TYPE = "pref_key_share_wallpaper_type";

    private static final int SLIDE_IN_ANIM_DURATION = 230;
    private static final int SLIDE_OUT_ANIM_DURATION = 170;

    private FrameLayout layout;
    private View mask;
    private ShareWaySelectView shareWaySelectView;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_share);

        layout = (FrameLayout) findViewById(android.R.id.content);
        mask = findViewById(R.id.share_background);
        shareWaySelectView = (ShareWaySelectView) findViewById(R.id.share_chooser_view);

        findViewById(R.id.close_share_way_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("Alert_ShareBy_Close_Clicked");
                slideOut();
            }
        });
        String picUrl = "";
        String source = "";
        Intent intent = getIntent();
        if (intent != null) {
            picUrl = intent.getStringExtra(INTENT_KEY_SHARE_PIC_URL);
            if (TextUtils.isEmpty(picUrl)) {
                Preferences preferences = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
                picUrl = preferences.getString(PREF_KEY_SHARE_PIC_URL, "");
            } else {
                source = "PicturePage";
            }
        }
        shareWaySelectView.bind(picUrl, source);

        layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                HSAnalytics.logEvent("Alert_ShareBy_Viewed");
                slideIn();
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void slideIn() {
        mask.setAlpha(0);
        mask.animate().alpha(1)
                .setDuration(SLIDE_IN_ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        shareWaySelectView.setTranslationY(layout.getHeight());
        shareWaySelectView.animate().translationY(0)
                .setDuration(SLIDE_IN_ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void slideOut() {
        mask.animate().alpha(0)
                .setDuration(SLIDE_OUT_ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        shareWaySelectView.animate().translationY(layout.getHeight())
                .setDuration(SLIDE_OUT_ANIM_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                    }
                })
                .start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            slideOut();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

package com.honeycomb.colorphone.activity;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.acb.call.constant.CPConst;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.ShareReceiver;
import com.honeycomb.colorphone.util.ShareAlertAutoPilotUtils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;
import java.io.FileOutputStream;

public class ShareAlertActivity extends Activity {
    public static final String PREFS_FILE = "share_alert_prefs_file_name";
    public static final String SHARE_ALERT_IN_APP_SHOW_COUNT = "share_alert_in_show_count";
    public static final String SHARE_ALERT_IN_APP_SHOW_TIME = "share_alert_in_show_time";
    public static final String SHARE_ALERT_OUT_APP_SHOW_COUNT = "share_alert_out_show_count";
    public static final String SHARE_ALERT_OUT_APP_SHOW_TIME = "share_alert_out_show_time";

    public static final String IS_INSIDE_APP = "is inside_app";
    private ThemePreviewWindow themePreviewWindow;
    private InCallActionView inCallActionView;

    private int themeID = HSPreferenceHelper.getDefault().getInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, Type.LED);
    private Type themeType = com.acb.utils.Utils.getTypeByThemeId(themeID);
    private boolean isInsideApp;
    private boolean v22 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static void starInsideApp(Activity activity) {
        Intent intent = new Intent(activity, ShareAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_INSIDE_APP, true);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        PreferenceHelper helper = PreferenceHelper.get(PREFS_FILE);
        helper.putLong(SHARE_ALERT_IN_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(SHARE_ALERT_IN_APP_SHOW_COUNT);
    }

    public static void startOutsideApp(Context context) {
        Intent intent = new Intent(context, ShareAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(IS_INSIDE_APP, false);
        context.startActivity(intent);
        PreferenceHelper helper = PreferenceHelper.get(PREFS_FILE);
        helper.putLong(SHARE_ALERT_OUT_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(SHARE_ALERT_OUT_APP_SHOW_COUNT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        isInsideApp =  getIntent().getBooleanExtra(IS_INSIDE_APP, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        initThemePreviewWindow();
        initShareAlertText();
        initShareButton();
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (isInsideApp) {
            HSAnalytics.logEvent("Colorphone_Inapp_ShareAlert_Show", "themeName", themeType.getName(), "v22", String.valueOf(v22));
        } else {
            HSAnalytics.logEvent("Colorphone_Outapp_ShareAlert_Show", "themeName", themeType.getName(), "v22", String.valueOf(v22));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        themePreviewWindow.playAnimation(themeType);
        inCallActionView.setTheme(themeType);
        if (themePreviewWindow.getImageCover() != null) {
            themePreviewWindow.getImageCover().setVisibility(View.VISIBLE);
            setImageCover();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        themePreviewWindow.stopAnimations();
        inCallActionView.stopAnimations();
    }

    private void initThemePreviewWindow() {
        themePreviewWindow = findViewById(R.id.card_flash_preview_window);
        inCallActionView = findViewById(R.id.card_in_call_action_view);
        inCallActionView.setEnabled(false);
    }

    private void initShareAlertText() {
        TextView title = findViewById(R.id.title);
        TextView content = findViewById(R.id.content);
        if (isInsideApp) {
            title.setText(ShareAlertAutoPilotUtils.getInsideAppShareAlertTitle());
            content.setText(ShareAlertAutoPilotUtils.getInsideAppShareDetail());
        } else {
            title.setText(ShareAlertAutoPilotUtils.getOutsideAppShareAlertTitle());
            content.setText(ShareAlertAutoPilotUtils.getOutsideAppShareDetail());
        }
    }

    private void initShareButton() {
        TextView shareButton = findViewById(R.id.share_button);
        shareButton.setText(isInsideApp ? ShareAlertAutoPilotUtils.getInsideAppShareBtnText() : ShareAlertAutoPilotUtils.getOutsideAppShareBtnText());
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
                if (isInsideApp) {
                    HSAnalytics.logEvent("Colorphone_Inapp_ShareAlert_Clicked", "themeName", themeType.getName(), "v22", String.valueOf(v22));
                } else {
                    HSAnalytics.logEvent("Colorphone_Outapp_ShareAlert_Clicked", "themeName", themeType.getName(), "v22", String.valueOf(v22));
                }
            }
        });
    }

    private void share() {
        ViewGroup cardView = findViewById(R.id.card_view);
        cardView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        cardView.setDrawingCacheEnabled(true);
        cardView.buildDrawingCache(false);
        Bitmap bitmap = Bitmap.createBitmap(cardView.getDrawingCache());
        cardView.setDrawingCacheEnabled(false);
        Canvas c = new Canvas(bitmap);
        File file, f;
        try {
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                file = new File(android.os.Environment.getExternalStorageDirectory(), "ColorPhone");
                if (!file.exists()) {
                    file.mkdirs();
                }
                f = new File(file.getAbsolutePath() + "/" + themeType.getName() + ".jpg");
                FileOutputStream ostream = new FileOutputStream(f);
                c.drawBitmap(bitmap, 0, 0, null);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.flush();
                ostream.close();

                File sharefile = new File(file.getAbsolutePath() + "/" + themeType.getName() + ".jpg");
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/*");
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharefile));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent receiver = new Intent(this, ShareReceiver.class);
                    receiver.putExtra(IS_INSIDE_APP, isInsideApp);
                    receiver.putExtra(ShareReceiver.THEME_NAME, themeType.getName());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                    Intent chooser = Intent.createChooser(share, "test", pendingIntent.getIntentSender());
                    startActivity(chooser);
                } else {
                    Intent chooser = Intent.createChooser(share, "test");
                    startActivity(chooser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImageCover() {
        GlideApp.with(this).asBitmap()
                .centerCrop()
                .load(themeType.getPreviewImage())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .dontAnimate()
                .into(themePreviewWindow.getImageCover());
    }


}

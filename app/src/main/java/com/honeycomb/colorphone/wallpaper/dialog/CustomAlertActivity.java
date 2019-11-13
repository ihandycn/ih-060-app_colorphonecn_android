package com.honeycomb.colorphone.wallpaper.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.wallpaper.LauncherAnalytics;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.base.BaseActivity;
import com.superapps.util.BackgroundDrawables;

import hugo.weaving.DebugLog;

import static com.honeycomb.colorphone.wallpaper.dialog.BaseLauncherDialog.adjustAlpha;


/**
 * Alert message with two buttons.
 */
public class CustomAlertActivity extends BaseActivity {

    private static final int FADE_IN_ANIM_DURATION = 200;

    public static final String RESULT_INTENT_KEY_USER_SELECTED_INDEX = "RESULT_INTENT_KEY_USER_SELECTED_INDEX";
    public static final String INTENT_KEY_TITLE = "com.huandong.wallpaper.live.title";
    public static final String INTENT_KEY_BODY = "com.huandong.wallpaper.live.body";
    public static final String INTENT_KEY_OK_BUTTON_TEXT = "com.huandong.wallpaper.live.ok_button_text";
    public static final String INTENT_KEY_CANCEL_BUTTON_TEXT = "com.huandong.wallpaper.live.cancel_button_text";
    public static final int RESULT_INTENT_VALUE_OK = 0;
    public static final int RESULT_INTENT_VALUE_CANCEL = 1;

    private RelativeLayout mLayout;

    private Intent mResultIntent = new Intent();

    public static Intent getLaunchIntent(Context context, String title, String body) {
        Intent launchIntent = new Intent(context, CustomAlertActivity.class);
        launchIntent.putExtra(INTENT_KEY_TITLE, title);
        launchIntent.putExtra(INTENT_KEY_BODY, body);
        return launchIntent;
    }

    public static Intent getLaunchIntent(Context context, String title, String body,
                                         String okButtonText, String cancelButtonText) {
        Intent launchIntent = getLaunchIntent(context, title, body);
        launchIntent.putExtra(INTENT_KEY_OK_BUTTON_TEXT, okButtonText);
        launchIntent.putExtra(INTENT_KEY_CANCEL_BUTTON_TEXT, cancelButtonText);
        return launchIntent;
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_two_btn);
        ViewGroup rootView = findViewById(R.id.dialog_root_view);
        rootView.setBackgroundColor(getResources().getColor(R.color.dialog_background_transparent));
        // Set default result (cancel) in case this activity is finished through unusual path
        mResultIntent.putExtra(RESULT_INTENT_KEY_USER_SELECTED_INDEX, RESULT_INTENT_VALUE_CANCEL);
        setResult(0, mResultIntent);

//        mLayout = (RelativeLayout) findViewById(R.id.custom_alert_layout);
        /*TextView titleTextView = (TextView) findViewById(R.id.custom_alert_title);
        TextView bodyTextView = (TextView) findViewById(R.id.custom_alert_body);*/

        String title = getIntent().getStringExtra(INTENT_KEY_TITLE);
        String content = getIntent().getStringExtra(INTENT_KEY_BODY);
        BaseLauncherDialogResIds resIds = new BaseLauncherDialogResIds(
                R.color.white,
                0,
                0,
                0,
                0,
                R.color.black_10_transparent
        );
        TypedValue out = new TypedValue();
        getResources().getValue(R.dimen.dialog_content_title, out, true);
        float alpha = out.getFloat();

        BaseLauncherDialogText t = new BaseLauncherDialogText(title,
                getResources().getColor(R.color.dialog_content_text),alpha);

        getResources().getValue(R.dimen.dialog_content_text, out, true);
        alpha = out.getFloat();
        BaseLauncherDialogText c = new BaseLauncherDialogText(content,
                getResources().getColor(R.color.dialog_content_text),
                alpha);

        BaseLauncherDialog.initBaseDialogStyle(rootView, this, resIds, t, c, null, BaseLauncherDialog.LIGHT_DIALOG_CONTENT_TEXT);

        TextView okBtn = findViewById(R.id.ok_btn);
        TextView cancelBtn = findViewById(R.id.negative_btn);
        okBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getResources().getColor(R.color.dialog_two_btn_negative),
                adjustAlpha(
                        getResources().getColor(R.color.dialog_two_btn_negative_text),
                        getResources().getColor(R.color.dialog_two_btn_negative)),
                (int) getResources().getDimension(R.dimen.dialog_btn_corner_radius),
                false, true));
        okBtn.setTextColor(Color.parseColor("#5B5B5B"));

        cancelBtn.setTextColor(getResources().getColor(R.color.white));

        cancelBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getResources().getColor(R.color.theme_main_color),
                adjustAlpha(getResources().getColor(R.color.white),
                        getResources().getColor(R.color.theme_main_color)),
                (int) getResources().getDimension(R.dimen.dialog_btn_corner_radius),
                false, true));

        final String bodyText = getIntent().getStringExtra(INTENT_KEY_BODY);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getString(R.string.online_wallpaper_cancel_edit_dialog_content).equals(bodyText)) {
                    LauncherAnalytics.logEvent("Wallpaper_Edit_Dialogue_Clicked", "type", "Cancel");
                }
                mResultIntent.putExtra(RESULT_INTENT_KEY_USER_SELECTED_INDEX, RESULT_INTENT_VALUE_CANCEL);
                setResult(0, mResultIntent);
//                fadeOut();
                finish();
            }
        });
        String cancelBtnText = getIntent().getStringExtra(INTENT_KEY_CANCEL_BUTTON_TEXT);
        if (cancelBtnText != null) {
            cancelBtn.setTextColor(Color.WHITE);
            cancelBtn.setText(cancelBtnText);
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getString(R.string.online_wallpaper_cancel_edit_dialog_content).equals(bodyText)) {
                    LauncherAnalytics.logEvent("Wallpaper_Edit_Dialogue_Clicked", "type", "OK");
                }
                mResultIntent.putExtra(RESULT_INTENT_KEY_USER_SELECTED_INDEX, RESULT_INTENT_VALUE_OK);
                setResult(0, mResultIntent);
//                fadeOut();
                finish();
            }
        });
        String okBtnText = getIntent().getStringExtra(INTENT_KEY_OK_BUTTON_TEXT);
        if (okBtnText != null) {
            okBtn.setText(okBtnText);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mResultIntent.putExtra(RESULT_INTENT_KEY_USER_SELECTED_INDEX, RESULT_INTENT_VALUE_CANCEL);
            setResult(0, mResultIntent);
//            fadeOut();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

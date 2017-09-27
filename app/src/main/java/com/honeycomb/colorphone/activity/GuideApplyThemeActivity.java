package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;

/**
 * Created by sundxing on 17/9/13.
 */

public class GuideApplyThemeActivity extends HSAppCompatActivity {

    public static String KEY_SHOW_TIME = "apply_guide_show_times";
    public static String KEY_DISPLAY_COUNT = "apply_guide_show_count";

    public static boolean FULL_SCREEN = true;

    public static void start(Context context, boolean fullScreen) {
        Intent starter = new Intent(context, GuideApplyThemeActivity.class);
        starter.putExtra("fullscreen", fullScreen);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFullScreen = getIntent().getBooleanExtra("fullscreen", true);
        setContentView(R.layout.guide_apply_success);
        if (isFullScreen) {
            StatusBarUtils.hideStatusBar(this);
        }
        View cbContainer = findViewById(R.id.welcome_guide_enable_checkbox_container);
        final CheckBox cb = (CheckBox) findViewById(R.id.welcome_guide_enable_checkbox);
        setUpPrivacyTextView();
        HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_Show");
        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_Cancel_Clicked");
                finish();
            }
        });
        TextView enableBtn = (TextView) findViewById(R.id.welcome_guide_function_enable_btn);
        enableBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb.isChecked()) {
                    HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_OK_Clicked");
                    CPSettings.setSMSAssistantModuleEnabled(true);
                    CPSettings.setCallAssistantModuleEnabled(true);
                    LockerSettings.setLockerEnabled(true);
                    ChargingScreenSettings.setChargingScreenEnabled(true);
                } else {
                    HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_OK_Clicked_WithUnselectFeature");
                }
                finish();
            }
        });


        cbContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.performClick();
            }
        });


    }

    @Override
    public void onBackPressed() {
        //Ignore back press.
    }

    private void setUpPrivacyTextView() {
        TextView privacyPolicy = (TextView) findViewById(R.id.welcome_guide_privacy_policy);
        privacyPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideApplyThemeActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_PRIVACY)));
            }
        });

        TextView termsOfService = (TextView) findViewById(R.id.welcome_guide_terms_of_service);
        termsOfService.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideApplyThemeActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERM_SERVICES)));
            }
        });
    }
}

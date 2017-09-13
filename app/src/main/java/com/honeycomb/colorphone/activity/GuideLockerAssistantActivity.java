package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;

/**
 * Created by sundxing on 17/9/13.
 */

public class GuideLockerAssistantActivity extends HSAppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, GuideLockerAssistantActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.guide_locker_assitant);
        StatusBarUtils.hideStatusBar(this);

        setUpPrivacyTextView();
        HSAnalytics.logEvent("ColorPhone_StartGuide_Show");
        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("ColorPhone_StartGuide_Cancel_Clicked");

                finish();
            }
        });
        findViewById(R.id.welcome_guide_function_enable_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("ColorPhone_StartGuide_OK_Clicked");
                CPSettings.setCallAssistantModuleEnabled(true);
                LockerSettings.setLockerEnabled(true);
                finish();
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
                Utils.startActivitySafely(GuideLockerAssistantActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_PRIVACY)));
            }
        });

        TextView termsOfService = (TextView) findViewById(R.id.welcome_guide_terms_of_service);
        termsOfService.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideLockerAssistantActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERM_SERVICES)));
            }
        });
    }
}

package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.acb.utils.PermissionHelper;
import com.acb.utils.PermissionUtils;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.recentapp.SmartAssistantUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.monitor.topapp.HSUsageAccessMgr;

/**
 * Created by sundxing on 17/9/13.
 */

public class GuideAllFeaturesActivity extends HSAppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, GuideAllFeaturesActivity.class);
        context.startActivity(starter);
    }

    public static boolean isStarted() {
       return HSPreferenceHelper.getDefault().getBoolean("guide_locker_stated", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSPreferenceHelper.getDefault().putBoolean("guide_locker_stated", true);

        setContentView(R.layout.guide_all_features);
        StatusBarUtils.hideStatusBar(this);

        setUpPrivacyTextView();
        LauncherAnalytics.logEvent("ColorPhone_StartGuide_Show");
        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAnalytics.logEvent("ColorPhone_StartGuide_Cancel_Clicked");

                finish();
            }
        });
        TextView enableBtn = (TextView) findViewById(R.id.welcome_guide_function_enable_btn);
        enableBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAnalytics.logEvent("ColorPhone_StartGuide_OK_Clicked");
                ModuleUtils.setAllModuleUserEnable();
                boolean needGuideNotificationPermisson = HSConfig.optBoolean(false,
                        "Application", "NotificationAccess", "GoToAccessPageFromFirstScreen");
                if (needGuideNotificationPermisson && !PermissionUtils.isNotificationAccessGranted(GuideAllFeaturesActivity.this)) {
                    PermissionHelper.requestNotificationPermission(ColorPhoneActivity.class, GuideAllFeaturesActivity.this, true, new Handler(), "FirstScreen");
                    LauncherAnalytics.logEvent("Colorphone_SystemNotificationAccessView_Show", "from", "FirstScreen");
                }
                finish();

                boolean needUsageAccess =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && SmartAssistantUtils.isConfigEnabled()
                        && SmartAssistantUtils.gainUsageAccessOnFirstLaunch();

                if (needUsageAccess) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);

                    // TODO

                    HSUsageAccessMgr.getInstance().checkPermission(new HSUsageAccessMgr.PermissionListener() {
                        @Override
                        public void onPermissionChanged(boolean b) {

                        }
                    });
                }


            }
        });

        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(titleNew() ? R.string.guide_first_page_title : R.string.guide_first_page_title_old);
    }

    private boolean titleNew() {
        String titleType = HSConfig.optString("new", "Application", "NotificationAccess", "FirstScreenTitle");
        if ("new".equalsIgnoreCase(titleType)) {
            return true;
        }
        return false;
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
                Utils.startActivitySafely(GuideAllFeaturesActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_PRIVACY)));
            }
        });

        TextView termsOfService = (TextView) findViewById(R.id.welcome_guide_terms_of_service);
        termsOfService.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideAllFeaturesActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERM_SERVICES)));
            }
        });
    }
}

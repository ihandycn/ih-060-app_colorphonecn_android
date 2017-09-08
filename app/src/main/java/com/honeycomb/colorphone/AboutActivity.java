package com.honeycomb.colorphone;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;


public class AboutActivity extends HSAppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, AboutActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.about);

        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);


        findViewById(R.id.privacy_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivitySafely(AboutActivity.this, getPrivacyViewIntent(HSConfig.optString("", "Application", "PrivacyPolicyURL")));
            }
        });

        findViewById(R.id.term_service_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivitySafely(AboutActivity.this, getPrivacyViewIntent(HSConfig.optString("", "Application", "TermsOfServiceURL")));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Intent getPrivacyViewIntent(String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }


    public static void startActivitySafely(Context context, Intent intent) {
        try {
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException | NullPointerException e) {
            HSLog.e("StartActivity", "Cannot start activity: " + intent);
        }
    }
}

package com.honeycomb.colorphone;

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
                startActivity(getPrivacyViewIntent(HSConfig.optString("", "Application", "PrivacyPolicyURL")));
            }
        });

        findViewById(R.id.term_service_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(getPrivacyViewIntent(HSConfig.optString("", "Application", "TermsOfServiceURL")));
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

    @Override
    protected void onStop() {

        super.onStop();
    }


    public static Intent getPrivacyViewIntent(String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
}

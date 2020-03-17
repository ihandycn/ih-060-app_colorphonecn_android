package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.gdpr.DataUsageSettingsActivity;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Navigations;


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

        Utils.configActivityStatusBar(this, toolbar);


        findViewById(R.id.privacy_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebLoadActivity.start(AboutActivity.this, Constants.getUrlPrivacy());
            }
        });

        findViewById(R.id.term_service_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebLoadActivity.start(AboutActivity.this, Constants.getUrlTermServices());
            }
        });

        View dataUsageCell = findViewById(R.id.data_usage_cell);
        dataUsageCell.setVisibility(View.GONE);
        findViewById(R.id.declare_rights).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigations.startActivitySafely(AboutActivity.this, getPrivacyViewIntent(Constants.getUrlDeclareRights()));
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


}

package com.acb.libwallpaper.live.customize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

public class Cc0ProtocolActivity extends BaseCustomizeActivity {
    public static final String PREFS_KEY_CC0_USER_AGREED = "PREFS_KEY_CC0_USER_AGREED";

    private TextView protocolContent;
    private Button nextButton;
    private AppCompatImageView checkBox;
    private boolean checked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_protocol);

        initActionBar();

        checkBox = findViewById(R.id.checkbox);
        nextButton = findViewById(R.id.next_btn);

        checkBox.setOnClickListener((View v) -> {
            checked = ! checked;
            checkBox.setImageResource(checked ? R.drawable.cusomize_checkbox_checked
            : R.drawable.customize_checkbox_unchecked);
            nextButton.setEnabled(checked);
        });

        checkBox.setBackground(BackgroundDrawables.createBackgroundDrawable(
                0xFFFFFFFF, Dimensions.pxFromDp(30f), true));

        nextButton.setOnClickListener((View v) -> {
            Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).putBoolean(PREFS_KEY_CC0_USER_AGREED, true);
            startActivity(new Intent(this, UploadWallpaperActivity.class));
            overridePendingTransition(R.anim.app_lock_slide_in_from_right, R.anim.app_lock_slide_out_from_left);
            finish();
        });

        protocolContent = findViewById(R.id.content);
        protocolContent.setMovementMethod(new ScrollingMovementMethod());
        String launcherName = getString(R.string.app_name);
        String text = String.format(getString(R.string.cc0_protocol_content), launcherName, launcherName, launcherName, launcherName);
        protocolContent.setText(text);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        toolbar.setTitle("");
        toolbar.setTitleTextColor(0xff4d4d4d);
        toolbar.setBackgroundColor(0xffffffff);
        setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}

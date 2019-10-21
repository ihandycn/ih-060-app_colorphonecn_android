package com.acb.libwallpaper.live.customize.activity.report;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.acb.libwallpaper.live.util.CommonUtils;
 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.customize.activity.BaseCustomizeActivity;
import com.superapps.util.Fonts;
import com.superapps.util.Navigations;

public class SelectReportReasonActivity extends BaseCustomizeActivity {

    public static final String INTENT_KEY_WALLPAPER_URL = "INTENT_KEY_WALLPAPER_URL";
    public static final String INTENT_KEY_REPORT_REASON = "INTENT_KEY_REPORT_REASON";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_report_wallpaper_reason);

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        Button nextButton = findViewById(R.id.next_btn);

        radioGroup.setOnCheckedChangeListener((RadioGroup group, int i) -> {
            nextButton.setEnabled(true);
        });

        nextButton.setOnClickListener((View v) -> {
            String reportReason = getReportReason(radioGroup.getCheckedRadioButtonId());
            Intent intent = new Intent(SelectReportReasonActivity.this, InputEmailActivity.class);
            intent.putExtra(INTENT_KEY_REPORT_REASON, reportReason);
            intent.putExtra(INTENT_KEY_WALLPAPER_URL, getIntent().getStringExtra(INTENT_KEY_WALLPAPER_URL));
            Navigations.startActivitySafely(SelectReportReasonActivity.this, intent);
            overridePendingTransition(R.anim.app_lock_slide_in_from_right, R.anim.app_lock_slide_out_from_left);
        });

        initActionBar();

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{
                        0xff7f7f7f
                        , getResources().getColor(R.color.blue),
                }
        );

        Typeface font = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD);

        for (int i = 0; i < 5; i++) {
            AppCompatRadioButton rb = ((AppCompatRadioButton) radioGroup.getChildAt(i));
            rb.setTypeface(font);
            rb.setSupportButtonTintList(colorStateList);
        }
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

    private String getReportReason(@IdRes int id) {
        if (id == R.id.reason_infringement) {
            return "infringement";
        } else if (id == R.id.reason_political) {
            return "political";
        } else if (id == R.id.reason_gambling) {
            return "gambling";
        } else if (id == R.id.reason_porn) {
            return "porn";
        } else if (id == R.id.reason_violence) {
            return "violence";
        }
        return "";
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

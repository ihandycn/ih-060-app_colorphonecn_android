package com.honeycomb.colorphone.lifeassistant;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class LifeAssistantSettingActivity extends HSAppCompatActivity {
    private AlertDialog dialog;
    private SwitchCompat switchView;
    private boolean confirmClose = true;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.life_assistant_setting_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        View rootView = findViewById(R.id.life_assistant_setting_layout);
        switchView = findViewById(R.id.life_assistant_setting_switch);
        boolean state = LifeAssistantConfig.isLifeAssistantSettingEnable();

        switchView.setChecked(state);

        rootView.setOnClickListener(v -> switchView.toggle());

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && confirmClose) {
                switchView.setChecked(true);
                showConfirmDialog();
            } else {
                confirmClose = true;
                LifeAssistantConfig.setLifeAssistantSettingEnable(isChecked);
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

    private void showConfirmDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }

        View view = getLayoutInflater().inflate(R.layout.layout_life_assistant_close_confirm_dialog, null);

        View bgView = view.findViewById(R.id.content_layout);
        bgView.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        View btn = view.findViewById(R.id.tv_first);
        btn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(26), true));
        btn.setOnClickListener(v -> {
            dismissDialog();
        });

        btn = view.findViewById(R.id.tv_second);
        btn.setOnClickListener(v -> {
            dismissDialog();
            confirmClose = false;
            switchView.setChecked(false);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
        builder.setCancelable(false);
        builder.setView(view);
        dialog = builder.create();

        showDialog(dialog);

    }

    public static void configActivityStatusBar(AppCompatActivity activity, Toolbar toolbar, int upDrawable) {
        toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        toolbar.setTitleTextColor(ContextCompat.getColor(activity, R.color.black_100_transparent));

        activity.setSupportActionBar(toolbar);
        final Drawable upArrow = ContextCompat.getDrawable(activity, upDrawable);
        upArrow.setColorFilter(ContextCompat.getColor(activity, R.color.black_100_transparent), PorterDuff.Mode.SRC_ATOP);
        activity.getSupportActionBar().setHomeAsUpIndicator(upArrow);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        Utils.applyFontForToolbarTitle(activity, toolbar);
    }
}

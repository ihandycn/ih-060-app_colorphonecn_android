package com.honeycomb.colorphone.dialer.guide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

public class GuideSetDefaultActivity extends AppCompatActivity {

    public static boolean start(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Not support default phone.
            return false;
        }

        if (ConfigEvent.dialerEnable()) {
            if (!DefaultPhoneUtils.isDefaultPhone()) {
                return Preferences.get("phone_guide").doOnce(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        DefaultPhoneUtils.saveSystemDefaultPhone();

                        if (ConfigEvent.setDefaultGuideShow()) {
                            Intent starter = new Intent(context, GuideSetDefaultActivity.class);
                            context.startActivity(starter);
                        } else {
                            DefaultPhoneUtils.checkDefaultPhoneSettings();
                        }
                    }
                }, "prefs_guide_show");
            }
        }
        return false;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_set_default_phone);
        ConfigEvent.guideShow();

        findViewById(R.id.dialog_content_container)
                .setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#ffffff"),
                        Dimensions.pxFromDp(24),
                        false));
        Button actionBtn = findViewById(R.id.guide_action);
        actionBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_BOLD));
        actionBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#448AFF"),
                Dimensions.pxFromDp(6),
                true));
        actionBtn.setOnClickListener(v ->
        {
            ConfigEvent.guideConfirmed();
            DefaultPhoneUtils.checkDefaultPhoneSettings();
            finish();
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

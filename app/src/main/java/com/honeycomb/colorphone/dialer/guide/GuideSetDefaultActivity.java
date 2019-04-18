package com.honeycomb.colorphone.dialer.guide;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class GuideSetDefaultActivity extends AppCompatActivity {

    private boolean mOkClicked;
    private static boolean sFlagOnce = true;

    public static boolean start(Context context, boolean limitOnce) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Not support default phone.
            return false;
        }
        if (limitOnce) {
            if (!sFlagOnce) {
                return false;
            }
            sFlagOnce = false;
        }

        if (ConfigEvent.dialerEnable()) {
            if (!DefaultPhoneUtils.isDefaultPhone()) {
                DefaultPhoneUtils.saveSystemDefaultPhone();

                if (ConfigEvent.setDefaultGuideShow()) {
                    Intent starter = new Intent(context, GuideSetDefaultActivity.class);
                    context.startActivity(starter);
                } else {
                    DefaultPhoneUtils.checkDefaultPhoneSettings();
                }
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
                .setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#1c1b29"),
                        Dimensions.pxFromDp(12),
                        false));
        Button actionBtn = findViewById(R.id.guide_action);
        actionBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_BOLD));
        actionBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#dcdcdc"),
                Dimensions.pxFromDp(22),
                true));
        actionBtn.setOnClickListener(v ->
        {
            ConfigEvent.guideConfirmed();
            DefaultPhoneUtils.checkDefaultPhoneSettings();
            mOkClicked = true;
            finish();
        });

        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigEvent.guideClose();
                finish();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (!mOkClicked) {

        }
        super.onDestroy();
    }
}

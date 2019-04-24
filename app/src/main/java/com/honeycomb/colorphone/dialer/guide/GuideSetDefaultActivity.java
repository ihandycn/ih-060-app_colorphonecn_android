package com.honeycomb.colorphone.dialer.guide;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.startguide.SetAsDialerDialog;
import com.honeycomb.colorphone.util.FontUtils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

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

                if (ConfigEvent.setDefaultGuideShow() && AutoPermissionChecker.hasFloatWindowPermission()) {
                    FloatWindowManager.getInstance().showDialog(new SetAsDialerDialog(context.getApplicationContext()));
//                    Intent starter = new Intent(context, GuideSetDefaultActivity.class);
//                    if (context instanceof Activity) {
//                        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                    }
//                    context.startActivity(starter);
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
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.guide_set_default_phone);

        ConfigEvent.guideShow();

        findViewById(R.id.dialog_content_container)
                .setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#1c1b29"),
                        Dimensions.pxFromDp(12),
                        false));
        Button actionBtn = findViewById(R.id.guide_action);
        actionBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_BOLD));
        actionBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#dcdcdc"),
                Color.parseColor("#55000000"),
                Dimensions.pxFromDp(22),
                false, true));
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
}

package com.honeycomb.colorphone.gdpr;


import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSGdprConsent;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class DataUsageSettingsActivity extends HSAppCompatActivity
        implements View.OnClickListener, SwitchCompat.OnCheckedChangeListener {

    private SwitchCompat mDataUsageSwitchButton;
    private View mContainer;
    private DataUsageAlertDialog closeDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_usage_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.gdpr_settings_cell);
        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);
        mDataUsageSwitchButton = findViewById(R.id.data_usage_toggle_button);
        mDataUsageSwitchButton.setOnCheckedChangeListener(this);
        mDataUsageSwitchButton.setChecked(HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.ACCEPTED);

        mContainer = findViewById(R.id.data_usage_cell);
        mContainer.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (closeDialog != null) {
            closeDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mContainer) {
            if (mDataUsageSwitchButton.isChecked()) {
                showCloseDialog();
            } else {
                mDataUsageSwitchButton.performClick();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mDataUsageSwitchButton) {
            GdprUtils.setDataUsageUserEnabled(true);
        }
    }

    private void showCloseDialog() {
        if (closeDialog == null) {
            closeDialog = new DataUsageAlertDialog(this);

            closeDialog.setPositiveClickListener(new DataUsageAlertDialog.OnClickListener() {
                @Override
                public void onClick() {
                    if (closeDialog == null) {
                        return;
                    }
                    closeDialog.dismiss();
                    closeDialog = null;
                }
            });

            closeDialog.setNegativeClickListener(new DataUsageAlertDialog.OnClickListener() {
                @Override
                public void onClick() {
                    if (closeDialog == null) {
                        return;
                    }
                    closeDialog.dismiss();
                    closeDialog = null;

                    LauncherAnalytics.logEvent("GDPR_Access_Closed_Settings");
                    GdprUtils.setDataUsageUserEnabled(false);
                    mDataUsageSwitchButton.setChecked(false);
                }
            });
        }
        closeDialog.show();
    }
}


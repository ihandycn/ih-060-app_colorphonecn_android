package com.honeycomb.colorphone.gdpr;


import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Threads;

public class DataUsageSettingsActivity extends HSAppCompatActivity
        implements View.OnClickListener, SwitchCompat.OnCheckedChangeListener {

    private SwitchCompat mDataUsageSwitchButton;
    private View mContainer;
    private DataUsageAlertDialog mCloseDialog;
    private RestartingAppProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_usage_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.gdpr_settings_cell);
        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);
        mDataUsageSwitchButton = findViewById(R.id.data_usage_toggle_button);
        mDataUsageSwitchButton.setOnCheckedChangeListener(this);
        mDataUsageSwitchButton.setFocusableInTouchMode(false);
        mDataUsageSwitchButton.setClickable(false);

        mContainer = findViewById(R.id.data_usage_cell);
        mContainer.setOnClickListener(this);
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
    protected void onDestroy() {
        super.onDestroy();
        if (mCloseDialog != null) {
            mCloseDialog.dismiss();
        }
        dismissLoadingDialog();
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
    }

    private void showCloseDialog() {
        dismissLoadingDialog();
        if (mCloseDialog == null) {
            mCloseDialog = new DataUsageAlertDialog(this);

            mCloseDialog.setPositiveClickListener(new DataUsageAlertDialog.OnClickListener() {
                @Override
                public void onClick() {
                    if (mCloseDialog == null) {
                        return;
                    }
                    mCloseDialog.dismiss();
                    mCloseDialog = null;
                }
            });

            mCloseDialog.setNegativeClickListener(new DataUsageAlertDialog.OnClickListener() {
                @Override
                public void onClick() {
                    if (mCloseDialog == null) {
                        return;
                    }
                    mCloseDialog.dismiss();
                    mCloseDialog = null;
//                    Analytics.logEvent("GDPR_Access_Closed_Settings");
                    mDataUsageSwitchButton.setChecked(false);
                    showProgressDialog();

                    Threads.postOnMainThreadDelayed((new Runnable() {
                        @Override
                        public void run() {
                            System.exit(0);
                        }
                    }), 3 * DateUtils.SECOND_IN_MILLIS);
                }
            });
        }
        mCloseDialog.show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = RestartingAppProgressDialog.show(getSupportFragmentManager());
        }
    }

    private void dismissLoadingDialog() {
        if (mProgressDialog != null && !isFinishing()) {
            mProgressDialog.dismissAllowingStateLoss();
            mProgressDialog = null;
        }
    }
}


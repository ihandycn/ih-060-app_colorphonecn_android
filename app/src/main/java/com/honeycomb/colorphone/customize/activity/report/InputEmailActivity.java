package com.honeycomb.colorphone.customize.activity.report;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.ReportManager;
import com.honeycomb.colorphone.customize.activity.BaseCustomizeActivity;
import com.honeycomb.colorphone.util.Utils;
import com.superapps.util.Toasts;

import java.util.Map;

import static com.honeycomb.colorphone.customize.activity.report.SelectReportReasonActivity.INTENT_KEY_REPORT_REASON;
import static com.honeycomb.colorphone.customize.activity.report.SelectReportReasonActivity.INTENT_KEY_WALLPAPER_URL;

public class InputEmailActivity extends BaseCustomizeActivity {

    private TextInputLayout inputLayout;
    private TextInputEditText inputEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_email);
        inputLayout = findViewById(R.id.input_layout);
        inputEditText = findViewById(R.id.input_edit_text);

        Button submit = findViewById(R.id.submit_btn);

        submit.setOnClickListener((View v) -> {
            String email = inputEditText.getText().toString();
            if (!email.isEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    inputLayout.setError(getString(R.string.customize_input_incorrect_email));
                } else {
                    // todo report
                    finishAffinity();
                    Map<String, String> report = new ArrayMap<>();
                    String reason =  getIntent().getStringExtra(INTENT_KEY_REPORT_REASON);
                    String url = getIntent().getStringExtra(INTENT_KEY_WALLPAPER_URL);
                    report.put("email", email);
                    report.put("reportUrl", url);
                    report.put("reason", reason);
                    ReportManager.report(report);
                    new Handler().postDelayed(() -> {
                        Toasts.showToast(R.string.customize_report_succeed);
                    }, 2000);
                }
            }
        });

        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int characterCount = charSequence.toString().length();
                submit.setEnabled(characterCount > 0);
                inputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        initActionBar();
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
        if (Utils.ATLEAST_LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }



}

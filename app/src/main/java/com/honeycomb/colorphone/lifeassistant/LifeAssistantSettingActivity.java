package com.honeycomb.colorphone.lifeassistant;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.activity.HSAppCompatActivity;

public class LifeAssistantSettingActivity extends HSAppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.life_assistant_setting_page);

        View close = findViewById(R.id.life_assistant_close);
        close.setOnClickListener(v -> {
            finish();
        });

        View rootView = findViewById(R.id.life_assistant_setting_layout);
        SwitchCompat switchView = findViewById(R.id.life_assistant_setting_switch);
        boolean state = true;

        switchView.setChecked(state);

        rootView.setOnClickListener(v -> switchView.toggle());

        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {


        });

    }
}

package com.colorphone.ringtones.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author sundxing
 */
public class RingtoneEntranceActivity extends AppCompatActivity {

    RingtonePageView mRingtonePageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRingtonePageView = new RingtonePageView(this);
        setContentView(mRingtonePageView);
    }

    @Override
    public void onBackPressed() {
        if (!mRingtonePageView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRingtonePageView.onStart();
    }

}

package com.honeycomb.colorphone.dialer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.acb.call.activity.HSAppCompatActivity;
import com.honeycomb.colorphone.R;

public class IncomingCallActivity extends HSAppCompatActivity {



    public static Intent getIntent(
            Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, InCallActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setWindowFlags();
        setContentView(R.layout.incall_screen);
    }

    private void setWindowFlags() {
        // Allow the activity to be shown when the screen is locked and filter out touch events that are
        // "too fat".
        int flags =
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;

        // When the audio stream is not via Bluetooth, turn on the screen once the activity is shown.

        flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

        getWindow().addFlags(flags);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

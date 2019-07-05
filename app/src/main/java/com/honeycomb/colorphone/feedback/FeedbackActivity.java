package com.honeycomb.colorphone.feedback;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Fonts;
import com.superapps.util.Toasts;

import java.util.Map;

import hugo.weaving.DebugLog;

/**
 * Activity for the user to send feedback.
 */
public class FeedbackActivity extends HSAppCompatActivity implements View.OnClickListener {

    private static final int MESSAGE_SHOW_KEYBOARD = 1021;

    private EditText mContentInput;
    private EditText mEmailInput;
    private TextView mSubmitBtn;

    private KeyboardHandler mHandler = new KeyboardHandler();

    public static final String INTENT_KEY_LAUNCH_FROM = "launch_from";
    public static final int LAUNCH_FROM_SETTING = 0;
    public static final int LAUNCH_FROM_RATING = 1;

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        final InsideScrollableScrollView scrollView = findViewById(R.id.feedback_scroll_view);
        mContentInput = findViewById(R.id.feedback_content);
        mEmailInput = findViewById(R.id.feedback_email);
        mSubmitBtn = findViewById(R.id.feedback_submit_btn);

        mSubmitBtn.setOnClickListener(this);

        Typeface typeface = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR);
        mContentInput.setTypeface(typeface);
        mEmailInput.setTypeface(typeface);
        mEmailInput.setText(getGoogleAccountEmail());

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.setScrollableDescendant(mContentInput);
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        int launchSource = getIntent().getIntExtra(INTENT_KEY_LAUNCH_FROM, LAUNCH_FROM_SETTING);
        Analytics.logEvent("Alert_Feedback_Shown", "type",
                launchSource == LAUNCH_FROM_RATING ? "From 5 Star Rating" : "From Launcher Setting");
    }

    @Override protected void onStop() {
        super.onStop();
        Analytics.logEvent("Alert_Feedback_Close");
    }

    private String getGoogleAccountEmail() {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        if (manager != null) {
            Account[] accounts = manager.getAccountsByType("com.google");
            for (Account account : accounts) {
                String possibleEmail = account.name;
                if (Patterns.EMAIL_ADDRESS.matcher(possibleEmail).matches()) {
                    return possibleEmail;
                }
            }
        }
        return "";
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_KEYBOARD, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            if (mHandler.hasMessages(MESSAGE_SHOW_KEYBOARD)) {
                mHandler.removeMessages(MESSAGE_SHOW_KEYBOARD);
            } else {
                Utils.hideKeyboard(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSubmitBtn) {
            String email = mEmailInput.getText().toString().trim();
            String content = mContentInput.getText().toString();
            if (content.isEmpty()) {
                Toasts.showToast(R.string.feedback_toast_empty_content);
                Analytics.logEvent("ColorPhone_Feedback_Btn_Clicked", "result", "feedback_empty");
                return;
            }
            

            Map<String, String> feedback = new ArrayMap<>();
            feedback.put("email", email);
            feedback.put("content", content);
            FeedbackManager.sendFeedback(feedback);
            Toasts.showToast(R.string.feedback_toast_send_success);
            Analytics.logEvent("ColorPhone_Feedback_Btn_Clicked", "result", "success");
            finish();
        }
    }

    @SuppressLint("HandlerLeak")
    private class KeyboardHandler extends Handler {
        KeyboardHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SHOW_KEYBOARD) {
                mContentInput.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                Utils.showKeyboard(FeedbackActivity.this);
            }
        }
    }
}

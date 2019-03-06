package com.honeycomb.colorphone.triviatip;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotEvent;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public class TriviaTipActivity extends AppCompatActivity implements TriviaTipLayout.onTipDismissListener {
    private static final String TAG = TriviaTipActivity.class.getSimpleName();
    public static final String EXTRA_ITEM = "extra_item_trivia";
    private TriviaTipLayout mTriviaTipLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        TriviaItem item = (TriviaItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        showTip(item);
        AutopilotEvent.onExtendedActive();
    }

    private void showTip(TriviaItem triviaItem) {
        mTriviaTipLayout = (TriviaTipLayout) View.inflate(getContext(), R.layout.trivia_tip, null);
        setContentView(mTriviaTipLayout);
        mTriviaTipLayout.bind(triviaItem);
        mTriviaTipLayout.setOnDismissListener(this);
        mTriviaTipLayout.show();
    }

    private Context getContext() {
        return this;
    }

    protected void onResume() {
        super.onResume();
        if (mTriviaTipLayout != null) {
            mTriviaTipLayout.onResume();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        HSLog.d("TriviaTip", "onDestroy");
        if (mTriviaTipLayout != null) {
            mTriviaTipLayout.dismiss();
        }
        mTriviaTipLayout = null;
        AutopilotEvent.onExtendedDeactive();
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (mTriviaTipLayout != null) {
            mTriviaTipLayout.onBackPressed();
        }
    }

    @Override
    public void onDismiss() {
        mTriviaTipLayout = null;
        finish();
    }

}

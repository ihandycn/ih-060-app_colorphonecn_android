package com.honeycomb.colorphone.triviatip;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

public class TriviaTipActivity extends AppCompatActivity implements TriviaTipLayout.onTipDismissListener {
    private static final String TAG = TriviaTipActivity.class.getSimpleName();
    public static final String EXTRA_ITEM = "extra_item_trivia";
    private TriviaTipLayout mTriviaTipLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TriviaItem item = (TriviaItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        showTip(item);
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

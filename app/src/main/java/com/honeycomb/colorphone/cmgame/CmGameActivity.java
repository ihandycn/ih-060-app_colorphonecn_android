package com.honeycomb.colorphone.cmgame;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;

import com.cmcm.cmgame.CmGameSdk;
import com.cmcm.cmgame.IAppCallback;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public class CmGameActivity extends AppCompatActivity implements IAppCallback {
    private final static float MIN_DOWN_DST = 100f;
    private boolean mIsSlideClosing = false;
    private int mUsefulSlideCount = 0;
    private float mTouchDownY = 0f;

    private boolean mLastScrollToBottom = false;
    private boolean mIsMoving = false;

    private NestedScrollView mContainer;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(Color.BLACK);

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);

        if (keyguardManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        }


        setContentView(R.layout.activity_cm_game);
        mContainer = findViewById(R.id.cm_game_scroll_container);
        mContainer.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (!mIsMoving && isCurrentAtBottom()) {
                        mLastScrollToBottom = true;
                    }
                });

        // 初始化小游戏 sdk 的账号数据，用于存储游戏内部的用户数据，
        // 为避免数据异常，这个方法建议在小游戏列表页面展现前（可以是二级页面）才调用
        CmGameSdk.INSTANCE.initCmGameAccount();
        CmGameSdk.INSTANCE.setGameClickCallback(this);

        mContainer.post(() -> mContainer.scrollTo(0, 0));
    }

    private boolean isCurrentAtBottom() {
        return mContainer.getChildAt(0).getMeasuredHeight()
                <= mContainer.getScrollY() + mContainer.getHeight();
    }

    @Override
    public void gameClickCallback(String gameName, String gameId) {
        Analytics.logEvent("GameCenter_Game_Played", "Game", gameName);
    }
}

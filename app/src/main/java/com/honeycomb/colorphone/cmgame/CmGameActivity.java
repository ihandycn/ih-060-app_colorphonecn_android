package com.honeycomb.colorphone.cmgame;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.cmcm.cmgame.CmGameSdk;
import com.cmcm.cmgame.IAppCallback;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;

public class CmGameActivity extends AppCompatActivity implements IAppCallback, View.OnTouchListener {
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

        setContentView(R.layout.activity_cm_game);
        mContainer = findViewById(R.id.cm_game_scroll_container);
        mContainer.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (!mIsMoving && isCurrentAtBottom()){
                        mLastScrollToBottom = true;
                    }
                });
        mContainer.setOnTouchListener(this);

        // 初始化小游戏 sdk 的账号数据，用于存储游戏内部的用户数据，
        // 为避免数据异常，这个方法建议在小游戏列表页面展现前（可以是二级页面）才调用
        CmGameSdk.INSTANCE.initCmGameAccount();
        CmGameSdk.INSTANCE.setGameClickCallback(this);

        mContainer.post(()->mContainer.scrollTo(0,0));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                if (!mIsMoving) {
                    mIsMoving = true;
                    mTouchDownY = event.getY();
                } else if(mLastScrollToBottom){
                    if (isCurrentAtBottom() && mTouchDownY - event.getY() > MIN_DOWN_DST) {
                        finishAndLog();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!mLastScrollToBottom) {
                    if ((mTouchDownY - event.getY() >= MIN_DOWN_DST)) {
                        mUsefulSlideCount++;
                    }
                }
                mLastScrollToBottom = isCurrentAtBottom();
                mIsMoving = false;
                mTouchDownY = 0;
                break;
        }
        return false;
    }

    private boolean isCurrentAtBottom() {
        return mContainer.getChildAt(0).getMeasuredHeight()
                <= mContainer.getScrollY() + mContainer.getHeight();
    }

    private void finishAndLog(){
        if (!mIsSlideClosing) {
            mIsSlideClosing = true;
            Analytics.logEvent("GameCenter_Closed", false, "Closedway", "slide");
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mIsSlideClosing) {
            Analytics.logEvent("GameCenter_Closed", false, "Closedway", "notslide");
        }
        Analytics.logEvent("GameCenter_List_Slide", false, "Type", "" + mUsefulSlideCount);
    }

    @Override
    public void gameClickCallback(String gameName, String gameId) {
        HSLog.d(CmGameUtil.TAG, "click $gameID----$gameName");
        Analytics.logEvent("GameCenter_Game_Played", true, "Game", gameName);
    }
}

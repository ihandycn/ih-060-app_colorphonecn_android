package com.honeycomb.colorphone.boost;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

public class BoostActivity extends HSActivity implements INotificationObserver {

    private static final String EXTRA_KEY_RESULT_TYPE = "EXTRA_KEY_RESULT_TYPE";

    private ViewGroup mContent;
    private WindowManager.LayoutParams mBoostTipParams;

    private BlackHole mBlackHole;
    private ViewGroup.LayoutParams mParams;
    private int resultPageType = ResultConstants.RESULT_TYPE_BOOST_PLUS;
    private boolean isResumed;
    private Runnable mResumePendingRunnable;

    public static void start(Context context, boolean toolbar) {
        Intent intent = new Intent(context, BoostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        if (toolbar) {
            intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_TOOLBAR);
        } else {
            intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
        }
        context.startActivity(intent);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSAlertMgr.delayRateAlert();

        Utils.showWhenLocked(this);

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_boost);

        mContent = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

        if (getIntent() != null) {
            resultPageType = getIntent().getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
        }

//        String wallpaperUrl = HSPreferenceHelper.create(HSApplication.getContext(),
//                WallpaperContainer.LOCKER_PREFS).getString(PREF_KEY_CURRENT_WALLPAPER_HD_URL,
//                HSConfig.optString(DEFAULT_WALLPAPER_URL, "Application", "WallPaper", "DefaultWallpaper"));
//
//        ImageView wallpaper = (ImageView) findViewById(R.id.background);
//        wallpaper.setImageBitmap(ImageLoader.getInstance().loadImageSync(wallpaperUrl));

        ImageView wallpaper = findViewById(R.id.background);
        wallpaper.setBackgroundColor(0xff2572E3);
        startForeignIconAnimation();
        HSGlobalNotificationCenter.addObserver(BlackHole.EVENT_BLACK_HOLE_ANIMATION_END, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if (mResumePendingRunnable != null) {
            mResumePendingRunnable.run();
            mResumePendingRunnable = null;
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        HSGlobalNotificationCenter.removeObserver(this);
        super.onDestroy();
    }

    private void startForeignIconAnimation() {
        mBlackHole = new BlackHole(BoostActivity.this);
        mParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContent.addView(mBlackHole, mParams);
        mBlackHole.setBlackHoleAnimationListener(new BlackHole.BlackHoleAnimationListener() {
            @Override
            public void onEnd() {
//                finishWithoutAnimation();
            }
        });
        mBlackHole.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBlackHole.startAnimation();
            }
        }, 300);
        ResultPageManager.getInstance().preloadResultPageAds();
    }

    private void finishWithoutAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        finishWithoutAnimation();
        super.onBackPressed();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case BlackHole.EVENT_BLACK_HOLE_ANIMATION_END:
                if (isResumed) {
                    startResultPageActivity();
                } else {
                    mResumePendingRunnable = new Runnable() {
                        @Override
                        public void run() {
                            startResultPageActivity();
                        }
                    };
                }
                break;
            default:
                break;
        }
    }

    private void startResultPageActivity() {
        int cleanSizeMb = BoostUtils.getBoostedMemSizeBytes(this, mBlackHole.getBoostedPercentage()) / (1024 * 1024);
        ResultPageActivity.startForBoostPlus(this, cleanSizeMb, resultPageType);

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                finishWithoutAnimation();
            }
        }, 400);
    }
}

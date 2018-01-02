package com.honeycomb.colorphone.boost;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;


public class BoostActivity extends HSActivity implements INotificationObserver {

    private static final String EXTRA_KEY_RESULT_TYPE = "EXTRA_KEY_RESULT_TYPE";

    private ViewGroup mContent;

    private BlackHole mBlackHole;
    private ViewGroup.LayoutParams mParams;
//    private int resultPageType = ResultConstants.RESULT_TYPE_BOOST_PLUS;

    public static void start(Context context, boolean toolbar) {
        Intent intent = new Intent(context, BoostActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        if (toolbar) {
//            intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_TOOLBAR);
//        } else {
//            intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
//        }
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

//        if (getIntent() != null) {
//            resultPageType = getIntent().getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
//        }

//        String wallpaperUrl = HSPreferenceHelper.create(HSApplication.getContext(),
//                WallpaperContainer.LOCKER_PREFS).getString(PREF_KEY_CURRENT_WALLPAPER_HD_URL,
//                HSConfig.optString(DEFAULT_WALLPAPER_URL, "Application", "WallPaper", "DefaultWallpaper"));
//
//        ImageView wallpaper = (ImageView) findViewById(R.id.background);
//        wallpaper.setImageBitmap(ImageLoader.getInstance().loadImageSync(wallpaperUrl));

        startForeignIconAnimation();
        HSGlobalNotificationCenter.addObserver(BlackHole.EVENT_BLACK_HOLE_ANIMATION_END, this);
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
//        ResultPageManager.getInstance().preLoadAdsAndMemInfo();
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
//                showBoostTip(this, mBlackHole.getBoostedPercentage());
//                break;
                finishWithoutAnimation();

                int cleanSizeMb = BoostUtils.getBoostedMemSizeBytes(this, mBlackHole.getBoostedPercentage()) / (1024 * 1024);
//                ResultPageActivity.startForBoostPlus(this, cleanSizeMb, resultPageType);




//            case BoostTip.EVENT_BOOST_TIP_FINISHED:
//                finishWithoutAnimation();
//
//                int cleanSizeMb = BoostUtils.getBoostedMemSizeBytes(this, mBlackHole.getBoostedPercentage()) / (1024 * 1024);
//                ResultPageActivity.startForBoostPlus(this, cleanSizeMb, resultPageType);
//
//                break;
            default:
                break;
        }
    }
}

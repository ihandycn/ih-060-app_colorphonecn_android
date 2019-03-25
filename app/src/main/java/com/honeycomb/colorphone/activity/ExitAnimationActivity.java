package com.honeycomb.colorphone.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Navigations;

import net.appcloudbox.ads.base.AcbInterstitialAd;

/**
 * @author sundxing
 */
public class ExitAnimationActivity extends AppCompatActivity {

    private boolean adPendingShow;
    private boolean adShowing;
    private LottieAnimationView animationView;
    private HomeKeyWatcher mHomeKeyWatcher;

    public static void start(Context context) {
        Intent starter = new Intent(context, ExitAnimationActivity.class);
        Navigations.startActivitySafely(context, starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exit_anim_layout);


        String type = Ap.IdleExitAd.animType();

        animationView = findViewById(R.id.exit_anim);

        animationView.setAnimation(Ap.IdleExitAd.TYPE_MAN.equals(type)
                ? "lottie/exit_anim_bye.json"
                : "lottie/exit_anim_bye_phone.json" );
        animationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                finish();
            }
        });
        animationView.setVisibility(View.INVISIBLE);
        AcbInterstitialAd ad = Ap.IdleExitAd.getInterstitialAd();
        if (ad != null) {
            ad.show();
            adPendingShow = true;
            Ap.IdleExitAd.logEvent("wire_after_callassistant_show");
            LauncherAnalytics.logEvent("wire_after_callassistant_show");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adPendingShow) {
            adPendingShow = false;
            adShowing = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!adPendingShow) {
            // Back from ad
            // or no need show ad
            animationView.playAnimation();
            animationView.setVisibility(View.VISIBLE);
            Ap.IdleExitAd.logEvent("call_assistant_close_animation_show");
            LauncherAnalytics.logEvent("call_assistant_close_animation_show");

            listenHomeKey();

        }
    }

    private void listenHomeKey() {
        if (mHomeKeyWatcher == null) {
            mHomeKeyWatcher = new HomeKeyWatcher(this);
            mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    Ap.IdleExitAd.logEvent("call_assistant_close_animation_interrupt");
                    LauncherAnalytics.logEvent("call_assistant_close_animation_Interrupt", "type", "Home");
                }

                @Override
                public void onRecentsPressed() {

                }
            });
            mHomeKeyWatcher.startWatch();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Ap.IdleExitAd.releaseInterstitialAd();
        if (mHomeKeyWatcher != null) {
            mHomeKeyWatcher.stopWatch();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Ap.IdleExitAd.logEvent("call_assistant_close_animation_interrupt");
        LauncherAnalytics.logEvent("call_assistant_close_animation_Interrupt", "type", "Back");
    }
}

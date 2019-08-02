package com.honeycomb.colorphone.dialer;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.dialer.util.CallFloatButton;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FloatCallButtonManager implements
        InCallPresenter.InCallStateListener {


    private Chronometer bottomTimerView;
    private LottieAnimationView mInCallDesktopButton;
    private LinearLayout mCallAnsweringLayout;
    private ImageView mCallAnsweringView;

    private DialerCall mOutCall;
    private boolean isTimerStarted;
    private CallFloatButton mCallFloatButton;


    public void show(Context context) {
        if (mCallFloatButton == null) {
            DialerCall firstCall = CallList.getInstance().getFirstCall();
            if (firstCall == null || DialerCallState.isDie(firstCall.getState())) {
                // No need
                return;
            }
            mCallFloatButton = new CallFloatButton(context);
            mCallAnsweringLayout = mCallFloatButton.getmCallAnsweringLayout();
            mCallAnsweringView = mCallFloatButton.getmCallAnsweringView();
            bottomTimerView = mCallFloatButton.getCallDurationView();
            mInCallDesktopButton = mCallFloatButton.getInCallDestopButton();
            mCallFloatButton.show();
            isTimerStarted = false;
            updateContactIfChanged();

        }
    }

    public void hide() {
        if (mCallFloatButton != null) {
            mCallFloatButton.dismiss();
            bottomTimerView = null;
            mCallAnsweringLayout = null;
            mCallAnsweringView = null;
            mInCallDesktopButton = null;
            mCallFloatButton = null;
            isTimerStarted = false;
        }
    }

    @Override
    public void onStateChange(InCallPresenter.InCallState oldState, InCallPresenter.InCallState newState, CallList callList) {
        // Contact info
        if (newState == InCallPresenter.InCallState.NO_CALLS) {
            hide();
        } else {
            if (mCallFloatButton != null) {
                updateContactIfChanged();
            }
        }
    }

    private void updateContactIfChanged() {
        DialerCall firstCall = CallList.getInstance().getFirstCall();
        boolean isSameCall = DialerCall.areSame(firstCall, mOutCall);
        if (isSameCall) {
            // Need to hide
            boolean isHangup = firstCall != null
                    && (firstCall.getState() == DialerCallState.DISCONNECTING
                    || firstCall.getState() == DialerCallState.DISCONNECTED);

            if (isHangup) {
                hide();
                return;
            }

            // Timer or status text.
            boolean isTimerVisible = firstCall != null
                    && firstCall.getState() == DialerCallState.ACTIVE;

            if (isTimerVisible) {
                mInCallDesktopButton.setAnimation("lottie/call_float_button/dialer_after_answering_lottie.json");
                mInCallDesktopButton.playAnimation();
                mCallAnsweringLayout.setVisibility(View.VISIBLE);
                bottomTimerView.setVisibility(View.VISIBLE);
                mCallAnsweringView.setVisibility(View.VISIBLE);
                bottomTimerView.setBase(
                        firstCall.getConnectTimeMillis()
                                - System.currentTimeMillis()
                                + SystemClock.elapsedRealtime());
                if (!isTimerStarted) {
                    LogUtil.i(
                            "FloatCallButtonManager.updateBottomRow",
                            "starting timer with base: %d",
                            bottomTimerView.getBase());
                    bottomTimerView.start();
                    isTimerStarted = true;
                }
            } else {
                mInCallDesktopButton.setAnimation("lottie/call_float_button/dialer_prompt_answer.json");
                mInCallDesktopButton.playAnimation();
                bottomTimerView.setVisibility(View.GONE);
                mCallAnsweringLayout.setVisibility(View.GONE);
                mCallAnsweringView.setVisibility(View.GONE);
                bottomTimerView.stop();
                isTimerStarted = false;
            }

        }
        mOutCall = firstCall;
    }

}

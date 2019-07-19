package com.honeycomb.colorphone.dialer;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Chronometer;

import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.dialer.util.CallFloatButton;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FloatCallButtonManager implements
        InCallPresenter.InCallStateListener {


    private Chronometer bottomTimerView;

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
            bottomTimerView = mCallFloatButton.getCallDurationView();
            mCallFloatButton.show();
            isTimerStarted = false;
            updateContactIfChanged();

        }
    }

    public void hide() {
        if (mCallFloatButton != null) {
            mCallFloatButton.dismiss();
            bottomTimerView = null;
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
            // Timer or status text.
            boolean isTimerVisible = firstCall != null
                    && firstCall.getState() == DialerCallState.ACTIVE;
            if (isTimerVisible) {
                bottomTimerView.setVisibility(View.VISIBLE);
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
                bottomTimerView.setVisibility(View.GONE);
                bottomTimerView.stop();
                isTimerStarted = false;
            }

        }
        mOutCall = firstCall;
    }

}

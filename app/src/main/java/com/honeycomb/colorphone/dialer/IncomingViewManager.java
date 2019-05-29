package com.honeycomb.colorphone.dialer;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.acb.call.FlashScreenPresenter;
import com.acb.call.service.InCallWindow;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;

@RequiresApi(api = Build.VERSION_CODES.M)
public class IncomingViewManager implements
        InCallPresenter.InCallStateListener,
        InCallPresenter.IncomingCallListener,
        ViewManager {

    private InCallWindow mInCallWindow;

    @Override
    public void onViewInit(InCallActivity activity, View mainCallView) {
        onViewReady();
    }


    @Override
    public void onViewDestroy() {
        onViewUnReady();
    }

    public void setInCallWindow(InCallWindow inCallWindow) {
        mInCallWindow = inCallWindow;
    }

    public void onViewReady() {
// register for call state changes last
        final InCallPresenter inCallPresenter = InCallPresenter.getInstance();
        inCallPresenter.addListener(this);
        inCallPresenter.addIncomingCallListener(this);
        final DialerCall dialerCall = CallList.getInstance().getIncomingCall();
        if (dialerCall != null) {
            FlashScreenPresenter.getInstance().onShowInDialer(dialerCall.getNumber());
            mInCallWindow.show(dialerCall.getNumber());
            mInCallWindow.setCallHandler(new InCallWindow.CallHandler() {
                @Override
                public void answer() {
                    dialerCall.answer();
                }

                @Override
                public void hangup() {
                    dialerCall.disconnect();
                }
            });
        } else {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("Incoming call view ready, but data lose!");
            }
            onViewUnReady();
        }
    }

    public void onViewUnReady() {
        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        if (mInCallWindow != null) {
            mInCallWindow.setCallHandler(null);
            mInCallWindow.endFlashCall();
        }
    }


    @Override
    public void onStateChange(InCallPresenter.InCallState oldState, InCallPresenter.InCallState newState, CallList callList) {
        // Contact info
        if (newState != InCallPresenter.InCallState.INCOMING) {
            onViewUnReady();
        }
    }


    @Override
    public void onIncomingCall(InCallPresenter.InCallState oldState, InCallPresenter.InCallState newState, DialerCall call) {

    }

}

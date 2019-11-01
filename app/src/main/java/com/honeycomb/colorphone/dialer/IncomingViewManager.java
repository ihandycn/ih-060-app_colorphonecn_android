package com.honeycomb.colorphone.dialer;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;

import com.acb.call.FlashScreenPresenter;
import com.acb.call.service.InCallWindow;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.util.StringUtils;

import java.io.IOException;

import okhttp3.ResponseBody;

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
            HttpManager.getInstance().getCallerAddressInfo(dialerCall.getNumber(), new Callback<ResponseBody>() {
                @Override
                public void onFailure(String errorMsg) {
                    if (mInCallWindow != null) {
                        mInCallWindow.show(dialerCall.getNumber(), "");
                    }
                }

                @Override
                public void onSuccess(ResponseBody responseBody) {
                    if (mInCallWindow != null) {
                        String string = "";
                        String address = "";
                        String province;
                        String city;
                        String operator;
                        try {
                            string = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!TextUtils.isEmpty(string)) {
                            province = StringUtils.getProvince(string);
                            city = StringUtils.getCity(string);
                            operator = StringUtils.getOperator(string);

                            if (!TextUtils.isEmpty(province)) {
                                if (province.equals(city)) {
                                    province = "";
                                }

                                address = province + " " + city + " " + operator;
                            }

                        }
                        mInCallWindow.show(dialerCall.getNumber(), address);
                    }
                }
            });

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

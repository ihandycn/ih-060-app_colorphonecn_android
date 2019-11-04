package com.honeycomb.colorphone.dialer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.text.TextUtils;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.StringUtils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Threads;

import java.io.IOException;

import okhttp3.ResponseBody;

@RequiresApi(api = Build.VERSION_CODES.M)
public class InCallCardManager implements
        InCallPresenter.InCallStateListener,
        InCallPresenter.InCallDetailsListener,
        InCallPresenter.IncomingCallListener,
        ViewManager {

    private TextView mContactView;
    private TextView mSecondTextView;
    private ViewAnimator bottomTextSwitcher;
    private TextView bottomTextView;
    private Chronometer bottomTimerView;

    private DialerCall mOutCall;
    private boolean isTimerStarted;

    @Override
    public void onViewInit(InCallActivity activity, View mainCallView) {
        mContactView = (TextView) mainCallView.findViewById(R.id.contactgrid_contact_name);
        mSecondTextView = (TextView) mainCallView.findViewById(R.id.second_line);
        bottomTextSwitcher = mainCallView.findViewById(R.id.contactgrid_bottom_text_switcher);
        bottomTextView = mainCallView.findViewById(R.id.contactgrid_bottom_text);
        bottomTimerView = mainCallView.findViewById(R.id.contactgrid_bottom_timer);

        Typeface typeface = ConfigProvider.get().getCustomTypeface();
        if (typeface != null) {
            mContactView.setTypeface(typeface);
            mSecondTextView.setTypeface(typeface);
            bottomTextView.setTypeface(typeface);
            bottomTimerView.setTypeface(typeface);
        }
        onViewReady();
    }

    @Override
    public void onViewDestroy() {
        onViewUnReady();
    }

    public void onViewReady() {
// register for call state changes last
        updateContactIfChanged();

        final InCallPresenter inCallPresenter = InCallPresenter.getInstance();
        inCallPresenter.addListener(this);
        inCallPresenter.addIncomingCallListener(this);
        inCallPresenter.addDetailsListener(this);
    }

    public void onViewUnReady() {
        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        InCallPresenter.getInstance().removeDetailsListener(this);
    }

    // TODO use name direct.
    private void loadContactInfoBackground(Context context, final String number) {
        LogUtil.d("InCallCardManager.loadContactInfoBackground", "number : " + number);
        Uri phonesUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        String name = "";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(phonesUri,
                    projection, null, null, null);
            String thumbnailUri = null;
            if (cursor != null && cursor.moveToFirst()) {
                thumbnailUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
            final String nameStr = name;
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    onLoadContactName(number, nameStr);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void onLoadContactName(String number, String nameStr) {
        if (mContactView != null) {
            int nameDirection = View.TEXT_DIRECTION_INHERIT;
            if (TextUtils.isEmpty(nameStr)) {
                nameDirection = View.TEXT_DIRECTION_LTR;
            }
            mContactView.setTextDirection(nameDirection);
            mContactView.setText(TextUtils.isEmpty(nameStr) ? number : nameStr);

            HttpManager.getInstance().getCallerAddressInfo(number, new Callback<ResponseBody>() {
                @Override
                public void onFailure(String errorMsg) {
                    if (mSecondTextView != null) {
                        if (TextUtils.isEmpty(nameStr)) {
                            mSecondTextView.setVisibility(View.GONE);
                        } else {
                            mSecondTextView.setVisibility(View.VISIBLE);
                            mSecondTextView.setText(number);
                        }
                    }

                    Analytics.logEvent("Dialer_Answering_Page_Location_Details", "withlocation", "false");
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(ResponseBody responseBody) {
                    if (mSecondTextView != null) {
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

                        if (TextUtils.isEmpty(nameStr)) {
                            if (TextUtils.isEmpty(address)) {
                                mSecondTextView.setVisibility(View.GONE);
                            } else {
                                mSecondTextView.setVisibility(View.VISIBLE);
                                mSecondTextView.setText(address);
                            }
                        } else {
                            mSecondTextView.setVisibility(View.VISIBLE);
                            if (TextUtils.isEmpty(address)) {
                                mSecondTextView.setText(number);
                            } else {
                                mSecondTextView.setText(number + " " + address);
                            }
                        }

                        if (TextUtils.isEmpty(address)) {
                            Analytics.logEvent("Dialer_Answering_Page_Location_Details", "withlocation", "false");
                        } else {
                            Analytics.logEvent("Dialer_Answering_Page_Location_Details", "withlocation", "true");
                        }
                    } else {
                        Analytics.logEvent("Dialer_Answering_Page_Location_Details", "withlocation", "false");
                    }
                }
            });
        }
    }


    public CharSequence getState(Context context, DialerCall dialerCall) {
        CharSequence label;
        int state = dialerCall.getState();

        if (DialerCallState.isDialing(state)) {
            label = context.getString(R.string.incall_connecting);
        } else if (state == DialerCallState.DISCONNECTING) {
            // While in the DISCONNECTING state we display a "Hanging up" message in order to make the UI
            // feel more responsive.  (In GSM it's normal to see a delay of a couple of seconds while
            // negotiating the disconnect with the network, so the "Hanging up" state at least lets the
            // user know that we're doing something.  This state is currently not used with CDMA.)
            label = context.getString(R.string.incall_hanging_up);
        } else if (state == DialerCallState.DISCONNECTED) {
            label = dialerCall.getDisconnectCause().getLabel();
            if (TextUtils.isEmpty(label)) {
                label = context.getString(R.string.incall_call_ended);
            }
        } else {
            label = "";
        }

        return label;
    }


    @Override
    public void onStateChange(InCallPresenter.InCallState oldState, InCallPresenter.InCallState newState, CallList callList) {
        // Contact info

        updateContactIfChanged();
    }

    private void updateContactIfChanged() {
        DialerCall firstCall = CallList.getInstance().getFirstCall();
        if (mOutCall == null && firstCall != null) {
            // Only load only if first out going call.
            loadContactInfoBackground(HSApplication.getContext(), firstCall.getNumber());
        }

        boolean isSameCall = DialerCall.areSame(firstCall, mOutCall);

        if (isSameCall) {
            // Timer or status text.
            boolean isTimerVisible = firstCall != null
                    && firstCall.getState() == DialerCallState.ACTIVE;
            if (isTimerVisible) {
                bottomTextSwitcher.setDisplayedChild(1);
                bottomTimerView.setBase(
                        firstCall.getConnectTimeMillis()
                                - System.currentTimeMillis()
                                + SystemClock.elapsedRealtime());
                if (!isTimerStarted) {
                    LogUtil.i(
                            "ContactGridManager.updateBottomRow",
                            "starting timer with base: %d",
                            bottomTimerView.getBase());
                    bottomTimerView.start();
                    isTimerStarted = true;
                }
            } else {
                bottomTextSwitcher.setDisplayedChild(0);
                if (isTimerStarted) {
                    long duration = SystemClock.elapsedRealtime() - bottomTimerView.getBase();
                    EventLogger.get().setDuration(duration);
                }
                bottomTimerView.stop();
                isTimerStarted = false;
            }

            // Status text
            if (firstCall != null) {
                bottomTextView.setText(getState(HSApplication.getContext(), firstCall));
            }

        }

        mOutCall = firstCall;
    }

    @Override
    public void onIncomingCall(InCallPresenter.InCallState oldState, InCallPresenter.InCallState newState, DialerCall call) {

    }

    @Override
    public void onDetailsChanged(DialerCall call, Call.Details details) {

    }

}

package com.honeycomb.colorphone.dialer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telecom.CallAudioState;
import android.telecom.PhoneAccountHandle;
import android.view.View;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.InCallPresenter.InCallState;
import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.dialer.call.TelecomAdapter;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class InCallButtonManager implements InCallButtonUiDelegate,
        AudioModeProvider.AudioModeListener,
        InCallPresenter.InCallStateListener,
        InCallPresenter.InCallDetailsListener,
        InCallPresenter.IncomingCallListener,
        ViewManager {
    public static final String TAG = "InCallButtonManager";
    private static final int BUTTON_COUNT = 4;
    private static final int BUTTONS_PER_ROW = 3;

    private static final String KEY_AUTOMATICALLY_MUTED_BY_ADD_CALL =
            "incall_key_automatically_muted_by_add_call";
    private static final String KEY_PREVIOUS_MUTE_STATE = "incall_key_previous_mute_state";

    private final int[] mRouteOrderedList = new int[]{
            CallAudioState.ROUTE_WIRED_HEADSET,
            CallAudioState.ROUTE_EARPIECE
    };

    private CheckableLabeledButton[] buttons = new CheckableLabeledButton[BUTTON_COUNT];

    private List<ButtonController> mButtonController = new ArrayList<>();
    private InCallActivity mInCallActivity;

    private InCallButtonUi inCallButtonUi;
    private DialerCall call;
    private boolean automaticallyMutedByAddCall = false;
    private boolean previousMuteState = false;
    private boolean isInCallButtonUiReady;
    private PhoneAccountHandle otherAccount;

    @Override
    public void onViewInit(InCallActivity activity, View root) {

        mInCallActivity = activity;

        mButtonController.add(new ButtonController.MuteButtonController(this));
        mButtonController.add(new ButtonController.DialpadButtonController(this));
        mButtonController.add(new ButtonController.SpeakerButtonController(this));
        mButtonController.add(new ButtonController.BluetoothButtonController(this));


        buttons[0] = ((CheckableLabeledButton) root.findViewById(R.id.incall_first_button));
        buttons[1] = ((CheckableLabeledButton) root.findViewById(R.id.incall_second_button));
        buttons[2] = ((CheckableLabeledButton) root.findViewById(R.id.incall_third_button));
        buttons[3] = ((CheckableLabeledButton) root.findViewById(R.id.incall_fourth_button));

        root.findViewById(R.id.incall_end_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEndCallClicked();
            }
        });

        onInCallButtonUiReady(new InCallSimpleButtonUi());

    }

    @Override
    public void onViewDestroy() {
        onInCallButtonUiUnready();
    }

    private ButtonController getOrderedController(int i) {
        // Order if needed.
        return mButtonController.get(i);
    }


    public void updateUi(InCallPresenter.InCallState state, DialerCall call) {
        LogUtil.v("CallButtonPresenter", "updating call UI for call: %s", call);

        if (inCallButtonUi == null) {
            return;
        }

        final boolean isEnabled =
                state.isConnectingOrConnected() && !state.isIncoming() && call != null;
        inCallButtonUi.setEnabled(isEnabled);

        if (call == null) {
            return;
        }

        updateButtonsState(call);
    }

    @Override
    public void onStateChange(InCallState oldState, InCallState newState, CallList callList) {
        Trace.beginSection("CallButtonPresenter.onStateChange");
        if (newState == InCallPresenter.InCallState.OUTGOING) {
            call = callList.getOutgoingCall();
        } else if (newState == InCallState.INCALL) {
            call = callList.getActiveOrBackgroundCall();

            // When connected to voice mail, automatically shows the dialpad.
            // (On previous releases we showed it when in-call shows up, before waiting for
            // OUTGOING.  We may want to do that once we start showing "Voice mail" label on
            // the dialpad too.)
            if (oldState == InCallState.OUTGOING && call != null) {
                if (call.isVoiceMailNumber() && mInCallActivity != null) {
                    mInCallActivity.showDialpadFragment(true /* show */, true /* animate */);
                }
            }
        } else if (newState == InCallState.INCOMING) {
            call = callList.getIncomingCall();
            // TODO incomming
        } else {
            call = null;
        }

        updateUi(newState, call);

        Trace.endSection();
    }

    /**
     * Updates the buttons applicable for the UI.
     *
     * @param call The active call.
     */
    @SuppressWarnings(value = {"MissingPermission"})
    private void updateButtonsState(DialerCall call) {
        LogUtil.v("CallButtonPresenter.updateButtonsState", "");
        final boolean isVideo = call.isVideoCall();

        // Common functionality (audio, hold, etc).
        // Show either HOLD or SWAP, but not both. If neither HOLD or SWAP is available:
        //     (1) If the device normally can hold, show HOLD in a disabled state.
        //     (2) If the device doesn't have the concept of hold/swap, remove the button.
//        final boolean showSwap = call.can(android.telecom.Call.Details.CAPABILITY_SWAP_CONFERENCE);
//        final boolean showHold =
//                !showSwap
//                        && call.can(android.telecom.Call.Details.CAPABILITY_SUPPORT_HOLD)
//                        && call.can(android.telecom.Call.Details.CAPABILITY_HOLD);
//        final boolean isCallOnHold = call.getState() == DialerCallState.ONHOLD;
//
//        final boolean showAddCall =
//                TelecomAdapter.getInstance().canAddCall() && UserManagerCompat.isUserUnlocked(getContext());

        final boolean showMute = call.can(android.telecom.Call.Details.CAPABILITY_MUTE);


//        otherAccount = TelecomUtil.getOtherAccount(getContext(), call.getAccountHandle());
//        boolean showSwapSim =
//                !call.isEmergencyCall()
//                        && otherAccount != null
//                        && !call.isVoiceMailNumber()
//                        && DialerCallState.isDialing(call.getState())
//                        // Most devices cannot make calls on 2 SIMs at the same time.
//                        && InCallPresenter.getInstance().getCallList().getAllCalls().size() == 1;


//        inCallButtonUi.showButton(InCallButtonIds.BUTTON_AUDIO, true);
        inCallButtonUi.showButton(InCallButtonIds.BUTTON_AUDIO_SPEAKER, true);
        inCallButtonUi.showButton(InCallButtonIds.BUTTON_AUDIO_BLUE, true);

//        inCallButtonUi.showButton(InCallButtonIds.BUTTON_SWAP, showSwap);
//        inCallButtonUi.showButton(InCallButtonIds.BUTTON_HOLD, showHold);
        inCallButtonUi.showButton(InCallButtonIds.BUTTON_MUTE, showMute);
//        inCallButtonUi.showButton(InCallButtonIds.BUTTON_SWAP_SIM, showSwapSim);
//        inCallButtonUi.showButton(InCallButtonIds.BUTTON_ADD_CALL, true);
//        inCallButtonUi.enableButton(InCallButtonIds.BUTTON_ADD_CALL, showAddCall);
        inCallButtonUi.showButton(InCallButtonIds.BUTTON_DIALPAD, true);

        AudioHelper.support(getCurrentAudioState(), CallAudioState.ROUTE_BLUETOOTH);

        inCallButtonUi.updateButtonStates();
    }

    public void onInCallButtonUiReady(InCallButtonUi ui) {
        Assert.checkState(!isInCallButtonUiReady);
        inCallButtonUi = ui;
        AudioModeProvider.getInstance().addListener(this);

        // register for call state changes last
        final InCallPresenter inCallPresenter = InCallPresenter.getInstance();
        inCallPresenter.addListener(this);
        inCallPresenter.addIncomingCallListener(this);
        inCallPresenter.addDetailsListener(this);
//        inCallPresenter.addCanAddCallListener(this);

        // Update the buttons state immediately for the current call
        onStateChange(InCallState.NO_CALLS, inCallPresenter.getInCallState(), CallList.getInstance());
        isInCallButtonUiReady = true;
    }

    public void onInCallButtonUiUnready() {
        Assert.checkState(isInCallButtonUiReady);
        inCallButtonUi = null;
        InCallPresenter.getInstance().removeListener(this);
        AudioModeProvider.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        InCallPresenter.getInstance().removeDetailsListener(this);
//        InCallPresenter.getInstance().removeCanAddCallListener(this);
        isInCallButtonUiReady = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_AUTOMATICALLY_MUTED_BY_ADD_CALL, automaticallyMutedByAddCall);
        outState.putBoolean(KEY_PREVIOUS_MUTE_STATE, previousMuteState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        automaticallyMutedByAddCall =
                savedInstanceState.getBoolean(
                        KEY_AUTOMATICALLY_MUTED_BY_ADD_CALL, automaticallyMutedByAddCall);
        previousMuteState = savedInstanceState.getBoolean(KEY_PREVIOUS_MUTE_STATE, previousMuteState);
    }

    @Override
    public void refreshMuteState() {
        // Restore the previous mute state
        if (automaticallyMutedByAddCall
                && AudioModeProvider.getInstance().getAudioState().isMuted() != previousMuteState) {
            if (inCallButtonUi == null) {
                return;
            }
            muteClicked(previousMuteState, false /* clickedByUser */);
        }
        automaticallyMutedByAddCall = false;
    }

    @Override
    public void muteClicked(boolean checked, boolean clickedByUser) {
        HSLog.d(TAG,
                String.format("turning on mute: %s, clicked by user: %s", checked, clickedByUser));
        TelecomAdapter.getInstance().mute(checked);
    }

    @Override
    public void speakerClicked(boolean checked, boolean clickedByUser) {
        if (checked) {
            TelecomAdapter.getInstance().setAudioRoute(CallAudioState.ROUTE_SPEAKER);
        } else {
            int route = restoreLastRoute(getCurrentAudioState(), CallAudioState.ROUTE_SPEAKER);
            TelecomAdapter.getInstance().setAudioRoute(route);
        }
    }

    private int restoreLastRoute(CallAudioState currentAudioState, int exceptRoute) {
        if (exceptRoute != CallAudioState.ROUTE_BLUETOOTH) {
            if (AudioHelper.support(currentAudioState, CallAudioState.ROUTE_BLUETOOTH)) {
                return CallAudioState.ROUTE_BLUETOOTH;
            }
        }
        for (int route : mRouteOrderedList) {
            if (AudioHelper.support(currentAudioState, route)) {
                return route;
            }
        }
        return CallAudioState.ROUTE_EARPIECE;
    }

    @Override
    public void bluetoothClicked(boolean checked, boolean clickedByUser) {
        if (!AudioHelper.support(getCurrentAudioState(), CallAudioState.ROUTE_BLUETOOTH)) {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            if (null != intent.resolveActivity(getContext().getPackageManager())) {
                mInCallActivity.startActivity(intent);
            }
            return;
        }
        if (checked) {
            TelecomAdapter.getInstance().setAudioRoute(CallAudioState.ROUTE_BLUETOOTH);
        } else {
            int route = restoreLastRoute(getCurrentAudioState(), CallAudioState.ROUTE_BLUETOOTH);
            TelecomAdapter.getInstance().setAudioRoute(route);
        }
    }

    @Override
    public void showDialpadClicked(boolean checked) {
        LogUtil.v("showDialpadClicked", "show dialpad " + String.valueOf(checked));
        mInCallActivity.showDialpadFragment(checked /* show */, true /* animate */);
    }

    /**
     * Function assumes that bluetooth is not supported.
     */
    @Override
    public void toggleSpeakerphone() {
        // Nothing
    }

    @Override
    public CallAudioState getCurrentAudioState() {
        return AudioModeProvider.getInstance().getAudioState();
    }

    @Override
    public void setAudioRoute(int route) {
        HSLog.d(TAG,
                "sending new audio route: " + CallAudioState.audioRouteToString(route));
        TelecomAdapter.getInstance().setAudioRoute(route);
    }

    @Override
    public void onEndCallClicked() {
        HSLog.d(TAG,
                "onEndCallClicked");
        if (call != null) {
            call.disconnect();
        }
    }

    @Override
    public Context getContext() {
        return mInCallActivity.getApplicationContext();
    }


    @NonNull
    public ButtonController getButtonController(@InCallButtonIds int id) {
        for (ButtonController buttonController : mButtonController) {
            if (buttonController.getInCallButtonId() == id) {
                return buttonController;
            }
        }
        Assert.fail();
        return null;
    }

    /**
     * Updates the user interface in response to a change in the details of a call. Currently handles
     * changes to the call buttons in response to a change in the details for a call. This is
     * important to ensure changes to the active call are reflected in the available buttons.
     *
     * @param call The active call.
     * @param details The call details.
     */
    @Override
    public void onDetailsChanged(DialerCall call, android.telecom.Call.Details details) {
        // Only update if the changes are for the currently active call
        if (inCallButtonUi != null && call != null && call.equals(this.call)) {
            updateButtonsState(call);
        }
    }

    @Override
    public void onAudioStateChanged(CallAudioState audioState) {
        if (inCallButtonUi != null) {
            inCallButtonUi.setAudioState(audioState);
        }
    }

    @Override
    public void onIncomingCall(InCallState oldState, InCallState newState, DialerCall call) {
        onStateChange(oldState, newState, CallList.getInstance());

    }

    private class InCallSimpleButtonUi implements InCallButtonUi {

        @Override
        public void showButton(int buttonId, boolean show) {
            LogUtil.v(
                    "InCallSimpleButtonUi.showButton",
                    "buttionId: %s, show: %b",
                    InCallButtonIdsExtension.toString(buttonId),
                    show);
            if (isSupportedButton(buttonId)) {
                getButtonController(buttonId).setAllowed(show);
            }
        }

        @Override
        public void enableButton(int buttonId, boolean enable) {
            LogUtil.v(
                    "InCallSimpleButtonUi.enableButton",
                    "buttonId: %s, enable: %b",
                    InCallButtonIdsExtension.toString(buttonId),
                    enable);
            if (isSupportedButton(buttonId)) {
                getButtonController(buttonId).setEnabled(enable);
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            LogUtil.v("InCallSimpleButtonUi", "ALl button enabled: " + enabled);
            for (ButtonController buttonController : mButtonController) {
                buttonController.setEnabled(enabled);
            }
        }

        @Override
        public void setAudioState(CallAudioState audioState) {
            LogUtil.i("InCallSimpleButtonUi", "audioState: " + audioState);

            ButtonController bluetoothController = getButtonController(InCallButtonIds.BUTTON_AUDIO_BLUE);
            boolean supportBluetooth = AudioHelper.support(audioState, CallAudioState.ROUTE_BLUETOOTH);
                bluetoothController.setCheckable(supportBluetooth);
            bluetoothController.setChecked(AudioHelper.isUseRoute(audioState, CallAudioState.ROUTE_BLUETOOTH));

            getButtonController(InCallButtonIds.BUTTON_AUDIO_SPEAKER)
                    .setChecked(AudioHelper.isUseRoute(audioState, CallAudioState.ROUTE_SPEAKER));

            getButtonController(InCallButtonIds.BUTTON_MUTE)
                    .setChecked(audioState.isMuted());

            updateButtonStates();
        }

        /**
         *
         */
        @Override
        public void updateButtonStates() {
            for (int i = 0; i < BUTTON_COUNT; i++) {
                ButtonController buttonController = getOrderedController(i);
                buttonController.setButton(buttons[i]);
            }
        }
    }

    private boolean isSupportedButton(int id) {
        return id == InCallButtonIds.BUTTON_AUDIO_BLUE
                || id == InCallButtonIds.BUTTON_AUDIO_SPEAKER
                || id == InCallButtonIds.BUTTON_MUTE
                || id == InCallButtonIds.BUTTON_DIALPAD
//                || id == InCallButtonIds.BUTTON_HOLD
//                || id == InCallButtonIds.BUTTON_SWAP
//                || id == InCallButtonIds.BUTTON_UPGRADE_TO_VIDEO
//                || id == InCallButtonIds.BUTTON_ADD_CALL
//                || id == InCallButtonIds.BUTTON_MERGE
//                || id == InCallButtonIds.BUTTON_MANAGE_VOICE_CONFERENCE
//                || id == InCallButtonIds.BUTTON_SWAP_SIM
//                || id == InCallButtonIds.BUTTON_UPGRADE_TO_RTT
                ;
    }

    /**
     * Interface for the call button UI.
     */
    public interface InCallButtonUi {

        void showButton(@InCallButtonIds int buttonId, boolean show);

        void enableButton(@InCallButtonIds int buttonId, boolean enable);

        void setEnabled(boolean on);

//        void setHold(boolean on);
//
//        void setCameraSwitched(boolean isBackFacingCamera);
//
//        void setVideoPaused(boolean isPaused);

        void setAudioState(CallAudioState audioState);

        /**
         * Once showButton() has been called on each of the individual buttons in the UI, call this to
         * configure the overflow menu appropriately.
         */
        void updateButtonStates();
    }

    public static class AudioHelper {
        static boolean isUseRoute(CallAudioState audioState, int flag) {
            return (audioState.getRoute() & flag) == flag;
        }

        static boolean support(CallAudioState audioState, int flag) {
            return (audioState.getSupportedRouteMask() & flag) == flag;
        }
    }
}

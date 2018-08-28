package com.honeycomb.colorphone.dialer;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.telecom.CallAudioState;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Toast;

import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.dialer.disconnectdialog.DisconnectMessage;
import com.honeycomb.colorphone.dialer.incalluilock.InCallUiLock;
import com.honeycomb.colorphone.telecomeventui.InternationalCallOnWifiDialogFragment;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class InCallActivity extends HSAppCompatActivity {


    private Animation dialpadSlideInAnimation;
    private Animation dialpadSlideOutAnimation;
    private Dialog errorDialog;
    private GradientDrawable backgroundDrawable;
//    private InCallOrientationEventListener inCallOrientationEventListener;
    private View pseudoBlackScreenOverlay;
//    private SelectPhoneAccountDialogFragment selectPhoneAccountDialogFragment;
    private String dtmfTextToPrepopulate;
    private boolean allowOrientationChange;
    private boolean animateDialpadOnShow;
    private boolean didShowAnswerScreen;
    private boolean didShowInCallScreen;
    private boolean didShowVideoCallScreen;
    private boolean didShowRttCallScreen;
    private boolean didShowSpeakEasyScreen;
    private String lastShownSpeakEasyScreenUniqueCallid = "";
    private boolean dismissKeyguard;
    private boolean isInShowMainInCallFragment;
    private boolean isRecreating; // whether the activity is going to be recreated
    private boolean isVisible;
    private boolean needDismissPendingDialogs;
    private boolean touchDownWhenPseudoScreenOff;
    private int[] backgroundDrawableColors;
    private InCallOrientationEventListener inCallOrientationEventListener;

    private static class ShouldShowUiResult {
        public final boolean shouldShow;
        public final DialerCall call;

        ShouldShowUiResult(boolean shouldShow, DialerCall call) {
            this.shouldShow = shouldShow;
            this.call = call;
        }
    }

    private static final class IntentExtraNames {
        static final String FOR_FULL_SCREEN = "InCallActivity.for_full_screen_intent";
        static final String NEW_OUTGOING_CALL = "InCallActivity.new_outgoing_call";
        static final String SHOW_DIALPAD = "InCallActivity.show_dialpad";
    }

    private static final class KeysForSavedInstance {
        static final String DIALPAD_TEXT = "InCallActivity.dialpad_text";
        static final String DID_SHOW_ANSWER_SCREEN = "did_show_answer_screen";
        static final String DID_SHOW_IN_CALL_SCREEN = "did_show_in_call_screen";
        static final String DID_SHOW_VIDEO_CALL_SCREEN = "did_show_video_call_screen";
        static final String DID_SHOW_RTT_CALL_SCREEN = "did_show_rtt_call_screen";
        static final String DID_SHOW_SPEAK_EASY_SCREEN = "did_show_speak_easy_screen";
    }

    /** Request codes for pending intents. */
    public static final class PendingIntentRequestCodes {
        static final int NON_FULL_SCREEN = 0;
        static final int FULL_SCREEN = 1;
        static final int BUBBLE = 2;
    }

    private static final class Tags {
        static final String ANSWER_SCREEN = "tag_answer_screen";
        static final String DIALPAD_FRAGMENT = "tag_dialpad_fragment";
        static final String IN_CALL_SCREEN = "tag_in_call_screen";
        static final String INTERNATIONAL_CALL_ON_WIFI = "tag_international_call_on_wifi";
        static final String SELECT_ACCOUNT_FRAGMENT = "tag_select_account_fragment";
        static final String VIDEO_CALL_SCREEN = "tag_video_call_screen";
        static final String RTT_CALL_SCREEN = "tag_rtt_call_screen";
        static final String POST_CHAR_DIALOG_FRAGMENT = "tag_post_char_dialog_fragment";
        static final String SPEAK_EASY_SCREEN = "tag_speak_easy_screen";
        static final String RTT_REQUEST_DIALOG = "tag_rtt_request_dialog";
    }

    private static final class ConfigNames {
        static final String ANSWER_AND_RELEASE_ENABLED = "answer_and_release_enabled";
    }

    public static Intent getIntent(
            Context context, boolean showDialpad, boolean newOutgoingCall, boolean isForFullScreen) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, InCallActivity.class);
        if (showDialpad) {
            intent.putExtra(IntentExtraNames.SHOW_DIALPAD, true);
        }
        intent.putExtra(IntentExtraNames.NEW_OUTGOING_CALL, newOutgoingCall);
        intent.putExtra(IntentExtraNames.FOR_FULL_SCREEN, isForFullScreen);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inCallOrientationEventListener = new InCallOrientationEventListener(this);

        setWindowFlags();
    }

    private void setWindowFlags() {
        // Allow the activity to be shown when the screen is locked and filter out touch events that are
        // "too fat".
        int flags =
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;

        // When the audio stream is not via Bluetooth, turn on the screen once the activity is shown.
        // When the audio stream is via Bluetooth, turn on the screen only for an incoming call.
        final int audioRoute = getAudioRoute();
        if (audioRoute != CallAudioState.ROUTE_BLUETOOTH
                || CallList.getInstance().getIncomingCall() != null) {
            flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        }

        getWindow().addFlags(flags);
    }

    private int getAudioRoute() {
        // TODO
        return 0;
    }

    public void setExcludeFromRecents(boolean exclude) {
        int taskId = getTaskId();
        List<ActivityManager.AppTask> tasks = getSystemService(ActivityManager.class).getAppTasks();
        for (ActivityManager.AppTask task : tasks) {
            try {
                if (task.getTaskInfo().id == taskId) {
                    task.setExcludeFromRecents(exclude);
                }
            } catch (RuntimeException e) {
                LogUtil.e("InCallActivity.setExcludeFromRecents", "RuntimeException:\n%s", e);
            }
        }
    }

    public void showDialogForInternationalCallOnWifi(DialerCall call) {

    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean getCallCardFragmentVisible() {
        return didShowInCallScreen
                || didShowVideoCallScreen
                || didShowRttCallScreen
                || didShowSpeakEasyScreen;
    }

    public boolean isInCallScreenAnimating() {
        return false;
    }

    public void dismissKeyguard(boolean dismiss) {
        if (dismissKeyguard == dismiss) {
            return;
        }

        dismissKeyguard = dismiss;
        if (dismiss) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }

    public void dismissPendingDialogs() {
        LogUtil.enterBlock("InCallActivity.dismissPendingDialogs");

        if (!isVisible) {
            // Defer the dismissing action as the activity is not visible and onSaveInstanceState may have
            // been called.
            LogUtil.i(
                    "InCallActivity.dismissPendingDialogs", "defer actions since activity is not visible");
            needDismissPendingDialogs = true;
            return;
        }

        // Dismiss the error dialog
        if (errorDialog != null) {
            errorDialog.dismiss();
            errorDialog = null;
        }

        // Dismiss the phone account selection dialog
//        if (selectPhoneAccountDialogFragment != null) {
//            selectPhoneAccountDialogFragment.dismiss();
//            selectPhoneAccountDialogFragment = null;
//        }

        // Dismiss the dialog for international call on WiFi
        InternationalCallOnWifiDialogFragment internationalCallOnWifiFragment =
                (InternationalCallOnWifiDialogFragment)
                        getSupportFragmentManager().findFragmentByTag(Tags.INTERNATIONAL_CALL_ON_WIFI);
        if (internationalCallOnWifiFragment != null) {
            internationalCallOnWifiFragment.dismiss();
        }

        // Dismiss the answer screen
//        AnswerScreen answerScreen = getAnswerScreen();
//        if (answerScreen != null) {
//            answerScreen.dismissPendingDialogs();
//        }

        needDismissPendingDialogs = false;
    }


    public void onPrimaryCallStateChanged() {
        Trace.beginSection("InCallActivity.onPrimaryCallStateChanged");
        showMainInCallFragment();
        Trace.endSection();
    }

    private void showMainInCallFragment() {
        Trace.beginSection("InCallActivity.showMainInCallFragment");
        // If the activity's onStart method hasn't been called yet then defer doing any work.
        if (!isVisible) {
            LogUtil.i("InCallActivity.showMainInCallFragment", "not visible yet/anymore");
            Trace.endSection();
            return;
        }

        // Don't let this be reentrant.
        if (isInShowMainInCallFragment) {
            LogUtil.i("InCallActivity.showMainInCallFragment", "already in method, bailing");
            Trace.endSection();
            return;
        }

        isInShowMainInCallFragment = true;

        // Show view;

        isInShowMainInCallFragment = false;
        Trace.endSection();
    }

    private void enableInCallOrientationEventListener(boolean enable) {
        if (enable) {
            inCallOrientationEventListener.enable(true /* notifyDeviceOrientationChange */);
        } else {
            inCallOrientationEventListener.disable();
        }
    }

    public void setAllowOrientationChange(boolean allowOrientationChange) {
        if (this.allowOrientationChange == allowOrientationChange) {
            return;
        }
        this.allowOrientationChange = allowOrientationChange;
        if (!allowOrientationChange) {
            setRequestedOrientation(InCallOrientationEventListener.ACTIVITY_PREFERENCE_DISALLOW_ROTATION);
        } else {
            setRequestedOrientation(InCallOrientationEventListener.ACTIVITY_PREFERENCE_ALLOW_ROTATION);
        }
        enableInCallOrientationEventListener(allowOrientationChange);
    }

    public void showDialpadFragment(boolean show, boolean animate) {
        if (show == isDialpadVisible()) {
            return;
        }

        if (!animate) {
            if (show) {
                showDialpadFragment();
            } else {
                hideDialpadFragment();
            }
        } else {
            if (show) {
                showDialpadFragment();
                getDialpadFragment().animateShowDialpad();
            }
            getDialpadFragment()
                    .startAnimation(show ? dialpadSlideInAnimation : dialpadSlideOutAnimation);
        }

        ProximitySensor sensor = InCallPresenter.getInstance().getProximitySensor();
        if (sensor != null) {
            sensor.onDialpadVisible(show);
        }
    }

    private void showDialpadFragment() {
       // TODO
    }

    private void hideDialpadFragment() {
      // TODO
    }

    public boolean isDialpadVisible() {
        DialpadFragment dialpadFragment = getDialpadFragment();
        return dialpadFragment != null
                && dialpadFragment.isVisible();
    }

    /** Returns the {@link DialpadFragment} that's shown by this activity, or {@code null} */
    @Nullable
    private DialpadFragment getDialpadFragment() {
        // TODO
        return null;
    }

    public void hideMainInCallFragment() {
        LogUtil.enterBlock("InCallActivity.hideMainInCallFragment");
        if (getCallCardFragmentVisible()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            hideInCallScreenFragment(transaction);
//            hideVideoCallScreenFragment(transaction);
            transaction.commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    public void showDialogForRttRequest(DialerCall call, int rttRequestId) {

    }

    public void showDialogForPostCharWait(String callId, String chars) {
        PostCharDialogFragment fragment = new PostCharDialogFragment(callId, chars);
        fragment.show(getSupportFragmentManager(), Tags.POST_CHAR_DIALOG_FRAGMENT);
    }

    public void showDialogOrToastForDisconnectedCall(DisconnectMessage disconnectMessage) {
        LogUtil.i(
                "InCallActivity.showDialogOrToastForDisconnectedCall",
                "disconnect cause: %s",
                disconnectMessage);

        if (disconnectMessage.dialog == null || isFinishing()) {
            return;
        }

        dismissPendingDialogs();

        // Show a toast if the app is in background when a dialog can't be visible.
        if (!isVisible()) {
            Toast.makeText(getApplicationContext(), disconnectMessage.toastMessage, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Show the dialog.
        errorDialog = disconnectMessage.dialog;
        final InCallUiLock lock = InCallPresenter.getInstance().acquireInCallUiLock("showErrorDialog");
        disconnectMessage.dialog.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        lock.release();
                        InCallActivity.this.onDialogDismissed();
                    }
                });
        disconnectMessage.dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        disconnectMessage.dialog.show();
    }

    private void onDialogDismissed() {
        errorDialog = null;
        CallList.getInstance().onErrorDialogDismissed();
    }

}

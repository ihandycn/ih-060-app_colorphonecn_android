package com.honeycomb.colorphone.dialer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.telecom.CallAudioState;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.acb.call.service.InCallWindow;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.animation.AnimUtils;
import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.dialer.call.TelecomAdapter;
import com.honeycomb.colorphone.dialer.disconnectdialog.DisconnectMessage;
import com.honeycomb.colorphone.dialer.incalluilock.InCallUiLock;
import com.honeycomb.colorphone.dialer.util.ViewUtil;
import com.honeycomb.colorphone.telecomeventui.InternationalCallOnWifiDialogFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class InCallActivity extends AppCompatActivity implements PseudoScreenState.StateChangedListener {

    private ValueAnimator dialpadSlideInAnimation;
    private ValueAnimator dialpadSlideOutAnimation;
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

    private List<ViewManager> mViewManagers = new ArrayList<>();

    /**
     *
     */
    private boolean mIncomingCallUI;
    private DialpadFragment mDialpadFragment;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            DIALPAD_REQUEST_NONE,
            DIALPAD_REQUEST_SHOW,
            DIALPAD_REQUEST_HIDE,
    })
    @interface DialpadRequestType {}
    private static final int DIALPAD_REQUEST_NONE = 1;
    private static final int DIALPAD_REQUEST_SHOW = 2;
    private static final int DIALPAD_REQUEST_HIDE = 3;

    @DialpadRequestType private int showDialpadRequest = DIALPAD_REQUEST_NONE;

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
        static final String INCOMING_CALL = "InCallActivity.incoming_call";
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

    public static Intent getIncomingCallIntent(
            Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, InCallActivity.class);
        intent.putExtra(IntentExtraNames.SHOW_DIALPAD, false);
        intent.putExtra(IntentExtraNames.INCOMING_CALL, true);
        intent.putExtra(IntentExtraNames.FOR_FULL_SCREEN, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        inCallOrientationEventListener = new InCallOrientationEventListener(this);

        mIncomingCallUI = getIntent().getBooleanExtra(IntentExtraNames.INCOMING_CALL, false)
        || InCallPresenter.getInstance().getInCallState().isIncoming();

        if (!mIncomingCallUI) {
            ConfigEvent.dialerShow();
        }

        setWindowFlags();
        setContentView(R.layout.incall_screen);
        internalResolveIntent(getIntent());


        boolean isLandscape =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean isRtl = ViewUtil.isRtl();

//        if (isLandscape) {
//            dialpadSlideInAnimation =
//                    AnimationUtils.loadAnimation(
//                            this, isRtl ? R.anim.dialpad_slide_in_left : R.anim.dialpad_slide_in_right);
//            dialpadSlideOutAnimation =
//                    AnimationUtils.loadAnimation(
//                            this, isRtl ? R.anim.dialpad_slide_out_left : R.anim.dialpad_slide_out_right);
//        } else {
//            dialpadSlideInAnimation = AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_in_bottom);
//            dialpadSlideOutAnimation =
//                    AnimationUtils.loadAnimation(this, R.anim.dialpad_slide_out_bottom);
//        }

        dialpadSlideInAnimation = ValueAnimator.ofFloat(1, 0).setDuration(400);
        dialpadSlideOutAnimation = ValueAnimator.ofFloat(0, 1).setDuration(400);
        dialpadSlideInAnimation.setInterpolator(AnimUtils.EASE_IN);
        dialpadSlideOutAnimation.setInterpolator(AnimUtils.EASE_OUT);

        dialpadSlideOutAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                hideDialpadFragment();
            }
        });

        if (bundle != null && showDialpadRequest == DIALPAD_REQUEST_NONE) {
            // If the dialpad was shown before, set related variables so that it can be shown and
            // populated with the previous DTMF text during onResume().
            if (bundle.containsKey(IntentExtraNames.SHOW_DIALPAD)) {
                boolean showDialpad = bundle.getBoolean(IntentExtraNames.SHOW_DIALPAD);
                showDialpadRequest = showDialpad ? DIALPAD_REQUEST_SHOW : DIALPAD_REQUEST_HIDE;
                animateDialpadOnShow = false;
            }
            dtmfTextToPrepopulate = bundle.getString(KeysForSavedInstance.DIALPAD_TEXT);
//
//            SelectPhoneAccountDialogFragment selectPhoneAccountDialogFragment =
//                    (SelectPhoneAccountDialogFragment)
//                            getFragmentManager().findFragmentByTag(Tags.SELECT_ACCOUNT_FRAGMENT);
//            if (selectPhoneAccountDialogFragment != null) {
//                selectPhoneAccountDialogFragment.setListener(selectPhoneAccountListener);
//            }
        }

        inCallOrientationEventListener = new InCallOrientationEventListener(this);

        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        pseudoBlackScreenOverlay = findViewById(R.id.psuedo_black_screen_overlay);

//        sendBroadcast(CallPendingActivity.getFinishBroadcast());
    }
    @Override
    protected void onSaveInstanceState(Bundle out) {
        LogUtil.enterBlock("InCallActivity.onSaveInstanceState");

        out.putBoolean(IntentExtraNames.SHOW_DIALPAD, isDialpadVisible());
        DialpadFragment dialpadFragment = getDialpadFragment();
        if (dialpadFragment != null) {
            out.putString(KeysForSavedInstance.DIALPAD_TEXT, dialpadFragment.getDtmfText());
        }

        super.onSaveInstanceState(out);
        isVisible = false;
    }

    @Override
    protected void onStart() {
        Trace.beginSection("InCallActivity.onStart");
        super.onStart();

        isVisible = true;
        showMainInCallFragment();

        InCallPresenter.getInstance().setActivity(this);
        enableInCallOrientationEventListener(
                getRequestedOrientation()
                        == InCallOrientationEventListener.ACTIVITY_PREFERENCE_ALLOW_ROTATION);
        InCallPresenter.getInstance().onActivityStarted();

        if (!isRecreating) {
            InCallPresenter.getInstance().onUiShowing(true);
        }

        Trace.endSection();
    }

    @Override
    protected void onResume() {
        Trace.beginSection("InCallActivity.onResume");
        super.onResume();

        if (!InCallPresenter.getInstance().isReadyForTearDown()) {
            updateTaskDescription();
        }

        // If there is a pending request to show or hide the dialpad, handle that now.
        if (showDialpadRequest != DIALPAD_REQUEST_NONE) {
            if (showDialpadRequest == DIALPAD_REQUEST_SHOW) {
                // Exit fullscreen so that the user has access to the dialpad hide/show button.
                // This is important when showing the dialpad from within dialer.
                InCallPresenter.getInstance().setFullScreen(false /* isFullScreen */, true /* force */);

                showDialpadFragment(true /* show */, animateDialpadOnShow /* animate */);
                animateDialpadOnShow = false;

                DialpadFragment dialpadFragment = getDialpadFragment();
                if (dialpadFragment != null) {
                    dialpadFragment.setDtmfText(dtmfTextToPrepopulate);
                    dtmfTextToPrepopulate = null;
                }
            } else {
                LogUtil.i("InCallActivity.onResume", "Force-hide the dialpad");
                if (getDialpadFragment() != null) {
                    showDialpadFragment(false /* show */, false /* animate */);
                }
            }
            showDialpadRequest = DIALPAD_REQUEST_NONE;
        }

        CallList.getInstance()
                .onInCallUiShown(getIntent().getBooleanExtra(IntentExtraNames.FOR_FULL_SCREEN, false));

        PseudoScreenState pseudoScreenState = InCallPresenter.getInstance().getPseudoScreenState();
        pseudoScreenState.addListener(this);
        onPseudoScreenStateChanged(pseudoScreenState.isOn());
        Trace.endSection();

    }

    @Override
    protected void onPause() {
        Trace.beginSection("InCallActivity.onPause");
        super.onPause();

        DialpadFragment dialpadFragment = getDialpadFragment();
        if (dialpadFragment != null) {
            dialpadFragment.onDialerKeyUp(null);
        }

        InCallPresenter.getInstance().getPseudoScreenState().removeListener(this);
        Trace.endSection();
    }

    @Override
    protected void onStop() {
        Trace.beginSection("InCallActivity.onStop");
        isVisible = false;
        super.onStop();

        // Disconnects the call waiting for a phone account when the activity is hidden (e.g., after the
        // user presses the home button).
        // Without this the pending call will get stuck on phone account selection and new calls can't
        // be created.
        // Skip this when the screen is locked since the activity may complete its current life cycle
        // and restart.
        if (!isRecreating && !getSystemService(KeyguardManager.class).isKeyguardLocked()) {
            DialerCall waitingForAccountCall = CallList.getInstance().getWaitingForAccountCall();
            if (waitingForAccountCall != null) {
                waitingForAccountCall.disconnect();
            }
        }

        enableInCallOrientationEventListener(false);
        InCallPresenter.getInstance().updateIsChangingConfigurations();
        InCallPresenter.getInstance().onActivityStopped();
        if (!isRecreating) {
            InCallPresenter.getInstance().onUiShowing(false);
        }
        if (errorDialog != null) {
            errorDialog.dismiss();
        }

        if (isFinishing()) {
            InCallPresenter.getInstance().unsetActivity(this);
        }

        Trace.endSection();
    }

    @Override
    protected void onDestroy() {
        Trace.beginSection("InCallActivity.onDestroy");
        super.onDestroy();
        for (ViewManager manager : mViewManagers) {
            manager.onViewDestroy();
        }
        InCallPresenter.getInstance().unsetActivity(this);
        InCallPresenter.getInstance().updateIsChangingConfigurations();

        if (mDialpadFragment != null) {
            mDialpadFragment.onDestroyView();
        }

        Trace.endSection();
    }

    @Override
    public void finish() {
        if (shouldCloseActivityOnFinish()) {
            // When user select incall ui from recents after the call is disconnected, it tries to launch
            // a new InCallActivity but InCallPresenter is already teared down at this point, which causes
            // crash.
            // By calling finishAndRemoveTask() instead of finish() the task associated with
            // InCallActivity is cleared completely. So system won't try to create a new InCallActivity in
            // this case.
            //
            // Calling finish won't clear the task and normally when an activity finishes it shouldn't
            // clear the task since there could be parent activity in the same task that's still alive.
            // But InCallActivity is special since it's singleInstance which means it's root activity and
            // only instance of activity in the task. So it should be safe to also remove task when
            // finishing.
            // It's also necessary in the sense of it's excluded from recents. So whenever the activity
            // finishes, the task should also be removed since it doesn't make sense to go back to it in
            // anyway anymore.
            super.finishAndRemoveTask();
        }
    }

    @Override
    public void onPseudoScreenStateChanged(boolean isOn) {
        LogUtil.i("InCallActivity.onPseudoScreenStateChanged", "isOn: " + isOn);
        pseudoBlackScreenOverlay.setVisibility(isOn ? View.GONE : View.VISIBLE);
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

    private void internalResolveIntent(Intent intent) {
        if (!intent.getAction().equals(Intent.ACTION_MAIN)) {
            return;
        }

        if (intent.hasExtra(IntentExtraNames.SHOW_DIALPAD)) {
            // IntentExtraNames.SHOW_DIALPAD can be used to specify whether the DTMF dialpad should be
            // initially visible.  If the extra is absent, leave the dialpad in its previous state.
            boolean showDialpad = intent.getBooleanExtra(IntentExtraNames.SHOW_DIALPAD, false);
            relaunchedFromDialer(showDialpad);
        }

        DialerCall outgoingCall = CallList.getInstance().getOutgoingCall();
        if (outgoingCall == null) {
            outgoingCall = CallList.getInstance().getPendingOutgoingCall();
        }
        if (intent.getBooleanExtra(IntentExtraNames.NEW_OUTGOING_CALL, false)) {
            intent.removeExtra(IntentExtraNames.NEW_OUTGOING_CALL);

            // InCallActivity is responsible for disconnecting a new outgoing call if there is no way of
            // making it (i.e. no valid call capable accounts).
            if (InCallPresenter.isCallWithNoValidAccounts(outgoingCall)) {
                LogUtil.i(
                        "InCallActivity.internalResolveIntent", "Call with no valid accounts, disconnecting");
                outgoingCall.disconnect();
            }

            dismissKeyguard(true);
        }
//
//        if (showPhoneAccountSelectionDialog()) {
//            hideMainInCallFragment();
//        }
    }

    private boolean shouldCloseActivityOnFinish() {
        if (!isVisible) {
            LogUtil.i(
                    "InCallActivity.shouldCloseActivityOnFinish",
                    "allowing activity to be closed because it's not visible");
            return true;
        }

        if (InCallPresenter.getInstance().isInCallUiLocked()) {
            LogUtil.i(
                    "InCallActivity.shouldCloseActivityOnFinish",
                    "in call ui is locked, not closing activity");
            return false;
        }

        LogUtil.i(
                "InCallActivity.shouldCloseActivityOnFinish",
                "activity is visible and has no locks, allowing activity to close");
        return true;
    }

    private void updateTaskDescription() {
        int color = getSecondaryColor();
        setTaskDescription(
                new ActivityManager.TaskDescription(
                        getResources().getString(R.string.notification_ongoing_call), null /* icon */, color));
    }

    private int getSecondaryColor() {
        // TODO
        return 0;
    }

    /**
     * When relaunching from the dialer app, {@code showDialpad} indicates whether the dialpad should
     * be shown on launch.
     *
     * @param showDialpad {@code true} to indicate the dialpad should be shown on launch, and {@code
     *     false} to indicate no change should be made to the dialpad visibility.
     */
    private void relaunchedFromDialer(boolean showDialpad) {
        showDialpadRequest = showDialpad ? DIALPAD_REQUEST_SHOW : DIALPAD_REQUEST_NONE;
        animateDialpadOnShow = true;

        if (showDialpadRequest == DIALPAD_REQUEST_SHOW) {
            // If there's only one line in use, AND it's on hold, then we're sure the user
            // wants to use the dialpad toward the exact line, so un-hold the holding line.
            DialerCall call = CallList.getInstance().getActiveOrBackgroundCall();
            if (call != null && call.getState() == DialerCallState.ONHOLD) {
                call.unhold();
            }
        }
    }

    private int getAudioRoute() {
        return AudioModeProvider.getInstance().getAudioState().getRoute();
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
        showInCallScreenFragment();
        // Init after showInCallFragment
        isInShowMainInCallFragment = false;
        Trace.endSection();
    }

    private void createDialPadFragment() {
        if (mDialpadFragment == null) {
            mDialpadFragment = new DialpadFragment();
        }
        ViewGroup container = findViewById(R.id.incall_dialpad_container);
        mDialpadFragment.onCreateView(this, container);
    }

    private boolean showInCallScreenFragment() {
        if (didShowInCallScreen) {
            return false;
        }

        ViewGroup root = findViewById(R.id.main);

        if (mIncomingCallUI) {
            InCallWindow inCallWindow = new InCallWindow(this);
            View mainCallView = inCallWindow.getFlashRootView();
            mainCallView.setTag("incoming");
            root.addView(mainCallView);

            IncomingViewManager incomingViewManager = new IncomingViewManager();
            incomingViewManager.setInCallWindow(inCallWindow);
            incomingViewManager.onViewInit(this, mainCallView);
            mViewManagers.add(incomingViewManager);
        } else {
            View mainCallView = getLayoutInflater().inflate(R.layout.frag_incall_voice, root, false);
            mainCallView.setTag("main");
            root.addView(mainCallView);

            InCallButtonManager inCallButtonManager = new InCallButtonManager();
            inCallButtonManager.onViewInit(this, mainCallView);
            mViewManagers.add(inCallButtonManager);

            InCallCardManager inCallCardManager = new InCallCardManager();
            inCallCardManager.onViewInit(this, mainCallView);
            mViewManagers.add(inCallCardManager);

            createDialPadFragment();

        }

        didShowInCallScreen = true;
        return true;
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
        mDialpadFragment.attach();
        mDialpadFragment.onResume();
    }

    private void hideDialpadFragment() {
        mDialpadFragment.detach();
    }

    public boolean isDialpadVisible() {
        DialpadFragment dialpadFragment = getDialpadFragment();
        return dialpadFragment != null
                && dialpadFragment.isVisible();
    }

    /** Returns the {@link DialpadFragment} that's shown by this activity, or {@code null} */
    @Nullable
    private DialpadFragment getDialpadFragment() {
        return mDialpadFragment;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.enterBlock("InCallActivity.onNewIntent");

        // If the screen is off, we need to make sure it gets turned on for incoming calls.
        // This normally works just fine thanks to FLAG_TURN_SCREEN_ON but that only works
        // when the activity is first created. Therefore, to ensure the screen is turned on
        // for the call waiting case, we recreate() the current activity. There should be no jank from
        // this since the screen is already off and will remain so until our new activity is up.
        if (!isVisible || (mIncomingCallUI &&
                !InCallPresenter.getInstance().getInCallState().isIncoming())) {
            onNewIntent(intent, true /* isRecreating */);
            LogUtil.i("InCallActivity.onNewIntent", "Restarting InCallActivity to force screen on.");
            recreate();
        } else {
            onNewIntent(intent, false /* isRecreating */);
        }
    }

    @VisibleForTesting
    void onNewIntent(Intent intent, boolean isRecreating) {
        this.isRecreating = isRecreating;

        // We're being re-launched with a new Intent.  Since it's possible for a single InCallActivity
        // instance to persist indefinitely (even if we finish() ourselves), this sequence can
        // happen any time the InCallActivity needs to be displayed.

        // Stash away the new intent so that we can get it in the future by calling getIntent().
        // Otherwise getIntent() will return the original Intent from when we first got created.
        setIntent(intent);

        // Activities are always paused before receiving a new intent, so we can count on our onResume()
        // method being called next.

        // Just like in onCreate(), handle the intent.
        // Skip if InCallActivity is going to be recreated since this will be called in onCreate().
        if (!isRecreating) {
            internalResolveIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {
        LogUtil.enterBlock("InCallActivity.onBackPressed");

        if (!isVisible) {
            return;
        }

        if (!getCallCardFragmentVisible()) {
            return;
        }

        DialpadFragment dialpadFragment = getDialpadFragment();
        if (dialpadFragment != null && dialpadFragment.isVisible()) {
            showDialpadFragment(false /* show */, true /* animate */);
            return;
        }

        if (CallList.getInstance().getIncomingCall() != null) {
            LogUtil.i(
                    "InCallActivity.onBackPressed",
                    "Ignore the press of the back key when an incoming call is ringing");
            return;
        }

        // Nothing special to do. Fall back to the default behavior.
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.i("InCallActivity.onOptionsItemSelected", "item: " + item);
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        DialpadFragment dialpadFragment = getDialpadFragment();
        if (dialpadFragment != null
                && dialpadFragment.isVisible()
                && dialpadFragment.onDialerKeyUp(event)) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_CALL) {
            // Always consume KEYCODE_CALL to ensure the PhoneWindow won't do anything with it.
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL:
                if (!InCallPresenter.getInstance().handleCallKey()) {
                    LogUtil.e(
                            "InCallActivity.onKeyDown",
                            "InCallPresenter should always handle KEYCODE_CALL in onKeyDown");
                }
                // Always consume KEYCODE_CALL to ensure the PhoneWindow won't do anything with it.
                return true;

            // Note that KEYCODE_ENDCALL isn't handled here as the standard system-wide handling of it
            // is exactly what's needed, namely
            // (1) "hang up" if there's an active call, or
            // (2) "don't answer" if there's an incoming call.
            // (See PhoneWindowManager for implementation details.)

            case KeyEvent.KEYCODE_CAMERA:
                // Consume KEYCODE_CAMERA since it's easy to accidentally press the camera button.
                return true;

            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                // Ringer silencing handled by PhoneWindowManager.
                break;

            case KeyEvent.KEYCODE_MUTE:
                TelecomAdapter.getInstance()
                        .mute(!AudioModeProvider.getInstance().getAudioState().isMuted());
                return true;

            case KeyEvent.KEYCODE_SLASH:
                // When verbose logging is enabled, dump the view for debugging/testing purposes.
                if (LogUtil.isVerboseEnabled()) {
                    View decorView = getWindow().getDecorView();
                    LogUtil.v("InCallActivity.onKeyDown", "View dump:\n%s", decorView);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_EQUALS:
                break;

            default: // fall out
        }

        // Pass other key events to DialpadFragment's "onDialerKeyDown" method in case the user types
        // in DTMF (Dual-tone multi-frequency signaling) code.
        DialpadFragment dialpadFragment = getDialpadFragment();
        if (dialpadFragment != null
                && dialpadFragment.isVisible()
                && dialpadFragment.onDialerKeyDown(event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
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

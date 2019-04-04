package colorphone.acb.com.libweather.util;

import android.content.Intent;
import android.os.Build;

/**
 * On Android Oreo, a {@link Intent#ACTION_CLOSE_SYSTEM_DIALOGS} broadcast with reason extra
 * "homekey" is unexpectedly sent if the user presses back / recents button after first pressing
 * recents button. This class fixes this by neglecting the first home key event after any recents
 * key event.
 */
public class SystemKeyRecognizer {

    private final static String SYSTEM_DIALOG_REASON_KEY = "reason";
    private final static String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private final static String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private final static String SYSTEM_DIALOG_REASON_VOICE_INTERACTION = "voiceinteraction";
    private final static String SYSTEM_DIALOG_REASON_LOCK = "lock";
    private final static String SYSTEM_DIALOG_REASON_ASSIST = "assist";

    private boolean mLastKeyWasRecents;

    public void onBroadcast(Intent intent, Runnable home) {
        onBroadcast(intent, home, null);
    }

    public void onBroadcast(Intent intent, Runnable home, Runnable recents) {
        onBroadcast(intent, home, recents, null, null, null);
    }

    public void onBroadcast(Intent intent,
                            Runnable home, Runnable recents,
                            Runnable voiceInteraction, Runnable lock, Runnable assist) {
        if (intent != null && intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason == null) {
                return;
            }

            boolean homeKey = reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY);
            boolean recentsKey = reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS);
            boolean voiceInteractionKey = reason.equals(SYSTEM_DIALOG_REASON_VOICE_INTERACTION);
            boolean lockKey = reason.equals(SYSTEM_DIALOG_REASON_LOCK);
            boolean assistKey = reason.equals(SYSTEM_DIALOG_REASON_ASSIST);

            if (homeKey && (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !mLastKeyWasRecents)) {
                if (home != null) {
                    home.run();
                }
            }

            if (recentsKey) {
                mLastKeyWasRecents = true;
                if (recents != null) {
                    recents.run();
                }
            } else {
                mLastKeyWasRecents = false;
            }

            if (voiceInteractionKey && voiceInteraction != null) {
                voiceInteraction.run();
            }

            if (lockKey && lock != null) {
                lock.run();
            }

            if (assistKey && assist != null) {
                assist.run();
            }
        }
    }
}

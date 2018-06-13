package colorphone.acb.com.libscreencard;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;

/**
 * Created by sundxing on 17/9/5.
 */

public class CardCustomConfig {

    private static CardCustomConfig INSTANCE = new CardCustomConfig();
    private String mChargingExpressAdName;
    private String mSPFileName;
    private String mLockerAdName;
    private int mLauncherIcon;
    private RemoteLogger mRemoteLogger = new DefaultLogger();

    public static CardCustomConfig get() {
        return INSTANCE;
    }

    public RemoteLogger getRemoteLogger() {
        return mRemoteLogger;
    }

    public void setRemoteLogger(RemoteLogger remoteLogger) {
        mRemoteLogger = remoteLogger;
    }

    public static RemoteLogger getLogger() {
        return CardCustomConfig.get().getRemoteLogger();
    }

    public interface RemoteLogger {
        void logEvent(String eventID);
        void logEvent(String eventID, String... vars);
    }

    public static class DefaultLogger implements RemoteLogger {
        @Override
        public void logEvent(String eventID) {
            HSAnalytics.logEvent(eventID);
        }

        @Override
        public void logEvent(String eventID, String... vars) {
            HSAnalytics.logEvent(eventID, vars);
        }
    }

    public static void logAdViewEvent(String placementName, boolean success) {
        HSLog.d("ad analytics logAppViewEvent: " + placementName + " - " + success);
        getLogger().logEvent("Colorphone_AcbAdNative_Viewed_In_App", new String[]{placementName, String.valueOf(success)});
    }
}

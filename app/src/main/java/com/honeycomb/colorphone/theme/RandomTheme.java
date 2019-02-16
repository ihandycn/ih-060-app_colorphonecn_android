package com.honeycomb.colorphone.theme;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ColorPhoneCrashlytics;
import com.honeycomb.colorphone.util.NetUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class RandomTheme {
    private static final String TAG = RandomTheme.class.getSimpleName();

    private static final String PREFS_TIME = "prefs_random_theme_time";
    private static final String PREFS_THEME_INDEX = "prefs_theme_index";

    private static RandomTheme sRandomTheme = new RandomTheme();

    private final List<Theme> mRandomThemePool = new ArrayList<>();

    private boolean rollFlag = true;
    private boolean flashDisplayFlag;

    public static RandomTheme getInstance() {
        return sRandomTheme;
    }

    public Theme getRealTheme() {
        HSLog.d(TAG, "getRealTheme");
        flashDisplayFlag = true;
        boolean isIntervalValid = System.currentTimeMillis() - getLastThemeTime() > getConfigInterval();
        int curIndex = getCurThemeIndex(-1);
        if (isIntervalValid) {
            // try get next theme
            int themeIndex = curIndex < 0 ? 0  : calcNextThemeIndex();
            Theme nextTheme = getTheme(themeIndex);
            if (nextTheme != null
                    && isThemeReady(nextTheme)) {
                HSLog.d(TAG, "Next theme ready , index = " + themeIndex);
                rollFlag = true;
                return nextTheme;
            }
            // Next theme not ready
            HSLog.d(TAG, "Next theme not ready , index = " + themeIndex);
            return getLastUsableTheme();
        } else {
            HSLog.d(TAG, "Interval not valid");
            return getLastUsableTheme();
        }
    }

    private Theme getLastUsableTheme() {
        int cuIndex = getCurThemeIndex();
        for (int i = cuIndex; i >= 0; i--) {
            Theme theme = getTheme(i);
            if (theme != null && isThemeReady(theme)) {
                return theme;
            }
        }
        return null;
    }

    public int calcNextThemeIndex() {
        int curIndex = getCurThemeIndex(-1);
        return (curIndex + 1) % Math.max(mRandomThemePool.size(), 1);
    }

    private boolean isThemeReady(Theme nextTheme) {
        return !nextTheme.isMedia()
                || TasksManager.getImpl().isThemeDownloaded(nextTheme.getId());
    }

    public void prepareNextTheme() {
        int nextIndex = calcNextThemeIndex();

        prepareTheme(nextIndex);
        if (nextIndex == 0) {
            prepareTheme(1);
        }
    }

    private void prepareTheme(int pendingThemeIndex) {
        HSLog.d(TAG, "Prepare next theme start : " + pendingThemeIndex);

        Theme theme = getTheme(pendingThemeIndex);
        if (theme != null
                && theme.isMedia()) {
            // Need download it first
            TasksManagerModel model = TasksManager.getImpl().getByThemeId(theme.getId());
            if (model == null) {
                Analytics.logEvent("Test_Theme_Model_NULL", "Index", String.valueOf(pendingThemeIndex));
                return;
            }
            if (TasksManager.getImpl().isDownloaded(model)) {
                HSLog.d(TAG, "prepareTheme next success , file already downloaded : " + pendingThemeIndex);
                return;
            }

            // Check wifi state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                HSLog.d(TAG, "Start theme download job : index " + pendingThemeIndex);
                try {
                    startDownloadJob();
                } catch (Exception e) {
                    ColorPhoneCrashlytics.getInstance().logException(e);
                }
            } else if (NetUtils.isWifiConnected(HSApplication.getContext())) {
                downloadMediaTheme(pendingThemeIndex, model, null);
            }
        } else {
            HSLog.d(TAG, "prepareTheme next success , native theme : " + pendingThemeIndex);
        }
    }

    public void downloadMediaTheme(int pendingThemeIndex, TasksManagerModel model, final DownloadStateListener delegateListener) {
        boolean downloadStart = TasksManager.doDownload(model, null);
        if (downloadStart) {
            Ap.RandomTheme.logEvent("random_theme_download_start");
            Analytics.logEvent("clorphone_random_theme_download_start");
        }
        final int taskId = model.getId();
        FileDownloadMultiListener.getDefault().addStateListener(taskId, new DownloadStateListener() {

            @Override
            public void updateDownloaded(boolean progressFlag) {
                // In case method call more than once.
                FileDownloadMultiListener.getDefault().removeStateListener(taskId);
                Ap.RandomTheme.logEvent("random_theme_download_success");
                Analytics.logEvent("colorphone_random_theme_download_success");

                if (delegateListener != null) {
                    delegateListener.updateDownloaded(progressFlag);
                }
                HSLog.d(TAG, "prepareTheme next success , file downloaded : " + pendingThemeIndex);
            }

            @Override
            public void updateNotDownloaded(int status, long sofar, long total) {
                FileDownloadMultiListener.getDefault().removeStateListener(taskId);
                if (delegateListener != null) {
                    delegateListener.updateNotDownloaded(status, sofar, total);
                }
                HSLog.d(TAG, "prepareTheme next fail , file not downloaded : " + pendingThemeIndex);
            }

            @Override
            public void updateDownloading(int status, long sofar, long total) {
                if (delegateListener != null) {
                    delegateListener.updateDownloading(status, sofar, total);
                }
            }
        });
    }

    public Theme getTheme(int themeIndex) {
        if (mRandomThemePool.isEmpty()) {
            List<String> randomNames = (List<String>) HSConfig.getList("Application", "Theme", "RandomTheme");
            for (String themeName : randomNames) {
                mRandomThemePool.add(getThemeByName(themeName));
            }
        }

        if (mRandomThemePool.isEmpty()) {
            return null;
        }

        if (themeIndex >= mRandomThemePool.size()) {
            return mRandomThemePool.get(0);
        }

        return mRandomThemePool.get(themeIndex);
    }

    private Theme getThemeByName(String idName) {
        for (Theme theme : Theme.themes()) {
            if (theme.getIdName().equals(idName)) {
                return theme;
            }
        }
        return null;
    }

    public int getCurThemeIndex() {
        return getCurThemeIndex(0);
    }

    public void roll() {
        if (rollFlag) {
            rollFlag = false;
            updateThemeTime();
            updateThemeIndex(calcNextThemeIndex());
        }
        prepareNextTheme();
    }

    public void updateThemeIndex(int index) {
        HSLog.d(TAG, "Update index : " + index);
        Preferences.get(Constants.PREF_FILE_DEFAULT).putInt(PREFS_THEME_INDEX, index);
    }

    private  int getCurThemeIndex(int defaultIndex) {
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getInt(PREFS_THEME_INDEX, defaultIndex);
    }

    private long getConfigInterval() {
        return Ap.RandomTheme.intervalHour() * DateUtils.HOUR_IN_MILLIS;
    }

    private long getLastThemeTime() {
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getLong(PREFS_TIME, 0);
    }

    private void updateThemeTime() {
        Preferences.get(Constants.PREF_FILE_DEFAULT).putLong(PREFS_TIME, System.currentTimeMillis());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startDownloadJob() {
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putInt(ThemeDownloadJobService.KEY_TYPE, ThemeDownloadJobService.TYPE_RANDOM_THEME);
        JobInfo jobInfo = new JobInfo.Builder(1000,
                new ComponentName(HSApplication.getContext(), ThemeDownloadJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setBackoffCriteria(5 * DateUtils.MINUTE_IN_MILLIS, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setExtras(persistableBundle)
                .build();

        JobScheduler jobScheduler = (JobScheduler) HSApplication.getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            int resultCode = jobScheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                HSLog.d(TAG, "Job scheduled!");
            } else {
                HSLog.d(TAG, "Job not scheduled");
            }
        }
    }

    public void onFlashShow(String themeId) {
        if (flashDisplayFlag) {
            flashDisplayFlag = false;
            HSLog.d(TAG, "onFlashShow");
            int id = Integer.valueOf(themeId);
            Theme targetTheme = null;
            for (Theme theme : mRandomThemePool) {
                if (theme.getId() == id) {
                    targetTheme = theme;
                    break;
                }
            }
            Preferences.get(Constants.PREF_FILE_DEFAULT).doOnce(new Runnable() {
                @Override
                public void run() {
                    Ap.RandomTheme.logEvent("random_theme_enabled");
                    Analytics.logEvent("colorphone_random_theme_enabled");
                }
            }, "colorphone_random_theme_enabled");
            Ap.RandomTheme.logEvent("random_theme_show");
            Analytics.logEvent("colorphone_random_theme_show",
                    "IdName", targetTheme == null ? "NULL" : targetTheme.getIdName(),
                    "Network", NetUtils.isWifiConnected(HSApplication.getContext()) ? "Wifi" : "Data");
        }
    }
}

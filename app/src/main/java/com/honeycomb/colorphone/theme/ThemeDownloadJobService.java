package com.honeycomb.colorphone.theme;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.text.format.DateUtils;
import android.util.Log;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ThemeDownloadJobService extends JobService {

    public static final String KEY_TYPE = "type";
    public static final String KEY_THEME_ID = "theme_id";
    public static final String KEY_TASK_ID = "task_id";

    public static final int TYPE_NORAL_THEME = 1;
    public static final int TYPE_RANDOM_THEME = 2;

    private static final String TAG = ThemeDownloadJobService.class.getSimpleName();
    boolean isWorking = false;
    boolean jobCancelled = false;
    private int type;
    private int themeId;

    public static void scheduleDownloadJob(int modelId) {
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putInt(KEY_TYPE, TYPE_NORAL_THEME);
        persistableBundle.putInt(KEY_TASK_ID, modelId);
        JobInfo jobInfo = new JobInfo.Builder(modelId,
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

    public static void scheduleDownloadJobAnyNet(int modelId) {
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putInt(KEY_TYPE, TYPE_NORAL_THEME);
        persistableBundle.putInt(KEY_TASK_ID, modelId);
        JobInfo jobInfo = new JobInfo.Builder(modelId,
                new ComponentName(HSApplication.getContext(), ThemeDownloadJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
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

    private JobParameters mjobParameters;
    final Runnable pendingWorkRunnable = new Runnable() {
        @Override
        public void run() {
            if (type == TYPE_RANDOM_THEME) {
                doRandomWork(mjobParameters);
            } else if (type == TYPE_NORAL_THEME) {
                doDownloadTask(mjobParameters);
            }
        }
    };

    private void doDownloadTask(JobParameters jobParameters) {
        int taskId = jobParameters.getExtras().getInt(KEY_TASK_ID);
        HSLog.d(TAG, "schedule download task : " + taskId);
        if (taskId != 0) {
            TasksManagerModel model = TasksManager.getImpl().getById(taskId);
            if (model == null) {
                if (TasksManager.getImpl().isLoading()) {
                    TasksManager.getImpl().setTaskReadyCallback(pendingWorkRunnable);
                } else {
                    onJobFinish(jobParameters, true);
                }
                return;
            }

            if (TasksManager.getImpl().isDownloaded(model)) {
                onJobFinish(jobParameters, false);
                return;
            }

            LauncherAnalytics.logEvent("Test_Job_Download_Start", LauncherAnalytics.FLAG_LOG_FABRIC);
            ThemeRecommendManager.logThemeRecommendThemeDownloadStart();
            TasksManager.doDownload(model, null);

            FileDownloadMultiListener.getDefault().addStateListener(taskId, new DownloadStateListener() {

                @Override
                public void updateDownloaded(boolean progressFlag) {
                    HSLog.d(TAG, "download normal task success: "+ model.getName());
                    LauncherAnalytics.logEvent("Test_Job_Download_Success", LauncherAnalytics.FLAG_LOG_FABRIC);
                    onJobFinish(jobParameters, false);
                    ThemeRecommendManager.logThemeRecommendThemeDownloadSuccess();
                }

                @Override
                public void updateNotDownloaded(int status, long sofar, long total) {
                    HSLog.d(TAG, "download normal task fail: "+ model.getName());
                    onJobFinish(jobParameters, true);
                    ThemeRecommendManager.logThemeRecommendThemeDownloadFail();
                }

                @Override
                public void updateDownloading(int status, long sofar, long total) {

                }
            });
        } else {
            HSLog.d(TAG, "schedule NOT download task : " + taskId);
        }
    }

    // Called by the Android system when it's time to run the job
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Job started!");
        type = jobParameters.getExtras().getInt(KEY_TYPE);
        themeId = jobParameters.getExtras().getInt(KEY_THEME_ID);
        isWorking = true;
        mjobParameters = jobParameters;
        Threads.postOnMainThreadDelayed(pendingWorkRunnable, 3000);
        return isWorking;
    }

    private void doRandomWork(JobParameters jobParameters) {
        if (jobCancelled)
            return;

        final int pendingThemeIndex = RandomTheme.getInstance().calcNextThemeIndex();
        Theme theme = RandomTheme.getInstance().getTheme(pendingThemeIndex);

        if (theme == null) {
            onJobFinish(jobParameters, false);
            HSLog.e(TAG, "download fail! Theme is null");
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("download fail! Theme is null");
            }
            return;
        }

        TasksManagerModel model = TasksManager.getImpl().getByThemeId(theme.getId());
        if (model == null) {
            if (TasksManager.getImpl().isLoading()) {
                TasksManager.getImpl().setTaskReadyCallback(pendingWorkRunnable);
            } else {
                onJobFinish(jobParameters, true);
                return;
            }
        }
        if (TasksManager.getImpl().isDownloaded(model)) {
            HSLog.d(TAG, "Roll next success , file already downloaded : " + pendingThemeIndex);
            onJobFinish(jobParameters, false);
            return;
        }

        RandomTheme.getInstance().downloadMediaTheme(pendingThemeIndex, model, new DownloadStateListener() {
            @Override
            public void updateDownloaded(boolean progressFlag) {
                onJobFinish(jobParameters, false);
            }

            @Override
            public void updateNotDownloaded(int status, long sofar, long total) {
                onJobFinish(jobParameters, true);
            }

            @Override
            public void updateDownloading(int status, long sofar, long total) {

            }
        });
    }

    private void onJobFinish(JobParameters jobParameters, boolean needsReschedule) {
        Log.d(TAG, "Job finished!");
        isWorking = false;
        jobFinished(jobParameters, needsReschedule);

        TasksManager.getImpl().setTaskReadyCallback(null);
    }

    // Called if the job was cancelled before being finished
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        jobFinished(jobParameters, needsReschedule);

        TasksManager.getImpl().setTaskReadyCallback(null);

        return needsReschedule;
    }
}
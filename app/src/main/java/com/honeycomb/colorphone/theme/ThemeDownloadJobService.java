package com.honeycomb.colorphone.theme;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.DownloadStateListener;
import com.honeycomb.colorphone.download.FileDownloadMultiListener;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.ihs.commons.utils.HSLog;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ThemeDownloadJobService extends JobService {
   private static final String TAG = ThemeDownloadJobService.class.getSimpleName();
   boolean isWorking = false;
   boolean jobCancelled = false;

   // Called by the Android system when it's time to run the job
   @Override
   public boolean onStartJob(JobParameters jobParameters) {
       Log.d(TAG, "Job started!");
       isWorking = true;
       doWork(jobParameters);
       return isWorking;
   }

    private void doWork(JobParameters jobParameters) {
       if (jobCancelled)
           return;

       final int pendingThemeIndex = RandomTheme.getInstance().calcNextThemeIndex();
       Theme theme = RandomTheme.getInstance().getTheme(pendingThemeIndex);

       if (theme == null) {
           HSLog.d(TAG, "download fail! Theme is null");
           return;
       }

       TasksManagerModel model = TasksManager.getImpl().getByThemeId(theme.getId());
       if (TasksManager.getImpl().isDownloaded(model)) {
           HSLog.d(TAG, "Roll next success , file already downloaded : " + pendingThemeIndex);
           onJobFinish(jobParameters, false);
           return;
       }

       // Check wifi state
       TasksManager.doDownload(model, null);
       FileDownloadMultiListener.getDefault().addStateListener(model.getId(), new DownloadStateListener() {

           @Override
           public void updateDownloaded(boolean progressFlag) {
               // In case method call more than once.
               HSLog.d(TAG, "Roll next success , file downloaded : " + pendingThemeIndex);
               RandomTheme.getInstance().updateThemeIndex(pendingThemeIndex);
               onJobFinish(jobParameters, false);
           }

           @Override
           public void updateNotDownloaded(int status, long sofar, long total) {
               HSLog.d(TAG, "Roll next fail , file not downloaded : " + pendingThemeIndex);
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
    }

    // Called if the job was cancelled before being finished
   @Override
   public boolean onStopJob(JobParameters jobParameters) {
       Log.d(TAG, "Job cancelled before being completed.");
       jobCancelled = true;
       boolean needsReschedule = isWorking;
       jobFinished(jobParameters, needsReschedule);
       return needsReschedule;
   }
}
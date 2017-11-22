package com.honeycomb.colorphone.notification;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ThemeNotificationJobService extends JobService {

    @Override
    public void onCreate() {
        super.onCreate();
        startJobScheduler();
    }

    public void startJobScheduler() {
        try {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), ThemeNotificationJobService.class.getName()));
            builder.setMinimumLatency(DateUtils.DAY_IN_MILLIS);

            JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override public boolean onStartJob(JobParameters params) {

        return false;
    }

    @Override public boolean onStopJob(JobParameters params) {
        return false;
    }

}

package com.honeycomb.colorphone;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.honeycomb.colorphone.util.HSPermanentUtils;

/**
 * Created by zhewang on 22/02/2017.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LockJobService extends JobService {

    @Override
    public void onCreate() {
        super.onCreate();
        startJobScheduler();
    }

    public void startJobScheduler() {
        try {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), LockJobService.class.getName()));
            builder.setPeriodic(60 * 1000);
            JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override public boolean onStartJob(JobParameters params) {
        HSPermanentUtils.checkAliveForProcess();
        return false;
    }

    @Override public boolean onStopJob(JobParameters params) {
        return false;
    }
}

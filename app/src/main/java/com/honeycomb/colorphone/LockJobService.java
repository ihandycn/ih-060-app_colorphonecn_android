package com.honeycomb.colorphone;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;

import com.call.assistant.util.CommonUtils;
import com.honeycomb.colorphone.util.ColorPhonePermanentUtils;
import com.ihs.app.framework.HSApplication;

/**
 * Created by zhewang on 22/02/2017.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LockJobService extends JobService {

    public static void startJobScheduler() {
        try {
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(HSApplication.getContext().getPackageName(), LockJobService.class.getName()));
            builder.setPeriodic((CommonUtils.ATLEAST_MARSHMALLOW ? 1 : 8) * DateUtils.HOUR_IN_MILLIS);
            JobScheduler jobScheduler = (JobScheduler) HSApplication.getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        HSApplication.setContext(this);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}

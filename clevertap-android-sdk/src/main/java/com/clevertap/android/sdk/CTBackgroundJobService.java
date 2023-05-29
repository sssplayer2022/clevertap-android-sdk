package com.clevertap.android.sdk;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import androidx.annotation.RequiresApi;

import static com.clevertap.android.sdk.extras.CleverTapExecutors.io;

/**
 * Background Job service to sync up for new notifications
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CTBackgroundJobService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters params) {
        Logger.v("Job Service is starting");
        io().execute(new Runnable() {
            @Override
            public void run() {
                CleverTapAPI.runJobWork(getApplicationContext(),params);
                jobFinished(params,true);
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; //to ensure reschedule
    }

}

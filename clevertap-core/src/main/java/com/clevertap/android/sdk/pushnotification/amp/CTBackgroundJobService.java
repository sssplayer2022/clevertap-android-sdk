package com.clevertap.android.sdk.pushnotification.amp;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.Logger;

/**
 * Background Job service to sync up for new notifications
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CTBackgroundJobService extends JobService {

    @Override
    public boolean onStartJob(final JobParameters params) {
        Logger.v("Job Service is starting");
        new Thread(new Runnable() {
            @Override
            public void run() {
                CleverTapAPI.runJobWork(getApplicationContext(), params);
                jobFinished(params, true);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; //to ensure reschedule
    }

}

package ceid.katefidis.calchas;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class PostRemainingService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        MobileArrayAdapter mobileArrayAdapter = new MobileArrayAdapter(getApplicationContext());
        mobileArrayAdapter.postRemaining();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}
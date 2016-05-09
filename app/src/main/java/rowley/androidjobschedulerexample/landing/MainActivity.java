package rowley.androidjobschedulerexample.landing;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rowley.androidjobschedulerexample.R;
import rowley.androidjobschedulerexample.scheduled.alarm.ServiceFromAlarm;
import rowley.androidjobschedulerexample.scheduled.scheduler.DataSyncJobService;
import rowley.androidjobschedulerexample.scheduled.simple.ServiceWithHandler;

/**
 * The main (and only, really) UI for this example. We present a few options for scheduling a task some time in the future
 * and handle the user's choice.
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

    private final int JOB_ID = 1;

    @Bind(R.id.label)
    TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        label.setText(String.format(
                getString(R.string.main_screen_message_formatted), getResources().getInteger(R.integer.example_delay_seconds)));
    }

    @OnClick(R.id.handlerButton)
    void onHandlerButtonClick() {
        startService(new Intent(this, ServiceWithHandler.class));
        finish();
    }

    @OnClick(R.id.alarmButton)
    void onAlarmButtonClick() {
        setupAlarmService();
    }

    @OnClick(R.id.jobSchedulerButton)
    void onJobSchedulerButtonClick() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupJobScheduler();
        } else {
            setupAlarmService();
        }
    }

    private void setupAlarmService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, new Intent(this, ServiceFromAlarm.class), PendingIntent.FLAG_CANCEL_CURRENT);

        long triggerTime = System.currentTimeMillis() + (getResources().getInteger(R.integer.example_delay_seconds) * 1000L);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupJobScheduler() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);

        //Some housekeeping to reset things. The choice to use SharedPreferences is discussed in DataSyncJobService.
        //I don't think it's the best choice, but it's a choice that works.
        getSharedPreferences(
                DataSyncJobService.SHARED_PREFS_KEY, MODE_PRIVATE).edit().
                putInt(DataSyncJobService.FILES_COMPLETED_BUNDLE_KEY, 0).apply();

        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(this, DataSyncJobService.class));
        jobBuilder.setMinimumLatency(getResources().getInteger(R.integer.example_delay_seconds) * 1000L);
        jobBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        ////////////////////////////////////////////////
        // Other options we have to configure our job //
        ////////////////////////////////////////////////

        //Require the device to be idle because we're going to use some resources and don't want to get in the way of the user
        //This can delay your job significantly, so use it if the timing is not critical
        //jobBuilder.setRequiresDeviceIdle(true);

        //If we need to implement a retry-and-die strategy for this job, we no longer need to handle the retries and especially
        //the increasing retry interval. We let the JobBuilder and the JobService framework handle all of that for us.
        //NOTE: Using this method in conjunction with #setRequiresDeviceIdle will result in an exception.
        //jobBuilder.setBackoffCriteria(
        //        getResources().getInteger(R.integer.example_delay_seconds) * 1000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        //If we want to ensure that our job is run by the time a MAXIMUM time has elapsed, regardless of other preconditions,
        //then we can set this value
        //jobBuilder.setOverrideDeadline(getResources().getInteger(R.integer.example_delay_seconds) * 1000L * 10);

        //Set this job to run periodically on a given schedule. This method will result in an error if used in conjunction with
        //setMinimumLatency or setOverrideDeadline
        //jobBuilder.setPeriodic(1000L);

        //We need this job to persist across device restarts. This method requires the RECEIVE_BOOT_COMPLETED permission, but
        //will ensure that we do not lose the opportunity to do important work if the device runs out of battery
        //jobBuilder.setPersisted(true);

        //Pretty straight-forward
        //jobBuilder.setRequiresCharging(true);

        //Provide additional data to the service with a PersistableBundle. This is a new type of Bundle and is more limited in
        //what it can hold in its values
        //PersistableBundle extras = new PersistableBundle(2);
        //extras.putInt("someKey", 42);
        //extras.putString("someOtherKey", "A Value");
        //jobBuilder.setExtras(extras);

        ////////////////////////////////////////////////
        // Other options we have to configure our job //
        ////////////////////////////////////////////////

        jobScheduler.schedule(jobBuilder.build());
        finish();
    }
}

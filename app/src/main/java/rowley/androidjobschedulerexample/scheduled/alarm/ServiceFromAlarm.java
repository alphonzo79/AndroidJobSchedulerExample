package rowley.androidjobschedulerexample.scheduled.alarm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import rowley.androidjobschedulerexample.R;

/**
 * The ServiceFromAlarm class demonstrates how we could use an {@link IntentService} to handle some background processing that
 * was previously scheduled to run at some time. In this case the delay has already been handled and we need to just move on with
 * doing the work. This is preferable to the example in
 * {@link rowley.androidjobschedulerexample.scheduled.simple.ServiceWithHandler} for several reasons: It adheres to the Single
 * Purpose principle of software development by not handling the delay itself. It allows us to launch the task even if the app
 * has been destroyed in the mean time. And it avoids keeping the application alive and consuming resources needlessly in the
 * interim. This is a good, clean method of handling a future task as it doesn't require much infrastructure.
 */
public class ServiceFromAlarm extends IntentService {
    public static final String TAG = ServiceFromAlarm.class.getSimpleName();

    private Handler handler;

    public ServiceFromAlarm() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ServiceFromAlarm.this, R.string.starting_task_message, Toast.LENGTH_LONG).show();
            }
        });

        //This job will run for about 5 seconds. If the application get destroyed while it's going on we don't really have a good
        //way to mark our progress and reschedule a retry without doing quite a bit of work inside onDestroy -- essentially
        //duplicating all the work we did in the Main Activity to get the AlarmService and setup the PendingIntent. Also, if we
        //lose a resource like network connectivity, then we have the same problem. We need to put code in place to check
        //the network, etc and handle various scenarios.
        int bigFilesDownloaded = 0;
        while(bigFilesDownloaded < 50) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, String.format(getString(R.string.big_files_downloaded_formatted), ++bigFilesDownloaded));
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ServiceFromAlarm.this, R.string.finished_task_message, Toast.LENGTH_LONG).show();
            }
        });
    }
}

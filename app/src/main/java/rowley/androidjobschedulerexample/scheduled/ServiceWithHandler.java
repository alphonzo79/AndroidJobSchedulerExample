package rowley.androidjobschedulerexample.scheduled;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import rowley.androidjobschedulerexample.R;

/**
 * The ServiceWithHandler class demonstrates an unsophisticated way of delaying a task. It extends IntentService, so we'll do
 * the delay on a background thread, which is good. One option for handling the delay might be to simply
 * {@link Thread#sleep(long)} and then post to a {@link android.os.Handler} to do the MainThread UI work. Another option
 * would be to immediately post to a {@link android.os.Handler} with a delay. Either method works, but keeps the application
 * alive (and using resources); also, if the app does crash or get killed by Android we lose our "scheduled" task. This is the
 * worst of the three examples in this sample app.
 */
public class ServiceWithHandler extends IntentService {
    private static final String TAG = ServiceWithHandler.class.getSimpleName();

    private Handler handler;

    /**
     * Creates an IntentService that will pop a toast to the user after the predefined period of time.
     */
    public ServiceWithHandler() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long delayMillis = getResources().getInteger(R.integer.example_delay_seconds) * 1000L;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ServiceWithHandler.this, R.string.starting_task_message, Toast.LENGTH_LONG).show();
            }
        }, delayMillis);
    }
}

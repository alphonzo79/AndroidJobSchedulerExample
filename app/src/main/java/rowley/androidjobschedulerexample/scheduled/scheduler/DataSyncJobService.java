package rowley.androidjobschedulerexample.scheduled.scheduler;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import rowley.androidjobschedulerexample.R;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * This class demonstrates the ability to easily handle and retry long-running tasks using the JobScheduler. All of the setup
 * work is done when the job is created in the MainActivity. This class's only concern is completing the work, or letting the
 * system know that it was unable to complete the work. It keeps things focused, simple and clean. The only catch to this method
 * is that, unlike extending an IntentService, we must manage our own thread work here. We simplify this by using RxJava.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DataSyncJobService extends JobService {
    private final String TAG = DataSyncJobService.class.getSimpleName();
    public static final String SHARED_PREFS_KEY = "default";
    public static final String FILES_COMPLETED_BUNDLE_KEY = "filesCompleted";
    private final int TOTAL_FILES = 50;
    private long INTERVAL_TIME = 100;
    private TimeUnit INTERVAL_UNIT = TimeUnit.MILLISECONDS;

    private volatile int completedFiles;
    private volatile boolean hasBeenCanceled = false;
    private Subscription subscription;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Toast.makeText(DataSyncJobService.this, R.string.starting_task_message, Toast.LENGTH_LONG).show();

        //NOTE I tried using params.getExtras().getInt(...) to return either the default of 0 or a value that I saved to the
        //bundle on a previous attempt, but it seems that any writes I make to the params.getExtras() bundle do not persist
        //across attempts. So, if we got cut short in a previous attempt we do not know where we left off. We have two options,
        //either just start again from the beginning, or find another way to manage half-done work. For the sake of simplicity
        //I'm just using a shared pref to stash that value. Probably not the best way, but it's a way...
        completedFiles = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE).getInt(FILES_COMPLETED_BUNDLE_KEY, 0);

        subscription = Observable.interval(INTERVAL_TIME, INTERVAL_UNIT).takeUntil(new Func1<Long, Boolean>() {
            @Override
            public Boolean call(Long aLong) {
                return completedFiles++ == TOTAL_FILES || hasBeenCanceled;
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Long>() {
            @Override
            public void onCompleted() {
                Toast.makeText(DataSyncJobService.this, R.string.finished_task_message, Toast.LENGTH_LONG).show();
                //Let Android know that we are no longer working in the background, and that we do not need to reschedule the job
                jobFinished(params, false);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                //Let Android know that we are no longer working in the background, but that we DO need to reschedule the job
                jobFinished(params, true);
            }

            @Override
            public void onNext(Long aLong) {
                Log.d(TAG, String.format(getString(R.string.big_files_downloaded_formatted), completedFiles));
            }
        });

        //If the completedFiles count is less than TOTAL_FILES then we have some work to do on a background thread. This method
        //runs synchronously on the app's MainThread, so we want to move our work to the background and tell Android that we are
        //continuing work even though this method has returned.
        return completedFiles < TOTAL_FILES;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        hasBeenCanceled = true;
        if(subscription != null) {
            subscription.unsubscribe();
        }
        getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE).edit().putInt(FILES_COMPLETED_BUNDLE_KEY, completedFiles).apply();

        //We were instructed to stop this job for some reason. Android wants to know whether we still have work to do, and it
        //it should therefore reschedule the job according to our retry criteria and/or when the conditions are again met.
        //Do we still have work to do?
        return completedFiles < TOTAL_FILES;
    }
}

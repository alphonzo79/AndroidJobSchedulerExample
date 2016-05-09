package rowley.androidjobschedulerexample.landing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rowley.androidjobschedulerexample.R;
import rowley.androidjobschedulerexample.scheduled.alarm.ServiceFromAlarm;
import rowley.androidjobschedulerexample.scheduled.simple.ServiceWithHandler;

/**
 * The main (and only, really) UI for this example. We present a few options for scheduling a task some time in the future
 * and handle the user's choice.
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();

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
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getService(this, 0, new Intent(this, ServiceFromAlarm.class), PendingIntent.FLAG_CANCEL_CURRENT);

        long triggerTime = System.currentTimeMillis() + (getResources().getInteger(R.integer.example_delay_seconds) * 1000L);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        finish();
    }

    @OnClick(R.id.jobSchedulerButton)
    void onJobSchedulerButtonClick() {
        // TODO: 5/9/16
    }
}

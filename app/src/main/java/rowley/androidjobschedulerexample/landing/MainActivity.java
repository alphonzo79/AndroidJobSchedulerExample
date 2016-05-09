package rowley.androidjobschedulerexample.landing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rowley.androidjobschedulerexample.R;
import rowley.androidjobschedulerexample.scheduled.ServiceWithHandler;

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
        // TODO: 5/9/16
    }

    @OnClick(R.id.jobSchedulerButton)
    void onJobSchedulerButtonClick() {
        // TODO: 5/9/16
    }
}

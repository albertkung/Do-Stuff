package albertkung.tsma;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Objects;

public class AddActivity extends AppCompatActivity {

    private static final String[] DAYS = { "sun", "mon", "tue", "wed", "thu", "fri", "sat" };
    private static final String[] MONTHS = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        TabHost.TabSpec spec = host.newTabSpec("Task");
        spec.setContent(R.id.task_tab);
        spec.setIndicator("Task");
        host.addTab(spec);

        spec = host.newTabSpec("Project");
        spec.setContent(R.id.assignment_tab);
        spec.setIndicator("Project");
        host.addTab(spec);

        spec = host.newTabSpec("Event");
        spec.setContent(R.id.event_tab);
        spec.setIndicator("Event");
        host.addTab(spec);

        spec = host.newTabSpec("Other");
        spec.setContent(R.id.other_tab);
        spec.setIndicator("Other");
        host.addTab(spec);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void AddTask(View view) {
        String name = ((EditText) findViewById(R.id.edit_name)).getText().toString();
        String date = ((EditText) findViewById(R.id.edit_date)).getText().toString();
        String time = ((EditText) findViewById(R.id.edit_time)).getText().toString();
        String details = ((EditText) findViewById(R.id.edit_details)).getText().toString();

        if (name.isEmpty()) {
            Toast toast = Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Calendar my_date = parseDate(date.trim().toLowerCase());
            if (my_date == null) {
                Toast toast = Toast.makeText(this, "Enter valid date", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            my_date = parseTime(time.trim().toLowerCase(), my_date);
            if (my_date == null) {
                Toast toast = Toast.makeText(this, "Enter valid time", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            Task my_task = new Task(name, details, my_date);
            final Intent data = new Intent();
            data.putExtra("task", my_task);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    private Calendar parseTime(String time, Calendar date) {
        if (time.matches(".*\\d+.*")) {
            time = time.replace(" ", "").replace(":", "");
            int hour = -1;
            int min = -1;
            if (time.matches("\\d+") && time.length() == 4) {
                hour = Integer.parseInt(time.substring(0, 2));
                min = Integer.parseInt(time.substring(2));
            }
            else {
                String my_time = time.replaceAll("\\D+", "");
                switch(my_time.length()) {
                    case 1:
                    case 2:
                        hour = Integer.parseInt(my_time);
                        min = 0;
                        break;
                    case 3:
                        hour = Integer.parseInt(my_time.substring(0, 1));
                        min = Integer.parseInt(my_time.substring(1, 3));
                        break;
                    case 4:
                        hour = Integer.parseInt(my_time.substring(0, 2));
                        min = Integer.parseInt(my_time.substring(2, 4));
                        break;
                }
                if (time.contains("pm")) {
                    hour += 12;
                }
            }
            if (hour < 24 && min < 60 && hour > -1 && min > -1) {
                date.set(Calendar.HOUR_OF_DAY, hour);
                date.set(Calendar.MINUTE, min);
                return date;
            }
        }
        else {
            int hour = -1;
            if (time.contains("morning")) {
                hour = 9;
            }
            else if (time.contains("afternoon")) {
                hour = 15;
            }
            else if (time.contains("evening")|| time.contains("night")) {
                hour = 21;
            }
            else if (time.contains("noon")) {
                hour = 12;
            }
            if (time.contains("early")) {
                hour -= 2;
            }
            else if (time.contains("late")) {
                hour += 2;
            }
            date.set(Calendar.HOUR_OF_DAY, hour);
            if (hour > -1) {
                return date;
            }
        }
        return null;
    }

    private Calendar parseDate(String date) {
        Calendar today = Calendar.getInstance();
        // actual date
        if (date.matches(".*\\d+.*")) {
            // parse month
            String day_str = date;
            if (date.contains("/")) {
                String month_str = date.substring(0, date.indexOf("/"));
                if (month_str.matches("\\d+")) {
                    int month = Integer.parseInt(month_str);
                    if (today.getActualMaximum(Calendar.MONTH) >= month) {
                        today.set(Calendar.MONTH, month);
                    }
                }
                day_str = date.substring(date.indexOf("/") + 1);
            }
            else {
                for (int x = 0; x < MONTHS.length; x++) {
                    if (date.contains(MONTHS[x])) {
                        today.set(Calendar.MONTH, x);
                    }
                }
                day_str = day_str.replace("th", "").replace("st", "").replace("nd", "").replace(" ", "");
            }
            // parse day
            if (day_str.matches("\\d+")) {
                int day = Integer.parseInt(date);
                if (today.getActualMaximum(Calendar.DAY_OF_MONTH) >= day) {
                    today.set(Calendar.DAY_OF_MONTH, day);
                    return today;
                }
            }
        }
        // text to date
        else {
            if (date.equals("today")) {
                return today;
            } else if (date.equals("tomorrow") || date.equals("tmr")) {
                today.add(Calendar.DATE, 1);
                return today;
            }
            for (int x = 0; x < DAYS.length; x++) {
                String day = DAYS[x];
                if (date.contains(day)) {
                    int current_day = today.get(Calendar.DAY_OF_WEEK);
                    int difference = x - (current_day + 1); // calendar constants start at 1
                    if (difference < 0) difference += 7;
                    if (date.contains("next")) difference += 7;
                    today.add(Calendar.DATE, difference);
                    return today;
                }
            }
            if (date.contains("week")) {
                today.add(Calendar.DATE, 7);
                return today;
            }
        }
        return null; // no comprendo
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getOrder() == 1) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

package albertkung.tsma;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TabHost;

public class AddActivity extends AppCompatActivity {

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
    }
}

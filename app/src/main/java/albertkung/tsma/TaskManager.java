package albertkung.tsma;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Calendar;

public class TaskManager {

    private ArrayList<Task> tasks;

    private static final String SAVE_TASK_FILE = "save_task";

    public TaskManager() {
        tasks = new ArrayList<>();
    }

    public void restoreTasks(Context context) {
        try {
            FileInputStream fis = context.openFileInput(SAVE_TASK_FILE);
            ObjectInputStream is = new ObjectInputStream(fis);
            if (is.readObject() != null) {
                this.tasks = (ArrayList<Task>) is.readObject();
            }
            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveTasks(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(SAVE_TASK_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(tasks);
            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public int getNumTasks(int days) {
        int n = 0;
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, days);
        for (Task task : tasks) {
            if (task.isBefore(date)) {
                n++;
            }
        }
        return n;
    }
}

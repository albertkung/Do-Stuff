package albertkung.tsma;

import java.io.Serializable;
import java.util.Calendar;

public class Task implements Serializable {

    private String name;
    private String details;
    private Calendar date;

    public Task(String name, String details, Calendar date) {
        this.name = name;
        this.details = details;
        this.date = date;
    }

    public byte[] getBytes() {
        return new byte[0];
    }
}

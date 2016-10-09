package albertkung.tsma;

import java.io.Serializable;

/**
 * Created by Albert on 10/9/2016.
 */

public class Config implements Serializable {

    private String userName;

    public Config() {
        userName = "friend";
    }

    public String getName() {
        return userName;
    }

    public void setName(String name) {
        userName = name;
    }

}

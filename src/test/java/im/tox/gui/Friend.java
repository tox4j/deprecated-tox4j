package im.tox.gui;

import java.io.Serializable;

public class Friend implements Serializable {

    private String name = "<No name>";

    @Override
    public String toString() {
        return "Friend (" + name + ')';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

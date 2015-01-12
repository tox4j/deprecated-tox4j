package im.tox.gui;

import javax.swing.*;
import java.util.ArrayList;

public class FriendList extends AbstractListModel<Friend> {

    private final ArrayList<Friend> friends = new ArrayList<>();

    public void add(int friendNumber) {
        while (friends.size() <= friendNumber) {
            friends.add(null);
        }
        friends.set(friendNumber, new Friend());
        fireIntervalAdded(this, friendNumber, friendNumber);
    }

    @Override
    public int getSize() {
        return friends.size();
    }

    @Override
    public Friend getElementAt(int index) {
        return friends.get(index);
    }

    public void setName(int friendNumber, String name) {
        friends.get(friendNumber).setName(name);
        fireContentsChanged(this, friendNumber, friendNumber);
    }
}

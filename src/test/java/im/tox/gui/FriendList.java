package im.tox.gui;

import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxStatus;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FriendList extends AbstractListModel<Friend> {

    private final ArrayList<Friend> friends = new ArrayList<>();

    public void add(int friendNumber, byte[] publicKey) {
        while (friends.size() <= friendNumber) {
            friends.add(null);
        }
        Friend oldFriend = friends.get(friendNumber);
        if (oldFriend == null || !Arrays.equals(oldFriend.getPublicKey(), publicKey)) {
            friends.set(friendNumber, new Friend(publicKey));
        }
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

    public void setConnectionStatus(int friendNumber, ToxConnection connectionStatus) {
        friends.get(friendNumber).setConnectionStatus(connectionStatus);
        fireContentsChanged(this, friendNumber, friendNumber);
    }

    public void setStatus(int friendNumber, ToxStatus status) {
        friends.get(friendNumber).setStatus(status);
        fireContentsChanged(this, friendNumber, friendNumber);
    }

    public void setStatusMessage(int friendNumber, String message) {
        friends.get(friendNumber).setStatusMessage(message);
        fireContentsChanged(this, friendNumber, friendNumber);
    }

    public void setTyping(int friendNumber, boolean isTyping) {
        friends.get(friendNumber).setTyping(isTyping);
        fireContentsChanged(this, friendNumber, friendNumber);
    }

}

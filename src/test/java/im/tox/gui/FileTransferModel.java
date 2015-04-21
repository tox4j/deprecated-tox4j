package im.tox.gui;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public final class FileTransferModel extends AbstractListModel<FileTransfer> {

  private final ArrayList<ArrayList<FileTransfer>> transfers = new ArrayList<>();

  @Override
  public int getSize() {
    int size = 0;
    for (ArrayList<FileTransfer> list : transfers) {
      size += list.size();
    }
    return size;
  }

  @Override
  public FileTransfer getElementAt(int index) {
    int position = 0;
    for (ArrayList<FileTransfer> list : transfers) {
      if (position + list.size() > index) {
        return list.get(index - position);
      }
      position += list.size();
    }
    throw new NoSuchElementException(String.valueOf(index));
  }

  private ArrayList<FileTransfer> ensureFileNumber(int friendNumber, int fileNumber) {
    while (transfers.size() <= friendNumber) {
      transfers.add(new ArrayList<FileTransfer>());
    }
    ArrayList<FileTransfer> list = transfers.get(friendNumber);
    while (list.size() <= fileNumber) {
      list.add(null);
    }
    return list;
  }

  public void addOutgoing(int friendNumber, File file, int fileNumber) {
    ArrayList<FileTransfer> list = ensureFileNumber(friendNumber, fileNumber);
    list.set(fileNumber, new FileTransferOutgoing(file));
  }

  public void addIncoming(int friendNumber, int fileNumber, int kind, long fileSize, File file) {
    ArrayList<FileTransfer> list = ensureFileNumber(friendNumber, fileNumber);
    list.set(fileNumber, new FileTransferIncoming(file));
  }

  public FileTransfer get(int friendNumber, int fileNumber) {
    return transfers.get(friendNumber).get(fileNumber);
  }

  public void remove(int friendNumber, int fileNumber) throws IOException {
    get(friendNumber, fileNumber).close();
  }

}

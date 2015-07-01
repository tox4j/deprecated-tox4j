package im.tox.gui;

import im.tox.gui.domain.FileTransferModel;
import im.tox.gui.domain.FriendList;
import im.tox.gui.events.*;
import im.tox.gui.forms.MainViewBase;
import im.tox.gui.util.InvokeLaterToxEventListener;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.callbacks.ToxEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.BoxedUnit;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainView extends MainViewBase {

  private static final Logger logger = LoggerFactory.getLogger(MainView.class);

  private static final int MAX_MESSAGES = 1000;

  public ToxCore<BoxedUnit> tox;
  public Thread eventLoop;

  private DefaultListModel<String> messageModel = new DefaultListModel<>();
  public FriendList friendListModel = new FriendList();
  public final FileTransferModel fileModel = new FileTransferModel();
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");

  public final ToxEventListener<BoxedUnit> toxEvents = new InvokeLaterToxEventListener<>(new GuiToxEventListener(this));

  private static final class SaveData implements Serializable {
    public final byte[] toxSave;
    public final FriendList friendList;
    public final DefaultListModel<String> messages;

    private SaveData(byte[] toxSave, FriendList friendList, DefaultListModel<String> messages) {
      this.toxSave = toxSave;
      this.friendList = friendList;
      this.messages = messages;
    }
  }

  public void addMessage(Object... args) {
    StringBuilder str = new StringBuilder();
    for (Object arg : args) {
      str.append(String.valueOf(arg));
    }
    if (messageModel.size() > MAX_MESSAGES) {
      DefaultListModel<String> newModel = new DefaultListModel<>();
      for (int i = 0; i < messageModel.size() - MAX_MESSAGES / 10; i++) {
        newModel.addElement(messageModel.get(i + MAX_MESSAGES / 10));
      }
      messageModel = newModel;
      messages.setModel(messageModel);
    }
    String message = DATE_FORMAT.format(new Date()) + ' ' + str;
    messageModel.addElement(message);
    logger.info(message);
    messages.ensureIndexIsVisible(messageModel.size() - 1);
    save();
  }

  public void save() {
    if (tox == null) {
      return;
    }
    try (ObjectOutput saveFile = new ObjectOutputStream(new FileOutputStream("/tmp/toxgui.tox"))) {
      SaveData saveData = new SaveData(tox.getSaveData(), friendListModel, messageModel);
      saveFile.writeObject(saveData);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Nullable
  public byte[] load() {
    try (ObjectInput saveFile = new ObjectInputStream(new FileInputStream("/tmp/toxgui.tox"))) {
      SaveData saveData = (SaveData) saveFile.readObject();

      if (saveData.friendList != null) {
        friendListModel = saveData.friendList;
        friendList.setModel(friendListModel);
      }

      if (saveData.messages != null) {
        messageModel = saveData.messages;
        messages.setModel(messageModel);
      }

      return saveData.toxSave;
    } catch (IOException e) {
      return null;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String printExn(Throwable exn) {
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      exn.printStackTrace(new PrintStream(output));
      return output.toString();
    } catch (IOException e1) {
      return exn.toString();
    }
  }

  /**
   * Create a new GUI application for Tox testing.
   */
  public MainView() {
    load();

    messages.ensureIndexIsVisible(messageModel.size() - 1);

    connectButton.addActionListener(new ConnectButtonOnAction(this));
    bootstrapButton.addActionListener(new BootstrapButtonOnAction(this));
    addFriendButton.addActionListener(new AddFriendButtonOnAction(this));
    sendButton.addActionListener(new SendButtonOnAction(this));
    messageText.addKeyListener(new MessageTextOnKey(this));
    sendFileButton.addActionListener(new SendFileButtonOnAction(this));
  }

}

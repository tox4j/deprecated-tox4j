package im.tox.gui;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class FileTransfer implements Closeable {

  protected final File file;

  public FileTransfer(File file) {
    this.file = file;
  }

  public abstract void resume() throws FileNotFoundException;

  public abstract byte[] read(long position, int length) throws IOException;

  public abstract void close() throws IOException;

  public abstract void write(long position, byte[] data) throws IOException;

}

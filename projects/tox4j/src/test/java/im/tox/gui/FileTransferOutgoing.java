package im.tox.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public final class FileTransferOutgoing extends FileTransfer {

  private RandomAccessFile input;

  public FileTransferOutgoing(File file) {
    super(file);
  }

  @Override
  public void resume() throws FileNotFoundException {
    this.input = new RandomAccessFile(file, "r");
  }

  @Override
  public byte[] read(long position, int length) throws IOException {
    input.seek(position);
    byte[] data = new byte[length];
    int readLength = input.read(data);
    if (data.length > readLength) {
      data = Arrays.copyOf(data, readLength);
    }
    return data;
  }

  @Override
  public void close() throws IOException {
    input.close();
  }

  @Override
  public void write(long position, byte[] data) throws IOException {
    throw new IOException("Cannot write to outgoing file");
  }

}

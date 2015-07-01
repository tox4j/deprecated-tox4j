package im.tox.gui.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class FileTransferIncoming extends FileTransfer {

  private static final Logger logger = LoggerFactory.getLogger(FileTransferIncoming.class);

  private RandomAccessFile input;

  public FileTransferIncoming(File file) {
    super(file);
  }

  @Override
  public void resume() throws FileNotFoundException {
    if (this.input != null) {
      logger.warn("RESUME received with open file; not re-opening");
    } else {
      this.input = new RandomAccessFile(file, "rw");
    }
  }

  @Override
  public byte[] read(long position, int length) throws IOException {
    throw new IOException("Cannot read from incoming file");
  }

  @Override
  public void close() throws IOException {
    input.close();
  }

  @Override
  public void write(long position, byte[] data) throws IOException {
    if (input == null) {
      logger.warn("Writing before receiving a RESUME control; opening file anyway");
      input = new RandomAccessFile(file, "w");
    }
    input.seek(position);
    input.write(data);
  }

}

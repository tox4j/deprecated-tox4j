package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;

public interface ReceiveVideoFrameCallback {

  @SuppressWarnings("checkstyle:parametername")
  void receiveVideoFrame(
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a
  );

}

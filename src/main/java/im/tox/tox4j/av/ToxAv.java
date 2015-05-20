package im.tox.tox4j.av;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.callbacks.*;
import im.tox.tox4j.av.enums.ToxCallControl;
import im.tox.tox4j.av.exceptions.*;

import java.io.Closeable;

public interface ToxAv extends Closeable {

  @Override
  void close();

  int iterationInterval();

  void iteration();

  void call(int friendNumber, int audioBitRate, int videoBitRate) throws ToxCallException;

  void callbackCall(@NotNull CallCallback callback);

  void answer(int friendNumber, int audioBitRate, int videoBitRate) throws ToxAnswerException;

  void callControl(int friendNumber, @NotNull ToxCallControl control) throws ToxCallControlException;

  void callbackCallControl(@NotNull CallStateCallback callback);

  void setAudioBitRate(int friendNumber, int bitRate) throws ToxBitRateException;

  void setVideoBitRate(int friendNumber, int bitRate) throws ToxBitRateException;

  @SuppressWarnings("checkstyle:parametername")
  void sendVideoFrame(
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a
  ) throws ToxSendFrameException;

  void sendAudioFrame(
      int friendNumber, @NotNull short[] pcm, int sampleCount, int channels, int samplingRate
  ) throws ToxSendFrameException;

  void callbackReceiveVideoFrame(@NotNull ReceiveVideoFrameCallback callback);

  void callbackReceiveAudioFrame(@NotNull ReceiveAudioFrameCallback callback);

  /**
   * Convenience method to set all event handlers at once.
   *
   * @param handler An event handler capable of handling all Tox AV events.
   */
  void callback(@NotNull ToxAvEventListener handler);

}

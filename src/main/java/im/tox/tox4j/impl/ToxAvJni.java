package im.tox.tox4j.impl;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.exceptions.*;
import scala.MatchError;

@SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
final class ToxAvJni {

  static {
    System.loadLibrary("tox4j");
  }

  static native int toxAvNew(int toxInstanceNumber) throws ToxAvNewException;
  static native void toxAvKill(int instanceNumber);
  static native void finalize(int instanceNumber);
  static native int toxAvIterationInterval(int instanceNumber);
  @Nullable
  static native byte[] toxAvIteration(int instanceNumber);
  static native void toxAvCall(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxCallException;
  static native void toxAvAnswer(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxAnswerException;
  static native void toxAvCallControl(int instanceNumber, int friendNumber, int control) throws ToxCallControlException;
  static native void toxAvSetAudioBitRate(int instanceNumber, int friendNumber, int audioBitRate) throws ToxBitRateException;
  static native void toxAvSetVideoBitRate(int instanceNumber, int friendNumber, int videoBitRate) throws ToxBitRateException;

  @SuppressWarnings("checkstyle:parametername")
  static native void toxAvSendVideoFrame(
      int instanceNumber,
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a
  ) throws ToxSendFrameException;

  static native void toxAvSendAudioFrame(
      int instanceNumber,
      int friendNumber,
      @NotNull short[] pcm, int sampleCount, int channels, int samplingRate
  ) throws ToxSendFrameException;

  static <T> T conversionError(@NotNull String className, @NotNull String name) {
    throw new MatchError("ToxAv: Could not convert " + className + "." + name);
  }

}

package im.tox.tox4j.impl.jni;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.exceptions.*;
import scala.MatchError;

@SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
final class ToxAvJni {

  static {
    System.loadLibrary("tox4j");
  }

  static native int toxavNew(int toxInstanceNumber) throws ToxAvNewException;
  static native void toxavKill(int instanceNumber);
  static native void toxavFinalize(int instanceNumber);
  static native int toxavIterationInterval(int instanceNumber);
  @Nullable
  static native byte[] toxavIterate(int instanceNumber);
  static native void toxavCall(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxAvCallException;
  static native void toxavAnswer(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxAvAnswerException;
  static native void toxavCallControl(int instanceNumber, int friendNumber, int control) throws ToxAvCallControlException;
  static native void toxavAudioBitRateSet(int instanceNumber, int friendNumber, int audioBitRate, boolean force) throws ToxAvBitRateException;
  static native void toxavVideoBitRateSet(int instanceNumber, int friendNumber, int videoBitRate, boolean force) throws ToxAvBitRateException;

  static native void toxavAudioSendFrame(
      int instanceNumber,
      int friendNumber,
      @NotNull short[] pcm, int sampleCount, int channels, int samplingRate
  ) throws ToxAvSendFrameException;

  @SuppressWarnings("checkstyle:parametername")
  static native void toxavVideoSendFrame(
      int instanceNumber,
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a
  ) throws ToxAvSendFrameException;

  static <T> T conversionError(@NotNull String className, @NotNull String name) {
    throw new MatchError("ToxAv: Could not convert " + className + "." + name);
  }

}

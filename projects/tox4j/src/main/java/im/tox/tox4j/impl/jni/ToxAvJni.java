package im.tox.tox4j.impl.jni;

import im.tox.tox4j.av.exceptions.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.MatchError;

@SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
public final class ToxAvJni {

  static {
    System.loadLibrary("tox4j");
  }

  static native int toxavNew(int toxInstanceNumber) throws ToxavNewException;
  static native void toxavKill(int instanceNumber);
  static native void toxavFinalize(int instanceNumber);
  static native int toxavIterationInterval(int instanceNumber);
  @Nullable
  static native byte[] toxavIterate(int instanceNumber);
  static native void toxavCall(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxavCallException;
  static native void toxavAnswer(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxavAnswerException;
  static native void toxavCallControl(int instanceNumber, int friendNumber, int control) throws ToxavCallControlException;
  static native void toxavAudioBitRateSet(int instanceNumber, int friendNumber, int audioBitRate, boolean force) throws ToxavSetBitRateException;
  static native void toxavVideoBitRateSet(int instanceNumber, int friendNumber, int videoBitRate, boolean force) throws ToxavSetBitRateException;

  static native void toxavAudioSendFrame(
      int instanceNumber,
      int friendNumber,
      @NotNull short[] pcm, int sampleCount, int channels, int samplingRate
  ) throws ToxavSendFrameException;

  @SuppressWarnings("checkstyle:parametername")
  static native void toxavVideoSendFrame(
      int instanceNumber,
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a
  ) throws ToxavSendFrameException;

  static <T> T conversionError(@NotNull String className, @NotNull String name) {
    throw new MatchError("ToxAv: Could not convert " + className + "." + name);
  }

}

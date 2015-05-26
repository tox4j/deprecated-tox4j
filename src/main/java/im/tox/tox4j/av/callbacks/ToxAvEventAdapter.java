package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.enums.ToxCallState;

import java.util.Collection;

@SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
public class ToxAvEventAdapter implements ToxAvEventListener {
  @Override public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) { }
  @Override public void callState(int friendNumber, @NotNull Collection<ToxCallState> state) { }
  @Override public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) { }
  @SuppressWarnings("checkstyle:parametername")
  @Override public void receiveVideoFrame(int friendNumber, int width, int height, @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a, int yStride, int uStride, int vStride, int aStride) { }
}

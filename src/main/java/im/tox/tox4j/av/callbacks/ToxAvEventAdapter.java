package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.enums.ToxCallControl;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxFileControl;
import im.tox.tox4j.enums.ToxFileKind;
import im.tox.tox4j.enums.ToxStatus;

public class ToxAvEventAdapter implements ToxAvEventListener {

    @Override public void call(int friendNumber) { }
    @Override public void callControl(int friendNumber, @NotNull ToxCallControl control) { }
    @Override public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) { }
    @Override public void receiveVideoFrame(int friendNumber, int width, int height, @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a) { }
    @Override public void requestAudioFrame(int friendNumber) { }
    @Override public void requestVideoFrame(int friendNumber) { }

}

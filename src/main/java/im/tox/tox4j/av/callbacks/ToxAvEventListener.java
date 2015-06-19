package im.tox.tox4j.av.callbacks;

public interface ToxAvEventListener extends
    CallCallback,
    CallStateCallback,
    ReceiveAudioFrameCallback,
    ReceiveVideoFrameCallback {

  ToxAvEventListener EMPTY = new ToxAvEventAdapter();

}

package im.tox.tox4j.av.callbacks;

public interface ToxAvEventListener extends
    CallCallback,
    CallStateCallback,
    AudioReceiveFrameCallback,
    VideoReceiveFrameCallback {
  ToxAvEventListener EMPTY = new ToxAvEventAdapter();
}

package im.tox.tox4j.av.callbacks;

public interface ToxAvEventListener extends
    CallCallback,
    CallControlCallback,
    CallStateCallback,
    AudioBitRateStatusCallback,
    VideoBitRateStatusCallback,
    AudioReceiveFrameCallback,
    VideoReceiveFrameCallback {
  ToxAvEventListener IGNORE = new ToxAvEventAdapter();
}

package im.tox.tox4j.av.callbacks

trait ToxAvEventListener extends
  CallCallback with
  CallStateCallback with
  AudioBitRateStatusCallback with
  VideoBitRateStatusCallback with
  AudioReceiveFrameCallback with
  VideoReceiveFrameCallback

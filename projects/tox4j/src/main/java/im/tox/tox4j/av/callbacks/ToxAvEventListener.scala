package im.tox.tox4j.av.callbacks

trait ToxAvEventListener[ToxCoreState]
  extends CallCallback[ToxCoreState]
  with CallStateCallback[ToxCoreState]
  with AudioBitRateStatusCallback[ToxCoreState]
  with VideoBitRateStatusCallback[ToxCoreState]
  with AudioReceiveFrameCallback[ToxCoreState]
  with VideoReceiveFrameCallback[ToxCoreState]

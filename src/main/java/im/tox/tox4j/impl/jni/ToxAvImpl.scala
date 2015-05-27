package im.tox.tox4j.impl.jni

import java.util

import com.google.protobuf.ByteString
import im.tox.tox4j.annotations.Nullable
import im.tox.tox4j.av.{ ToxAv, AbstractToxAv }
import im.tox.tox4j.av.callbacks._
import im.tox.tox4j.av.enums.{ ToxCallControl, ToxCallState }
import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.av.proto._
import im.tox.tox4j.core.ToxCore

// scalastyle:off
private object ToxAvImpl {
  private def convert(kind: CallState.Kind.EnumVal): ToxCallState = {
    kind match {
      case CallState.Kind.ERROR       => ToxCallState.ERROR
      case CallState.Kind.FINISHED    => ToxCallState.FINISHED
      case CallState.Kind.SENDING_A   => ToxCallState.SENDING_A
      case CallState.Kind.SENDING_V   => ToxCallState.SENDING_V
      case CallState.Kind.RECEIVING_A => ToxCallState.RECEIVING_A
      case CallState.Kind.RECEIVING_V => ToxCallState.RECEIVING_V
      case _                          => ToxAvJni.conversionError[ToxCallState](kind.getClass.getName, kind.name)
    }
  }
}

/**
 * Initialise an A/V session for the existing Tox instance.
 *
 * @param tox An instance of the C-backed ToxCore implementation.
 */
@throws[ToxAvNewException]("If there was already an A/V session.")
final class ToxAvImpl(private val tox: ToxCoreImpl) extends AbstractToxAv {

  private val instanceNumber = ToxAvJni.toxavNew(this.tox.instanceNumber)

  private val onClose = this.tox.addOnCloseCallback(close)

  private var callCallback = CallCallback.IGNORE
  private var callStateCallback = CallStateCallback.IGNORE
  private var audioBitRateStatusCallback = AudioBitRateStatusCallback.IGNORE
  private var videoBitRateStatusCallback = VideoBitRateStatusCallback.IGNORE
  private var audioReceiveFrameCallback = AudioReceiveFrameCallback.IGNORE
  private var videoReceiveFrameCallback = VideoReceiveFrameCallback.IGNORE

  override def create(tox: ToxCore): ToxAv =
    new ToxAvImpl(tox.asInstanceOf[ToxCoreImpl])

  override def close(): Unit = {
    tox.removeOnCloseCallback(onClose)
    ToxAvJni.toxavKill(instanceNumber)
  }

  protected override def finalize(): Unit = {
    try {
      ToxAvJni.toxavFinalize(instanceNumber)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
    super.finalize()
  }

  override def iterate(): Unit = {
    Option(ToxAvJni.toxavIterate(instanceNumber)).map(ByteString.copyFrom).map(AvEvents.parseFrom) match {
      case None =>
      case Some(AvEvents(
        call,
        callState,
        audioBitRateStatus,
        videoBitRateStatus,
        audioReceiveFrame,
        videoReceiveFrame)) =>
        call.foreach {
          case Call(friendNumber, audioEnabled, videoEnabled) =>
            callCallback.call(
              friendNumber,
              audioEnabled,
              videoEnabled
            )
        }
        callState.foreach {
          case CallState(friendNumber, state) =>
            callStateCallback.callState(
              friendNumber,
              util.Arrays.asList(state.map(ToxAvImpl.convert): _*)
            )
        }
        audioBitRateStatus.foreach {
          case AudioBitRateStatus(friendNumber, stable, bitRate) =>
            audioBitRateStatusCallback.audioBitRateStatus(
              friendNumber,
              stable,
              bitRate
            )
        }
        videoBitRateStatus.foreach {
          case VideoBitRateStatus(friendNumber, stable, bitRate) =>
            videoBitRateStatusCallback.videoBitRateStatus(
              friendNumber,
              stable,
              bitRate
            )
        }
        audioReceiveFrame.foreach {
          case AudioReceiveFrame(friendNumber, pcm, channels, samplingRate) =>
            audioReceiveFrameCallback.receiveAudioFrame(
              friendNumber,
              pcm.map(_.toShort).toArray,
              channels,
              samplingRate
            )
        }
        videoReceiveFrame.foreach {
          case VideoReceiveFrame(friendNumber, width, height, y, u, v, a, yStride, uStride, vStride, aStride) =>
            videoReceiveFrameCallback.receiveVideoFrame(
              friendNumber,
              width,
              height,
              y.toByteArray,
              u.toByteArray,
              v.toByteArray,
              a.map(_.toByteArray).orNull,
              yStride,
              uStride,
              vStride,
              aStride
            )
        }
    }
  }

  override def iterationInterval: Int =
    ToxAvJni.toxavIterationInterval(instanceNumber)

  @throws[ToxCallException]
  override def call(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavCall(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxAnswerException]
  override def answer(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavAnswer(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxCallControlException]
  override def callControl(friendNumber: Int, control: ToxCallControl): Unit =
    ToxAvJni.toxavCallControl(instanceNumber, friendNumber, control.ordinal)

  @throws[ToxBitRateException]
  override def audioBitRateSet(friendNumber: Int, bitRate: Int, force: Boolean): Unit =
    ToxAvJni.toxavAudioBitRateSet(instanceNumber, friendNumber, bitRate, force)

  @throws[ToxBitRateException]
  override def videoBitRateSet(friendNumber: Int, bitRate: Int, force: Boolean): Unit =
    ToxAvJni.toxavVideoBitRateSet(instanceNumber, friendNumber, bitRate, force)

  @throws[ToxSendFrameException]
  override def audioSendFrame(friendNumber: Int, pcm: Array[Short], sampleCount: Int, channels: Int, samplingRate: Int): Unit =
    ToxAvJni.toxavAudioSendFrame(instanceNumber, friendNumber, pcm, sampleCount, channels, samplingRate)

  @throws[ToxSendFrameException]
  override def videoSendFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte], @Nullable a: Array[Byte]): Unit =
    ToxAvJni.toxavVideoSendFrame(instanceNumber, friendNumber, width, height, y, u, v, a)

  override def callbackCall(callback: CallCallback): Unit = this.callCallback = callback
  override def callbackCallControl(callback: CallStateCallback): Unit = this.callStateCallback = callback
  override def callbackVideoReceiveFrame(callback: VideoReceiveFrameCallback): Unit = this.videoReceiveFrameCallback = callback
  override def callbackAudioReceiveFrame(callback: AudioReceiveFrameCallback): Unit = this.audioReceiveFrameCallback = callback
  override def callbackAudioBitRateStatus(callback: AudioBitRateStatusCallback): Unit = this.audioBitRateStatusCallback = callback
  override def callbackVideoBitRateStatus(callback: VideoBitRateStatusCallback): Unit = this.videoBitRateStatusCallback = callback
}

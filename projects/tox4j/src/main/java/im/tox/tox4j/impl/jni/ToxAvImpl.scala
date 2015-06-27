package im.tox.tox4j.impl.jni

import java.util

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.ToxImplBase.tryAndLog
import im.tox.tox4j.annotations.{ NotNull, Nullable }
import im.tox.tox4j.av.callbacks._
import im.tox.tox4j.av.enums.{ ToxCallControl, ToxCallState }
import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.av.proto.Av._
import im.tox.tox4j.av.{ AbstractToxAv, ToxAv }
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.impl.jni.ToxAvImpl.{ convert, logger }
import org.slf4j.LoggerFactory

private object ToxAvImpl {

  private val logger = Logger(LoggerFactory.getLogger(getClass))

  private def convert(kind: CallState.Kind): ToxCallState = {
    kind match {
      case CallState.Kind.ERROR       => ToxCallState.ERROR
      case CallState.Kind.FINISHED    => ToxCallState.FINISHED
      case CallState.Kind.SENDING_A   => ToxCallState.SENDING_A
      case CallState.Kind.SENDING_V   => ToxCallState.SENDING_V
      case CallState.Kind.RECEIVING_A => ToxCallState.RECEIVING_A
      case CallState.Kind.RECEIVING_V => ToxCallState.RECEIVING_V
    }
  }

}

/**
 * Initialise an A/V session for the existing Tox instance.
 *
 * @param tox An instance of the C-backed ToxCore implementation.
 */
// scalastyle:off var.field no.finalize
@throws[ToxAvNewException]("If there was already an A/V session.")
final class ToxAvImpl(@NotNull private val tox: ToxCoreImpl) extends AbstractToxAv {

  private val instanceNumber = ToxAvJni.toxavNew(tox.instanceNumber)

  private val onClose = tox.addOnCloseCallback(close)

  private var callCallback = CallCallback.IGNORE
  private var callStateCallback = CallStateCallback.IGNORE
  private var audioBitRateStatusCallback = AudioBitRateStatusCallback.IGNORE
  private var videoBitRateStatusCallback = VideoBitRateStatusCallback.IGNORE
  private var audioReceiveFrameCallback = AudioReceiveFrameCallback.IGNORE
  private var videoReceiveFrameCallback = VideoReceiveFrameCallback.IGNORE

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.AsInstanceOf"))
  override def create(tox: ToxCore): ToxAv = {
    try {
      new ToxAvImpl(tox.asInstanceOf[ToxCoreImpl])
    } catch {
      case _: ClassCastException =>
        throw new ToxAvNewException(ToxAvNewException.Code.INCOMPATIBLE, tox.getClass.getCanonicalName)
    }
  }

  override def close(): Unit = {
    tox.removeOnCloseCallback(onClose)
    ToxAvJni.toxavKill(instanceNumber)
  }

  protected override def finalize(): Unit = {
    try {
      ToxAvJni.toxavFinalize(instanceNumber)
    } catch {
      case e: Throwable =>
        logger.error("Exception caught in finalizer; this indicates a serious problem in native code", e)
    }
    super.finalize()
  }

  private def dispatchCall(call: Seq[Call]): Unit = {
    call.foreach {
      case Call(friendNumber, audioEnabled, videoEnabled) =>
        tryAndLog(callCallback)(_.call(
          friendNumber,
          audioEnabled,
          videoEnabled
        ))
    }
  }

  private def dispatchCallState(callState: Seq[CallState]): Unit = {
    callState.foreach {
      case CallState(friendNumber, state) =>
        tryAndLog(callStateCallback)(_.callState(
          friendNumber,
          util.Arrays.asList(state.map(convert): _*)
        ))
    }
  }

  private def dispatchAudioBitRateStatus(audioBitRateStatus: Seq[AudioBitRateStatus]): Unit = {
    audioBitRateStatus.foreach {
      case AudioBitRateStatus(friendNumber, stable, bitRate) =>
        tryAndLog(audioBitRateStatusCallback)(_.audioBitRateStatus(
          friendNumber,
          stable,
          bitRate
        ))
    }
  }

  private def dispatchVideoBitRateStatus(videoBitRateStatus: Seq[VideoBitRateStatus]): Unit = {
    videoBitRateStatus.foreach {
      case VideoBitRateStatus(friendNumber, stable, bitRate) =>
        tryAndLog(videoBitRateStatusCallback)(_.videoBitRateStatus(
          friendNumber,
          stable,
          bitRate
        ))
    }
  }

  private def dispatchAudioReceiveFrame(audioReceiveFrame: Seq[AudioReceiveFrame]): Unit = {
    audioReceiveFrame.foreach {
      case AudioReceiveFrame(friendNumber, pcm, channels, samplingRate) =>
        tryAndLog(audioReceiveFrameCallback)(_.receiveAudioFrame(
          friendNumber,
          pcm.map(_.toShort).toArray,
          channels,
          samplingRate
        ))
    }
  }

  @SuppressWarnings(Array("im.tox.tox4j.lint.OptionOrNull"))
  private def dispatchVideoReceiveFrame(videoReceiveFrame: Seq[VideoReceiveFrame]): Unit = {
    videoReceiveFrame.foreach {
      case VideoReceiveFrame(friendNumber, width, height, y, u, v, a, yStride, uStride, vStride, aStride) =>
        tryAndLog(videoReceiveFrameCallback)(_.receiveVideoFrame(
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
        ))
    }
  }

  private def dispatchEvents(events: AvEvents): Unit = {
    dispatchCall(events.call)
    dispatchCallState(events.callState)
    dispatchAudioBitRateStatus(events.audioBitRateStatus)
    dispatchVideoBitRateStatus(events.videoBitRateStatus)
    dispatchAudioReceiveFrame(events.audioReceiveFrame)
    dispatchVideoReceiveFrame(events.videoReceiveFrame)
  }

  override def iterate(): Unit = {
    Option(ToxAvJni.toxavIterate(instanceNumber))
      .map(AvEvents.parseFrom)
      .foreach(dispatchEvents)
  }

  override def iterationInterval: Int =
    ToxAvJni.toxavIterationInterval(instanceNumber)

  @throws[ToxAvCallException]
  override def call(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavCall(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxAvAnswerException]
  override def answer(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavAnswer(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxAvCallControlException]
  override def callControl(friendNumber: Int, control: ToxCallControl): Unit =
    ToxAvJni.toxavCallControl(instanceNumber, friendNumber, control.ordinal)

  @throws[ToxAvSetBitRateException]
  override def audioBitRateSet(friendNumber: Int, bitRate: Int, force: Boolean): Unit =
    ToxAvJni.toxavAudioBitRateSet(instanceNumber, friendNumber, bitRate, force)

  @throws[ToxAvSetBitRateException]
  override def videoBitRateSet(friendNumber: Int, bitRate: Int, force: Boolean): Unit =
    ToxAvJni.toxavVideoBitRateSet(instanceNumber, friendNumber, bitRate, force)

  @throws[ToxAvSendFrameException]
  override def audioSendFrame(friendNumber: Int, pcm: Array[Short], sampleCount: Int, channels: Int, samplingRate: Int): Unit =
    ToxAvJni.toxavAudioSendFrame(instanceNumber, friendNumber, pcm, sampleCount, channels, samplingRate)

  @throws[ToxAvSendFrameException]
  override def videoSendFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte], @Nullable a: Array[Byte]): Unit =
    ToxAvJni.toxavVideoSendFrame(instanceNumber, friendNumber, width, height, y, u, v, a)

  override def callbackCall(callback: CallCallback): Unit = this.callCallback = callback
  override def callbackCallState(callback: CallStateCallback): Unit = this.callStateCallback = callback
  override def callbackVideoReceiveFrame(callback: VideoReceiveFrameCallback): Unit = this.videoReceiveFrameCallback = callback
  override def callbackAudioReceiveFrame(callback: AudioReceiveFrameCallback): Unit = this.audioReceiveFrameCallback = callback
  override def callbackAudioBitRateStatus(callback: AudioBitRateStatusCallback): Unit = this.audioBitRateStatusCallback = callback
  override def callbackVideoBitRateStatus(callback: VideoBitRateStatusCallback): Unit = this.videoBitRateStatusCallback = callback

}

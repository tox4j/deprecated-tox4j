package im.tox.tox4j.impl.jni

import java.util

import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import im.tox.tox4j.ToxImplBase.tryAndLog
import im.tox.tox4j.av.ToxAv
import im.tox.tox4j.av.callbacks._
import im.tox.tox4j.av.enums.{ ToxavCallControl, ToxavFriendCallState }
import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.av.proto.Av._
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.impl.jni.ToxAvImpl.{ convert, logger }
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory

import scalaz.Scalaz._

private object ToxAvImpl {

  private val logger = Logger(LoggerFactory.getLogger(getClass))

  private def convert(kind: CallState.Kind): ToxavFriendCallState = {
    kind match {
      case CallState.Kind.ERROR       => ToxavFriendCallState.ERROR
      case CallState.Kind.FINISHED    => ToxavFriendCallState.FINISHED
      case CallState.Kind.SENDING_A   => ToxavFriendCallState.SENDING_A
      case CallState.Kind.SENDING_V   => ToxavFriendCallState.SENDING_V
      case CallState.Kind.ACCEPTING_A => ToxavFriendCallState.ACCEPTING_A
      case CallState.Kind.ACCEPTING_V => ToxavFriendCallState.ACCEPTING_V
    }
  }

  private def convert(callState: util.Collection[ToxavFriendCallState]): Int = {
    import scala.collection.JavaConverters._
    callState.asScala.foldLeft(0) { (bitMask, state) =>
      val nextMask = state match {
        case ToxavFriendCallState.ERROR       => 1 << 0
        case ToxavFriendCallState.FINISHED    => 1 << 1
        case ToxavFriendCallState.SENDING_A   => 1 << 2
        case ToxavFriendCallState.SENDING_V   => 1 << 3
        case ToxavFriendCallState.ACCEPTING_A => 1 << 4
        case ToxavFriendCallState.ACCEPTING_V => 1 << 5
      }
      bitMask | nextMask
    }
  }

}

/**
 * Initialise an A/V session for the existing Tox instance.
 *
 * @param tox An instance of the C-backed ToxCore implementation.
 */
// scalastyle:off no.finalize
@throws[ToxavNewException]("If there was already an A/V session.")
final class ToxAvImpl[ToxCoreState](@NotNull private val tox: ToxCoreImpl[ToxCoreState]) extends ToxAv[ToxCoreState] {

  private val instanceNumber = ToxAvJni.toxavNew(tox.instanceNumber)

  private val onClose = tox.addOnCloseCallback(close)

  private var eventListener: ToxAvEventListener[ToxCoreState] = new ToxAvEventAdapter[ToxCoreState] // scalastyle:ignore var.field

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.AsInstanceOf"))
  override def create(tox: ToxCore[ToxCoreState]): ToxAv[ToxCoreState] = {
    try {
      new ToxAvImpl(tox.asInstanceOf[ToxCoreImpl[ToxCoreState]])
    } catch {
      case _: ClassCastException =>
        throw new ToxavNewException(ToxavNewException.Code.INCOMPATIBLE, tox.getClass.getCanonicalName)
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

  private def dispatchCall(call: Seq[Call])(state: ToxCoreState): ToxCoreState = {
    call.foldLeft(state) {
      case (state, Call(friendNumber, audioEnabled, videoEnabled)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.call(
          friendNumber,
          audioEnabled,
          videoEnabled
        ))
    }
  }

  private def dispatchCallState(callState: Seq[CallState])(state: ToxCoreState): ToxCoreState = {
    callState.foldLeft(state) {
      case (state, CallState(friendNumber, callState)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.callState(
          friendNumber,
          util.Arrays.asList(callState.map(convert): _*)
        ))
    }
  }

  private def dispatchAudioBitRateStatus(audioBitRateStatus: Seq[AudioBitRateStatus])(state: ToxCoreState): ToxCoreState = {
    audioBitRateStatus.foldLeft(state) {
      case (state, AudioBitRateStatus(friendNumber, stable, bitRate)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.audioBitRateStatus(
          friendNumber,
          stable,
          bitRate
        ))
    }
  }

  private def dispatchVideoBitRateStatus(videoBitRateStatus: Seq[VideoBitRateStatus])(state: ToxCoreState): ToxCoreState = {
    videoBitRateStatus.foldLeft(state) {
      case (state, VideoBitRateStatus(friendNumber, stable, bitRate)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.videoBitRateStatus(
          friendNumber,
          stable,
          bitRate
        ))
    }
  }

  private def toShortArray(bytes: ByteString): Array[Short] = {
    val shortBuffer = bytes.asReadOnlyByteBuffer().asShortBuffer()
    val shortArray = Array.ofDim[Short](shortBuffer.capacity)
    shortBuffer.get(shortArray)
    shortArray
  }

  private def dispatchAudioReceiveFrame(audioReceiveFrame: Seq[AudioReceiveFrame])(state: ToxCoreState): ToxCoreState = {
    audioReceiveFrame.foldLeft(state) {
      case (state, AudioReceiveFrame(friendNumber, pcm, channels, samplingRate)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.audioReceiveFrame(
          friendNumber,
          toShortArray(pcm),
          channels,
          samplingRate
        ))
    }
  }

  @SuppressWarnings(Array("im.tox.tox4j.lint.OptionOrNull"))
  private def dispatchVideoReceiveFrame(videoReceiveFrame: Seq[VideoReceiveFrame])(state: ToxCoreState): ToxCoreState = {
    videoReceiveFrame.foldLeft(state) {
      case (state, VideoReceiveFrame(friendNumber, width, height, y, u, v, yStride, uStride, vStride)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.videoReceiveFrame(
          friendNumber,
          width,
          height,
          y.toByteArray,
          u.toByteArray,
          v.toByteArray,
          yStride,
          uStride,
          vStride
        ))
    }
  }

  private def dispatchEvents(state: ToxCoreState, events: AvEvents): ToxCoreState = {
    (state
      |> dispatchCall(events.call)
      |> dispatchCallState(events.callState)
      |> dispatchAudioBitRateStatus(events.audioBitRateStatus)
      |> dispatchVideoBitRateStatus(events.videoBitRateStatus)
      |> dispatchAudioReceiveFrame(events.audioReceiveFrame)
      |> dispatchVideoReceiveFrame(events.videoReceiveFrame))
  }

  override def iterate(state: ToxCoreState): ToxCoreState = {
    Option(ToxAvJni.toxavIterate(instanceNumber))
      .map(AvEvents.parseFrom)
      .foldLeft(state)(dispatchEvents)
  }

  override def iterationInterval: Int =
    ToxAvJni.toxavIterationInterval(instanceNumber)

  @throws[ToxavCallException]
  override def call(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavCall(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxavAnswerException]
  override def answer(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavAnswer(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxavCallControlException]
  override def callControl(friendNumber: Int, control: ToxavCallControl): Unit =
    ToxAvJni.toxavCallControl(instanceNumber, friendNumber, control.ordinal)

  @throws[ToxavSetBitRateException]
  override def setAudioBitRate(friendNumber: Int, bitRate: Int, force: Boolean): Unit =
    ToxAvJni.toxavAudioBitRateSet(instanceNumber, friendNumber, bitRate, force)

  @throws[ToxavSetBitRateException]
  override def setVideoBitRate(friendNumber: Int, bitRate: Int, force: Boolean): Unit =
    ToxAvJni.toxavVideoBitRateSet(instanceNumber, friendNumber, bitRate, force)

  @throws[ToxavSendFrameException]
  override def audioSendFrame(friendNumber: Int, pcm: Array[Short], sampleCount: Int, channels: Int, samplingRate: Int): Unit =
    ToxAvJni.toxavAudioSendFrame(instanceNumber, friendNumber, pcm, sampleCount, channels, samplingRate)

  @throws[ToxavSendFrameException]
  override def videoSendFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte]): Unit =
    ToxAvJni.toxavVideoSendFrame(instanceNumber, friendNumber, width, height, y, u, v)

  override def callback(handler: ToxAvEventListener[ToxCoreState]): Unit = {
    this.eventListener = handler
  }

  def invokeAudioBitRateStatus(friendNumber: Int, stable: Boolean, bitRate: Int): Unit =
    ToxAvJni.invokeAudioBitRateStatus(instanceNumber, friendNumber, stable, bitRate)
  def invokeAudioReceiveFrame(friendNumber: Int, pcm: Array[Short], channels: Int, samplingRate: Int): Unit =
    ToxAvJni.invokeAudioReceiveFrame(instanceNumber, friendNumber, pcm, channels, samplingRate)
  def invokeCall(friendNumber: Int, audioEnabled: Boolean, videoEnabled: Boolean): Unit =
    ToxAvJni.invokeCall(instanceNumber, friendNumber, audioEnabled, videoEnabled)
  def invokeCallState(friendNumber: Int, callState: util.Collection[ToxavFriendCallState]): Unit =
    ToxAvJni.invokeCallState(instanceNumber, friendNumber, convert(callState))
  def invokeVideoBitRateStatus(friendNumber: Int, stable: Boolean, bitRate: Int): Unit =
    ToxAvJni.invokeVideoBitRateStatus(instanceNumber, friendNumber, stable, bitRate)
  // scalastyle:ignore line.size.limit
  def invokeVideoReceiveFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte], yStride: Int, uStride: Int, vStride: Int): Unit =
    ToxAvJni.invokeVideoReceiveFrame(instanceNumber, friendNumber, width, height, y, u, v, yStride, uStride, vStride)

}

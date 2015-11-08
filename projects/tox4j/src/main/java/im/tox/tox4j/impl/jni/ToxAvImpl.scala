package im.tox.tox4j.impl.jni

import java.util

import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import im.tox.tox4j.ToxImplBase.tryAndLog
import im.tox.tox4j.av.ToxAv
import im.tox.tox4j.av.callbacks._
import im.tox.tox4j.av.enums.{ToxavCallControl, ToxavFriendCallState}
import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.av.proto.Av._
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.impl.jni.ToxAvImpl.{convert, logger}
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.util.control.NonFatal

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

  private def dispatchBitRateStatus(bitRateStatus: Seq[BitRateStatus])(state: ToxCoreState): ToxCoreState = {
    bitRateStatus.foldLeft(state) {
      case (state, BitRateStatus(friendNumber, audioBitRate, videoBitRate)) =>
        tryAndLog(tox.options.fatalErrors, state, eventListener)(_.bitRateStatus(
          friendNumber,
          audioBitRate,
          videoBitRate
        ))
    }
  }

  private def toShortArray(bytes: ByteString): Array[Short] = {
    val shortBuffer = bytes.asReadOnlyByteBuffer().asShortBuffer()
    val shortArray = Array.ofDim[Short](shortBuffer.capacity)
    shortBuffer.get(shortArray)
    shortArray
  }

  @tailrec
  private def dispatchAudioReceiveFrame(audioReceiveFrame: Seq[AudioReceiveFrame])(state: ToxCoreState): ToxCoreState = {
    if (audioReceiveFrame.isEmpty) {
      state
    } else {
      val thisFrame = audioReceiveFrame.head
      val nextState =
        if (!tox.options.fatalErrors) {
          try {
            eventListener.audioReceiveFrame(
              thisFrame.friendNumber,
              toShortArray(thisFrame.pcm),
              thisFrame.channels,
              thisFrame.samplingRate
            )(state)
          } catch {
            case NonFatal(e) =>
              logger.warn("Exception caught while executing audioReceiveFrame", e)
              state
          }
        } else {
          eventListener.audioReceiveFrame(
            thisFrame.friendNumber,
            toShortArray(thisFrame.pcm),
            thisFrame.channels,
            thisFrame.samplingRate
          )(state)
        }
      dispatchAudioReceiveFrame(audioReceiveFrame.tail)(nextState)
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

  @im.tox.tox4j.Optimised
  private def dispatchEvents(state: ToxCoreState, events: AvEvents): ToxCoreState = {
    (state
      |> dispatchCall(events.call)
      |> dispatchCallState(events.callState)
      |> dispatchBitRateStatus(events.bitRateStatus)
      |> dispatchAudioReceiveFrame(events.audioReceiveFrame)
      |> dispatchVideoReceiveFrame(events.videoReceiveFrame))
  }

  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Null"))
  override def iterate(state: ToxCoreState): ToxCoreState = {
    val eventData = ToxAvJni.toxavIterate(instanceNumber)
    if (eventData != null) { // scalastyle:ignore null
      val events = AvEvents.parseFrom(eventData)
      dispatchEvents(state, events)
    } else {
      state
    }
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

  @throws[ToxavBitRateSetException]
  override def setBitRate(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxavBitRateSet(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxavSendFrameException]
  override def audioSendFrame(friendNumber: Int, pcm: Array[Short], sampleCount: Int, channels: Int, samplingRate: Int): Unit =
    ToxAvJni.toxavAudioSendFrame(instanceNumber, friendNumber, pcm, sampleCount, channels, samplingRate)

  @throws[ToxavSendFrameException]
  override def videoSendFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte]): Unit =
    ToxAvJni.toxavVideoSendFrame(instanceNumber, friendNumber, width, height, y, u, v)

  override def callback(handler: ToxAvEventListener[ToxCoreState]): Unit = {
    this.eventListener = handler
  }

  def invokeAudioReceiveFrame(friendNumber: Int, pcm: Array[Short], channels: Int, samplingRate: Int): Unit =
    ToxAvJni.invokeAudioReceiveFrame(instanceNumber, friendNumber, pcm, channels, samplingRate)
  def invokeBitRateStatus(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.invokeBitRateStatus(instanceNumber, friendNumber, audioBitRate, videoBitRate)
  def invokeCall(friendNumber: Int, audioEnabled: Boolean, videoEnabled: Boolean): Unit =
    ToxAvJni.invokeCall(instanceNumber, friendNumber, audioEnabled, videoEnabled)
  def invokeCallState(friendNumber: Int, callState: util.Collection[ToxavFriendCallState]): Unit =
    ToxAvJni.invokeCallState(instanceNumber, friendNumber, convert(callState))
  def invokeVideoReceiveFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte], yStride: Int, uStride: Int, vStride: Int): Unit = // scalastyle:ignore line.size.limit
    ToxAvJni.invokeVideoReceiveFrame(instanceNumber, friendNumber, width, height, y, u, v, yStride, uStride, vStride)

}

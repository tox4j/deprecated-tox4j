package im.tox.tox4j.impl

import java.util

import com.google.protobuf.ByteString
import im.tox.tox4j.annotations.Nullable
import im.tox.tox4j.av.AbstractToxAv
import im.tox.tox4j.av.callbacks._
import im.tox.tox4j.av.enums.{ ToxCallControl, ToxCallState }
import im.tox.tox4j.av.exceptions._
import im.tox.tox4j.av.proto._

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
      case _                          => ToxAvJni.conversionError(kind.getClass.getName, kind.name)
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

  private val instanceNumber = ToxAvJni.toxAvNew(this.tox.instanceNumber)

  private val onClose = this.tox.addOnCloseCallback(close)

  private var callCallback = CallCallback.EMPTY
  private var callStateCallback = CallStateCallback.EMPTY
  private var receiveVideoFrameCallback = ReceiveVideoFrameCallback.EMPTY
  private var receiveAudioFrameCallback = ReceiveAudioFrameCallback.EMPTY

  override def close(): Unit = {
    tox.removeOnCloseCallback(onClose)
    ToxAvJni.toxAvKill(instanceNumber)
  }

  protected override def finalize(): Unit = {
    try {
      ToxAvJni.finalize(instanceNumber)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
    super.finalize()
  }

  override def iterationInterval: Int = {
    ToxAvJni.toxAvIterationInterval(instanceNumber)
  }

  override def iteration(): Unit = {
    Option(ToxAvJni.toxAvIteration(instanceNumber)).map(ByteString.copyFrom).map(AvEvents.parseFrom) match {
      case None =>
      case Some(AvEvents(
        call,
        callState,
        receiveAudioFrame,
        receiveVideoFrame)) =>
        call.foreach {
          case Call(friendNumber, audioEnabled, videoEnabled) =>
            callCallback.call(friendNumber, audioEnabled, videoEnabled)
        }
        callState.foreach {
          case CallState(friendNumber, state) =>
            callStateCallback.callState(friendNumber, util.Arrays.asList(state.map(ToxAvImpl.convert): _*))
        }
        receiveAudioFrame.foreach {
          case ReceiveAudioFrame(friendNumber, pcm, channels, samplingRate) =>
            receiveAudioFrameCallback.receiveAudioFrame(friendNumber, pcm.map(_.toShort).toArray, channels, samplingRate)
        }
        receiveVideoFrame.foreach {
          case ReceiveVideoFrame(friendNumber, width, height, y, u, v, a) =>
            receiveVideoFrameCallback.receiveVideoFrame(
              friendNumber,
              width,
              height,
              y.toByteArray,
              u.toByteArray,
              v.toByteArray,
              a.map(_.toByteArray).orNull
            )
        }
    }
  }

  @throws[ToxCallException]
  override def call(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxAvCall(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxAnswerException]
  override def answer(friendNumber: Int, audioBitRate: Int, videoBitRate: Int): Unit =
    ToxAvJni.toxAvAnswer(instanceNumber, friendNumber, audioBitRate, videoBitRate)

  @throws[ToxCallControlException]
  override def callControl(friendNumber: Int, control: ToxCallControl): Unit =
    ToxAvJni.toxAvCallControl(instanceNumber, friendNumber, control.ordinal)

  @throws[ToxBitRateException]
  override def setAudioBitRate(friendNumber: Int, bitRate: Int): Unit =
    ToxAvJni.toxAvSetAudioBitRate(instanceNumber, friendNumber, bitRate)

  @throws[ToxBitRateException]
  override def setVideoBitRate(friendNumber: Int, bitRate: Int): Unit =
    ToxAvJni.toxAvSetVideoBitRate(instanceNumber, friendNumber, bitRate)

  @throws[ToxSendFrameException]
  override def sendVideoFrame(friendNumber: Int, width: Int, height: Int, y: Array[Byte], u: Array[Byte], v: Array[Byte], @Nullable a: Array[Byte]): Unit =
    ToxAvJni.toxAvSendVideoFrame(instanceNumber, friendNumber, width, height, y, u, v, a)

  @throws[ToxSendFrameException]
  override def sendAudioFrame(friendNumber: Int, pcm: Array[Short], sampleCount: Int, channels: Int, samplingRate: Int): Unit =
    ToxAvJni.toxAvSendAudioFrame(instanceNumber, friendNumber, pcm, sampleCount, channels, samplingRate)

  override def callbackCall(callback: CallCallback): Unit = this.callCallback = callback
  override def callbackCallControl(callback: CallStateCallback): Unit = this.callStateCallback = callback
  override def callbackReceiveVideoFrame(callback: ReceiveVideoFrameCallback): Unit = this.receiveVideoFrameCallback = callback
  override def callbackReceiveAudioFrame(callback: ReceiveAudioFrameCallback): Unit = this.receiveAudioFrameCallback = callback

}

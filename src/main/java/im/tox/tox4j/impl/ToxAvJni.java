package im.tox.tox4j.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.av.callbacks.*;
import im.tox.tox4j.av.enums.ToxCallControl;
import im.tox.tox4j.av.enums.ToxCallState;
import im.tox.tox4j.av.exceptions.*;
import im.tox.tox4j.av.proto.Av;
import im.tox.tox4j.internal.Event;

@SuppressWarnings("checkstyle:nofinalizer")
public final class ToxAvJni implements ToxAv {

  static {
    System.loadLibrary("tox4j");
  }

  private final ToxCoreJni tox;
  private final int instanceNumber;
  private final Event.Id onClose;
  private CallCallback callCallback;
  private CallStateCallback callStateCallback;
  private RequestVideoFrameCallback requestVideoFrameCallback;
  private RequestAudioFrameCallback requestAudioFrameCallback;
  private ReceiveVideoFrameCallback receiveVideoFrameCallback;
  private ReceiveAudioFrameCallback receiveAudioFrameCallback;

  private static native int toxAvNew(int toxInstanceNumber) throws ToxAvNewException;

  /**
   * Initialise an A/V session for the existing Tox instance.
   *
   * @param tox An instance of the C-backed ToxCore implementation.
   * @throws ToxAvNewException If there was already an A/V session.
   */
  public ToxAvJni(ToxCoreJni tox) throws ToxAvNewException {
    this.tox = tox;
    this.instanceNumber = toxAvNew(this.tox.instanceNumber);

    this.onClose = this.tox.addOnCloseCallback(new Runnable() {
      @Override
      public void run() {
        ToxAvJni.this.close();
      }
    });
  }


  private static native void toxAvKill(int instanceNumber);

  @Override
  public void close() {
    tox.removeOnCloseCallback(onClose);
    toxAvKill(instanceNumber);
  }


  private static native void finalize(int instanceNumber);

  @Override
  public void finalize() throws Throwable {
    try {
      finalize(instanceNumber);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    super.finalize();
  }


  private static native int toxAvIterationInterval(int instanceNumber);

  @Override
  public int iterationInterval() {
    return toxAvIterationInterval(instanceNumber);
  }


  private static ToxCallState convert(Av.CallState.Kind kind) {
    switch (kind) {
      case RINGING: return ToxCallState.RINGING;
      case SENDING_NONE: return ToxCallState.SENDING_NONE;
      case SENDING_A: return ToxCallState.SENDING_A;
      case SENDING_V: return ToxCallState.SENDING_V;
      case SENDING_AV: return ToxCallState.SENDING_AV;
      case PAUSED: return ToxCallState.PAUSED;
      case END: return ToxCallState.END;
      case ERROR: return ToxCallState.ERROR;
    }
    throw new IllegalStateException("Bad enumerator: " + kind);
  }

  private static native byte[] toxAvIteration(int instanceNumber);

  @Override
  public void iteration() {
    byte[] events = toxAvIteration(instanceNumber);
    Av.AvEvents toxEvents;
    try {
      toxEvents = Av.AvEvents.parseFrom(events);
    } catch (InvalidProtocolBufferException e) {
      // This would be very bad, meaning something went wrong in our own C++ code.
      throw new RuntimeException(e);
    }

    if (callCallback != null) {
      for (Av.Call call : toxEvents.getCallList()) {
        callCallback.call(call.getFriendNumber(), call.getAudioEnabled(), call.getVideoEnabled());
      }
    }
    if (callStateCallback != null) {
      for (Av.CallState callState : toxEvents.getCallStateList()) {
        callStateCallback.callState(callState.getFriendNumber(), convert(callState.getState()));
      }
    }
    if (requestAudioFrameCallback != null) {
      for (Av.RequestAudioFrame requestAudioFrame : toxEvents.getRequestAudioFrameList()) {
        requestAudioFrameCallback.requestAudioFrame(requestAudioFrame.getFriendNumber());
      }
    }
    if (requestVideoFrameCallback != null) {
      for (Av.RequestVideoFrame requestVideoFrame : toxEvents.getRequestVideoFrameList()) {
        requestVideoFrameCallback.requestVideoFrame(requestVideoFrame.getFriendNumber());
      }
    }
    if (receiveAudioFrameCallback != null) {
      for (Av.ReceiveAudioFrame receiveAudioFrame : toxEvents.getReceiveAudioFrameList()) {
        short[] pcm = new short[receiveAudioFrame.getPcmCount()];
        for (int i = 0; i < pcm.length; i++) {
          pcm[i] = (short) receiveAudioFrame.getPcm(i);
        }
        receiveAudioFrameCallback.receiveAudioFrame(receiveAudioFrame.getFriendNumber(),
            pcm, receiveAudioFrame.getChannels(), receiveAudioFrame.getSamplingRate());
      }
    }
    if (receiveVideoFrameCallback != null) {
      for (Av.ReceiveVideoFrame receiveVideoFrame : toxEvents.getReceiveVideoFrameList()) {
        receiveVideoFrameCallback.receiveVideoFrame(
            receiveVideoFrame.getFriendNumber(),
            receiveVideoFrame.getWidth(),
            receiveVideoFrame.getHeight(),
            receiveVideoFrame.getY().toByteArray(),
            receiveVideoFrame.getU().toByteArray(),
            receiveVideoFrame.getV().toByteArray(),
            receiveVideoFrame.hasA() ? receiveVideoFrame.getA().toByteArray() : null
        );
      }
    }
  }


  private static native void toxAvCall(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate)
      throws ToxCallException;

  @Override
  public void call(int friendNumber, int audioBitRate, int videoBitRate) throws ToxCallException {
    toxAvCall(instanceNumber, friendNumber, audioBitRate, videoBitRate);
  }

  @Override
  public void callbackCall(@Nullable CallCallback callback) {
    this.callCallback = callback;
  }


  private static native void toxAvAnswer(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate)
      throws ToxAnswerException;

  @Override
  public void answer(int friendNumber, int audioBitRate, int videoBitRate) throws ToxAnswerException {
    toxAvAnswer(instanceNumber, friendNumber, audioBitRate, videoBitRate);
  }


  private static native void toxAvCallControl(int instanceNumber, int friendNumber, int control)
      throws ToxCallControlException;

  @Override
  public void callControl(int friendNumber, @NotNull ToxCallControl control) throws ToxCallControlException {
    toxAvCallControl(instanceNumber, friendNumber, control.ordinal());
  }

  @Override
  public void callbackCallControl(@Nullable CallStateCallback callback) {
    this.callStateCallback = callback;
  }


  private static native void toxAvSetAudioBitRate(int instanceNumber, int friendNumber, int audioBitRate)
      throws ToxBitRateException;

  @Override
  public void setAudioBitRate(int friendNumber, int bitRate) throws ToxBitRateException {
    toxAvSetAudioBitRate(instanceNumber, friendNumber, bitRate);
  }


  private static native void toxAvSetVideoBitRate(int instanceNumber, int friendNumber, int videoBitRate)
      throws ToxBitRateException;

  @Override
  public void setVideoBitRate(int friendNumber, int bitRate) throws ToxBitRateException {
    toxAvSetVideoBitRate(instanceNumber, friendNumber, bitRate);
  }

  @Override
  public void callbackRequestVideoFrame(@Nullable RequestVideoFrameCallback callback) {
    this.requestVideoFrameCallback = callback;
  }


  @SuppressWarnings("checkstyle:parametername")
  private static native void toxAvSendVideoFrame(
      int instanceNumber,
      int friendNumber,
      int width, int height,
      byte[] y, byte[] u, byte[] v, byte[] a
  ) throws ToxSendFrameException;

  @Override
  @SuppressWarnings("checkstyle:parametername")
  public void sendVideoFrame(
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a
  ) throws ToxSendFrameException {
    toxAvSendVideoFrame(instanceNumber, friendNumber, width, height, y, u, v, a);
  }

  @Override
  public void callbackRequestAudioFrame(@Nullable RequestAudioFrameCallback callback) {
    this.requestAudioFrameCallback = callback;
  }


  private static native void toxAvSendAudioFrame(
      int instanceNumber, int friendNumber, short[] pcm, int sampleCount, int channels, int samplingRate
  ) throws ToxSendFrameException;

  @Override
  public void sendAudioFrame(int friendNumber, @NotNull short[] pcm, int sampleCount, int channels, int samplingRate)
      throws ToxSendFrameException {
    toxAvSendAudioFrame(instanceNumber, friendNumber, pcm, sampleCount, channels, samplingRate);
  }

  @Override
  public void callbackReceiveVideoFrame(ReceiveVideoFrameCallback callback) {
    this.receiveVideoFrameCallback = callback;
  }

  @Override
  public void callbackReceiveAudioFrame(ReceiveAudioFrameCallback callback) {
    this.receiveAudioFrameCallback = callback;
  }

  @Override
  public void callback(@Nullable ToxAvEventListener handler) {
    callbackCall(handler);
    callbackCallControl(handler);
    callbackRequestAudioFrame(handler);
    callbackRequestVideoFrame(handler);
    callbackReceiveAudioFrame(handler);
    callbackReceiveVideoFrame(handler);
  }

}

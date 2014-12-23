package im.tox.tox4j;

import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.av.callbacks.*;
import im.tox.tox4j.av.enums.ToxCallControl;
import im.tox.tox4j.av.exceptions.*;

public final class ToxAvImpl implements ToxAv {

    private final ToxCoreImpl tox;
    private final int instanceNumber;
    private CallCallback callCallback;
    private CallControlCallback callControlCallback;
    private RequestVideoFrameCallback requestVideoFrameCallback;
    private RequestAudioFrameCallback requestAudioFrameCallback;
    private ReceiveVideoFrameCallback receiveVideoFrameCallback;
    private ReceiveAudioFrameCallback receiveAudioFrameCallback;

    /**
     * Calls kill() on every AV instance. This will invalidate all instances without notice, and should only be
     * used during testing or debugging. In particular, {@link im.tox.tox4j.ToxCoreImpl} will contain references to
     * destroyed AV instances.
     */
    static native void destroyAll();

    private static native int toxAvNew(int toxInstanceNumber) throws ToxAvNewException;

    public ToxAvImpl(ToxCore tox) throws ToxAvNewException {
        if (tox instanceof ToxCoreImpl) {
            this.tox = (ToxCoreImpl) tox;
            this.tox.av = this;
            instanceNumber = toxAvNew(this.tox.instanceNumber);
        } else {
            throw new IllegalArgumentException(
                    "This implementation of " + ToxAv.class.getName() +
                    " requires an instance of " + ToxCoreImpl.class.getName()
            );
        }
    }


    private static native void toxAvKill(int instanceNumber);

    @Override
    public void close() {
        tox.av = null;
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


    private static native void toxAvIteration(int instanceNumber);

    @Override
    public void iteration() {
        toxAvIteration(instanceNumber);
    }


    private static native void toxAvCall(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxCallException;

    @Override
    public void call(int friendNumber, int audioBitRate, int videoBitRate) throws ToxCallException {
        toxAvCall(instanceNumber, friendNumber, audioBitRate, videoBitRate);
    }

    @Override
    public void callbackCall(CallCallback callback) {
        this.callCallback = callback;
    }


    private static native void toxAvAnswer(int instanceNumber, int friendNumber, int audioBitRate, int videoBitRate) throws ToxAnswerException;

    @Override
    public void answer(int friendNumber, int audioBitRate, int videoBitRate) throws ToxAnswerException {
        toxAvAnswer(instanceNumber, friendNumber, audioBitRate, videoBitRate);
    }


    private static native void toxAvCallControl(int instanceNumber, int friendNumber, int control) throws ToxCallControlException;

    @Override
    public void callControl(int friendNumber, ToxCallControl control) throws ToxCallControlException {
        toxAvCallControl(instanceNumber, friendNumber, control.ordinal());
    }

    @Override
    public void callbackCallControl(CallControlCallback callback) {
        this.callControlCallback = callback;
    }


    private static native void toxAvSetAudioBitRate(int instanceNumber, int friendNumber, int audioBitRate) throws ToxBitRateException;

    @Override
    public void setAudioBitRate(int friendNumber, int bitRate) throws ToxBitRateException {
        toxAvSetAudioBitRate(instanceNumber, friendNumber, bitRate);
    }


    private static native void toxAvSetVideoBitRate(int instanceNumber, int friendNumber, int videoBitRate) throws ToxBitRateException;

    @Override
    public void setVideoBitRate(int friendNumber, int bitRate) throws ToxBitRateException {
        toxAvSetVideoBitRate(instanceNumber, friendNumber, bitRate);
    }

    @Override
    public void callbackRequestVideoFrame(RequestVideoFrameCallback callback) {
        this.requestVideoFrameCallback = callback;
    }


    private static native void toxAvSendVideoFrame(int instanceNumber, int friendNumber, int width, int height, byte[] y, byte[] u, byte[] v, byte[] a) throws ToxSendFrameException;

    @Override
    public void sendVideoFrame(int friendNumber, int width, int height, byte[] y, byte[] u, byte[] v, byte[] a) throws ToxSendFrameException {
        toxAvSendVideoFrame(instanceNumber, friendNumber, width, height, y, u, v, a);
    }

    @Override
    public void callbackRequestAudioFrame(RequestAudioFrameCallback callback) {
        this.requestAudioFrameCallback = callback;
    }


    private static native void toxAvSendAudioFrame(int instanceNumber, int friendNumber, byte[] pcm, int sampleCount, int channels, int samplingRate) throws ToxSendFrameException;

    @Override
    public void sendAudioFrame(int friendNumber, byte[] pcm, int sampleCount, int channels, int samplingRate) throws ToxSendFrameException {
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
}

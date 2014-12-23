package im.tox.tox4j.av.callbacks;

public interface ToxAvEventListener extends
        CallCallback,
        CallControlCallback,
        ReceiveAudioFrameCallback,
        ReceiveVideoFrameCallback,
        RequestAudioFrameCallback,
        RequestVideoFrameCallback
{
}

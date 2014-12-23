package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.AliceBobAvTest;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.av.enums.ToxCallState;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;
import org.junit.Assert;

import javax.sound.sampled.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class AudioCallTest extends AliceBobAvTest {

    private static abstract class AudioGenerator {

        private int t;

        public abstract byte getSample(int t);

        public short[] getFrame16(int count) {
            short[] frame = new short[count];
            for (int i = 0; i < frame.length; i++) {
                frame[i] = (short) (getSample(t++) << 8);
            }
            return frame;
        }

    }

    private static class MortalKombat extends AudioGenerator {

        public byte getSample(int t) {
            int a = (
                    2 * t % 4000 * (
                            "!!#!$!%$".charAt(t % 16000 / 2000) - 32 + (t % 64000 > 32000 ? 7 : 0)
                    )
            ) * (
                    t % 32000 > 16000 ? 2 : 1
            );
            int b = (
                    (t % 128000 > 64000 ? 2 * t : 0) * (
                            "%%%'%+''%%%$%+))%%%%'+'%$%%%$%%%$%%".charAt(t % 70000 / 2000) - 36
                    )
            );
            return (byte) (a | b);
        }

    }

    private static class ItCrowd extends AudioGenerator {

        @Override
        public byte getSample(int t) {
            t %= 96000 + 4000 * 8;

            int a = 1 / (128000 - t);
            int b;
            if (t > 96000)
                b = t % 4000 * ("'&(&*$,*".charAt(t % 96000 / 4000) - 32);
            else
                b = (
                        t % 2000 * (
                                "$$$&%%%''''%%%'&".charAt(t % 32000 / 2000) - 32 - (
                                        t > 28000 && t < 32000 ? 2 : 0
                                )
                        )
                ) / (
                        t % 8000 < 4000 ? 2 : 1
                );
            return (byte) (a | b);
        }
    }


    public static void main(String[] args) throws Exception {
        AudioGenerator generator = new ItCrowd();

        AudioFormat format = new AudioFormat(8000f, 8, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try (SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info)) {
            soundLine.open(format, 32000);
            soundLine.start();

            byte[] buffer = new byte[8];
            int t = 0;

            while (buffer.length != 0) {
                for (int n = 0; n < buffer.length; n++) {
                    buffer[n] = generator.getSample(t++);
                }
                soundLine.write(buffer, 0, buffer.length);
            }
        }
    }


    @Override
    protected ChatClient newAlice() {
        return new Alice();
    }

    private static class Alice extends AvClient {

        private final AudioGenerator generator = new ItCrowd();

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(ToxAv av) throws ToxException {
                        debug("calling " + getFriendName());
                        av.call(friendNumber, 100, 100);
                    }
                });
            }
        }

        @Override
        public void callState(int friendNumber, @NotNull ToxCallState state) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("call state is now " + state);
        }

        @Override
        public void call(final int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("received call from " + friendNumber);
            fail("Alice should not get a call");
        }

        @Override
        public void requestAudioFrame(final int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("request audio frame");
            addTask(new Task() {
                @Override
                public void perform(ToxAv av) throws ToxException {
                    int frameSize = 100;
                    av.sendAudioFrame(friendNumber, generator.getFrame16(frameSize), frameSize, 1, 8000);
                }
            });
        }

        @Override
        public void requestVideoFrame(int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("request video frame");
        }

    }


    @Override
    protected ChatClient newBob() {
        return new Bob();
    }

    private static class Bob extends AvClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
            }
        }

        @Override
        public void callState(int friendNumber, @NotNull ToxCallState state) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("call state is now " + state);
        }

        @Override
        public void call(final int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("received call from " + getFriendName());
            addTask(new Task() {
                {
                    // Wait for a few iterations before answering.
                    sleep(10);
                }

                @Override
                public void perform(ToxAv av) throws ToxException {
                    debug("answering call");
                    av.answer(friendNumber, 100, 100);
                }
            });
        }

        @Override
        public void requestAudioFrame(int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("request audio frame");
        }

        @Override
        public void requestVideoFrame(int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("request video frame");
        }

    }

}
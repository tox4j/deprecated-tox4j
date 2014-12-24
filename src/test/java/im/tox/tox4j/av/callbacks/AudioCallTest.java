package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.AliceBobAvTest;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import javax.sound.sampled.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class AudioCallTest extends AliceBobAvTest {

    private static abstract class AudioGenerator {

        public int length() {
            return 128000;
        }

        public abstract short getSample(int t);

        public int nextFrame16(int t, short[] frame) {
            for (int i = 0; i < frame.length; i++) {
                frame[i] = getSample(t++);
            }
            return t;
        }

    }

    private static final AudioGenerator MortalKombat = new AudioGenerator() {
        @Override
        public short getSample(int t) {
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
            return (short) ((a | b) << 8);
        }
    };

    private static final AudioGenerator ItCrowd = new AudioGenerator() {
        @Override
        public int length() {
            return 96000 + 4000 * 8;
        }

        @Override
        public short getSample(int t) {
            // Period
            t %= length();

            int a = 1 / (128000 - t);
            int b;
            if (t > 96000) {
                b = t % 4000 * ("'&(&*$,*".charAt(t % 96000 / 4000) - 32);
            } else {
                b = (
                        t % 2000 * (
                                "$$$&%%%''''%%%'&".charAt(t % 32000 / 2000) - 32 - (
                                        t > 28000 && t < 32000 ? 2 : 0
                                )
                        )
                ) / (
                        t % 8000 < 4000 ? 2 : 1
                );
            }
            return (short) ((a | b) << 8);
        }
    };


    private static final AudioGenerator Sine = new AudioGenerator() {
        @Override
        public short getSample(int t) {
            return (short) ((int) (Math.sin(t / (10000d / t)) * 128) << 8);
        }
    };


    private static final AudioGenerator Sine2 = new AudioGenerator() {
        @Override
        public short getSample(int t) {
            if (t % 2 == 0) {
                t = 1;
            }
            return (short) ((int) (Math.sin(t / (10000d / t)) * 128) << 8);
        }
    };


    // Selected audio generator for tests.
    private static final AudioGenerator AUDIO = MortalKombat;


    private static byte[] serialiseAudioFrame(short[] pcm) {
        byte[] buffer = new byte[pcm.length * 2];
        for (int i = 0; i < buffer.length; i += 2) {
            buffer[i] = (byte) (pcm[i / 2] >> 8);
            buffer[i + 1] = (byte) pcm[i / 2];
        }

        return buffer;
    }


    public static void main(String[] args) throws Exception {
        AudioFormat format = new AudioFormat(8000f, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try (SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info)) {
            soundLine.open(format, 32000);
            soundLine.start();

            byte[] buffer = new byte[160];
            int t = 0;

            while (true) {
                for (int n = 0; n < buffer.length; n += 2) {
                    short sample = AUDIO.getSample(t++);
                    buffer[n] = (byte) (sample >> 8);
                    buffer[n + 1] = (byte) sample;
                }
                soundLine.write(buffer, 0, buffer.length);
            }
        }
    }


    private static final int SAMPLING_RATE  = 8000;
    private static final int AUDIO_BIT_RATE = 96;
    private static final int CHANNELS       = 1;
    private static final int FRAME_SIZE     = 480;


    @NotNull
    @Override
    protected ChatClient newAlice() throws Exception {
        return new Alice();
    }

    private static class Alice extends AvClient {

        private int t = 0;

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxAv av) throws ToxException {
                        debug("calling " + getFriendName());
                        av.call(friendNumber, AUDIO_BIT_RATE, 0);
                    }
                });
            }
        }

        @Override
        public void requestAudioFrame(final int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            addTask(new Task() {
                @Override
                public void perform(@NotNull ToxAv av) throws ToxException {
                    short[] frame = new short[FRAME_SIZE];
                    t = AUDIO.nextFrame16(t, frame);
                    av.sendAudioFrame(friendNumber, frame, FRAME_SIZE, CHANNELS, SAMPLING_RATE);

                    if (t >= AUDIO.length()) {
                        finish();
                    }
                }
            });
        }

    }


    @NotNull
    @Override
    protected ChatClient newBob() throws Exception {
        return new Bob();
    }

    private static class Bob extends AvClient {

        private int t = 0;
        private final SourceDataLine soundLine;

        public Bob() throws Exception {
            AudioFormat format = new AudioFormat(SAMPLING_RATE, 16, CHANNELS, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            this.soundLine = (SourceDataLine) AudioSystem.getLine(info);
            soundLine.open(format, SAMPLING_RATE * CHANNELS * 4);
            soundLine.start();
        }

        @Override
        public void call(final int friendNumber) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            debug("received call from " + getFriendName());
            addTask(new Task() {
                // Wait for a few iterations before answering.
                { sleep(2); }
                @Override
                public void perform(@NotNull ToxAv av) throws ToxException {
                    debug("answering call");
                    av.answer(friendNumber, 64, 0);
                }
            });
        }

        @Override
        public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) {
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals(CHANNELS, channels);
            assertEquals(SAMPLING_RATE, samplingRate);
            assertEquals(FRAME_SIZE, pcm.length);

            byte[] buffer = serialiseAudioFrame(pcm);
            soundLine.write(buffer, 0, buffer.length);

            t += pcm.length;
            if (t >= AUDIO.length()) {
                finish();
            }
        }

    }

}
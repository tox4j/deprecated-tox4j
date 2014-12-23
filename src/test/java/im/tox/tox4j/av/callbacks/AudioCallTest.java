package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.AliceBobAvTest;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import javax.sound.sampled.*;

public class AudioCallTest extends AliceBobAvTest {

    @Override
    protected ChatClient newClient() {
        return new Client();
    }

    private interface AudioGenerator {

        byte getSample(int t);

    }

    private static class MortalKombat implements AudioGenerator {

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

    private static class ItCrowd implements AudioGenerator {

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
        SourceDataLine soundLine = (SourceDataLine)AudioSystem.getLine(info);
        soundLine.open(format, 32000);
        soundLine.start();

        byte[] buffer = new byte[8];
        int t = 0;

        while (buffer.length != 0) {
            for(int n = 0; n < buffer.length; n++) {
                buffer[n] = generator.getSample(t++);
            }
            soundLine.write(buffer, 0, buffer.length);
        }
    }


    private static class Client extends AvClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(ToxAv av) throws ToxException {
                        av.call(friendNumber, 100, 100);
                    }
                });
            }
        }

        @Override
        public void call(final int friendNumber) {
            debug("received call from " + friendNumber);
            addTask(new Task() {
                @Override
                public void perform(ToxAv av) throws ToxException {
                    av.answer(friendNumber, 100, 100);
                }
            });
        }
    }

}
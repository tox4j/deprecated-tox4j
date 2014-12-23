package im.tox.tox4j;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Test;

public final class ToxCorePlayground {

    public static void main(String[] args) throws ToxException {
        try (ToxCoreImpl tox = new ToxCoreImpl(new ToxOptions())) {
            tox.playground();
        }
    }

}

package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.av.ToxAvTestBase;
import im.tox.tox4j.av.exceptions.ToxAvNewException;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.exceptions.ToxNewException;
import org.junit.After;

public abstract class ToxAvImplTestBase extends ToxAvTestBase {

    @NotNull
    @Override
    protected final ToxCore newTox(ToxOptions options, byte[] data) throws ToxNewException {
        return new ToxCoreImpl(options, data);
    }

    @Override
    protected final ToxAv newToxAv(ToxCore tox) throws ToxAvNewException {
        return new ToxAvImpl(tox);
    }

}

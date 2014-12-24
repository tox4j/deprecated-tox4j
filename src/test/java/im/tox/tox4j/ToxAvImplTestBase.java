package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.av.ToxAvTestBase;
import im.tox.tox4j.av.exceptions.ToxAvNewException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.After;

public abstract class ToxAvImplTestBase extends ToxAvTestBase {

    @After
    public void tearDown() {
        // Make sure we leave the system in a clean state in the event of exceptions that prevented a cleanup.
        ToxAvImpl.destroyAll();
        ToxCoreImpl.destroyAll();
        System.gc();
    }

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

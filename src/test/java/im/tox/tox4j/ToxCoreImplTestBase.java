package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.After;

public abstract class ToxCoreImplTestBase extends ToxCoreTestBase {

    @After
    public void tearDown() {
        // Make sure we leave the system in a clean state in the event of exceptions that prevented a cleanup.
        ToxCoreImpl.destroyAll();
        System.gc();
    }

    @NotNull
    @Override
    protected final ToxCore newTox(ToxOptions options, byte[] data) throws ToxNewException {
        return new ToxCoreImpl(options, data);
    }

}

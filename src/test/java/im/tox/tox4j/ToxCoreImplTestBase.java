package im.tox.tox4j;

import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.After;

public class ToxCoreImplTestBase extends ToxCoreTestBase {

    @After
    public void tearDown() {
        // Make sure we leave the system in a clean state in the event of exceptions that prevented a cleanup.
        ToxCoreImpl.destroyAll();
        System.gc();
    }

    @Override
    protected final ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

}

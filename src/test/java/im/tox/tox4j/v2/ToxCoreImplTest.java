package im.tox.tox4j.v2;

import org.junit.After;

public class ToxCoreImplTest extends ToxCoreTest {

    @After
    public void tearDown() {
        // Make sure we leave the system in a clean state in the event of exceptions that prevented a cleanup.
        //ToxCoreImpl.destroyAll();
        System.gc();
    }

    @Override
    public ToxCore newTox(ToxOptions options) {
        return new ToxCoreImpl(options);
    }

}

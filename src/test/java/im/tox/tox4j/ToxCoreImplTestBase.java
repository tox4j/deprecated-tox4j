package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.exceptions.ToxNewException;
import org.junit.After;

public abstract class ToxCoreImplTestBase extends ToxCoreTestBase {

    private static final DhtNodeSelector dht = new DhtNodeSelector();

    @NotNull
    @Override
    protected final ToxCore newTox(ToxOptions options, byte[] data) throws ToxNewException {
        return new ToxCoreImpl(options, data);
    }

    @NotNull
    @Override
    protected DhtNode node() {
        return dht.node(this);
    }

}

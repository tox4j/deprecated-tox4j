package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.exceptions.ToxNewException;
import org.junit.After;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ToxCoreImplTestBase extends ToxCoreTestBase {

    private static final DhtNode node = new DhtNodeSelector().node(new ToxFactory() {
        @NotNull
        @Override
        public ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException {
            ToxOptions options = new ToxOptions();
            options.setIpv6Enabled(ipv6Enabled);
            options.setUdpEnabled(udpEnabled);
            return new ToxCoreImpl(options);
        }
    });

    private final Collection<ToxCoreImpl> toxes = new ArrayList<>();

    @After
    public void tearDown() {
        for (ToxCoreImpl tox : toxes) {
            tox.close();
        }
        toxes.clear();
        System.gc();
    }

    @NotNull
    @Override
    protected final ToxCore newTox(ToxOptions options, byte[] data) throws ToxNewException {
        ToxCoreImpl tox = new ToxCoreImpl(options, data);
        toxes.add(tox);
        return tox;
    }

    @NotNull
    @Override
    protected DhtNode node() {
        return node;
    }

}

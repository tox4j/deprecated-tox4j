package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.av.ToxAvTestBase;
import im.tox.tox4j.av.exceptions.ToxAvNewException;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.exceptions.ToxNewException;
import org.junit.After;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ToxAvImplTestBase extends ToxAvTestBase {

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
    private final Collection<ToxAvImpl> avs = new ArrayList<>();

    @After
    public void tearDown() {
        for (ToxAvImpl av : avs) {
            av.close();
        }
        avs.clear();
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

    @Override
    protected final ToxAv newToxAv(ToxCore tox) throws ToxAvNewException {
        ToxAvImpl av = new ToxAvImpl(tox);
        avs.add(av);
        return av;
    }

    @NotNull
    @Override
    protected DhtNode node() {
        return node;
    }

}

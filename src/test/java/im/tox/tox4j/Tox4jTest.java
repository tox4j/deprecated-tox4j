package im.tox.tox4j;

import org.junit.After;

import im.tox.tox4j.exceptions.ToxException;

public class Tox4jTest extends ToxSimpleChatTest {

    @After
    public void tearDown() {
        // Make sure we leave the system in a clean state in the event of exceptions that prevented a cleanup.
        Tox4j.destroyAll();
        System.gc();
    }

    @Override
    protected ToxSimpleChat newTox() throws ToxException {
        return new Tox4j();
    }

    @Override
    protected ToxSimpleChat newTox(boolean ipv6Enabled, boolean udpDisabled) throws ToxException {
        return new Tox4j(ipv6Enabled, udpDisabled);
    }

    @Override
    protected ToxSimpleChat newTox(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort) throws ToxException {
        return new Tox4j(ipv6Enabled, udpDisabled, proxyEnabled, proxyAddress, proxyPort);
    }

}
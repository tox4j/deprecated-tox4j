package im.tox.tox4j;

import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.*;

public class Tox4jTest extends ToxSimpleChatTest {

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
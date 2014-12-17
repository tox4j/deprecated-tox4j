package im.tox.tox4j.v2;

import im.tox.tox4j.v2.enums.ToxProxyType;
import im.tox.tox4j.v2.exceptions.ToxNewException;

public abstract class ToxCoreTestBase {

    protected static final boolean LOGGING = true;
    protected static final int TIMEOUT = 10000;
    protected static final int ITERATIONS = 500;


    protected abstract ToxCore newTox(ToxOptions options) throws ToxNewException;

    protected ToxCore newTox() throws ToxNewException {
        return newTox(new ToxOptions());
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException {
        ToxOptions options = new ToxOptions();
        options.setIpv6Enabled(ipv6Enabled);
        options.setUdpEnabled(udpEnabled);
        return newTox(options);
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled, ToxProxyType proxyType, String proxyAddress, int proxyPort) throws ToxNewException {
        ToxOptions options = new ToxOptions();
        options.setIpv6Enabled(ipv6Enabled);
        options.setUdpEnabled(udpEnabled);
        options.enableProxy(proxyType, proxyAddress, proxyPort);
        return newTox(options);
    }

}

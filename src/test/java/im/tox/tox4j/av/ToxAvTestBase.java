package im.tox.tox4j.av;

import im.tox.tox4j.ToxConstants;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.exceptions.ToxAvNewException;
import im.tox.tox4j.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.exceptions.ToxBootstrapException;
import im.tox.tox4j.exceptions.ToxFriendAddException;
import im.tox.tox4j.exceptions.ToxNewException;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

public abstract class ToxAvTestBase extends ToxCoreTestBase {

    protected abstract ToxAv newToxAv(ToxCore tox) throws ToxAvNewException;

    protected final ToxAv newToxAv(ToxOptions options, byte[] data) throws ToxNewException, ToxAvNewException {
        return newToxAv(newTox(options, data));
    }

    protected final ToxAv newToxAv() throws ToxNewException, ToxAvNewException {
        return newToxAv(newTox());
    }

    protected final ToxAv newToxAv(byte[] data) throws ToxNewException, ToxAvNewException {
        return newToxAv(newTox(data));
    }

    protected final ToxAv newToxAv(ToxOptions options) throws ToxNewException, ToxAvNewException {
        return newToxAv(newTox(options));
    }

    protected final ToxAv newToxAv(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException, ToxAvNewException {
        return newToxAv(newTox(ipv6Enabled, udpEnabled));
    }

    protected final ToxAv newToxAv(boolean ipv6Enabled, boolean udpEnabled, ToxProxyType proxyType, String proxyAddress, int proxyPort) throws ToxNewException, ToxAvNewException {
        return newToxAv(newTox(ipv6Enabled, udpEnabled, proxyType, proxyAddress, proxyPort));
    }

}

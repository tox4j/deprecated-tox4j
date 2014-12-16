package im.tox.tox4j.v2;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.v2.enums.ToxProxyType;

import java.io.Closeable;

/**
 * This class contains all the startup options for Tox. You can either allocate
 * this object yourself, and pass it to tox_options_default, or call
 * tox_options_new to get a new default options object.
 */
public class ToxOptions {

    /**
     * The type of socket to create.
     *
     * If this is set to false, an IPv4 socket is created, which subsequently
     * only allows IPv4 communication.
     * If it is set to true, an IPv6 socket is created, allowing both IPv4 and
     * IPv6 communication.
     */
    public boolean ipv6Enabled;

    /**
     * Enable the use of UDP communication when available.
     *
     * Setting this to false will force Tox to use TCP only. Communications will
     * need to be relayed through a TCP relay node, potentially slowing them down.
     * Disabling UDP support is necessary when using anonymous proxies or Tor.
     */
    public boolean udpEnabled;

    /**
     * Pass communications through a proxy.
     */
    public ToxProxyType proxyType;

    /**
     * The IP address or DNS name of the proxy to be used.
     *
     * If used, this must be non-NULL and be a valid DNS name. The name must not
     * exceed 255 characters.
     *
     * This member is ignored (it can be NULL) if proxyEnabled is false.
     */
    public String proxyAddress;

    /**
     * The port to use to connect to the proxy server.
     *
     * Ports must be in the range (1, 65535). The value is ignored if
     * proxyEnabled is false.
     */
    public int proxyPort;

}

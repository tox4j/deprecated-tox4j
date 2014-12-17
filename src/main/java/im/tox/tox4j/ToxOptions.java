package im.tox.tox4j;

import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.exceptions.ToxNewException;

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
    private boolean ipv6Enabled = true;

    /**
     * Enable the use of UDP communication when available.
     *
     * Setting this to false will force Tox to use TCP only. Communications will
     * need to be relayed through a TCP relay node, potentially slowing them down.
     * Disabling UDP support is necessary when using anonymous proxies or Tor.
     */
    private boolean udpEnabled = true;

    /**
     * Pass communications through a proxy.
     */
    private ToxProxyType proxyType = ToxProxyType.NONE;

    /**
     * The IP address or DNS name of the proxy to be used.
     *
     * If used, this must be non-NULL and be a valid DNS name. The name must not
     * exceed 255 characters.
     *
     * This member is ignored (it can be NULL) if proxyEnabled is false.
     */
    private String proxyAddress = null;

    /**
     * The port to use to connect to the proxy server.
     *
     * Ports must be in the range (1, 65535). The value is ignored if
     * proxyEnabled is false.
     */
    private int proxyPort = 0;


    public boolean isIpv6Enabled() {
        return ipv6Enabled;
    }

    public void setIpv6Enabled(boolean ipv6Enabled) {
        this.ipv6Enabled = ipv6Enabled;
    }


    public boolean isUdpEnabled() {
        return udpEnabled;
    }

    public void setUdpEnabled(boolean udpEnabled) {
        this.udpEnabled = udpEnabled;
    }


    public ToxProxyType getProxyType() {
        return proxyType;
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public int getProxyPort() {
        return proxyPort;
    }


    public void enableProxy(ToxProxyType type, String address, int port) throws ToxNewException {
        if (port < 0) {
            throw new ToxNewException(ToxNewException.Code.PROXY_BAD_PORT);
        }
        if (port > 65535) {
            throw new ToxNewException(ToxNewException.Code.PROXY_BAD_PORT);
        }
        // The rest is not checked here, because the C++ code already checks it, and we want to exercise that.
        proxyType = type;
        proxyAddress = address;
        proxyPort = port;
    }

    public void disableProxy() {
        proxyType = ToxProxyType.NONE;
        proxyAddress = null;
        proxyPort = 0;
    }

}

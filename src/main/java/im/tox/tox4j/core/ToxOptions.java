package im.tox.tox4j.core;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.core.enums.ToxProxyType;
import im.tox.tox4j.core.exceptions.ToxNewException;

/**
 * This class contains all the startup options for Tox. You can either allocate
 * this object yourself, and pass it to tox_options_default, or call
 * tox_options_new to get a new default options object.
 */
public class ToxOptions {

  /**
   * The type of socket to create.
   *
   * <p>
   * If this is set to false, an IPv4 socket is created, which subsequently
   * only allows IPv4 communication.
   * If it is set to true, an IPv6 socket is created, allowing both IPv4 and
   * IPv6 communication.
   * </p>
   */
  public final boolean ipv6Enabled;

  /**
   * Enable the use of UDP communication when available.
   *
   * <p>
   * Setting this to false will force Tox to use TCP only. Communications will
   * need to be relayed through a TCP relay node, potentially slowing them down.
   * Disabling UDP support is necessary when using anonymous proxies or Tor.
   * </p>
   */
  public final boolean udpEnabled;

  /**
   * Pass communications through a proxy.
   */
  @NotNull
  public final ToxProxyType proxyType;

  /**
   * The IP address or DNS name of the proxy to be used.
   *
   * <p>
   * If used, this must be non-NULL and be a valid DNS name. The name must not
   * exceed 255 characters.
   * </p>
   *
   * <p>
   * This member is ignored (it can be NULL) if proxyEnabled is false.
   * </p>
   */
  @Nullable
  public final String proxyAddress;

  /**
   * The port to use to connect to the proxy server.
   *
   * <p>
   * Ports must be in the range (1, 65535). The value is ignored if
   * proxyEnabled is false.
   * </p>
   */
  public final int proxyPort;


  /**
   * Set all public members according to the parameters passed.
   *
   * @param ipv6Enabled See {@link ToxOptions#ipv6Enabled}.
   * @param udpEnabled See {@link ToxOptions#udpEnabled}.
   * @param proxyType See {@link ToxOptions#proxyType}.
   * @param proxyAddress See {@link ToxOptions#proxyAddress}.
   * @param proxyPort See {@link ToxOptions#proxyPort}.
   * @throws ToxNewException if the proxy port is out of range when proxyType was not NONE.
   */
  public ToxOptions(
      boolean ipv6Enabled, boolean udpEnabled,
      @NotNull ToxProxyType proxyType, @Nullable String proxyAddress, int proxyPort
  ) throws ToxNewException {
    if (proxyType != ToxProxyType.NONE) {
      if (proxyPort < 0) {
        throw new ToxNewException(ToxNewException.Code.PROXY_BAD_PORT);
      }
      if (proxyPort > 65535) {
        throw new ToxNewException(ToxNewException.Code.PROXY_BAD_PORT);
      }
    }
    this.ipv6Enabled = ipv6Enabled;
    this.udpEnabled = udpEnabled;
    this.proxyType = proxyType;
    this.proxyAddress = proxyAddress;
    this.proxyPort = proxyPort;
  }

  public ToxOptions(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException {
    this(ipv6Enabled, udpEnabled, ToxProxyType.NONE, null, 0);
  }

  public ToxOptions() throws ToxNewException {
    this(true, true);
  }

  @NotNull
  public ToxOptions enableProxy(
      @NotNull ToxProxyType proxyType, @NotNull String proxyAddress, int proxyPort
  ) throws ToxNewException {
    return new ToxOptions(ipv6Enabled, udpEnabled, proxyType, proxyAddress, proxyPort);
  }
}

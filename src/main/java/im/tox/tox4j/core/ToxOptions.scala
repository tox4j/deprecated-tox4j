package im.tox.tox4j.core

import im.tox.tox4j.core.enums.ToxProxyType

/**
 * This class contains all the startup options for Tox.
 *
 * @param ipv6Enabled The type of socket to create.
 *
 * If this is set to false, an IPv4 socket is created, which subsequently only allows IPv4
 * communication. If it is set to true, an IPv6 socket is created, allowing both IPv4 and IPv6
 * communication.
 *
 * @param udpEnabled Enable the use of UDP communication when available.
 * Setting this to false will force Tox to use TCP only. Communications will
 * need to be relayed through a TCP relay node, potentially slowing them down.
 * Disabling UDP support is necessary when using anonymous proxies or Tor.
 *
 * @param proxyType Pass communications through a proxy.
 *
 * @param proxyAddress The IP address or DNS name of the proxy to be used.
 * If used, this must be a valid DNS name. The name must not exceed [[ToxConstants.MAX_HOSTNAME_LENGTH]] characters.
 * This member is ignored (it can be anything) if [[proxyType]] is [[ToxProxyType.NONE]].
 *
 * @param proxyPort The port to use to connect to the proxy server.
 * Ports must be in the range (1, 65535). The value is ignored if [[proxyType]] is [[ToxProxyType.NONE]].
 *
 * @param saveData Optional serialised instance data from [[ToxCore#load]]
 */
final case class ToxOptions(
    ipv6Enabled: Boolean = true,
    udpEnabled: Boolean = true,
    proxyType: ToxProxyType = ToxProxyType.NONE,
    proxyAddress: String = "localhost",
    proxyPort: Int = 8080,
    saveData: Array[Byte] = Array.ofDim(0)
) {
  if (proxyType != ToxProxyType.NONE) {
    require(proxyPort > 0, "Proxy port cannot be 0 or negative")
    require(proxyPort <= 65535, "Proxy port out of range")
  }
}

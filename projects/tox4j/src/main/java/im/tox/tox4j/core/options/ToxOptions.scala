package im.tox.tox4j.core.options

import im.tox.tox4j.core.{ToxCore, ToxCoreConstants}

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
 *
 * Setting this to false will force Tox to use TCP only. Communications will
 * need to be relayed through a TCP relay node, potentially slowing them down.
 * Disabling UDP support is necessary when using anonymous proxies or Tor. If
 * UDP is enabled, it will be used, even if a proxy is configured. The proxy
 * is only used for TCP connections.
 *
 * @param proxy Pass communications through a proxy.
 * @param startPort The start port of the inclusive port range to attempt to use.
 *
 * If both startPort and endPort are 0, the default port range will be
 * used: [33445, 33545].
 *
 * If either startPort or endPort is 0 while the other is non-zero, the
 * non-zero port will be the only port in the range.
 *
 * @param endPort The end port of the inclusive port range to attempt to use.
 * @param tcpPort The port to use for the TCP server. If 0, the tcp server is disabled.
 * @param saveData Optional serialised instance data from [[ToxCore.load]] or secret key from [[ToxCore.getSecretKey]].
 * @param fatalErrors Whether exceptions in [[ToxCore.iterate]] should abort the iteration.
 */
final case class ToxOptions(
    ipv6Enabled: Boolean = true,
    udpEnabled: Boolean = true,
    proxy: ProxyOptions = ProxyOptions.None,
    startPort: Int = ToxCoreConstants.DefaultStartPort,
    endPort: Int = ToxCoreConstants.DefaultEndPort,
    tcpPort: Int = ToxCoreConstants.DefaultTcpPort,
    saveData: SaveDataOptions = SaveDataOptions.None,
    fatalErrors: Boolean = true
) {
  private def requireValidPort(name: String, port: Int): Unit = {
    require(port >= 0 && port <= 65535, s"$name port should be a valid 16 bit positive integer")
  }
  require(startPort <= endPort, s"startPort ($startPort) should not be greater than endPort ($endPort)")
  requireValidPort("Start", startPort)
  requireValidPort("End", endPort)
  requireValidPort("TCP", tcpPort)
}

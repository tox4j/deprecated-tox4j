package im.tox.tox4j

final case class DhtNode(ipv4: String, ipv6: String, udpPort: Int, tcpPort: Int, dhtId: Array[Byte]) {

  def this(ipv4: String, ipv6: String, udpPort: Int, tcpPort: Int, dhtId: String) {
    this(ipv4, ipv6, udpPort, tcpPort, ToxCoreTestBase.parsePublicKey(dhtId))
  }

  def this(ipv4: String, ipv6: String, port: Int, dhtId: String) {
    this(ipv4, ipv6, port, port, dhtId)
  }

}

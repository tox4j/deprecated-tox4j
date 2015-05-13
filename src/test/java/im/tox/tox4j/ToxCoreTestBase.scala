package im.tox.tox4j

import java.io.{ Closeable, IOException }
import java.net.{ InetAddress, Socket }
import java.util.Random

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.callbacks.ConnectionStatusCallback
import im.tox.tox4j.core.enums.{ ToxConnection, ToxProxyType }
import im.tox.tox4j.core.exceptions.{ ToxBootstrapException, ToxFriendAddException, ToxNewException }
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory, ToxOptions }
import org.junit.Assume.{ assumeNotNull, assumeTrue }
import org.scalatest.junit.JUnitSuite

object ToxCoreTestBase {

  private[tox4j] val nodeCandidates = Seq(
    new DhtNode("192.254.75.102", "2607:5600:284::2", 33445, "951C88B7E75C867418ACDB5D273821372BB5BD652740BCDF623A4FA293E75D2F"),
    new DhtNode("144.76.60.215", "2a01:4f8:191:64d6::1", 33445, "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F"),
    new DhtNode("23.226.230.47", "2604:180:1::3ded:b280", 33445, "A09162D68618E742FFBCA1C2C70385E6679604B2D80EA6E84AD0996A1AC8A074"),
    new DhtNode("178.62.125.224", "2a03:b0c0:1:d0::178:6001", 33445, "10B20C49ACBD968D7C80F2E8438F92EA51F189F4E70CFBBB2C2C8C799E97F03E"),
    new DhtNode("178.21.112.187", "2a02:2308::216:3eff:fe82:eaef", 33445, "4B2C19E924972CB9B57732FB172F8A8604DE13EEDA2A6234E348983344B23057"),
    new DhtNode("195.154.119.113", "2001:bc8:3698:101::1", 33445, "E398A69646B8CEACA9F0B84F553726C1C49270558C57DF5F3C368F05A7D71354"),
    new DhtNode("192.210.149.121", null, 33445, "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67"),
    new DhtNode("104.219.184.206", null, 443, "8CD087E31C67568103E8C2A28653337E90E6B8EDA0D765D57C6B5172B4F1F04C"),
    new DhtNode("76.191.23.96", null, 33445, "93574A3FAB7D612FEA29FD8D67D3DD10DFD07A075A5D62E8AF3DD9F5D0932E11"),
    new DhtNode("178.62.250.138", "2a03:b0c0:2:d0::16:1", 33445, "788236D34978D1D5BD822F0A5BEBD2C53C64CC31CD3149350EE27D4D9A2F9B6B"),
    new DhtNode("78.225.128.39", null, 33445, "7A2306BFBA665E5480AE59B31E116BE9C04DCEFE04D9FE25082316FA34B4DA0C"),
    new DhtNode("130.133.110.14", "2001:6f8:1c3c:babe::14:1", 33445, "461FA3776EF0FA655F1A05477DF1B3B614F7D6B124F7DB1DD4FE3C08B03B640F"),
    new DhtNode("104.167.101.29", null, 33445, "5918AC3C06955962A75AD7DF4F80A5D7C34F7DB9E1498D2E0495DE35B3FE8A57"),
    new DhtNode("195.154.109.148", null, 33445, "391C96CB67AE893D4782B8E4495EB9D89CF1031F48460C06075AA8CE76D50A21 "),
    new DhtNode("192.3.173.88", null, 33445, "3E1FFDEB667BFF549F619EC6737834762124F50A89C8D0DBF1DDF64A2DD6CD1B"),
    new DhtNode("205.185.116.116", null, 33445, "A179B09749AC826FF01F37A9613F6B57118AE014D4196A0E1105A98F93A54702"),
    new DhtNode("198.98.51.198", "2605:6400:1:fed5:22:45af:ec10:f329", 33445, "1D5A5F2F5D6233058BF0259B09622FB40B482E4FA0931EB8FD3AB8E7BF7DAF6F"),
    new DhtNode("80.232.246.79", null, 33445, "0B8DCEAA7BDDC44BB11173F987CAE3566A2D7057D8DD3CC642BD472B9391002A")
  )

  protected[tox4j] class ToxList(factory: ToxCoreTestBase, count: Int) extends Closeable {

    private val toxes = new Array[ToxCore](count)
    private val connected = new Array[ToxConnection](count)

    (0 until count) foreach { i =>
      connected(i) = ToxConnection.NONE

      toxes(i) = factory.newTox()
      toxes(i).callbackConnectionStatus(new ConnectionStatusCallback {
        override def connectionStatus(connectionStatus: ToxConnection) {
          connected(i) = connectionStatus
        }
      })
    }

    def close() {
      toxes.foreach(_.close())
    }

    def isAllConnected: Boolean = connected.forall(_ != ToxConnection.NONE)
    def isAnyConnected: Boolean = connected.exists(_ != ToxConnection.NONE)

    def iteration(): Unit = toxes.foreach(_.iteration())

    def iterationInterval: Int = toxes.map(_.iterationInterval()).max

    def get(index: Int): ToxCore = toxes(index)
    def size: Int = toxes.length

  }

  private[tox4j] def entropy(@NotNull data: Array[Byte]): Double = {
    val frequencies = new Array[Int](256)
    for (b <- data) {
      frequencies(127 - b) += 1
    }
    var entropy = 0.0
    for (frequency <- frequencies) {
      if (frequency != 0) {
        val probability = frequency.toDouble / data.length
        entropy -= probability * (Math.log(probability) / Math.log(256))
      }
    }
    entropy
  }

  @NotNull protected def randomBytes(length: Int): Array[Byte] = {
    val array = new Array[Byte](length)
    new Random().nextBytes(array)
    array
  }

  @NotNull def readablePublicKey(@NotNull id: Array[Byte]): String = {
    val str = new StringBuilder
    id foreach { c => str.append(f"$c%02X") }
    str.toString()
  }

  @NotNull def parsePublicKey(@NotNull id: String): Array[Byte] = {
    val publicKey = new Array[Byte](id.length / 2)
    (0 until publicKey.length) foreach { i =>
      publicKey(i) = ((fromHexDigit(id.charAt(i * 2)) << 4) + fromHexDigit(id.charAt(i * 2 + 1))).toByte
    }
    publicKey
  }

  private def fromHexDigit(c: Char): Byte = {
    (c match {
      case _ if c >= '0' && c <= '9' => c - '0'
      case _ if c >= 'A' && c <= 'F' => c - 'A' + 10
      case _ if c >= 'a' && c <= 'f' => c - 'a' + 10
      case _ =>
        throw new IllegalArgumentException("Non-hex digit character: " + c)
    }).toByte
  }

  protected def assumeConnection(ip: String, port: Int) {
    var socket: Socket = null
    try {
      socket = new Socket(InetAddress.getByName(ip), port)
      assumeNotNull(socket.getInputStream)
    } catch {
      case e: IOException =>
        assumeTrue("A network connection can't be established to " + ip + ':' + port + ": " + e.getMessage, false)
    } finally {
      if (socket != null) {
        socket.close()
      }
    }
  }

  protected[tox4j] def assumeIPv4() {
    assumeConnection("8.8.8.8", 53)
  }

  protected[tox4j] def assumeIPv6() {
    assumeConnection("2001:4860:4860::8888", 53)
  }
}

abstract class ToxCoreTestBase extends JUnitSuite {

  @NotNull protected def node: DhtNode

  @NotNull
  @throws(classOf[ToxNewException])
  @Deprecated
  protected def newTox(options: ToxOptions, data: Array[Byte]): ToxCore

  @NotNull
  @throws(classOf[ToxNewException])
  @Deprecated
  protected final def newTox(): ToxCore = {
    newTox(new ToxOptions, null)
  }

  @NotNull
  @throws(classOf[ToxNewException])
  @Deprecated
  protected final def newTox(data: Array[Byte]): ToxCore = {
    newTox(new ToxOptions, data)
  }

  @NotNull
  @throws(classOf[ToxNewException])
  @Deprecated
  protected final def newTox(options: ToxOptions): ToxCore = {
    newTox(options, null)
  }

  @NotNull
  @throws(classOf[ToxNewException])
  @Deprecated
  protected final def newTox(ipv6Enabled: Boolean, udpEnabled: Boolean): ToxCore = {
    newTox(new ToxOptions(ipv6Enabled, udpEnabled), null)
  }

  @NotNull
  @throws(classOf[ToxNewException])
  @Deprecated
  protected final def newTox(ipv6Enabled: Boolean, udpEnabled: Boolean, proxyType: ToxProxyType, proxyAddress: String, proxyPort: Int): ToxCore = {
    newTox(new ToxOptions(ipv6Enabled, udpEnabled, proxyType, proxyAddress, proxyPort), null)
  }

  @throws(classOf[ToxNewException])
  @throws(classOf[ToxFriendAddException])
  protected def addFriends(@NotNull tox: ToxCore, count: Int): Int = {
    if (count < 1) {
      throw new IllegalArgumentException("Cannot add less than 1 friend: " + count)
    }
    val message = "heyo".getBytes
    (0 until count).map { (i: Int) =>
      ToxCoreFactory.withTox { friend =>
        tox.addFriendNoRequest(friend.getPublicKey)
      }
    }.last
  }

  @NotNull
  @throws(classOf[ToxBootstrapException])
  private[tox4j] def bootstrap(useIPv6: Boolean, udpEnabled: Boolean, @NotNull tox: ToxCore): ToxCore = {
    tox.bootstrap(
      if (useIPv6) node.ipv6 else node.ipv4,
      if (udpEnabled) node.udpPort else node.tcpPort,
      node.dhtId
    )
    tox
  }

}

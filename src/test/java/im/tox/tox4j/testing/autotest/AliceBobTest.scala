package im.tox.tox4j.testing.autotest

import im.tox.tox4j.TestConstants._
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.{ SocksServer, ToxCoreTestBase }
import org.junit.Assert._
import org.junit.{ AssumptionViolatedException, Test }

abstract class AliceBobTest extends AliceBobTestBase {

  private def exhaustiveNetworkTests(): Unit = {
    throw new AssumptionViolatedException("not running")
  }

  private def withBootstrappedTox(ipv6Enabled: Boolean, udpEnabled: Boolean)(f: ToxCore => Unit): Unit = {
    ToxCoreFactory.withTox(ipv6Enabled, udpEnabled) { tox =>
      bootstrap(ipv6Enabled, udpEnabled, tox)
      f(tox)
    }
  }

  private def runAliceBobTest_Socks(ipv6Enabled: Boolean, udpEnabled: Boolean): Unit = {
    if (ipv6Enabled) {
      ToxCoreTestBase.assumeIPv6()
    } else {
      ToxCoreTestBase.assumeIPv4()
    }

    val proxy = SocksServer.run { proxy =>
      runAliceBobTest { f =>
        ToxCoreFactory.withTox(ipv6Enabled, udpEnabled, new ProxyOptions.Socks5(proxy.getAddress, proxy.getPort)) { tox =>
          bootstrap(ipv6Enabled, udpEnabled, tox)
          f(tox)
        }
      }
      proxy
    }
    assertEquals(2, proxy.getAccepted)
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Udp4(): Unit = {
    runAliceBobTest(ToxCoreFactory.withTox(false, true))
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Udp6(): Unit = {
    exhaustiveNetworkTests()
    runAliceBobTest(ToxCoreFactory.withTox(true, true))
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Tcp4(): Unit = {
    exhaustiveNetworkTests()
    ToxCoreTestBase.assumeIPv4()
    runAliceBobTest(withBootstrappedTox(false, false))
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Tcp6(): Unit = {
    exhaustiveNetworkTests()
    ToxCoreTestBase.assumeIPv6()
    runAliceBobTest(withBootstrappedTox(true, false))
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Socks_Udp4(): Unit = {
    exhaustiveNetworkTests()
    runAliceBobTest_Socks(false, true)
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Socks_Udp6(): Unit = {
    exhaustiveNetworkTests()
    runAliceBobTest_Socks(true, true)
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Socks_Tcp4(): Unit = {
    exhaustiveNetworkTests()
    runAliceBobTest_Socks(false, false)
  }

  @Test(timeout = TIMEOUT)
  final def runAliceBobTest_Socks_Tcp6(): Unit = {
    exhaustiveNetworkTests()
    runAliceBobTest_Socks(true, false)
  }

}

package im.tox.tox4j.testing.autotest

import im.tox.tox4j.TestConstants._
import im.tox.tox4j.core.options.ProxyOptions
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import im.tox.tox4j.{ SocksServer, ToxCoreTestBase }
import org.junit.Assert._
import org.scalatest.concurrent.Timeouts
import org.scalatest.exceptions.TestFailedDueToTimeoutException
import org.scalatest.time.SpanSugar._

import scala.language.postfixOps

abstract class AliceBobTest extends AliceBobTestBase with Timeouts {

  protected def ignoreTimeout = false

  protected def enableUdp = true
  protected def enableTcp = false
  protected def enableIpv4 = true
  protected def enableIpv6 = false
  protected def enableHttp = false
  protected def enableSocks = false

  private def withBootstrappedTox(ipv6Enabled: Boolean, udpEnabled: Boolean, proxyOptions: ProxyOptions.Type = ProxyOptions.None)(f: ToxCore => Unit): Unit = {
    ToxCoreFactory.withTox(ipv6Enabled, udpEnabled, proxyOptions) { tox =>
      bootstrap(ipv6Enabled, udpEnabled, tox)
      f(tox)
    }
  }

  private def runAliceBobTest_Direct(withTox: => (ToxCore => Unit) => Unit): Unit = {
    failAfter(TIMEOUT millis) {
      runAliceBobTest(withTox)
    }
  }

  private def runAliceBobTest_Socks(ipv6Enabled: Boolean, udpEnabled: Boolean): Unit = {
    val proxy = SocksServer.withServer { proxy =>
      failAfter(TIMEOUT millis) {
        runAliceBobTest(withBootstrappedTox(ipv6Enabled, udpEnabled, new ProxyOptions.Socks5(proxy.getAddress, proxy.getPort)))
      }
      proxy
    }
    if (!udpEnabled) {
      assertEquals(2, proxy.getAccepted)
    }
  }

  test("UDP4") {
    assume(enableUdp)
    assume(enableIpv4)
    try {
      runAliceBobTest_Direct(ToxCoreFactory.withTox(ipv6Enabled = false, udpEnabled = true))
    } catch {
      case e: TestFailedDueToTimeoutException if ignoreTimeout =>
        cancel(s"Test timed out after $TIMEOUT millis", e)
    }
  }

  test("UDP6") {
    assume(enableUdp)
    assume(enableIpv6)
    failAfter(TIMEOUT millis) {
      runAliceBobTest_Direct(ToxCoreFactory.withTox(ipv6Enabled = true, udpEnabled = true))
    }
  }

  test("TCP4") {
    assume(enableTcp)
    assume(enableIpv4)
    assume(ToxCoreTestBase.checkIPv4.isEmpty)
    failAfter(TIMEOUT millis) {
      runAliceBobTest_Direct(withBootstrappedTox(ipv6Enabled = false, udpEnabled = false))
    }
  }

  test("TCP6") {
    assume(enableTcp)
    assume(enableIpv6)
    assume(ToxCoreTestBase.checkIPv6.isEmpty)
    failAfter(TIMEOUT millis) {
      runAliceBobTest_Direct(withBootstrappedTox(ipv6Enabled = true, udpEnabled = false))
    }
  }

  test("UDP4+SOCKS5") {
    assume(enableUdp)
    assume(enableIpv4)
    assume(enableSocks)
    assume(ToxCoreTestBase.checkIPv4.isEmpty)
    runAliceBobTest_Socks(ipv6Enabled = false, udpEnabled = true)
  }

  test("UDP6+SOCKS5") {
    assume(enableUdp)
    assume(enableIpv6)
    assume(enableSocks)
    assume(ToxCoreTestBase.checkIPv6.isEmpty)
    runAliceBobTest_Socks(ipv6Enabled = true, udpEnabled = true)
  }

  test("TCP4+SOCKS5") {
    assume(enableTcp)
    assume(enableIpv4)
    assume(enableSocks)
    assume(ToxCoreTestBase.checkIPv4.isEmpty)
    runAliceBobTest_Socks(ipv6Enabled = false, udpEnabled = false)
  }

  test("TCP6+SOCKS5") {
    assume(enableTcp)
    assume(enableIpv6)
    assume(enableSocks)
    assume(ToxCoreTestBase.checkIPv6.isEmpty)
    runAliceBobTest_Socks(ipv6Enabled = true, udpEnabled = false)
  }

}

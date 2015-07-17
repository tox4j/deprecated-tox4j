package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.options.ProxyOptions
import org.junit.Assert.assertEquals
import org.junit.Test

@deprecated("This uses newTox because it needs to pass constructor arguments", "0.0.0")
final class ToxNewExceptionTest extends ToxCoreTestBase {

  @Test
  def testToxNewProxyNull(): Unit = {
    try {
      newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5(null, 1)).close()
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code)
    }
  }

  @Test
  def testToxNewProxyEmpty(): Unit = {
    try {
      newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5("", 1)).close()
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code)
    }
  }

  @Test
  def testToxNewProxyBadPort0(): Unit = {
    try {
      newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5("localhost", 0)).close()
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.code)
    }
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testToxNewProxyBadPortNegative(): Unit = {
    newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5("localhost", -10)).close()
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testToxNewProxyBadPortTooLarge(): Unit = {
    newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5("localhost", 0x10000)).close()
  }

  @Test
  def testToxNewProxyBadAddress1(): Unit = {
    try {
      // scalastyle:ignore non.ascii.character.disallowed
      newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5("\u2639", 1)).close()
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code)
    }
  }

  @Test
  def testToxNewProxyBadAddress2(): Unit = {
    try {
      newTox(ipv6Enabled = true, udpEnabled = true, new ProxyOptions.Socks5(".", 1)).close()
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code)
    }
  }

  @Test
  def testTooManyToxCreations(): Unit = {
    try {
      val toxes = (0 until 102) map (_ => newTox())
      toxes.foreach(_.close())
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.PORT_ALLOC, e.code)
    }
  }

  @Test
  def testLoadEncrypted(): Unit = {
    try {
      val tox: ToxCore[Unit] = newTox("toxEsave blah blah blah".getBytes)
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.LOAD_ENCRYPTED, e.code)
    }
  }

  @Test
  def testLoadBadFormat(): Unit = {
    try {
      val tox: ToxCore[Unit] = newTox("blah blah blah".getBytes)
      fail()
    } catch {
      case e: ToxNewException =>
        assertEquals(ToxNewException.Code.LOAD_BAD_FORMAT, e.code)
    }
  }

}
